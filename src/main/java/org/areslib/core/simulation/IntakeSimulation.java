package org.areslib.core.simulation;

import java.util.ArrayDeque;
import java.util.Queue;
import org.dyn4j.collision.CollisionBody;
import org.dyn4j.collision.Fixture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.BodyFixture;
import org.dyn4j.dynamics.contact.Contact;
import org.dyn4j.geometry.Convex;
import org.dyn4j.world.ContactCollisionData;
import org.dyn4j.world.listener.ContactListenerAdapter;

/**
 * Simulates a hardware intake mathematically registering overlapping game elements. Employs
 * zero-allocation arrays internally to bypass GC jitter when flagging collisions.
 */
public class IntakeSimulation extends BodyFixture {

  private final Body parentChassis;
  private boolean isRunning = false;
  private final String targetTag;

  // Use bounded array deque to prevent allocations
  private final Queue<Body> bodiesToRemove = new ArrayDeque<>(10);
  private int collectedCount = 0;
  private final int capacity;

  /**
   * Creates a new intake simulation region.
   *
   * @param shape The shape corresponding to the extended intake zone.
   * @param parentChassis The dyn4j body of the robot this is attached to.
   * @param targetTag The user data tag that indicates a valid game piece (e.g. "DECODE_ARTIFACT")
   * @param capacity Maximum amount of pieces the robot can hold simultaneously
   */
  public IntakeSimulation(Convex shape, Body parentChassis, String targetTag, int capacity) {
    super(shape);
    super.setDensity(0); // Intake fixture represents a zone, not a massive object
    super.setSensor(true); // Don't physically bounce things off the intake region
    this.parentChassis = parentChassis;
    this.targetTag = targetTag;
    this.capacity = capacity;

    // Register listener with the physics world
    AresPhysicsWorld.getInstance().addContactListener(new IntakeContactListener());
  }

  public void startIntake() {
    if (isRunning) return;
    parentChassis.addFixture(this);
    isRunning = true;
  }

  public void stopIntake() {
    if (!isRunning) return;
    parentChassis.removeFixture(this);
    isRunning = false;
  }

  public boolean isRunning() {
    return isRunning;
  }

  public int getCollectedCount() {
    return collectedCount;
  }

  /**
   * Processes any pending obtained pieces. Removes them mechanically from the dyn4j environment.
   */
  public void processPendingTakes() {
    while (!bodiesToRemove.isEmpty()) {
      Body artifact = bodiesToRemove.poll();
      AresPhysicsWorld.getInstance().removeBody(artifact);
    }
  }

  /**
   * Call this when your physical mechanism moves pieces from intake storage into a shooter or
   * scoring bucket
   */
  public boolean takePieceFromIntake() {
    if (collectedCount > 0) {
      collectedCount--;
      return true;
    }
    return false;
  }

  /** Internal dyn4j interceptor */
  private final class IntakeContactListener extends ContactListenerAdapter<Body> {
    @Override
    public void begin(ContactCollisionData<Body> collision, Contact contact) {
      if (!isRunning) return;
      if (collectedCount >= capacity) return;

      final CollisionBody<?> body1 = collision.getBody1();
      final CollisionBody<?> body2 = collision.getBody2();
      final Fixture fix1 = collision.getFixture1();
      final Fixture fix2 = collision.getFixture2();

      Body artifact = null;

      if (fix1 == IntakeSimulation.this && targetTag.equals(body2.getUserData())) {
        artifact = (Body) body2;
      } else if (fix2 == IntakeSimulation.this && targetTag.equals(body1.getUserData())) {
        artifact = (Body) body1;
      }

      if (artifact != null && !bodiesToRemove.contains(artifact)) {
        bodiesToRemove.offer(artifact);
        collectedCount++;
      }
    }
  }
}
