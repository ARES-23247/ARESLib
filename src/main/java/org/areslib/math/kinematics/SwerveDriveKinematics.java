package org.areslib.math.kinematics;

import org.areslib.math.geometry.Translation2d;
import org.areslib.math.geometry.Rotation2d;

/**
 * Helper class that converts a chassis velocity (dx, dy, and dtheta components) into individual
 * module states and vice versa.
 */
public class SwerveDriveKinematics {
    private final Translation2d[] m_modules;

    public SwerveDriveKinematics(Translation2d... moduleTranslations) {
        m_modules = moduleTranslations;
    }

    /**
     * Converts a chassis speed to array of swerve module states.
     * 
     * @param chassisSpeeds The chassis speeds.
     * @return Array of swerve module states.
     */
    public SwerveModuleState[] toSwerveModuleStates(ChassisSpeeds chassisSpeeds) {
        SwerveModuleState[] states = new SwerveModuleState[m_modules.length];
        for (int i = 0; i < m_modules.length; i++) {
            // Using standard X forward, Y left coordinate system
            double vx = chassisSpeeds.vxMetersPerSecond - chassisSpeeds.omegaRadiansPerSecond * m_modules[i].getY();
            double vy = chassisSpeeds.vyMetersPerSecond + chassisSpeeds.omegaRadiansPerSecond * m_modules[i].getX();
            
            states[i] = new SwerveModuleState(Math.hypot(vx, vy), new Rotation2d(vx, vy));
        }
        return states;
    }

    /**
     * Renormalizes the wheel speeds if any individual speed is above the specified maximum.
     *
     * @param moduleStates The array of module states.
     * @param attainableMaxSpeedMetersPerSecond The absolute max speed that a module can reach.
     */
    public static void desaturateWheelSpeeds(
            SwerveModuleState[] moduleStates, double attainableMaxSpeedMetersPerSecond) {
        double realMaxSpeed = 0.0;
        for (SwerveModuleState state : moduleStates) {
            realMaxSpeed = Math.max(realMaxSpeed, Math.abs(state.speedMetersPerSecond));
        }
        if (realMaxSpeed > attainableMaxSpeedMetersPerSecond) {
            for (SwerveModuleState state : moduleStates) {
                state.speedMetersPerSecond = state.speedMetersPerSecond / realMaxSpeed * attainableMaxSpeedMetersPerSecond;
            }
        }
    }
}
