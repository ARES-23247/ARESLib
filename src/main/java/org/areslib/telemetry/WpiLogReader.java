package org.areslib.telemetry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Native Java parser for standard WPILog formatted files. Loads the entire historical timeline into
 * a chronological RAM cache on initialization.
 */
public class WpiLogReader {

  private static final int HEADER_SIZE_BYTES = 12;
  private static final int MAGIC_STRING_LENGTH = 6;
  private static final String MAGIC_STRING_EXPECTED = "WPILOG";
  private static final byte RECORD_DIRECT_MASK = 0x7F;

  public static class LogEntry {
    public final long timestamp;
    public final Object value;

    public LogEntry(long timestamp, Object value) {
      this.timestamp = timestamp;
      this.value = value;
    }
  }

  private final Map<Integer, SchemaInfo> idToSchema = new HashMap<>();
  private final Map<String, List<LogEntry>> historyCache = new HashMap<>();

  private static class SchemaInfo {
    final String key;
    final String type;

    SchemaInfo(String key, String type) {
      this.key = key;
      this.type = type;
    }
  }

  public WpiLogReader(String filepath) throws IOException {
    File file = new File(filepath);
    if (!file.exists()) {
      throw new IOException("WPILog file not found: " + filepath);
    }

    try (FileInputStream fis = new FileInputStream(file);
        FileChannel channel = fis.getChannel()) {

      long fileSize = channel.size();
      if (fileSize < HEADER_SIZE_BYTES) {
        throw new IOException("WPILog too small to contain header");
      }

      ByteBuffer buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
      buffer.order(ByteOrder.LITTLE_ENDIAN);

      // Validate header
      byte[] header = new byte[MAGIC_STRING_LENGTH];
      buffer.get(header);
      String magic = new String(header, StandardCharsets.UTF_8);
      if (!MAGIC_STRING_EXPECTED.equals(magic)) {
        throw new IOException("Invalid WPILOG magic header");
      }

      buffer.position(HEADER_SIZE_BYTES);

      while (buffer.hasRemaining()) {
        byte controlByte = buffer.get();
        if ((controlByte & RECORD_DIRECT_MASK) == RECORD_DIRECT_MASK) {
          // Standard WpiLogBackend 0x7F hardcoded format: 4-byte ID, 4-byte size, 8-byte timestamp
          int entryId = buffer.getInt();
          int payloadSize = buffer.getInt();
          long timestamp = buffer.getLong();
          if (buffer.remaining() < payloadSize) {
            break;
          }

          byte[] payload = new byte[payloadSize];
          buffer.get(payload);

          processRecord(entryId, payloadSize, timestamp, payload);
        } else {
          // General WPILog format parser
          int entryLen = (controlByte & 3) + 1;
          int sizeLen = ((controlByte >> 2) & 3) + 1;
          int timeLen = ((controlByte >> 4) & 7) + 1;

          int entryId = (int) readVarInt(buffer, entryLen);
          int payloadSize = (int) readVarInt(buffer, sizeLen);
          long timestamp = readVarInt(buffer, timeLen);
          if (buffer.remaining() < payloadSize) {
            break;
          }

          byte[] payload = new byte[payloadSize];
          buffer.get(payload);

          processRecord(entryId, payloadSize, timestamp, payload);
        }
      }
    }
  }

  private void processRecord(int entryId, int payloadSize, long timestamp, byte[] payload) {
    if (entryId == 0) {
      if (payload.length == 0) {
        return;
      }
      int controlType = payload[0];
      if (controlType == 0) { // Start Record
        ByteBuffer pBuf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
        pBuf.get(); // skip control type
        int targetId = pBuf.getInt();

        int nameLen = pBuf.getInt();
        byte[] nameBytes = new byte[nameLen];
        pBuf.get(nameBytes);
        String name = new String(nameBytes, StandardCharsets.UTF_8);

        int typeLen = pBuf.getInt();
        byte[] typeBytes = new byte[typeLen];
        pBuf.get(typeBytes);
        String type = new String(typeBytes, StandardCharsets.UTF_8);

        idToSchema.put(targetId, new SchemaInfo(name, type));
        historyCache.put(name, new ArrayList<>());
      }
    } else {
      SchemaInfo schema = idToSchema.get(entryId);
      if (schema == null) {
        return;
      }

      ByteBuffer pBuf = ByteBuffer.wrap(payload).order(ByteOrder.LITTLE_ENDIAN);
      Object parsedValue = null;

      switch (schema.type) {
        case "double":
          if (payloadSize == 8) {
            parsedValue = pBuf.getDouble();
          }
          break;
        case "int":
        case "int64":
          if (payloadSize == 8) {
            parsedValue = pBuf.getLong();
          } else if (payloadSize == 4) {
            parsedValue = pBuf.getInt();
          }
          break;
        case "boolean":
          if (payloadSize >= 1) {
            parsedValue = (pBuf.get() != 0);
          }
          break;
        case "string":
          parsedValue = new String(payload, StandardCharsets.UTF_8);
          break;
        case "double[]":
          double[] dArr = new double[payloadSize / 8];
          for (int i = 0; i < dArr.length; i++) {
            dArr[i] = pBuf.getDouble();
          }
          parsedValue = dArr;
          break;
        case "boolean[]":
          boolean[] bArr = new boolean[payloadSize];
          for (int i = 0; i < bArr.length; i++) {
            bArr[i] = (pBuf.get() != 0);
          }
          parsedValue = bArr;
          break;
        case "string[]":
          if (payloadSize >= 4) {
            int strCount = pBuf.getInt();
            String[] sArr = new String[strCount];
            for (int i = 0; i < strCount; i++) {
              if (pBuf.remaining() >= 4) {
                int len = pBuf.getInt();
                byte[] sBytes = new byte[len];
                pBuf.get(sBytes);
                sArr[i] = new String(sBytes, StandardCharsets.UTF_8);
              }
            }
            parsedValue = sArr;
          }
          break;
        default:
          // Raw struct data byte array
          parsedValue = payload;
          break;
      }

      if (parsedValue != null) {
        historyCache.get(schema.key).add(new LogEntry(timestamp, parsedValue));
      }
    }
  }

  private long readVarInt(ByteBuffer buffer, int bytes) {
    long result = 0;
    for (int i = 0; i < bytes; i++) {
      result |= ((long) (buffer.get() & 0xFF)) << (i * 8);
    }
    return result;
  }

  /**
   * Performs a Binary Search through the chronological arrays to find the record closest (but
   * before or exactly on) the mock timestamp target.
   */
  public Object getLatestValue(String key, long targetTimestamp) {
    List<LogEntry> entries = historyCache.get(key);
    if (entries == null || entries.isEmpty()) {
      return null;
    }

    int low = 0;
    int high = entries.size() - 1;
    Object bestMatch = null;

    while (low <= high) {
      int mid = (low + high) >>> 1;
      LogEntry midVal = entries.get(mid);

      if (midVal.timestamp <= targetTimestamp) {
        bestMatch = midVal.value;
        low = mid + 1;
      } else {
        high = mid - 1;
      }
    }
    return bestMatch;
  }
}
