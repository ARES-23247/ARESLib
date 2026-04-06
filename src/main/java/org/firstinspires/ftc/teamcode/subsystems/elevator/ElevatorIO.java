package org.firstinspires.ftc.teamcode.subsystems.elevator;

import org.areslib.telemetry.AresLoggableInputs;

public interface ElevatorIO {
    public static class ElevatorIOInputs implements AresLoggableInputs {
        public double positionMeters = 0.0;
        public double velocityMetersPerSec = 0.0;
        public double appliedVolts = 0.0;
        public double[] currentAmps = new double[]{};
    }

    public default void updateInputs(ElevatorIOInputs inputs) {}
    public default void setVoltage(double volts) {}
}
