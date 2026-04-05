package org.areslib.core.localization;

import com.acmerobotics.dashboard.config.Config;
import com.pedropathing.follower.FollowerConstants;

@Config
public class AresPedroConstants {
    // These constants should be configured by the team in TeamCode, but we provide
    // some baseline functional definitions here for ARESlib integration

    public static double mass = 12.0; // Robot mass in kg

    // Drive PID values for path following
    public static double drivePIDFCoefficients[] = {0.0, 0.0, 0.0, 0.0};
    
    // Heading PID values
    public static double headingPIDFCoefficients[] = {0.0, 0.0, 0.0, 0.0};

    // Translation PID values
    public static double translationalPIDFCoefficients[] = {0.0, 0.0, 0.0, 0.0};

    // TeleOp Telemetry & Sim Tuning Variables
    public static double teleOpMaxSpeedForward = 2.0; // max Y speed m/s
    public static double teleOpMaxSpeedStrafe = 2.0;  // max X speed m/s
    public static double teleOpMaxTurnRads = 2.0;     // max turn speed rad/s
    public static double teleOpBoostMultiplier = 1.5; // multiplier when right trigger pulled

    /**
     * Call this in the user's autonomous init to inject custom PID tuned values
     * into Pedro's FollowerConstants.
     */
    public static FollowerConstants createConstants() {
        FollowerConstants constants = new FollowerConstants();
        constants.mass = mass;
        // Optional: Map array coefficients cleanly into FollowerConstants if needed here
        // Pedro will use these values natively for path calculation.
        return constants;
    }
}
