package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.CRServo;
import org.areslib.hardware.interfaces.AresMotor;

public class CRServoWrapper implements AresMotor {
    private final CRServo servo;
    // CR Servos generally operate on a -1.0 to 1.0 power scale, equivalent to 12V voltage range.

    public CRServoWrapper(CRServo servo) {
        this.servo = servo;
    }

    @Override
    public void setVoltage(double volts) {
        // Map voltage (-12 to 12) down to CR Servo power (-1.0 to 1.0)
        double power = volts / 12.0;
        power = Math.max(-1.0, Math.min(1.0, power));
        servo.setPower(power);
    }

    @Override
    public double getVoltage() {
        return servo.getPower() * 12.0;
    }
}
