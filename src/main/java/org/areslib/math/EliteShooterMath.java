package org.areslib.math;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.kinematics.ChassisSpeeds;

/**
 * Advanced Shot-On-The-Move mathematical solver ingested from Team 254 (2024).
 *
 * <p>Calculates exact trajectory kinematics, solving the quadratic equation for time-of-flight
 * while applying gravity and lift compensation. Adapted from FRC to FTC game piece physics.
 *
 * <p>Students: This is used for aiming mechanisms that need to launch game pieces while the robot
 * is still moving. The math accounts for the robot's velocity so the game piece arrives at the
 * target despite the robot drifting.
 */
public class EliteShooterMath {

  /**
   * Data class for holding calculated shot parameters. Contains the exact angles and feedforwards
   * needed to hit the target while moving.
   */
  public static class EliteShooterSetpoint {
    /** The yaw angle (in radians) the robot should face to aim at the virtual target. */
    public double robotAimYawRadians;

    /** Angular velocity feedforward (rad/s) to add to the chassis rotation controller. */
    public double chassisAngularFeedforward;

    /** The pitch/hood angle (radians from horizontal) for the launcher. */
    public double hoodRadians;

    /** Hood angular velocity feedforward to compensate for robot motion. */
    public double hoodFeedforward;

    /** The adjusted launch speed (m/s) after gravity and lift compensation. */
    public double launchSpeedMetersPerSec;

    /** Whether the solution is physically valid (positive time-of-flight). */
    public boolean isValid;

    /** Resets the setpoint to an invalid state. */
    public void reset() {
      robotAimYawRadians = 0;
      chassisAngularFeedforward = 0;
      hoodRadians = 0;
      hoodFeedforward = 0;
      launchSpeedMetersPerSec = 0;
      isValid = false;
    }
  }

