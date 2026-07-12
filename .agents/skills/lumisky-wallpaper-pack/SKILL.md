---
name: lumisky-wallpaper-pack
description: Use for Lumisky wallpaper ZIPs, definitions, catalog entries, layers, shaders, video assets, WallpaperPackCompiler, schema or semantic validation.
---


1. Determine source kind: layered, hybrid, video or procedural.
2. Runtime package must be self-contained with relative paths.
3. Offline/build-time work: segmentation, inpainting, transcoding, thumbnail/poster, variants and content hash.
4. Validate schema plus semantics: IDs, paths, assets, alpha/canvas, shader uniform types, video poster/codec, license/source and safe fallback.
5. Generate lightweight catalog metadata separately from heavy definition.
6. Keep per-runtime asset variants and estimated memory/GPU cost.
7. Do not make runtime parse every definition at startup.
8. Produce deterministic output; invalid package fails with actionable paths.

For image layer packages, verify recomposition/visual parity and parallax overscan.

