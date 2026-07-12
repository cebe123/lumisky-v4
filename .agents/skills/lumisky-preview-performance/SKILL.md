---
name: lumisky-preview-performance
description: Use for fullscreen 120 FPS, catalog 60 FPS, live FPS policy, adaptive governor, render scale, preview lease, thumbnail-first UI or scroll jank.
---


Read `.agents/references/preview-performance.md`.

Invariants:
- Fullscreen start 60/max 120; catalog start 30/max 60; live default 30.
- Filter steps against actual display modes; never draw 120 on a 60 Hz surface.
- Governor uses real stability window; degrade fast, promote slowly.
- FPS, render scale and quality form one decision.
- Catalog scroll: renderer 0. Settled center item: max one lease.
- Thumbnail remains until first successful swap.
- Lease loss pauses/releases sensor/video/session according to policy.

Evidence: frame deadlines, request/draw/swap counts, lease count, scroll benchmark, thermal/power transition.

