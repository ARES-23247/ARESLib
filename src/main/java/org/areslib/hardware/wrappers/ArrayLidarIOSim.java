package org.areslib.hardware.wrappers;

import org.areslib.hardware.sensors.ArrayLidarIO;

public class ArrayLidarIOSim implements ArrayLidarIO {

    private final int resolution;
    private final java.util.function.Supplier<org.areslib.hardware.interfaces.OdometryIO.OdometryInputs> odometrySupplier;

    // Standard FTC field dimensions (144 inches = ~3.6576 meters)
    private static final double FIELD_SIZE_METERS = 3.6576;
    private static final double FIELD_FOV_RADIANS = Math.toRadians(45.0);
    private static final double MAX_RANGE_MM = 4000.0; // 4 meters max range typical for VL53L5CX

    /** Create a simulation configured for 16 (4x4) or 64 (8x8) resolution */
    public ArrayLidarIOSim(int resolution, java.util.function.Supplier<org.areslib.hardware.interfaces.OdometryIO.OdometryInputs> odometrySupplier) {
        this.resolution = resolution;
        this.odometrySupplier = odometrySupplier;
    }

    /** Defaults to standard full 64 (8x8) resolution */
    public ArrayLidarIOSim(java.util.function.Supplier<org.areslib.hardware.interfaces.OdometryIO.OdometryInputs> odometrySupplier) {
        this.resolution = 64;
        this.odometrySupplier = odometrySupplier;
    }

    @Override
    public void updateInputs(ArrayLidarInputs inputs) {
        if (inputs.distanceZonesMm.length != resolution) {
            inputs.distanceZonesMm = new double[resolution];
        }

        // Get actual robot position from supplier
        org.areslib.hardware.interfaces.OdometryIO.OdometryInputs odo = null;
        if (odometrySupplier != null) {
            odo = odometrySupplier.get();
        }

        if (odo == null) {
            // Null fallback
            for (int i = 0; i < resolution; i++) {
                inputs.distanceZonesMm[i] = MAX_RANGE_MM;
            }
            return;
        }

        double rx = odo.xMeters;
        double ry = odo.yMeters;
        double heading = odo.headingRadians;

        int gridDim = (int) Math.sqrt(resolution);

        for (int row = 0; row < gridDim; row++) {
            for (int col = 0; col < gridDim; col++) {
                // Calculate angular offsets in the sensor's local frame
                // Mapping col to horizontal angle (yaw)
                double colFraction = (col + 0.5) / gridDim - 0.5; // -0.5 to 0.5
                double yawOffset = colFraction * FIELD_FOV_RADIANS;

                // Mapping row to vertical angle (pitch)
                double rowFraction = (row + 0.5) / gridDim - 0.5; // -0.5 to 0.5
                double pitchOffset = rowFraction * FIELD_FOV_RADIANS;

                // 2D Raycast in field space
                double rayAngle = heading + yawOffset;
                double cosRay = Math.cos(rayAngle);
                double sinRay = Math.sin(rayAngle);

                // Distance to vertical walls (x = 0 or x = FIELD_SIZE_METERS)
                double distX = Double.MAX_VALUE;
                if (Math.abs(cosRay) > 1e-6) {
                    double targetX = (cosRay > 0) ? FIELD_SIZE_METERS : 0.0;
                    distX = (targetX - rx) / cosRay;
                }

                // Distance to horizontal walls (y = 0 or y = FIELD_SIZE_METERS)
                double distY = Double.MAX_VALUE;
                if (Math.abs(sinRay) > 1e-6) {
                    double targetY = (sinRay > 0) ? FIELD_SIZE_METERS : 0.0;
                    distY = (targetY - ry) / sinRay;
                }

                // Minimum valid positive distance in 2D plane
                double dist2D = Double.MAX_VALUE;
                if (distX >= 0 && distX < dist2D) dist2D = distX;
                if (distY >= 0 && distY < dist2D) dist2D = distY;

                // Convert 2D flat distance to true 3D hypotenuse using pitch
                double dist3D_mm = (dist2D / Math.cos(pitchOffset)) * 1000.0;

                // Cap to max sensor range
                int index = (row * gridDim) + col;
                inputs.distanceZonesMm[index] = Math.min(dist3D_mm, MAX_RANGE_MM);
            }
        }
    }
}
