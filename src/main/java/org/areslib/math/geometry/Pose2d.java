package org.areslib.math.geometry;

import java.util.Objects;

/** Represents a 2D pose containing translational and rotational elements. */
public class Pose2d implements Interpolatable<Pose2d> {
  private Translation2d translation;
  private Rotation2d rotation;

  public Pose2d() {
    translation = new Translation2d();
    rotation = new Rotation2d();
  }

  public Pose2d(Translation2d translation, Rotation2d rotation) {
    this.translation = translation;
    this.rotation = rotation;
  }

  public Pose2d(double x, double y, Rotation2d rotation) {
    translation = new Translation2d(x, y);
    this.rotation = rotation;
  }

  /**
   * Sets the pose to match another pose in-place. Eliminates the need to instantiate new Pose2d
   * objects in tight odometry loops.
   */
  public void set(Pose2d other) {
    translation.set(other.translation);
    rotation.set(other.rotation);
  }

  /**
   * Sets the pose components in-place.
   *
   * @param translation The new translation.
   * @param rotation The new rotation.
   */
  public void set(Translation2d translation, Rotation2d rotation) {
    this.translation.set(translation);
    this.rotation.set(rotation);
  }

  /**
   * Creates a deep copy of the pose.
   *
   * @return A new Pose2d instance with copied components.
   */
  public Pose2d copy() {
    return new Pose2d(translation.copy(), rotation.copy());
  }

  /**
   * Sets the pose components in-place.
   *
   * @param x The new X coordinate.
   * @param y The new Y coordinate.
   * @param angleRadians The new angle in radians.
   */
  public void set(double x, double y, double angleRadians) {
    translation.set(x, y);
    rotation.set(angleRadians);
  }

  public Transform2d minus(Pose2d other) {
    return new Transform2d(other, this);
  }

  public Translation2d getTranslation() {
    return translation;
  }

  public double getX() {
    return translation.getX();
  }

  public double getY() {
    return translation.getY();
  }

  public Rotation2d getRotation() {
    return rotation;
  }

  public Pose2d plus(Pose2d other) {
    return transformBy(other);
  }

  public Pose2d transformBy(Pose2d other) {
    return new Pose2d(
        translation.plus(other.translation.rotateBy(rotation)), rotation.plus(other.rotation));
  }

  public Pose2d relativeTo(Pose2d other) {
    Translation2d transform =
        translation.minus(other.translation).rotateBy(other.rotation.unaryMinus());
    Rotation2d rotation = this.rotation.minus(other.rotation);
    return new Pose2d(transform, rotation);
  }

  @Override
  public Pose2d interpolate(Pose2d endValue, double t) {
    if (t <= 0) return this;
    if (t >= 1) return endValue;

    Twist2d twist = this.log(endValue);
    return this.exp(twist.scaled(t));
  }

  @Override
  public String toString() {
    return String.format("Pose2d(%s, %s)", translation.toString(), rotation.toString());
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!(obj instanceof Pose2d)) return false;
    Pose2d other = (Pose2d) obj;
    return translation.equals(other.translation) && rotation.equals(other.rotation);
  }

  @Override
  public int hashCode() {
    return Objects.hash(translation, rotation);
  }

  /**
   * Obtain a new Pose2d from a (constant curvature) velocity.
   *
   * <p>See <a href="https://file.tavsys.net/control/controls-engineering-in-frc.pdf">Controls
   * Engineering in the FIRST Robotics Competition</a> section 10.2 "Pose exponential" for a
   * derivation.
   *
   * @param twist The twist to map to a Pose2d.
   * @return The new Pose2d.
   */
  public Pose2d exp(Twist2d twist) {
    Pose2d out = new Pose2d();
    exp(twist, out);
    return out;
  }

  /**
   * Calculates the exponential map (constant curvature velocity integration) in-place.
   *
   * @param twist The twist to apply.
   * @param out The object to populate with the result.
   */
  public void exp(Twist2d twist, Pose2d out) {
    double dx = twist.dx;
    double dy = twist.dy;
    double dtheta = twist.dtheta;

    double sinTheta = Math.sin(dtheta);
    double cosTheta = Math.cos(dtheta);

    double s, c;
    if (Math.abs(dtheta) < 1E-9) {
      s = 1.0 - 1.0 / 6.0 * dtheta * dtheta;
      c = 0.5 * dtheta;
    } else {
      s = sinTheta / dtheta;
      c = (1.0 - cosTheta) / dtheta;
    }

    double tx = dx * s - dy * c;
    double ty = dx * c + dy * s;

    // Apply the local transform to the current pose
    double cos = rotation.getCos();
    double sin = rotation.getSin();

    double newX = translation.getX() + (tx * cos - ty * sin);
    double newY = translation.getY() + (tx * sin + ty * cos);
    double newTheta = rotation.getRadians() + dtheta;

    out.set(newX, newY, newTheta);
  }

  /**
   * Returns a Twist2d that maps this pose to the end pose.
   *
   * <p>If c is the cosine of dtheta and s is the sine of dtheta, and the transformation is given by
   * (x, y, theta), then the velocity vector (dx, dy, dtheta) can be computed.
   *
   * @param end The end pose.
   * @return The twist that maps this pose to the end pose.
   */
  public Twist2d log(Pose2d end) {
    Pose2d transform = end.relativeTo(this);
    double dtheta = transform.getRotation().getRadians();
    double halfDtheta = 0.5 * dtheta;
    double cosMinusOne = transform.getRotation().getCos() - 1.0;

    double halfThetaByTanOfHalfDtheta;
    if (Math.abs(cosMinusOne) < 1E-9) {
      halfThetaByTanOfHalfDtheta = 1.0 - 1.0 / 12.0 * dtheta * dtheta;
    } else {
      halfThetaByTanOfHalfDtheta = -(halfDtheta * transform.getRotation().getSin()) / cosMinusOne;
    }

    Translation2d translationPart =
        transform
            .getTranslation()
            .rotateBy(new Rotation2d(halfThetaByTanOfHalfDtheta, -halfDtheta))
            .times(Math.hypot(halfThetaByTanOfHalfDtheta, halfDtheta));

    return new Twist2d(translationPart.getX(), translationPart.getY(), dtheta);
  }
}
