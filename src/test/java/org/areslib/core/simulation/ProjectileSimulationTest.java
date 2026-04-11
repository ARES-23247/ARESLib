package org.areslib.core.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ProjectileSimulationTest {

  @BeforeEach
  void setUp() {
    AresPhysicsWorld.getInstance().reset();
  }

  @Test
  void testProjectileTrajectory() {
    // Shoot straight up at 11 m/s from 0 height. Gravity is 11 m/s^2.
    // It should reach velocity=0 at t=1.0s, height = 5.5m.
    // It should touch ground at t=2.0s.

    double[] pos = {0.0, 0.0, 0.0};
    double[] vel = {0.0, 0.0, 11.0};

    ProjectileSimulation projectile = new ProjectileSimulation(pos, vel);
    projectile.setTouchGroundHeightOffset(0.0);

    // Step forward 1 second
    projectile.step(1.0);
    assertFalse(projectile.isGrounded());

    double[] currentPos = new double[3];
    projectile.getCurrentPosition(currentPos);
    assertEquals(0.0, currentPos[0], 0.01);
    assertEquals(0.0, currentPos[1], 0.01);
    assertEquals(5.5, currentPos[2], 0.01);

    // Step forward another 1.01 seconds -> it should hit ground and clamp
    projectile.step(1.01);

    assertTrue(projectile.isGrounded(), "Projectile should be grounded");

    projectile.getCurrentPosition(currentPos);
    assertTrue(currentPos[2] < 0.0);

    // Spawning artifact
    projectile.spawnPhysicalBody(AresPhysicsWorld.getInstance());
    assertEquals(1, AresPhysicsWorld.getInstance().getWorld().getBodyCount());
  }
}
