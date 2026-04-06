package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import org.areslib.core.AresCommandOpMode;
import org.areslib.command.CommandScheduler;
import org.areslib.telemetry.AresTelemetry;
import org.areslib.telemetry.AndroidDashboardBackend;
import org.firstinspires.ftc.teamcode.RobotContainer;

@Autonomous(name = "Team Template: Pedro Path Auto", group = "Teamcode")
public class MainAuto extends AresCommandOpMode {

    private RobotContainer robot;

    @Override
    public void robotInit() {
        // 1. Telemetry
        AresTelemetry.registerBackend(new AndroidDashboardBackend());

        // 2. Initialize Hardware map state via the core architectural container
        robot = new RobotContainer(hardwareMap, gamepad1, gamepad2);

        // 3. Schedule the autonomous routine!
        CommandScheduler.getInstance().schedule(robot.getAutonomousCommand());
    }
}
