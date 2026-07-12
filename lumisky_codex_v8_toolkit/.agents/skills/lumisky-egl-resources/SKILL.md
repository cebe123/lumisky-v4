---
name: lumisky-egl-resources
description: Use for EGL surface/context state, context loss, GL thread ownership, texture/program/FBO creation, upload, resize, release or shader warmup failures.
---


Read module `wallpaper/AGENTS.md` or `engine/AGENTS.md` and the EGL section of the full V8 doc only if needed.

Rules:
- All EGL/GL calls require render-thread assertion and current context when applicable.
- Separate window-surface detach from full context release.
- On context loss: invalidate Java/Kotlin handles; do not rely on `glDelete*` or old IDs.
- Recreate size/quality-dependent FBOs after resize/profile change.
- Decode off GL thread; upload under frame budget or loading transaction.
- Shader/program failure keeps current scene and selects fallback.
- No global texture/program/FBO ID cache.

Tests: 100 surface cycles, forced context loss, bad shader, resize/quality invalidation, no stale handle.

