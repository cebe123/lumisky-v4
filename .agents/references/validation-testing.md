# V8 kısa referans — validation ve test

Unit: command reducer, session/EGL state machine, scheduler/deadline, governor hysteresis, compiler/validator, fallback, daylight/location.

Integration: visibility stops work, surface resize/recreate, context loss, rapid scene switch, preview lease, thermal/power, video OES restore.

Visual regression: deterministic time/parallax/quality/aspect. Legacy-yeni backend toleranslı diff.

Content validation: schema + semantic; paths, duplicate IDs, missing assets, shader uniform types, alpha/canvas, video poster/codec, license, safe fallback.

Dar test ilkesi: changed module compile -> ilgili unit -> integration only when lifecycle/cross-module -> full release gate last.
