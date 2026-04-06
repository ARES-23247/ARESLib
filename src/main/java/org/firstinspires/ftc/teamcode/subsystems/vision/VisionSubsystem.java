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
    
    public void setPipeline(int index) {
        io.setPipeline(index);
    }
}
