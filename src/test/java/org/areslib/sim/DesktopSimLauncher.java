package org.areslib.sim;

import org.areslib.command.CommandScheduler;
import org.areslib.hardware.SwerveModuleIOSim;
import org.areslib.hardware.wrappers.ArrayLidarIOSim;
import org.areslib.subsystems.drive.DriveSubsystem;
import org.areslib.telemetry.AresTelemetry;
import org.areslib.telemetry.DesktopLiveBackend;
import org.areslib.telemetry.WpiLogBackend;
import org.areslib.hardware.interfaces.OdometryIO;

import org.areslib.hardware.wrappers.AresGamepad;

public class DesktopSimLauncher {

    public static void main(String[] args) {
        System.out.println("Initializing ARES Simulator Environment...");

        // 1. Register Telemetry Base
        AresTelemetry.registerBackend(new DesktopLiveBackend());
        AresTelemetry.registerBackend(new WpiLogBackend("logs/sim"));
        AresTelemetry.registerBackend(new org.areslib.telemetry.RlogServerBackend(5800));

        // 2. Mock Hardware Layer
        DriveSubsystem driveSubsystem = new DriveSubsystem(
            new SwerveModuleIOSim(),
            new SwerveModuleIOSim(),
            new SwerveModuleIOSim(),
            new SwerveModuleIOSim()
        );

        OdometryIO.OdometryInputs odometryInputs = new OdometryIO.OdometryInputs();
        CommandScheduler.getInstance().registerSubsystem(driveSubsystem);

        ArrayLidarIOSim lidarSim = new ArrayLidarIOSim(() -> odometryInputs); 
        org.areslib.hardware.sensors.ArrayLidarIO.ArrayLidarInputs lidarInputs = new org.areslib.hardware.sensors.ArrayLidarIO.ArrayLidarInputs();

        // Driver Station GUI Init
        AresDriverStationApp dsApp = new AresDriverStationApp();
        AresGamepad driverGamepad = new AresGamepad(dsApp.getGamepadWrapper().gamepad);

        System.out.println("Sim Started! Connect AdvantageScope to 127.0.0.1");

        // 3. Application Math Core
        try {
            while (true) {
                long startTime = System.currentTimeMillis();

                // 1. Update Gamepad Inputs
                dsApp.getGamepadWrapper().update();

                // 2. Fake TeleOp Control Mapping (Max 2.0 m/s and 2.0 rad/s)
                double driveY = driverGamepad.getLeftY() * -2.0;    // Forward (FTC Y is negative when pushed up)
                double driveX = driverGamepad.getLeftX() * -2.0;    // Strafe Left (FTC X is negative when pushed left, WPILib +Y is left)
                double driveTurn = driverGamepad.getRightX() * -2.0; // Turn Left (CCW is positive)
                
                // If triggers are pulled, boost speed
                if (dsApp.getGamepadWrapper().gamepad.right_trigger > 0.5) {
                    driveY *= 1.5; driveX *= 1.5;
                }
                
                driveSubsystem.drive(driveY, driveX, driveTurn);

                // Scheduler Tick
                CommandScheduler.getInstance().run();

                // Fake Physics Integration (20ms loop)
                double loopSecs = 0.02;
                double vx = driveSubsystem.getCommandedVx();      // Robot-centric forward (m/s)
                double vy = driveSubsystem.getCommandedVy();      // Robot-centric left (m/s)
                double omega = driveSubsystem.getCommandedOmega(); // rad/s
                
                double currentHeadingRad = odometryInputs.headingRadians;
                
                // Convert to field-centric
                double vXField = vx * Math.cos(currentHeadingRad) - vy * Math.sin(currentHeadingRad);
                double vYField = vx * Math.sin(currentHeadingRad) + vy * Math.cos(currentHeadingRad);
                
                // Integrate
                odometryInputs.headingRadians += omega * loopSecs;
                odometryInputs.xMeters += vXField * loopSecs;
                odometryInputs.yMeters += vYField * loopSecs;

                // Lidar Update
                lidarSim.updateInputs(lidarInputs);
                org.areslib.telemetry.AresAutoLogger.processInputs("Sensors/LiDAR", lidarInputs);
                org.areslib.telemetry.AresAutoLogger.processInputs("Pedro/Odometry", odometryInputs);
                
                // Publish Pose2d using the modern struct format to avoid deprecation warnings
                AresTelemetry.putPose2d("Robot/Pose", 
                    odometryInputs.xMeters, 
                    odometryInputs.yMeters, 
                    odometryInputs.headingRadians 
                );

                // Telemetry Event Push
                AresTelemetry.update();

                // Precise 50Hz sleep
                long loopTime = System.currentTimeMillis() - startTime;
                if (loopTime < 20) {
                    Thread.sleep(20 - loopTime);
                }
            }
        } catch (InterruptedException e) {
            System.err.println("Simulation Faulted: " + e.getMessage());
        }
    }
}
