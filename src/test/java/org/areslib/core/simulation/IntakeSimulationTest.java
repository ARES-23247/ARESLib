package org.areslib.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IntakeSimulationTest {

  @BeforeEach
  void setUp() {
    AresPhysicsWorld.getInstance().reset();
  }

  @Test
  void testIntakeCollectsDecoupledArtifact() {
    Body robot = new Body();
    robot.addFixture(Geometry.createRectangle(0.4, 0.4));
    robot.translate(0, 0);
    robot.setMass(MassType.NORMAL);
    AresPhysicsWorld.getInstance().addBody(robot);

    // Spawn a theoretical artifact at x=0.5
    Body artifact = new Body();
    artifact.addFixture(Geometry.createCircle(DecodeFieldSim.ARTIFACT_RADIUS_METERS));
    artifact.translate(0.5, 0);
    artifact.setMass(MassType.NORMAL);
    artifact.setUserData("DECODE_ARTIFACT");
    AresPhysicsWorld.getInstance().addBody(artifact);

    IntakeSimulation intake =
        new IntakeSimulation(Geometry.createRectangle(0.6, 0.6), robot, "DECODE_ARTIFACT", 1);
    intake.startIntake();

    // Push the artifact into the robot
    artifact.setAtRest(false);
    artifact.setLinearVelocity(new org.dyn4j.geometry.Vector2(-1.0, 0.0));

    // Step the world repeatedly to resolve collision
    for (int i = 0; i < 50; i++) {
      AresPhysicsWorld.getInstance().step(0.01);
      intake.processPendingTakes();
      System.out.println("Artifact X: " + artifact.getTransform().getTranslationX());
    }

    System.out.println("Collected Count: " + intake.getCollectedCount());
    assertEquals(1, intake.getCollectedCount());

    boolean hasArtifact = false;
    for (Body b : AresPhysicsWorld.getInstance().getWorld().getBodies()) {
      if ("DECODE_ARTIFACT".equals(b.getUserData())) {
        hasArtifact = true;
      }
    }
    assertFalse(hasArtifact, "Artifact should have been removed from the physical world");

    assertTrue(intake.takePieceFromIntake(), "Should successfully take piece from intake");
    assertEquals(0, intake.getCollectedCount());
  }
}
