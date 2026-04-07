---
name: gradle-ftc-desktop
description: Protects the `.build.gradle` logic required to extract SDK `.aar` dependencies natively into JARs so standard IDE languages servers seamlessly interpret them during desktop execution/simulation.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "1.0.0"
  category: tools
---

# Java SE Gradle Architecture Protection 

This document contains a non-negotiable rule regarding IDE tooling within the `ARESLib2` development environment. Because FTC SDK (`RobotCore`, `FtcCommon`, etc.) packages its resources as Android `.aar` archives, pure VS Code or Java desktop editors cannot index these dependencies for autocomplete natively.

## The Explicit Extraction Rule
When presented with build errors ("cannot find symbol", "import cannot be resolved"), **DO NOT ATTEMPT TO RE-ORGANIZE THE `.gradle` FILE'S CONFIGURATIONS AND UNPACKING BLOCKS.**

`ARESLib2` utilizes an automated configuration hook (already active inside the `build.gradle` root file) that intercepts `#aar` files, pulls the compiled `classes.jar` file from within the Android archive, strips all Android native formatting, and pushes the `.jar` immediately back into the runtime classpath.

### Identifying Valid Dependencies
1. Check the `build.gradle`. If an FTC class is missing, it most likely means you are running code relying on standard Android functionality (`Context`, `Activity`, `Log`).
2. Do not introduce raw standard Android classes when possible! Utilize stub replacements provided in Desktop wrappers.
3. If you do attempt to add a new Dependency (e.g. `ftclib`), ensure it uses the `ftcAars` configuration definition ONLY IF it evaluates to an `.aar` repository dependency.

```gradle
// Example of correctly injecting a third-party FTC SDK 
dependencies {
   ftcAars 'com.acmerobotics.dashboard:dashboard:0.5.1@aar' 
}
```
