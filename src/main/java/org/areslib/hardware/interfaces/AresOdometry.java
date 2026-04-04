package org.areslib.hardware.interfaces;

import org.areslib.math.geometry.Pose2d;

public interface AresOdometry {
    /**
     * Gets the computed position of the robot from the odometry system.
     * @return A Pose2d representing X meters, Y meters, and Heading.
     */
    Pose2d getPoseMeters();
}
