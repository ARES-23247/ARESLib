package org.areslib.hardware.interfaces;

/**
 * Hardware-agnostic interface for an encoder.
 * Allows users to drop in native FTC encoders, OctoQuad, or SRSHub readings 
 * transparently into ARESlib's tracking without tightly coupling dependencies.
 */
public interface AresEncoder {
    
    /** @return Distance in native units (user defines the scale). */
    double getPosition();

    /** @return Velocity in native units per second. */
    double getVelocity();
}
