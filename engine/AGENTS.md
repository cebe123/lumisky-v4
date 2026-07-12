# Engine modülü kuralları

- Bütün GL çağrıları ve GL handle'ları render thread/session owner'ıdır.
- `SceneScheduler`, `FrameDemandController`, `ParallaxState`, `SceneState`, frame state ve cache session-bound'dır.
- JSON, file I/O, shader source okuma ve decode frame loop içinde yasaktır.
- `SceneCompiler` çıktısı typed `CompiledWallpaperScene/CompiledLayerGraph` olmalıdır.
- Hot path'te collection/data-class allocation, `Calendar`, `ZoneId.of`, string/map uniform lookup kullanma.
- `needsUpdate` ve `needsDraw` ayrı kararlardır. Low-FPS update tek başına GPU draw sayısını azalttı varsayma.
- Transparent layer sırası korunur. FBO yalnız profiler kanıtıyla.
- Video frame callback GL çağrısı yapmaz; `updateTexImage()` current context'li render thread'dedir.
- Render scale gerçek viewport/FBO boyutuna uygulanmalıdır.
- Shader/texture candidate failure current scene'i bozmamalıdır.

İlk doğrulama: ilgili engine unit test, shader compile/validator ve `:engine:compileDebugKotlin`.
