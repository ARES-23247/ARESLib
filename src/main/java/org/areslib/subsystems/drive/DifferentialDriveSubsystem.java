package org.areslib.subsystems.drive;

import org.areslib.command.SubsystemBase;

/**
 * AdvantageKit-style Differential Drive Subsystem.
 * Acts as the structural controller for handling physics logic across left and right sides.
 */
public class DifferentialDriveSubsystem extends SubsystemBase {

    private final DifferentialDriveIO io;
    private final DifferentialDriveIO.DifferentialDriveInputs inputs = new DifferentialDriveIO.DifferentialDriveInputs();

    private double commandedVx = 0.0;
    private double commandedOmega = 0.0;

    // Ported WPILib kinematics (0.6 meters track width)
    private final org.areslib.math.kinematics.DifferentialDriveKinematics kinematics = new org.areslib.math.kinematics.DifferentialDriveKinematics(0.6);

    private final org.areslib.math.controller.PIDController leftPid = new org.areslib.math.controller.PIDController(1.0, 0.0, 0.0);
    private final org.areslib.math.controller.PIDController rightPid = new org.areslib.math.controller.PIDController(1.0, 0.0, 0.0);
    private final org.areslib.math.controller.SimpleMotorFeedforward driveFeedforward = new org.areslib.math.controller.SimpleMotorFeedforward(0.1, 2.5);

    /**
     * Constructs the DifferentialDriveSubsystem.
     * @param io The unified Differential Hardware interface.
     */
    public DifferentialDriveSubsystem(DifferentialDriveIO io) {
        this.io = io;
    }

    @Override
    public void periodic() {
        io.updateInputs(inputs);
        org.areslib.telemetry.AresAutoLogger.processInputs("DifferentialDrive", inputs);
    }

    /**
     * @return The commanded X velocity in m/s.
     */
    public double getCommandedVx() { return commandedVx; }

    /**
     * @return The commanded angular velocity in rad/s.
     */
    public double getCommandedOmega() { return commandedOmega; }

    /**
     * Commands the differential drive to move.
     * @param forwardMetersPerSec The forward velocity in m/s (X axis).
     * @param turnRadPerSec The angular velocity in rad/s.
     */
    public void drive(double forwardMetersPerSec, double turnRadPerSec) {
        this.commandedVx = forwardMetersPerSec;
        this.commandedOmega = turnRadPerSec;
        
        org.areslib.math.kinematics.ChassisSpeeds speeds = new org.areslib.math.kinematics.ChassisSpeeds(
            forwardMetersPerSec, 0.0, turnRadPerSec
        );

        org.areslib.math.kinematics.DifferentialDriveWheelSpeeds wheelSpeeds = kinematics.toWheelSpeeds(speeds);

        double leftVolts = driveFeedforward.calculate(wheelSpeeds.leftMetersPerSecond) 
            + leftPid.calculate(inputs.leftVelocityMps, wheelSpeeds.leftMetersPerSecond);
            
        double rightVolts = driveFeedforward.calculate(wheelSpeeds.rightMetersPerSecond) 
            + rightPid.calculate(inputs.rightVelocityMps, wheelSpeeds.rightMetersPerSecond);

        io.setVoltages(leftVolts, rightVolts);
    }
}
