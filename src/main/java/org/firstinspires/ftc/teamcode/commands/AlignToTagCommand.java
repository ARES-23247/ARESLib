package org.firstinspires.ftc.teamcode.commands;

import static org.firstinspires.ftc.teamcode.Constants.AlignConstants.*;

import org.areslib.command.Command;
import org.areslib.subsystems.drive.SwerveDriveSubsystem;
import org.areslib.subsystems.vision.AresVisionSubsystem;

/**
 * AlignToTagCommand standard implementation.
 *
 * <p>This class provides the core structural components or hardware abstraction for {@code
 * AlignToTagCommand}. Extracted and compiled as part of the ARESLib Code Audit for missing
 * documentation coverage.
 */
public class AlignToTagCommand extends Command {
  private final SwerveDriveSubsystem drive;
  private final AresVisionSubsystem vision;

  // Proportional Gains (Teams should tune these real-world limits)
  private final double targetAreaPercentage;

  public AlignToTagCommand(
      SwerveDriveSubsystem drive, AresVisionSubsystem vision, double targetAreaPercent) {
    this.drive = drive;
    this.vision = vision;
    this.targetAreaPercentage = targetAreaPercent;
    addRequirements(drive);
  }

  @Override
  public void execute() {
    if (vision.hasTarget()) {
      double targetXOffsetDegrees = vision.getTargetXOffset();
      double targetAreaPercentage = vision.getTargetArea();

      // targetXOffsetDegrees is in DEGREES (±29.8° FOV). ALIGN_P_X converts degrees → m/s.
      // At max offset (29.8°): strafeMetersPerSecond ≈ 29.8 * 0.05 = 1.49 m/s.
      double strafeMetersPerSecond = -targetXOffsetDegrees * ALIGN_P_X;

      // Distance error (targetAreaPercentage = percentage of image area)
      // Moving closer increases targetAreaPercentage. E.g. target area 2.0%, current 1.0% -> we
      // need to move forward
      double forwardMetersPerSecond =
          (this.targetAreaPercentage - targetAreaPercentage) * ALIGN_P_Y;

      // Send autonomous velocities to drive base, zero rotation
      drive.drive(forwardMetersPerSecond, strafeMetersPerSecond, 0.0);
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
