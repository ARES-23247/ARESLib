package org.areslib.hardware.interfaces;

public interface AresAbsoluteEncoder extends AresEncoder {
    /**
     * Gets the absolute position of the encoder.
     * @return the normalized absolute position (usually 0 to 2PI radians, or 0.0 to 1.0 depending on implementation)
     */
    double getAbsolutePosition();
}
