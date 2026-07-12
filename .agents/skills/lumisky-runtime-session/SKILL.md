---
name: lumisky-runtime-session
description: Use for RenderCommand, mailbox, RenderSession, scheduler, sensor/parallax, dirty-frame demand, visibility or transactional scene activation tasks.
---


Read `.agents/references/runtime-session.md`.

Checks:
- UI/service/sensor only emits immutable command.
- FIFO versus latest-wins semantics are explicit.
- Mutable scheduler/parallax/frame/cache state is session-owned.
- No direct caller-thread renderer method.
- Invisible state cancels callbacks and work.
- Dirty reason requests a frame without permanent continuous loop.
- Candidate scene commits only after first successful swap.
- Event queue cannot grow from sensor/touch MOVE.

Tests: reducer/coalescing, live+preview isolation, invisible silence, backlog, first-swap transaction.

