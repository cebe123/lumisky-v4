# Lumisky Final Mimari v5 — Production-Hardened Data-Driven Wallpaper Engine

**Proje:** Lumisky  
**Hedef:** 100+ canlı wallpaper, minimum pil/RAM/GPU tüketimi, zero-jank hot path, kolay yeni wallpaper ekleme, premium preview ve liste deneyimi  
**Ana karar:** Lean Data-Driven Wallpaper Engine + Context-bound GL Resource Management + Layer Scheduled Rendering + Production Hardening + Runtime Observability

---

## 0. Araştırma Sonucu Alınan Nihai Kararlar

Bu v5 sürümünde mimari; Android resmi dokümanları, Google/Android grafik örnekleri, OpenGL ES/SurfaceTexture pratikleri, Play Asset Delivery, Macrobenchmark/Baseline Profile, Android GPU Inspector ve texture compression dokümanları dikkate alınarak sadeleştirilmiştir.

### Kesin kararlar

1. **Gerçek canlı wallpaper:** `WallpaperService.Engine + SurfaceHolder + custom GLThread/EGL`.
2. **Uygulama içi preview:** `GLSurfaceView` veya kontrollü `TextureView` tabanlı preview kullanılabilir.
3. **Render frame pacing:** GL thread üzerinde `Choreographer` destekli frame clock. NDK tabanlı engine'e geçilirse Swappy opsiyonel olarak değerlendirilebilir.
4. **Yeni wallpaper ekleme:** Kotlin `when`, enum veya hardcoded sahne listesiyle değil; JSON `WallpaperDefinition` ile yapılır.
5. **Registry:** GL handle tutmaz. Sadece `LayerFactory`, `ShaderSource`, `EffectFactory` gibi stateless/factory kayıtları tutar.
6. **GL kaynakları:** Texture/program/FBO/buffer ID'leri sadece aktif EGL context/session içinde yaşar.
7. **Video:** Normal texture değildir. `VideoOesLayer + SurfaceTexture + GL_TEXTURE_EXTERNAL_OES + samplerExternalOES` kullanılır.
8. **Low-FPS layer:** Güncellenmediği frame'de kaybolmaz. Son cached FBO texture her frame final composite içinde çizilir.
9. **Back buffer preservation:** Mimari `EGL_SWAP_BEHAVIOR_PRESERVED` davranışına güvenmez.
10. **Catalog:** Ana liste/grid içinde canlı GL preview açılmaz; thumbnail-first UI kullanılır.
11. **Asset dağıtımı:** Büyük texture/video/shader paketleri Play Asset Delivery ile ayrılır.
12. **Texture stratejisi:** Thumbnail için WebP/AVIF; runtime GPU texture için ASTC/ETC2 destekli paketleme opsiyonel, cihaz desteğine göre fallback.
13. **Ölçüm:** Macrobenchmark, Baseline Profiles, AGI frame profiling ve GPU overdraw analizi kabul kriterlerine eklenir.

14. **Schema migration:** `schemaVersion` değişiklikleri migration + validation + default normalizer ile yönetilir.
15. **Fallback-first runtime:** Unknown layer, eksik asset, shader compile hatası, PAD hatası ve GL init hatası crash yerine fallback politikasına gider.
16. **Persistent state:** Seçili wallpaper, kalite tercihi, parallax ayarı, asset pack state, entitlement ve last successful scene DataStore/Room repository katmanında tutulur.
17. **Asset pack state machine:** `NotInstalled → Pending → Downloading → Transferring → Installed` ve failure/retry/offline akışları açıkça modellenir.
18. **CI validation:** JSON schema, shaderRef, layer type, uniform type, texture path, asset pack ve premium productId build/CI aşamasında doğrulanır.

19. **Shader binary cache:** OpenGL ES 3.0 program binary cache destekleniyorsa kullanılabilir; destek yoksa veya binary geçersizse lazy compile fallback çalışır.
20. **BitmapPool:** WebP/PNG/JPG decode için `inBitmap` tabanlı bitmap reuse havuzu kullanılır; GPU compressed texture akışında bitmap pool devre dışıdır.
21. **SensorDispatcher:** Parallax sensörleri tek merkezden yönetilir, aktif subscriber yoksa sensörler unregister edilir, veri throttle/smoothing sonrası engine'e aktarılır.
22. **MVVM ↔ Engine bridge:** Sürekli durumlar StateFlow, tek seferlik komutlar SharedFlow üzerinden EngineController'a gelir ve GL Event Queue'ya güvenli aktarılır.
23. **Telemetry:** Fallback-first runtime sessiz kalmaz; shader/asset/GL/scheduler hataları rate-limited non-fatal telemetry ile izlenir.
24. **WallpaperColors / Material You:** Android 12+ dinamik tema için renkler GL yüzeyinden readback yapılmadan metadata veya thumbnail üzerinden hesaplanır ve cache'lenir.
25. **Display metrics / foldable:** Surface boyutu değişimi sadece viewport resize değildir; foldable/tablet geçişlerinde parallax sınırları, UI scale ve projection/FOV yeniden hesaplanır.
26. **Ambient/AOD runtime:** Kilit ekranı/AOD benzeri düşük güç durumlarında canlı render loop durur; burn-in korumalı statik/low-luminance frame veya `glClear` politikası uygulanır.
27. **OS Thermal listener:** Android OS thermal sinyalleri `PowerManager.OnThermalStatusChangedListener` ile AdaptiveQualityController'a bağlanır; severe/critical durumda acil degradation yapılır.
28. **GLThread Looper:** Choreographer kullanılacak custom GL thread mutlaka kendi `Looper.prepare()` / `Looper.loop()` yaşam döngüsünü kurar.
29. **Video playback:** Video layer için Media3/ExoPlayer tercih edilir; `SurfaceTexture.updateTexImage()` yalnızca current GL context olan thread'de frame-flag/fence senkronizasyonu ile çağrılır.
30. **Warmup concurrency:** Shader warmup/render asset hazırlığı render hot path'i bloklamaz; compile/warmup kuyruğu sınırlı worker ile throttle edilir.

---

## 1. Mimari Özeti

```text
Lumisky v5
├── Catalog System
│   ├── lightweight metadata index
│   ├── lazy wallpaper definition loading
│   ├── thumbnail-first UI
│   └── asset pack status model
│
├── Runtime Session System
│   ├── LiveWallpaperSession
│   ├── SystemPreviewSession
│   ├── AppPreviewSession
│   └── ThumbnailCaptureSession
│
├── Render Engine
│   ├── SceneCompiler
│   ├── RuntimeScene
│   ├── RenderLayer components
│   ├── SceneScheduler
│   ├── LayerFramePolicy
│   ├── CachedLayerRenderer
│   ├── EventTriggerSystem
│   ├── AtmosphereController
│   ├── ParallaxController
│   └── AdaptiveQualityController
│
├── GL Resource System
│   ├── EglManager
│   ├── GlResourceManager
│   ├── ShaderProgramPool
│   ├── TexturePool
│   ├── FramebufferPool
│   ├── MeshRegistry
│   └── GlReleaseQueue
│
├── Asset System
│   ├── LumiskyAssetManager
│   ├── AssetPackResolver
│   ├── TextureFormatResolver
│   ├── ShaderSourceLoader
│   └── DefinitionLoader
│
└── UI System
    ├── Compose catalog
    ├── stable UI models
    ├── one active GL preview max
    └── performance benchmark coverage
```

---

## 2. Gradle Modül Kararı

### Yeni proje için önerilen yapı

Başlangıçta çok modüllü Clean Architecture yerine **tek `:app` modülü + package-by-feature** kullanılmalıdır.

Sebep:

- Build/Gradle sync hızlı kalır.
- Mimari daha kolay yönetilir.
- Engine hâlâ paket sınırlarıyla izole edilir.
- Erken aşamada over-engineering oluşmaz.

```text
LumiskyProject/
├── app/
│   └── src/main/java/com/adnan/lumisky/
│       ├── core/
│       ├── engine/
│       ├── layers/
│       ├── definition/
│       ├── registry/
│       ├── assets/
│       ├── data/
│       ├── preview/
│       └── ui/
├── assetpacks/
│   ├── nature_pack/
│   ├── city_pack/
│   ├── space_pack/
│   └── premium_pack/
└── benchmark/   # Macrobenchmark/Baseline Profile için ayrı test modülü
```

### Mevcut Lumisky kod tabanı için geçiş kararı

Mevcut Lumisky kod tabanı `:app`, `:core`, `:engine`, `:wallpaper` gibi modül sınırlarıyla çalışıyorsa migration sırasında bu sınırlar korunmalıdır. Tek `:app` modülü örneği yeni/sıfırdan proje içindir; mevcut projede modülleri tek modüle toplamak migration riskini ve diff boyutunu artırır.

Mevcut modül uyarlaması:

```text
:app
  - Compose katalog, ayarlar, detay/fullscreen preview ekranları
  - Thumbnail-first liste akışı
  - ViewModel ve UI state üretimi
  - Premium/download karar ekranları

:core
  - WallpaperDefinition, LayerDefinition, RuntimeProfile, QualityTier
  - Schema migration, validator, default normalizer
  - Repository arayüzleri ve ortak policy modelleri
  - WallpaperConfig/manifest -> WallpaperDefinition adapter sözleşmesi

:engine
  - SceneCompiler, RuntimeScene, RenderLayer API
  - PreviewGlRenderer ile ortak render çekirdeği entegrasyonu
  - GL resource manager, shader binary cache, scheduler, parallax, atmosphere, effect layers
  - Shader/texture/FBO lifecycle ve fallback layer davranışı

:wallpaper
  - WallpaperService.Engine bridge
  - WallpaperRenderController / GL thread / surface lifecycle
  - ServiceRenderPolicyResolver ile FPS, thermal, power-save policy uyumu
  - Live wallpaper session, last successful scene restore
```

Kural:

```text
Yeni mimari mevcut sınıf düzenini birebir miras almaz.
Ama mevcut modül sınırları, preview/live ayrımı ve manifest-driven katalog akışı migration boyunca korunur.
Modül birleştirme ancak eski render path tamamen kapandıktan ve benchmark sonuçları stabil olduktan sonra değerlendirilir.
```

### Mevcut kod tabanı uyum matrisi

Bu bölüm mevcut Lumisky kod tabanına göre migration kararlarını somutlaştırır. Amaç, v5 hedef mimarisini mevcut çalışan preview/live wallpaper ayrımını bozmadan aşamalı taşımaktır.

| Mevcut kod alanı | Durum | v5 karşılığı | Migration kararı |
|---|---|---|---|
| `settings.gradle.kts` içindeki `:app`, `:core`, `:engine`, `:wallpaper` ayrımı | Zaten doğru yönde | Modül sınırı | Korunur; tek modüle toplama yapılmaz. |
| `WallpaperManifestCatalogSource` + `wallpapers/index.json` | Manifest-driven katalog zemini var | `WallpaperDefinition` + `DefinitionValidator` | Yeni şema adapter ile bağlanır; katalog akışı bir anda değiştirilmez. |
| `WallpaperConfig` | Runtime policy, effects, textures, capabilities taşıyor | `CompiledSceneDefinition` / `RuntimeScene` | Geçiş döneminde adapter kaynak model olarak kullanılır. |
| `PreviewRendererSurfaceView -> PreviewGlRenderer -> SkyEngine -> PreviewSkyProgram` | App preview için çalışan hat var | App preview renderer bridge | İlk fazda korunur; yeni scene compiler çıktılarını besleyecek adapter eklenir. |
| `SkyWallpaperService -> WallpaperRenderController -> WallpaperRenderEngine -> WallpaperEglSession` | Live wallpaper için ayrı thread/surface/policy hattı var | `WallpaperGlThread` + `EglManager` + `SceneScheduler` | Controller davranışı korunarak iç render implementation kademeli değiştirilir. |
| `ServiceRenderPolicyResolver`, `PerformanceMode`, thermal/power-save kararları | Zaten production değeri yüksek | Adaptive degradation policy | Yeni layer policy sistemi bu kararları yeniden yazmaz, içine plug-in olur. |
| `RenderAssetCache`, `SnapshotPreviewAssetLoader`, texture byte LRU cache | Asset cache var ama runtime decode reuse sınırlı | `LumiskyAssetManager` + `BitmapPool` + texture policy | LRU cache korunur; `inBitmap` pool ve GPU format ayrımı eklenir. |
| `PreviewSkyProgram` shader compile/link ve fallback texture davranışı | Çalışıyor, CrashDiagnostics var | `ShaderCompiler` + `ShaderProgramPool` + binary cache | Program binary cache opsiyonel katman olarak eklenir; compile fallback zorunlu kalır. |
| `TiltParallaxTracker` | Preview ve live wallpaper ayrı listener açabiliyor | `SensorDispatcher` | Tracker mantığı dispatcher içine taşınır; subscriber modeliyle tek sensor kaynağı kullanılır. |
| `HomeViewModel` mutable Compose state + repository listener | UI çalışıyor ama engine event sınırı formal değil | `StateFlow` / `SharedFlow` engine bridge | Mevcut state korunur; engine-facing state ayrı flow facade ile çıkarılır. |
| `ErrorReporter` / `CrashDiagnostics` / Crashlytics | Genel crash/non-fatal altyapı var | `RenderTelemetryLogger` | Render fallback eventleri rate-limited telemetry katmanına bağlanır. |

Geçişte eksik kabul edilen v5 bileşenleri:

```text
SceneCompiler
RenderLayer API
LayerRegistry + LayerCreateResult
DefinitionValidator + schema migration chain
ShaderBinaryCache
BitmapPool
SensorDispatcher
StateFlow/SharedFlow EngineController bridge
RenderTelemetryLogger
AssetPack state machine
Feature flag / remote kill switch facade
```

Kural:

```text
Mevcut kodda çalışan lifecycle, scheduler, catalog ve fallback davranışları migration sırasında korunur.
Yeni v5 bileşenleri önce adapter/facade olarak eklenir.
Eski render path ancak aynı davranış test, benchmark ve cihaz doğrulamasıyla sağlandıktan sonra kaldırılır.
```

### Modüllere ne zaman ayrılmalı?

