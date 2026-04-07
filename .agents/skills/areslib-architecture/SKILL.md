---
name: areslib-architecture
description: Helps write and maintain code mapped to the ARESLib2 FTC framework, detailing coordinate systems, vision fusion architectures, and simulator parity techniques. Use when modifying or adding areslib subsystems, handling Pedro Pathing conversions, injecting vision offsets, or logging 3D poses natively to AdvantageScope.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "2.0.0"
  category: framework
---

# ARESLib2 System Architecture & Design Patterns

This guide outlines the critical geometric math, sensor integration patterns, and architectural abstractions used in the `ARESLib2` FTC framework. Before you make architectural changes, please review these standardized conventions.

## Coordinate Mapping Conventions (CRITICAL)

ARESLib acts as a bridge between standard FTC SI unit paradigms and the custom Imperial structures required by WPILib logging / Pedro Pathing.

| System | Origin Location | Native Translation Unit | Angular Unit |
| :--- | :--- | :--- | :--- |
| **Dyn4j Physics** | Absolute Center (0, 0) | Meters (m) | Radians |
| **Limelight / WPILib** | Absolute Center (0, 0) | Meters (m) | Radians / Quaternions |
| **Pedro Pathing** | Bottom-Left Corner (72, 72)| Inches (in) | Radians |

### Code Usage Strategy
- All hardware `IO` Wrappers (e.g., `OdometryIO`, `VisionIO`) should **always emit data in SI units (Meters/Radians) with a Center Origin**.
- Any code inside `AresPedroLocalizer` translates this automatically by:
  1. Converting meters to inches: `meters / 0.0254`
  2. Offsetting dynamically: adding `+72.0` inches.

## AdvantageScope 3D Formats

When exporting positional telemetry (e.g. `VisionInputs.botPose3d`), the array must be specifically formatted so that AdvantageKit and AdvantageScope deserialize it flawlessly as an active `Pose3d` structure.

**Correct 3D Pose Structure: Length 7 `double` array**
- `[X_Meters, Y_Meters, Z_Meters, W, X_Quat, Y_Quat, Z_Quat]`
- Do **NOT** use `[X, Y, Z, Roll, Pitch, Yaw]`. Length 6 arrays are misidentified as a 2x3 trajectory (two 2D poses) by AdvantageScope.
- Utilize standard geometric formulation to create Quaternions in Wrappers:
  ```java
  double cr = Math.cos(roll * 0.5); double sr = Math.sin(roll * 0.5);
  double cp = Math.cos(pitch * 0.5); double sp = Math.sin(pitch * 0.5);
  double cy = Math.cos(yaw * 0.5); double sy = Math.sin(yaw * 0.5);
  // w = cr*cp*cy + sr*sp*sy 
  // x = sr*cp*cy - cr*sp*sy 
  // y = cr*sp*cy + sr*cp*sy 
  // z = cr*cp*sy - sr*sp*cy
  ```

## Limelight Multi-Camera Sensor Fusion

**Core Philosophy:** Winner-Takes-All, while visualizing everything.
- `AresSensorFusionSubsystem` is a lightweight complementary filter. It should **not** average camera positions directly because of distant noise.
- Multiple Limelights should be processed at the **Wrapper Level** (`LimelightVisionWrapper`).
- The wrapper compares `Target Area (TA)` to evaluate which camera is physically closer to generating the most trustworthy target calculation.
- The single "best" target is mapped to `VisionInputs` standard properties for Odometry fusion.

**Raw Camera Telemetry:**
- To preserve debugging, the `LimelightVisionWrapper` packages *every* valid Limelight measurement into a single flat array: `rawCameraPoses`.
- Layout is sequential length 7s: `[x,y,z,w,i,j,k,  x,y,z,w,i,j,k]`.
- AdvantageScope will dynamically read this `double[]` array as a `Pose3D[]` list and spawn multiple ghost robots to let drivers debug tracking noise.

## Simulation Integrity & HUD Rendering

When you construct any new `AresSubsystem`, verify if it has a real hardware equivalent. 
- If yes, use constructor-level Dependency Injection (IO Abstractions) from the `RobotContainer`.
- Initialize `DesktopSimLauncher` with identical dependencies to prove zero logic branching exists inside the subsystems. 
- The simulation physics loop is exactly `50Hz`, natively pushing `xMeters` and `yMeters` through `odometrySupplier` functions into `ArrayVisionIOSim` and `ArrayLidarIOSim`.

### Desktop Simulation HUD (`DesktopSimLauncher`)
We use `java.awt.Graphics2D` alongside `com.github.WilliamAHartman.Jamepad` for our advanced debugging visualizer:
- **Gamepads**: Raw gamepad input updates (including all ABXY face buttons, triggers, and bumpers) must be pushed into the HUD at 50Hz via `setGamepadState()`.
- **Glassmorphism UI**: When updating visual elements in `AresDriverStationApp.java` or `DesktopSimLauncher.java`, prioritize high-quality aesthetic UI designs over basic Swing primitives.


## Advanced Simulation Physics & Telemetry Quirks

Future developers, take note of these previously isolated engineering snags:

### 1. AdvantageScope Swerve State Array Format
When logging structural `SwerveModuleState[]` arrays directly as `double[]` streams for AdvantageScope to ingest, the 8-length array **MUST** be packed as `[Angle_rads, Speed_mps, Angle_rads, Speed_mps...]`. 
- If you pack it as `[Speed, Angle]`, AdvantageScope will visually swap the physical size of the visual 3D vector arrows with their rotational angle, leading to impossibly confusing debugging where wheels appear to rotate precisely parallel with their speed.

### 2. Gamepad Axis Normalization
FTC Hardware gamepads natively map "Stick Pushed UP" to a **Negative Y (`-1.0`)** value. 
- Standard GUI APIs (like Java AWT or SDL2/Jamepad) map "UP" as Positive Y (`+1.0`) because `(0,0)` is the top-left graphical corner.
- When injecting inputs via `VirtualGamepadWrapper`, you must manually invert the Y-axis native capture to strictly enforce `-1.0` for UP. 
- If this is misaligned, downstream math (like `AresGamepad` interpreting it) will cancel out errors, causing the robot to drive correctly but UI elements (like driver station crosshairs) to render upside down relative to finger inputs.

### 3. Dyn4j Wheel Slip vs Odometry (PID Differential)
In a deeply simulated environment, theoretical "Encoder Distance" and actual "Field Translation" will never perfectly match due to **Wheel Slip**.
- `dyn4j` applies simulated Field Drag (`linearDamping` / `angularDamping`) and Mass to the `robotBody`.
- A high `linearDamping` (e.g., `8.0`) forces the robot to move sluggishly. Pedro Pathing's translation PIDs will detect the robot lagging and significantly ramp up commanded target velocities (`vTarget`) to drag the heavy robot to its setpoint.
- Because `SwerveModuleIOSim` integrates internal wheel odometry based directly on these bloated closed-loop voltage commands (treating them as frictionless calculations), the theoretical accumulated wheel distance scales rapidly.
- E.g: The physics body moves `1.37 meters`, but the wheels spun the equivalent of `2.0 meters` fighting the carpet drag. This is an accurate physical model of mechanical slip; to reduce parity drift, lower the Dyn4j `linearDamping` constant.
