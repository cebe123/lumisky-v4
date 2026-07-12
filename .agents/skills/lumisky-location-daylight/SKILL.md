---
name: lumisky-location-daylight
description: Use for device/manual location mode, last-known/current fix, timezone, sunrise/sunset, day-night shader time, date/time broadcasts or polar state.
---


Read `.agents/references/location-daylight.md`.

Rules:
- Do not convert DEVICE preference to MANUAL because GPS is off or snapshot missing.
- Resolve current/last/persisted/manual/safe sources explicitly with age/accuracy metadata.
- Location refresh is throttled, cancellable and non-blocking.
- Reverse geocoding is label-only async cache, not daylight dependency.
- Coordinate timezone and date/time/timezone events invalidate daylight cache.
- Model normal/polar-day/polar-night results; avoid unconditional 06:00/18:00 fallback.
- Deliver result through command/state flow, not direct renderer call.

Tests: GPS off + cache, stale cache, permission loss, timezone border, midnight rollover, polar cases.

