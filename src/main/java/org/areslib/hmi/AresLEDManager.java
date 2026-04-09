package org.areslib.hmi;

import com.qualcomm.hardware.rev.RevBlinkinLedDriver;
import com.qualcomm.robotcore.hardware.HardwareMap;
import org.areslib.telemetry.AresTelemetry;

/**
 * Singleton manager to control visual feedback via REV Blinkin LED Driver. Emulates the FRC
 * CANdle/LED architecture to abstract physical hardware from subsystem logic. Subsystems request
 * semantic states instead of hardware colors.
 */
public class AresLEDManager {

  private static AresLEDManager instance;
  private RevBlinkinLedDriver blinkinLedDriver;
  private LEDState currentState = LEDState.OFF;

  private AresLEDManager() {}

  /**
   * Returns the singleton instance of the LED Manager.
   *
   * @return The singleton instance of the LED Manager
   */
  public static AresLEDManager getInstance() {
    if (instance == null) {
      instance = new AresLEDManager();
    }
    return instance;
  }

  /**
   * Initializes the physical REV Blinkin hardware.
   *
   * @param hwMap The active OpMode hardware map
   * @param deviceName The configuration name of the Blinkin driver
   */
  public void init(HardwareMap hwMap, String deviceName) {
    try {
      blinkinLedDriver = hwMap.get(RevBlinkinLedDriver.class, deviceName);
      setState(LEDState.IDLE);
    } catch (IllegalArgumentException e) {
      // Hardware not found, gracefully degrade
      blinkinLedDriver = null;
      AresTelemetry.putString("AresLEDManager", "Failed to find Blinkin LED driver: " + deviceName);
    }
  }

  /**
   * Sets the active LED state and applies the mapped visual pattern. Promotes state changes to
   * AresTelemetry.
   *
   * @param newState The semantic state requested
   */
  public void setState(LEDState newState) {
    if (this.currentState == newState) {
      return;
    }

    this.currentState = newState;

    if (blinkinLedDriver != null) {
      blinkinLedDriver.setPattern(mapStateToPattern(newState));
    }

    // Publish to Telemetry dashboards
    AresTelemetry.putString("HMI/LEDState", newState.toString());
  }

  /**
   * Returns the current active LED state.
   *
   * @return The current active LED state
   */
  public LEDState getCurrentState() {
    return currentState;
  }

  /** Maps semantic robot states to physical FTC Blinkin patterns. */
  private RevBlinkinLedDriver.BlinkinPattern mapStateToPattern(LEDState state) {
    switch (state) {
      case IDLE:
        return RevBlinkinLedDriver.BlinkinPattern.BREATH_BLUE;
      case INTAKING:
        return RevBlinkinLedDriver.BlinkinPattern.STROBE_GOLD;
      case HAS_GAME_PIECE:
        return RevBlinkinLedDriver.BlinkinPattern.GREEN;
      case ALIGNED_TO_TARGET:
        return RevBlinkinLedDriver.BlinkinPattern.AQUA;
      case AUTO_SEQUENCE_ACTIVE:
        return RevBlinkinLedDriver.BlinkinPattern.COLOR_WAVES_PARTY_PALETTE;
      case ERROR:
        return RevBlinkinLedDriver.BlinkinPattern.STROBE_RED;
      case DIAGNOSTIC:
        return RevBlinkinLedDriver.BlinkinPattern.HEARTBEAT_WHITE;
      case OFF:
      default:
        return RevBlinkinLedDriver.BlinkinPattern.BLACK;
    }
  }
}
