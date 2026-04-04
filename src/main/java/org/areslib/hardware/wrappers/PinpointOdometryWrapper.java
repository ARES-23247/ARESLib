package org.areslib.hardware.wrappers;

import org.areslib.hardware.interfaces.AresOdometry;
import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import com.qualcomm.robotcore.hardware.HardwareMap;
import java.lang.reflect.Method;

/**
 * An optional wrapper for the GoBilda Pinpoint driver.
 * Uses Reflection to prevent ARESlib from having a hard dependency on the app module's teamcode. 
 */
public class PinpointOdometryWrapper implements AresOdometry {

    private final Object pinpointDevice;
    private final Method updateMethod;
    private final Method getPosXMethod;
    private final Method getPosYMethod;
    private final Method getHeadingMethod;

    public PinpointOdometryWrapper(HardwareMap hardwareMap, String deviceName) {
        try {
            this.pinpointDevice = hardwareMap.get(deviceName);
            Class<?> clazz = pinpointDevice.getClass();
            this.updateMethod = clazz.getMethod("update");
            this.getPosXMethod = clazz.getMethod("getPosX");
            this.getPosYMethod = clazz.getMethod("getPosY");
            this.getHeadingMethod = clazz.getMethod("getHeading");
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: Failed to bind to GoBilda Pinpoint driver using Reflection. Ensure the driver is installed.", e);
        }
    }

    /**
     * Updates the pinpoint's internal odometry calculation. Must be called before fetching pose natively or through loop.
     */
    public void update() {
        try {
            updateMethod.invoke(pinpointDevice);
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: Pinpoint update() failed.", e);
        }
    }

    @Override
    public Pose2d getPoseMeters() {
        try {
            // Assuming the Pinpoint driver's getPosX/Y returns values in millimeters
            double xMeters = ((double) getPosXMethod.invoke(pinpointDevice)) / 1000.0;
            double yMeters = ((double) getPosYMethod.invoke(pinpointDevice)) / 1000.0;
            double headingRads = (double) getHeadingMethod.invoke(pinpointDevice);

            return new Pose2d(xMeters, yMeters, new Rotation2d(headingRads));
        } catch (Exception e) {
            throw new RuntimeException("ARESlib: Pinpoint getPose() failed.", e);
        }
    }
}
