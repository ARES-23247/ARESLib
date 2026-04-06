package org.firstinspires.ftc.teamcode.subsystems;

import org.areslib.command.Subsystem;
import org.areslib.core.localization.AresFollower;
import org.firstinspires.ftc.teamcode.subsystems.vision.VisionSubsystem;
import com.pedropathing.geometry.Pose;

/**
 * Sensor Fusion Subsystem orchestrates the mathematical blending of the underlying 
 * Pedro Pathing dead-wheel odometry with absolute global Vision coordinates.
 */
public class SensorFusionSubsystem implements Subsystem {

    private final AresFollower odometry;
    private final VisionSubsystem vision;

    // The maximum percentage the odometry can be "nudged" toward the vision target per 20ms loop cycle.
    // 0.15 = 15% interpolation.
    private static final double MAX_VISION_TRUST_FACTOR = 0.15;

    public SensorFusionSubsystem(AresFollower odometry, VisionSubsystem vision) {
        this.odometry = odometry;
        this.vision = vision;
    }

    @Override
    public void periodic() {
        Pose visionPose = vision.getEstimatedGlobalPose();
        
        // If the vision system doesn't see anything, we trust the dead-wheels 100%.
        if (visionPose == null) return;

        double confidence = vision.getPoseConfidence();
        
        // Reject extremely noisy or distant measurements
        if (confidence <= 0.05) return;

        Pose currentPose = odometry.getPose();

        // Calculate dynamic interpolation weight based on camera confidence
        double blendWeight = confidence * MAX_VISION_TRUST_FACTOR;

        // Perform linear interpolation (lerp) for X and Y coordinates
        double interpolatedX = currentPose.getX() + (visionPose.getX() - currentPose.getX()) * blendWeight;
        double interpolatedY = currentPose.getY() + (visionPose.getY() - currentPose.getY()) * blendWeight;

        // Note: For headings, standard Lerp can snap weirdly around the 360-degree wraparound.
        // FRC best practice is to heavily trust the IMU for precise yaw, and only softly correct it.
        // A minimal approach is used here, or we can just retain the highly accurate IMU heading.
        double currentHeading = currentPose.getHeading();
        double visionHeading = visionPose.getHeading();
        
        // Shortest path angle difference
        double angleDifference = visionHeading - currentHeading;
        while (angleDifference > Math.PI) angleDifference -= 2 * Math.PI;
        while (angleDifference < -Math.PI) angleDifference += 2 * Math.PI;

        double interpolatedHeading = currentHeading + (angleDifference * blendWeight);

        // Nudge the core localization follower
        odometry.setPose(new Pose(interpolatedX, interpolatedY, interpolatedHeading));
    }
}
