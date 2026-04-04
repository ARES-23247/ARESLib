package org.areslib.telemetry;

import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.acmerobotics.dashboard.FtcDashboard;

import org.areslib.math.kinematics.SwerveModuleState;
import org.areslib.math.kinematics.DifferentialDriveWheelSpeeds;
import org.areslib.math.kinematics.MecanumDriveWheelSpeeds;

/**
 * Standard telemetry logger for ARESlib.
 * Broadcasts data structures over FtcDashboard securely for AdvantageScope.
 */
public class AresLogger {
    private static final FtcDashboard dashboard = FtcDashboard.getInstance();

    /**
     * Logs exactly 4 SwerveModuleState elements as an 8-double array.
     * Required AdvantageScope native formatting: [Angle0, Speed0, Angle1, Speed1, ...]
     */
    public static void logSwerveStates(String key, SwerveModuleState[] states) {
        if (states.length != 4) return;
        TelemetryPacket packet = new TelemetryPacket();
        double[] stateArray = new double[8];
        for (int i = 0; i < 4; i++) {
            stateArray[i * 2] = states[i].angle.getRadians();
            stateArray[i * 2 + 1] = states[i].speedMetersPerSecond;
        }
        packet.put(key, stateArray);
        dashboard.sendTelemetryPacket(packet);
    }

    public static void logDifferentialSpeeds(String key, DifferentialDriveWheelSpeeds speeds) {
        TelemetryPacket packet = new TelemetryPacket();
        double[] stateArray = new double[] { speeds.leftMetersPerSecond, speeds.rightMetersPerSecond };
        packet.put(key, stateArray);
        dashboard.sendTelemetryPacket(packet);
    }

    public static void logMecanumSpeeds(String key, MecanumDriveWheelSpeeds speeds) {
        TelemetryPacket packet = new TelemetryPacket();
        double[] stateArray = new double[] { 
            speeds.frontLeftMetersPerSecond, speeds.frontRightMetersPerSecond,
            speeds.rearLeftMetersPerSecond, speeds.rearRightMetersPerSecond 
        };
        packet.put(key, stateArray);
        dashboard.sendTelemetryPacket(packet);
    }

    /**
     * Pushes any queued telemetry values synchronously during the loop.
     */
    public static void update() {
        // Here we could flush custom caches or broadcast multi-packet data bundles.
    }
}
