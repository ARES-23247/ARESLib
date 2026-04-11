package org.areslib.math.kinematics;

/**
 * MecanumDriveWheelSpeeds standard implementation.
 *
 * <p>Hardened for zero-allocation in high-frequency loops.
 */
public class MecanumDriveWheelSpeeds {
  public double frontLeftMetersPerSecond;
  public double frontRightMetersPerSecond;
  public double rearLeftMetersPerSecond;
  public double rearRightMetersPerSecond;

  public MecanumDriveWheelSpeeds() {}

  public MecanumDriveWheelSpeeds(
      double frontLeftMetersPerSecond,
      double frontRightMetersPerSecond,
      double rearLeftMetersPerSecond,
      double rearRightMetersPerSecond) {
    set(
        frontLeftMetersPerSecond,
        frontRightMetersPerSecond,
        rearLeftMetersPerSecond,
        rearRightMetersPerSecond);
  }

  /**
   * Sets the wheel speeds in-place.
   *
   * @param fl Front left speed (m/s).
   * @param fr Front right speed (m/s).
   * @param rl Rear left speed (m/s).
   * @param rr Rear right speed (m/s).
   */
  public void set(double fl, double fr, double rl, double rr) {
    this.frontLeftMetersPerSecond = fl;
    this.frontRightMetersPerSecond = fr;
    this.rearLeftMetersPerSecond = rl;
    this.rearRightMetersPerSecond = rr;
  }
}
