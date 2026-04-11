package org.areslib.command;

import org.areslib.telemetry.AresAutoLogger;

/**
 * A Command that tracks elapsed match time and publishes it to telemetry.
 *
 * <p>Publishes {@code Match/ElapsedSeconds} and {@code Match/TimeRemaining} to AdvantageScope.
 * Teams can use the remaining time to trigger end-game state transitions (e.g., auto-climb at
 * T-30s).
 *
 * <pre>{@code
 * // Schedule at the start of teleop:
 * CommandScheduler.getInstance().schedule(
 *     new MatchTimerCommand(120.0)); // 2-minute teleop
 * }</pre>
 */
public class MatchTimerCommand extends Command {

  private final double matchDurationSeconds;
  private long startNanos;

  /**
   * Creates a new MatchTimerCommand.
   *
   * @param matchDurationSeconds Total match duration in seconds (e.g., 120.0 for FTC teleop, 150.0
   *     for full match).
   */
  public MatchTimerCommand(double matchDurationSeconds) {
    this.matchDurationSeconds = matchDurationSeconds;
  }

  @Override
  public void initialize() {
    startNanos = System.nanoTime();
    AresAutoLogger.recordOutput("Match/Duration", matchDurationSeconds);
  }

  @Override
  public void execute() {
    double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
    double remaining = Math.max(0.0, matchDurationSeconds - elapsed);

    AresAutoLogger.recordOutput("Match/ElapsedSeconds", elapsed);
    AresAutoLogger.recordOutput("Match/TimeRemaining", remaining);
  }

  @Override
  public boolean isFinished() {
    double elapsed = (System.nanoTime() - startNanos) / 1_000_000_000.0;
    return elapsed >= matchDurationSeconds;
  }

  @Override
  public void end(boolean interrupted) {
    AresAutoLogger.recordOutput("Match/TimeRemaining", 0.0);
    AresAutoLogger.recordOutput("Match/Status", interrupted ? "INTERRUPTED" : "COMPLETE");
  }

  /**
   * Returns the current elapsed time since the command was scheduled.
   *
   * @return Elapsed time in seconds.
   */
  public double getElapsedSeconds() {
    return (System.nanoTime() - startNanos) / 1_000_000_000.0;
  }

  /**
   * Returns the remaining time in the match.
   *
   * @return Remaining time in seconds, clamped to >= 0.
   */
  public double getTimeRemaining() {
    return Math.max(0.0, matchDurationSeconds - getElapsedSeconds());
  }
}
