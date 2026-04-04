package com.acmerobotics.dashboard.telemetry;

import java.util.HashMap;
import java.util.Map;

/**
 * Mock TelemetryPacket for ARESLib simulation when full dashboard backend isn't linked.
 */
public class TelemetryPacket {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object value) {
        data.put(key, value);
    }

    public void putAll(Map<String, Object> map) {
        data.putAll(map);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
