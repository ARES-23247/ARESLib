package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.areslib.hardware.interfaces.AresColorSensor;
import org.areslib.hardware.interfaces.AresDistanceSensor;

public class RevColorSensorWrapper implements AresColorSensor, AresDistanceSensor {
    private final ColorSensor colorSensor;
    private final DistanceSensor distanceSensor;

    public RevColorSensorWrapper(ColorSensor colorSensor) {
        this.colorSensor = colorSensor;
        // In the FTC SDK, the REV Color Sensor natively implements both ColorSensor and DistanceSensor
        if (colorSensor instanceof DistanceSensor) {
            this.distanceSensor = (DistanceSensor) colorSensor;
        } else {
            throw new IllegalArgumentException("ARESlib: Provided ColorSensor does not implement DistanceSensor. Make sure it is a REV Color Sensor V2/V3.");
        }
    }

    @Override
    public int getARGB() {
        return colorSensor.argb();
    }

    @Override
    public double getDistanceMeters() {
        return distanceSensor.getDistance(DistanceUnit.METER);
    }
}
