package org.areslib.math.kinematics;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Twist2d;

public class MecanumDriveOdometry {
  private final MecanumDriveKinematics kinematics;
  private Pose2d pose;
  private final Rotation2d previousAngle = new Rotation2d();
  private final MecanumDriveWheelPositions previousWheelPositions =
      new MecanumDriveWheelPositions();
  private final Twist2d twistCache = new Twist2d();

  public MecanumDriveOdometry(
      MecanumDriveKinematics kinematics,
      Rotation2d gyroAngle,
      MecanumDriveWheelPositions wheelPositions,
      Pose2d initialPose) {
    this.kinematics = kinematics;
    this.pose = initialPose;
    this.previousAngle.set(gyroAngle);
    this.previousWheelPositions.set(wheelPositions);
  }

  public MecanumDriveOdometry(
      MecanumDriveKinematics kinematics,
      Rotation2d gyroAngle,
      MecanumDriveWheelPositions wheelPositions) {
    this(kinematics, gyroAngle, wheelPositions, new Pose2d());
  }

  public void resetPosition(
      Rotation2d gyroAngle, MecanumDriveWheelPositions wheelPositions, Pose2d pose) {
    this.pose.set(pose);
    this.previousAngle.set(gyroAngle);
    this.previousWheelPositions.set(wheelPositions);
  }

  /**
   * Resets the robot's pose translation without disrupting internal kinematic wheel buffers.
   * Necessary for Vision Estimators.
   *
   * @param pose The new pose of the robot.
   */
  public void resetTranslation(Pose2d pose) {
    this.pose = pose;
  }

  public Pose2d getPose() {
    return pose;
  }

  public Pose2d update(Rotation2d gyroAngle, MecanumDriveWheelPositions wheelPositions) {
    kinematics.toTwist2d(previousWheelPositions, wheelPositions, twistCache);

    // Override the kinematic twist with the gyro delta for accuracy
    twistCache.dtheta = gyroAngle.minus(previousAngle).getRadians();

    pose.exp(twistCache, pose);

    previousAngle.set(gyroAngle);
    previousWheelPositions.set(wheelPositions);

    return pose;
  }
}
