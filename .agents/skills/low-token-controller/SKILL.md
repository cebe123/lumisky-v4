---
name: low-token-controller
description: Minimizes context and output usage for coding tasks. Use when the user says low-token, token-efficient, economical, minimal-context, az token, or gereksiz dosya okuma.
---
# Low-token controller

## Goal
Complete the task with the smallest sufficient context, tool usage, diff, and response.

## Protocol
1. Restate the target internally in one sentence; do not produce a long plan.
2. Inspect filenames and search results before opening files.
3. Open only the smallest relevant ranges. Expand only when blocked.
4. Prefer existing project patterns over external research or broad exploration.
5. Change the fewest files and lines that correctly solve the task.
6. Run the narrowest useful validation first.
7. Stop after acceptance criteria pass; do not perform optional cleanup.
8. Report only: changed files, essential behavior, validation, unresolved risk.

## Limits
- Do not scan the whole repository by default.
- Do not reread unchanged files.
- Do not paste full files unless explicitly requested.
- Do not repeat command output or explain routine steps.
- Ask a question only when the missing answer materially changes implementation.
