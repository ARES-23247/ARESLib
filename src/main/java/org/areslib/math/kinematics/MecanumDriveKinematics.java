package org.areslib.math.kinematics;

import org.areslib.math.geometry.Translation2d;
import org.areslib.math.geometry.Twist2d;

/**
 * Mecanum Drive Kinematics solver.
 *
 * <p>Hardened for zero-allocation in high-frequency loops.
 */
public class MecanumDriveKinematics {
  private final double[][] inverseKinematics;
  private final double[][] forwardKinematics;

  public MecanumDriveKinematics(
      Translation2d frontLeftWheelMeters,
      Translation2d frontRightWheelMeters,
      Translation2d rearLeftWheelMeters,
      Translation2d rearRightWheelMeters) {

    inverseKinematics = new double[4][3];
    inverseKinematics[0][0] = 1;
    inverseKinematics[0][1] = -1;
    inverseKinematics[0][2] = -(frontLeftWheelMeters.getX() + frontLeftWheelMeters.getY());
    inverseKinematics[1][0] = 1;
    inverseKinematics[1][1] = 1;
    inverseKinematics[1][2] = frontRightWheelMeters.getX() - frontRightWheelMeters.getY();
    inverseKinematics[2][0] = 1;
    inverseKinematics[2][1] = 1;
    inverseKinematics[2][2] = rearLeftWheelMeters.getX() - rearLeftWheelMeters.getY();
    inverseKinematics[3][0] = 1;
    inverseKinematics[3][1] = -1;
    inverseKinematics[3][2] = -(rearRightWheelMeters.getX() + rearRightWheelMeters.getY());

    forwardKinematics = InverseMatrixHelper.pseudoInverse(inverseKinematics);
  }

  /**
   * Converts chassis speeds into wheel speeds.
   *
   * @param chassisSpeeds The robot-relative speeds.
   * @return The wheel speeds. (Allocates)
   */
  public MecanumDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
    MecanumDriveWheelSpeeds out = new MecanumDriveWheelSpeeds();
    toWheelSpeeds(chassisSpeeds, out);
    return out;
  }

  /**
   * Converts chassis speeds into wheel speeds in-place.
   *
   * @param chassisSpeeds The robot-relative speeds.
   * @param out The object to populate with results.
   */
  public void toWheelSpeeds(ChassisSpeeds chassisSpeeds, MecanumDriveWheelSpeeds out) {
    out.frontLeftMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond * inverseKinematics[0][0]
            + chassisSpeeds.vyMetersPerSecond * inverseKinematics[0][1]
            + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[0][2];
    out.frontRightMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond * inverseKinematics[1][0]
            + chassisSpeeds.vyMetersPerSecond * inverseKinematics[1][1]
            + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[1][2];
    out.rearLeftMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond * inverseKinematics[2][0]
            + chassisSpeeds.vyMetersPerSecond * inverseKinematics[2][1]
            + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[2][2];
    out.rearRightMetersPerSecond =
        chassisSpeeds.vxMetersPerSecond * inverseKinematics[3][0]
            + chassisSpeeds.vyMetersPerSecond * inverseKinematics[3][1]
            + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[3][2];
  }

  /**
   * Converts wheel speeds into chassis speeds.
   *
   * @param wheelSpeeds The individual wheel speeds.
   * @return The robot-relative speeds. (Allocates)
   */
  public ChassisSpeeds toChassisSpeeds(MecanumDriveWheelSpeeds wheelSpeeds) {
    ChassisSpeeds out = new ChassisSpeeds();
    toChassisSpeeds(wheelSpeeds, out);
    return out;
  }

  /**
   * Converts wheel speeds into chassis speeds in-place.
   *
   * @param wheelSpeeds The individual wheel speeds.
   * @param out The object to populate with results.
   */
  public void toChassisSpeeds(MecanumDriveWheelSpeeds wheelSpeeds, ChassisSpeeds out) {
    out.vxMetersPerSecond =
        forwardKinematics[0][0] * wheelSpeeds.frontLeftMetersPerSecond
            + forwardKinematics[0][1] * wheelSpeeds.frontRightMetersPerSecond
            + forwardKinematics[0][2] * wheelSpeeds.rearLeftMetersPerSecond
            + forwardKinematics[0][3] * wheelSpeeds.rearRightMetersPerSecond;

    out.vyMetersPerSecond =
        forwardKinematics[1][0] * wheelSpeeds.frontLeftMetersPerSecond
            + forwardKinematics[1][1] * wheelSpeeds.frontRightMetersPerSecond
            + forwardKinematics[1][2] * wheelSpeeds.rearLeftMetersPerSecond
            + forwardKinematics[1][3] * wheelSpeeds.rearRightMetersPerSecond;

    out.omegaRadiansPerSecond =
        forwardKinematics[2][0] * wheelSpeeds.frontLeftMetersPerSecond
            + forwardKinematics[2][1] * wheelSpeeds.frontRightMetersPerSecond
            + forwardKinematics[2][2] * wheelSpeeds.rearLeftMetersPerSecond
            + forwardKinematics[2][3] * wheelSpeeds.rearRightMetersPerSecond;
  }

  /**
   * Converts wheel position deltas into a Twist2d. (Allocates)
   *
   * @param start Initial positions.
   * @param end Final positions.
   * @return The twist over the interval.
   */
  public Twist2d toTwist2d(MecanumDriveWheelPositions start, MecanumDriveWheelPositions end) {
    Twist2d out = new Twist2d();
    toTwist2d(start, end, out);
    return out;
  }

  /**
   * Converts wheel position deltas into a Twist2d in-place.
   *
   * @param start Initial positions.
   * @param end Final positions.
   * @param out The object to populate.
   */
  public void toTwist2d(
      MecanumDriveWheelPositions start, MecanumDriveWheelPositions end, Twist2d out) {
    double dfl = end.frontLeftMeters - start.frontLeftMeters;
    double dfr = end.frontRightMeters - start.frontRightMeters;
    double drl = end.rearLeftMeters - start.rearLeftMeters;
    double drr = end.rearRightMeters - start.rearRightMeters;

    out.dx =
        forwardKinematics[0][0] * dfl
            + forwardKinematics[0][1] * dfr
            + forwardKinematics[0][2] * drl
            + forwardKinematics[0][3] * drr;

    out.dy =
        forwardKinematics[1][0] * dfl
            + forwardKinematics[1][1] * dfr
            + forwardKinematics[1][2] * drl
            + forwardKinematics[1][3] * drr;

    out.dtheta =
        forwardKinematics[2][0] * dfl
            + forwardKinematics[2][1] * dfr
            + forwardKinematics[2][2] * drl
            + forwardKinematics[2][3] * drr;
  }
}
