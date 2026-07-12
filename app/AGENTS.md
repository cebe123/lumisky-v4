# App modülü kuralları

- Sorumluluk: Compose UI, katalog, detail/fullscreen preview, seçim/apply akışı ve preview lease istemcisi.
- Katalog thumbnail-first kalır. Scroll sırasında aktif GL/video renderer 0; scroll durduğunda tek lease.
- Card başına kalıcı `GLSurfaceView`/`TextureView`/decoder oluşturma.
- Fullscreen preview başlangıç 60, mutlak max 120 FPS; katalog max 60 FPS. Kararı UI değil shared governor verir.
- Compose state değişikliği render engine mutable state'ine doğrudan yazmaz; contract/command/facade kullanır.
- Main thread'de shader compile, texture decode/upload, full definition parse veya location blocking yok.
- UI değişikliğinde recomposition scope, thumbnail memory ve scroll jank düşünülür.
- App tarafına GL resource ID, EGL object veya session-owned controller taşıma.

İlk doğrulama: hedef Compose test/unit test; sonra `:app:compileDebugKotlin`. Preview navigation/lease değiştiyse debug assemble veya ilgili instrumentation testi.
