package org.areslib.hardware.interfaces;

public interface AresColorSensor {
    /**
     * Gets the 32-bit ARGB color value reported by the sensor.
     * @return ARGB integer
     */
    int getARGB();
}
