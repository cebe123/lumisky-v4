---
name: compact-session-handoff
description: Creates or updates a concise task handoff for continuing work in a new conversation without reloading large chat history. Use when pausing, switching agents, or preserving implementation state.
---
# Compact session handoff

## Goal
Preserve only information required to continue the current task.

## Write
Create or update `.agents/context/CURRENT_TASK.md` with at most 80 lines:

- Objective
- Acceptance criteria
- Relevant files and symbols
- Decisions and invariants
- Changes completed
- Validation performed
- Current failure or blocker
- Exact next step
- Commands worth reusing

## Rules
- Replace stale content; do not append a chronological diary.
- Do not paste full source code, logs, chat transcripts, or generic project documentation.
- Include paths and symbol names instead of prose descriptions where possible.
- Record only confirmed facts; label assumptions.
- Never include secrets or credentials.
