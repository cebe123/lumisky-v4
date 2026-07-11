---
name: focused-debugger
description: Diagnoses a concrete build error, crash, failing test, incorrect behavior, or stack trace using a narrow evidence-first workflow.
---
# Focused debugger

## Goal
Find and fix the first proven root cause without speculative repository exploration.

## Procedure
1. Capture the exact failure: command, error, stack frame, input, and expected behavior.
2. Search the exact error and top project-owned symbol.
3. Inspect the failing definition and its direct caller/configuration only.
4. Form one primary hypothesis from evidence.
5. Apply the smallest fix that tests that hypothesis.
6. Re-run only the failing test/task first.
7. Expand scope only if the same failure remains.

## Rules
- Prefer compiler/test/runtime evidence over guesses.
- Do not propose many hypothetical causes before inspecting the concrete failure.
- Do not upgrade libraries, rewrite architecture, clear every cache, or change unrelated settings as a first action.
- Preserve logs only when they are needed for future diagnosis.

## Output
Root cause, minimal fix, validation, and any unresolved uncertainty.
