package org.firstinspires.ftc.teamcode.teleop;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.areslib.core.AresCommandOpMode;
import org.areslib.command.CommandScheduler;
import org.areslib.command.Command;
import org.areslib.hardware.AresHardwareManager;
import org.areslib.hardware.wrappers.AresGamepad;
import org.areslib.hardware.wrappers.DcMotorExWrapper;
import org.areslib.hardware.wrappers.CRServoWrapper;
import org.areslib.hardware.wrappers.LimelightVisionWrapper;
import org.areslib.hardware.wrappers.AresOctoQuadSensor;
import org.areslib.hardware.coprocessors.OctoMode;

import org.areslib.subsystems.drive.SwerveDriveSubsystem;
import org.areslib.subsystems.drive.SwerveModuleIOReal;
import org.areslib.telemetry.AresTelemetry;
import org.areslib.telemetry.AndroidDashboardBackend;

import org.firstinspires.ftc.teamcode.subsystems.elevator.ElevatorSubsystem;
import org.firstinspires.ftc.teamcode.subsystems.elevator.ElevatorIOReal;
import org.firstinspires.ftc.teamcode.subsystems.vision.VisionSubsystem;

import org.firstinspires.ftc.teamcode.commands.ElevatorToPositionCommand;
import org.firstinspires.ftc.teamcode.commands.AlignToTagCommand;

/**
 * Advanced Command-Based TeleOp demonstrating multiple subsystems and automated alignment.
 */
@TeleOp(name = "Team Template: Advanced Command TeleOp", group = "Teamcode")
public class MainTeleOp extends AresCommandOpMode {

    private SwerveDriveSubsystem drive;
    private ElevatorSubsystem elevator;
    private VisionSubsystem vision;
    
    private AresGamepad driver;
    private AresGamepad operator;

    @Override
    public void robotInit() {
        // 1. Initialize Telemetry Backends
        AresTelemetry.registerBackend(new AndroidDashboardBackend());

        // 2. Hardware Bulk Caching Initialization
        AresHardwareManager.initHardware(hardwareMap);

        // 3. Subsystem Initialization
        // - Swerve Drive
        drive = new SwerveDriveSubsystem(
            // Front Left Module
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "flDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "flTurn")),      
                new AresOctoQuadSensor(0, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(4, OctoMode.ABSOLUTE)                       
            ),
            // Front Right Module
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "frDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "frTurn")),      
                new AresOctoQuadSensor(1, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(5, OctoMode.ABSOLUTE)                       
            ),
            // Back Left Module
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "blDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "blTurn")),      
                new AresOctoQuadSensor(2, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(6, OctoMode.ABSOLUTE)                       
            ),
            // Back Right Module
            new SwerveModuleIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "brDrive")), 
                new CRServoWrapper(hardwareMap.get(CRServo.class, "brTurn")),      
                new AresOctoQuadSensor(3, OctoMode.ENCODER),                       
                new AresOctoQuadSensor(7, OctoMode.ABSOLUTE)                       
            )
        );
        CommandScheduler.getInstance().registerSubsystem(drive);
        
        // - Elevator
        elevator = new ElevatorSubsystem(
            new ElevatorIOReal(
                new DcMotorExWrapper(hardwareMap.get(DcMotorEx.class, "elevatorMotor")),
                0.005 // Distance per tick meters
            )
        );
        CommandScheduler.getInstance().registerSubsystem(elevator);
        
        // - Vision (Limelight 3A)
        vision = new VisionSubsystem(
            new LimelightVisionWrapper(hardwareMap, "limelight")
        );
        CommandScheduler.getInstance().registerSubsystem(vision);

        // 4. Input Bindings
        driver = new AresGamepad(gamepad1);
        operator = new AresGamepad(gamepad2);

        // --- Default Commands ---
        // Field-centric Swerve
        CommandScheduler.getInstance().setDefaultCommand(drive, new Command() {
            public Command init() {
                addRequirements(drive);
                return this;
            }
            @Override
            public void execute() {
                drive.drive(
                    driver.getLeftY() * 3.0,
                    driver.getLeftX() * 3.0,
                    driver.getRightX() * 2.5
                );
            }
        }.init());

        // --- Explicit Trigger Commands ---
        
        // Hold A to Auto-Align to AprilTag while driving manually
        driver.a().whileTrue(
            new AlignToTagCommand(drive, vision, 5.0) // Try to reach 5% area targeting
        );
        
        // Press Dpad Up to run Elevator to 0.8 meters
        operator.dpadUp().onTrue(
            new ElevatorToPositionCommand(elevator, 0.8)
        );
        
        // Press Dpad Down to return Elevator to Home (0.0m)
        operator.dpadDown().onTrue(
            new ElevatorToPositionCommand(elevator, 0.0)
        );
    }
}
