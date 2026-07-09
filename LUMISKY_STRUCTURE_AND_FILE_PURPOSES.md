# Lumisky v5 Klasör ve Dosya Yapısı

Bu iskelet, Lumisky v5 production-hardened mimarisine göre hazırlanmıştır.

## Kullanım

Bu paket tam bitmiş çalışan uygulama değildir. Amaç, mimariyi projeye taşırken gerekli klasörleri, dosya isimlerini ve sorumluluk sınırlarını netleştirmektir.

## Önerilen Uygulama Sırası

1. `definition/` parser, migration, validator
2. `registry/` LayerFactory + LayerRegistry
3. `engine/` SceneCompiler + RuntimeScene
4. `layers/TextureLayer` ve `layers/ShaderLayer`
5. `preview/` app detail preview
6. `core/` live wallpaper bridge
7. Scheduler, cached FBO, VideoOesLayer, PAD, telemetry

## Klasör Ağacı

```text
├── app
│   ├── src
│   │   └── main
│   │       ├── assets
│   │       │   ├── shaders
│   │       │   │   ├── common
│   │       │   │   │   ├── fullscreen_quad.vert
│   │       │   │   │   └── gradient.frag
│   │       │   │   └── video
│   │       │   │       └── video_oes.frag
│   │       │   └── wallpapers
│   │       │       ├── index.json
│   │       │       └── starter_gradient.json
│   │       ├── java
│   │       │   └── com
│   │       │       
│   │       │           └── lumisky
│   │       │               ├── assets
│   │       │               │   ├── AssetCachePolicy.kt
│   │       │               │   ├── AssetPackResolver.kt
│   │       │               │   ├── AssetPackState.kt
│   │       │               │   ├── LumiskyAssetManager.kt
│   │       │               │   ├── ShaderSourceLoader.kt
│   │       │               │   ├── TextureFormatResolver.kt
│   │       │               │   ├── ThumbnailLoader.kt
│   │       │               │   └── WallpaperColorProvider.kt
│   │       │               ├── core
│   │       │               │   ├── EngineController.kt
│   │       │               │   ├── EngineEventQueue.kt
│   │       │               │   ├── LumiskyWallpaperEngine.kt
│   │       │               │   ├── LumiskyWallpaperService.kt
│   │       │               │   ├── WallpaperEventReceiver.kt
│   │       │               │   ├── WallpaperFrameClock.kt
│   │       │               │   └── WallpaperGlThread.kt
│   │       │               ├── data
│   │       │               │   ├── AssetDownloadRepository.kt
│   │       │               │   ├── EntitlementRepository.kt
│   │       │               │   ├── LocalWallpaperDataSource.kt
│   │       │               │   ├── SettingsRepository.kt
│   │       │               │   ├── WallpaperCatalogDataSource.kt
│   │       │               │   └── WallpaperRepository.kt
│   │       │               ├── definition
│   │       │               │   ├── AnimationDefinition.kt
│   │       │               │   ├── CatalogDefinition.kt
│   │       │               │   ├── DefinitionDefaults.kt
│   │       │               │   ├── DefinitionValidator.kt
│   │       │               │   ├── EventDefinition.kt
│   │       │               │   ├── LayerDefinition.kt
│   │       │               │   ├── PreviewDefinition.kt
│   │       │               │   ├── QualityDefinition.kt
│   │       │               │   ├── SchemaMigration.kt
│   │       │               │   ├── SchemaMigrator.kt
│   │       │               │   ├── ShaderDefinition.kt
│   │       │               │   ├── TextureSlotDefinition.kt
│   │       │               │   ├── WallpaperDefinition.kt
│   │       │               │   └── WallpaperDefinitionParser.kt
│   │       │               ├── device
│   │       │               │   ├── AmbientModeController.kt
│   │       │               │   ├── DisplayMetricsController.kt
│   │       │               │   ├── SensorDispatcher.kt
│   │       │               │   └── ThermalStateController.kt
│   │       │               ├── engine
│   │       │               │   ├── gl
│   │       │               │   │   ├── BitmapPool.kt
│   │       │               │   │   ├── EglManager.kt
│   │       │               │   │   ├── FramebufferPool.kt
│   │       │               │   │   ├── GlBuffer.kt
│   │       │               │   │   ├── GlError.kt
│   │       │               │   │   ├── GlFramebuffer.kt
│   │       │               │   │   ├── GlProgram.kt
│   │       │               │   │   ├── GlReleaseQueue.kt
│   │       │               │   │   ├── GlResource.kt
│   │       │               │   │   ├── GlResourceManager.kt
│   │       │               │   │   ├── GlStateController.kt
│   │       │               │   │   ├── GlTexture.kt
│   │       │               │   │   ├── MeshRegistry.kt
│   │       │               │   │   ├── ShaderBinaryCache.kt
│   │       │               │   │   ├── ShaderCompiler.kt
│   │       │               │   │   ├── ShaderProgramPool.kt
│   │       │               │   │   └── TexturePool.kt
│   │       │               │   ├── pipeline
│   │       │               │   │   ├── BlendMode.kt
│   │       │               │   │   ├── CachedLayerRenderer.kt
│   │       │               │   │   ├── FboResolutionPolicy.kt
│   │       │               │   │   ├── FinalCompositeRenderer.kt
│   │       │               │   │   ├── LayerComposer.kt
│   │       │               │   │   ├── RenderPass.kt
│   │       │               │   │   └── RenderTargetMode.kt
│   │       │               │   ├── AdaptiveQualityController.kt
│   │       │               │   ├── AtmosphereController.kt
│   │       │               │   ├── EventTriggerSystem.kt
│   │       │               │   ├── LumiskyRenderer.kt
│   │       │               │   ├── MutableRenderFrameState.kt
│   │       │               │   ├── ParallaxController.kt
│   │       │               │   ├── RenderContext.kt
│   │       │               │   ├── RuntimeScene.kt
│   │       │               │   ├── SceneCompiler.kt
│   │       │               │   ├── SceneInputSnapshot.kt
│   │       │               │   ├── SceneScheduler.kt
│   │       │               │   ├── SceneState.kt
│   │       │               │   └── ShaderWarmupController.kt
│   │       │               ├── layers
│   │       │               │   ├── AnimationLayer.kt
│   │       │               │   ├── BaseLayer.kt
│   │       │               │   ├── CloudLayer.kt
│   │       │               │   ├── FogLayer.kt
│   │       │               │   ├── ForegroundLayer.kt
│   │       │               │   ├── MoonLayer.kt
│   │       │               │   ├── RainLayer.kt
│   │       │               │   ├── RenderLayer.kt
│   │       │               │   ├── ShaderLayer.kt
│   │       │               │   ├── StarsLayer.kt
│   │       │               │   ├── SunLayer.kt
│   │       │               │   ├── TextureLayer.kt
│   │       │               │   └── VideoOesLayer.kt
│   │       │               ├── preview
│   │       │               │   ├── CardPreviewController.kt
│   │       │               │   ├── FullscreenPreviewController.kt
│   │       │               │   ├── PreviewMode.kt
│   │       │               │   ├── PreviewSurfaceController.kt
│   │       │               │   └── RuntimeProfile.kt
│   │       │               ├── registry
│   │       │               │   ├── EffectRegistry.kt
│   │       │               │   ├── LayerCreateResult.kt
│   │       │               │   ├── LayerFactory.kt
│   │       │               │   ├── LayerRegistry.kt
│   │       │               │   ├── SceneFactory.kt
│   │       │               │   └── ShaderRegistry.kt
│   │       │               ├── telemetry
│   │       │               │   ├── FeatureFlagRepository.kt
│   │       │               │   ├── RenderFallbackEvent.kt
│   │       │               │   └── RenderTelemetryLogger.kt
│   │       │               ├── ui
│   │       │               │   ├── catalog
│   │       │               │   │   ├── WallpaperCatalogScreen.kt
│   │       │               │   │   ├── WallpaperCatalogUiModel.kt
│   │       │               │   │   └── WallpaperCatalogViewModel.kt
│   │       │               │   ├── preview
│   │       │               │   │   ├── WallpaperPreviewScreen.kt
│   │       │               │   │   └── WallpaperPreviewViewModel.kt
│   │       │               │   └── settings
│   │       │               │       ├── SettingsScreen.kt
│   │       │               │       └── SettingsViewModel.kt
│   │       │               └── LumiskyApp.kt
│   │       ├── res
│   │       │   ├── values
│   │       │   │   ├── strings.xml
│   │       │   │   └── styles.xml
│   │       │   └── xml
│   │       │       └── lumisky_wallpaper.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── assetpacks
│   ├── city_pack
│   │   └── src
│   │       └── main
│   │           └── assets
│   │               └── README.md
│   ├── nature_pack
│   │   └── src
│   │       └── main
│   │           └── assets
│   │               └── README.md
│   ├── premium_pack
│   │   └── src
│   │       └── main
│   │           └── assets
│   │               └── README.md
│   └── space_pack
│       └── src
│           └── main
│               └── assets
│                   └── README.md
├── benchmark
│   └── README.md
├── tools
│   └── validation
│       └── README.md
├── build.gradle.kts
└── settings.gradle.kts
```