Aşağıdaki şartlardan en az ikisi oluşmadan `domain`, `data`, `engine`, `wallpaper`, `preview` gibi ayrı Gradle modüllerine geçilmemelidir:

- Engine kodu 20.000+ satıra yaklaştı.
- Birden fazla geliştirici aynı anda farklı katmanlarda çalışıyor.
- Benchmark/CI süreleri paket ayrımı gerektiriyor.
- Engine bağımsız test/reuse ihtiyacı oluştu.
- Asset pack ve render engine ayrı release cycle istiyor.

---

## 3. Önerilen Paket Yapısı

```text
com.adnan.lumisky/
│
├── core/
│   ├── LumiskyWallpaperService.kt
│   ├── LumiskyWallpaperEngine.kt
│   ├── WallpaperGlThread.kt
│   ├── WallpaperFrameClock.kt
│   ├── WallpaperEventReceiver.kt
│   └── EngineEventQueue.kt
│
├── engine/
│   ├── LumiskyRenderer.kt
│   ├── RenderContext.kt
│   ├── MutableRenderFrameState.kt
│   ├── SceneState.kt
│   ├── SceneInputSnapshot.kt
│   ├── SceneCompiler.kt
│   ├── RuntimeScene.kt
│   ├── SceneScheduler.kt
│   ├── EventTriggerSystem.kt
│   ├── AtmosphereController.kt
│   ├── ParallaxController.kt
│   └── AdaptiveQualityController.kt
│
├── engine/gl/
│   ├── EglManager.kt
│   ├── GlResource.kt
│   ├── GlResourceManager.kt
│   ├── GlProgram.kt
│   ├── GlTexture.kt
│   ├── GlFramebuffer.kt
│   ├── GlBuffer.kt
│   ├── ShaderCompiler.kt
│   ├── ShaderProgramPool.kt
│   ├── TexturePool.kt
│   ├── FramebufferPool.kt
│   ├── MeshRegistry.kt
│   ├── GlStateController.kt
│   ├── GlReleaseQueue.kt
│   └── GlError.kt
│
├── engine/pipeline/
│   ├── LayerComposer.kt
│   ├── CachedLayerRenderer.kt
│   ├── FinalCompositeRenderer.kt
│   ├── RenderPass.kt
│   ├── BlendMode.kt
│   ├── RenderTargetMode.kt
│   └── FboResolutionPolicy.kt
│
├── layers/
│   ├── RenderLayer.kt
│   ├── BaseLayer.kt
│   ├── TextureLayer.kt
│   ├── ShaderLayer.kt
│   ├── VideoOesLayer.kt
│   ├── AnimationLayer.kt
│   ├── SunLayer.kt
│   ├── MoonLayer.kt
│   ├── StarsLayer.kt
│   ├── CloudLayer.kt
│   ├── RainLayer.kt
│   ├── FogLayer.kt
│   └── ForegroundLayer.kt
│
├── definition/
│   ├── WallpaperDefinition.kt
│   ├── LayerDefinition.kt
│   ├── TextureSlotDefinition.kt
│   ├── ShaderDefinition.kt
│   ├── AnimationDefinition.kt
│   ├── EventDefinition.kt
│   ├── QualityDefinition.kt
│   ├── PreviewDefinition.kt
│   ├── CatalogDefinition.kt
│   ├── WallpaperDefinitionParser.kt
│   └── DefinitionValidator.kt
│
├── registry/
│   ├── LayerFactory.kt
│   ├── LayerRegistry.kt
│   ├── ShaderRegistry.kt
│   ├── EffectRegistry.kt
│   └── SceneFactory.kt
│
├── assets/
│   ├── LumiskyAssetManager.kt
│   ├── AssetPackResolver.kt
│   ├── TextureFormatResolver.kt
│   ├── ShaderSourceLoader.kt
│   ├── WallpaperColorProvider.kt
│   ├── ThumbnailLoader.kt
│   └── AssetCachePolicy.kt
│
├── device/
│   ├── DisplayMetricsController.kt
│   ├── AmbientModeController.kt
│   └── ThermalStateController.kt
│
├── data/
│   ├── WallpaperRepository.kt
│   ├── WallpaperCatalogDataSource.kt
│   ├── LocalWallpaperDataSource.kt
│   ├── SettingsRepository.kt
│   └── AssetDownloadRepository.kt
│
├── preview/
│   ├── PreviewMode.kt
│   ├── RuntimeProfile.kt
│   ├── PreviewSurfaceController.kt
│   ├── CardPreviewController.kt
│   └── FullscreenPreviewController.kt
│
└── ui/
    ├── catalog/
    ├── preview/
    ├── settings/
    └── components/
```

---

## 4. Wallpaper Runtime Ayrımı

### 4.1 Gerçek Canlı Wallpaper

Gerçek wallpaper için `WallpaperService.Engine` ana giriş noktasıdır.

```text
WallpaperService.Engine
    ↓ SurfaceHolder
LumiskyWallpaperEngine
    ↓
WallpaperGlThread + EglManager
    ↓
LumiskyRenderer
    ↓
RuntimeScene
```

Kurallar:

- `WallpaperService.Engine` sadece lifecycle bridge olmalıdır.
- GL resource oluşturma/silme sadece GL thread üzerinde yapılmalıdır.
- `onVisibilityChanged(false)` geldiğinde render loop, sensörler ve video player durdurulmalıdır.
- `onSurfaceDestroyed()` geldiğinde scene ve EGL kaynakları güvenli şekilde release edilmelidir.

### 4.2 App Preview

App içi preview için `GLSurfaceView` kullanılabilir.

```text
Compose/Android View
    ↓
GLSurfaceView
    ↓ queueEvent { ... }
LumiskyRenderer
```

Kurallar:

- Liste/grid içinde GL preview yok.
- Detay ekranında tek aktif GL preview var.
- Fullscreen preview gerçek engine çekirdeğini kullanır ama `RuntimeProfile` farklıdır.

---

## 5. Frame Clock ve Choreographer

Render loop, `System.currentTimeMillis()` ile sürülmemelidir. Frame zamanlaması için `Choreographer.FrameCallback` kullanılmalıdır.

```kotlin
class WallpaperFrameClock(
    private val onFrame: (Long) -> Unit
) : Choreographer.FrameCallback {

    private var running = false

    fun start() {
        if (running) return
        running = true
        Choreographer.getInstance().postFrameCallback(this)
    }

    fun stop() {
        running = false
        Choreographer.getInstance().removeFrameCallback(this)
    }

    override fun doFrame(frameTimeNanos: Long) {
        if (!running) return
        onFrame(frameTimeNanos)
        Choreographer.getInstance().postFrameCallback(this)
    }
}
```

### GLThread notu

Custom GL thread üzerinde Choreographer kullanılacaksa thread'in `Looper` sahibi olması gerekir.

```text
HandlerThread / custom Thread
    ↓ Looper.prepare()
    ↓ EglManager.createContext()
    ↓ Choreographer.getInstance()
    ↓ frame callbacks
    ↓ Looper.loop()
```

Kesin kural:

```text
Choreographer.getInstance():
    yalnızca Looper.prepare() çağrılmış GL thread içinde alınır

Frame callback:
    sadece visible/surfaceAttached/renderNeeded durumunda post edilir

Pause/destroy:
    removeFrameCallback
    queue GL release
    Looper.quitSafely
```

Örnek iskelet:

```kotlin
class WallpaperGlThread : Thread("LumiskyGlThread") {
    private lateinit var handler: Handler
    private lateinit var choreographer: Choreographer

    override fun run() {
        Looper.prepare()
        handler = Handler(Looper.myLooper()!!)
        eglManager.createContext()
        choreographer = Choreographer.getInstance()
        Looper.loop()
    }
}
```

`HandlerThread` kullanılacaksa da aynı kural geçerlidir: `Choreographer.getInstance()` render thread üzerinde, EGL/context kurulumu ile aynı lifecycle içinde alınır. Main thread Choreographer referansı GL thread render loop'u için kullanılmaz.

### Swappy kararı

Android Frame Pacing Library / Swappy, OpenGL/Vulkan oyunlarında doğru frame pacing için tasarlanmıştır. Ancak Kotlin/Java ağırlıklı wallpaper engine v1 için ek NDK karmaşıklığı oluşturur.

Karar:

```text
v1/v2: Choreographer tabanlı frame clock
İleride NDK engine ihtiyacı doğarsa: Swappy araştırılır/entegre edilir
```

---

## 6. Thread-Safety Modeli

### Yanlış model

```text
Main Thread event → doğrudan layer değiştirir → Race condition
```

### Doğru model

```text
Main Thread
    ↓
EngineEventQueue
    ↓
GLThread frame başı drain
    ↓
EventTriggerSystem
    ↓
SceneState snapshot update
    ↓
Layer.onEvent()
```

```kotlin
class EngineEventQueue {
    private val queue = ConcurrentLinkedQueue<WallpaperEvent>()

    fun offer(event: WallpaperEvent) {
        queue.offer(event)
    }

    fun drainTo(target: MutableList<WallpaperEvent>) {
        while (true) {
            val event = queue.poll() ?: break
            target.add(event)
        }
    }
}
```

### SceneState kuralı

`SceneState` Main Thread ve GLThread arasında paylaşılan mutable object olmamalıdır.

Doğru yapı:

```text
Main Thread:
    SceneInputSnapshot üretir
    EventQueue'ya gönderir

GLThread:
    Snapshot'ı frame başında alır
    Kendi SceneState'ini günceller
    Layerlar yalnızca bu GLThread SceneState'ini okur
```

---

## 7. JSON → Runtime Scene Derleme Akışı

Render loop içinde JSON okunmaz, parse edilmez, string map aranmaz.

```text
Wallpaper JSON
    ↓ load-time parse
WallpaperDefinition
    ↓ validation
CompiledSceneDefinition
    ↓ layer factory
RuntimeScene
    ↓ GL resource init
Render loop
```

### Neden gerekli?

- Disk I/O render thread'e girmez.
- JSON parse frame içinde olmaz.
- Uniform map lookup frame içinde olmaz.
- Allocation/GC riski azalır.
- Layerlar typed primitive parametrelerle çalışır.

### Derlenmiş layer parametresi örneği

```kotlin
data class SunParams(
    val size: Float,
    val glowIntensity: Float,
    val colorR: Float,
    val colorG: Float,
    val colorB: Float
)
```

Runtime layer:

```kotlin
class SunLayer(
    private val params: SunParams
) : BaseLayer() {
    private var uSize = -1
    private var uGlow = -1

    override fun onCreateGl(gl: GlResourceManager) {
        val program = gl.program("common.sun.disc.v1")
        uSize = program.uniformLocation("u_Size")
        uGlow = program.uniformLocation("u_Glow")
    }

    override fun render(frame: MutableRenderFrameState) {
        // JSON map lookup yok; sadece primitive parametre ve cached uniform handle.
    }
}
```

---

## 8. Registry ve Hilt Kullanımı

### Yanlış

```kotlin
object ShaderRegistry {
    val programIds = mutableMapOf<String, Int>()
}
```

### Doğru

Registry yalnızca factory/source bilgisi tutar.

```kotlin
interface LayerFactory {
    fun create(definition: CompiledLayerDefinition): RenderLayer
}

class LayerRegistry(
    private val factories: Map<String, Provider<LayerFactory>>
) {
    fun create(definition: CompiledLayerDefinition): LayerCreateResult {
        val factory = factories[definition.type]?.get()
            ?: return LayerCreateResult.UnknownType(
                layerId = definition.id,
                type = definition.type,
                required = definition.required
            )

        return runCatching {
            LayerCreateResult.Created(factory.create(definition))
        }.getOrElse { error ->
            LayerCreateResult.CreateFailed(
                layerId = definition.id,
                type = definition.type,
                required = definition.required,
                cause = error
            )
        }
    }
}

sealed interface LayerCreateResult {
    data class Created(val layer: RenderLayer) : LayerCreateResult
    data class UnknownType(
        val layerId: String,
        val type: String,
        val required: Boolean
    ) : LayerCreateResult
    data class CreateFailed(
        val layerId: String,
        val type: String,
        val required: Boolean,
        val cause: Throwable
    ) : LayerCreateResult
}
```

`SceneCompiler`, `UnknownType` veya `CreateFailed` sonucunu fallback policy ile yorumlar. Required layer başarısızsa scene fallback'e gider; optional layer başarısızsa layer skip edilir ve telemetry/log kaydı üretilir. Registry crash üretmez, tanımlı sonuç döndürür.

Hilt/Dagger görevi:

```text
- LayerFactory map sağlar
- ShaderSourceLoader sağlar
- Repository sağlar
- AssetPackResolver sağlar

Sağlamaz:
- Aktif GL texture ID
- Aktif shader program ID
- Aktif FBO ID
- Wallpaper scene instance singleton
```

---

## 9. RenderLayer Interface

```kotlin
interface RenderLayer {
    val id: String
    val zIndex: Int
    val renderPass: RenderPass
    val blendMode: BlendMode
    val renderTargetMode: RenderTargetMode
    val framePolicy: LayerFramePolicy
    val parallaxDepth: Float

    fun onCreateGl(gl: GlResourceManager, context: RenderContext)
    fun onSurfaceChanged(context: RenderContext, width: Int, height: Int)
    fun onEvent(event: WallpaperEvent)
    fun update(frame: MutableRenderFrameState)
    fun render(frame: MutableRenderFrameState)
    fun onQualityChanged(profile: QualityProfile)
    fun onDestroyGl(gl: GlResourceManager)
}
```

### Interface ayrıştırma

Her layer her input'u implement etmek zorunda kalmamalı.

```kotlin
interface ParallaxReactiveLayer {
    fun onParallaxChanged(offsetX: Float, offsetY: Float)
}

interface TimeReactiveLayer {
    fun onMinuteChanged(timeState: TimeState)
}

interface TouchReactiveLayer {
    fun onTouch(input: TouchInput)
}
```

---

## 10. Render Pass, Blend ve Target Sistemi

```kotlin
enum class RenderPass {
    BACKGROUND,
    OPAQUE,
    ALPHA_TESTED,
    TRANSPARENT,
    POST_PROCESS,
    OVERLAY
}

enum class BlendMode {
    NONE,
    ALPHA,
    ADDITIVE,
    MULTIPLY,
    SCREEN
}

enum class RenderTargetMode {
    DIRECT,
    OFFSCREEN_FBO,
    CACHED_TEXTURE,
    POST_PROCESS
}
```

