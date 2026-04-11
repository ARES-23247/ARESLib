package org.areslib.math.estimator;

import static org.junit.jupiter.api.Assertions.*;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Translation2d;
import org.areslib.math.kinematics.SwerveDriveKinematics;
import org.areslib.math.kinematics.SwerveModulePosition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SwerveDrivePoseEstimatorTest {

  private static final double EPSILON = 1e-6;
  private SwerveDriveKinematics kinematics;
  private SwerveDrivePoseEstimator estimator;
  private SwerveModulePosition[] initialPositions;

  @BeforeEach
  void setUp() {
    kinematics =
        new SwerveDriveKinematics(
            new Translation2d(0.5, 0.5),
            new Translation2d(0.5, -0.5),
            new Translation2d(-0.5, 0.5),
            new Translation2d(-0.5, -0.5));

    initialPositions =
        new SwerveModulePosition[] {
          new SwerveModulePosition(0.0, new Rotation2d(0.0)),
          new SwerveModulePosition(0.0, new Rotation2d(0.0)),
          new SwerveModulePosition(0.0, new Rotation2d(0.0)),
          new SwerveModulePosition(0.0, new Rotation2d(0.0))
        };
    Pose2d initialPose = new Pose2d(0.0, 0.0, new Rotation2d(0.0));

    estimator =
        new SwerveDrivePoseEstimator(
            kinematics, new Rotation2d(0.0), initialPositions, initialPose);
  }

  @Test
  @DisplayName("Estimator starts at initial estimated pose")
  void startsAtInitialPose() {
    Pose2d currentPos = estimator.getEstimatedPosition();
    assertEquals(0.0, currentPos.getX(), EPSILON);
    assertEquals(0.0, currentPos.getY(), EPSILON);
  }

  @Test
  @DisplayName("Update applies kinematic twist delta")
  void updateAppliesKinematicStep() {
    SwerveModulePosition[] newPositions =
        new SwerveModulePosition[] {
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0))
        };

    Pose2d newEstimatedPos = estimator.update(new Rotation2d(0.0), newPositions, 0.5);

    // With all modules pointing forward at 1.0m, X should increment by 1.0
    assertEquals(1.0, newEstimatedPos.getX(), EPSILON);
    assertEquals(1.0, estimator.getEstimatedPosition().getX(), EPSILON);
  }

  @Test
  @DisplayName("Vision measurement applies correctly with history buffer")
  void visionMeasurementAppliesCorrectly() {
    SwerveModulePosition[] positions1 =
        new SwerveModulePosition[] {
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0)),
          new SwerveModulePosition(1.0, new Rotation2d(0.0))
        };
    estimator.update(new Rotation2d(0.0), positions1, 0.5);

    SwerveModulePosition[] positions2 =
        new SwerveModulePosition[] {
          new SwerveModulePosition(2.0, new Rotation2d(0.0)),
          new SwerveModulePosition(2.0, new Rotation2d(0.0)),
          new SwerveModulePosition(2.0, new Rotation2d(0.0)),
          new SwerveModulePosition(2.0, new Rotation2d(0.0))
        };
    estimator.update(new Rotation2d(0.0), positions2, 1.0);

    // After two 1m steps, we are at X=2.0.
    // Vision says at t=0.5 we were at X=1.5 (instead of X=1.0).
    // Correction will pull the current pose forward.
    Pose2d visionPose = new Pose2d(1.5, 0.0, new Rotation2d(0.0));
    estimator.addVisionMeasurement(visionPose, 0.5);

    Pose2d correctedPose = estimator.getEstimatedPosition();
    // Default trust is 0.1 std dev -> weight ~0.9.
    // Error at t=0.5 was +0.5m. Correction ~ +0.45m.
    // Final X should be ~2.45m.
    assertTrue(
        correctedPose.getX() > 2.4 && correctedPose.getX() < 2.50,
        "Pose X should be around 2.45 after fusion but was " + correctedPose.getX());
  }

  @Test
  @DisplayName("Resetting position zero-outs the odometry states")
  void resetPositionClearsState() {
    SwerveModulePosition[] movement =
        new SwerveModulePosition[] {
          new SwerveModulePosition(5.0, new Rotation2d(0.0)),
          new SwerveModulePosition(5.0, new Rotation2d(0.0)),
          new SwerveModulePosition(5.0, new Rotation2d(0.0)),
          new SwerveModulePosition(5.0, new Rotation2d(0.0))
        };
    estimator.update(new Rotation2d(0.0), movement, 1.0);

    estimator.resetPosition(
        new Rotation2d(0.0), initialPositions, new Pose2d(10.0, 10.0, new Rotation2d(0.0)));

    Pose2d newEstimated = estimator.getEstimatedPosition();
    assertEquals(10.0, newEstimated.getX(), EPSILON);
    assertEquals(10.0, newEstimated.getY(), EPSILON);
  }
}
