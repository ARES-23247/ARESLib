package org.areslib.core.localization;

import com.pedropathing.drivetrain.Drivetrain;
import com.pedropathing.math.Vector;
import org.areslib.subsystems.drive.DriveSubsystem;

public class AresPedroDrivetrain extends Drivetrain {

    private final DriveSubsystem driveSubsystem;
    
    // Internal state cache for logging/telemetry
    private double currentForward = 0.0;
    private double currentStrafe = 0.0;
    private double currentTurn = 0.0;

    public AresPedroDrivetrain(DriveSubsystem driveSubsystem) {
        super();
        this.driveSubsystem = driveSubsystem;
    }

    @Override
    public double[] calculateDrive(Vector driveError, Vector headingError, Vector driveVector, double headingVector) {
        // Pedro Pathing calculates error vectors based on inches natively.
        // We will convert these into reasonable inputs for Swerve Drive kinematics.
        // ARESlib DriveSubsystem takes (forwardMetersPerSec, strafeMetersPerSec, turnRadPerSec)
        
        // Pedro Pathing calculates PID outputs internally and passes them via driveVector and headingVector.
        // driveError and headingError are the true geometric errors, NOT the control inputs.
        // We only want the control inputs (PID outputs).
        double forwardInput = driveVector.getXComponent();
        double strafeInput = driveVector.getYComponent();
        double turnInput = headingVector; 

        // Note: Pedro typically outputs motor powers natively, but since we are simulating field-centric chassis speeds:
        // We will scale these raw PID outputs into approximate physical velocities for the simulator.
        currentForward = forwardInput * 2.0; // Scaled up to represent roughly 2 m/s max
        currentStrafe = strafeInput * 2.0;
        currentTurn = turnInput * 2.0; // Scaled up to represent roughly 2 rad/s max

        driveSubsystem.drive(currentForward, currentStrafe, currentTurn);

        return new double[]{0, 0, 0, 0}; // Return unused power array
    }

    @Override
    public void startTeleopDrive() {}

    @Override
    public void startTeleopDrive(boolean useVoltageCompensation) {}

    @Override
    public void runDrive(double[] powers) {
        // Unused directly, as calculateDrive drives the subsystem asynchronously
    }

    @Override
    public void breakFollowing() {
        driveSubsystem.drive(0, 0, 0);
    }

    @Override
    public double xVelocity() { return 0; }

    @Override
    public double yVelocity() { return 0; }

    @Override
    public void updateConstants() {}

    @Override
    public void setXVelocity(double xV) {}

    @Override
    public void setYVelocity(double yV) {}

    @Override
    public double getVoltage() { return 12.0; }

    @Override
    public String debugString() { return "AresPedroSwerveBridge"; }
}
