package org.areslib.subsystems;

import java.util.function.DoubleSupplier;
import java.util.function.Supplier;
import org.areslib.command.Command;
import org.areslib.command.InstantCommand;
import org.areslib.command.SubsystemBase;
import org.areslib.core.StateMachine;
import org.areslib.math.geometry.Pose2d;
import org.areslib.telemetry.AresAutoLogger;

/**
 * Base class for a robot Superstructure.
 *
 * <p>A Superstructure is a "meta-subsystem" that coordinates multiple smaller subsystems (Intake,
 * Arm, Shooter) into high-level logical states. It uses an internal {@link StateMachine} to ensure
 * transition safety and deterministic behavior.
 *
 * <p>Includes built-in "Beached" safety (automatic mechanism disable on high robot tilt).
 *
 * @param <S> The enum type representing superstructure states.
 */
public abstract class AresSuperstructure<S extends Enum<S>> extends SubsystemBase {

  protected final StateMachine<S> stateMachine;
  protected final Supplier<Pose2d> poseSupplier;
  protected final DoubleSupplier tiltDegreesSupplier;

  private final S stowedState;
  private final S beachedState;

  private double beachThresholdDegrees = 25.0;
  private double recoveryThresholdDegrees = 20.0;

  /**
   * Constructs the superstructure.
   *
   * @param name Name for telemetry keys.
   * @param enumClass The state enum class.
   * @param initialState The state to start in.
   * @param stowedState The state representing a "safe/compressed" configuration.
   * @param beachedState The state representing "disabled/safe" on high tilt.
   * @param poseSupplier Supplier for robot field pose.
   * @param tiltDegreesSupplier Supplier for robot tilt in degrees.
   */
  public AresSuperstructure(
      String name,
      Class<S> enumClass,
      S initialState,
      S stowedState,
      S beachedState,
      Supplier<Pose2d> poseSupplier,
      DoubleSupplier tiltDegreesSupplier) {

    this.stateMachine = new StateMachine<>(name, enumClass, initialState);
    this.stowedState = stowedState;
    this.beachedState = beachedState;
    this.poseSupplier = poseSupplier;
    this.tiltDegreesSupplier = tiltDegreesSupplier;

    // Wildcard: Any state can go to BEACED or STOWED for safety
    stateMachine.addWildcardTo(stowedState);
    stateMachine.addWildcardTo(beachedState);
  }

  /**
   * Configures the tilt thresholds for beached mode.
   *
   * @param beach Threshold to enter beached mode.
   * @param recovery Threshold to exit beached mode back to stowed.
   */
  public void setBeachThresholds(double beach, double recovery) {
    this.beachThresholdDegrees = beach;
    this.recoveryThresholdDegrees = recovery;
  }

  /**
   * Request a transition to a new state. Transition will be rejected if illegal in the state
   * machine transition table.
   *
   * @param target The desired state.
   * @return A command to perform the request.
   */
  public Command requestState(S target) {
    return new InstantCommand(() -> stateMachine.requestTransition(target), this);
  }

  /**
   * Force the superstructure into a state, bypassing transition table logic.
   *
   * @param target The state to force.
   */
  public void forceState(S target) {
    stateMachine.forceState(target);
  }

  @Override
  public void periodic() {
    // 1. Tilt Safety (Beached Mode)
    double currentTilt = Math.abs(tiltDegreesSupplier.getAsDouble());

    if (currentTilt > beachThresholdDegrees && stateMachine.getState() != beachedState) {
      forceState(beachedState);
    } else if (currentTilt < recoveryThresholdDegrees && stateMachine.getState() == beachedState) {
      forceState(stowedState);
    }

    // 2. Update State Machine
    stateMachine.update();

    // 3. User Logic
    S currentState = stateMachine.getState();
    updateMechanisms(currentState);

    // 4. Telemetry
    AresAutoLogger.recordOutput(
        "Superstructure/" + stateMachine.getName() + "/CurrentState", currentState.name());
    AresAutoLogger.recordOutput(
        "Superstructure/" + stateMachine.getName() + "/TiltDegrees", currentTilt);
  }

  /**
   * Subclasses implement this to drive subsystem targets based on the current superstructure state.
   *
   * @param state The current active state.
   */
  protected abstract void updateMechanisms(S state);

  public S getState() {
    return stateMachine.getState();
  }
}
