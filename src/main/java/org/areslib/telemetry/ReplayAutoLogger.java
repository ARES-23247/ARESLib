package org.areslib.telemetry;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.areslib.math.kinematics.SwerveModuleState;

/**
 * Replay-Mode AdvantageKit-Style Logger.
 *
 * <p>During replay mode, instead of reading physical subsystems and pushing them to AdvantageScope,
 * we deserialize historical WPILogs and push the historical data back into the Java subsystem
 * Inputs via Reflection.
 */
public class ReplayAutoLogger {

  /** Unified entry for a single field being restored. */
  private static class ReplayEntry {
    final Field field;
    final String key;
    final LogType type;

    ReplayEntry(Field field, String key) {
      this.field = field;
      this.key = key;
      this.type = LogType.fromClass(field.getType());
      this.field.setAccessible(true);
    }
  }

  private enum LogType {
    DOUBLE,
    INT,
    BOOLEAN,
    STRING,
    DOUBLE_ARRAY,
    SWERVE_STATES,
    UNKNOWN;

    static LogType fromClass(Class<?> clazz) {
      if (clazz == double.class || clazz == Double.class) return DOUBLE;
      if (clazz == int.class || clazz == Integer.class) return INT;
      if (clazz == boolean.class || clazz == Boolean.class) return BOOLEAN;
      if (clazz == String.class) return STRING;
      if (clazz == double[].class) return DOUBLE_ARRAY;
      if (clazz == SwerveModuleState[].class) return SWERVE_STATES;
      return UNKNOWN;
    }
  }

  /** Cache of entries per prefix+class combination. */
  private static final Map<String, List<ReplayEntry>> ENTRY_CACHE = new ConcurrentHashMap<>();

  /**
   * Called by the core AresAutoLogger ONLY when Replay Mode is active. Modifies the provided inputs
   * object fields using data fetched from the Log Reader.
   *
   * @param prefix Base directory string.
   * @param inputs The inputs object to overwrite.
   */
  public static void processReplayInputs(String prefix, AresLoggableInputs inputs) {
    if (inputs == null) return;

    // TODO: This would query the WpiLogReader for the current mock timestamp's entries.
    // Map<String, Object> tickData =
    // WpiLogReader.getInstance().getTickData(AresTimer.getFPGATimestamp());

    String cacheKey = prefix + "_" + inputs.getClass().getName();
    List<ReplayEntry> entries =
        ENTRY_CACHE.computeIfAbsent(
            cacheKey,
            k -> {
              List<ReplayEntry> list = new ArrayList<>();
              Field[] fields = inputs.getClass().getDeclaredFields();
              for (Field f : fields) {
                list.add(new ReplayEntry(f, prefix + "/" + f.getName()));
              }
              return list;
            });

    for (int i = 0; i < entries.size(); i++) {
      ReplayEntry entry = entries.get(i);
      try {
        // Mock integration hook:
        // Object historicalData = tickData.get(entry.key);
        // if (historicalData == null) continue;

        // Example assignment:
        // entry.field.set(inputs, historicalData);

        // Real deserialization requires type casting matching the WPILog format.
        // Leaving hook available for WPILogReader parity.
      } catch (Exception e) {
        // Skip reflection failures
      }
    }
  }
}
