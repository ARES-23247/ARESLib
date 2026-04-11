# ARESLib Controller Mappings

This document is automatically generated during compilation from `RobotContainer.java`.

## 🎮 Pilot (Driver) - Controller 0

> **Primary drive controls and high-level macro sequences.**

| Controller Input | Mapped Action |
| :--- | :--- |
| <kbd>Left Joystick</kbd> | **Holonomic Translation (X/Y)** |
| <kbd>Right Joystick</kbd> | **Holonomic Rotation** |
| <kbd>X Button</kbd> | **SPEAKER Mode** |\n| <kbd>Y Button</kbd> | **HP (Human Player) Mode** |\n| <kbd>A Button (Hold)</kbd> | **Align to Tag** |\n| <kbd>B Button</kbd> | **Reset Field-Centric Yaw** |\n| <kbd>Right Bumper</kbd> | **Shoot On The Move** |\n
---

## 🕹️ CoPilot (Operator) - Controller 1

> **Manual overrides, sub-mechanism control, and fault resets.**

| Controller Input | Mapped Action |
| :--- | :--- |
| <kbd>X Button</kbd> | **POOP (Ground Intake) Mode** |\n| <kbd>Y Button</kbd> | **CLIMB Mode** |\n| <kbd>D-Pad Up</kbd> | **Elevator: HIGH** |\n| <kbd>D-Pad Down</kbd> | **Elevator: LOW** |\n
---
> [!NOTE]
> All automated scoring sequences natively return the superstructure to the 
> safe `STOWED` state immediately upon release of the binding.
