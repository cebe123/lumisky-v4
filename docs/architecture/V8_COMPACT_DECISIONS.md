# Lumisky V8 compact decisions

Bu dosya hızlı referanstır; ayrıntı için aynı klasördeki tam V8 belgesinin yalnız ilgili bölümünü aç.

## Sıra

0 baseline -> 1 session/command -> 2 sensor/frame demand -> 3 compiled zero-allocation runtime -> 4 EGL/transactional loading -> 5 adaptive preview FPS -> 6 location parity -> 7 hybrid layer backend -> 8 VideoOes -> 9 preview lease/UI -> 10 compiler/CI/rollout.

Bu sıra atlanmaz. Video, mesh warp, genel FBO, PAD ve program binary; ownership, compiler ve EGL tamamlanmadan ana iş değildir.

## Runtime

Command-only bridge; render-thread/session ownership; FIFO + latest-wins mailbox; dirty reasons; visible-only work; first-swap commit.

## Rendering

Typed source + compiled graph; hot path I/O/JSON/string/map/allocation yok; update/draw ayrı; transparent order stabil; FBO profiler-gated.

## Preview

Live default 30. Fullscreen start 60/max 120. Catalog start 30/max 60, thumbnail-first, scroll 0 renderer, max 1 lease. Degrade fast/promote slow.

## Location

DEVICE preference kalıcı; current/last/persisted/manual/safe source resolution; async geocoder; coordinate timezone; date/time events; polar state.

## Content

Offline layer/inpainting/transcode/variants. Runtime only validates/selects/loads. Self-contained package, schema + semantic validator, poster/safe fallback.
