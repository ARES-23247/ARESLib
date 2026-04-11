package org.areslib.sim;

/**
 * Battery Simulation Model for ARESLib.
 *
 * <p>Computes estimated voltage sag during simulation based on the sum of current draw (Amps)
 * across all active motors and mechanisms. Allows realistic simulation of brownout behavior and
 * thermal limits.
 */
public class AresBatterySim {

  private static final double NOMINAL_VOLTAGE = 12.0;

  // The internal resistance of standard FTC/FRC 12V batteries (e.g., TETRIX, Rev, FRC SLA)
  private static final double INTERNAL_RESISTANCE_OHMS = 0.018;
  private static final double BASE_DRAW_AMPS = 1.5; // RoboRIO/ControlHub base draw

  private double currentDrawAmps = 0.0;
  private double loadedVoltage = NOMINAL_VOLTAGE;

  private static final AresBatterySim INSTANCE = new AresBatterySim();

  public static AresBatterySim getInstance() {
    return INSTANCE;
  }

  private AresBatterySim() {}

  /** Resets the battery state for a new simulation tick. */
  public void resetTick() {
    currentDrawAmps = BASE_DRAW_AMPS;
  }

  /**
   * Calculates the current draw of a DC motor given its applied voltage and back-EMF state, and
   * accumulates it into the battery's total load.
   *
   * <p>I = (V_applied - k_v * angular_velocity) / R_motor
   *
   * @param appliedVolts The voltage currently supplied to the motor.
   * @param velocityRadSec The physical velocity of the motor shaft.
   * @param kV The velocity constant of the motor (Volts per Radian/sec).
   * @param rMotor The internal electrical resistance of the motor coil (Ohms).
   * @return The instantaneous current (Amps) drawn by the motor.
   */
  public double addMotorLoad(double appliedVolts, double velocityRadSec, double kV, double rMotor) {
    if (rMotor <= 0) return 0;

    double backEmf = kV * velocityRadSec;

    // Back-EMF opposes the applied voltage
    // Sign logic is tricky, but absolute value captures the load properly.
    double currentAmps = (appliedVolts - backEmf) / rMotor;

    // Motor current draw is magnitude only for battery drain purposes,
    // but regenerative braking could be negative (not modeled here).
    double magnitude = Math.abs(currentAmps);
    currentDrawAmps += magnitude;

    return magnitude;
  }

  /**
   * Directly adds a known current draw to the battery load.
   *
   * @param amps Current to add.
   */
  public void addDirectLoad(double amps) {
    currentDrawAmps += Math.abs(amps);
  }

  /**
   * Computes the new battery voltage based on accumulated loads. V_loaded = V_nominal - (I_total *
   * R_internal)
   */
  public void update() {
    loadedVoltage = Math.max(0.0, NOMINAL_VOLTAGE - (currentDrawAmps * INTERNAL_RESISTANCE_OHMS));
  }

  /**
   * Returns the dynamically sagged voltage available to the robot on this simulation tick.
   *
   * @return The dynamically sagged voltage available to the robot on this simulation tick.
   */
  public double getLoadedVoltage() {
    return loadedVoltage;
  }

  /**
   * Returns the output total amperage drain for telemetry plotting.
   *
   * @return Output total amperage drain for telemetry plotting
   */
  public double getTotalCurrentDraw() {
    return currentDrawAmps;
  }
}
