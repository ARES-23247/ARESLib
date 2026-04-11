package org.areslib.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.kinematics.ChassisSpeeds;
import org.junit.jupiter.api.Test;

public class EliteShooterMathTest {

  @Test
  public void testStaticShot() {
    Pose2d currentPose = new Pose2d(0.0, 0.0, new Rotation2d());
    ChassisSpeeds currentSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);

    double vShot = 20.0;

    EliteShooterMath.EliteShooterSetpoint result =
        EliteShooterMath.calculateShotOnTheMove(
            currentPose, currentSpeeds, 5.0, 0.0, 0.0, 0.0, vShot, 9.81, 0.0);

    // Aim yaw should be 0 radians (straight ahead on X axis)
    assertEquals(0.0, result.robotAimYawRadians, 1e-4);

    // Shot should be valid
    assertTrue(result.isValid);

    // Feedforwards should be 0 since the robot is stationary and target is aligned
    assertEquals(0.0, result.chassisAngularFeedforward, 1e-4);
    assertEquals(0.0, result.hoodFeedforward, 1e-4);
  }

  @Test
  public void testLateralMovementShot() {
    Pose2d currentPose = new Pose2d(0.0, 0.0, new Rotation2d());
    ChassisSpeeds currentSpeeds = new ChassisSpeeds(0.0, 2.0, 0.0);

    double vShot = 20.0;

    EliteShooterMath.EliteShooterSetpoint result =
        EliteShooterMath.calculateShotOnTheMove(
            currentPose, currentSpeeds, 5.0, 0.0, 0.0, 0.0, vShot, 9.81, 0.0);

    // Because the robot is moving left(+Y), the virtual target must be shifted right(-Y) to
    // compensate
    // Thus the aim yaw should be negative
    assertTrue(
        result.robotAimYawRadians < 0.0, "Aim yaw must compensate for lateral inherited velocity");
  }

  @Test
  public void testFeedforwardCalculation() {
    Pose2d currentPose = new Pose2d(0.0, 0.0, new Rotation2d());
    ChassisSpeeds currentSpeeds = new ChassisSpeeds(2.0, 0.0, 0.0);

    double vShot = 20.0;

    EliteShooterMath.EliteShooterSetpoint result =
        EliteShooterMath.calculateShotOnTheMove(
            currentPose, currentSpeeds, 5.0, 5.0, 0.0, 0.0, vShot, 9.81, 0.0);

    // There should be a non-zero chassis angular feedforward
    assertTrue(
        Math.abs(result.chassisAngularFeedforward) > 0.0,
        "Chassis angular FF must be non-zero when driving laterally relative to target");
  }
}
