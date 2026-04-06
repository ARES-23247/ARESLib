package org.firstinspires.ftc.teamcode.commands;

import org.areslib.command.Command;
import org.firstinspires.ftc.teamcode.subsystems.elevator.ElevatorSubsystem;

public class ElevatorToPositionCommand extends Command {
    private final ElevatorSubsystem elevator;
    private final double targetPositionMeters;

    public ElevatorToPositionCommand(ElevatorSubsystem elevator, double targetPositionMeters) {
        this.elevator = elevator;
        this.targetPositionMeters = targetPositionMeters;
        addRequirements(elevator);
    }

    @Override
    public void initialize() {
        elevator.setTargetPosition(targetPositionMeters);
    }

    @Override
    public void execute() {
        // Continuous logic would go here if position was interpolated mapping
    }

    @Override
    public boolean isFinished() {
        // Exit command when the physical position is within 0.05 meters of target
        return Math.abs(elevator.getPositionMeters() - targetPositionMeters) < 0.05;
    }

    @Override
    public void end(boolean interrupted) {
        // Optional logic when reaching target or being overridden
    }
}
