package org.areslib.core;

import org.areslib.math.Units;

/**
 * Utility functions for coordinate conversions, interpolation, and geometry scaling used within the
 * ARESLib framework.
 *
 * <p><b>Note:</b> Basic unit conversions (inches↔meters, mm↔meters) are canonical in {@link Units}.
 * This class delegates to {@code Units} for those and adds geometry/interpolation helpers.
 */
public class CoordinateUtil {

  /**
   * Converts inches to meters.
   *
   * @param inches The distance in inches
   * @return The distance in meters
   * @see Units#inchesToMeters(double)
   */
  public static double inchesToMeters(double inches) {
    return Units.inchesToMeters(inches);
  }

  /**
   * Converts meters to inches.
   *
   * @param meters The distance in meters
   * @return The distance in inches
   * @see Units#metersToInches(double)
   */
  public static double metersToInches(double meters) {
    return Units.metersToInches(meters);
  }

  /**
   * Converts millimeters to meters.
   *
   * @param mm The distance in millimeters
   * @return The distance in meters
   * @see Units#millimetersToMeters(double)
   */
  public static double mmToMeters(double mm) {
    return Units.millimetersToMeters(mm);
  }

  /**
   * Linearly interpolates between a and b by percentage f.
   *
   * @param a The start value
   * @param b The end value
   * @param f The parameter, 0-1
   * @return The interpolated value
   */
  public static double lerp(double a, double b, double f) {
    return a + f * (b - a);
  }

  /**
   * Linearly interpolates between two angles in radians across the shortest path.
   *
   * @param a The start angle in radians
   * @param b The end angle in radians
   * @param f The parameter, 0-1
   * @return The interpolated angle in radians
   */
  public static double shortestAngleLerp(double a, double b, double f) {
    double diff = b - a;
    while (diff > Math.PI) diff -= 2 * Math.PI;
    while (diff < -Math.PI) diff += 2 * Math.PI;
    return a + diff * f;
  }

  /**
   * Retrieves the Kalman gain multiplier based on vision confidence.
   *
   * @param confidence Camera confidence metric
   * @return Kalman gain scaler
   */
  public static double computeVisionKalmanGain(double confidence) {
    return 0.1 * confidence; // simplified trust model
  }
}
