---
name: lumisky-test-selector
description: Use after Lumisky changes to choose the narrowest sufficient Gradle tests, validators, instrumentation or performance evidence from changed paths.
---


1. Run `.agents/scripts/changed-scope.ps1` or inspect targeted diff.
2. Map paths using `.agents/references/validation-testing.md`.
3. Select in order:
   - changed module compile,
   - exact unit test/class,
   - relevant validator,
   - lifecycle/integration only for cross-thread/surface/preview/video,
   - full suite only for release or broad refactor.
4. Do not run AGI/Perfetto/Macrobenchmark as interchangeable tools; select by metric.
5. Print commands before executing expensive/device tests.
6. Report skipped tests and exact reason.

Helper: `.agents/scripts/select-tests.ps1`; add `-Run` only when execution is intended.

