package org.firstinspires.ftc.teamcode.subsystems.vision;

import org.areslib.command.Subsystem;
import org.areslib.telemetry.AresAutoLogger;
import org.areslib.hardware.interfaces.VisionIO;

public class VisionSubsystem implements Subsystem {
    
    private final VisionIO io;
    private final VisionIO.VisionInputs inputs = new VisionIO.VisionInputs();

    public VisionSubsystem(VisionIO io) {
        this.io = io;
    }

    @Override
    public void periodic() {
        // Automatically fetch network tables or driver inputs
        io.updateInputs(inputs);
        
        // This line performs magic: It automatically diffs the fields within 'inputs' 
        // and pushes the changes across network tables into AdvantageScope for logging.
        AresAutoLogger.processInputs("Vision", inputs);
    }

    public boolean hasTarget() {
        return inputs.hasTarget;
    }

    /**
     * @return Horizontal offset from crosshair to target (Tx) in degrees.
     */
    public double getTargetXOffset() {
        return inputs.tx;
    }

    /**
     * @return Vertical offset from crosshair to target (Ty) in degrees.
     */
    public double getTargetYOffset() {
        return inputs.ty;
    }

    /**
     * @return Target Area (Ta) in percent of image.
     */
    public double getTargetArea() {
        return inputs.ta;
    }

    /**
     * @return Field-centric 2D pose estimated by the vision system. Null if no target.
     */
    public com.pedropathing.geometry.Pose getEstimatedGlobalPose() {
        if (!inputs.hasTarget) return null;
        return new com.pedropathing.geometry.Pose(
            inputs.botPose3d[0], 
            inputs.botPose3d[1], 
            inputs.botPose3d[5] // Yaw
        );
    }
    
    /**
     * Calculates trust coefficient dynamically based on AprilTag latency and visible surface area.
     * @return Raw confidence scale (0.0 to 1.0)
     */
    public double getPoseConfidence() {
        if (!inputs.hasTarget) return 0.0;
        
        // Simple heuristic: larger area = higher confidence. Cap at 1.0.
        // A single tag taking up > 1.5% of the screen is very clear and close.
        double confidence = inputs.ta / 1.5; 
        return Math.min(confidence, 1.0);
    }
    
    public void setPipeline(int index) {
        io.setPipeline(index);
    }
}
