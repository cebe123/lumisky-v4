# V8 kısa referans — rendering ve içerik

Kaynak türleri: `LAYERED_IMAGE`, `HYBRID`, `VIDEO`, `PROCEDURAL`.

`HybridSceneRendererBackend` -> `CompiledLayerGraph` -> ordered passes/layers/animation/resource/fallback plan.

Production layer seti:
- TextureLayer
- ShaderEffectLayer
- ParticleLayer
- VideoOesLayer
- Color/GradientLayer
- LayerGroupNode

Pass sırası: BACKGROUND, WORLD, ATMOSPHERE, SUBJECT, FOREGROUND, POST_PROCESS, UI_DEBUG. İç sıra `zIndex`, sonra declaration order. Alpha layer reorder edilmez.

Hot path:
- JSON/map/string lookup yok.
- Asset I/O/decode/compile yok.
- Motor kaynaklı yeni collection/data-class yok.
- Update cadence ve draw cadence ayrı.

İleri özellikler (mesh warp, depth warp, bloom, general FBO, program binary) yalnız ölçüm sonrası.
