package org.areslib.telemetry;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.areslib.math.kinematics.DifferentialDriveWheelSpeeds;
import org.areslib.math.kinematics.MecanumDriveWheelSpeeds;
import org.areslib.math.kinematics.SwerveModuleState;

/**
 * Global telemetry distribution hub. Routes log data to all registered backend implementations
 * (e.g., FtcDashboard, wpilog).
 */
public class AresTelemetry {
  private static final List<AresLoggerBackend> BACKENDS = new CopyOnWriteArrayList<>();

  /**
   * Registers a new telemetry backend to receive data. Uses class-based deduplication so that
   * re-registering the same backend type (e.g., across OpMode transitions) replaces the old
   * instance instead of duplicating.
   *
   * @param backend The backend implementation to register.
   */
  public static void registerBackend(AresLoggerBackend backend) {
    // Remove any existing backend of the same class to prevent accumulation
    BACKENDS.removeIf(existing -> existing.getClass().equals(backend.getClass()));
    BACKENDS.add(backend);
  }

  /**
   * Closes and removes all registered BACKENDS. Calls {@link AresLoggerBackend#close()} on each
   * backend to release file handles and sockets before clearing. Should be called during scheduler
   * reset to prevent stale backend accumulation across OpMode transitions.
   */
  public static void clearBackends() {
    for (AresLoggerBackend backend : BACKENDS) {
      try {
        backend.close();
      } catch (Exception e) {
        // Log but don't propagate — we're shutting down
        com.qualcomm.robotcore.util.RobotLog.e(
            "AresTelemetry: Error closing backend "
                + backend.getClass().getSimpleName()
                + ": "
                + e);
      }
    }
    BACKENDS.clear();
  }

  /**
   * Puts a number value into telemetry.
   *
   * @param key The telemetry key.
   * @param value The value.
   */
  public static void putNumber(String key, double value) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putNumber(key, value);
    }
  }

  /**
   * Puts an array of numbers into telemetry.
   *
   * @param key The telemetry key.
   * @param values The values array.
   */
  public static void putNumberArray(String key, double[] values) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putNumberArray(key, values);
    }
  }

  /**
   * Puts a string value into telemetry.
   *
   * @param key The telemetry key.
   * @param value The value.
   */
  public static void putString(String key, String value) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putString(key, value);
    }
  }

  /**
   * Puts a boolean value into telemetry.
   *
   * @param key The telemetry key.
   * @param value The value.
   */
  public static void putBoolean(String key, boolean value) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putBoolean(key, value);
    }
  }

  /**
   * Puts an array of booleans into telemetry.
   *
   * @param key The telemetry key.
   * @param values The values array.
   */
  public static void putBooleanArray(String key, boolean[] values) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putBooleanArray(key, values);
    }
  }

  /**
   * Puts an array of strings into telemetry.
   *
   * @param key The telemetry key.
   * @param values The values array.
   */
  public static void putStringArray(String key, String[] values) {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.putStringArray(key, values);
    }
  }

  /** Updates all registered BACKENDS. Should be called periodically. */
  public static void update() {
    for (AresLoggerBackend backend : BACKENDS) {
      backend.update();
    }
  }

  // Thread-local caches for zero-GC hot-path logging.
  // ThreadLocal ensures safety when the physics thread and main loop both log concurrently.
  private static final ThreadLocal<double[]> POSE2D_CACHE =
      ThreadLocal.withInitial(() -> new double[3]);
  private static final ThreadLocal<double[]> SWERVE_STATES_CACHE =
      ThreadLocal.withInitial(() -> new double[8]);
  private static final ThreadLocal<double[]> DIFF_SPEEDS_CACHE =
      ThreadLocal.withInitial(() -> new double[2]);
  private static final ThreadLocal<double[]> MECANUM_SPEEDS_CACHE =
      ThreadLocal.withInitial(() -> new double[4]);

  // Helper methods ported from the old AresLogger

  /**
   * Logs a Pose2d into telemetry as a double array.
   *
   * @param key The telemetry key.
   * @param xMeters The X position in meters.
   * @param yMeters The Y position in meters.
   * @param headingRadians The heading in radians.
   */
  public static void putPose2d(String key, double xMeters, double yMeters, double headingRadians) {
    double[] cache = POSE2D_CACHE.get();
    cache[0] = xMeters;
    cache[1] = yMeters;
    cache[2] = headingRadians;
    putNumberArray(key, cache);
  }

  /**
   * Logs exactly 4 SwerveModuleState elements as a double array in AdvantageScope format.
   *
   * @param key The telemetry key.
   * @param states Array of 4 swerve module states.
   */
  public static void logSwerveStates(String key, SwerveModuleState[] states) {
    if (states.length != 4) return;
    double[] cache = SWERVE_STATES_CACHE.get();
    for (int i = 0; i < 4; i++) {
      cache[i * 2] = states[i].angle.getRadians(); // Angle first for AdvantageScope
      cache[i * 2 + 1] = states[i].speedMetersPerSecond;
    }
    putNumberArray(key, cache);
  }

  /**
   * Logs differential drive speeds as an array for AdvantageScope.
   *
   * @param key The telemetry key.
   * @param speeds The wheel speeds.
   */
  public static void logDifferentialSpeeds(String key, DifferentialDriveWheelSpeeds speeds) {
    double[] cache = DIFF_SPEEDS_CACHE.get();
    cache[0] = speeds.leftMetersPerSecond;
    cache[1] = speeds.rightMetersPerSecond;
    putNumberArray(key, cache);
  }

  /**
   * Logs mecanum drive speeds as an array for AdvantageScope.
   *
   * @param key The telemetry key.
   * @param speeds The wheel speeds.
   */
  public static void logMecanumSpeeds(String key, MecanumDriveWheelSpeeds speeds) {
    double[] cache = MECANUM_SPEEDS_CACHE.get();
    cache[0] = speeds.frontLeftMetersPerSecond;
    cache[1] = speeds.frontRightMetersPerSecond;
    cache[2] = speeds.rearLeftMetersPerSecond;
    cache[3] = speeds.rearRightMetersPerSecond;
    putNumberArray(key, cache);
  }
}
