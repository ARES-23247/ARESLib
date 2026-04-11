package org.areslib.math.kinematics;

import org.areslib.math.geometry.Twist2d;

/**
 * Differential Drive Kinematics solver.
 *
 * <p>Hardened for zero-allocation in high-frequency loops.
 */
public class DifferentialDriveKinematics {
  /** The track width in meters representing the distance between the left and right wheels. */
  public final double trackWidthMeters;

  /**
   * Constructs a differential drive kinematics object.
   *
   * @param trackWidthMeters The track width of the drivetrain.
   */
  public DifferentialDriveKinematics(double trackWidthMeters) {
    this.trackWidthMeters = trackWidthMeters;
  }

  /**
   * Converts a chassis speed to individual wheel speeds.
   *
   * @param chassisSpeeds The chassis speeds.
   * @return The individual wheel speeds. (Allocates)
   */
  public DifferentialDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
    DifferentialDriveWheelSpeeds out = new DifferentialDriveWheelSpeeds();
    toWheelSpeeds(chassisSpeeds, out);
    return out;
  }

  /**
   * Converts a chassis speed to individual wheel speeds in-place.
   *
   * @param chassisSpeeds The chassis speeds.
   * @param out The object to populate with results.
   */
  public void toWheelSpeeds(ChassisSpeeds chassisSpeeds, DifferentialDriveWheelSpeeds out) {
    out.leftMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond
            - trackWidthMeters / 2.0 * chassisSpeeds.omegaRadiansPerSecond;
    out.rightMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond
            + trackWidthMeters / 2.0 * chassisSpeeds.omegaRadiansPerSecond;
  }

  /**
   * Converts individual wheel speeds to a single chassis speed.
   *
   * @param wheelSpeeds The individual wheel speeds.
   * @return The chassis speed. (Allocates)
   */
  public ChassisSpeeds toChassisSpeeds(DifferentialDriveWheelSpeeds wheelSpeeds) {
    ChassisSpeeds out = new ChassisSpeeds();
    toChassisSpeeds(wheelSpeeds, out);
    return out;
  }

  /**
   * Converts individual wheel speeds to a single chassis speed in-place.
   *
   * @param wheelSpeeds The individual wheel speeds.
   * @param out The object to populate with results.
   */
  public void toChassisSpeeds(DifferentialDriveWheelSpeeds wheelSpeeds, ChassisSpeeds out) {
    out.vxMetersPerSecond =
        (wheelSpeeds.leftMetersPerSecond + wheelSpeeds.rightMetersPerSecond) / 2.0;
    out.vyMetersPerSecond = 0.0;
    out.omegaRadiansPerSecond =
        (wheelSpeeds.rightMetersPerSecond - wheelSpeeds.leftMetersPerSecond) / trackWidthMeters;
  }

  /**
   * Converts a wheel position delta to a Twist2d. (Allocates)
   *
   * @param start The starting wheel positions.
   * @param end The ending wheel positions.
   * @return The twist over the interval.
   */
  public Twist2d toTwist2d(
      DifferentialDriveWheelPositions start, DifferentialDriveWheelPositions end) {
    Twist2d out = new Twist2d();
    toTwist2d(start, end, out);
    return out;
  }

  /**
   * Converts a wheel position delta to a Twist2d in-place.
   *
   * @param start The starting wheel positions.
   * @param end The ending wheel positions.
   * @param out The object to populate.
   */
  public void toTwist2d(
      DifferentialDriveWheelPositions start, DifferentialDriveWheelPositions end, Twist2d out) {
    double dl = end.leftMeters - start.leftMeters;
    double dr = end.rightMeters - start.rightMeters;

    out.dx = (dl + dr) / 2.0;
    out.dy = 0.0;
    out.dtheta = (dr - dl) / trackWidthMeters;
  }
}
