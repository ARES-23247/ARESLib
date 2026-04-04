package org.areslib.hardware.wrappers;

import com.qualcomm.hardware.sparkfun.SparkFunOTOS;
import org.areslib.hardware.interfaces.AresOdometry;
import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;

public class OtosOdometryWrapper implements AresOdometry {
    
    private final SparkFunOTOS otos;

    public OtosOdometryWrapper(SparkFunOTOS otos) {
        this.otos = otos;
    }

    @Override
    public Pose2d getPoseMeters() {
        SparkFunOTOS.Pose2D otosPose = otos.getPosition();
        // OTOS natively uses inches and degrees. Convert to meters and radians.
        double xMeters = otosPose.x * 0.0254;
        double yMeters = otosPose.y * 0.0254;
        double headingRads = Math.toRadians(otosPose.h);
        
        return new Pose2d(xMeters, yMeters, new Rotation2d(headingRads));
    }
}
