---
name: surgical-editor
description: Implements a localized feature, fix, or refactor with a minimal diff. Use for targeted code changes where public behavior and unrelated code should remain stable.
---
# Surgical editor

## Goal
Produce the smallest correct patch consistent with existing architecture and style.

## Procedure
1. Identify the exact contract and acceptance criteria.
2. Reuse nearby abstractions, naming, and error handling.
3. Modify only necessary symbols; preserve public APIs unless change is required.
4. Avoid formatting unrelated lines, renaming unrelated code, or opportunistic refactors.
5. Add comments only for non-obvious invariants.
6. Validate the changed path with the narrowest test/build command.
7. Review `git diff --stat` and `git diff -- <changed-files>` before finishing.

## Output
State only:
- files changed
- behavior changed
- validation result
- remaining risk, if any

## Constraints
Do not rewrite complete files to make a small edit. Do not add dependencies when the project already has a suitable facility.
