---
name: skill-authoring
description: Meta-skill for creating new AI skills when new subsystems, hardware abstractions, or framework features are added to ARESLib2. Use this whenever you build a new subsystem and want to generate a matching skill so the AI assistant understands it in future sessions.
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "1.0.0"
  category: meta
---

# Creating New ARESLib2 Skills

When you add a new subsystem, hardware abstraction, or framework feature to ARESLib2, **always create a matching skill** so the AI assistant understands the new code in future conversations. Skills are the AI's long-term memory for your codebase.

## When to Create a Skill

Create a new skill whenever you:
- Add a new subsystem (e.g., `ClawSubsystem`, `ElevatorSubsystem`)
- Implement a new IO abstraction (e.g., `ColorSensorIO`)
- Build a new framework utility (e.g., a path planner, a new logger)
- Add a complex integration (e.g., a new vision pipeline, a new motor controller API)
- Establish a new pattern that should be followed consistently

## Skill Directory Structure

```
.agents/skills/<skill-name>/
├── SKILL.md          # (REQUIRED) Main instruction file with YAML frontmatter
├── TROUBLESHOOTING.md  # (Optional) Common issues and fixes
├── TUNING.md           # (Optional) Tuning guides for control loops
├── examples/           # (Optional) Reference implementations
└── resources/          # (Optional) Config files, templates
```

## SKILL.md Template

Every skill MUST have a `SKILL.md` file with this exact format:

```markdown
---
name: <skill-name>
description: <One-sentence description. Start with a verb. Explain WHEN to use this skill.>
license: MIT
compatibility: Claude Code, Codex CLI, VS Code Copilot, Cursor
metadata:
  author: areslib-agent
  version: "1.0.0"
  category: <subsystem|hardware|framework|testing|simulation|tooling>
---

# <Human-Readable Title>

<1-2 paragraph overview of what this system does and why it exists.>

## 1. Architecture

<Describe the class hierarchy. Always reference the IO abstraction pattern:>
- `[Name]IO.java` — Interface defining inputs/outputs
- `[Name]IOReal.java` — Hardware implementation (uses HardwareMap)
- `[Name]IOSim.java` — Simulation implementation (uses dyn4j or math)
- `[Name]Subsystem.java` — Business logic, periodic updates

## 2. Key Classes & Methods

<List the most important classes and their critical methods. Include signatures.>

## 3. Configuration

<Document any constants, config objects, or tuning parameters.>

## 4. Usage Examples

<Show how to instantiate and use the subsystem in both real and sim modes.>

## 5. Telemetry & Logging

<Document what fields are logged and how to view them in AdvantageScope.>

## 6. Testing

<Show how to write a headless JUnit 5 test for this subsystem.>

## 7. Common Pitfalls

<List common mistakes and how to avoid them.>
```

## Naming Conventions

| Component Type | Skill Name Pattern | Example |
|:---|:---|:---|
| Subsystem | `areslib-<name>` | `areslib-elevator` |
| Hardware IO | `areslib-<sensor>` | `areslib-colorsensor` |
| Framework utility | `areslib-<feature>` | `areslib-pathing` |
| External integration | `<vendor>-<feature>` | `photonvision-pipeline` |
| Tooling/build | `<tool>-<purpose>` | `gradle-ftc-desktop` |

## Required Sections Checklist

Before finalizing a skill, verify it covers:

- [ ] **YAML frontmatter** with `name`, `description`, `license`, `compatibility`, `metadata`
- [ ] **Description starts with a verb** (e.g., "Helps write...", "Documents the...", "Defines the...")
- [ ] **Description says WHEN to use** (e.g., "Use when implementing...", "Use when adding...")
- [ ] **IO pattern documented** (Interface → IOReal → IOSim)
- [ ] **Code examples** for both real hardware and simulation
- [ ] **Test examples** showing headless JUnit 5 patterns
- [ ] **Coordinate system notes** if the subsystem deals with poses or field positions
- [ ] **AdvantageScope logging** — what keys are published and how to visualize

## How to Generate a Skill from Existing Code

When asked to create a skill for an existing subsystem, follow this procedure:

### Step 1: Scan the Source
```
# Find all related files
grep -r "class [Name]" src/main/java/ --include="*.java" -l
grep -r "[Name]IO" src/main/java/ --include="*.java" -l
grep -r "[Name]" src/test/java/ --include="*.java" -l
```

### Step 2: Extract the Architecture
- Identify the IO interface and all implementations (Real, Sim)
- Identify the Subsystem class and its periodic() method
- Identify any Commands that use this subsystem
- Identify any StateMachine enums

### Step 3: Document Configuration
- Find all `public static final` constants
- Find any `Config` inner classes
- Find PID/feedforward tuning values

### Step 4: Document Telemetry
- Find all `AresTelemetry.log()` or `@AutoLog` annotations
- Map log keys to AdvantageScope visualization types

### Step 5: Write the SKILL.md
Use the template above. Be specific — include actual class names, actual method signatures, actual log keys. Generic skills are useless.

### Step 6: Write Tests (if missing)
If the subsystem doesn't have a test file yet, create one following the `areslib-testing` skill patterns.

### Step 7: Register the Skill
Place the skill in `.agents/skills/<skill-name>/SKILL.md` and commit:
```bash
git add .agents/skills/<skill-name>/
git commit -m "feat: add <skill-name> AI skill for <purpose>"
git push
```

## Anti-Patterns

### Don't: Create generic skills
```markdown
# BAD — too vague, AI can't use this
## Usage
Use the subsystem by calling its methods.
```

### Don't: Skip the IO pattern documentation
```markdown
# BAD — doesn't explain how to swap between real and sim
## Architecture
There is a subsystem class.
```

### Don't: Forget coordinate system context
```markdown
# BAD — leads to flipped X/Y bugs in every future session
## Poses
The subsystem uses poses.

# GOOD — specific to ARESLib2's coordinate convention
## Poses
All poses use WPILib convention (X-forward, Y-left, θ CCW+).
Pedro Pathing uses (X-right, Y-forward) — convert via:
  wpilibX = pedroY, wpilibY = -pedroX
```

### Don't: Hardcode version-specific APIs
```markdown
# BAD — will break when Pedro Pathing updates
follower.followPath(path, true);

# GOOD — reference the skill that tracks API changes
See `pedro-pathing` skill for current API signatures.
```

## Updating Existing Skills

When modifying an existing subsystem:
1. Read the existing skill: `.agents/skills/<name>/SKILL.md`
2. Update any changed method signatures, config values, or log keys
3. Bump the `version` in YAML frontmatter
4. Commit with message: `docs: update <skill-name> skill for <change>`
