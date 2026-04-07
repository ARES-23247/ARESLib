package org.areslib.templates;

import org.areslib.command.SequentialCommandGroup;
import org.areslib.command.FollowPathCommand;
import org.areslib.command.InstantCommand;
import org.areslib.command.WaitCommand;
import org.areslib.core.localization.AresFollower;

import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

/**
 * STARTER TEMPLATE: Basic mecanum autonomous using Pedro Pathing.
 * <p>
 * This demonstrates a simple 3-waypoint autonomous:
 * <ol>
 *   <li>Drive to scoring position</li>
 *   <li>Score a game piece</li>
 *   <li>Drive to parking zone</li>
 * </ol>
 *
 * <h3>Key Concepts:</h3>
 * <ul>
 *   <li>Pedro Pathing uses inches with origin at bottom-left (72, 72 = field center)</li>
 *   <li>Commands are chained with SequentialCommandGroup — no raw while loops</li>
 *   <li>Each path is wrapped in a FollowPathCommand for proper scheduling</li>
 * </ul>
 *
 * <h3>Quick Start:</h3>
 * <ol>
 *   <li>Copy this file into your teamcode package</li>
 *   <li>Update the start/scoring/parking coordinates for your field setup</li>
 *   <li>Add your mechanism commands (intake, scoring, etc.)</li>
 * </ol>
 */
public class BasicMecanumAuto {

    // ========== Field Coordinates (Pedro inches, bottom-left origin) ==========
    // Field center = (72, 72). +X is right, +Y is forward.
    // Adjust these for your alliance and starting position.

    private static final Pose START_POSE = new Pose(24, 72, 0);       // Left side start
    private static final Pose SCORING_POSE = new Pose(24, 110, 0);   // Near the goal
    private static final Pose PARKING_POSE = new Pose(60, 110, 0);   // Parking zone

    /**
     * Builds the complete autonomous command sequence.
     *
     * @param follower The configured AresFollower (from your OpMode setup)
     * @return A command that runs the entire autonomous routine
     */
    public static SequentialCommandGroup buildAuto(AresFollower follower) {

        // Path 1: Start → Scoring position
        PathChain pathToScoring = follower.getFollower().pathBuilder()
                .addPath(new BezierLine(START_POSE, SCORING_POSE))
                .setLinearHeadingInterpolation(0, 0) // Keep heading constant at 0 rad
                .build();

        // Path 2: Scoring → Parking
        PathChain pathToParking = follower.getFollower().pathBuilder()
                .addPath(new BezierLine(SCORING_POSE, PARKING_POSE))
                .setLinearHeadingInterpolation(0, 0)
                .build();

        // Chain it all together:
        return new SequentialCommandGroup(
                // Drive to scoring zone
                new FollowPathCommand(follower, pathToScoring),

                // Score (replace with your mechanism command)
                new InstantCommand(() -> System.out.println("SCORING!")),
                new WaitCommand(1.0),

                // Drive to parking
                new FollowPathCommand(follower, pathToParking),

                // Done!
                new InstantCommand(() -> System.out.println("AUTO COMPLETE"))
        );
    }

    /*
     * ========== Usage in your OpMode ==========
     *
     * @Override
     * public void initialize() {
     *     AresFollower follower = new AresFollower(hardwareMap, startPose);
     *     Command auto = BasicMecanumAuto.buildAuto(follower);
     *     auto.schedule();
     * }
     *
     * @Override
     * public void run() {
     *     CommandScheduler.getInstance().run();
     * }
     */
}
