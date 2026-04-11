package org.areslib.core;

import java.util.ArrayList;
import java.util.List;

/**
 * A Data Transfer Object (DTO) containing recorded teleop macro sequences. Can be serialized and
 * deserialized cleanly to/from JSON.
 */
public class GhostData {

  /** The loop period the recording was taken at, usually 0.02s (50Hz). */
  public double periodSeconds;

  /** Lists of recorded velocity inputs. */
  public List<Double> vxMetersPerSecond = new ArrayList<>();

  public List<Double> vyMetersPerSecond = new ArrayList<>();
  public List<Double> omegaRadiansPerSecond = new ArrayList<>();

  /**
   * Bitmask arrays storing binary inputs (e.g., bumpers, triggers, buttons). Bit 0: Button 0, Bit
   * 1: Button 1, etc.
   */
  public List<Integer> buttonMasks = new ArrayList<>();

  public GhostData() {
    // Required for Gson
  }

  public GhostData(double periodSeconds) {
    this.periodSeconds = periodSeconds;
  }

  /**
   * Appends a new timestamped frame to the macro recording.
   *
   * @param vx X velocity (forward).
   * @param vy Y velocity (strafe).
   * @param omega Omega velocity (turn).
   * @param mask Bitmask of pressed boolean buttons.
   */
  public void pushFrame(double vx, double vy, double omega, int mask) {
    vxMetersPerSecond.add(vx);
    vyMetersPerSecond.add(vy);
    omegaRadiansPerSecond.add(omega);
    buttonMasks.add(mask);
  }

  /**
   * Validates the integrity of the recording data. All parallel lists must have equal size.
   *
   * @return {@code true} if the data is structurally valid.
   * @throws IllegalStateException if lists have mismatched sizes.
   */
  public boolean validate() {
    int expectedSize = vxMetersPerSecond.size();
    if (vyMetersPerSecond.size() != expectedSize
        || omegaRadiansPerSecond.size() != expectedSize
        || buttonMasks.size() != expectedSize) {
      throw new IllegalStateException(
          "GhostData integrity failure: list sizes mismatch — vx="
              + expectedSize
              + " vy="
              + vyMetersPerSecond.size()
              + " omega="
              + omegaRadiansPerSecond.size()
              + " masks="
              + buttonMasks.size());
    }
    return true;
  }

  /**
   * Returns the number of recorded frames.
   *
   * @return The frame count.
   */
  public int getFrameCount() {
    return vxMetersPerSecond.size();
  }
}
