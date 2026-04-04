package org.areslib.math.kinematics;

public class DifferentialDriveKinematics {
    public final double trackWidthMeters;

    public DifferentialDriveKinematics(double trackWidthMeters) {
        this.trackWidthMeters = trackWidthMeters;
    }

    public DifferentialDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
        return new DifferentialDriveWheelSpeeds(
            chassisSpeeds.vxMetersPerSecond - trackWidthMeters / 2.0 * chassisSpeeds.omegaRadiansPerSecond,
            chassisSpeeds.vxMetersPerSecond + trackWidthMeters / 2.0 * chassisSpeeds.omegaRadiansPerSecond
        );
    }
}
