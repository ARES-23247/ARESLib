package org.areslib.hardware;

import org.areslib.hardware.interfaces.AresEncoder;
import org.areslib.hardware.interfaces.AresMotor;

public class SwerveModuleIOReal implements SwerveModuleIO {
    
    private final AresMotor driveMotor;
    private final AresMotor turnMotor;
    private final AresEncoder driveEncoder;
    private final AresEncoder turnEncoder;

    private final double driveDistancePerTick;
    private final double turnRadsPerTick;

    public SwerveModuleIOReal(
            AresMotor driveMotor, 
            AresMotor turnMotor, 
            AresEncoder driveEncoder, 
            AresEncoder turnEncoder,
            double driveDistancePerTick,
            double turnRadsPerTick) {
        this.driveMotor = driveMotor;
        this.turnMotor = turnMotor;
        this.driveEncoder = driveEncoder;
        this.turnEncoder = turnEncoder;
        this.driveDistancePerTick = driveDistancePerTick;
        this.turnRadsPerTick = turnRadsPerTick;
    }

    @Override
    public void updateInputs(SwerveModuleInputs inputs) {
        // Direct, zero-latency reads from the abstracted cache. 
        // Handles transparent expansion hub bulk caching or native OctoQuad arrays equally.
        inputs.drivePositionMeters = driveEncoder.getPosition() * driveDistancePerTick;
        inputs.driveVelocityMps = driveEncoder.getVelocity() * driveDistancePerTick;
        
        inputs.turnAbsolutePositionRad = turnEncoder.getPosition() * turnRadsPerTick;
        inputs.turnVelocityRadPerSec = turnEncoder.getVelocity() * turnRadsPerTick;
    }

    @Override
    public void setDriveVoltage(double volts) {
        driveMotor.setVoltage(volts);
    }

    @Override
    public void setTurnVoltage(double volts) {
        turnMotor.setVoltage(volts);
    }
}
