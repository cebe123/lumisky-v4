# Build-logic ve içerik compiler kuralları

- Runtime'da segmentation, inpainting, transcode, thumbnail üretimi veya definition migration işi bırakma.
- `WallpaperPackCompiler` deterministic ve cacheable input/output kullanmalıdır.
- JSON Schema + semantic validation birlikte uygulanır.
- Kontroller: duplicate ID/layer, path traversal, missing asset, alpha/canvas, shader source/uniform, video codec/variant/poster, license/source metadata, safe fallback.
- Generated index/content hash sırası deterministic olmalıdır.
- Büyük asset'i memory'ye birden fazla tam kopya olarak alma; streaming/bounded işlem tercih et.
- Validator hatası actionable path + reason üretmeli; `getOrNull()` ile entry düşürmemeli.

İlk doğrulama: ilgili Gradle TestKit/unit test ve mevcut `validateLumisky*` görevleri. Görev henüz yoksa bunu açıkça belirt.
