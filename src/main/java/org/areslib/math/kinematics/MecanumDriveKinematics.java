package org.areslib.math.kinematics;

import org.areslib.math.geometry.Translation2d;

public class MecanumDriveKinematics {
    private final Translation2d m_frontLeftWheelMeters;
    private final Translation2d m_frontRightWheelMeters;
    private final Translation2d m_rearLeftWheelMeters;
    private final Translation2d m_rearRightWheelMeters;

    public MecanumDriveKinematics(
            Translation2d frontLeftWheelMeters,
            Translation2d frontRightWheelMeters,
            Translation2d rearLeftWheelMeters,
            Translation2d rearRightWheelMeters) {
        m_frontLeftWheelMeters = frontLeftWheelMeters;
        m_frontRightWheelMeters = frontRightWheelMeters;
        m_rearLeftWheelMeters = rearLeftWheelMeters;
        m_rearRightWheelMeters = rearRightWheelMeters;
    }

    public MecanumDriveWheelSpeeds toWheelSpeeds(ChassisSpeeds chassisSpeeds) {
        // Inverse kinematics using standard FTC/WPILib coordinate system (+x forward, +y left, +theta CCW)
        return new MecanumDriveWheelSpeeds(
            chassisSpeeds.vxMetersPerSecond - chassisSpeeds.vyMetersPerSecond - chassisSpeeds.omegaRadiansPerSecond * (m_frontLeftWheelMeters.getX() + m_frontLeftWheelMeters.getY()),
            chassisSpeeds.vxMetersPerSecond + chassisSpeeds.vyMetersPerSecond + chassisSpeeds.omegaRadiansPerSecond * (m_frontRightWheelMeters.getX() - m_frontRightWheelMeters.getY()),
            chassisSpeeds.vxMetersPerSecond + chassisSpeeds.vyMetersPerSecond - chassisSpeeds.omegaRadiansPerSecond * (m_rearLeftWheelMeters.getX() - m_rearLeftWheelMeters.getY()),
            chassisSpeeds.vxMetersPerSecond - chassisSpeeds.vyMetersPerSecond + chassisSpeeds.omegaRadiansPerSecond * (m_rearRightWheelMeters.getX() + m_rearRightWheelMeters.getY())
        );
    }
}