Kural:

```text
Default: DIRECT
FBO: sadece cache, blur, bloom, distortion, multi-pass gerekiyorsa
```

### Draw order

```text
1. zIndex'e göre sırala
2. renderPass'e göre grupla
3. blendMode değişimlerini minimuma indir
4. final composite aşamasında cached layer texture'larını çiz
```

---

## 11. LayerFramePolicy ve SceneScheduler

```kotlin
data class LayerFramePolicy(
    val mode: LayerFrameMode,
    val fps: Int? = null,
    val cacheMode: LayerCacheMode = LayerCacheMode.NONE,
    val updateWhenInvisible: Boolean = false,
    val degradeInBatterySaver: Boolean = true,
    val batterySaverFps: Int? = null,
    val idleFps: Int? = null
)

enum class LayerFrameMode {
    STATIC,
    ON_DEMAND,
    MINUTE_TICK,
    ONE_FPS,
    FIXED_FPS,
    MATCH_SCENE,
    CONTINUOUS,
    VIDEO_SYNC,
    EVENT_BASED
}

enum class LayerCacheMode {
    NONE,
    CPU_STATE_ONLY,
    FBO_CACHE,
    STATIC_TEXTURE
}
```

### Scheduler akışı

```text
onFrame(frameTimeNanos):
    renderContext.update(frameTimeNanos)
    eventQueue.drain()
    sceneState.update()

    for layer in layers:
        if scheduler.shouldUpdate(layer):
            layer.update(frame)

        if scheduler.shouldRefreshCache(layer):
            cachedRenderer.refresh(layer)

    finalComposite.clear()

    for layer in layers:
        if layer.cacheMode == FBO_CACHE:
            cachedRenderer.compositeLastTexture(layer)
        else:
            layer.render(frame)

    eglSwapBuffers()
```

---

## 12. Cached FBO ve Low-FPS Layer Davranışı

Low-FPS layer her frame yeniden render edilmez. Ancak son çıktısı her frame composite edilir.

```text
FogLayer: 10 FPS refresh
Scene: 30/60 FPS composite

Frame 1: fog FBO refresh + composite
Frame 2: fog refresh yok + eski FBO texture composite
Frame 3: fog refresh yok + eski FBO texture composite
```

### Kural

```text
Back buffer preservation'a güvenme.
Her frame final sahneyi clear + compose et.
Cached layer output'u texture olarak tekrar çiz.
```

### FBO çözünürlük politikası

```text
HIGH/ULTRA: full-res veya 0.75x
BALANCED: half-res
LOW/Battery Saver: half-res veya quarter-res
```

Sis, bloom, blur, haze gibi fullscreen efektler çoğu zaman half-res çalışmalıdır.

---

## 13. VideoOesLayer

Video layer normal `TextureLayer` değildir.

```text
Media3/ExoPlayer or MediaPlayer
    ↓ Surface
SurfaceTexture
    ↓ GL_TEXTURE_EXTERNAL_OES
VideoOesLayer
    ↓ samplerExternalOES shader
FinalComposite
```

Kurallar:

1. Yeni video entegrasyonunda tercih edilen player AndroidX Media3/ExoPlayer'dır; `MediaPlayer` yalnızca basit/fallback kullanım için kabul edilir.
2. `SurfaceTexture.updateTexImage()` yalnızca GL context current olan GL thread üzerinde çağrılır.
3. `samplerExternalOES` kullanılır.
4. Texture target `GL_TEXTURE_EXTERNAL_OES` olur; `GL_TEXTURE_2D` gibi bind edilmez.
5. `getTransformMatrix()` sonucu video shader'a aktarılır.
6. Wallpaper görünmez olduğunda video pause edilir.
7. `SurfaceTexture.release()` ve player release lifecycle içinde yapılır.
8. Unlock/event video tek seferlik oynatılabilir; bitince `FREEZE_LAST_FRAME` veya `DISABLED` state'e geçer.

```kotlin
class VideoOesLayer : BaseLayer() {
    private var oesTexId = 0
    private lateinit var surfaceTexture: SurfaceTexture
    private val transformMatrix = FloatArray(16)
    private val frameAvailable = AtomicBoolean(false)

    fun attachOnGlThread() {
        surfaceTexture.setOnFrameAvailableListener {
            frameAvailable.set(true)
            engineEventQueue.offer(WallpaperEvent.VideoFrameAvailable(layerId))
        }
    }

    override fun update(frame: MutableRenderFrameState) {
        if (frameAvailable.getAndSet(false)) {
            surfaceTexture.updateTexImage()
            surfaceTexture.getTransformMatrix(transformMatrix)
        }
    }
}
```

Senkronizasyon kuralı:

```text
Decoder/player thread:
    frameAvailable flag set eder
    GL thread'e lightweight event yollar

GL thread:
    current EGL context altında updateTexImage çağırır
    transform matrix'i aynı frame state'e yazar
    gerekirse fence/sync primitive ile texture reuse yarışını engeller
```

---

## 14. Asset Delivery ve Texture Format Stratejisi

### Play Asset Delivery

```text
Base APK:
- Core engine
- UI
- 5-10 starter wallpaper
- catalog index
- thumbnails

Fast-follow pack:
- popüler wallpaperlar
- orta boy texture/video paketleri

On-demand pack:
- premium/büyük video wallpaperlar
- yüksek çözünürlüklü texture setleri
```

### Asset path kuralı

Fast-follow/on-demand asset pack dosya konumları kalıcı varsayılmamalıdır. Her session başında `AssetPackResolver` path'i tekrar çözmelidir.

### Asset pack doğrulama kuralı

Her load öncesi asset pack erişimi tekrar doğrulanır. Eski absolute path cache'i güvenilir kaynak sayılmaz.

```text
AssetPackResolver.resolve(assetRef):
    AssetPackManager state kontrolü yap
    pack installed/available değilse placeholder/fallback döndür
    pack location path'ini o anda çöz
    pack manifest hash / asset hash doğrula
    dizin okunabilirlik ve beklenen prefix kontrolü yap
    sadece doğrulanmış path'i LumiskyAssetManager'a ver
```

Kural:

```text
PAD path yoksa       -> placeholder artwork
Hash uyuşmazsa       -> pack corrupt telemetry + fallback
Permission/read fail -> fallback + retry policy
Path traversal       -> hard reject, render'a gönderme
```

### Texture format kararı

```text
Thumbnail/UI:
- WebP veya AVIF

Runtime GL texture:
- Küçük/orta asset: WebP decode + glTexImage2D
- Büyük GPU texture: ASTC destekliyse ASTC
- ASTC yoksa ETC2 veya WebP fallback
```

### OpenGL ES seviyesi

```text
Önerilen hedef: OpenGL ES 3.0
Neden: ETC2 texture compression desteği ES 3.0 tarafında daha sağlamdır.
Fallback: ES 2.0 basit renderer + WebP decoded texture
```

---

## 15. Catalog ve UI Performans Kuralları

Ana katalogda yalnızca hafif metadata ve thumbnail kullanılmalıdır.

```json
{
  "id": "ocean_sunset_clouds",
  "name": "Ocean Sunset Clouds",
  "category": "Nature",
  "thumbnail": "thumbnails/ocean_sunset_clouds.webp",
  "definitionPath": "wallpapers/ocean_sunset_clouds.json",
  "assetPack": "nature_pack",
  "isPremium": false,
  "estimatedCost": {
    "gpu": "medium",
    "battery": "low",
    "memory": "medium"
  }
}
```

### Yasaklar

```text
Liste item içinde GLSurfaceView açmak          -> Yasak
Liste item içinde shader/video çalıştırmak    -> Yasak
Her item için full JSON parse etmek            -> Yasak
Her item için full texture decode etmek        -> Yasak
Her recomposition'da yeni UI model üretmek     -> Yasak
```

### Doğru akış

```text
App startup:
    catalog metadata + thumbnail manifest

Catalog scroll:
    thumbnail-only

Detail screen:
    selected wallpaper definition lazy-load

Fullscreen preview:
    single RuntimeScene

Set wallpaper:
    live RuntimeProfile + actual resources
```

---

## 16. WallpaperDefinition Şeması v5

```json
{
  "schemaVersion": 5,
  "id": "cyberpunk_city_01",
  "name": "Cyberpunk City",
  "category": "City",
  "assetPack": "city_pack",
  "colorHints": {
    "primary": "#0A1024",
    "secondary": "#D96B45",
    "tertiary": "#6FC3FF",
    "supportsDarkText": false,
    "supportsDarkTheme": true,
    "source": "metadata"
  },
  "capabilities": {
    "parallax": true,
    "touch": false,
    "locationAware": false,
    "supportsPreviewTimeSimulation": true
  },
  "displayPolicy": {
    "foldableAware": true,
    "baseAspectRatio": 0.5625,
    "fovPhone": 45.0,
    "fovExpanded": 38.0,
    "maxOffsetXPhone": 0.035,
    "maxOffsetXExpanded": 0.022,
    "maxOffsetY": 0.018
  },
  "ambientPolicy": {
    "enabled": true,
    "mode": "BLACK_CLEAR_OR_LOW_LUMINANCE_FRAME",
    "burnInProtectionPx": 3,
    "refreshSeconds": 60
  },
  "renderScheduler": {
    "mode": "LAYER_SCHEDULED",
    "sceneMaxFps": 30,
    "batterySaverSceneMaxFps": 15,
    "idleSceneMaxFps": 8
  },
  "qualityPolicy": {
    "defaultTier": "BALANCED",
    "allowAdaptiveQuality": true,
    "maxTextureSizeLow": 1024,
    "maxTextureSizeBalanced": 2048,
    "maxTextureSizeHigh": 4096
  },
  "parallax": {
    "enabled": true,
    "maxOffsetX": 0.035,
    "maxOffsetY": 0.018,
    "smoothing": 0.12
  },
  "layers": [
    {
      "id": "background_video",
      "type": "VideoOesLayer",
      "enabled": true,
      "zIndex": 0,
      "renderPass": "BACKGROUND",
      "blendMode": "NONE",
      "renderTarget": "DIRECT",
      "source": "videos/city_loop.mp4",
      "framePolicy": {
        "mode": "VIDEO_SYNC",
        "cacheMode": "NONE"
      },
      "parallax": {
        "factorX": 0.0,
        "factorY": 0.0,
        "depth": 0.0
      }
    },
    {
      "id": "rain",
      "type": "RainLayer",
      "enabled": true,
      "zIndex": 40,
      "renderPass": "TRANSPARENT",
      "blendMode": "ALPHA",
      "renderTarget": "DIRECT",
      "shaderRef": "common.rain.v1",
      "uniforms": {
        "u_Density": { "type": "float", "value": 0.8 },
        "u_Speed": { "type": "float", "value": 1.2 }
      },
      "framePolicy": {
        "mode": "FIXED_FPS",
        "fps": 30,
        "cacheMode": "NONE",
        "degradeInBatterySaver": true,
        "batterySaverFps": 15,
        "idleFps": 8
      },
      "parallax": {
        "factorX": 0.12,
        "factorY": 0.04,
        "depth": 0.35
      }
    },
    {
      "id": "unlock_flash",
      "type": "AnimationLayer",
      "enabled": true,
      "zIndex": 80,
      "renderPass": "OVERLAY",
      "blendMode": "ADDITIVE",
      "renderTarget": "DIRECT",
      "framePolicy": {
        "mode": "EVENT_BASED",
        "cacheMode": "NONE"
      },
      "defaultState": "IDLE"
    }
  ],
  "events": [
    {
      "trigger": "ON_USER_PRESENT",
      "action": "PLAY_ANIMATION",
      "targetLayer": "unlock_flash",
      "then": "DISABLED"
    }
  ],
  "preview": {
    "thumbnail": "thumbnails/cyberpunk_city_01.webp",
    "cardMode": "THUMBNAIL",
    "fullscreenMode": "FAST_TIME_SIMULATION"
  }
}
```

---

## 17. Shader Override ve Yeni Efekt Stratejisi

### Mevcut davranışı değiştirmek

Uniform override kullanılır.

```json
"uniforms": {
  "u_StarCount": { "type": "int", "value": 45 },
  "u_TwinkleIntensity": { "type": "float", "value": 0.16 }
}
```

### Yeni görsel davranış eklemek

Ortak shader şişirilmez. Yeni layer eklenir.

```text
Güneş diski       -> SunDiscLayer
Güneş halesi      -> SunHaloLayer
Lens flare        -> LensFlareLayer
Deniz yansıması   -> SunReflectionLayer
```

Kural:

```text
Mevcut davranışı değiştirmek  -> uniform override
Yeni efekt eklemek             -> bağımsız RenderLayer
Zorunlu shader ayrımı           -> kontrollü shader variant
```

---

## 18. Parallax Sistemi

Parallax üç seviyede çözülür:

1. Global wallpaper parallax
2. Layer-level parallax
3. TextureSlot-level parallax

```json
{
  "uniform": "u_CloudTex0",
  "path": "textures/clouds/cloud_soft_01.webp",
  "filter": "LINEAR",
  "wrapS": "REPEAT",
  "wrapT": "CLAMP_TO_EDGE",
  "parallaxFactor": [0.08, 0.02],
  "uvScale": [1.25, 1.0],
  "opacity": 0.58
}
```

Sensor input Main Thread'den gelir ama GLThread'e queue/snapshot olarak aktarılır. Layerlar sensör callback'ini doğrudan dinlemez.

---

## 19. Quality Tier ve Adaptive Degradation

```kotlin
enum class QualityTier {
    LOW,
    BALANCED,
    HIGH,
    ULTRA
}
```

### Degradation sırası

Performans düşerse veya Battery Saver aktifse sırayla:

1. Scene max FPS düşür.
2. Particle count multiplier azalt.
3. Rain/fog/cloud FPS düşür.
4. FBO resolution scale azalt.
5. Post-process layerları kapat.
6. Video resolution/bitrate fallback kullan.
7. Gerekiyorsa ultra/premium layerları disable et.

