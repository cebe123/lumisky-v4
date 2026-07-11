---
name: selective-test-runner
description: Selects and runs the smallest relevant validation for changed code. Use after implementing or debugging changes when tests, builds, lint, or checks are needed.
---
# Selective test runner

## Goal
Gain confidence with minimal execution and context output.

## Validation ladder
1. Existing test targeting the changed class/function/module.
2. Module-level compile or unit tests.
3. Relevant lint/static analysis for changed files or module.
4. Broader build/test only when lower levels cannot validate the change or expose integration risk.

## Procedure
- Infer affected module from changed paths and build files.
- Use quiet/plain output flags when available.
- Capture the first useful failure; avoid dumping complete logs.
- On failure, extract the command, failing target, primary error, and nearest project-owned stack frame.
- Do not rerun successful checks without a code/config change.

## Stop conditions
Stop when acceptance criteria are validated and no material integration risk remains.
