---
name: lumisky-surgical-editor
description: Use for a small Lumisky bug fix or implementation after scope is known; preserve APIs, user changes and V8 invariants with the smallest patch.
---


1. Confirm scope and inspect targeted current diff.
2. State the behavioral invariant being changed.
3. Prefer editing existing type/policy over adding parallel abstractions.
4. Touch only necessary files; no broad rename, formatting or dependency update.
5. Preserve backward compatibility/fallback unless the task explicitly removes it.
6. Add/update the narrowest test that fails before and passes after.
7. Run module compile plus targeted test.
8. Report changed paths, validation and one real risk only.

Do not use for broad architecture discovery; run `$lumisky-context-scout` first when scope is unknown.

