# AGENTS.md

## Project Type

This is a production-grade Android live wallpaper application using Kotlin, MVVM, Compose, and OpenGL ES rendering.

The project may include:
- WallpaperService
- GLSurfaceView / renderer lifecycle
- Modular wallpaper definitions
- Shader-based wallpapers
- Static image / texture layers
- Time-based sun, moon, sky, and effect systems
- Preview screens
- Wallpaper list UI
- Repository / ViewModel architecture
- FPS and battery optimization logic

## Primary Objective

Work with minimum token usage, maximum consistency, and minimal code changes.

Always prefer:
- small patches
- targeted file access
- architecture preservation
- concise outputs
- deterministic implementation

## Context Management

Before every task:
1. Ignore unrelated previous conversations, old plans, obsolete TODOs, and stale assumptions.
2. Keep only the current task, active constraints, relevant architecture, and files directly required.
3. Do not carry over previous failed approaches unless they are explicitly relevant.
4. If context is too large, summarize only the necessary state and discard the rest.

Preserve only:
- current user request
- current target files
- active build/runtime constraints
- important project architecture
- renderer lifecycle requirements
- wallpaper performance requirements

Discard:
- unrelated logs
- unrelated previous refactor ideas
- old failed solutions
- unused file references
- stale TODO lists
- unrelated dependency discussions
- full repository memory

## File Access Rules

Do not scan the whole repository unless explicitly requested.

File priority:
1. AGENTS.md
2. Files directly related to the current task
3. Interfaces/base classes used by those files
4. Required Gradle/config files
5. Manifest/resources only if necessary

Forbidden unless explicitly requested:
- opening all files
- full repository exploration
- broad grep/search over the entire project
- reading unrelated packages
- large dependency analysis
- unrelated module inspection

When searching:
- use specific keywords
- search only the relevant module/folder if possible
- avoid repeated searches

## Code Change Rules

Use SMALL PATCH MODE.

Rules:
- Do not rewrite full files.
- Do not change unrelated code.
- Do not rename files/classes unless required.
- Do not perform formatting-only changes.
- Do not add abstractions unless needed.
- Do not add dependencies unless necessary.
- Do not modernize unrelated code.
- Preserve existing APIs when possible.
- Preserve package structure.
- Preserve naming conventions.
- Preserve current architecture.

Prefer:
- changing only affected functions
- changing only affected blocks
- extending existing systems
- keeping diffs small
- making one logical change at a time

## Android Rules

Respect the existing Android architecture.

Preserve:
- MVVM structure
- Repository/ViewModel boundaries
- Compose UI structure
- lifecycle-aware state handling
- existing navigation structure
- existing resource organization

Avoid:
- unnecessary Gradle changes
- unnecessary manifest changes
- unnecessary permission additions
- unnecessary Compose rewrites
- converting XML to Compose or Compose to XML unless requested

## OpenGL ES / Wallpaper Rules

Preserve rendering correctness and battery efficiency.

Do not break:
- WallpaperService lifecycle
- GLSurfaceView lifecycle
- renderer initialization
- EGL context behavior
- shader loading system
- texture loading system
- FPS limit logic
- minute-based rendering mode
- preview rendering mode
- low battery wallpaper behavior

Avoid:
- introducing continuous rendering unless required
- increasing render frequency unnecessarily
- loading textures repeatedly
- recompiling shaders unnecessarily
- adding heavy per-frame CPU work
- breaking modular wallpaper configuration

## Shader Rules

When modifying shaders:
- change only the required shader file
- preserve existing uniforms when possible
- do not rename uniforms unless all usages are updated
- avoid expensive loops
- avoid unnecessary branches
- keep mobile GPU performance in mind
- preserve aspect-ratio correction logic
- preserve sun/moon/horizon behavior unless the task targets it

## Token Optimization Rules

Keep responses compact.

Avoid:
- long explanations
- repeated task summaries
- dumping entire files
- showing unchanged code
- listing many alternatives
- verbose reasoning
- unnecessary logs
- unnecessary markdown tables
- unrelated suggestions

Prefer final output format:

- Changed files:
- What changed:
- What was not touched:
- Validation:
- Next smallest step:

## Task Execution Protocol

For each task:
1. Briefly restate the task in one sentence.
2. List the minimum files needed.
3. Explain why each file is needed in one short phrase.
4. Make the smallest safe change.
5. Validate if possible.
6. Report concisely.

If the task is large:
- split it into small phases
- implement only the first safe phase
- do not attempt a giant refactor

If unsure:
- inspect the smallest relevant file first
- avoid assumptions
- ask only when blocked
- otherwise make the smallest safe implementation

## Output Style

Be concise, technical, and implementation-focused.

Do not include:
- hidden reasoning
- unnecessary theory
- full unchanged files
- long background explanations

Use code blocks only for changed snippets or new files.

## Safety Rule

Consistency, low token usage, and minimal diffs are more important than aggressive refactoring.
