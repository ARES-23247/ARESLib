package org.areslib.math.estimator;

import static org.junit.jupiter.api.Assertions.*;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Translation2d;
import org.areslib.math.kinematics.MecanumDriveKinematics;
import org.areslib.math.kinematics.MecanumDriveWheelPositions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MecanumDrivePoseEstimatorTest {

  private static final double EPSILON = 1e-6;
  private MecanumDriveKinematics kinematics;
  private MecanumDrivePoseEstimator estimator;

  @BeforeEach
  void setUp() {
    // Standard square mecanum base
    kinematics =
        new MecanumDriveKinematics(
            new Translation2d(0.2, 0.2),
            new Translation2d(0.2, -0.2),
            new Translation2d(-0.2, 0.2),
            new Translation2d(-0.2, -0.2));

    MecanumDriveWheelPositions initialPositions =
        new MecanumDriveWheelPositions(0.0, 0.0, 0.0, 0.0);
    Pose2d initialPose = new Pose2d(0.0, 0.0, new Rotation2d(0.0));

    estimator =
        new MecanumDrivePoseEstimator(
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
    // Forward 1.0m (all wheels 1m)
    MecanumDriveWheelPositions newPositions = new MecanumDriveWheelPositions(1.0, 1.0, 1.0, 1.0);

    Pose2d newEstimatedPos = estimator.update(new Rotation2d(0.0), newPositions, 0.5);

    assertEquals(1.0, newEstimatedPos.getX(), EPSILON);
    assertEquals(1.0, estimator.getEstimatedPosition().getX(), EPSILON);
  }

  @Test
  @DisplayName("Vision measurement applies correctly with history buffer")
  void visionMeasurementAppliesCorrectly() {
    // Step 1: Move from X=0 to X=1 at t=0.5
    estimator.update(new Rotation2d(0.0), new MecanumDriveWheelPositions(1.0, 1.0, 1.0, 1.0), 0.5);

    // Step 2: Move from X=1 to X=2 at t=1.0
    estimator.update(new Rotation2d(0.0), new MecanumDriveWheelPositions(2.0, 2.0, 2.0, 2.0), 1.0);

    // Vision says at t=0.5 we were at X=1.5
    Pose2d visionPose = new Pose2d(1.5, 0.0, new Rotation2d(0.0));
    estimator.addVisionMeasurement(visionPose, 0.5);

    Pose2d correctedPose = estimator.getEstimatedPosition();
    assertTrue(
        correctedPose.getX() > 2.4 && correctedPose.getX() < 2.50,
        "Pose X should be around 2.45 after fusion but was " + correctedPose.getX());
  }

  @Test
  @DisplayName("Resetting position zero-outs the odometry states")
  void resetPositionClearsState() {
    // Move to X=5.0
    estimator.update(new Rotation2d(0.0), new MecanumDriveWheelPositions(5.0, 5.0, 5.0, 5.0), 1.0);

    estimator.resetPosition(
        new Rotation2d(0.0),
        new MecanumDriveWheelPositions(0.0, 0.0, 0.0, 0.0),
        new Pose2d(10.0, 10.0, new Rotation2d(0.0)));

    Pose2d newEstimated = estimator.getEstimatedPosition();
    assertEquals(10.0, newEstimated.getX(), EPSILON);
    assertEquals(10.0, newEstimated.getY(), EPSILON);
  }
}