```text
Normal:
    Scene max FPS: 30
    Clouds: 12 FPS
    Stars: 4 FPS
    Particles: 30 FPS

Battery Saver:
    Scene max FPS: 15
    Clouds: 6 FPS
    Stars: 2 FPS
    Particles: 15 FPS veya disabled

Idle:
    Scene max FPS: 8
    Clouds: 4 FPS
    Stars: 1 FPS
    Particles: disabled
```

---

## 20. GL Resource Lifecycle

OpenGL kaynakları EGL context'e bağlıdır.

| Kaynak | Yaşam Döngüsü |
|---|---|
| JSON metadata | App process |
| Shader source text | App process cache |
| Shader program ID | EGL context/session |
| Texture ID | EGL context/session |
| FBO ID | EGL context/session |
| SurfaceTexture | Video layer lifecycle |
| MediaPlayer/Media3 Player | Video layer/session lifecycle |

### Context loss akışı

```text
EGL context lost
    ↓
GlResourceManager.onContextLost()
    ↓
ShaderProgramPool.clear()
TexturePool.clear()
FramebufferPool.clear()
MeshRegistry.clear()
    ↓
Scene keeps compiled CPU definition
    ↓
onContextRestored()
    ↓
GL resources recreated
```

---

## 21. Test ve Benchmark Planı

### Unit test

| Test | Amaç |
|---|---|
| `WallpaperDefinitionParserTest` | JSON doğru parse ediliyor mu |
| `DefinitionValidatorTest` | Schema/allowlist kontrolü çalışıyor mu |
| `SceneCompilerTest` | JSON → typed runtime model doğru mu |
| `LayerFramePolicyTest` | FPS/update kararları doğru mu |
| `SceneSchedulerTest` | Layer update/draw/cache sırası doğru mu |
| `ParallaxResolverTest` | Global/layer/texture parallax doğru birleşiyor mu |
| `QualityTierResolverTest` | Cihaz/pil/ısı durumuna göre kalite doğru mu |
| `WallpaperColorProviderTest` | Metadata/thumbnail üzerinden WallpaperColors cache doğru mu |
| `DisplayMetricsControllerTest` | Foldable/tablet/rotation değişimlerinde FOV/parallax clamp doğru mu |
| `ThermalStateControllerTest` | OS thermal status adaptive kaliteye doğru aktarılıyor mu |

### Instrumentation test

| Test | Amaç |
|---|---|
| `WallpaperServiceLifecycleTest` | Surface/visibility lifecycle doğru mu |
| `PreviewLifecycleTest` | Preview aç/kapat kaynak sızdırıyor mu |
| `ContextLossRecoveryTest` | EGL context kaybı sonrası sahne toparlanıyor mu |
| `VideoOesLayerTest` | SurfaceTexture/update/release doğru mu |
| `AssetPackResolverTest` | PAD path çözümü doğru mu |
| `WallpaperColorsEngineTest` | `onComputeColors()` GL readback yapmadan cached renk döndürüyor mu |
| `AmbientModeLifecycleTest` | Ambient/AOD profile render loop, sensör ve video kaynaklarını durduruyor mu |
| `ThermalListenerIntegrationTest` | OS thermal listener severe/critical durumda acil degradation tetikliyor mu |

### Benchmark

| Benchmark | Hedef |
|---|---|
| Startup benchmark | App açılışı hızlı |
| Catalog scroll benchmark | 100+ wallpaper listesi takılmadan akar |
| Preview open benchmark | Detay preview hızlı açılır |
| Frame timing benchmark | Frame drop/jank düşük |
| Memory benchmark | Preview kapatılınca kaynaklar bırakılır |
| Battery profiling | Görünmezken render/sensör/video çalışmaz |
| AGI frame profile | Draw call, overdraw ve GPU bottleneck görülür |

---

## 22. Ölçülebilir Kabul Kriterleri

| Kriter | Hedef |
|---|---:|
| App startup full wallpaper JSON parse | 0 |
| App startup full texture decode | 0 |
| Catalog item GL surface sayısı | 0 |
| Aynı anda aktif GL preview | Maksimum 1 |
| Görünmeyen wallpaper render FPS | 0 |
| Render hot path allocation | 0 veya ihmal edilebilir |
| Battery Saver scene FPS | 15 FPS veya altı |
| Idle scene FPS | 8 FPS veya altı |
| Ambient/AOD scene FPS | 0 veya burn-in policy periyodu kadar |
| `onComputeColors()` GL readback | 0 |
| Foldable/tablet surface değişiminde projection/parallax recompute | 1 forced update |
| Thermal severe/critical response | Bir sonraki queued engine command içinde degradation |
| Aktif GL resources | Sadece aktif scene/session |
| Texture ID singleton cache | 0 |
| JSON per-frame access | 0 |

---

## 23. Araştırılan Repo ve Kaynaklardan Alınan Dersler

### google/grafika

Grafika, Android grafik/media denemeleri için en faydalı referanslardan biridir. Ancak repo arşivlenmiştir ve production mimari olarak doğrudan kopyalanmamalıdır. Alınacak dersler:

- SurfaceTexture/video + OpenGL pratikleri incelenebilir.
- Custom EGL/render thread örnekleri incelenebilir.
- Activity odaklı örnekler wallpaper lifecycle'a birebir uyarlanamaz.

### GLWallpaperService / BasicGLWallpaper benzeri eski örnekler

Alınacak dersler:

- WallpaperService + OpenGL entegrasyon akışı anlaşılabilir.
- Modern Android lifecycle, asset delivery, benchmark ve Compose preview ihtiyaçları için yeterli değildir.

### Google Filament

Filament güçlü bir renderer'dır fakat Lumisky v1 için fazla ağırdır. Alınacak ders:

- Resource ownership, swapchain/surface ve render lifecycle fikirleri incelenebilir.
- Lumisky'nin 2D/layer-based wallpaper hedefi için doğrudan engine olarak kullanılmamalıdır.

### Android resmi dokümanları

Nihai mimaride resmi dokümanlardan alınan ana kararlar:

- `WallpaperService.Engine` gerçek wallpaper surface lifecycle sahibidir.
- `GLSurfaceView.Renderer` preview için uygundur; EGL context kaybında GL kaynakları yeniden oluşturulmalıdır.
- `SurfaceTexture` video/camera frame'lerini GLES texture olarak sunar ve `GL_TEXTURE_EXTERNAL_OES` kullanır.
- `Choreographer.FrameCallback` frame zamanlaması için VSync uyumlu `frameTimeNanos` sağlar.
- Play Asset Delivery büyük assetler için `install-time`, `fast-follow`, `on-demand` modelleri sunar.
- Macrobenchmark/Baseline Profile performans kabul kriterlerine eklenmelidir.
- Android GPU Inspector GL/GPU bottleneck analizi için kullanılmalıdır.

---


---

## 24. Production Hardening — Taşımadan Önce Zorunlu Ekler

Bu bölüm, mimarinin gerçek uygulamaya taşınmadan önce netleştirilmesi gereken üretim kurallarını tanımlar. Bu kurallar olmadan 100+ wallpaper içeren kataloglarda bozuk JSON, eksik asset, indirilemeyen paket, premium yetki hatası, context loss veya shader compile hatası runtime crash’e dönüşebilir.

Bu yüzden aşağıdaki sistemler **opsiyonel değil**, taşıma öncesi zorunlu mimari parçalardır:

```text
Zorunlu production ekleri:
1. Schema migration/versioning
2. Runtime fallback/error policy
3. Persistent state model
4. Asset pack state machine
5. Capability matrix
6. Premium/entitlement model
7. CI validation tool
8. Existing app migration phases
9. Ölçülebilir performance budgets
```

---

## 25. Schema Migration ve Versioning

`schemaVersion` tek başına yeterli değildir. JSON şeması değiştiğinde eski wallpaper tanımları bozulmamalıdır. Her schema versiyonu için migration ve default üretme kuralları bulunmalıdır.

### 25.1 Temel karar

```text
Wallpaper JSON = dış veri formatı
CompiledSceneDefinition = engine'in çalıştırdığı stabilize edilmiş runtime modeli
```

Engine doğrudan JSON modelini render etmez. Akış:

```text
Raw JSON
  ↓ parse
WallpaperDefinitionVx
  ↓ migrate
WallpaperDefinitionLatest
  ↓ validate
  ↓ normalize/defaults
CompiledSceneDefinition
  ↓ create runtime objects
RuntimeScene
```

### 25.2 Version migration arayüzü

```kotlin
interface WallpaperSchemaMigration {
    val fromVersion: Int
    val toVersion: Int

    fun migrate(rawJson: JsonObject): JsonObject
}

class WallpaperSchemaMigrator(
    private val migrations: List<WallpaperSchemaMigration>,
    private val latestVersion: Int
) {
    fun migrateToLatest(rawJson: JsonObject): SchemaMigrationResult {
        var current = rawJson
        var version = current["schemaVersion"]?.jsonPrimitive?.int ?: 1

        while (version < latestVersion) {
            val migration = migrations.firstOrNull { it.fromVersion == version }
                ?: return SchemaMigrationResult.UnsupportedVersion(version)
            current = migration.migrate(current)
            version = migration.toVersion
        }

        return SchemaMigrationResult.Migrated(current)
    }
}

sealed interface SchemaMigrationResult {
    data class Migrated(val json: JsonObject) : SchemaMigrationResult
    data class UnsupportedVersion(val fromVersion: Int) : SchemaMigrationResult
    data class InvalidJson(val reason: String) : SchemaMigrationResult
}
```

`UnsupportedVersion` runtime crash üretmez; validator bu sonucu scene load error'a çevirir. Release build'de last successful scene veya starter fallback wallpaper kullanılır, CI validator eksik migration varsa build'i fail eder.

### 25.3 Required / optional / default kuralı

Her JSON alanı üç kategoriden birinde olmalıdır:

| Tür | Davranış |
|---|---|
| Required | Eksikse definition invalid olur |
| Optional with default | Eksikse normalizer default üretir |
| Optional nullable | Eksik olabilir, runtime bunu destekler |

Örnek default üretimi:

```kotlin
object WallpaperDefaults {
    val defaultBlendMode = BlendMode.ALPHA
    val defaultRenderTarget = RenderTargetMode.DIRECT
    val defaultFramePolicy = LayerFramePolicy(
        mode = LayerFrameMode.MATCH_SCENE,
        cacheMode = LayerCacheMode.NONE
    )
    val defaultParallax = LayerParallaxDefinition(
        factorX = 0f,
        factorY = 0f,
        depth = 0f
    )
}
```

### 25.4 Schema uyumluluk kuralı

```text
Aynı major schema içinde eski wallpaperlar migrate edilebilir olmalı.
Breaking change gerekiyorsa yeni major schema açılmalı.
Migration yazılmadan schema değişikliği merge edilmemeli.
```

---

## 26. Runtime Fallback ve Error Policy

Lumisky crash-first değil, fallback-first çalışmalıdır. Hata oluştuğunda kullanıcı siyah ekran, crash veya donmuş GL surface görmemelidir.

### 26.1 Error kategorileri

```kotlin
sealed class SceneLoadError {
    data class UnknownLayerType(val type: String) : SceneLoadError()
    data class MissingAsset(val path: String) : SceneLoadError()
    data class ShaderCompileFailed(val shaderRef: String, val log: String) : SceneLoadError()
    data class TextureDecodeFailed(val path: String) : SceneLoadError()
    data class AssetPackUnavailable(val packName: String) : SceneLoadError()
    data class GlInitFailed(val reason: String) : SceneLoadError()
    data class ContextRestoreFailed(val sceneId: String) : SceneLoadError()
    data class InvalidDefinition(val errors: List<String>) : SceneLoadError()
}
```

### 26.2 Fallback hiyerarşisi

Runtime hata durumunda fallback sırası:

```text
1. Layer-level fallback
   - Shader compile failed → fallback shader
   - Missing optional texture → fallback texture
   - Broken effect layer → layer disable

2. Scene-level fallback
   - Scene invalid → last successful scene
   - Last successful scene unavailable → starter fallback wallpaper

3. Engine-level fallback
   - GL init failed → static fallback image
   - Repeated GL failure → safe mode, GL preview disabled

4. UI-level fallback
   - Asset pack missing → download/retry UI
   - Offline → installed fallback wallpaper
```

### 26.3 Layer fallback örneği

```json
{
  "id": "fog",
  "type": "FogLayer",
  "shaderRef": "common.fog.volumetric.v1",
  "fallback": {
    "onShaderError": "disable_layer",
    "onMissingTexture": "use_transparent_texture"
  }
}
```

### 26.4 Fallback kuralları

```text
Unknown required layer:
    Scene invalid → last successful scene

Unknown optional layer:
    Layer skip → warning log

Shader compile fail:
    fallback shader varsa kullan
    yoksa optional layer disable
    required layer ise scene fallback

Missing texture:
    fallback texture varsa kullan
    yoksa optional layer disable
    required layer ise scene fallback

Asset pack unavailable:
    UI download state'e geç
    live wallpaper tarafında last successful scene korunur

Context loss restore failed:
    scene resources bir kez temiz yeniden oluşturulur
    tekrar başarısızsa starter fallback wallpaper

GL init failed:
    GL render başlatılmaz
    static image fallback gösterilir
```

### 26.5 Son başarılı scene

Uygulama, son başarılı çalışan scene bilgisini persistent olarak saklamalıdır:

```kotlin
data class LastSuccessfulSceneState(
    val wallpaperId: String,
    val definitionVersion: Int,
    val assetPackVersions: Map<String, Long>,
    val qualityTier: QualityTier,
    val timestampMillis: Long
)
```

Kural:

```text
Yeni scene tamamen load + validate + GL resource init başarılı olmadan aktif scene değiştirilmez.
```

Bu sayede bozuk wallpaper seçimi canlı wallpaperı siyah ekrana düşürmez.

---

## 27. Persistent State Model

Runtime ve kullanıcı tercihleri RAM'de kaybolmamalıdır. Ancak render layer içinde DataStore/Room okunmamalıdır.

### 27.1 DataStore ile tutulacak ayarlar

DataStore için uygun state’ler:

```kotlin
data class UserWallpaperSettings(
    val selectedWallpaperId: String,
    val qualityPreference: QualityPreference,
    val parallaxEnabled: Boolean,
    val parallaxStrength: Float,
    val batterySaverBehavior: BatterySaverBehavior,
    val lastSuccessfulWallpaperId: String?,
    val lastSuccessfulDefinitionVersion: Int?
)
```

