package org.areslib.hardware.interfaces;

public interface AresServo {
    /**
     * Sets the position of the servo.
     * @param position A value typically between 0.0 and 1.0.
     */
    void setPosition(double position);
}
