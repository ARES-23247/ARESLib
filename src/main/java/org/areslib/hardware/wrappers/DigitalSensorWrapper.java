package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import org.areslib.hardware.interfaces.AresDigitalSensor;

public class DigitalSensorWrapper implements AresDigitalSensor {
    private final DigitalChannel digitalChannel;

    public DigitalSensorWrapper(DigitalChannel digitalChannel) {
        this.digitalChannel = digitalChannel;
        this.digitalChannel.setMode(DigitalChannel.Mode.INPUT);
    }

    @Override
    public boolean getState() {
        return digitalChannel.getState();
    }
}