## Dosya Görevleri

| Dosya | Görev |
|---|---|
| `LumiskyApp.kt` | Uygulama sınıfı. Hilt, global telemetry ve app-level initialization için giriş noktası. |
| `assets/AssetCachePolicy.kt` | Disk/RAM cache limitleri, LRU ve eviction kuralları. |
| `assets/AssetPackResolver.kt` | Play Asset Delivery pack state/path çözümü ve hash/path validation. |
| `assets/AssetPackState.kt` | NotInstalled, Pending, Downloading, Installed, Failed gibi PAD state modeli. |
| `assets/LumiskyAssetManager.kt` | Definition, texture, shader, video, thumbnail asset path çözüm katmanı. |
| `assets/ShaderSourceLoader.kt` | Shader source metinlerini asset pack veya app assets içinden yükler. |
| `assets/TextureFormatResolver.kt` | ASTC/ETC2/WebP fallback kararlarını cihaz desteğine göre verir. |
| `assets/ThumbnailLoader.kt` | Catalog thumbnail decode, target size ve memory cache işlemleri. |
| `assets/WallpaperColorProvider.kt` | Material You renklerini metadata/thumbnail üzerinden cache’li üretir. |
| `core/EngineController.kt` | StateFlow/SharedFlow akışlarını engine command/event kuyruğuna bağlar. |
| `core/EngineEventQueue.kt` | Main/UI thread eventlerini GL thread’de güvenli işlemek için Concurrent queue. |
| `core/LumiskyWallpaperEngine.kt` | WallpaperService.Engine lifecycle olaylarını RuntimeSession ve GL thread’e çeviren bridge katmanı. |
| `core/LumiskyWallpaperService.kt` | Android canlı wallpaper servisidir. Sadece WallpaperService.Engine üretir; render detaylarını bilmez. |
| `core/WallpaperEventReceiver.kt` | Screen on/off, user present ve battery gibi sistem eventlerini yakalayıp EngineEventQueue’ya iletir. |
| `core/WallpaperFrameClock.kt` | VSync uyumlu frame clock. frameTimeNanos ile delta hesaplanmasını sağlar. |
| `core/WallpaperGlThread.kt` | Gerçek live wallpaper için custom GL thread, Looper, EGL context ve Choreographer lifecycle yönetimi. |
| `data/AssetDownloadRepository.kt` | AssetPackState, PAD download/retry/offline akışı ve UI state üretimi. |
| `data/EntitlementRepository.kt` | Google Play Billing/premium yetki doğrulama kaynağı. |
| `data/LocalWallpaperDataSource.kt` | Local app assets ve starter wallpaper definition erişimi. |
| `data/SettingsRepository.kt` | DataStore tabanlı seçili wallpaper, quality, parallax, last successful scene saklama. |
| `data/WallpaperCatalogDataSource.kt` | wallpaper_catalog.json metadata index okuma kaynağı. |
| `data/WallpaperRepository.kt` | Catalog, definition, entitlement ve install state’i birleştiren ana repository. |
| `definition/AnimationDefinition.kt` | UV_SCROLL, PULSE, PATH_FOLLOW gibi layer animasyon tanımı. |
| `definition/CatalogDefinition.kt` | wallpaper_catalog.json için hafif metadata modeli. |
| `definition/DefinitionDefaults.kt` | Eksik optional alanlar için default üretir. |
| `definition/DefinitionValidator.kt` | Layer type, shaderRef, texture path, capability matrix doğrulaması. |
| `definition/EventDefinition.kt` | Event trigger ve targetLayer/action tanımı. |
| `definition/LayerDefinition.kt` | Layer type, shaderRef, uniforms, framePolicy, parallax tanımı. |
| `definition/PreviewDefinition.kt` | thumbnail, cardMode, fullscreenMode preview davranışları. |
| `definition/QualityDefinition.kt` | QualityTier ve degradation policy tanımları. |
| `definition/SchemaMigration.kt` | schemaVersion migration arayüzü ve migration result modelleri. |
| `definition/SchemaMigrator.kt` | Raw JSON’u desteklenen son schemaVersion’a taşır. |
| `definition/ShaderDefinition.kt` | shaderRef veya custom shader path tanımı. |
| `definition/TextureSlotDefinition.kt` | Texture slot path, sampler uniform, filter/wrap, UV/parallax/opacity ayarları. |
| `definition/WallpaperDefinition.kt` | JSON wallpaper tanımının latest schema data modeli. |
| `definition/WallpaperDefinitionParser.kt` | Raw JSON’u WallpaperDefinition modeline parse eder. |
| `device/AmbientModeController.kt` | AOD/ambient/low-power durumda render loop ve burn-in policy kontrolü. |
| `device/DisplayMetricsController.kt` | Foldable/tablet/rotation surface değişimlerinde projection/parallax clamp hesaplar. |
| `device/SensorDispatcher.kt` | Parallax için tek merkezi sensor listener; throttling/smoothing/subscriber yönetimi. |
| `device/ThermalStateController.kt` | OS thermal listener ile kalite degradation sinyali verir. |
| `engine/AdaptiveQualityController.kt` | Battery saver, thermal, frame pacing ve cihaz profiline göre kalite kararı verir. |
| `engine/AtmosphereController.kt` | Gündoğumu/günbatımı, ışık, sis, yıldız görünürlüğü gibi ortak atmosfer state’ini üretir. |
| `engine/EventTriggerSystem.kt` | ON_USER_PRESENT, ON_SUNSET gibi eventleri layer aksiyonlarına dönüştürür. |
| `engine/LumiskyRenderer.kt` | Tek universal renderer. RuntimeScene, scheduler ve GL resource sistemini kullanarak frame çizer. |
| `engine/MutableRenderFrameState.kt` | Her frame yeniden allocate edilmeden güncellenen preallocated frame state. |
| `engine/ParallaxController.kt` | SensorDispatcher’dan gelen parallax inputunu smoothing ve clamp ile SceneState’e işler. |
| `engine/RenderContext.kt` | Hot-path için allocation yapmadan kullanılan teknik render state: viewport, matrices, frame time. |
| `engine/RuntimeScene.kt` | Runtime layer instance listesini, event dispatch’i ve release akışını yönetir. |
| `engine/SceneCompiler.kt` | WallpaperDefinition’ı validate/normalize edilmiş CompiledSceneDefinition’a çevirir. |
| `engine/SceneInputSnapshot.kt` | Main thread’den GL thread’e aktarılan immutable input snapshot modeli. |
| `engine/SceneScheduler.kt` | LayerFramePolicy’ye göre update, cache refresh ve render kararlarını verir. |
| `engine/SceneState.kt` | Sahnenin mantıksal durumu: zaman, atmosfer, parallax, battery, visibility, quality. |
| `engine/ShaderWarmupController.kt` | Shader compile/binary load işlerini throttled şekilde hazırlar. |
| `engine/gl/BitmapPool.kt` | WebP/PNG/JPG decode için inBitmap reuse havuzu. |
| `engine/gl/EglManager.kt` | EGL display/context/surface lifecycle yöneticisi. GLThread dışında kullanılmaz. |
| `engine/gl/FramebufferPool.kt` | FBO reuse, half-res/quarter-res policy ve release işlemleri. |
| `engine/gl/GlBuffer.kt` | VBO/IBO gibi buffer objelerinin wrapper’ı. |
| `engine/gl/GlError.kt` | GL hata tipleri ve telemetry’ye aktarılacak hata modelleri. |
| `engine/gl/GlFramebuffer.kt` | FBO ve bağlı texture/renderbuffer kaynaklarını temsil eder. |
| `engine/gl/GlProgram.kt` | Compiled OpenGL shader program wrapper. Uniform location cache ve use/delete işlemleri. |
| `engine/gl/GlReleaseQueue.kt` | GL kaynaklarının doğru thread/context üzerinde silinmesini garantiler. |
| `engine/gl/GlResource.kt` | Context-bound GL resource interface’i. |
| `engine/gl/GlResourceManager.kt` | Aktif scene için shader, texture, FBO, mesh kaynaklarını context scope içinde yönetir. |
| `engine/gl/GlStateController.kt` | Blend/depth/viewport/program state değişimlerini merkezileştirir. |
| `engine/gl/GlTexture.kt` | Texture ID wrapper. GL_TEXTURE_2D kaynaklarını context-bound temsil eder. |
| `engine/gl/MeshRegistry.kt` | Fullscreen quad ve ortak mesh/VBO kaynaklarını context scope içinde tutar. |
| `engine/gl/ShaderBinaryCache.kt` | Desteklenen cihazlarda glGetProgramBinary/glProgramBinary tabanlı disk cache katmanı. |
| `engine/gl/ShaderCompiler.kt` | Shader source compile/link, loglama, fallback shader ve validation görevleri. |
| `engine/gl/ShaderProgramPool.kt` | Aktif EGL context içinde lazy shader program cache. Singleton GL handle tutmaz. |
| `engine/gl/TexturePool.kt` | Aktif scene texture cache ve LRU eviction yönetimi. |
| `engine/pipeline/BlendMode.kt` | NONE, ALPHA, ADDITIVE, MULTIPLY, SCREEN blend davranışlarını tanımlar. |
| `engine/pipeline/CachedLayerRenderer.kt` | Low-FPS/FBO cached layer output’unu gerektiğinde refresh eder, her frame composite ettirir. |
| `engine/pipeline/FboResolutionPolicy.kt` | QualityTier ve layer maliyetine göre FBO çözünürlüğü seçer. |
| `engine/pipeline/FinalCompositeRenderer.kt` | Final sahneyi clear + compose + swap akışına hazırlar. |
| `engine/pipeline/LayerComposer.kt` | zIndex, RenderPass ve BlendMode’a göre layer çizim sırasını yönetir. |
| `engine/pipeline/RenderPass.kt` | BACKGROUND, OPAQUE, TRANSPARENT, POST_PROCESS, OVERLAY render geçişlerini tanımlar. |
| `engine/pipeline/RenderTargetMode.kt` | DIRECT, OFFSCREEN_FBO, CACHED_TEXTURE, POST_PROCESS hedeflerini tanımlar. |
| `layers/AnimationLayer.kt` | Unlock flash gibi event-based/loop animasyon layer’ı. |
| `layers/BaseLayer.kt` | RenderLayer için ortak default davranışları sağlayan soyut temel sınıf. |
| `layers/CloudLayer.kt` | UV scroll ve çoklu texture slot destekli bulut layer. |
| `layers/FogLayer.kt` | Düşük FPS cached FBO ile sis/haze layer. |
| `layers/ForegroundLayer.kt` | Statik veya parallax destekli ön plan image layer. |
| `layers/MoonLayer.kt` | Ay pozisyonu/fazı ve gece görünürlüğü için layer. |
| `layers/RainLayer.kt` | Particle/shader tabanlı yağmur layer. |
| `layers/RenderLayer.kt` | Tüm çizim bileşenlerinin ortak interface’i. |
| `layers/ShaderLayer.kt` | Fullscreen veya mesh tabanlı generic shader layer. |
| `layers/StarsLayer.kt` | Starfield shader/texture layer; düşük FPS twinkle destekler. |
| `layers/SunLayer.kt` | Güneş diski, glow ve zamana bağlı pozisyon için layer. |
| `layers/TextureLayer.kt` | Statik image/texture layer. Foreground/background için. |
| `layers/VideoOesLayer.kt` | SurfaceTexture + GL_TEXTURE_EXTERNAL_OES ile video layer pipeline. |
| `preview/CardPreviewController.kt` | Liste/kart preview’da thumbnail-only veya tek seçili low-cost preview kararı. |
| `preview/FullscreenPreviewController.kt` | Fullscreen preview runtime scene, simulated time ve quality policy yönetimi. |
| `preview/PreviewMode.kt` | THUMBNAIL, CARD_PREVIEW, FULLSCREEN_PREVIEW, REAL_WALLPAPER modları. |
| `preview/PreviewSurfaceController.kt` | App preview surface lifecycle ve tek aktif GL preview kuralı. |
| `preview/RuntimeProfile.kt` | Live wallpaper, system preview, app detail preview runtime politikaları. |
| `registry/EffectRegistry.kt` | Effect/layer variant kayıtları için stateless registry. |
| `registry/LayerCreateResult.kt` | Layer oluşturma sonucunu crash atmadan typed result olarak döndürür. |
| `registry/LayerFactory.kt` | CompiledLayerDefinition’dan RenderLayer instance üreten factory sözleşmesi. |
| `registry/LayerRegistry.kt` | Layer type string -> LayerFactory map. GL handle tutmaz. |
| `registry/SceneFactory.kt` | CompiledSceneDefinition ve LayerRegistry ile RuntimeScene üretir. |
| `registry/ShaderRegistry.kt` | shaderRef -> shader source/path/family bilgisi. GL program ID tutmaz. |
| `telemetry/FeatureFlagRepository.kt` | Riskli renderer/layer özellikleri için kill switch ve feature flag kaynağı. |
| `telemetry/RenderFallbackEvent.kt` | Telemetry’ye gönderilecek fallback event modelleri. |
| `telemetry/RenderTelemetryLogger.kt` | Fallback, shader fail, missing asset, GL init fail gibi non-fatal olayları rate-limited loglar. |
| `ui/catalog/WallpaperCatalogScreen.kt` | Compose katalog ekranı. Sadece thumbnail ve stable UI model kullanır. |
| `ui/catalog/WallpaperCatalogUiModel.kt` | Liste item için stable, hafif UI modeli. |
| `ui/catalog/WallpaperCatalogViewModel.kt` | Catalog UI state, premium/download kararları ve filtreleme akışları. |
| `ui/preview/WallpaperPreviewScreen.kt` | Detay/fullscreen preview ekranı ve PreviewSurfaceController entegrasyonu. |
| `ui/preview/WallpaperPreviewViewModel.kt` | Preview açma, asset hazırlık ve set wallpaper aksiyonlarını yönetir. |
| `ui/settings/SettingsScreen.kt` | Kalite, parallax, battery behavior gibi kullanıcı ayarları. |
| `ui/settings/SettingsViewModel.kt` | SettingsRepository ile UI state ve engine command bridge. |
| `app/src/main/assets/shaders/common/fullscreen_quad.vert` | Örnek asset/config dosyası. |
| `app/src/main/assets/shaders/common/gradient.frag` | Örnek asset/config dosyası. |
| `app/src/main/assets/shaders/video/video_oes.frag` | Örnek asset/config dosyası. |
| `app/src/main/assets/wallpapers/index.json` | Örnek asset/config dosyası. |
| `app/src/main/assets/wallpapers/starter_gradient.json` | Örnek asset/config dosyası. |
| `assetpacks/city_pack/src/main/assets/README.md` | Örnek asset/config dosyası. |
| `assetpacks/nature_pack/src/main/assets/README.md` | Örnek asset/config dosyası. |
| `assetpacks/premium_pack/src/main/assets/README.md` | Örnek asset/config dosyası. |
| `assetpacks/space_pack/src/main/assets/README.md` | Örnek asset/config dosyası. |
| `benchmark/README.md` | Örnek asset/config dosyası. |
| `tools/validation/README.md` | Örnek asset/config dosyası. |

## Kritik Mimari Kurallar

- Registry GL handle tutmaz; sadece factory/source map tutar.
- GL texture/program/FBO ID’leri sadece aktif EGL context içinde yaşar.
- Main thread eventleri doğrudan layer’a gitmez; EngineEventQueue üzerinden GL thread’de işlenir.
- JSON render loop içinde okunmaz veya parse edilmez.
- Low-FPS cached FBO layer son texture’ını her frame final composite içinde çizdirir.
- Ana katalogda GL preview yoktur; thumbnail-first UI kullanılır.
- Yeni wallpaper eklemek için Kotlin `when`/enum değil, JSON `WallpaperDefinition` kullanılır.
- Play Asset Delivery path’i kalıcı varsayılmaz; her session resolve/validate edilir.
