package org.areslib.math.geometry;

import java.util.Objects;

/**
 * Represents a 2D pose containing translational and rotational elements.
 */
public class Pose2d {
    private final Translation2d m_translation;
    private final Rotation2d m_rotation;

    public Pose2d() {
        m_translation = new Translation2d();
        m_rotation = new Rotation2d();
    }

    public Pose2d(Translation2d translation, Rotation2d rotation) {
        m_translation = translation;
        m_rotation = rotation;
    }

    public Pose2d(double x, double y, Rotation2d rotation) {
        m_translation = new Translation2d(x, y);
        m_rotation = rotation;
    }

    public Translation2d getTranslation() {
        return m_translation;
    }

    public double getX() {
        return m_translation.getX();
    }

    public double getY() {
        return m_translation.getY();
    }

    public Rotation2d getRotation() {
        return m_rotation;
    }

    public Pose2d plus(Pose2d other) {
        return transformBy(other);
    }

    public Pose2d transformBy(Pose2d other) {
        return new Pose2d(
            m_translation.plus(other.m_translation.rotateBy(m_rotation)),
            m_rotation.plus(other.m_rotation)
        );
    }

    public Pose2d relativeTo(Pose2d other) {
        Translation2d transform = m_translation.minus(other.m_translation).rotateBy(other.m_rotation.unaryMinus());
        Rotation2d rotation = m_rotation.minus(other.m_rotation);
        return new Pose2d(transform, rotation);
    }

    @Override
    public String toString() {
        return String.format("Pose2d(%s, %s)", m_translation.toString(), m_rotation.toString());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Pose2d)) return false;
        Pose2d other = (Pose2d) obj;
        return m_translation.equals(other.m_translation) && m_rotation.equals(other.m_rotation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_translation, m_rotation);
    }
}