### 27.2 Room veya disk index ile tutulacak katalog/download state

100+ wallpaper ve asset pack state için Room tercih edilebilir. İlk MVP’de DataStore + JSON index yeterli olabilir; katalog büyüyünce Room’a geçilebilir.

```kotlin
data class WallpaperInstallState(
    val wallpaperId: String,
    val assetPackName: String?,
    val downloadState: AssetPackState,
    val installedVersion: Long?,
    val lastError: String?,
    val updatedAtMillis: Long
)
```

### 27.3 Entitlement/premium state

Premium yetki catalog JSON’dan gelmemelidir. Catalog sadece ürünün premium olduğunu söyleyebilir; gerçek yetki `EntitlementRepository` üzerinden doğrulanmalıdır.

```kotlin
data class EntitlementState(
    val userIdHash: String?,
    val ownedProductIds: Set<String>,
    val subscriptionActive: Boolean,
    val lastVerifiedAtMillis: Long,
    val source: EntitlementSource
)

enum class EntitlementSource {
    GOOGLE_PLAY_BILLING,
    LOCAL_DEBUG_OVERRIDE,
    UNKNOWN
}
```

### 27.4 State akış kuralı

```text
DataStore/Room/Billing
  ↓
Repository snapshot
  ↓
ViewModel UI state
  ↓
EngineController command
  ↓
GL Event Queue
  ↓
RuntimeScene update
```

Layer, repository veya DataStore okumaz.

---

## 28. Asset Pack State Machine

Play Asset Delivery kullanılan projede asset pack durumları açıkça modellenmelidir. Aksi halde offline, retry, pack silinmesi ve indirme yarıda kalması gibi durumlar belirsiz kalır.

### 28.1 AssetPackState

```kotlin
sealed class AssetPackState {
    data object NotRequired : AssetPackState()
    data object NotInstalled : AssetPackState()
    data object Pending : AssetPackState()
    data class Downloading(
        val bytesDownloaded: Long,
        val totalBytesToDownload: Long,
        val progress: Float
    ) : AssetPackState()
    data object Transferring : AssetPackState()
    data class Installed(
        val packName: String,
        val version: Long,
        val localPath: String
    ) : AssetPackState()
    data class Failed(
        val reason: AssetPackFailureReason,
        val retryable: Boolean
    ) : AssetPackState()
    data object RequiresWifiConfirmation : AssetPackState()
    data object Canceled : AssetPackState()
    data object Removed : AssetPackState()
}
```

### 28.2 Failure reason

```kotlin
enum class AssetPackFailureReason {
    NETWORK_ERROR,
    INSUFFICIENT_STORAGE,
    PLAY_STORE_UNAVAILABLE,
    PACK_NOT_FOUND,
    USER_CANCELED,
    PERMISSION_OR_POLICY_BLOCKED,
    UNKNOWN
}
```

### 28.3 State geçişleri

```text
NotInstalled
  ↓ requestDownload
Pending
  ↓ download started
Downloading
  ↓ downloaded
Transferring
  ↓ ready
Installed

Downloading
  ↓ fail
Failed(retryable=true)

Installed
  ↓ pack removed / app data cleared / missing path
Removed
  ↓ requestDownload
Pending
```

### 28.4 Offline davranış

```text
Seçilen wallpaper installed değil + internet yok:
    UI: "İndirme gerekli" durumu gösterilir
    Engine: last successful scene çalışmaya devam eder

Aktif wallpaper asset pack sonradan silinmiş:
    Engine: last successful scene valid değilse starter fallback wallpaper
    UI: wallpaper state = Removed

Starter fallback wallpaper:
    Base APK içinde bulunur
    Asset pack gerektirmez
    Shader/texture minimaldir
```

### 28.5 Asset switch kuralı

```text
Asset pack indirme tamamlanmadan scene aktif edilmez.
GL kaynakları hazır olmadan current scene değiştirilmez.
Başarılı load sonrası selectedWallpaperId kalıcı hale getirilir.
```

---

## 29. Capability Matrix

Her layer her runtime modunda çalışamaz. Bu matrisi engine zorlamalıdır.

### 29.1 Runtime mode

```kotlin
enum class RuntimeMode {
    CATALOG_GRID,
    APP_DETAIL_PREVIEW,
    APP_FULLSCREEN_PREVIEW,
    SYSTEM_PREVIEW,
    LIVE_WALLPAPER,
    THUMBNAIL_CAPTURE
}
```

### 29.2 Layer capability tablosu

| Layer | Catalog Grid | Detail Preview | Fullscreen Preview | System Preview | Live Wallpaper | Battery Saver |
|---|---:|---:|---:|---:|---:|---|
| ImageLayer | Thumbnail only | Evet | Evet | Evet | Evet | Evet |
| ShaderLayer | Hayır | Evet | Evet | Evet | Evet | Degrade |
| AtmosphereLayer | Hayır | Evet | Evet | Evet | Evet | 1 FPS/cache |
| Sun/Moon Layer | Hayır | Evet | Evet | Evet | Evet | Minute tick |
| StarsLayer | Hayır | Evet | Evet | Evet | Evet | FPS düşer |
| RainLayer | Hayır | Opsiyonel | Evet | Evet | Evet | FPS düşer/disable |
| FogLayer | Hayır | Opsiyonel | Evet | Evet | Evet | FBO half-res/cache |
| VideoOesLayer | Hayır | Opsiyonel | Evet | Evet | Evet | Pause/degrade |
| AnimationLayer | Hayır | Evet | Evet | Evet | Event-based | Event-based |
| PostProcessLayer | Hayır | Opsiyonel | Evet | Opsiyonel | Opsiyonel | Disable |
| TouchReactiveLayer | Hayır | Opsiyonel | Evet | Opsiyonel | Opsiyonel | Disable |
| SensorParallaxLayer | Hayır | Simulated | Evet | Evet | Evet | Sensitivity düşer |

### 29.3 Enforce kuralı

```text
SceneCompiler, RuntimeProfile'a göre layer izinlerini kontrol eder.
İzinli olmayan layer:
    catalog grid → thumbnail only
    preview → layer disable veya düşük kalite
    live → policy'ye göre disable/degrade
```

Bu sayede katalog listesinde yanlışlıkla video veya GL preview çalışmaz.

---

## 30. Premium ve Entitlement Modeli

Catalog JSON premium bilgisini gösterebilir, ancak yetki kaynağı olamaz.

### 30.1 Catalog model

```json
{
  "id": "premium_neon_city",
  "isPremium": true,
  "productId": "lumisky_pack_city_premium",
  "assetPack": "city_premium_pack"
}
```

### 30.2 Doğru yetki akışı

```text
Catalog says:
    this wallpaper is premium

EntitlementRepository says:
    user can/cannot use this wallpaper

AssetPackRepository says:
    required asset pack installed/not installed
```

### 30.3 Runtime karar tablosu

| Premium | Entitled | Asset installed | Davranış |
|---:|---:|---:|---|
| Hayır | - | Evet | Aç |
| Hayır | - | Hayır | İndir |
| Evet | Hayır | - | Paywall göster |
| Evet | Evet | Hayır | İndir |
| Evet | Evet | Evet | Aç |

### 30.4 Engine güvenlik kuralı

```text
Engine, premium doğrulaması yapmaz.
Engine sadece kendisine verilen doğrulanmış scene'i çalıştırır.
Premium kararı Repository/ViewModel katmanında çözülür.
```

---

## 31. CI Validation Aracı

100+ wallpaper içeren projede JSON hataları runtime’a kalmamalıdır. Build/CI aşamasında tüm wallpaper paketleri doğrulanmalıdır.

### 31.1 Validator görevleri

```text
JSON schema valid mi?
schemaVersion destekleniyor mu?
migration çalışıyor mu?
required alanlar var mı?
layer type allowlist içinde mi?
shaderRef var mı?
custom shader path var mı?
uniform isimleri shader ile uyumlu mu?
uniform type doğru mu?
texture path var mı?
texture boyutu kalite limitlerini aşıyor mu?
texture format destek listesine uygun mu?
assetPack içinde beklenen dosyalar var mı?
video dosyası boyut/süre limitini aşıyor mu?
zIndex çakışmaları kabul edilebilir mi?
runtime capability matrix ihlali var mı?
premium wallpaper productId içeriyor mu?
starter fallback wallpaper valid mi?
```

### 31.2 CLI örnek

```bash
./gradlew validateLumiskyWallpapers
```

### 31.3 CI output örneği

```text
[OK] ocean_sunset_clouds.json
[OK] cyberpunk_city_01.json
[ERROR] premium_neon_city.json
  - Missing shaderRef: common.neon.glow.v1
  - Texture exceeds HIGH limit: textures/city/bg_8k.webp
  - Premium wallpaper missing productId
```

### 31.4 Merge kuralı

```text
Validator error varsa release branch'e merge yok.
Warning varsa manuel onay gerekir.
Starter fallback wallpaper invalid ise build fail.
```

---

## 32. Ölçülebilir Performance Budgets

Performans hedefleri ölçülebilir olmalıdır. Aşağıdaki değerler ilk üretim hedefidir; cihaz segmentlerine göre testlerde netleştirilebilir.

Kural:

```text
Her hedef cihaz sınıfı için ortalama değil p95/p99 davranışı izlenir.
Benchmark sonucu "akıcı görünüyor" diye kabul edilmez; frame timing, bellek ve lifecycle release kanıtı gerekir.
```

### 32.1 UI ve katalog

| Metrik | Hedef |
|---|---:|
| Cold start first meaningful UI | < 1.5-2.5 sn |
| Catalog metadata load | < 150 ms |
| Catalog first screen thumbnail ready | < 500 ms |
| Catalog scroll jank | p95 frame <= 16.6 ms hedef, jank frame oranı düşük |
| Catalog item GL surface sayısı | 0 |
| Aynı anda aktif GL preview | Maksimum 1 |

### 32.2 Preview

| Metrik | Hedef |
|---|---:|
| Detail preview open | < 700 ms hedef, fallback thumbnail hemen |
| Fullscreen preview open | < 1 sn hedef |
| Preview frame pacing | p95 frame time seçilen FPS budget'ını aşmamalı |
| Preview close sonrası GL resource release | Texture/program/FBO/player release log veya test kanıtı |

### 32.3 Live wallpaper

| Metrik | Hedef |
|---|---:|
| Balanced scene FPS | 24-30 FPS |
| Premium scene FPS | 30-45 FPS |
| Ultra FPS | Sadece güçlü cihaz/preview |
| Battery saver scene FPS | 8-15 FPS |
| Idle scene FPS | 1-8 FPS |
| Görünmez wallpaper FPS | 0 FPS |
| Render hot path allocation | 0 veya ihmal edilebilir |

### 32.4 Bellek ve GPU budget

| Cihaz | Aktif runtime texture budget | FBO budget | Not |
|---|---:|---:|---|
| LOW | 32-48 MB | minimum/half-res | post-process kapalı |
| BALANCED | 64-96 MB | kontrollü | varsayılan |
| HIGH | 128 MB civarı | izinli | kaliteli texture |
| ULTRA | cihaz durumuna bağlı | izinli | ısınma izlenir |

### 32.5 Frame time hedefleri

| FPS | Ortalama frame budget |
|---:|---:|
| 15 FPS | 66.6 ms |
| 30 FPS | 33.3 ms |
| 45 FPS | 22.2 ms |
| 60 FPS | 16.6 ms |

Kural:

```text
Lumisky'nin ana live wallpaper hedefi 60 FPS değil, stabil frame pacing + düşük pil tüketimidir.
60 FPS sadece özel preview/etkileşimli sahnelerde hedeflenir.
```

### 32.6 Kabul eşikleri

İlk üretim kabulü için önerilen eşikler:

```text
30 FPS live wallpaper:
    p95 frame time <= 33.3 ms
    p99 frame time <= 50 ms
    görünmez durumda render callback = 0

15 FPS battery saver:
    p95 frame time <= 66.6 ms
    video/particle/post-process policy downgrade uygulanmış olmalı

Catalog:
    liste item içinde GL surface = 0
    100+ metadata parse açılışta yapılmamalı
    thumbnail cache miss durumunda UI bloklanmamalı

Preview:
    preview kapanınca aktif EGL context, texture, FBO ve player release doğrulanmalı
    aynı anda ikinci GL preview açılmamalı

Shader cache:
    binary cache miss/fail normal compile fallback'e döner
    compile/link render hot path içinde yapılmaz
```

---

## 33. Mevcut Uygulamadan Yeni Mimariye Migration Plan

Büyük rewrite yapılmamalıdır. Eski render path kontrollü olarak yeni data-driven hatta taşınmalıdır.

### İlk uygulanabilir minimum slice

Mevcut Lumisky kod tabanına en güvenli başlangıç, render motorunu tamamen değiştirmek değil, mevcut manifest/catalog akışının yanına data-driven definition hattını eklemektir.

```text
Minimum slice:
1. Mevcut `wallpapers/index.json` veya eşdeğer catalog kaynağı korunur.
2. Tek bir mevcut wallpaper seçilir.
3. Bu wallpaper için `WallpaperDefinition` JSON'u yazılır.
4. Parser + validator + default normalizer çalışır.
5. SceneCompiler sadece `TextureLayer` + basit `ShaderLayer` üretir.
6. İlk doğrulama app detail preview üzerinde yapılır.
7. Live wallpaper service eski render path ile çalışmaya devam eder.
8. Yeni preview stabil olunca live adapter feature flag arkasında bağlanır.
```

Bu dilim geçmeden `VideoOesLayer`, PAD, premium entitlement, multi-layer scheduler, shader binary cache zorunluluğu ve eski renderer kaldırma işlerine başlanmamalıdır.

### Mevcut sınıf anchor'larıyla geçiş

