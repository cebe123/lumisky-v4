---
name: lumisky-video-oes
description: Use for Media3 ExoPlayer, Surface, SurfaceTexture, GL_TEXTURE_EXTERNAL_OES, video variants, seamless loop, decoder fallback or video lifecycle.
---


Rules:
- Decoder callback only sets latest frame flag and `VIDEO_FRAME` dirty reason.
- `updateTexImage()` and transform-matrix read happen on current-context render thread.
- Source frame rate is independent from compositor FPS.
- Invisible/background/lease-lost state pauses or releases according to explicit policy.
- Context loss recreates OES texture and surface binding safely.
- Audio is disabled for wallpaper.
- Decoder/capability failure falls back to poster/hybrid scene without losing current scene.
- Use low/high variants based on runtime profile; no runtime transcode.

Tests: 30 FPS source under 120 FPS compositor, 100 surface/context cycles, lease churn, fallback poster, seamless loop.

