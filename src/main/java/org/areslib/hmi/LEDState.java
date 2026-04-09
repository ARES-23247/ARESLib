package org.areslib.hmi;

/**
 * Standardized semantic states for robot visual feedback. These states abstract away the physical
 * LED patterns so subsystems don't need to know *how* to blink, only *why* they are blinking.
 */
public enum LEDState {
  /** Robot is disabled or in a neutral startup state */
  OFF,
  /** Robot is active and ready for input */
  IDLE,
  /** A subsystem is actively acquiring a game piece */
  INTAKING,
  /** The robot successfully possesses a game piece */
  HAS_GAME_PIECE,
  /** The robot is aligned to a target */
  ALIGNED_TO_TARGET,
  /** The robot is executing an automated sequence */
  AUTO_SEQUENCE_ACTIVE,
  /** A critical fault or error has occurred */
  ERROR,
  /** System check or diagnostic mode is running */
  DIAGNOSTIC
}
