---
name: advantagescope-hud-sim
description: Helps construct high-fidelity visualizers, gamepads mappings, and Java 2D Graphics environments for interacting with simulated physics (e.g., ArrayLidarIOSim, dyn4j) rendering inside AdvantageScope arrays or internal desktop windows.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "1.0.0"
  category: simulation
---

# AdvantageScope & HUD Simulation Standards

This skill defines the visual architecture expectations for desktop simulation debugging, specifically inside the `AresDriverStationApp.java` and `DesktopSimLauncher.java` environments.

## Premium UI Design
All visual output rendered in a desktop Swing application should prioritize **modern glassmorphism aesthetics**. Do not settle for default Swing components.

- **Backgrounds**: Use dark mode rendering (`#1C1C1E`) with subtle gradients.
- **Translucency**: Draw semi-transparent rounded rectangles with alpha layers.
- **Typography**: Utilize highly legible, anti-aliased System fonts (`Inter`, `San Francisco`, or `Segoe UI`).
- **Telemetry Bars**: Draw real-time bars with distinct, saturated colors (e.g. Cyan for joysticks, vibrant Green for triggers, bright Red/Blue for buttons).

## Physics & Gamepad Mappings Integration

When updating `AresDriverStationApp.java`:
1. **Gamepad Rate Limit**: `setGamepadState()` must be fed exactly at 50Hz, aligned with the `dyn4j` physics step interpolation.
2. **Keybindings Overlay**: If new keyboard shortcuts are mapped to Virtual Gamepads (e.g., `A` key for `Cross/A` button), you MUST dynamically draw a "Cheat Sheet" on the HUD rendering cycle so drivers know the mapping without looking at code.
3. **Face Buttons Array**: The A, B, X, Y buttons must be extracted individually and passed through the constructor or state updater methods correctly. Be mindful of Jamepad mapping versus XInput mappings.

## Hardware Spoofing to AdvantageScope
When extracting debugging telemetry that does not natively map to WPILib 3D geometries (e.g., Lidar Arrays, Time of Flight distance matricies):

1. **Raycasting**: Do not just log the `distance` double natively. Render ghost "points" around the robot based on the exact yaw angles of the hardware array. 
2. **Point Clouds Map**: Format the data as an active `double[]` flat array containing `[x1, y1, z1, x2, y2, z2...]` structure representing each physical contact point in 3D space, and pipe it through the `updateInputs()` AdvantageKit logger. AdvantageScope can then view this array as a 3D Pointcloud!
