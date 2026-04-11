---
name: areslib-bindings
description: Documents the required structure for declaring button bindings in ARESLib using the AresGamepad wrappers. Use when mapping macros to controller buttons or troubleshooting the automated HTML controller documentation pipeline.
---

You are an expert FRC/FTC software engineer for Team ARES. When mapping controller inputs or resolving button bindings in the ARESLib codebase, adhere strictly to the following guidelines.

## 1. Architecture

ARESLib relies on a custom Gradle plugin (`generateControllerMappings`) that dynamically parses `RobotContainer.java` to build the team's drive team documentation (`CONTROLLER_MAPPINGS.html`). This means **binding syntax is strictly controlled**.

You MUST use the custom `AresGamepad` binding methods rather than raw WPILib/FTC `.onTrue()` logic, otherwise the CI parser will fail to register the correct driver manual.

## 2. Key Rules

### Rule A: Always use `bindOnTrue` or `bindWhileTrue`
Instead of calling `driver.x().onTrue(new Command())`, use the tracking hooks in `AresGamepad` that require explicit human-readable string documentation for the HTML generator.

**BAD:**
```java
driver.x().onTrue(new IntakeCommand());
```

**GOOD:**
```java
driver.bindOnTrue(
    driver.x(),
    "X Button",
    "Run Intake",
    new IntakeCommand()
);
```

### Rule B: Parameter Format
The `bind` hooks require exactly 4 parameters, ensuring the regex properly builds HTML columns:
1. `Trigger trigger` -> `driver.a()`, `operator.dpadUp()`
2. `String buttonName` -> Physical name (e.g., `"Left Bumper"`, `"A Button (Hold)"`)
3. `String actionName` -> Descriptive action (e.g., `"Shoot on Move"`, `"Speaker Mode"`)
4. `Command cmd` -> The execution callback.

## 3. Telemetry

The `AresGamepad` wrappers automatically echo the bound actions to NetworkTables/AdvantageScope under:
- `GamepadBindings/Gamepad/X Button` -> `Run Intake`
