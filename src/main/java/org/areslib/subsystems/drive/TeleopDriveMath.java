package org.areslib.subsystems.drive;

/**
 * Teleop drive math utilities for processing raw joystick inputs into smooth, predictable robot
 * motion. Ported from MARSLib swerve drive stack.
 *
 * <p>Provides deadband filtering, exponential response curves, and angular input scaling for
 * precision driving during teleop.
 *
 * <p><strong>Mathematical References:</strong>
 *
 * <ul>
 *   <li>Polar Deadbands: <a
 *       href="https://www.gamedeveloper.com/programming/doing-thumbstick-dead-zones-right">"Doing
 *       Thumbstick Dead Zones Right"</a>
 *   <li>Exponential Curves: f(x) = sgn(x) * |x|^k, where k is the sensitivity exponent.
 * </ul>
 */
public class TeleopDriveMath {

  /** Pre-allocated result buffer for {@link #processJoystick2D}. */
  private static final double[] JOYSTICK_2D_CACHE = new double[2];

  /**
   * Applies a deadband to a joystick axis value. Values within the deadband are zeroed.
   *
   * @param value Raw joystick value (-1.0 to 1.0).
   * @param deadband Deadband threshold (typically 0.05 to 0.15).
   * @return Filtered value with deadband applied and rescaled to full range.
   */
  public static double applyDeadband(double value, double deadband) {
    if (Math.abs(value) < deadband) {
      return 0.0;
    }
    // Rescale so the output is 0.0 at the edge of the deadband and 1.0 at max input
    return Math.signum(value) * ((Math.abs(value) - deadband) / (1.0 - deadband));
  }

  /**
   * Applies an exponential response curve to a joystick axis value. This gives fine control at low
   * inputs and fast response at high inputs.
   *
   * @param value Input value (-1.0 to 1.0), typically after deadband.
   * @param exponent The exponent for the curve (1.0 = linear, 2.0 = quadratic, 3.0 = cubic).
   * @return Curve-adjusted value preserving sign.
   */
  public static double applyExponentialCurve(double value, double exponent) {
    return Math.signum(value) * Math.pow(Math.abs(value), exponent);
  }

  /**
   * Full joystick processing pipeline: deadband → exponential curve → max speed scaling.
   *
   * @param rawInput Raw joystick value (-1.0 to 1.0).
   * @param deadband Deadband threshold.
   * @param exponent Response curve exponent.
   * @param maxSpeedMpsOrRadPerSec Maximum output speed (m/s or rad/s).
   * @return Processed speed value ready for ChassisSpeeds.
   */
  public static double processJoystickInput(
      double rawInput, double deadband, double exponent, double maxSpeedMpsOrRadPerSec) {
    double filtered = applyDeadband(rawInput, deadband);
    double curved = applyExponentialCurve(filtered, exponent);
    return curved * maxSpeedMpsOrRadPerSec;
  }

  /**
   * Processes 2D joystick inputs (X and Y) using polar deadband for consistent diagonal behavior.
   *
   * <p>A polar deadband treats the joystick as a single 2D vector rather than two independent axes.
   * This prevents the "cross-shaped" dead zone that independent axis deadbands create.
   *
   * @param rawX Raw X-axis joystick value.
   * @param rawY Raw Y-axis joystick value.
   * @param deadband Polar deadband radius.
   * @param exponent Response curve exponent applied to the magnitude.
   * @param maxSpeedMps Maximum output speed.
   * @return A double array [processedX, processedY] ready for ChassisSpeeds.
   */
  public static double[] processJoystick2D(
      double rawX, double rawY, double deadband, double exponent, double maxSpeedMps) {
    double magnitude = Math.sqrt(rawX * rawX + rawY * rawY);

    if (magnitude < deadband) {
      JOYSTICK_2D_CACHE[0] = 0.0;
      JOYSTICK_2D_CACHE[1] = 0.0;
      return JOYSTICK_2D_CACHE;
    }

    // Rescale magnitude from deadband edge
    double rescaled = (magnitude - deadband) / (1.0 - deadband);
    double curved = Math.pow(Math.min(rescaled, 1.0), exponent);

    // Maintain original direction, apply curved magnitude
    double scale = curved * maxSpeedMps / magnitude;
    JOYSTICK_2D_CACHE[0] = rawX * scale;
    JOYSTICK_2D_CACHE[1] = rawY * scale;
    return JOYSTICK_2D_CACHE;
  }
}
