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
    EliteShooterMath.EliteShooterSetpoint result = new EliteShooterMath.EliteShooterSetpoint();

    EliteShooterMath.calculateShotOnTheMove(
        currentPose, currentSpeeds, 5.0, 0.0, 0.0, 0.0, vShot, 9.81, 0.0, result);

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
    EliteShooterMath.EliteShooterSetpoint result = new EliteShooterMath.EliteShooterSetpoint();

    EliteShooterMath.calculateShotOnTheMove(
        currentPose, currentSpeeds, 5.0, 0.0, 0.0, 0.0, vShot, 9.81, 0.0, result);

    // Because the robot is moving left(+Y), the virtual target must be shifted right(-Y) to
    // compensate
    // Thus the aim yaw should be negative
    assertTrue(
        result.robotAimYawRadians < 0.0, "Aim yaw must compensate for lateral inherited velocity");
  }

  @Test
  public void testGravityCompensation() {
    Pose2d currentPose = new Pose2d(0.0, 0.0, new Rotation2d());
    ChassisSpeeds currentSpeeds = new ChassisSpeeds(0.0, 0.0, 0.0);

    double vShot = 10.0;
    double gravity = 9.81;
    double distance = 5.0;
    double targetZ = 0.0;
    double releaseZ = 0.0;

    EliteShooterMath.EliteShooterSetpoint result = new EliteShooterMath.EliteShooterSetpoint();
    EliteShooterMath.calculateShotOnTheMove(
        currentPose, currentSpeeds, distance, 0.0, targetZ, releaseZ, vShot, gravity, 0.0, result);

    // Ball falls 0.5 * 9.81 * (0.5^2) = 1.22625 meters.
    // To hit a target at release height, it must aim UP.
    assertTrue(result.hoodRadians > 0, "Must aim UP to compensate for gravity");

    // Exact Vz required = (dz + drop) / t = (0 + 1.22625) / 0.5 = 2.4525
    // atan2(2.4525, 10) approx 13.77 degrees
    assertEquals(Math.atan2(2.4525, 10), result.hoodRadians, 1e-2);
  }

  @Test
  public void testFeedforwardCalculation() {
    Pose2d currentPose = new Pose2d(0.0, 0.0, new Rotation2d());
    ChassisSpeeds currentSpeeds = new ChassisSpeeds(2.0, 0.0, 0.0);

    double vShot = 20.0;
    EliteShooterMath.EliteShooterSetpoint result = new EliteShooterMath.EliteShooterSetpoint();

    EliteShooterMath.calculateShotOnTheMove(
        currentPose, currentSpeeds, 5.0, 5.0, 0.0, 0.0, vShot, 9.81, 0.0, result);

    // There should be a non-zero chassis angular feedforward
    assertTrue(
        Math.abs(result.chassisAngularFeedforward) > 0.0,
        "Chassis angular FF must be non-zero when driving laterally relative to target");
  }
}
