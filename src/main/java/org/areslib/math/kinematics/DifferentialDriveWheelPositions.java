package org.areslib.math.kinematics;

/** Represents the wheel positions for a differential drive drivetrain. */
public class DifferentialDriveWheelPositions {
  public double leftMeters;
  public double rightMeters;

  public DifferentialDriveWheelPositions() {}

  public DifferentialDriveWheelPositions(double leftMeters, double rightMeters) {
    this.leftMeters = leftMeters;
    this.rightMeters = rightMeters;
  }

  public void set(DifferentialDriveWheelPositions other) {
    this.leftMeters = other.leftMeters;
    this.rightMeters = other.rightMeters;
  }

  public void set(double leftMeters, double rightMeters) {
    this.leftMeters = leftMeters;
    this.rightMeters = rightMeters;
  }

  public DifferentialDriveWheelPositions copy() {
    return new DifferentialDriveWheelPositions(leftMeters, rightMeters);
  }
}
