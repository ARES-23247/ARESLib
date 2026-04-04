# ARESlib Global Constraints
* **Mission:** You are building ARESlib, an FTC Java library designed to mirror FRC's WPILib Command-based framework.
* **Pure Java:** ZERO FRC HAL or JNI dependencies. Any WPILib math or command classes must be ported as 100% pure Java to run on FTC Android Control Hubs or standard desktop JVMs.
* **AdvantageKit IO Pattern:** All subsystems must be decoupled from hardware using IO interfaces (e.g., `SwerveModuleIO`, `SwerveModuleIOReal`, `SwerveModuleIOSim`).
* **Loop Optimization:** Loops must be sub-10ms. You MUST use PhotonCore thread-bypassing, REV Hub manual bulk caching, and strict single-payload I2C bulk reads for coprocessors.
