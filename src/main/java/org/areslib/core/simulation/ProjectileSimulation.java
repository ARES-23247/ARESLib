package org.areslib.core.simulation;

import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;

/**
 * Mathematically pseudo-simulates a 3D projectile launched from a robot.
 *
 * <p><strong>Mathematical References:</strong>
 *
 * <ul>
 *   <li>Kinematic Equations for Projectile Motion: <a
 *       href="https://en.wikipedia.org/wiki/Projectile_motion">Wikipedia - Projectile Motion</a>
 *   <li>Uniformly Accelerated Motion (Z-axis): d = v₀t + ½at²
 * </ul>
 */
public class ProjectileSimulation {

  // Standard projectile gravity in m/s^2 (adjust if necessary based on real-world measurements
  // backing dyn4j scale)
  public static final double GRAVITY = 11.0;

  private final double initialXMeters;
  private final double initialYMeters;
  private final double initialZMeters; // Height

  private final double initialVelXMps;
  private final double initialVelYMps;
  private final double initialVelZMps;

  private double timeElapsedSec = 0.0;
  private boolean isGrounded = false;
  private double touchGroundHeightOffset = 0.0635; // Default to DECODE Artifact Radius

  /**
   * Constructs a ProjectileSimulation using 3D position and velocity arrays to avoid object
   * allocation.
   *
   * @param initialPosMeters Array containing {x, y, z} in meters
   * @param initialVelMps Array containing {vx, vy, vz} in meters per second
   */
  public ProjectileSimulation(double[] initialPosMeters, double[] initialVelMps) {
    this.initialXMeters = initialPosMeters[0];
    this.initialYMeters = initialPosMeters[1];
    this.initialZMeters = initialPosMeters[2];

    this.initialVelXMps = initialVelMps[0];
    this.initialVelYMps = initialVelMps[1];
    this.initialVelZMps = initialVelMps[2];
  }

  public void setTouchGroundHeightOffset(double touchGroundHeightOffset) {
    this.touchGroundHeightOffset = touchGroundHeightOffset;
  }

  /**
   * Steps the simulation time forward.
   *
   * @param dtSeconds time passed in seconds
   */
  public void step(double dtSeconds) {
    if (isGrounded) return;
    timeElapsedSec += dtSeconds;

    double currentZMeters =
        initialZMeters
            + initialVelZMps * timeElapsedSec
            - 0.5 * GRAVITY * timeElapsedSec * timeElapsedSec;
    double currentVelZMps = initialVelZMps - GRAVITY * timeElapsedSec;

    if (currentZMeters <= touchGroundHeightOffset && currentVelZMps < 0) {
      isGrounded = true;
    }
  }

  public boolean isGrounded() {
    return isGrounded;
  }

  public boolean hasGoneOutOfField() {
    double currentXMeters = initialXMeters + initialVelXMps * timeElapsedSec;
    double currentYMeters = initialYMeters + initialVelYMps * timeElapsedSec;
    // FTC field is +- 1.83m from center
    final double edgeTolerance = 0.5;
    return currentXMeters < (-1.83 - edgeTolerance)
        || currentXMeters > (1.83 + edgeTolerance)
        || currentYMeters < (-1.83 - edgeTolerance)
        || currentYMeters > (1.83 + edgeTolerance);
  }

  /**
   * Populates the passed array with the current {x,y,z} pseudo-3D location.
   *
   * @param outPosMeters A pre-allocated double[3] to store the current position
   */
  public void getCurrentPosition(double[] outPosMeters) {
    outPosMeters[0] = initialXMeters + initialVelXMps * timeElapsedSec;
    outPosMeters[1] = initialYMeters + initialVelYMps * timeElapsedSec;
    outPosMeters[2] =
        initialZMeters
            + initialVelZMps * timeElapsedSec
            - 0.5 * GRAVITY * timeElapsedSec * timeElapsedSec;
  }

  /**
   * Converts this active projectile back into a Dyn4j simulated Field Body.
   *
   * @param worldWrapper the global simulation wrapper
   */
  public void spawnPhysicalBody(AresPhysicsWorld worldWrapper) {
    double currentXMeters = initialXMeters + initialVelXMps * timeElapsedSec;
    double currentYMeters = initialYMeters + initialVelYMps * timeElapsedSec;

    Body artifact = new Body();
    BodyFixture fixture =
        artifact.addFixture(Geometry.createCircle(DecodeFieldSim.ARTIFACT_RADIUS_METERS));

    fixture.setDensity(50.0);
    fixture.setFriction(0.3);
    fixture.setRestitution(0.6);

    artifact.translate(currentXMeters, currentYMeters);
    artifact.setMass(MassType.NORMAL);
    artifact.setUserData("DECODE_ARTIFACT");

    artifact.setLinearVelocity(initialVelXMps, initialVelYMps);

    artifact.setLinearDamping(1.5);
    artifact.setAngularDamping(1.5);

    worldWrapper.addBody(artifact);
  }
}
