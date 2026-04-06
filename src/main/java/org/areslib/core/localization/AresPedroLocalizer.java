package org.areslib.core.localization;

import org.areslib.core.FieldConstants;
import org.areslib.hardware.interfaces.OdometryIO;
import com.pedropathing.localization.Localizer;
import com.pedropathing.geometry.Pose;
import com.pedropathing.math.Vector;

/**
 * ARESlib integration layer for Pedro Pathing's {@link Localizer}.
 * <p>
 * This class translates ARESLib's SI-unit based {@link OdometryIO} hardware abstractions
 * (meters, radians, m/s) into the specific imperial units (inches) expected by Pedro Pathing's
 * internal tracking algorithms. It handles coordinate offsets to support mid-match pathing updates.
 *
 * <h3>Coordinate Systems</h3>
 * <ul>
 *   <li><b>ARESLib / Sensor:</b> meters, origin at field center, +X forward, +Y left.</li>
 *   <li><b>Pedro Pathing:</b> inches, origin at bottom-left corner of the field.</li>
 * </ul>
 * The conversion adds {@link FieldConstants#HALF_FIELD_INCHES} (72") to shift from
 * center-origin to bottom-left-origin. This offset is applied <em>after</em> any heading
 * rotation so that the rotation pivot is always the sensor origin, not the shifted point.
 */
public class AresPedroLocalizer implements Localizer {
    private final OdometryIO.OdometryInputs inputs;
    
    // Internal state to handle Pedro's tracking of custom starting offsets
    private double offsetXInches = 0.0;
    private double offsetYInches = 0.0;
    private double offsetHeadingRadians = 0.0;

    /**
     * Constructs a new AresPedroLocalizer bound to a specific hardware odometry source.
     *
     * @param inputs The populated {@link OdometryIO.OdometryInputs} data object from the hardware layer.
     */
    public AresPedroLocalizer(OdometryIO.OdometryInputs inputs) {
        this.inputs = inputs;
    }

    /**
     * Gets the current field-centric pose of the robot.
     * <p>
     * Converts ARESlib meters to Pedro Pathing inches, applying heading offset
     * rotation <em>before</em> the 72-inch origin shift to avoid rotating the
     * constant offset vector.
     *
     * @return The Cartesian coordinates and heading wrapped in a {@link Pose}.
     */
    @Override
    public Pose getPose() {
        // Step 1: Convert raw sensor meters → inches (still centered at origin)
        double rawXInches = inputs.xMeters * FieldConstants.METERS_TO_INCHES;
        double rawYInches = inputs.yMeters * FieldConstants.METERS_TO_INCHES;

        // Step 2: Apply heading offset rotation around the SENSOR origin (0,0).
        // This must happen BEFORE adding the 72-inch shift to avoid rotating the constant offset.
        double rotatedX = rawXInches * Math.cos(offsetHeadingRadians) - rawYInches * Math.sin(offsetHeadingRadians);
        double rotatedY = rawXInches * Math.sin(offsetHeadingRadians) + rawYInches * Math.cos(offsetHeadingRadians);

        // Step 3: Shift from center-origin to Pedro bottom-left-origin, then apply position offsets
        return new Pose(
            rotatedX + FieldConstants.HALF_FIELD_INCHES + offsetXInches,
            rotatedY + FieldConstants.HALF_FIELD_INCHES + offsetYInches,
            inputs.headingRadians + offsetHeadingRadians
        );
    }

    /**
     * Gets the current velocity vector of the robot.
     * Conversions applied mapping m/s to in/s, with heading offset rotation.
     *
     * @return A {@link Pose} where X = xVelocity, Y = yVelocity, and Heading = angularVelocity.
     */
    @Override
    public Pose getVelocity() {
        double rawVx = inputs.xVelocityMetersPerSecond * FieldConstants.METERS_TO_INCHES;
        double rawVy = inputs.yVelocityMetersPerSecond * FieldConstants.METERS_TO_INCHES;
        
        double rotatedVx = rawVx * Math.cos(offsetHeadingRadians) - rawVy * Math.sin(offsetHeadingRadians);
        double rotatedVy = rawVx * Math.sin(offsetHeadingRadians) + rawVy * Math.cos(offsetHeadingRadians);

        return new Pose(
            rotatedVx,
            rotatedVy,
            inputs.angularVelocityRadiansPerSecond
        );
    }

