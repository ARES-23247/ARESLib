package org.areslib.math.kinematics;

import org.areslib.math.geometry.Translation2d;

/**
 * Helper class that converts a chassis velocity (dx, dy, and dtheta components) into individual
 * module states and vice versa.
 */
public class SwerveDriveKinematics {
  private final Translation2d[] modules;
  private final double[][] inverseKinematics;
  private final double[][] forwardKinematics;

  public SwerveDriveKinematics(Translation2d... moduleTranslations) {
    if (moduleTranslations.length < 2) {
      throw new IllegalArgumentException("A swerve drive requires at least two modules");
    }
    modules = moduleTranslations;
    int numModules = modules.length;

    inverseKinematics = new double[numModules * 2][3];
    for (int i = 0; i < numModules; i++) {
      inverseKinematics[i * 2][0] = 1;
      inverseKinematics[i * 2][1] = 0;
      inverseKinematics[i * 2][2] = -modules[i].getY();
      inverseKinematics[i * 2 + 1][0] = 0;
      inverseKinematics[i * 2 + 1][1] = 1;
      inverseKinematics[i * 2 + 1][2] = modules[i].getX();
    }

    forwardKinematics = InverseMatrixHelper.pseudoInverse(inverseKinematics);
  }

  /**
   * Converts a chassis speed to array of swerve module states.
   *
   * @param chassisSpeeds The chassis speeds.
   * @param states The array to populate with new states. Must be of length >= numModules.
   */
  public void toSwerveModuleStates(ChassisSpeeds chassisSpeeds, SwerveModuleState[] states) {
    for (int i = 0; i < modules.length; i++) {
      double vx =
          chassisSpeeds.vxMetersPerSecond * inverseKinematics[i * 2][0]
              + chassisSpeeds.vyMetersPerSecond * inverseKinematics[i * 2][1]
              + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[i * 2][2];

      double vy =
          chassisSpeeds.vxMetersPerSecond * inverseKinematics[i * 2 + 1][0]
              + chassisSpeeds.vyMetersPerSecond * inverseKinematics[i * 2 + 1][1]
              + chassisSpeeds.omegaRadiansPerSecond * inverseKinematics[i * 2 + 1][2];

      states[i].speedMetersPerSecond = Math.hypot(vx, vy);
      states[i].angle.set(vx, vy);
    }
  }

  /**
   * Converts a chassis speed to array of swerve module states.
   *
   * @param chassisSpeeds The chassis speeds.
   * @return Array of swerve module states.
   */
  public SwerveModuleState[] toSwerveModuleStates(ChassisSpeeds chassisSpeeds) {
    SwerveModuleState[] states = new SwerveModuleState[modules.length];
    for (int i = 0; i < modules.length; i++) {
      states[i] = new SwerveModuleState();
    }
    toSwerveModuleStates(chassisSpeeds, states);
    return states;
  }

  /**
   * Converts an array of swerve module states into a single chassis speed.
   *
   * @param result The object to populate with computed speeds.
   * @param moduleStates Array of swerve module states.
   */
  public void toChassisSpeeds(ChassisSpeeds result, SwerveModuleState... moduleStates) {
    if (moduleStates.length != modules.length) {
      throw new IllegalArgumentException("Number of module states must match number of modules");
    }

    double vx = 0;
    double vy = 0;
    double omega = 0;

    for (int i = 0; i < modules.length; i++) {
      double moduleVx = moduleStates[i].speedMetersPerSecond * moduleStates[i].angle.getCos();
      double moduleVy = moduleStates[i].speedMetersPerSecond * moduleStates[i].angle.getSin();

      vx += forwardKinematics[0][i * 2] * moduleVx + forwardKinematics[0][i * 2 + 1] * moduleVy;
      vy += forwardKinematics[1][i * 2] * moduleVx + forwardKinematics[1][i * 2 + 1] * moduleVy;
      omega += forwardKinematics[2][i * 2] * moduleVx + forwardKinematics[2][i * 2 + 1] * moduleVy;
    }

    result.set(vx, vy, omega);
  }

  /**
   * Converts an array of swerve module states into a single chassis speed.
   *
   * @param moduleStates Array of swerve module states.
   * @return The chassis speed.
   */
  public ChassisSpeeds toChassisSpeeds(SwerveModuleState... moduleStates) {
    ChassisSpeeds result = new ChassisSpeeds();
    toChassisSpeeds(result, moduleStates);
    return result;
  }

  /**
   * Converts swerve module position deltas into a Twist2d. (Allocates)
   *
   * @param previousPositions The module positions at the start of the interval.
   * @param currentPositions The module positions at the end of the interval.
   * @return The twist over the interval.
   */
  public org.areslib.math.geometry.Twist2d toTwist2d(
      SwerveModulePosition[] previousPositions, SwerveModulePosition[] currentPositions) {
    org.areslib.math.geometry.Twist2d out = new org.areslib.math.geometry.Twist2d();
    toTwist2d(previousPositions, currentPositions, out);
    return out;
  }

  /**
   * Converts swerve module position deltas into a Twist2d in-place.
   *
   * @param previousPositions The module positions at the start of the interval.
   * @param currentPositions The module positions at the end of the interval.
   * @param out The object to populate.
   */
  public void toTwist2d(
      SwerveModulePosition[] previousPositions,
      SwerveModulePosition[] currentPositions,
      org.areslib.math.geometry.Twist2d out) {
    if (previousPositions.length != modules.length || currentPositions.length != modules.length) {
      throw new IllegalArgumentException("Number of module positions must match number of modules");
    }

    double dx = 0;
    double dy = 0;
    double dtheta = 0;

    for (int i = 0; i < modules.length; i++) {
      double distanceDelta =
          currentPositions[i].distanceMeters - previousPositions[i].distanceMeters;
      // Rotation2d used here is immutable, but we are using its pre-computed trig values
      double moduleDx = distanceDelta * currentPositions[i].angle.getCos();
      double moduleDy = distanceDelta * currentPositions[i].angle.getSin();

      dx += forwardKinematics[0][i * 2] * moduleDx + forwardKinematics[0][i * 2 + 1] * moduleDy;
      dy += forwardKinematics[1][i * 2] * moduleDx + forwardKinematics[1][i * 2 + 1] * moduleDy;
      dtheta += forwardKinematics[2][i * 2] * moduleDx + forwardKinematics[2][i * 2 + 1] * moduleDy;
    }

    out.dx = dx;
    out.dy = dy;
    out.dtheta = dtheta;
  }

  /**
   * Renormalizes the wheel speeds if any individual speed is above the specified maximum.
   *
   * @param moduleStates The array of module states.
   * @param attainableMaxSpeedMetersPerSecond The absolute max speed that a module can reach.
   */
  public static void desaturateWheelSpeeds(
      SwerveModuleState[] moduleStates, double attainableMaxSpeedMetersPerSecond) {
    double realMaxSpeed = 0.0;
    for (SwerveModuleState state : moduleStates) {
      realMaxSpeed = Math.max(realMaxSpeed, Math.abs(state.speedMetersPerSecond));
    }
    if (realMaxSpeed > attainableMaxSpeedMetersPerSecond) {
      for (SwerveModuleState state : moduleStates) {
        state.speedMetersPerSecond =
            state.speedMetersPerSecond / realMaxSpeed * attainableMaxSpeedMetersPerSecond;
      }
    }
  }
}
