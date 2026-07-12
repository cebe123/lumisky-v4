---
name: lumisky-layer-shader
description: Use for SceneCompiler, CompiledLayerGraph, TextureLayer, ShaderEffectLayer, particles, animation tracks, parallax, render passes or GLSL changes.
---


Read `.agents/references/rendering-content.md`.

Workflow:
1. Identify source kind and runtime profile.
2. Keep JSON parsing/compiler work outside render.
3. Compile uniforms/assets to typed bindings and integer/index access.
4. Preserve pass/zIndex/declaration order for transparency.
5. Separate update cadence from draw/cache invalidation.
6. Combine base transform + animation + parallax deterministically; test overscan edges.
7. GLSL: verify GLES target, uniform contract, compile/link log and visual parity.
8. Do not add FBO/post-process/mesh warp without measured need.

Tests: compiler/validator, deterministic animation, parallax bounds, shader compile and screenshot diff.

