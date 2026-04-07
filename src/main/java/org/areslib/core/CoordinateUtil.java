package org.areslib.core;

import org.areslib.math.geometry.Pose2d;
import org.areslib.math.geometry.Rotation2d;
import org.areslib.math.geometry.Translation2d;

/**
 * Centralized coordinate conversions between the three systems used in ARESLib2.
 * <p>
 * <b>WPILib/dyn4j:</b> Meters, field center origin, X-forward Y-left θ CCW+.<br>
 * <b>Pedro Pathing:</b> Inches, bottom-left origin, X-right Y-forward.<br>
 * <b>AdvantageScope:</b> Same as WPILib.
 * <p>
 * <b>Rule:</b> Never write raw {@code * 0.0254} or {@code + 72.0} in application code.
 * Always use this class for conversions. Inline conversions are coordinate bugs waiting to happen.
 *
 * @see FieldConstants
 */
public final class CoordinateUtil {

    private CoordinateUtil() {} // Utility class

    // ===== Unit Conversions =====

    /**
     * Converts meters to inches.
     */
    public static double metersToInches(double meters) {
        return meters * FieldConstants.METERS_TO_INCHES;
    }

    /**
     * Converts inches to meters.
     */
    public static double inchesToMeters(double inches) {
        return inches * FieldConstants.INCHES_TO_METERS;
    }

    // ===== Origin Shifts =====

    /**
     * Converts a center-origin value (meters) to Pedro's bottom-left origin (inches).
     * Applies: {@code (meters * M2I) + 72.0}
     */
    public static double centerMetersToBottomLeftInches(double centerMeters) {
        return metersToInches(centerMeters) + FieldConstants.HALF_FIELD_INCHES;
    }

    /**
     * Converts a Pedro bottom-left origin value (inches) to center-origin (meters).
     * Applies: {@code (inches - 72.0) * I2M}
     */
    public static double bottomLeftInchesToCenterMeters(double bottomLeftInches) {
        return inchesToMeters(bottomLeftInches - FieldConstants.HALF_FIELD_INCHES);
    }

    // ===== Axis Swaps (WPILib ↔ Pedro) =====

    /**
     * Converts a WPILib Pose2d (meters, center origin, X-forward Y-left)
     * to a Pedro Pose (inches, bottom-left origin, X-right Y-forward).
     * <p>
     * Axis mapping: pedroX = -wpilibY, pedroY = wpilibX
     */
    public static com.pedropathing.geometry.Pose wpiToPedro(Pose2d wpiPose) {
        double pedroX = centerMetersToBottomLeftInches(-wpiPose.getY());
        double pedroY = centerMetersToBottomLeftInches(wpiPose.getX());
        return new com.pedropathing.geometry.Pose(pedroX, pedroY, wpiPose.getRotation().getRadians());
    }

    /**
     * Converts a Pedro Pose (inches, bottom-left origin, X-right Y-forward)
     * to a WPILib Pose2d (meters, center origin, X-forward Y-left).
     * <p>
     * Axis mapping: wpilibX = pedroY, wpilibY = -pedroX
     */
    public static Pose2d pedroToWpi(com.pedropathing.geometry.Pose pedroPose) {
        double wpiX = bottomLeftInchesToCenterMeters(pedroPose.getY());
        double wpiY = -bottomLeftInchesToCenterMeters(pedroPose.getX());
        return new Pose2d(wpiX, wpiY, new Rotation2d(pedroPose.getHeading()));
    }

    /**
     * Converts a WPILib Translation2d (meters, center origin)
     * to a Pedro Pose at heading 0 (inches, bottom-left origin) with axis swap.
     */
    public static com.pedropathing.geometry.Pose wpiToPedroPose(Translation2d wpiTranslation) {
        double pedroX = centerMetersToBottomLeftInches(-wpiTranslation.getY());
        double pedroY = centerMetersToBottomLeftInches(wpiTranslation.getX());
        return new com.pedropathing.geometry.Pose(pedroX, pedroY, 0);
    }

    /**
     * Converts a vision center-origin pose (meters) to Pedro bottom-left origin (inches).
     * <b>No axis swap</b> — vision and Pedro share the same axis orientation
     * when the vision system has already been aligned to the Pedro frame.
     * For raw WPILib-frame vision data, use {@link #wpiToPedro(Pose2d)} instead.
     */
    public static com.pedropathing.geometry.Pose visionCenterToPedro(double xMeters, double yMeters, double headingRad) {
        return new com.pedropathing.geometry.Pose(
                centerMetersToBottomLeftInches(xMeters),
                centerMetersToBottomLeftInches(yMeters),
                headingRad
        );
    }
}
