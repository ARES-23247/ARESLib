package org.areslib.hardware.wrappers;

import org.areslib.hardware.interfaces.AresEncoder;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.lang.reflect.Method;

/**
 * Optional Wrapper for the SRSHub driver using reflection.
 */
public class SrsHubEncoderWrapper implements AresEncoder {

    private final Object srsHubDevice;
    private final int channelIndex;
    private final Method getEncoderPositionMethod;
    private final Method getEncoderVelocityMethod;

    public SrsHubEncoderWrapper(HardwareMap hardwareMap, String deviceName, int channel) {
        this.channelIndex = channel;
        try {
            this.srsHubDevice = hardwareMap.get(deviceName);
            Class<?> clazz = srsHubDevice.getClass();
            this.getEncoderPositionMethod = clazz.getMethod("getEncoderPosition", int.class);
            this.getEncoderVelocityMethod = clazz.getMethod("getEncoderVelocity", int.class);
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: Failed to bind to SRSHub driver. Make sure the driver is installed.", e);
        }
    }

    @Override
    public double getPosition() {
        try {
            return ((Number) getEncoderPositionMethod.invoke(srsHubDevice, channelIndex)).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: SRSHub getEncoderPosition failed.", e);
        }
    }

    @Override
    public double getVelocity() {
        try {
            return ((Number) getEncoderVelocityMethod.invoke(srsHubDevice, channelIndex)).doubleValue();
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: SRSHub getEncoderVelocity failed.", e);
        }
    }
}
