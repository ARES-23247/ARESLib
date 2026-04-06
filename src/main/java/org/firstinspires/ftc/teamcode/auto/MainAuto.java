package org.firstinspires.ftc.teamcode.auto;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.areslib.core.AresCommandOpMode;
import org.areslib.command.CommandScheduler;
import org.areslib.command.Subsystem;
import org.areslib.hardware.AresHardwareManager;
import org.areslib.hardware.wrappers.DcMotorExWrapper;
import org.areslib.hardware.wrappers.CRServoWrapper;
import org.areslib.hardware.wrappers.AresOctoQuadSensor;
import org.areslib.hardware.coprocessors.OctoMode;

import org.areslib.subsystems.drive.SwerveDriveSubsystem;
import org.areslib.subsystems.drive.SwerveModuleIOReal;
import org.areslib.telemetry.AresTelemetry;
import org.areslib.telemetry.AndroidDashboardBackend;

import org.firstinspires.ftc.teamcode.subsystems.elevator.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.elevator.ElevatorIOReal;
import org.areslib.core.localization.AresFollower;
import org.areslib.hardware.wrappers.PinpointOdometryWrapper;
import org.areslib.hardware.interfaces.OdometryIO;
import org.firstinspires.ftc.teamcode.commands.TeamAutoCommand;

@Autonomous(name = "Team Template: Pedro Path Auto", group = "Teamcode")
public class MainAuto extends AresCommandOpMode {

    private SwerveDriveSubsystem drive;
    private ElevatorSubsystem elevator;
    private AresFollower follower;
    private PinpointOdometryWrapper pinpoint;
    private final OdometryIO.OdometryInputs pinpointInputs = new OdometryIO.OdometryInputs();

    @Override
    public void robotInit() {
        // 1. Telemetry
        AresTelemetry.registerBackend(new AndroidDashboardBackend());

        // 2. Hardware Bulk Caching
        AresHardwareManager.initHardware(hardwareMap);

        // 3. Subsystems
        drive = new SwerveDriveSubsystem(
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "flDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "flTurn")),      
                new AresOctoQuadSensor(0, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(4, OctoMode.ABSOLUTE)                       
            ),
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "frDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "frTurn")),      
                new AresOctoQuadSensor(1, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(5, OctoMode.ABSOLUTE)                       
            ),
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "blDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "blTurn")),      
                new AresOctoQuadSensor(2, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(6, OctoMode.ABSOLUTE)                       
            ),
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "brDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "brTurn")),      
                new AresOctoQuadSensor(3, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(7, OctoMode.ABSOLUTE)                       
            )
        );
        CommandScheduler.getInstance().registerSubsystem(drive);
        
        elevator = new ElevatorSubsystem(
            new ElevatorIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "elevatorMotor")),
                0.005 // Distance per tick meters
            )
        );
        CommandScheduler.getInstance().registerSubsystem(elevator);

        // 4. Odometry & Follower Integration
        pinpoint = new PinpointOdometryWrapper(hardwareMap, "pinpoint");
        
        // We create an inline anonymous subsystem specifically to poll odometry.
        // This ensures tracking data is refreshed automatically every scheduler cycle.
        CommandScheduler.getInstance().registerSubsystem(new Subsystem() {
            @Override
            public void periodic() {
                pinpoint.updateInputs(pinpointInputs);
            }
        });

        // Instantiate follower wrapped in ARESLib integration
        follower = new AresFollower(drive, pinpointInputs);
        CommandScheduler.getInstance().registerSubsystem(follower);

        // 5. Schedule the autonomous routine!
        CommandScheduler.getInstance().schedule(new TeamAutoCommand(follower, elevator));
    }
}