  /**
   * Mathematically solves the exact shot state needed to hit a 3D target given current robot speeds
   * and constraints.
   *
   * <p><strong>Mathematical Reference:</strong> Trajectory compensation solves for {@code t} in:
   * {@code ||P_target - (P_robot + V_robot*t)|| = V_shot * t}
   *
   * @param robotPose Current field-relative robot pose.
   * @param fieldRelativeSpeeds Current field-relative speeds of the chassis.
   * @param targetXMeters The X coordinate of the target on the field (meters).
   * @param targetYMeters The Y coordinate of the target on the field (meters).
   * @param targetZMeters The Z coordinate (height) of the target on the field (meters).
   * @param releaseHeightZMeters Height of the robot's shooter mechanism from the floor (meters).
   * @param nominalShotSpeedMetersPerSec Base shot velocity output limit.
   * @param gravityMetersPerSecSq Gravity constant (typically 9.81).
   * @param liftCoefficient Aerodynamic lift coefficient of the game piece (0 for no lift).
   * @param result Setpoint object to populate (Zero-allocation pattern).
   */
  public static void calculateShotOnTheMove(
      Pose2d robotPose,
      ChassisSpeeds fieldRelativeSpeeds,
      double targetXMeters,
      double targetYMeters,
      double targetZMeters,
      double releaseHeightZMeters,
      double nominalShotSpeedMetersPerSec,
      double gravityMetersPerSecSq,
      double liftCoefficient,
      EliteShooterSetpoint result) {

    // Vector from robot to target
    double deltaXMeters = targetXMeters - robotPose.getTranslation().getX();
    double deltaYMeters = targetYMeters - robotPose.getTranslation().getY();
    double deltaZMeters = targetZMeters - releaseHeightZMeters;

    double velocityRobotXMetersPerSec = fieldRelativeSpeeds.vxMetersPerSecond;
    double velocityRobotYMetersPerSec = fieldRelativeSpeeds.vyMetersPerSecond;

    // Solve quadratic equation for time-of-flight:
    // a = vx^2 + vy^2 - vShot^2
    // b = -2 * (dx * vx + dy * vy)
    // c = dx^2 + dy^2 + dz^2
    double coeffsA =
        velocityRobotXMetersPerSec * velocityRobotXMetersPerSec
            + velocityRobotYMetersPerSec * velocityRobotYMetersPerSec
            - nominalShotSpeedMetersPerSec * nominalShotSpeedMetersPerSec;

    if (Math.abs(coeffsA) < 1e-6) {
      // Adjust to avoid division by zero / non-quadratic states
      nominalShotSpeedMetersPerSec = 1.01 * nominalShotSpeedMetersPerSec;
      coeffsA =
          velocityRobotXMetersPerSec * velocityRobotXMetersPerSec
              + velocityRobotYMetersPerSec * velocityRobotYMetersPerSec
              - nominalShotSpeedMetersPerSec * nominalShotSpeedMetersPerSec;
    }

    double coeffsB =
        -2.0
            * (deltaXMeters * velocityRobotXMetersPerSec
                + deltaYMeters * velocityRobotYMetersPerSec);
    double coeffsC =
        deltaXMeters * deltaXMeters + deltaYMeters * deltaYMeters + deltaZMeters * deltaZMeters;

    double discriminant = coeffsB * coeffsB - 4.0 * coeffsA * coeffsC;
    if (discriminant < 0.0) {
      discriminant = 0.0;
    }

    // Solve for time of flight
    double timeOfFlightSeconds = (-coeffsB - Math.sqrt(discriminant)) / (2.0 * coeffsA);

    if (timeOfFlightSeconds <= 0) {
      result.reset();
      return;
    }

    // Virtual shot vector: where the ball needs to go relative to robot
    double virtualShotXMetersPerSec =
        (deltaXMeters - velocityRobotXMetersPerSec * timeOfFlightSeconds) / timeOfFlightSeconds;
    double virtualShotYMetersPerSec =
        (deltaYMeters - velocityRobotYMetersPerSec * timeOfFlightSeconds) / timeOfFlightSeconds;

    double virtualTargetYawRad = Math.atan2(virtualShotYMetersPerSec, virtualShotXMetersPerSec);
    double xyVelocityMetersPerSec =
        Math.sqrt(
            virtualShotXMetersPerSec * virtualShotXMetersPerSec
                + virtualShotYMetersPerSec * virtualShotYMetersPerSec);

    // Apply gravity and lift compensation
    // Corrected: Aim UP by 'drop' to offset gravity pulling the ball down.
    double gravityDropMeters =
        0.5 * timeOfFlightSeconds * timeOfFlightSeconds * gravityMetersPerSecSq;
    gravityDropMeters += 0.5 * liftCoefficient * coeffsC;

    double pitchAngleRad =
        Math.atan2(
            (deltaZMeters + gravityDropMeters) / timeOfFlightSeconds, xyVelocityMetersPerSec);
    double adjustedLaunchSpeedMps =
        Math.sqrt(
            Math.pow((deltaZMeters + gravityDropMeters) / timeOfFlightSeconds, 2)
                + xyVelocityMetersPerSec * xyVelocityMetersPerSec);

    // Compute Chassis Aim and Feedforward (use distanceSquared directly — avoids sqrt only to
    // re-square)
    double distanceSquaredMeters = deltaXMeters * deltaXMeters + deltaYMeters * deltaYMeters;

    double chassisAngularFF =
        (deltaYMeters * velocityRobotXMetersPerSec - deltaXMeters * velocityRobotYMetersPerSec)
            / distanceSquaredMeters;

    // Project robot velocity into the target frame for hood feed-forward
    double cosYaw = Math.cos(virtualTargetYawRad);
    double sinYaw = Math.sin(virtualTargetYawRad);
    double projectedXVelocityMps =
        velocityRobotXMetersPerSec * cosYaw + velocityRobotYMetersPerSec * sinYaw;

    double hoodFF =
        projectedXVelocityMps
            * -deltaZMeters
            / (distanceSquaredMeters + deltaZMeters * deltaZMeters);

    result.robotAimYawRadians = virtualTargetYawRad;
    result.chassisAngularFeedforward = chassisAngularFF;
    result.hoodRadians = pitchAngleRad;
    result.hoodFeedforward = hoodFF;
    result.launchSpeedMetersPerSec = adjustedLaunchSpeedMps;
    result.isValid = true;
  }
}
