package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.AnalogInput;
import org.areslib.hardware.interfaces.AresAbsoluteEncoder;

public class AnalogAbsoluteEncoderWrapper implements AresAbsoluteEncoder {
    
    private final AnalogInput analogInput;
    private final double maxVoltage;

    /**
     * @param analogInput The analog input port.
     * @param maxVoltage The maximum voltage returned by the encoder (usually 3.3V).
     */
    public AnalogAbsoluteEncoderWrapper(AnalogInput analogInput, double maxVoltage) {
        this.analogInput = analogInput;
        this.maxVoltage = maxVoltage;
    }

    @Override
    public double getAbsolutePosition() {
        // Returns normalized absolute position from 0.0 to 1.0
        return analogInput.getVoltage() / maxVoltage;
    }

    @Override
    public double getPosition() {
        return getAbsolutePosition() * 2 * Math.PI; // Standard mapped position in radians
    }

    @Override
    public double getVelocity() {
        return 0.0; // Pure analog absolute encoders generally don't measure velocity natively
    }
}
