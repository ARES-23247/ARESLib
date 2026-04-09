package org.areslib.subsystems.controllers;

import org.areslib.core.StateMachine;
import org.areslib.telemetry.AresAutoLogger;

/**
 * Manages robot operational modes using state machine pattern.
 *
 * <p>Inspired by Team 254's controller mode architecture, this provides structured mode switching
 * with entry/exit actions, conditional transitions, and timeout-based fallbacks. Each mode can
 * configure subsystem behaviors differently (e.g., SPEAKER mode enables high-speed shooting, CLIMB
 * mode disables scoring systems).
 *
 * <p>Usage example:
 *
 * <pre>{@code
 * ControllerModeManager modes = new ControllerModeManager();
 * modes.configure(); // Sets up all mode transitions
 *
 * // In periodic loop:
 * modes.update();
 *
 * // Mode-specific behavior:
 * if (modes.getMode() == ControllerMode.SPEAKER) {
 *   shooter.enableHighSpeed();
 * }
 * }</pre>
 */
public class ControllerModeManager {

  private final StateMachine<ControllerMode> stateMachine;
  private double modeSwitchTime = 0.0;

  /** Constructs a new ControllerModeManager starting in IDLE mode. */
  public ControllerModeManager() {
    stateMachine = new StateMachine<>(ControllerMode.IDLE);
  }

  /**
   * Configures all state machine transitions, entry/exit actions, and mode-specific behaviors.
   *
   * <p>Call this after construction to set up the complete mode hierarchy.
   */
  public void configure() {
    // Entry actions for each mode
    stateMachine.onEntry(
        ControllerMode.SPEAKER,
        () -> {
          logModeChange("SPEAKER");
          // Enable high-speed shooter, vision targeting to speaker
        });

    stateMachine.onEntry(
        ControllerMode.HP,
        () -> {
          logModeChange("HP");
          // Extend intake, prepare for gentle handling
        });

    stateMachine.onEntry(
        ControllerMode.POOP,
        () -> {
          logModeChange("POOP");
          // Lower intake for ground pickup
        });

    stateMachine.onEntry(
        ControllerMode.CLIMB,
        () -> {
          logModeChange("CLIMB");
          // Disable shooter, engage climb brakes
        });

    stateMachine.onEntry(
        ControllerMode.AMP,
        () -> {
          logModeChange("AMP");
          // Configure for lob shots or direct placement
        });

    stateMachine.onEntry(
        ControllerMode.AUTO,
        () -> {
          logModeChange("AUTO");
          // Disable manual controls, enable path following
        });

    // Exit actions to clean up mode-specific state
    stateMachine.onExit(
        ControllerMode.SPEAKER,
        () -> {
          // Disable vision targeting
        });

    stateMachine.onExit(
        ControllerMode.CLIMB,
        () -> {
          // Re-enable scoring systems
        });

    // Conditional transitions (can be driven by gamepad, field state, or auto decisions)
    // Example: Auto-transition from HP to SPEAKER after successful intake
    stateMachine.transition(
        ControllerMode.HP,
        ControllerMode.SPEAKER,
        () -> {
          // Condition: Has game piece + ready to shoot
          return hasGamePiece() && isReadyToShoot();
        });

    // Example: Auto-transition from CLIMB to IDLE after climb complete (with timeout safety)
    stateMachine.transitionAfter(ControllerMode.CLIMB, ControllerMode.IDLE, 15.0);
  }

  /** Updates the state machine. Must be called every periodic loop. */
  public void update() {
    stateMachine.update();
  }

  /**
   * Forces a transition to the specified mode.
   *
   * @param mode The target mode.
   */
  public void setMode(ControllerMode mode) {
    if (stateMachine.getState() != mode) {
      modeSwitchTime = getCurrentTime();
      stateMachine.forceState(mode);
    }
  }

  /**
   * Returns the current active mode.
   *
   * @return The current controller mode.
   */
  public ControllerMode getMode() {
    return stateMachine.getState();
  }

  /**
   * Returns the previous mode.
   *
   * @return The previous controller mode.
   */
  public ControllerMode getPreviousMode() {
    return stateMachine.getPreviousState();
  }

  /**
   * Returns time spent in current mode (seconds).
   *
   * @return Time in current mode.
   */
  public double getTimeInMode() {
    return stateMachine.getTimeInState();
  }

  /**
   * Returns time since last mode switch (seconds).
   *
   * @return Time since mode switch.
   */
  public double getTimeSinceModeSwitch() {
    return getCurrentTime() - modeSwitchTime;
  }

  /**
   * Checks if robot is in a specific mode.
   *
   * @param mode The mode to check.
   * @return True if currently in the specified mode.
   */
  public boolean isInMode(ControllerMode mode) {
    return stateMachine.isInState(mode);
  }

  /**
   * Checks if robot is in any scoring mode (SPEAKER, AMP).
   *
   * @return True if in a scoring mode.
   */
  public boolean isScoringMode() {
    ControllerMode mode = getMode();
    return mode == ControllerMode.SPEAKER || mode == ControllerMode.AMP;
  }

  /**
   * Checks if robot is in any intake mode (HP, POOP).
   *
   * @return True if in an intake mode.
   */
  public boolean isIntakeMode() {
    ControllerMode mode = getMode();
    return mode == ControllerMode.HP || mode == ControllerMode.POOP;
  }

  // ── Helper conditions for transitions ────────────────────────────────────────

  private boolean hasGamePiece() {
    // TODO: Integrate with actual game piece sensor
    return false;
  }

  private boolean isReadyToShoot() {
    // TODO: Check shooter readiness, target lock, etc.
    return false;
  }

  private void logModeChange(String mode) {
    AresAutoLogger.recordOutput("ControllerMode/CurrentMode", mode);
    AresAutoLogger.recordOutput("ControllerMode/LastSwitchTime", getCurrentTime());
  }

  private static double getCurrentTime() {
    return System.nanoTime() / 1_000_000_000.0;
  }
}
