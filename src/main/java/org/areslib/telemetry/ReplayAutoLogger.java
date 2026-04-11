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
  @SuppressWarnings("unused")
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

  private static WpiLogReader reader = null;
  private static long currentMockTimestampMicroSec = 0;

  /** Loads the historical log file into memory for replay mode. */
  public static void loadLog(String filepath) {
    try {
      reader = new WpiLogReader(filepath);
    } catch (java.io.IOException e) {
      com.qualcomm.robotcore.util.RobotLog.e("ReplayAutoLogger failed: " + e.getMessage());
    }
  }

  /**
   * Advances the internal simulation clock. Call this at the start of periodic loops to maintain
   * synthetic determinism (decoupled from the real-world OS clock).
   */
  public static void setTimestamp(long mockTimeMicroSec) {
    currentMockTimestampMicroSec = mockTimeMicroSec;
  }

  /**
   * Called by the core AresAutoLogger ONLY when Replay Mode is active. Modifies the provided inputs
   * object fields using data fetched from the Log Reader.
   *
   * @param prefix Base directory string.
   * @param inputs The inputs object to overwrite.
   */
  @SuppressWarnings("unused")
  public static void processReplayInputs(String prefix, AresLoggableInputs inputs) {
    if (inputs == null || reader == null) return;

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
      Object val = reader.getLatestValue(entry.key, currentMockTimestampMicroSec);
      if (val != null) {
        try {
          if (entry.type == LogType.SWERVE_STATES && val instanceof double[]) {
            double[] dArr = (double[]) val;
            // Standard decoding of 2 doubles per state (angleRad, speedMps)
            int numStates = dArr.length / 2;
            SwerveModuleState[] states = new SwerveModuleState[numStates];
            for (int s = 0; s < numStates; s++) {
              states[s] =
                  new SwerveModuleState(
                      dArr[(s * 2) + 1], new org.areslib.math.geometry.Rotation2d(dArr[s * 2]));
            }
            entry.field.set(inputs, states);
          } else {
            entry.field.set(inputs, val);
          }
        } catch (Exception e) {
          // Ignore silent reflection cast exceptions on mismatches
        }
      }
    }
  }
}
