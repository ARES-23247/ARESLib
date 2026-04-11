package org.areslib.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.areslib.telemetry.AresAutoLogger;

/**
 * A runtime-tunable number backed by FTC Dashboard for live PID tuning.
 *
 * <p>Wraps a {@code double} value that can be changed live from the FTC Dashboard web interface
 * without redeploying code. The value is published to telemetry on creation and on each change,
 * allowing live monitoring and adjustment.
 *
 * <p><b>Per-Consumer Change Tracking:</b> Multiple consumers (e.g., a PID controller and a
 * feedforward model) can independently track whether they've seen the latest value using {@link
 * #hasChanged(int)}. Each consumer is identified by a unique integer ID.
 *
 * <p><b>Usage:</b>
 *
 * <pre>{@code
 * TunableNumber kP = new TunableNumber("Drive/kP", 0.1);
 * TunableNumber kD = new TunableNumber("Drive/kD", 0.01);
 *
 * // In periodic():
 * if (kP.hasChanged(hashCode())) {
 *     drivePid.setP(kP.get());
 * }
 * }</pre>
 */
@com.acmerobotics.dashboard.config.Config
public class TunableNumber {

  /**
   * FTC Dashboard Button Hook. Toggle to true in the dashboard web interface to trigger a
   * system-wide log dump sequence of all tuned constants. Resets to false automatically.
   */
  public static boolean dumpJsonNow = false;

  private final String key;
  private double value;
  private double defaultValue;

  /** Global registry of all instantiated tunable numbers for bulk exporting. */
  private static final List<TunableNumber> INSTANCES = new ArrayList<>();

  /** Per-consumer change tracking: maps consumer ID → last seen value. */
  private final Map<Integer, Double> lastHasChangedValues = new HashMap<>();

  /**
   * Constructs a TunableNumber with a dashboard key and default value.
   *
   * @param key The telemetry key used for display and editing (e.g., "Drive/kP").
   * @param defaultValue The initial value.
   */
  public TunableNumber(String key, double defaultValue) {
    this.key = key;
    value = defaultValue;
    this.defaultValue = defaultValue;

    // Publish the initial value
    AresAutoLogger.recordOutput("Tunables/" + key, value);

    synchronized (INSTANCES) {
      INSTANCES.add(this);
    }
  }

  /**
   * Gets the current value.
   *
   * @return The current tunable value.
   */
  public double get() {
    return value;
  }

  /**
   * Sets the value programmatically and publishes it.
   *
   * @param value The new value.
   */
  public void set(double value) {
    this.value = value;
    AresAutoLogger.recordOutput("Tunables/" + key, value);
  }

  /**
   * Returns true if the value has changed since the last time this specific consumer checked. Each
   * consumer is tracked independently by the provided ID.
   *
   * <p><b>Students:</b> Use {@code this.hashCode()} or a constant int as the consumer ID. This
   * allows multiple subsystems to independently detect tunable changes without interfering with
   * each other.
   *
   * @param id A unique identifier for the consumer (e.g., {@code this.hashCode()}).
   * @return Whether the current value differs from the last value seen by this consumer.
   */
  public boolean hasChanged(int id) {
    double currentValue = value;
    Double lastValue = lastHasChangedValues.get(id);
    if (lastValue == null || Math.abs(currentValue - lastValue) > 1e-12) {
      lastHasChangedValues.put(id, currentValue);
      return true;
    }
    return false;
  }

  /**
   * Returns the default value this tunable was constructed with.
   *
   * @return The default value.
   */
  public double getDefault() {
    return defaultValue;
  }

  /**
   * Returns the dashboard key for this tunable number.
   *
   * @return The key.
   */
  public String getKey() {
    return key;
  }

  /**
   * Generates a formatted JSON string of all current active Tunable constants. Useful for dumping
   * final tuned PID values and quickly copy-pasting them straight into your permanent source
   * Constants.java file.
   *
   * @return JSON formatted dump.
   */
  public static String dumpAllJson() {
    StringBuilder sb = new StringBuilder();
    sb.append("{\n");
    synchronized (INSTANCES) {
      for (int i = 0; i < INSTANCES.size(); i++) {
        TunableNumber t = INSTANCES.get(i);
        sb.append(String.format("  \"%s\": %.6f", t.getKey(), t.get()));
        if (i < INSTANCES.size() - 1) {
          sb.append(",\n");
        } else {
          sb.append("\n");
        }
      }
    }
    sb.append("}");
    return sb.toString();
  }

  @Override
  public String toString() {
    return String.format("TunableNumber(%s=%.4f)", key, value);
  }
}
