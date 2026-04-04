package org.areslib.math.kinematics;

public class MecanumDriveWheelPositions {
    public double frontLeftMeters;
    public double frontRightMeters;
    public double rearLeftMeters;
    public double rearRightMeters;

    public MecanumDriveWheelPositions() {}

    public MecanumDriveWheelPositions(
            double frontLeftMeters,
            double frontRightMeters,
            double rearLeftMeters,
            double rearRightMeters) {
        this.frontLeftMeters = frontLeftMeters;
        this.frontRightMeters = frontRightMeters;
        this.rearLeftMeters = rearLeftMeters;
        this.rearRightMeters = rearRightMeters;
    }
}
