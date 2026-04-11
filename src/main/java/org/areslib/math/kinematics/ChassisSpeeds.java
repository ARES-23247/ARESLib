package org.areslib.math.kinematics;

import org.areslib.math.geometry.Rotation2d;

/** Represents the speed of a robot chassis. */
public class ChassisSpeeds {
  /** The velocity in the X direction (meters per second). */
  public double vxMetersPerSecond;

  /** The velocity in the Y direction (meters per second). */
  public double vyMetersPerSecond;

  /** The angular velocity (radians per second). */
  public double omegaRadiansPerSecond;

  public ChassisSpeeds() {}

  public ChassisSpeeds(
      double vxMetersPerSecond, double vyMetersPerSecond, double omegaRadiansPerSecond) {
    this.vxMetersPerSecond = vxMetersPerSecond;
    this.vyMetersPerSecond = vyMetersPerSecond;
    this.omegaRadiansPerSecond = omegaRadiansPerSecond;
  }

  /**
   * Sets the velocity components.
   *
   * @param vx Forward velocity (m/s).
   * @param vy Strafe velocity (m/s).
   * @param omega Angular velocity (rad/s).
   * @return This object for chaining.
   */
  public ChassisSpeeds set(double vx, double vy, double omega) {
    this.vxMetersPerSecond = vx;
    this.vyMetersPerSecond = vy;
    this.omegaRadiansPerSecond = omega;
    return this;
  }

  /**
   * Sets this object from field-relative speeds.
   *
   * @param vx Field-relative X (m/s).
   * @param vy Field-relative Y (m/s).
   * @param omega Angular velocity (rad/s).
   * @param robotAngle Current robot heading.
   * @return This object for chaining.
   */
  public ChassisSpeeds fromFieldRelative(
      double vx, double vy, double omega, Rotation2d robotAngle) {
    double cos = robotAngle.getCos();
    double sin = robotAngle.getSin();
    this.vxMetersPerSecond = vx * cos + vy * sin;
    this.vyMetersPerSecond = -vx * sin + vy * cos;
    this.omegaRadiansPerSecond = omega;
    return this;
  }

  public static ChassisSpeeds fromFieldRelativeSpeeds(
      double vxMetersPerSecond,
      double vyMetersPerSecond,
      double omegaRadiansPerSecond,
      Rotation2d robotAngle) {
    return new ChassisSpeeds()
        .fromFieldRelative(vxMetersPerSecond, vyMetersPerSecond, omegaRadiansPerSecond, robotAngle);
  }

  /**
   * Discretizes a continuous-time chassis speed into an equivalent discrete speed for a loop cycle.
   *
   * @param vxMetersPerSecond Forward velocity.
   * @param vyMetersPerSecond Sideways velocity.
   * @param omegaRadiansPerSecond Angular velocity.
   * @param dtSeconds The duration of the control loop.
   * @param result Object to populate with discretized speeds.
   */
  public static void discretize(
      double vxMetersPerSecond,
      double vyMetersPerSecond,
      double omegaRadiansPerSecond,
      double dtSeconds,
      ChassisSpeeds result) {

    if (dtSeconds <= 0.0) {
      result.set(vxMetersPerSecond, vyMetersPerSecond, omegaRadiansPerSecond);
      return;
    }

    // If omega is negligible, discretization is identity
    if (Math.abs(omegaRadiansPerSecond) < 1e-9) {
      result.set(vxMetersPerSecond, vyMetersPerSecond, omegaRadiansPerSecond);
      return;
    }

    // Exact pose logarithm mapping (the inverse of what we had)
    double theta = omegaRadiansPerSecond * dtSeconds;
    double halfTheta = 0.5 * theta;
    double cosMinusOne = Math.cos(theta) - 1.0;

    double halfThetaByTanOfHalfTheta;
    if (Math.abs(cosMinusOne) < 1e-9) {
      halfThetaByTanOfHalfTheta = 1.0 - 1.0 / 12.0 * theta * theta;
    } else {
      halfThetaByTanOfHalfTheta = -(halfTheta * Math.sin(theta)) / cosMinusOne;
    }

    // Log calculation involves rotating the desired translation to find the equivalent twist
    // x = halfTheta * cot(halfTheta), y = -halfTheta
    double x = halfThetaByTanOfHalfTheta;
    double y = -halfTheta;

    // Apply rotation matrix:
    // newVx = vx * x - vy * y
    // newVy = vx * y + vy * x
    double resultVx = vxMetersPerSecond * x - vyMetersPerSecond * y;
    double resultVy = vxMetersPerSecond * y + vyMetersPerSecond * x;

    result.set(resultVx, resultVy, omegaRadiansPerSecond);
  }

  public static ChassisSpeeds discretize(ChassisSpeeds continuousSpeeds, double dtSeconds) {
    ChassisSpeeds result = new ChassisSpeeds();
    discretize(
        continuousSpeeds.vxMetersPerSecond,
        continuousSpeeds.vyMetersPerSecond,
        continuousSpeeds.omegaRadiansPerSecond,
        dtSeconds,
        result);
    return result;
  }

  @Override
  public String toString() {
    return String.format(
        "ChassisSpeeds(Vx: %.2f m/s, Vy: %.2f m/s, Omega: %.2f rad/s)",
        vxMetersPerSecond, vyMetersPerSecond, omegaRadiansPerSecond);
  }
}
