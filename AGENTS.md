# AGENTS.md

## Project Type

This is an Android live wallpaper app using Kotlin, MVVM, Compose, and OpenGL ES.

The project may include:
- WallpaperService and GLSurfaceView lifecycle
- Modular wallpaper definitions and shader-based rendering
- Time-based sun/moon/sky effects and preview screens
- Wallpaper selection UI, repository/ViewModel architecture
- FPS and battery optimization logic

## Primary Objective

Use minimum tokens in assistant responses (≤300 unless requested), preserve existing architecture, and make minimal code changes.

Always prefer:
- small patches (prefer single-file edits or ≤30 changed lines)
- targeted file access
- preserving APIs and architecture
- concise outputs
- battery-friendly rendering
- When constraints conflict, apply precedence: (1) preserve lifecycle/renderer safety, (2) avoid battery regressions, (3) preserve APIs, (4) minimize files changed, (5) minimize response tokens.

## Context Management

Before every task:
1. Keep only current task, target files, active constraints, and relevant architecture.
2. Do not reuse unrelated old plans, TODOs, or logs.
3. If total context exceeds 3000 tokens, summarize only: request, referenced files, active constraints.
4. If a required file is missing or unreadable, report: 'MISSING: path/to/file' and ask: 'Can I open the file or should I infer changes?'
5. Do not repeat the same conclusion once established.

## File Access Rules

Do not scan the whole repo unless explicitly requested.
Priority: AGENTS.md, task files, related interfaces/base classes, Gradle/config, manifest/resources.
Avoid broad repo searches, large dependency analysis, unrelated module inspection, and reading binary files unless needed.

## Code Change Rules

Use SMALL PATCH MODE.
- Do not rewrite full files unless necessary.
- Do not change unrelated code.
- Do not rename files/classes unless required. If a rename is needed, propose it with risk analysis and get approval first.
- Do not perform formatting-only changes.
- Do not add dependencies unless necessary.
- Prefer incremental changes.
- Preserve APIs, package structure, naming, and architecture.
- Change only affected functions/blocks whenever possible.

## Android / Wallpaper Rules

Respect Android architecture and wallpaper lifecycle.
- Preserve MVVM, repository/ViewModel boundaries, Compose UI structure, lifecycle-aware state handling, navigation, resources, and wallpaper extensibility.
- Avoid unnecessary Gradle, manifest, permissions, or UI rewrites.
- Preserve WallpaperService and GLSurfaceView lifecycle, EGL context, shader loading, texture loading, FPS limit logic, minute-based home rendering, preview mode, and low-battery behavior.
- Avoid continuous rendering, extra per-frame CPU work, repeated texture loading, shader recompilation, and UI thread blocking.

## Shader Rules

When changing shaders:
- edit only the required shader file
- preserve existing uniforms and shared shader reuse
- avoid expensive loops and unnecessary branches
- keep mobile GPU performance in mind
- preserve aspect-ratio, sun/moon/horizon behavior unless targeted

## Output Style

Be concise, technical, and focused.
Do not include hidden reasoning, unnecessary theory, unchanged file dumps, or verbose reports.
Use code blocks only for changed snippets or new files.

Prefer final report format:
- Files read:
- Changed files:
- What changed:
- What was not touched:
- Validation:
- Next smallest step:
- Keep responses compact; limit each section to 1-3 short bullet points.

## Task Execution Protocol

For each task:
1. Restate the task in one sentence.
2. List minimum files needed.
3. Explain why each file is needed.
4. Make the smallest safe change.
5. Validate if possible.
- Validation means: (a) project compiles, (b) existing unit tests pass; if impossible, state which validations were skipped.
6. Report concisely.

If a task is large (>3 files or >200 changed lines): plan in phases, implement only phase 1, and summarize changed files, risks, and next step.
If a change is risky: explain why, keep the patch reversible, and if constraints prevent a viable fix, offer up to 3 options with trade-offs.

## Performance Rules

Priority: stability, battery efficiency, smooth rendering, maintainability, visual quality.
For performance tasks, include metrics: FPS, jank, memory, GPU load, battery impact.
Report against targets: FPS ≥30 home, ≥60 preview, frame time P95 <33ms, memory delta <10MB, battery CPU increase <5%.

## Safety Rule

Favor consistency, low token usage, and minimal diffs over aggressive refactoring.