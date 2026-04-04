package org.areslib.math.kinematics;

public class MecanumDriveWheelSpeeds {
    public double frontLeftMetersPerSecond;
    public double frontRightMetersPerSecond;
    public double rearLeftMetersPerSecond;
    public double rearRightMetersPerSecond;

    public MecanumDriveWheelSpeeds() {}

    public MecanumDriveWheelSpeeds(
            double frontLeftMetersPerSecond,
            double frontRightMetersPerSecond,
            double rearLeftMetersPerSecond,
            double rearRightMetersPerSecond) {
        this.frontLeftMetersPerSecond = frontLeftMetersPerSecond;
        this.frontRightMetersPerSecond = frontRightMetersPerSecond;
        this.rearLeftMetersPerSecond = rearLeftMetersPerSecond;
        this.rearRightMetersPerSecond = rearRightMetersPerSecond;
    }
}
