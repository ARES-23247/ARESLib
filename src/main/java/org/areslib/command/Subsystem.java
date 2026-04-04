package org.areslib.command;

public interface Subsystem {
    /**
     * This method is called periodically by the CommandScheduler. Useful for updating
     * subsystem-specific state that you don't want to defer to a Command.
     */
    default void periodic() {}

    /**
     * This method is called periodically by the CommandScheduler. Useful for updating
     * simulation-specific state that you don't want to defer to a Command.
     */
    default void simulationPeriodic() {}
}
