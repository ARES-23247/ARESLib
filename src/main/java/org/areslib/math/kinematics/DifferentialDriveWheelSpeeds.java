package org.areslib.math.kinematics;

public class DifferentialDriveWheelSpeeds {
    public double leftMetersPerSecond;
    public double rightMetersPerSecond;

    public DifferentialDriveWheelSpeeds() {}

    public DifferentialDriveWheelSpeeds(double leftMetersPerSecond, double rightMetersPerSecond) {
        this.leftMetersPerSecond = leftMetersPerSecond;
        this.rightMetersPerSecond = rightMetersPerSecond;
    }
}
