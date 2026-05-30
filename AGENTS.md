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
- battery-friendly rendering

## Context Management

Before every task:
1. Ignore unrelated previous conversations, old plans, obsolete TODOs, and stale assumptions.
2. Keep only the current task, active constraints, relevant architecture, and files directly required.
3. Do not carry over previous failed approaches unless they are explicitly relevant.
4. If context is too large, summarize only the necessary state and discard the rest.
5. Do not repeat the same analysis when the relevant conclusion is already established.

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
- identify the minimum relevant files before editing
- do not read large binary files unless they are directly required

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
- Prefer incremental migration over large refactors.
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
- wallpaper extensibility

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
- separation between preview mode and home screen mode
- low battery wallpaper behavior

Avoid:
- introducing continuous rendering unless required
- increasing render frequency unnecessarily
- loading textures repeatedly
- recompiling shaders unnecessarily
- adding heavy per-frame CPU work
- blocking the render thread or UI thread
- rendering every wallpaper list item at the same time
- breaking modular wallpaper configuration

## Shader Rules

When modifying shaders:
- change only the required shader file
- preserve existing uniforms when possible
- do not rename uniforms unless all usages are updated
- preserve shared shader ownership and reuse
- avoid expensive loops
- avoid unnecessary branches
- keep mobile GPU performance in mind
- preserve aspect-ratio correction logic
- preserve sun/moon/horizon behavior unless the task targets it
- avoid texture duplication and unnecessary asset copies

## Token Optimization Rules

Keep responses compact.

For every task response, include:
- Files read:
  - list only files actually opened/read
  - include one short reason per file
  - do not include command outputs as files unless a file was opened
  - do not paste unchanged file content

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

- Files read:
- Changed files:
- What changed:
- What was not touched:
- Validation:
- Next smallest step:

For development tasks:
- Ask for or infer the smallest target area before reading files.
- Start with `git status --short`, then only the directly affected files.
- Use scoped `rg -n "symbol" path/to/module` instead of broad repository searches.
- Read interfaces/base classes only after the target file proves they are needed.
- Avoid generated/cache folders from `.gitignore`: `build/`, `.gradle/`, `.kotlin/`, `.codex-local/`, `.understand-anything/`, `snapshot-output/`.
- Prefer focused validation tasks over full builds unless release/runtime confidence requires a full build.
- If the requested change is broad, split it into phases and implement only the first safe phase.

## Task Execution Protocol

For each task:
1. Briefly restate the task in one sentence.
2. List the minimum files needed.
3. Explain why each file is needed in one short phrase.
4. Make the smallest safe change.
5. Validate if possible.
6. Report concisely.

If the task is large:
- create a short plan first
- split it into small phases
- implement only the first safe phase
- do not attempt a giant refactor
- summarize changed files, risks, and the next safe step after each phase

If a change is risky:
- explain the reason before editing
- keep the patch reversible and isolated

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

## Performance Rules

Priority order:
1. Stability
2. Battery efficiency
3. Smooth rendering
4. Maintainability
5. Visual quality

For performance tasks, evaluate:
- FPS
- jank
- memory
- GPU load
- battery impact

## MCP Awareness

When using filesystem MCP:
- stay inside the workspace
- do not inspect files outside the project unless explicitly requested
- avoid reading large binary files unnecessarily

When using Sequential Thinking:
- prefer short plans over long reasoning
- split work into small checkpoints

When using GitHub MCP:
- keep PR descriptions concise
- keep commit messages short


## Crucial Development Rules

- Do not perform large refactors/rewrites.
- Read only the required files.
- Always write a short plan before starting any task.
- Android WallpaperService lifecycle must not be broken.
- OpenGL ES renderer thread safety must be preserved.
- Preview mode and set wallpaper mode must remain separated.
- Minute-based render logic on the home screen must be preserved.
- Continuous rendering should only be applied if explicitly requested.
- Battery optimization is the highest priority.
- OpenGL ES compatibility must be preserved in shader changes.
- Avoid duplicate textures or asset copies.
- Write a risk analysis before any change to Gradle, Manifest, Service, Renderer, or shader loader.
- Every patch must be small and reversible.
- At the end of the task, report the changed files and the next safe step.

## Safety Rule

Consistency, low token usage, and minimal diffs are more important than aggressive refactoring.