```text
PreviewGlRenderer:
    Yeni RuntimeScene'i önce app preview tarafında dener.
    Eski PreviewSkyProgram davranışı referans görsel olarak korunur.

WallpaperRenderController:
    GL thread, visibility, minute tick ve parallax scheduling davranışını taşır.
    Yeni SceneScheduler bu lifecycle kararlarının yerine geçmez; onlara bağlanır.

ServiceRenderPolicyResolver:
    Battery saver, thermal ve FPS kararları için mevcut policy anchor'ı olarak kalır.
    AdaptiveQualityController bu kararı genişletir, sıfırdan ayrı policy üretmez.

WallpaperConfig / manifest:
    Mevcut capabilities/effects/textures/creator bilgisi yeni WallpaperDefinition'a map edilir.
    İlk fazda eski manifest formatını kırmadan adapter yazılır.
```

### Faz 0 — Envanter

```text
Mevcut wallpaperlar listelenir.
Mevcut shader/texture dosyaları çıkarılır.
Ortak shader davranışları gruplanır.
Hangi özellikler layer'a dönüşecek belirlenir.
```

### Faz 1 — Catalog ve Definition Parser

```text
wallpaper_catalog.json oluştur.
1-2 wallpaper için JSON definition yaz.
Parser + validator + default normalizer kur.
Henüz GL render değiştirilmez.
```

### Faz 2 — SceneCompiler ve LayerFactory

```text
LayerDefinition → CompiledLayerDefinition hattı kur.
LayerFactory map kur.
ImageLayer ve ShaderLayer ile ilk sahne çalıştır.
Eski render path hâlâ durur.
```

### Faz 3 — Preview Adapter

```text
App detail preview yeni engine ile çalışır.
Liste/grid thumbnail-only kalır.
Aynı anda tek GL preview kuralı uygulanır.
```

### Faz 4 — Live Wallpaper Adapter

```text
WallpaperService.Engine yeni RuntimeSession'a bağlanır.
Visibility/surface/context lifecycle test edilir.
Eski live render path feature flag ile açık/kapalı yapılır.
```

### Faz 5 — Scheduler ve Cache

```text
LayerFramePolicy aktif edilir.
FBO_CACHE ve STATIC_TEXTURE uygulanır.
Cached FBO final composite her frame çizilir.
Battery saver/idle degradation eklenir.
```

### Faz 6 — Event ve Parallax

```text
EngineEventQueue eklenir.
ParallaxController eklenir.
ON_USER_PRESENT / ON_SCREEN_ON eventleri yeni hatta taşınır.
AnimationLayer eklenir.
```

### Faz 7 — VideoOesLayer

```text
VideoOesLayer ayrı pipeline olarak eklenir.
SurfaceTexture lifecycle ve context restore test edilir.
Video sadece izinli runtime mode'larda aktif olur.
```

### Faz 8 — PAD ve Premium

```text
AssetPackRepository eklenir.
Asset pack state machine bağlanır.
EntitlementRepository eklenir.
Premium/download UI state ayrılır.
```

### Faz 9 — Eski Render Path Kapatma

```text
Tüm aktif wallpaperlar JSON definition ile çalışır.
Macrobenchmark ve memory testleri geçer.
Eski renderer kaldırılır.
```

### Faz 10 — Release Hardening

```text
CI validator zorunlu yapılır.
Starter fallback wallpaper doğrulanır.
Context loss recovery testi yapılır.
Crash-free fallback politikası test edilir.
```

---

## 34. v5 Nihai Uygulama Önceliği

Taşımaya başlamadan önce minimum eklenmesi gereken 5 bölüm:

```text
1. Schema Migration
2. Fallback/Error Policy
3. Persistent State Model
4. Asset Pack State Machine
5. Migration Phases from Current App
```

Bu 5 bölüm tamamlanmadan doğrudan render engine taşımaya başlamak risklidir. Çünkü engine doğru çizse bile bozuk JSON, eksik asset, indirilemeyen paket veya eski scene restore hatası uygulamayı siyah ekrana veya crash’e götürebilir.




---

## 35. v5 Ek Üretim Sertleştirme Kararları

Bu bölüm, v5 mimarisine eklenen son üretim sertleştirme kararlarını tanımlar. Amaç; sahne geçişlerinde shader jank riskini azaltmak, bitmap decode sırasında GC baskısını düşürmek, sensörleri tek merkezden yönetmek, MVVM ile GL engine arasındaki asenkron köprüyü netleştirmek ve fallback mekanizmasının geliştirici tarafında gözlemlenebilir olmasını sağlamaktır.

---

## 36. Shader Binary Cache ve Shader Warmup

Shader compile/link işlemi GL thread üzerinde yapılır. Karmaşık atmosfer, volumetric fog, procedural sky veya post-process shader'ları ilk sahne açılışında jank oluşturabilir. Bu yüzden Lumisky, OpenGL ES 3.0 program binary cache'i destekleyen cihazlarda shader binary cache kullanır.

Program binary cache bir doğruluk mekanizması değildir ve her cihaz/driver üzerinde garanti performans sağlamaz. Amaç "sıfır gecikme garantisi" vermek değil, sahne geçişinde compile/link işini mümkün olduğunca önceden ve ölçülebilir şekilde azaltmaktır.

### 36.1 Temel karar

```text
Öncelik:
1. Program binary cache'den yükle
2. Başarısızsa source compile/link yap
3. Compile/link başarılıysa binary cache'e yaz
4. Compile başarısızsa fallback shader/layer policy uygula
```

Cache'e yazılacak programlar link öncesinde retrievable olacak şekilde işaretlenmelidir.

```kotlin
GLES30.glProgramParameteri(
    programId,
    GLES30.GL_PROGRAM_BINARY_RETRIEVABLE_HINT,
    GLES30.GL_TRUE
)
```

### 36.2 Destek kontrolü

Program binary cache zorunlu bir sistem değildir. Cihazda destek olup olmadığı runtime'da kontrol edilir.

```kotlin
class ShaderBinarySupport {
    fun isSupported(): Boolean {
        val count = IntArray(1)
        GLES30.glGetIntegerv(GLES30.GL_NUM_PROGRAM_BINARY_FORMATS, count, 0)
        return count[0] > 0
    }
}
```

Kural:

```text
GL_NUM_PROGRAM_BINARY_FORMATS == 0:
    ShaderBinaryCache disabled
    Normal lazy compile pipeline kullanılır
```

### 36.3 Cache key

Shader binary cache, yalnızca shader ID ile anahtarlanmamalıdır. Driver/compiler değişirse binary geçersiz olabilir.

```kotlin
data class ShaderBinaryCacheKey(
    val shaderRef: String,
    val vertexSourceHash: String,
    val fragmentSourceHash: String,
    val definesHash: String,
    val shaderSchemaVersion: Int,
    val appVersionCode: Long,
    val glVendor: String,
    val glRenderer: String,
    val glVersion: String,
    val glslVersion: String,
    val binaryFormat: Int
)
```

### 36.4 Binary load akışı

```kotlin
class ShaderProgramPool(
    private val binaryCache: ShaderBinaryCache,
    private val compiler: ShaderCompiler
) {
    fun getOrCreate(shader: ResolvedShader): GlProgram {
        val key = binaryCache.buildKey(shader)

        val cached = binaryCache.read(key)
        if (cached != null) {
            val program = GLES30.glCreateProgram()
            GLES30.glProgramBinary(
                program,
                cached.binaryFormat,
                cached.bytes,
                cached.bytes.size
            )

            if (isLinked(program)) {
                return GlProgram(program)
            }

            GLES30.glDeleteProgram(program)
            binaryCache.invalidate(key)
        }

        val compiled = compiler.compileAndLink(shader)
        binaryCache.writeIfSupported(key, compiled.programId)
        return compiled
    }
}
```

### 36.5 Cache invalidation

Binary cache şu durumlarda invalid edilir:

```text
App version değişti
Shader source hash değişti
Shader define/variant değişti
Shader schema version değişti
GL_VENDOR / GL_RENDERER / GL_VERSION değişti
GLSL version değişti
Program binary load başarısız oldu
Driver update sonrası link status false döndü
Cache dosyası checksum geçersiz
```

Uygulama başlangıcında veya ilk EGL context kurulumunda driver imzası değişimi otomatik algılanır.

```kotlin
data class GlDriverSignature(
    val vendor: String,
    val renderer: String,
    val version: String,
    val glslVersion: String,
    val binaryFormatsHash: String
)

class ShaderBinaryCache(
    private val metadataStore: ShaderCacheMetadataStore
) {
    fun validateDriverSignature(current: GlDriverSignature) {
        val previous = metadataStore.readDriverSignature()
        if (previous != null && previous != current) {
            invalidateAll(reason = "driver_signature_changed")
        }
        metadataStore.writeDriverSignature(current)
    }
}
```

Binary format uyumsuzluğu, `glProgramBinary` sonrası link başarısızlığı veya cache metadata uyuşmazlığı tek programı değil, aynı driver imzasıyla yazılmış ilgili cache bucket'ını da invalidate edebilir. Aynı bozuk binary session içinde tekrar denenmez.

### 36.6 Warmup policy

Shader compile işlemi render hot path içinde yapılmaz.

```text
Catalog grid:
    Shader compile yok

Detail preview açılırken:
    Seçili wallpaper shaderları preload edilir

Fullscreen preview:
    Gerekli shaderlar hazır değilse thumbnail/frozen frame gösterilir

Live wallpaper set:
    Scene aktif edilmeden önce shader programları hazır edilir
```

Mevcut Lumisky akışında warmup sırası:

```text
WallpaperManifestCatalogSource / index metadata:
    Shader compile yok

PreviewGlRenderer açılmadan önce:
    seçili wallpaper shaderRef listesi resolve edilir
    ShaderProgramPool preload başlatır

WallpaperRenderController live scene switch:
    last successful scene korunur
    yeni scene shader + texture hazırlığı tamamlanmadan current scene değiştirilmez
```

### 36.7 Warmup concurrency ve throttling

Shader warmup kuyruğu render frame'ini bloklamaz. Ön hazırlık işi GL context gerektirdiği için tek render context üzerinde kontrollü yapılır; CPU tarafı shader source resolve/hash gibi işler ayrı worker'da hazırlanabilir.

```text
ShaderWarmupQueue:
    maxParallelCpuPrepare = 1-2
    maxGlCompilePerFrame = 1
    maxWarmupMillisPerFrame = 2-4 ms
    priority = selected wallpaper > adjacent detail preview > catalog metadata
```

Kural:

```text
Catalog scroll sırasında compile yok.
Detail açılırken düşük öncelikli preload var.
Fullscreen/live activate sırasında required shader hazır değilse thumbnail/frozen fallback gösterilir.
Compile queue büyürse eski/düşük öncelikli warmup işleri drop edilir.
```

`ShaderCompiler` aynı anda birden fazla ağır compile/link ile GL thread'i kilitlemez. Kaynak compile fallback gerekirse telemetry ile işaretlenir ama kullanıcıya siyah ekran gösterilmez.

### 36.8 Production kuralı

```text
Shader binary cache performans optimizasyonudur.
Doğruluk kaynağı değildir.
Binary cache başarısız olursa app crash etmez; normal shader compile fallback'e döner.
```

---

## 37. BitmapPool ve Decode Bellek Yönetimi

Runtime büyük texture stratejisi öncelikle GPU compressed texture formatlarına dayanır. Ancak thumbnail, fallback WebP/PNG/JPG, starter wallpaper ve bazı asset pack içerikleri bitmap decode gerektirebilir. Bu durumda her decode için yeni bitmap allocation yapılması GC baskısı oluşturabilir.

### 37.1 Temel karar

```text
GPU compressed texture:
    BitmapPool kullanılmaz

WebP/PNG/JPG decode:
    BitmapPool + inBitmap reuse kullanılır

Texture upload sonrası:
    Bitmap JVM heap'te tutulmaz
    Uygunsa pool'a döner, değilse recycle/release edilir
```

### 37.2 BitmapPool

```kotlin
class BitmapPool(
    private val maxBytes: Long
) {
    private val pool = ArrayDeque<Bitmap>()
    private var currentBytes = 0L

    fun get(width: Int, height: Int, config: Bitmap.Config): Bitmap? {
        val iterator = pool.iterator()
        while (iterator.hasNext()) {
            val candidate = iterator.next()
            if (candidate.isMutable && canReuse(candidate, width, height, config)) {
                iterator.remove()
                currentBytes -= candidate.allocationByteCount
                return candidate
            }
        }
        return null
    }

    fun put(bitmap: Bitmap) {
        if (!bitmap.isMutable || bitmap.isRecycled) return
        if (bitmap.allocationByteCount > maxBytes) {
            bitmap.recycle()
            return
        }

        while (currentBytes + bitmap.allocationByteCount > maxBytes && pool.isNotEmpty()) {
            val removed = pool.removeFirst()
            currentBytes -= removed.allocationByteCount
            removed.recycle()
        }

        pool.addLast(bitmap)
        currentBytes += bitmap.allocationByteCount
    }

    private fun canReuse(
        bitmap: Bitmap,
        width: Int,
        height: Int,
        config: Bitmap.Config
    ): Boolean {
        val requiredBytes = width * height * bytesPerPixel(config)
        return bitmap.allocationByteCount >= requiredBytes
    }
}
```

### 37.3 Decode akışı

```kotlin
class BitmapDecoder(
    private val bitmapPool: BitmapPool
) {
    fun decode(path: String, target: DecodeTarget): DecodeResult {
        val options = BitmapFactory.Options().apply {
            inMutable = true
            inPreferredConfig = Bitmap.Config.ARGB_8888
            inSampleSize = target.inSampleSize
            inBitmap = bitmapPool.get(target.width, target.height, Bitmap.Config.ARGB_8888)
        }

        return try {
            BitmapFactory.decodeFile(path, options)?.let(DecodeResult::Decoded)
                ?: DecodeResult.Failed(path, DecodeFailureReason.NULL_RESULT)
        } catch (e: IllegalArgumentException) {
            options.inBitmap = null
            BitmapFactory.decodeFile(path, options)?.let(DecodeResult::Decoded)
                ?: DecodeResult.Failed(path, DecodeFailureReason.IN_BITMAP_REJECTED)
        } catch (e: OutOfMemoryError) {
            DecodeResult.Failed(path, DecodeFailureReason.OUT_OF_MEMORY)
        }
    }
}

sealed interface DecodeResult {
    data class Decoded(val bitmap: Bitmap) : DecodeResult
    data class Failed(
        val path: String,
        val reason: DecodeFailureReason
    ) : DecodeResult
}

enum class DecodeFailureReason {
    NULL_RESULT,
    IN_BITMAP_REJECTED,
    OUT_OF_MEMORY,
    UNSUPPORTED_FORMAT
}
```

