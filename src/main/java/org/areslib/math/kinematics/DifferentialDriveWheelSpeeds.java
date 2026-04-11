package org.areslib.math.kinematics;

/**
 * DifferentialDriveWheelSpeeds standard implementation.
 *
 * <p>Hardened for zero-allocation in high-frequency loops.
 */
public class DifferentialDriveWheelSpeeds {
  public double leftMetersPerSecond;
  public double rightMetersPerSecond;

  public DifferentialDriveWheelSpeeds() {}

  public DifferentialDriveWheelSpeeds(double leftMetersPerSecond, double rightMetersPerSecond) {
    set(leftMetersPerSecond, rightMetersPerSecond);
  }

  /**
   * Sets the wheel speeds in-place.
   *
   * @param leftMps Left side speed (m/s).
   * @param rightMps Right side speed (m/s).
   */
  public void set(double leftMps, double rightMps) {
    this.leftMetersPerSecond = leftMps;
    this.rightMetersPerSecond = rightMps;
  }
}
