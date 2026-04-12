package org.areslib.core.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.areslib.command.CommandScheduler;
import org.areslib.command.Subsystem;
import org.areslib.core.AresRobot;

/**
 * High-performance backend orchestrator for decoupled physics constraints. Separates physical
 * OpMode robot loops from Dyn4j spatial calculations.
 */
public class AresSimulator {
  private static Thread physicsThread = null;
  private static volatile boolean isRunning = false;

  /** Pre-allocated buffer for DECODE_ARTIFACT pose telemetry (grown as needed). */
  private static double[] artifactPosesCache = new double[30]; // 10 artifacts * 3 doubles

  /** Pre-allocated slice for telemetry output — avoids Arrays.copyOf per tick. */
  private static double[] artifactSliceCache = new double[30];

  /** Pre-allocated subsystem snapshot list — avoids new ArrayList per tick. */
  private static final List<Subsystem> SUBSYSTEM_SNAPSHOT = new ArrayList<>(16);

  /**
   * Starts an independent background thread that executes subsystem.simulationPeriodic()
   * iteratively at a specified cycle rate.
   *
   * @param periodMs The length of the targeted physics tick period in milliseconds (ex: 5ms for
   *     200Hz).
   */
  @SuppressWarnings("ThreadPriorityCheck")
  public static synchronized void startPhysicsSim(int periodMs) {
    // Prevent running if not in a simulated environment
    if (!AresRobot.isSimulation()) {
      com.qualcomm.robotcore.util.RobotLog.w(
          "AresSimulator warning: Ignoring start command; AresRobot is not in simulated mode.");
      return;
    }

    if (isRunning) return;
    isRunning = true;

    // Populate the physical world with DECODE assets
    DecodeFieldSim.buildField();

    physicsThread =
        new Thread(
            () -> {
              while (isRunning && AresRobot.isSimulation()) {
                long start = System.currentTimeMillis();

                // Step the centralized physics world
                double dtSeconds = periodMs / 1000.0;
                AresPhysicsWorld.getInstance().step(dtSeconds);

                // Publish DECODE_ARTIFACTs to telemetry for AdvantageScope
                java.util.List<org.dyn4j.dynamics.Body> bodies =
                    AresPhysicsWorld.getInstance().getWorld().getBodies();
                int artifactCount = 0;
                for (org.dyn4j.dynamics.Body body : bodies) {
                  if ("DECODE_ARTIFACT".equals(body.getUserData())) {
                    artifactCount++;
                  }
                }

                if (artifactCount > 0) {
                  int requiredLength = artifactCount * 3;
                  if (artifactPosesCache.length < requiredLength) {
                    artifactPosesCache = new double[requiredLength];
                  }
                  int ix = 0;
                  for (org.dyn4j.dynamics.Body body : bodies) {
                    if ("DECODE_ARTIFACT".equals(body.getUserData())) {
                      artifactPosesCache[ix * 3] = body.getTransform().getTranslationX();
                      artifactPosesCache[ix * 3 + 1] = body.getTransform().getTranslationY();
                      artifactPosesCache[ix * 3 + 2] = body.getTransform().getRotationAngle();
                      ix++;
                    }
                  }
                  // Only publish the filled portion of the cache — reuse slice buffer
                  if (artifactSliceCache.length < requiredLength) {
                    artifactSliceCache = new double[requiredLength];
                  }
                  System.arraycopy(artifactPosesCache, 0, artifactSliceCache, 0, requiredLength);
                  org.areslib.telemetry.AresAutoLogger.recordOutputArray(
                      "Simulation/DECODE_ARTIFACTs",
                      Arrays.copyOf(artifactSliceCache, requiredLength));
                }

                // Snapshot the subsystems set to avoid ConcurrentModificationException —
                // the main thread may register/unregister subsystems concurrently.
                // Reuse the pre-allocated snapshot list to avoid per-tick allocation.
                SUBSYSTEM_SNAPSHOT.clear();
                SUBSYSTEM_SNAPSHOT.addAll(CommandScheduler.getInstance().getSubsystems());
                for (int si = 0; si < SUBSYSTEM_SNAPSHOT.size(); si++) {
                  SUBSYSTEM_SNAPSHOT.get(si).simulationPeriodic();
                }

                long elapsed = System.currentTimeMillis() - start;
                long sleepTime = periodMs - elapsed;

                if (sleepTime > 0) {
                  try {
                    Thread.sleep(sleepTime);
                  } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                  }
                }
              }
            });

    // Mark as daemon so it dies implicitly alongside the main application thread
    physicsThread.setDaemon(true);
    // Ensure simulation engine evaluates prior to standard background operations
    physicsThread.setPriority(Thread.MAX_PRIORITY);
    physicsThread.start();
  }

  /**
   * Returns whether the physics simulation thread is currently active. Used by {@link
   * org.areslib.command.CommandScheduler} to avoid double-calling {@code simulationPeriodic()} when
   * the high-frequency physics thread is already running.
   *
   * @return true if the physics thread is actively running
   */
  public static boolean isPhysicsRunning() {
    return isRunning;
  }

  /** Halts the background physics engine processing. */
  public static synchronized void stopPhysicsSim() {
    isRunning = false;
    if (physicsThread != null) {
      physicsThread.interrupt();
      physicsThread = null;
    }
  }
}