### 37.4 Kurallar

```text
Render loop içinde bitmap decode yapılmaz.
Bitmap decode IO/decode pipeline'da yapılır.
GL texture upload GL thread'de yapılır.
Bitmap upload sonrası JVM'de tutulmaz.
BitmapPool boyutu cihaz kalite profiline göre sınırlanır.
inBitmap başarısız olursa fallback decode denenir.
Decode yine başarısız olursa required asset için scene fallback, optional asset için transparent/fallback texture kullanılır.
BitmapPool trimMemory/onLowMemory sinyallerinde agresif küçültülür.
```

### 37.5 Trim memory davranışı

BitmapPool uygulama process belleğini korumak için Android memory sinyallerine doğrudan tepki verir.

```kotlin
class LumiskyMemoryCallbacks(
    private val bitmapPool: BitmapPool,
    private val assetByteCache: AssetByteCache
) : ComponentCallbacks2 {
    override fun onTrimMemory(level: Int) {
        when {
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL -> bitmapPool.trimToBytes(0)
            level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW -> bitmapPool.trimToFraction(0.25f)
            level >= ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> bitmapPool.trimToFraction(0.50f)
        }
        assetByteCache.trimFor(level)
    }

    override fun onLowMemory() {
        bitmapPool.clear()
        assetByteCache.clear()
    }
}
```

Kural:

```text
Preview kapandıysa bitmap pool küçültülür.
Live wallpaper görünmezse transient decode cache düşürülür.
Asset pack değişiminde eski pack'e ait reusable bitmapler pool'dan çıkarılır.
inBitmap IllegalArgumentException sonrası aynı bitmap tekrar pool'a konmaz.
```

---

## 38. SensorDispatcher

Parallax için sensörler tek bir merkezden yönetilir. Live wallpaper ve app preview aynı anda aktif olduğunda iki ayrı `SensorManager` listener açılması engellenir.

### 38.1 Temel karar

```text
SensorDispatcher:
    Application/process scope olur; Activity veya Service context tutmaz
    SensorManager listener sayısını minimize eder
    Aktif subscriber sayısına göre register/unregister yapar
    Veriyi throttle + smoothing sonrası dağıtır
    GL engine'e snapshot/event queue üzerinden aktarır
```

### 38.2 Subscriber modeli

```kotlin
enum class SensorConsumerType {
    LIVE_WALLPAPER,
    DETAIL_PREVIEW,
    FULLSCREEN_PREVIEW
}

data class SensorSubscription(
    val id: String,
    val type: SensorConsumerType,
    val maxHz: Int,
    val priority: Int
)
```

### 38.3 Aktif consumer önceliği

```text
Öncelik:
1. LIVE_WALLPAPER visible
2. APP_FULLSCREEN_PREVIEW visible
3. APP_DETAIL_PREVIEW visible
4. Hiçbiri yoksa sensör kapalı
```

Aynı anda live wallpaper ve preview çalışıyorsa:

```text
Live wallpaper görünür değilse preview sensör alabilir.
Live wallpaper görünürse live wallpaper birincil tüketicidir.
Foreground fullscreen preview açık ve live wallpaper görünür değilse preview birincil tüketici olur.
Detail preview sensörü sadece kullanıcı preview ekranındayken ve parallax açıkken alır.
Katalog grid sensör almaz.
```

### 38.4 Throttle ve smoothing

```kotlin
class SensorDispatcher(
    private val sensorManager: SensorManager
) : SensorEventListener {

    private var lastEmitNanos: Long = 0L
    private var targetIntervalNanos: Long = 16_666_666L // 60Hz
    private val latest = MutableStateFlow(ParallaxInput.Zero)

    val parallaxFlow: StateFlow<ParallaxInput> = latest

    override fun onSensorChanged(event: SensorEvent) {
        val now = event.timestamp
        if (now - lastEmitNanos < targetIntervalNanos) return

        lastEmitNanos = now
        val raw = mapSensorToParallax(event)
        val smoothed = smooth(raw)
        latest.value = smoothed
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
```

### 38.5 Pil kuralları

```text
Visible consumer yoksa unregister.
Battery saver'da maxHz düşürülür.
Idle durumda sensör kapatılabilir veya 5-15Hz'e düşer.
Parallax disabled ise sensör hiç açılmaz.
Sensor yoksa parallax graceful disable olur.
Subscription lifecycle açıkça kapatılır; preview dispose olduğunda subscriber unregister edilir.
WallpaperRenderController visible=false olduğunda service subscription pasiflenir.
```

---

## 39. MVVM ↔ Engine State Bridge

UI ve engine farklı hızlarda ve farklı thread'lerde çalışır. UI state doğrudan layer'a dokunmamalıdır. Sürekli durumlar `StateFlow`, tek seferlik olaylar `SharedFlow` üzerinden EngineController'a aktarılır.

### 39.1 Sürekli durumlar: StateFlow

StateFlow kullanılacak alanlar:

```text
selectedWallpaperId
qualityPreference
parallaxEnabled
parallaxStrength
batterySaverMode
entitlementState
assetPackState
runtimeProfile
```

### 39.2 Tek seferlik olaylar: SharedFlow

SharedFlow kullanılacak alanlar:

```text
setWallpaperClicked
previewOpened
previewClosed
touchEvent
screenOn
screenOff
userPresent
retryAssetDownload
forceReloadScene
```

### 39.3 Engine command modeli

```kotlin
sealed class EngineCommand {
    data class LoadScene(val wallpaperId: String) : EngineCommand()
    data class ApplySettings(val settings: RuntimeSettingsSnapshot) : EngineCommand()
    data class DispatchEvent(val event: WallpaperEvent) : EngineCommand()
    data class SetVisibility(val visible: Boolean) : EngineCommand()
    data object ReleaseScene : EngineCommand()
}
```

### 39.4 EngineController

```kotlin
class EngineController(
    private val settingsRepository: SettingsRepository,
    private val assetPackRepository: AssetPackRepository,
    private val engineEventQueue: EngineEventQueue,
    private val scope: CoroutineScope
) {
    fun bind() {
        scope.launch {
            settingsRepository.settingsFlow
                .distinctUntilChanged()
                .collectLatest { settings ->
                    engineEventQueue.offer(
                        EngineCommand.ApplySettings(settings.toRuntimeSnapshot())
                    )
                }
        }

        scope.launch {
            assetPackRepository.stateFlow
                .collectLatest { packState ->
                    engineEventQueue.offer(
                        EngineCommand.DispatchEvent(
                            WallpaperEvent.AssetPackStateChanged(packState)
                        )
                    )
                }
        }
    }

    fun sendOneShot(event: WallpaperEvent) {
        engineEventQueue.offer(EngineCommand.DispatchEvent(event))
    }
}
```

### 39.5 Coalescing kuralı

Aynı frame içinde çok fazla state güncellemesi gelirse engine hepsini tek tek işlememelidir.

```text
Quality setting:
    latest wins

Parallax input:
    latest snapshot wins

Touch event:
    ordered delivery

Wallpaper load:
    cancel previous load, latest wins

Asset download state:
    ordered but UI throttled
```

### 39.6 Thread güvenliği

```text
Repository/ViewModel:
    Flow üretir

EngineController:
    Flow toplar, immutable snapshot üretir

EngineEventQueue:
    GLThread'e command taşır

RuntimeScene:
    Sadece GLThread'de mutasyona uğrar

Lifecycle:
    EngineController bind işlemi RuntimeSession scope'una bağlıdır
    Preview kapanınca collector job'ları cancel edilir
    Wallpaper service destroy olduğunda flow collection durur
    SharedFlow event buffer taşarsa state olmayan inputlar drop policy ile sınırlandırılır
```

---

## 40. Render Telemetry ve Non-Fatal Diagnostics

Fallback-first mimari kullanıcıyı crash'ten korur; ancak geliştirici tarafında sessiz hatalar görünmez kalmamalıdır. Bu yüzden fallback tetikleyen her önemli olay telemetry sistemine düşük maliyetle raporlanır.

### 40.1 Telemetry event türleri

```kotlin
sealed class RenderTelemetryEvent {
    data class ShaderCompileFailed(
        val shaderRef: String,
        val glRenderer: String,
        val logHash: String
    ) : RenderTelemetryEvent()

    data class AssetMissing(
        val wallpaperId: String,
        val pathHash: String,
        val assetPack: String?
    ) : RenderTelemetryEvent()

    data class FallbackActivated(
        val wallpaperId: String,
        val fallbackType: String,
        val reason: String
    ) : RenderTelemetryEvent()

    data class ContextRestoreFailed(
        val wallpaperId: String,
        val glRenderer: String
    ) : RenderTelemetryEvent()

    data class FrameBudgetExceeded(
        val wallpaperId: String,
        val p95FrameMs: Float,
        val qualityTier: QualityTier
    ) : RenderTelemetryEvent()

    data class ThermalEmergencyDegrade(
        val wallpaperId: String,
        val thermalStatus: Int,
        val appliedSceneMaxFps: Int
    ) : RenderTelemetryEvent()
}
```

### 40.2 TelemetryLogger

```kotlin
interface RenderTelemetryLogger {
    fun log(event: RenderTelemetryEvent)
}

class QueuedRenderTelemetryLogger(
    private val crashReporter: CrashReporter,
    private val scope: CoroutineScope
) : RenderTelemetryLogger {
    private val events = Channel<RenderTelemetryEvent>(
        capacity = 64,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    init {
        scope.launch(Dispatchers.Default) {
            for (event in events) {
                if (!shouldReport(event)) continue
                crashReporter.recordNonFatal(event)
            }
        }
    }

    override fun log(event: RenderTelemetryEvent) {
        events.trySend(event)
    }
}

interface CrashReporter {
    fun recordNonFatal(event: RenderTelemetryEvent)
}

class CrashlyticsCrashReporter : CrashReporter {
    override fun recordNonFatal(event: RenderTelemetryEvent) {
        Firebase.crashlytics.setCustomKey("lumisky_event", event::class.simpleName ?: "unknown")
        Firebase.crashlytics.setCustomKey("lumisky_wallpaper", event.wallpaperIdOrUnknown())
        Firebase.crashlytics.recordException(RenderNonFatalException(event.safeMessage()))
    }
}
```

### 40.3 Rate limiting

Telemetry render loop'u etkilememelidir.

```text
Render thread içinde network/Crashlytics çağrısı yok.
Event lightweight queue'ya atılır.
Background dispatcher raporlar.
Aynı shader/layer/wallpaper hatası session içinde dedupe edilir.
Saatlik/günlük rate limit uygulanır.
PII veya raw path/log gönderilmez; hash/safe metadata gönderilir.
Crashlytics yoksa noop logger kullanılır; engine telemetry bağımlılığı yüzünden crash etmez.
```

Session-local dedupe ve backoff zorunludur.

```kotlin
class TelemetryRateLimiter {
    private val seenThisSession = HashSet<String>()
    private val backoffByKey = HashMap<String, BackoffState>()

    fun shouldReport(event: RenderTelemetryEvent): Boolean {
        val key = event.dedupeKey()
        if (!seenThisSession.add(key)) return false
        val backoff = backoffByKey.getOrPut(key) { BackoffState.initial() }
        return backoff.allowNow()
    }

    fun onSendFailed(event: RenderTelemetryEvent) {
        backoffByKey[event.dedupeKey()] = backoffByKey
            .getOrPut(event.dedupeKey()) { BackoffState.initial() }
            .nextExponential()
    }
}
```

Kural:

```text
Aynı shader/layer/wallpaper hatası session içinde bir kez raporlanır.
Dış servis hatasında exponential backoff uygulanır.
Backoff state render thread'de tutulmaz.
Telemetry queue dolarsa en eski event düşer, render asla beklemez.
```

### 40.4 Gözlemlenmesi gereken fallback'ler

```text
Shader binary cache miss/fail
Shader compile fail
Missing texture
Texture decode fail
VideoOES init fail
Asset pack unavailable
Context restore fail
Unknown layer skipped
Scene fallback activated
Battery/thermal forced degradation
Frame budget p95 exceeded
```

---

## 41. Android OS Entegrasyon Sertleştirmeleri

Canlı wallpaper motoru sadece GL performansı ile production-ready sayılmaz. Android OS; sistem renkleri, display metrikleri, kilit ekranı/AOD davranışı ve thermal sinyalleri üzerinden engine'i doğrudan etkiler. Bu sinyaller render loop içinde tahmin edilmez; platform callback'leri controller katmanlarına normalize edilip `RuntimeSettingsSnapshot` / `RuntimeProfile` olarak GL Event Queue'ya aktarılır.

### 41.1 Material You ve WallpaperColors

Android 12+ dinamik tema ve launcher renkleri için wallpaper renkleri kontrollü şekilde sağlanmalıdır. `WallpaperService.Engine.onComputeColors()` çağrısı GL yüzeyinden pixel readback yapmamalıdır.

Kural:

```text
onComputeColors()
    ↓
WallpaperColorProvider.cachedOrCompute(definition)
    ↓
1. colorHints / primaryColors metadata varsa direkt WallpaperColors üret
2. metadata yoksa statik thumbnail decode et
3. BitmapPool ile küçük decode yap
4. WallpaperColors.fromBitmap(thumbnail) veya palette extractor kullan
5. sonucu wallpaperId + definitionVersion ile cache'le
    ↓
Android'e cached WallpaperColors döndür
```

GL surface, FBO veya live frame bu akışta okunmaz. Renk bilgisi wallpaper değiştiğinde veya metadata değiştiğinde güncellenir; engine gerekirse `notifyColorsChanged()` çağırır. Thumbnail decode render thread dışında yapılır; decode başarısızsa güvenli koyu renk fallback döner.

Şema alanı:

```json
"colorHints": {
  "primary": "#0A1024",
  "secondary": "#D96B45",
  "tertiary": "#6FC3FF",
  "supportsDarkText": false,
  "supportsDarkTheme": true,
  "source": "metadata"
}
```

### 41.2 DisplayMetricsController ve foldable/tablet geçişleri

