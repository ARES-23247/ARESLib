package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.areslib.hardware.interfaces.AresDistanceSensor;

public class RevDistanceSensorWrapper implements AresDistanceSensor {
    private final DistanceSensor distanceSensor;

    public RevDistanceSensorWrapper(DistanceSensor distanceSensor) {
        this.distanceSensor = distanceSensor;
    }

    @Override
    public double getDistanceMeters() {
        return distanceSensor.getDistance(DistanceUnit.METER);
    }
}
