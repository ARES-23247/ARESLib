package org.areslib.command;

import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;

/**
 * A state machine representing a complete action to be performed by the robot. Commands are
 * run by the CommandScheduler, and can be composed into CommandGroups to allow users to build
 * complicated multi-step actions without the need to write a state machine.
 */
public abstract class Command {
    protected Set<Subsystem> m_requirements = new HashSet<>();

    protected Command() {}

    /**
     * The initial subroutine of a command. Called once when the command is initially scheduled.
     */
    public void initialize() {}

    /**
     * The main body of a command. Called repeatedly while the command is scheduled.
     */
    public void execute() {}

    /**
     * The action to take when the command ends. Called when either the command finishes normally,
     * or when it interrupted.
     *
     * @param interrupted whether the command was interrupted/canceled
     */
    public void end(boolean interrupted) {}

    /**
     * Whether the command has finished. Once a command finishes, the scheduler will call its
     * end() method and un-schedule it.
     *
     * @return whether the command has finished.
     */
    public boolean isFinished() {
        return false;
    }

    /**
     * Specifies that the given subsystems are used by this command.
     *
     * @param requirements the subsystems the command requires
     */
    public final void addRequirements(Subsystem... requirements) {
        m_requirements.addAll(Arrays.asList(requirements));
    }

    /**
     * Gets the subsystems required by this command.
     *
     * @return the set of required subsystems
     */
    public Set<Subsystem> getRequirements() {
        return m_requirements;
    }
}
