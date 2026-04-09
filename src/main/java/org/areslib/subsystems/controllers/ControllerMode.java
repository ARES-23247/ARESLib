package org.areslib.subsystems.controllers;

/**
 * Operational modes for robot control, inspired by elite FRC team architectures.
 *
 * <p>Each mode represents a distinct robot behavior pattern with specific intake, shooting, and
 * movement characteristics. Modes enable mode-specific logic for path planning, target selection,
 * and subsystem coordination.
 *
 * <p>Based on Team 254 (Cheesy Poofs) 2024 controller mode architecture:
 * https://github.com/Team254/FRC-2024-Public
 */
public enum ControllerMode {
  /** Default mode with no active scoring behavior. */
  IDLE,

  /** High-speed scoring into the speaker/primary goal. Prioritizes shot speed over precision. */
  SPEAKER,

  /**
   * Human Player intake mode. Optimized for receiving game pieces from the human player zone with
   * extended intake and gentle handling.
   */
  HP,

  /**
   * Ground intake mode. "POOP" refers to picking up from the floor/ground. Includes sub-modes for
   * different approach angles and shot types.
   */
  POOP,

  /**
   * Endgame climbing mode. Disables scoring systems, reconfigures drivetrain for stability, and
   * engages climbing mechanisms.
   */
  CLIMB,

  /**
   * Amp/side-scoring mode. For precise placement into side goals with lob shots or direct
   * placement.
   */
  AMP,

  /** Autonomous mode. All subsystems under full automation with manual override disabled. */
  AUTO
}
