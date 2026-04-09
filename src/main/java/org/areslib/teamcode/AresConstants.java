package org.areslib.teamcode;

import com.acmerobotics.dashboard.config.Config;

/**
 * Sample configuration constants class. All public static variables here will automatically appear
 * in the FTC Custom Dashboard and in the standard AcmeRobotics FtcDashboard interface!
 */
@Config
public class AresConstants {

  // Notice how these are grouped automatically by the Dashboard

  public static double kP = 0.5;
  public static double kI = 0.0;
  public static double kD = 0.1;

  public static double maxVelocity = 2.5;
  public static double maxAcceleration = 1.0;

  public static boolean enableLogging = true;

  public static String defaultAutoPath = "BlueRight_Path1";

  // You can also create nested groups by making public static nested classes
  public static class FlywheelConstants {
    public static int targetRPM = 4500;
    public static double feedforward = 0.03;
  }
}