    /**
     * Retrieves the strictly positional velocity of the robot mapped as a Vector.
     *
     * @return A {@link Vector} containing planar X/Y inch/sec velocity.
     */
    @Override
    public Vector getVelocityVector() {
        Pose vel = getVelocity();
        Vector vec = new Vector();
        vec.setOrthogonalComponents(vel.getX(), vel.getY());
        return vec;
    }

    /**
     * Unused interface method. Start pose is dictated by {@link #setPose(Pose)}.
     */
    @Override
    public void setStartPose(Pose setStart) {
        // Unused in ARES
    }

    /**
     * Injects a specific absolute pose onto the robot.
     * <p>
     * Calculates the offset delta between the requested pose and the current raw
     * sensor reading so that subsequent {@link #getPose()} calls return the injected
     * value without modifying the underlying hardware state.
     *
     * @param setPose The desired global coordinate pose (in Pedro inches, bottom-left origin).
     */
    @Override
    public void setPose(Pose setPose) {
        // Step 1: Get raw sensor position in inches (center-origin, no offset applied)
        double currentRawX = inputs.xMeters * FieldConstants.METERS_TO_INCHES;
        double currentRawY = inputs.yMeters * FieldConstants.METERS_TO_INCHES;

        // Step 2: Compute heading offset
        offsetHeadingRadians = setPose.getHeading() - inputs.headingRadians;

        // Step 3: Rotate raw sensor readings by the new heading offset
        double rotatedRawX = currentRawX * Math.cos(offsetHeadingRadians) - currentRawY * Math.sin(offsetHeadingRadians);
        double rotatedRawY = currentRawX * Math.sin(offsetHeadingRadians) + currentRawY * Math.cos(offsetHeadingRadians);

        // Step 4: The offset is what remains after the rotation + 72" shift
        offsetXInches = setPose.getX() - (rotatedRawX + FieldConstants.HALF_FIELD_INCHES);
        offsetYInches = setPose.getY() - (rotatedRawY + FieldConstants.HALF_FIELD_INCHES);
    }

    /**
     * Core update routine invoked by Pedro Follower.
     * Left intentionally empty because ARESlib updates logic concurrently within {@code CommandScheduler}.
     */
    @Override
    public void update() {
        // Nothing to compute specifically here because ares hardware wrappers update internal IO state
        // asynchronously or strictly sequentially before Pedro updates.
    }

    /**
     * Gets the absolute heading of the robot including initialization offsets.
     */
    @Override
    public double getTotalHeading() {
        return inputs.headingRadians + offsetHeadingRadians;
    }

    /** Tracking wheel functional variable. Unused. */
    @Override
    public double getForwardMultiplier() {
        return 1.0;
    }

    /** Tracking wheel functional variable. Unused. */
    @Override
    public double getLateralMultiplier() {
        return 1.0;
    }

    /** Tracking wheel functional variable. Unused. */
    @Override
    public double getTurningMultiplier() {
        return 1.0;
    }

    /** Overriding the built-in IMU reset function. Unused as ARESlib sensors handle orientation externally. */
    @Override
    public void resetIMU() throws InterruptedException {
        // Handled outside of localizer
    }

    /** Retrieve absolute heading directly from sensors. */
    @Override
    public double getIMUHeading() {
        return inputs.headingRadians;
    }

    /** Detects if internal hardware mapping returned NaN invalid outputs. */
    @Override
    public boolean isNAN() {
        return Double.isNaN(inputs.xMeters) || Double.isNaN(inputs.yMeters);
    }

    /** Programmatically inject a raw X position without altering the rest of the pose. */
    @Override
    public void setX(double x) {
        Pose current = getPose();
        setPose(new Pose(x, current.getY(), current.getHeading()));
    }

    /** Programmatically inject a raw Y position without altering the rest of the pose. */
    @Override
    public void setY(double y) {
        Pose current = getPose();
        setPose(new Pose(current.getX(), y, current.getHeading()));
    }

    /** Programmatically inject a raw angular rotation without altering the rest of the pose. */
    @Override
    public void setHeading(double heading) {
        Pose current = getPose();
        setPose(new Pose(current.getX(), current.getY(), heading));
    }
}
