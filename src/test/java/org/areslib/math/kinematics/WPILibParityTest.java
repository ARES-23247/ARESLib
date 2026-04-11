package org.areslib.math.kinematics;

import static org.junit.jupiter.api.Assertions.*;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Twist2d;
import org.junit.jupiter.api.Test;

class WPILibParityTest {
  private static final double EPSILON = 1e-9;

  @Test
  void testDiscretizeIdentity() {
    ChassisSpeeds result = new ChassisSpeeds();
    ChassisSpeeds.discretize(1.0, 1.0, 0.0, 0.02, result);
    assertEquals(1.0, result.vxMetersPerSecond, EPSILON);
    assertEquals(1.0, result.vyMetersPerSecond, EPSILON);
    assertEquals(0.0, result.omegaRadiansPerSecond, EPSILON);
  }

  @Test
  void testDiscretizeKnownCase() {
    // Case: Forward 1m/s, Half-turn/s, for 1s.
    // We want to end up at Pose(1, 0, PI/2)
    double vx = 1.0;
    double vy = 0.0;
    double omega = Math.PI / 2.0;
    double dt = 1.0;

    ChassisSpeeds result = new ChassisSpeeds();
    ChassisSpeeds.discretize(vx, vy, omega, dt, result);

    // Calculated manually: vx_d = PI/4, vy_d = -PI/4
    assertEquals(Math.PI / 4.0, result.vxMetersPerSecond, EPSILON);
    assertEquals(-Math.PI / 4.0, result.vyMetersPerSecond, EPSILON);
    assertEquals(omega, result.omegaRadiansPerSecond, EPSILON);

    // Check if applying exp to these discretized speeds gets us to (1, 0, PI/2)
    Twist2d twist =
        new Twist2d(
            result.vxMetersPerSecond * dt,
            result.vyMetersPerSecond * dt,
            result.omegaRadiansPerSecond * dt);
    Pose2d start = new Pose2d(0, 0, new Rotation2d(0));
    Pose2d end = start.exp(twist);

    assertEquals(1.0, end.getTranslation().getX(), EPSILON);
    assertEquals(0.0, end.getTranslation().getY(), EPSILON);
    assertEquals(Math.PI / 2.0, end.getRotation().getRadians(), EPSILON);
  }

  @Test
  void testPoseLogScaling() {
    // Check if Pose2d.log matches the inverse of exp
    Pose2d start = new Pose2d(0, 0, new Rotation2d(0));
    // Large rotation to emphasize scaling issues
    Pose2d end = new Pose2d(1.0, 0.5, new Rotation2d(Math.PI / 4.0));

    Twist2d twist = start.log(end);
    Pose2d roundTrip = start.exp(twist);

    assertEquals(end.getTranslation().getX(), roundTrip.getTranslation().getX(), EPSILON);
    assertEquals(end.getTranslation().getY(), roundTrip.getTranslation().getY(), EPSILON);
    assertEquals(end.getRotation().getRadians(), roundTrip.getRotation().getRadians(), EPSILON);
  }

  @Test
  void testDiscretizeMetersPerSecond() {
    // Test with typical loop times and small speeds
    double vx = 2.5;
    double vy = -1.2;
    double omega = 0.75;
    double dt = 0.02;

    ChassisSpeeds result = new ChassisSpeeds();
    ChassisSpeeds.discretize(vx, vy, omega, dt, result);

    // Verify round trip via exp
    Twist2d twist =
        new Twist2d(
            result.vxMetersPerSecond * dt,
            result.vyMetersPerSecond * dt,
            result.omegaRadiansPerSecond * dt);
    Pose2d start = new Pose2d(0, 0, new Rotation2d(0));
    Pose2d end = start.exp(twist);

    // The robot should end up at (vx*dt, vy*dt, omega*dt)
    assertEquals(vx * dt, end.getTranslation().getX(), EPSILON);
    assertEquals(vy * dt, end.getTranslation().getY(), EPSILON);
    assertEquals(omega * dt, end.getRotation().getRadians(), EPSILON);
  }
}
