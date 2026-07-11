---
name: diff-reviewer
description: Reviews current Git changes or a patch for correctness, regressions, security, and performance. Use for PR review, git diff review, or pre-commit checking.
---
# Diff reviewer

## Scope
Review the diff first, not the full repository.

## Procedure
1. Read `git status --short`, `git diff --stat`, then the relevant diff.
2. Open surrounding code only when required to verify a finding.
3. Check changed behavior for correctness, lifecycle/state errors, nullability, concurrency, security, performance, and missing tests.
4. Report only actionable findings supported by file and line evidence.
5. Rank findings: blocker, high, medium, low.
6. If no issue is found, state that directly and mention the validation gap only if material.

## Constraints
- Do not summarize every changed line.
- Do not report style preferences already enforced by formatters.
- Do not invent issues without a concrete failure path.
- Cap findings at the most important 8 unless the user requests exhaustive review.
