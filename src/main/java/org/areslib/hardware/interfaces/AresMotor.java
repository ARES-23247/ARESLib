package org.areslib.hardware.interfaces;

/**
 * Hardware-agnostic interface for a motor.
 * Ensures ARESlib can control standard REV motors or custom controllers effortlessly.
 */
public interface AresMotor {

    /** 
     * Command the motor using voltage mapping (-12.0 to 12.0). 
     * @param volts Target voltage.
     */
    void setVoltage(double volts);

    /** @return Estimated or actual applied voltage. */
    double getVoltage();
}
