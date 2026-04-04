package org.areslib.command;

/**
 * A base for subsystems that handles registration in the constructor, and provides a more
 * intuitive API for requiring the subsystem.
 */
public abstract class SubsystemBase implements Subsystem {

    public SubsystemBase() {
        CommandScheduler.getInstance().registerSubsystem(this);
    }

}
