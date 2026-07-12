---
name: lumisky-diff-review
description: Use to review a targeted Lumisky git diff for correctness, V8 architecture regressions, performance risks and missing tests; do not implement changes.
---


Review only `git diff -- <target paths>` plus directly required definitions/tests.

Priority findings:
1. Thread/GL ownership and singleton mutable state.
2. Invisible-state work, event backlog and lifecycle leaks.
3. Hot-path allocations, I/O, JSON/string/map lookup.
4. FPS/source/compositor confusion and preview lease count.
5. Transactional scene/fallback/context-loss correctness.
6. Location preference/fallback/timezone regressions.
7. Asset/schema/shader/video validation gaps.
8. Missing targeted tests.

Output findings by severity with path and concrete failure mode. Do not restate unaffected code and do not modify files.