Katlanabilir cihazlarda surface boyutu değişimi yalnızca `glViewport(width, height)` değildir. İç/dış ekran, tablet split-screen, rotation ve density değişimlerinde scene projection, parallax limitleri ve UI scale yeniden hesaplanmalıdır.

```text
DisplayMetricsController
    inputs:
        WallpaperService.Engine.getDisplayContext()
        onSurfaceChanged(width, height)
        onApplyWindowInsets()
        WindowMetrics / fold posture signal if available
    outputs:
        viewportSize
        aspectRatio
        density
        displayClass
        fov
        maxParallaxOffsetX/Y
        uiScale
        safeInsets
```

Kural:

```text
Surface changed:
    recompute DisplayProfile
    update camera projection matrix
    update parallax clamp
    update layer scale policy
    mark cached FBOs dirty if resolution bucket changed
    render one forced frame if visible
```

Foldable expanded mode'da `maxOffsetX` çoğu sahnede düşürülür; aksi halde geniş ekranda parallax çok erken clamp'e çarpar veya arka plan yatay uzamış görünür. `displayPolicy` şema alanı wallpaper bazlı override sağlar.

### 41.3 Ambient/AOD RuntimeProfile

AOD/kilit ekranı düşük güç durumlarında engine normal live wallpaper gibi davranmamalıdır. Amaç pil tüketimini ve OLED burn-in riskini düşürmektir.

Runtime profile genişletmesi:

```kotlin
enum class RuntimeProfile {
    APP_PREVIEW,
    LIVE_WALLPAPER,
    LOCK_SCREEN,
    AMBIENT_MODE
}
```

Ambient sinyali platform/launcher/Wear ortamında doğrudan callback olarak gelebilir; standart telefon runtime'ında ise `onVisibilityChanged`, display state, power/interactivity state, keyguard state ve OEM callback'leri `AmbientModeController` içinde tek profile çevrilir. Mimari `onAmbientModeChanged` benzeri callback'in her cihazda var olduğunu varsaymaz.

Ambient davranışı:

```text
Enter AMBIENT_MODE:
    stop continuous frame loop
    unregister sensors
    pause video layers
    pause particle/post-process layers
    draw one static low-luminance frame or glClear black
    optionally apply burn-in pixel shift every N seconds

Exit AMBIENT_MODE:
    restore previous RuntimeProfile
    recreate dirty GL resources if needed
    render one forced frame
```

`ambientPolicy` yoksa safe default siyah/çok düşük parlaklık frame'dir. High-contrast statik pikseller AOD'da uzun süre sabit bırakılmaz.

### 41.4 OS Thermal API ve acil kalite düşüşü

Thermal durum tahminle veya sadece periyodik polling ile yönetilmez. Android 10+ için `PowerManager.OnThermalStatusChangedListener` doğrudan `ThermalStateController` üzerinden `AdaptiveQualityController`'a bağlanır.

```kotlin
class ThermalStateController(
    private val powerManager: PowerManager,
    private val adaptiveQualityController: AdaptiveQualityController
) {
    private val listener = PowerManager.OnThermalStatusChangedListener { status ->
        adaptiveQualityController.onOsThermalStatusChanged(status)
    }

    fun start() {
        adaptiveQualityController.onOsThermalStatusChanged(powerManager.currentThermalStatus)
        powerManager.addThermalStatusListener(listener)
    }

    fun stop() {
        powerManager.removeThermalStatusListener(listener)
    }
}
```

Severe/critical davranışı:

```text
THERMAL_STATUS_SEVERE or worse:
    high-priority EngineCommand.ForceThermalDegrade
    sceneMaxFps = batterySaverSceneMaxFps
    disable particles
    disable post-process
    reduce FBO scale
    pause video if policy allows
    log RenderTelemetryEvent.ThermalEmergencyDegrade
```

Bu komut normal frame bütçesini beklemeden render/event kuyruğunda öncelikli işlenir. Thermal durum normale dönünce kalite birden yükseltilmez; hysteresis/cooldown uygulanır.

---

## 42. Feature Flag ve Remote Kill Switch

Bazı görsel özellikler cihaz/driver bazında sorun çıkarabilir. Bu yüzden riskli özellikler feature flag ile açılıp kapatılabilir olmalıdır.

### 42.1 Flag örnekleri

```kotlin
data class RenderFeatureFlags(
    val shaderBinaryCacheEnabled: Boolean,
    val videoOesEnabled: Boolean,
    val postProcessEnabled: Boolean,
    val fboCacheEnabled: Boolean,
    val astcEnabled: Boolean,
    val sensorParallaxEnabled: Boolean,
    val bitmapPoolEnabled: Boolean,
    val renderTelemetryEnabled: Boolean
)
```

### 42.2 Kullanım kuralı

```text
Feature flag engine'e RuntimeSettingsSnapshot içinde gelir.
Layerlar flag'i render loop içinde repository'den okumaz.
Remote config kullanılacaksa değerler snapshot'a çevrilip GL queue'ya aktarılır.
Remote kill switch offline durumda safe default değerlerle açılır.
Driver/device bazlı blacklist ShaderBinaryCache ve VideoOesLayer gibi riskli özellikleri kapatabilir.
```

---

## 43. v5 Taşıma Önceliği

v5 ile taşıma öncesi zorunlu liste güncellendi:

```text
1. Schema Migration
2. Fallback/Error Policy
3. Persistent State Model
4. Asset Pack State Machine
5. Migration Phases from Current App
6. ShaderBinaryCache capability + fallback
7. BitmapPool + decode memory policy
8. SensorDispatcher
9. StateFlow/SharedFlow Engine Bridge
10. Render Telemetry
11. WallpaperColors / Material You color cache
12. DisplayMetricsController / foldable adaptation
13. Ambient RuntimeProfile / AOD burn-in policy
14. PowerManager thermal listener emergency degradation
15. GLThread Looper/Choreographer ownership
16. SurfaceTexture GL-thread sync + Media3 lifecycle
17. PAD manifest hash/path validation
18. Shader warmup queue concurrency throttle
19. Telemetry session dedupe + exponential backoff
```

Bu 19 başlık tamamlandıktan sonra render engine taşıması çok daha güvenli olur.


## 44. Geliştirme Yol Haritası

### Faz 1 — Çekirdek data-driven hat

1. `CatalogDefinition`
2. `WallpaperDefinition`
3. `DefinitionValidator`
4. `SceneCompiler`
5. `LayerFactory`
6. `LayerRegistry` + `LayerCreateResult`
7. Basit `TextureLayer`
8. Basit `ShaderLayer`

Hedef:

```text
JSON → CompiledSceneDefinition → RuntimeScene → Direct render
```

### Faz 2 — Render lifecycle

1. `WallpaperService.Engine` bridge
2. `WallpaperGlThread`
3. `EglManager`
4. `Choreographer` frame clock
5. GLThread Looper ownership check
6. `GlResourceManager`
7. Context loss recovery
8. Shader binary capability detection
9. Shader warmup/preload policy
10. Shader warmup queue throttling
11. `WallpaperColorProvider` + `onComputeColors()` cache
12. `AmbientModeController`
13. `ThermalStateController`

### Faz 3 — Scheduler ve cache

1. `LayerFramePolicy`
2. `SceneScheduler`
3. `CachedLayerRenderer`
4. `FramebufferPool`
5. Battery/idle degradation
6. BitmapPool + decode memory policy

### Faz 4 — Parallax, atmosfer, event

1. `ParallaxController`
2. `AtmosphereController`
3. `SensorDispatcher`
4. `DisplayMetricsController`
5. `EngineEventQueue`
6. `EventTriggerSystem`
7. StateFlow/SharedFlow Engine Bridge
8. Unlock animation/video trigger

### Faz 5 — Video

1. `VideoOesLayer`
2. Media3/ExoPlayer preferred player adapter
3. `SurfaceTexture` lifecycle
4. OES shader
5. Frame-available sync flag/fence pattern
6. Video pause/resume/release
7. Freeze last frame

### Faz 6 — UI/preview/catalog

1. Thumbnail-only catalog
2. Stable UI model
3. Single active app preview
4. Fullscreen preview runtime profile
5. PAD asset status UI
6. PAD manifest hash/path validation

### Faz 7 — Performans ve release hazırlığı

1. Macrobenchmark
2. Baseline Profile
3. AGI frame profile
4. Overdraw/GPU inspection
5. Memory leak tests
6. Battery profiling
7. Render telemetry + non-fatal diagnostics
8. Feature flag / remote kill switch verification
9. AOD / lock-screen low-power verification
10. Thermal severe/critical degradation verification
11. Foldable/tablet viewport and parallax verification

---

## 45. Kaçınılması Gereken Hatalar

1. Her wallpaper için ayrı renderer yazmak.
2. Wallpaper sahnesini Kotlin `when(id)` ile kurmak.
3. GL texture/program/FBO ID'lerini singleton'da tutmak.
4. Render loop içinde JSON okumak.
5. Render loop içinde bitmap decode etmek.
6. Liste item içinde GL preview açmak.
7. Her layer'ı 30/60 FPS çalıştırmak.
8. Her layer için FBO kullanmak.
9. Back buffer içeriğinin korunacağını varsaymak.
10. Video texture'ı normal `GL_TEXTURE_2D` gibi ele almak.
11. `SurfaceTexture.updateTexImage()` çağrısını yanlış thread'de yapmak.
12. Görünmeyen wallpaper'da render/sensör/video devam ettirmek.
13. Remote shader/asset'i validation olmadan çalıştırmak.
14. FBO'yu performans çözümü sanmak.
15. Benchmark olmadan “akıcı” kabul etmek.
16. Shader binary cache'i zorunlu doğruluk kaynağı sanmak.
17. `BitmapPool`'u GPU compressed texture akışı için kullanmaya çalışmak.
18. Preview ve live wallpaper için ayrı ayrı sensör listener açmak.
19. Flow collector lifecycle'ını session kapanınca iptal etmemek.
20. Crashlytics/telemetry çağrısını render thread içinde yapmak.
21. `onComputeColors()` içinde GL surface/FBO pixel readback yapmak.
22. Foldable/tablet geçişlerini sadece viewport resize sanmak.
23. AOD/ambient durumda live render loop, video veya sensörleri çalıştırmak.
24. Severe/critical thermal durumunu sadece polling ile yakalamaya çalışmak.
25. GL render loop için Main Thread `Choreographer` referansını kullanmak.
26. `SurfaceTexture.updateTexImage()` çağrısını decoder/player callback thread'inde yapmak.
27. PAD absolute path'ini hash/permission doğrulamadan kalıcı cache kabul etmek.
28. Shader warmup için sınırsız parallel compile başlatmak.
29. Telemetry retry/backoff olmadan aynı hatayı sürekli dış servise göndermek.

---

## 46. Kaynaklar

- Android WallpaperService.Engine: https://developer.android.com/reference/android/service/wallpaper/WallpaperService.Engine
- Android WallpaperColors: https://developer.android.com/reference/android/app/WallpaperColors
- Android GLSurfaceView / Renderer: https://developer.android.com/reference/android/opengl/GLSurfaceView
- Android GLSurfaceView.Renderer: https://developer.android.com/reference/android/opengl/GLSurfaceView.Renderer
- Android SurfaceTexture: https://developer.android.com/reference/kotlin/android/graphics/SurfaceTexture
- Android Choreographer: https://developer.android.com/reference/android/view/Choreographer
- Android PowerManager: https://developer.android.com/reference/android/os/PowerManager
- AndroidX Media3: https://developer.android.com/media/media3
- Android BitmapFactory.Options: https://developer.android.com/reference/android/graphics/BitmapFactory.Options
- Android SensorManager: https://developer.android.com/reference/android/hardware/SensorManager
- OpenGL ES GLES30: https://developer.android.com/reference/android/opengl/GLES30
- Android Frame Pacing / Swappy: https://developer.android.com/games/sdk/frame-pacing
- Play Asset Delivery: https://developer.android.com/guide/playcore/asset-delivery
- Texture compression / Android App Bundles: https://developer.android.com/guide/playcore/asset-delivery/texture-compression
- Android game texture optimization: https://developer.android.com/games/optimize/textures
- Android reduce overdraw: https://developer.android.com/topic/performance/rendering/overdraw
- Macrobenchmark metrics: https://developer.android.com/topic/performance/benchmarking/macrobenchmark-metrics
- Baseline Profiles overview: https://developer.android.com/topic/performance/baselineprofiles/overview
- Android GPU Inspector: https://developer.android.com/agi
- Dagger multibindings: https://dagger.dev/dev-guide/multibindings.html
- Hilt dependency injection: https://developer.android.com/training/dependency-injection/hilt-android
- Kotlin StateFlow and SharedFlow: https://developer.android.com/kotlin/flow/stateflow-and-sharedflow
- Firebase Crashlytics custom keys: https://firebase.google.com/docs/crashlytics/customize-crash-reports
- Google Grafika repo: https://github.com/google/grafika

---

## 47. Nihai Kısa Özet

Lumisky v5 için en doğru yapı:

```text
Tek engine çekirdeği
JSON tabanlı wallpaper definition
SceneCompiler ile typed runtime model
Hilt/Dagger factory registry
Context-bound GL resource system
WallpaperService.Engine + custom GLThread
Looper-owned Choreographer frame clock
GLSurfaceView yalnızca app preview için
Choreographer frame clock
Layer scheduled rendering
Cached FBO final composite
VideoOesLayer özel pipeline
Media3/SurfaceTexture GL-thread sync
Thumbnail-first catalog
Play Asset Delivery asset strategy
PAD manifest hash/path validation
Macrobenchmark + AGI ile ölçülebilir performans
Shader binary cache ve warmup
Shader warmup queue throttling
BitmapPool ile kontrollü decode reuse
SensorDispatcher ile tek merkezli parallax input
StateFlow/SharedFlow ile UI-engine köprüsü
Render telemetry + non-fatal diagnostics
Telemetry session dedupe + exponential backoff
WallpaperColors / Material You metadata cache
DisplayMetricsController ile foldable/tablet uyumu
Ambient RuntimeProfile ile AOD/burn-in koruması
PowerManager thermal listener ile acil kalite düşüşü
```

Bu yapı hem sade kalır hem de 100+ wallpaper, shader/texture/video/animation/parallax/event desteğini motor kodunu sürekli değiştirmeden taşıyabilir.
