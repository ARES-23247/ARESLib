package org.areslib.math.geometry;

import java.util.Objects;

/** A rotation in a 2D coordinate frame represented a point on the unit circle (cosine and sine). */
public class Rotation2d implements Interpolatable<Rotation2d> {
  private double valueRadians;
  private double cos;
  private double sin;

  public Rotation2d() {
    valueRadians = 0.0;
    cos = 1.0;
    sin = 0.0;
  }

  public Rotation2d(double valueRadians) {
    this.valueRadians = valueRadians;
    cos = Math.cos(valueRadians);
    sin = Math.sin(valueRadians);
  }

  public Rotation2d(double x, double y) {
    set(x, y);
  }

  /**
   * Sets the rotation in radians in-place. Useful for eliminating garbage collection overhead in
   * tight loops.
   */
  public void set(double valueRadians) {
    this.valueRadians = valueRadians;
    cos = Math.cos(valueRadians);
    sin = Math.sin(valueRadians);
  }

  /** Sets the rotation in degrees in-place. */
  public void setDegrees(double degrees) {
    set(Math.toRadians(degrees));
  }

  /**
   * Sets the rotation based on X and Y components (atan2).
   *
   * @param x X component.
   * @param y Y component.
   */
  public void set(double x, double y) {
    double magnitude = Math.hypot(x, y);
    if (magnitude > 1e-6) {
      sin = y / magnitude;
      cos = x / magnitude;
    } else {
      sin = 0.0;
      cos = 1.0;
    }
    valueRadians = Math.atan2(sin, cos);
  }

  /** Sets the rotation equal to another Rotation2d in-place. */
  public void set(Rotation2d other) {
    valueRadians = other.valueRadians;
    cos = other.cos;
    sin = other.sin;
  }

  public Rotation2d copy() {
    Rotation2d out = new Rotation2d();
    out.set(this);
    return out;
  }

  public static Rotation2d fromDegrees(double degrees) {
    return new Rotation2d(Math.toRadians(degrees));
  }

  public double getRadians() {
    return valueRadians;
  }

  public double getDegrees() {
    return Math.toDegrees(valueRadians);
  }

  public double getCos() {
    return cos;
  }

  public double getSin() {
    return sin;
  }

  public Rotation2d plus(Rotation2d other) {
    return rotateBy(other);
  }

  public Rotation2d minus(Rotation2d other) {
    return rotateBy(other.unaryMinus());
  }

  public Rotation2d unaryMinus() {
    return new Rotation2d(-valueRadians);
  }

  public Rotation2d times(double scalar) {
    return new Rotation2d(valueRadians * scalar);
  }

  public Rotation2d rotateBy(Rotation2d other) {
    return new Rotation2d(cos * other.cos - sin * other.sin, cos * other.sin + sin * other.cos);
  }

  @Override
  public Rotation2d interpolate(Rotation2d endValue, double t) {
    if (t <= 0) return this;
    if (t >= 1) return endValue;
    double diff = endValue.minus(this).getRadians();
    return this.plus(new Rotation2d(diff * t));
  }

  @Override
  public String toString() {
    return String.format(
        "Rotation2d(Rads: %.2f, Deg: %.2f)", valueRadians, Math.toDegrees(valueRadians));
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Rotation2d)) return false;
    Rotation2d other = (Rotation2d) obj;
    return Math.hypot(cos - other.cos, sin - other.sin) < 1e-9;
  }

  @Override
  public int hashCode() {
    // Round to 1e-9 to match the epsilon tolerance used in equals().
    // Two Rotation2d values that are equals() MUST produce the same hash.
    long cosHash = Math.round(cos * 1e9);
    long sinHash = Math.round(sin * 1e9);
    return Objects.hash(cosHash, sinHash);
  }
}
