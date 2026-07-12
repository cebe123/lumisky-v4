---
name: lumisky-session-handoff
description: Use at the end of a long Lumisky task to create a compact continuation note that prevents the next Codex session from rereading the repository.
---


Fill `.agents/templates/SESSION_HANDOFF.md` with facts only.

Limits:
- Maximum 250 words.
- Include exact changed paths and relevant symbols.
- Include commands/tests and their outcomes.
- State one next exact action.
- List files/areas that do not need rereading.
- Do not copy large code, logs, architecture sections or speculative plans.
- If no continuation is needed, do not create a handoff.

