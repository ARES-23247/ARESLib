package org.areslib.hardware.wrappers;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.areslib.hardware.interfaces.VisionIO;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;

public class LimelightVisionWrapper implements VisionIO {

    private final Limelight3A limelight;

    public LimelightVisionWrapper(HardwareMap hardwareMap, String deviceName) {
        // Officially supported in FTC SDK >= 10.1
        this.limelight = hardwareMap.get(Limelight3A.class, deviceName);
        this.limelight.pipelineSwitch(0);
        this.limelight.start();
    }

    @Override
    public void updateInputs(VisionInputs inputs) {
        if (!limelight.isConnected()) {
            inputs.hasTarget = false;
            return;
        }

        LLResult result = limelight.getLatestResult();
        
        if (result == null || !result.isValid()) {
            inputs.hasTarget = false;
            inputs.latencyMs = 0;
            inputs.fiducialCount = 0;
            return;
        }

        inputs.hasTarget = true;
        inputs.tx = result.getTx();
        inputs.ty = result.getTy();
        inputs.ta = result.getTa();
        inputs.pipelineIndex = result.getPipelineIndex();
        inputs.fiducialCount = result.getClassifierResults() != null ? result.getClassifierResults().size() : 
                               (result.getBarcodeResults() != null ? result.getBarcodeResults().size() : 1);
        
        // FId details usually hidden inside arrays for complex multi-tag operations, 
        // fallback to standard Pose3d processing.
        
        // Ensure network latency is accounted for
        inputs.latencyMs = result.getCaptureLatency() + result.getTargetingLatency();

        // Process Botpose (Returns WPI field-centric pose if Limelight is configured correctly)
        // FTC SDK stores Pose3d object format
        Pose3D botpose = result.getBotpose();
        if (botpose != null) {
            inputs.botPose3d[0] = botpose.getPosition().toUnit(DistanceUnit.METER).x;
            inputs.botPose3d[1] = botpose.getPosition().toUnit(DistanceUnit.METER).y;
            inputs.botPose3d[2] = botpose.getPosition().toUnit(DistanceUnit.METER).z;
            inputs.botPose3d[3] = botpose.getOrientation().getRoll(AngleUnit.RADIANS);
            inputs.botPose3d[4] = botpose.getOrientation().getPitch(AngleUnit.RADIANS);
            inputs.botPose3d[5] = botpose.getOrientation().getYaw(AngleUnit.RADIANS);
        }

        Pose3D botposeMT2 = result.getBotpose_MT2();
        if (botposeMT2 != null) {
            inputs.botPoseMegaTag2[0] = botposeMT2.getPosition().toUnit(DistanceUnit.METER).x;
            inputs.botPoseMegaTag2[1] = botposeMT2.getPosition().toUnit(DistanceUnit.METER).y;
            inputs.botPoseMegaTag2[2] = botposeMT2.getPosition().toUnit(DistanceUnit.METER).z;
            inputs.botPoseMegaTag2[3] = botposeMT2.getOrientation().getRoll(AngleUnit.RADIANS);
            inputs.botPoseMegaTag2[4] = botposeMT2.getOrientation().getPitch(AngleUnit.RADIANS);
            inputs.botPoseMegaTag2[5] = botposeMT2.getOrientation().getYaw(AngleUnit.RADIANS);
        }
    }

    @Override
    public void setPipeline(int index) {
        if (limelight != null) {
            limelight.pipelineSwitch(index);
        }
    }

    /**
     * Provide raw access to the driver if teams need Limelight-specific deep functions.
     * Use sparingly to avoid breaking simulator portability.
     */
    public Limelight3A getRawDriver() {
        return limelight;
    }
}
