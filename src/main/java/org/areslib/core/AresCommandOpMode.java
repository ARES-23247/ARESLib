package org.areslib.core;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.hardware.lynx.LynxModule;

import org.areslib.command.CommandScheduler;
import org.areslib.hardware.AresHardwareManager;
import org.areslib.telemetry.AresLogger;

import java.util.List;

/**
 * Base high-performance OpMode class.
 * Integrates PhotonCore for thread lock bypassing and enables manual bulk caching.
 */
public abstract class AresCommandOpMode extends LinearOpMode {

    private List<LynxModule> allHubs;

    /**
     * Subclasses must override this to initialize their robots (subsystems, default commands).
     */
    public abstract void robotInit();

    public void runOpMode() throws InterruptedException {
        // Enable extensions automatically
        // pre-flight hooks go here.
        
        allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        // Run user-provided initialization code
        robotInit();

        waitForStart();

        while (opModeIsActive() && !isStopRequested()) {
            // 1. Clear bulk cache
            for (LynxModule hub : allHubs) {
                hub.clearBulkCache();
            }

            // 2. Hardware Coprocessors (Odometry pods, IMU updates)
            AresHardwareManager.updateCoprocessors();

            // 3. Command Scheduler Loop
            CommandScheduler.getInstance().run();

            // 4. Fire Telemetry
            AresLogger.update();
        }

        // OpMode finished, reset scheduler state for next run
        CommandScheduler.getInstance().cancelAll();
    }
}
