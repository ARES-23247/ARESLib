package org.areslib.math.kinematics;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Translation2d;

/**
 * Shoot-On-The-Move (SOTM) Solver.
 *
 * <p>Provides kinematic algorithms to predict virtual target locations based on robot velocity,
 * target position, and projectile flight time.
 */
public class SOTMSolver {

  /**
   * Calculates the required turret/chassis yaw to hit a target while moving. Based on the Team 254
   * / 1690 SOTM approach, which calculates the target's "virtual" position relative to the robot's
   * future position.
   *
   * @param robotPose Current global pose of the robot.
   * @param fieldRelativeSpeeds Current field-relative velocity of the robot (x/y in meters/sec).
   * @param targetPose Global pose of the center of the target (e.g. Speaker).
   * @param projectileSpeedMps The velocity of the game piece leaving the shooter in meters per
   *     second.
   * @return A Rotation2d representing the global heading the robot/turret must face to hit the
   *     target.
   */
  public static Rotation2d getVirtualTargetYaw(
      Pose2d robotPose,
      Translation2d fieldRelativeSpeeds,
      Pose2d targetPose,
      double projectileSpeedMps) {

    // Calculate lateral distance to the actual target
    double distanceMeters = robotPose.getTranslation().getDistance(targetPose.getTranslation());

    // Approximate flight time (d = v * t). For a more advanced solver, this should factor in
    // gravity/drag.
    double expectedFlightTime = distanceMeters / projectileSpeedMps;

    // Based on our velocity, where will the robot be when the projectile arrives?
    // Equivalently: how far does the target "appear" to move relative to us?
    double virtualRobotX = robotPose.getX() + (fieldRelativeSpeeds.getX() * expectedFlightTime);
    double virtualRobotY = robotPose.getY() + (fieldRelativeSpeeds.getY() * expectedFlightTime);

    // The angle from our VIRTUAL future position to the REAL target position
    // is the same as shooting from our CURRENT position to the VIRTUAL target position.
    double deltaX = targetPose.getX() - virtualRobotX;
    double deltaY = targetPose.getY() - virtualRobotY;

    return new Rotation2d(Math.atan2(deltaY, deltaX));
  }
}
