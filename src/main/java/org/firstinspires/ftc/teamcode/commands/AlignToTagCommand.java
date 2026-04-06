package org.firstinspires.ftc.teamcode.commands;

import org.areslib.command.Command;
import org.areslib.subsystems.drive.SwerveDriveSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.vision.VisionSubsystem;

public class AlignToTagCommand extends Command {
    private final SwerveDriveSubsystem drive;
    private final VisionSubsystem vision;
    
    // Proportional Gains (Teams should tune these real-world limits)
    private static final double kPx = 0.05; // Tunes Strafe to zero out Tx error
    private static final double kPy = 0.15; // Tunes Forward to zero out Ty area error
    
    private final double targetArea;

    public AlignToTagCommand(SwerveDriveSubsystem drive, VisionSubsystem vision, double targetAreaPercent) {
        this.drive = drive;
        this.vision = vision;
        this.targetArea = targetAreaPercent;
        addRequirements(drive);
    }

    @Override
    public void execute() {
        if (vision.hasTarget()) {
            double tx = vision.getTargetXOffset();
            double ta = vision.getTargetArea();
            
            // X offset correlates to Strafe (Left/Right)
            double strafeCmd = -tx * kPx;
            
            // Area discrepancy correlates to Forward/Back
            double forwardCmd = (targetArea - ta) * kPy;
            
            // Send autonomous velocities to drive base, zero rotation
            drive.drive(forwardCmd, strafeCmd, 0.0);
        } else {
            // Stop if no target found to avoid blind flight
            drive.drive(0.0, 0.0, 0.0);
        }
    }

    @Override
    public void end(boolean interrupted) {
        drive.drive(0.0, 0.0, 0.0);
    }

    @Override
    public boolean isFinished() {
        // Run continuously while button is held
        return false;
    }
}
