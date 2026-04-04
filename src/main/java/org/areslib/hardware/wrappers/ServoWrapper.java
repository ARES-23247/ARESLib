package org.areslib.hardware.wrappers;

import com.qualcomm.robotcore.hardware.Servo;
import org.areslib.hardware.interfaces.AresServo;

public class ServoWrapper implements AresServo {
    private final Servo servo;

    public ServoWrapper(Servo servo) {
        this.servo = servo;
    }

    @Override
    public void setPosition(double position) {
        servo.setPosition(position);
    }
}
