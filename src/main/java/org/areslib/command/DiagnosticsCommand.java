package org.areslib.command;

import org.areslib.faults.AresDiagnostics;
import org.areslib.telemetry.AresAutoLogger;

/**
 * A Command that continuously runs pre-match hardware diagnostics during the init_loop phase.
 *
 * <p>Unlike the static {@link AresDiagnostics#runPreMatchCheck}, this command runs repeatedly,
 * allowing the driver station telemetry to show a live "pre-flight checklist" that updates as
 * hardware comes online (e.g., a motor is plugged in after the OpMode has already started init).
 *
 * <p>Schedule this command in {@code robotInit()} before {@code waitForStart()}.
 *
 * <pre>{@code
 * CommandScheduler.getInstance().schedule(
 *     new DiagnosticsCommand(hardwareMap));
 * }</pre>
 */
public class DiagnosticsCommand extends Command {

  private final com.qualcomm.robotcore.hardware.HardwareMap hardwareMap;
  private boolean allPassed = false;
  private int cycleCount = 0;

  /** Minimum number of consecutive passing cycles before the command finishes. */
  private static final int REQUIRED_PASS_CYCLES = 3;

  private int consecutivePasses = 0;

  /**
   * Creates a new DiagnosticsCommand.
   *
   * @param hardwareMap The robot's hardware map.
   */
  public DiagnosticsCommand(com.qualcomm.robotcore.hardware.HardwareMap hardwareMap) {
    this.hardwareMap = hardwareMap;
  }

  @Override
  public void initialize() {
    consecutivePasses = 0;
    cycleCount = 0;
    allPassed = false;
    AresAutoLogger.recordOutput("Diagnostics/CommandState", "RUNNING");
  }

  @Override
  public void execute() {
    cycleCount++;
    boolean passed = AresDiagnostics.runPreMatchCheck(hardwareMap);

    if (passed) {
      consecutivePasses++;
    } else {
      consecutivePasses = 0;
    }

    allPassed = consecutivePasses >= REQUIRED_PASS_CYCLES;

    AresAutoLogger.recordOutput("Diagnostics/Cycle", cycleCount);
    AresAutoLogger.recordOutput("Diagnostics/ConsecutivePasses", consecutivePasses);
    AresAutoLogger.recordOutput("Diagnostics/AllPassed", allPassed ? "TRUE" : "FALSE");
  }

  @Override
  public boolean isFinished() {
    return allPassed;
  }

  @Override
  public void end(boolean interrupted) {
    AresAutoLogger.recordOutput(
        "Diagnostics/CommandState", interrupted ? "INTERRUPTED" : "COMPLETE");
  }
}
