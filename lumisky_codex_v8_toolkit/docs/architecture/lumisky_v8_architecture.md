# Lumisky V4 — Production Recovery, Hibrit İçerik ve Geliştirme Planı v8

**Proje:** Lumisky Android Live Wallpaper  
**Hedef repo:** `cebe123/lumisky-v4`  
**Referans repo:** `cebe123/lumisky`  
**Temel mimari referansı:** `lumisky_mimari_v6(1).md`  
**Tarih:** 12 Temmuz 2026  
**v8 kapsamı:** Katmanlı görsel, hibrit animasyon, procedural efekt ve `VideoOesLayer` içerik hattı  

---

## 1. Amaç ve kesin kararlar

Bu planın amacı V4’ü eski Lumisky’ye geri çevirmek değildir. Amaç, V4’teki data-driven layer mimarisini, ana ekran animasyonunu, preview sistemini ve yeni özellikleri koruyarak eski Lumisky’nin akıcılığını, güvenilir konum davranışını ve düşük pil tüketimini geri kazandırmaktır.

Kesin kararlar:

1. **Big-bang rewrite yapılmayacak.** V6’daki strangler migration yaklaşımı uygulanacak.
2. **Tek GL owner** kuralı geçerli olacak; EGL ve bütün GL handle’ları yalnız render thread’de yaşayacak.
3. Main thread, Compose, sensör ve `WallpaperService.Engine` yalnız immutable `RenderCommand` üretecek.
4. Runtime mutable state hiçbir zaman uygulama-geneli singleton olmayacak.
5. JSON yalnız yükleme/derleme aşamasında okunacak; render hot path typed ve allocation-free olacak.
6. Yeni scene ilk başarılı frame’i üretmeden aktif ve “last successful” kabul edilmeyecek.
7. Konum sistemi eski Lumisky’deki last-known, foreground refresh, timezone ve fallback davranışlarını eksiksiz taşıyacak.
8. Görünmeyen wallpaper/preview için render, sensör, video ve scheduler işi sıfır olacak.
9. Katalog thumbnail-first kalacak; ancak ürün gereksinimi nedeniyle merkezdeki tek karta canlı preview verilecek.
10. Preview FPS sınırları:
    - **Fullscreen preview:** cihaz ve ekran kapasitesine göre **maksimum 120 FPS**.
    - **Katalog preview:** cihaz ve ekran kapasitesine göre **maksimum 60 FPS**.
    - FPS hiçbir zaman yalnız ekran yenileme hızı yüksek diye yükseltilmeyecek; sahne maliyeti, thermal durum, battery saver ve gerçek frame istikrarı birlikte değerlendirilecek.
11. Motor dört içerik ailesini typed olarak destekleyecek: `LAYERED_IMAGE`, `HYBRID`, `VIDEO` ve `PROCEDURAL`.
12. `HybridSceneRendererBackend`, tek bir monolitik “her şeyi yapan layer” yerine typed layer graph’ını yönetecek.
13. Video içerikler Media3/ExoPlayer → `Surface` → `SurfaceTexture` → `GL_TEXTURE_EXTERNAL_OES` hattında, render-thread ownership kurallarıyla çalışacak.
14. Tek görselden wallpaper üretimi runtime’da yapılmayacak; katman ayırma, inpainting, optimizasyon, thumbnail ve definition üretimi build-time/offline içerik pipeline’ında gerçekleştirilecek.
15. Kaynağı Instagram veya başka bir üreticiye ait olan içerik yalnız lisans/izin varsa kullanılacak; mimari teknik dönüşümü destekler fakat kullanım hakkı sağlamaz.

---

## 2. Yönetici özeti

V4’ün mimari yönü doğru, ancak runtime uygulamasında üretim seviyesini engelleyen kritik hatalar bulunuyor:

- `SceneScheduler` ve `ParallaxController` mutable state’leri session’lar arasında paylaşılabiliyor.
- Sensör örnekleri event/render fırtınası üretebiliyor.
- Zaman, celestial state, JSON uniform ve texture erişimleri render hot path içinde gereksiz CPU/GC yükü oluşturuyor.
- Layer FPS politikası çoğu layer için yalnız `update()` hızını düşürüyor; gerçek GPU draw sayısını düşürmüyor.
- Ana `sky` wallpaper layer mimarisine rağmen tek büyük continuous shader olarak çalışıyor.
- Preview frame-demand sistemi animasyon bittiğinde parallax değişimlerini kaçırabiliyor.
- Konum servisi kapalı olduğunda kayıtlı last-known konum kullanılmadan manuel moda düşülebiliyor.
- Texture upload, shader compile, EGL surface/context lifecycle ve first-frame commit hattında takılma ve fallback riskleri bulunuyor.

Bu plan önce correctness ve ownership hatalarını çözer; sonra ölçülebilir performans optimizasyonlarına geçer.

---

## 3. V6 planından korunan ve revize edilen kararlar

### 3.1 Aynen korunan kararlar

- Mevcut modüler yapı korunur; gereksiz yeni Gradle modülü açılmaz.
- `HandlerThread` korunur; manuel Looper rewrite yapılmaz.
- Command-only bridge ve render-thread confinement uygulanır.
- Surface ve EGL context lifecycle ayrılır.
- CPU definition ile GPU session resource’ları ayrılır.
- Katalog metadata ile ağır wallpaper definition ayrılır.
- Shared renderer backend ile live ve preview görsel eşdeğerliği sağlanır.
- V4’ün mevcut çalışan GLES yolu önce görsel parity ile korunur; GLES3 ana yol olabilir, sorunlu/uyumsuz cihazlar için capability-gated GLES2 veya sadeleştirilmiş fallback hazırlanır.
- Shader binary cache, PAD, BitmapPool ve genel FBO cache ölçüm-gated özelliklerdir.
- Transactional scene switch ve safe base-APK wallpaper zorunludur.
- Macrobenchmark yalnız app UI için; live service için Perfetto, AGI ve batterystats kullanılır.

### 3.2 Revize edilen V6 kararı: katalog preview

V6’daki şu hedef ürün gereksinimi nedeniyle değiştirildi:

```text
Catalog item active GL/video = 0
```

Yeni hedef:

```text
Scroll sırasında aktif GL preview = 0
Scroll durduktan sonra aktif katalog preview = en fazla 1
Katalog preview maksimum FPS = 60
Katalog preview varsayılan kalite = LOW/BALANCED
```

Katalog hâlâ thumbnail-first çalışır. GL renderer yalnız merkezdeki kart, scroll tamamen durduktan sonra ve warmup hazırsa lease alır.

---

## 4. Doğrulanan P0 kök nedenler

## 4.1 Session state’in singleton paylaşılması

### Sorun

`SceneScheduler` uygulama genelinde singleton olduğunda live wallpaper ile fullscreen/katalog preview aynı `sceneId:layerId` zaman anahtarını kullanabilir. Bir session diğerinin update/cache deadline’ını tüketebilir.

`ParallaxController` da mutable `currentX/currentY` alanlarıyla singleton olduğunda iki farklı GL thread aynı smoothing state’ini değiştirebilir.

### Sonuç

- Rastgele frame atlama
- Preview açıkken live wallpaper’ın düzensizleşmesi
- Layer’ların donmuş görünmesi
- Thread race ve tekrar üretmesi zor jitter

### Zorunlu düzeltme

Aşağıdaki bileşenler **her `RenderEngineSession` için ayrı instance** olmalıdır:

```text
SceneScheduler
FrameDemandController
ParallaxState / ParallaxController
SceneState
MutableRenderFrameState
CachedLayerRenderer
RuntimeScene
TelemetrySession
```

Singleton kalabilecekler yalnız stateless veya process-cache bileşenleridir:

```text
DefinitionParser
DefinitionValidator
Shader source text cache
Catalog repository
Settings repository
Sensor hardware dispatcher
Thermal state repository
Factory registry
```

---

## 4.2 Sensör kaynaklı event ve render fırtınası

### Sorun

`SENSOR_DELAY_GAME` ile gelen hemen her sensör örneği listener’a ve event kuyruğuna aktarılırsa saniyede onlarca gereksiz render isteği oluşur. Event backlog, GL thread CPU yükü ve GPU wakeup sayısı artar.

### Yeni sensör hattı

```text
Sensor hardware callback
    -> orientation remap
    -> low-pass filter
    -> dead-zone / epsilon
    -> rate limiter
    -> latest-value slot
    -> markDirty(PARALLAX)
```

### Oranlar

| Runtime | Sensör yayım üst sınırı |
|---|---:|
| Fullscreen preview | 60 Hz input, render policy ayrı |
| Katalog preview | 30 Hz input |
| Live wallpaper | 30 Hz input |
| Battery Saver | 8–15 Hz |
| Thermal severe | 5–10 Hz |
| Görünmez | 0 Hz |

Sensör hızı render FPS ile aynı olmak zorunda değildir. Render thread yalnız son örneği kullanır; eski sensör örnekleri kuyruğa birikmez.

### Filtre kuralları

- `epsilonX/Y`: cihaz testine göre yaklaşık `0.002–0.004`.
- Output smoothing: frame-rate bağımsız exponential smoothing.
- Aynı listener iki kez eklenemez; subscriber yapısı `MutableSet` veya token tabanlı olur.
- Aktif subscriber kalmadığında fiziksel sensör unregister edilir.
- Sensör callback’i doğrudan engine metodu veya EGL çağrısı yapamaz.

---

## 4.3 Hot path allocation ve wall-clock hesabı

### Sorun

Her frame `ZoneId`, `Instant`, `LocalTime`, `Calendar`, data class, `Vec2/Vec3`, `uppercase/lowercase` ve JSON nesnesi üretmek CPU ve GC baskısı yaratır.

### Yeni zaman modeli

```text
DATE/TIME/TIMEZONE/LOCATION değişimi
    -> CachedWallClockState güncellenir
    -> daylight ve timezone state yeniden çözülür

Frame loop
    -> monotonic delta ile animasyon zamanı ilerler
    -> gerçek saat yalnız minute tick veya clock eventinde resync edilir
```

### Hot path sözleşmesi

```text
Yeni app object allocation = 0 hedef
JSON erişimi = 0
Map/string policy lookup = 0
ZoneId parse = 0
Asset I/O = 0
Bitmap decode = 0
Shader compile = 0
Blocking repository call = 0
```

`CelestialMotionController` reusable `MutableCelestialState` içine yazar. Orbit tipi, wallpaper özel flag’leri ve uniform tipleri `SceneCompiler` aşamasında çözülür.

---

## 4.4 JSON ve string tabanlı shader parametreleri

### Sorun

`definition.uniforms` ve texture path listelerini her frame dolaşmak, `JsonArray/JsonPrimitive` okumak ve uniformları isim string’iyle çözmek V6’nın compiled runtime model şartını ihlal eder.

### Hedef pipeline

```text
WallpaperDefinition JSON
    -> migrate
    -> validate
    -> normalize
    -> capability resolution
    -> SceneCompiler
    -> CompiledWallpaperScene
    -> GL materialization
```

Örnek compiled yapı:

```kotlin
data class CompiledShaderBinding(
    val programKey: ProgramKey,
    val standardUniformLocations: StandardUniformLocations,
    val customFloatLocations: IntArray,
    val customFloatValues: FloatArray,
    val textures: Array<CompiledTextureBinding>,
)
```

Render sırasında JSON, asset path veya uniform string’i okunmaz.

---

## 4.5 Layer FPS’in gerçek draw FPS’ini azaltmaması

### Sorun

Bir layer `8 FPS` update edilse bile `cacheMode = NONE` ise scene 30/60 FPS çizildiğinde layer shader’ı her frame yeniden draw edilebilir. Bu durumda scheduler yalnız CPU state update’ini azaltır; GPU maliyeti değişmez.

### Yeni ayrım

```text
Update cadence
Render cadence
Cache refresh cadence
Final scene present cadence
```

Bunlar farklı kavramlardır.

### Hedef davranış

| Layer türü | Önerilen davranış |
|---|---|
| Statik texture | İlk frame/surface değişimi; doğrudan draw veya scene cache |
| Gündüz-gece atmosferi | Minute tick; catch-up sırasında geçici yüksek FPS |
| Bulut | 8–20 FPS; profiler doğrularsa half-res FBO |
| Güneş/ay | Normalde minute tick; preview time simulation’da scene FPS |
| Parallax foreground | Dirty-driven; sensör değiştiğinde |
| Video | External frame callback |
| Touch/event animasyonu | Animasyon süresince scene FPS |

FBO her düşük-FPS layer için zorunlu değildir. Yalnız pahalı, tekrar kullanılabilir ve profiler ile net kazanç gösteren pass’lerde kullanılmalıdır.

---

## 4.6 `sky` wallpaper’ın monolitik continuous shader olması

Layer mimarisi bulunsa da kale, foreground, gündüz/gece texture’ı, bulutlar, güneş/ay ve atmosfer tek fullscreen shader içinde çalışıyorsa layer scheduler’ın pil avantajı kullanılamaz.

### Önerilen hibrit Sky pipeline

```text
Pass 1 — Atmosphere/background
  minute-tick veya preview simulation cadence

Pass 2 — Clouds
  8–20 FPS, gerekirse half-resolution FBO

Pass 3 — Celestial
  minute-tick; preview/catch-up süresince yüksek cadence

Pass 4 — Foreground/castle
  parallax dirty veya day/night transition

Pass 5 — Optional glow/post effect
  quality ve thermal policy’ye bağlı
```

Ancak çok sayıda fullscreen pass de overdraw yaratabilir. Bu nedenle tek optimize shader ile hibrit pipeline AGI üzerinde A/B karşılaştırılmalı; en düşük gerçek GPU maliyeti seçilmelidir.

---

## 4.7 Preview frame-demand eksikliği

Preview animasyonu bittiğinde frame clock tamamen durursa, yalnız volatile parallax değerinin değişmesi yeni frame üretmeyebilir.

### Çözüm

```kotlin
enum class FrameDemandReason {
    INITIAL_FRAME,
    SURFACE_CHANGED,
    SCENE_SWITCH,
    TEXTURE_UPLOAD,
    PARALLAX_CHANGED,
    TOUCH_CHANGED,
    TIME_TICK,
    CLOUD_DEADLINE,
    VIDEO_FRAME,
    EVENT_ANIMATION,
    QUALITY_CHANGED,
    LOCATION_CHANGED,
}
```

`FrameDemandController`:

- dirty reason’ları bitmask olarak tutar,
- en yakın deadline’ı hesaplar,
- sürekli frame callback yerine gerektiğinde callback planlar,
- scene sabitse callback zincirini durdurur,
- parallax/touch/video geldiğinde tekrar uyandırır.

---

## 4.8 Konum davranışı gerilemesi

### Sorun

Konum servisi kapalıyken `lastLocation` okunmadan `null` dönmek ve kullanıcı seçimini otomatik `MANUAL` yapmak eski Lumisky davranışıyla uyumlu değildir.

### Doğru model

```kotlin
data class LocationLightingState(
    val selectedMode: LocationMode,
    val permissionState: PermissionState,
    val providerEnabled: Boolean,
    val liveLocation: LocationSnapshot?,
    val lastKnownLocation: LocationSnapshot?,
    val manualLocation: ManualLocationPreset,
    val resolvedLocation: ResolvedLocation,
    val daylight: DaylightState,
    val refreshState: LocationRefreshState,
)
```

**Seçili mod**, **provider durumu** ve **gerçekte kullanılan kaynak** ayrı state’lerdir.

Örnek:

```text
selectedMode = DEVICE
providerEnabled = false
resolvedSource = LAST_KNOWN
```

GPS kapalı diye kullanıcı seçimi manuel moda çevrilmez.

---

## 5. Hedef runtime mimarisi

```text
Compose / WallpaperService / Sensors / Broadcasts
                    │
                    ▼
           immutable RenderCommand
                    │
                    ▼
          RenderCommandMailbox
       FIFO + latest-wins coalescing
                    │
                    ▼
            RenderController
      thread startup + lifecycle bridge
                    │
                    ▼
       RenderSession (thread-owned)
       ├── RenderSessionState
       ├── FrameDemandController
       ├── SceneScheduler
       ├── ParallaxState
       ├── EglSession
       ├── SessionGlResources
       ├── SceneRendererBackend
       └── TelemetrySession
                    │
                    ▼
        CompiledWallpaperScene
                    │
          ┌─────────┴─────────┐
          ▼                   ▼
 LegacyRendererBackend   LayeredRendererBackend
```

### Command coalescing

FIFO zorunlu:

```text
AttachSurface
DetachSurface
One-shot Trigger
Stop
```

Latest-wins:

```text
ResizeSurface
SetParallax
SetTouch
SetPerformancePolicy
ThermalChanged
PowerChanged
LocationChanged
ApplyScene (hazırlama başlamadıysa)
```

---

## 6. Adaptif FPS ve kalite sistemi

## 6.1 Runtime profilleri

```kotlin
enum class RuntimeMode {
    LIVE_WALLPAPER,
    PREVIEW_FULLSCREEN,
    PREVIEW_CATALOG,
}
```

```kotlin
data class RuntimeProfile(
    val mode: RuntimeMode,
    val absoluteMaxFps: Int,
    val preferredFpsSteps: IntArray,
    val maxRenderScale: Float,
    val allowHighRefresh: Boolean,
    val allowSensorParallax: Boolean,
    val allowPostProcess: Boolean,
)
```

### Profil sınırları

| Profil | Mutlak üst sınır | Tipik başlangıç |
|---|---:|---:|
| Live wallpaper | 30 FPS varsayılan; özel scene/device ile 60 opsiyonel | 30 |
| Fullscreen preview | **120 FPS** | 60 |
| Katalog preview | **60 FPS** | 30 |

Live wallpaper pil odaklıdır. Fullscreen preview kullanıcı aktif olarak uygulama içindeyken daha yüksek akıcılık sağlayabilir.

---

## 6.2 Cihaz kapasitesi statik tahmin yerine iki aşamada çözülür

### Aşama A — Güvenli başlangıç tahmini

Kullanılabilecek sinyaller:

- `ActivityManager.isLowRamDevice`
- memory class
- desteklenen GLES sürümü ve extension’lar
- maksimum texture size
- display refresh rate ve desteklenen display mode’lar
- mevcut thermal status
- Power Saver
- surface pixel sayısı
- scene complexity metadata

Üretici/model adıyla sabit whitelist/blacklist ana karar mekanizması olmayacaktır.

### Aşama B — Runtime adaptasyon

İlk birkaç saniyede rolling pencere tutulur:

```text
CPU submission p50/p95
frame deadline miss ratio
swap interval stability
thermal trend
UI jank / scroll state
scene complexity
```

GLES3 + uygun extension varsa GPU timer query deneysel sinyal olabilir; desteklenmiyorsa CPU submission GPU zamanı olarak yorumlanmaz.

---

## 6.3 Preview FPS basamakları

### Fullscreen preview

Mutlak sınır: **120 FPS**.

Aday basamaklar display’e göre pacing-compatible seçilir:

| Display | Önerilen basamaklar |
|---|---|
| 60 Hz | 60, 30, 20, 15 |
| 90 Hz | 90, 45, 30, 15 |
| 120 Hz | 120, 60, 40, 30, 24, 20, 15 |
| 144 Hz | 120, 72, 48, 36, 24, 18 |

144 Hz ekranda ürün üst sınırı 120 olduğu için 144 kullanılmaz. Gerçek present düzeni stabil olan en yakın basamak seçilir.

### Katalog preview

Mutlak sınır: **60 FPS**.

| Display | Önerilen basamaklar |
|---|---|
| 60 Hz | 60, 30, 20, 15 |
| 90 Hz | 45, 30, 15 |
| 120 Hz | 60, 40, 30, 20, 15 |
| 144 Hz | 48, 36, 24, 18 |

Katalog için 90 Hz ekranda 60 FPS zorlamak yerine daha stabil 45 FPS tercih edilir.

---

## 6.4 FPS yükseltme ve düşürme kuralları

### Hızlı düşürme

Aşağıdakilerden biri oluşursa bir veya birden fazla basamak anında düşürülür:

- arka arkaya 4–6 deadline miss,
- rolling p95 frame süresinin bütçeyi aşması,
- thermal status `MODERATE` üstü,
- Power Saver açılması,
- katalog parent/row scroll başlaması,
- texture upload veya shader warmup sırasında frame spike,
- UI jank oranının artması.

### Yavaş yükseltme

FPS yalnız şu şartlar birlikte sağlanırsa bir basamak yükseltilir:

- en az 3–5 saniye stabil çalışma,
- deadline miss oranı belirlenen eşiğin altında,
- thermal status normal/light,
- Power Saver kapalı,
- scene resource warmup tamamlanmış,
- renderer görünür ve kullanıcı aktif,
- katalogda scroll tamamen durmuş.

Bu hysteresis FPS’in sürekli 60↔120 veya 30↔60 arasında zıplamasını engeller.

### Örnek governor

```kotlin
class AdaptiveFrameRateGovernor(
    private val profile: RuntimeProfile,
    private val displayModes: DisplayModeSnapshot,
) {
    fun resolveTarget(
        capability: DeviceCapability,
        runtime: RuntimeHealthSnapshot,
        scene: CompiledSceneCost,
    ): FrameRateDecision
}
```

`FrameRateDecision` yalnız FPS değil şunları da içerir:

```kotlin
data class FrameRateDecision(
    val targetFps: Int,
    val renderScale: Float,
    val qualityTier: QualityTier,
    val postProcessEnabled: Boolean,
    val particleEffectsEnabled: Boolean,
    val sensorRateHz: Int,
    val reason: DecisionReason,
)
```

---

## 6.5 Thermal ve Power Saver davranışı

| Durum | Fullscreen preview | Katalog preview | Live wallpaper |
|---|---:|---:|---:|
| Normal / güçlü cihaz | 60–120 | 30–60 | 30, scene uygunsa 60 |
| Normal / orta cihaz | 45–60 | 30–45 | 24–30 |
| Normal / düşük cihaz | 24–30 | 15–30 | 15–24 |
| Power Saver | 15–30 | 10–20 | 8–15 |
| Thermal moderate | üst basamaktan düş | 15–30 | 10–20 |
| Thermal severe | 15–24, düşük scale | 10–15 | 5–12 |
| Görünmez | 0 | 0 | 0 |

`PowerManager.isPowerSaveMode` her frame okunmaz; state değişimi flow/command olarak gelir.

---

## 6.6 Render scale ve kalite birlikte yönetilir

FPS tek başına düşürülmemelidir. Bazı ağır sahnelerde 120 FPS için önce render scale düşürmek daha iyi olabilir.

Örnek sıralama:

```text
1. Optional post-process kapat
2. Particle/star density azalt
3. Heavy pass resolution düşür
4. Render scale düşür
5. FPS basamağını düşür
```

Ancak UI/ürün kalitesine göre bu sıra scene metadata ile değişebilir.

Önerilen preview scale sınırları:

| Profil | Scale aralığı |
|---|---|
| Fullscreen preview | 0.65–1.0 |
| Katalog preview | 0.35–0.75 |
| Live wallpaper | 0.5–1.0 |

Katalog kartında fiziksel surface boyutu zaten küçük olduğu için ayrıca tam ekran çözünürlüklü FBO oluşturulmaz.

---

## 7. Katalog preview lease mimarisi

## 7.1 Temel akış

```text
Thumbnail her zaman görünür
    -> scroll başladı
       -> aktif lease iptal
       -> preview FPS = 0
    -> scroll tamamen durdu
       -> 250–400 ms debounce
       -> merkez kart seçildi
       -> resource warmup
       -> tek PreviewLease verildi
       -> ilk başarılı swap
       -> thumbnail -> GL preview crossfade
```

### Zorunlu kurallar

- Uygulama genelinde aynı anda en fazla **bir katalog preview renderer**.
- Parent `LazyColumn` veya ilgili `LazyRow` scroll ediyorsa renderer mount edilmez.
- Kart merkezden çıkınca lease iptal edilir.
- Yeni lease verilmeden önce önceki renderer’ın surface/thread/resource release süreci tamamlanır.
- Katalog preview maksimum **60 FPS**.
- Katalog preview yüksek FPS’e yalnız scroll sonrası stabilite kanıtlandığında çıkar.
- Katalog preview sensörü maksimum 30 Hz örnekler; 60 FPS render smoothing ile üretilebilir.
- Thumbnail cache byte-size tabanlı LRU olur.
- Kart badge zamanı ortak minute ticker’dan gelir; her kart `Calendar.getInstance()` çağırmaz.

### Önerilen API

```kotlin
interface PreviewLeaseCoordinator {
    fun request(request: PreviewLeaseRequest): PreviewLeaseResult
    fun release(ownerKey: String)
    val activeLease: StateFlow<PreviewLease?>
}
```

---

## 8. Fullscreen preview mimarisi

Fullscreen preview:

- aynı shared backend’i kullanır,
- cihaz ve sahne uygunsa 120 FPS’e kadar çıkar,
- başlangıçta konservatif 60 FPS ile açılır,
- texture/shader warmup sırasında FPS yükseltmez,
- lifecycle `STOPPED` olduğunda render ve sensörü tamamen kapatır,
- ilk başarılı `eglSwapBuffers` sonrası thumbnail’dan GL surface’e crossfade yapar,
- day/night simulation ve event animasyonlarını destekler,
- gerçek live wallpaper state’ini değiştirmez.

### First-frame doğruluğu

UI’ya “ilk frame hazır” sinyali yalnız başarılı swap sonrası gönderilir. Day progress callback sayısı first-frame ölçütü olarak kullanılamaz.

```kotlin
data class PresentedFrame(
    val frameNumber: Long,
    val presentedAtNanos: Long,
)
```

---

## 9. Live wallpaper performans politikası

Live wallpaper için ana hedef maksimum FPS değil minimum enerjiyle yeterli akıcılıktır.

### Varsayılan

- Scene max: 30 FPS.
- Çok hafif ve açıkça izin verilmiş scene + güçlü cihaz: 60 FPS opsiyonel.
- Parallax aktif hareket: maksimum 30 FPS varsayılan.
- Cihaz sabit ve yalnız bulut hareketi: 8–20 FPS.
- Minute-tick scene: sürekli loop yok.
- Görünmez: 0 FPS.

### High refresh ayarı düzeltmesi

`highRefreshEnabled == true` scene’in 30 FPS limitini otomatik 60’a çıkarmamalıdır.

```kotlin
resolvedFps = minOf(
    profile.absoluteMaxFps,
    scene.maxFps,
    displayCompatibleFps,
    capabilityLimit,
    thermalLimit,
    batteryLimit,
)
```

---

## 10. Konum ve daylight migration planı

## 10.1 Kaynak önceliği

```text
Persist edilmiş uygun snapshot
    -> Fused getLastLocation
    -> provider açık ve stale ise getCurrentLocation
    -> manual fallback
```

`getLastLocation` provider kapalı olsa bile izin varsa denenebilir. Snapshot yaşı uygulama tarafından değerlendirilir.

### Yaş sınıfları

| Yaş | Kullanım |
|---|---|
| < 1 saat | Fresh |
| 1–24 saat | Valid |
| 1–7 gün | Stale but usable; background refresh |
| > 7 gün | Last-resort; UI’da belirt |
| Veri yok | Manual fallback |

Güneş saatleri için 15 dakikalık sert sınır gereksizdir; kullanıcı şehir değiştirmediyse daha eski konum hâlâ işlevseldir.

## 10.2 DEVICE tercihi korunur

- Provider kapalıysa `selectedMode = DEVICE` korunur.
- UI “Son bilinen konum kullanılıyor” gösterir.
- Provider tekrar açıldığında tek current request yapılır.
- Kullanıcı açıkça manual seçmeden mod değişmez.

## 10.3 Current location isteği

Yalnız:

- DEVICE moduna ilk geçiş,
- kullanıcı manuel yenileme,
- foreground dönüşte stale snapshot,
- provider yeniden açılması,
- anlamlı konum değişimi

durumlarında yapılır.

Request’ler throttle ve cancellation destekler.

## 10.4 Reverse geocoder

Reverse geocoder sonucu daylight hesabını bloklamaz.

```text
Coordinates geldi
  -> daylight hemen hesapla
  -> snapshot kaydet
  -> label async çöz
  -> label state’i daha sonra güncelle
```

Label geohash/grid anahtarıyla cache’lenir.

## 10.5 Timezone

Sıralama:

1. Koordinatla eşleşen daha önce çözülmüş timezone.
2. Offline coordinate-to-timezone resolver.
3. Cihaz timezone’u.
4. Güvenli fallback.

Cihaz başka ülkedeyken yalnız `TimeZone.getDefault()` kullanmak yanlış sunrise/sunset üretebilir.

## 10.6 Yeniden hesaplama eventleri

```text
ACTION_DATE_CHANGED
ACTION_TIME_CHANGED
ACTION_TIMEZONE_CHANGED
local midnight
foreground start
manual location change
new device location
provider state change
```

Daylight sonucu `WallpaperEvent.DaylightChanged` veya `RenderCommand.LocationChanged` ile GL session’a iletilir ve ilgili dirty reason’lar işaretlenir.

## 10.7 Polar durumlar

```kotlin
sealed interface SolarDayState {
    data class Normal(...) : SolarDayState
    data object PolarDay : SolarDayState
    data object PolarNight : SolarDayState
    data class Fallback(...) : SolarDayState
}
```

Null sonucu doğrudan 06:00–18:00 yapmak yerine polar state modellenir.

---

## 11. EGL ve lifecycle hardening

## 11.1 State machine

```text
Empty
  -> ContextReady
  -> SurfaceReady
  -> SurfaceLost / ContextReady
  -> SurfaceReady
  -> ContextLost
  -> ContextReady
  -> Released
```

Surface kaybı context kaybı değildir.

```kotlin
fun detachWindowSurface(retainContext: Boolean)
fun releaseAll(contextLost: Boolean)
```

### Kurallar

- `onContextCreated()` yalnız yeni EGL context üretildiğinde çağrılır.
- Surface yeniden oluştuğunda aynı context korunmuşsa yalnız surface attach/resize yapılır.
- Context lost ise eski GL ID’lerine `glDelete` uygulanmaz; Java/Kotlin handle’ları sıfırlanır.
- `eglSwapBuffers` sonucu kontrol edilir.
- `EGL_CONTEXT_LOST` ve `EGL_BAD_SURFACE` ayrı recovery yollarına gider.
- `onSurfaceChanged` viewport, aspect, parallax clamp ve FBO invalidation transaction’ını tetikler.
- FBO cache key’i width/height/quality/format içerir.

## 11.2 GLES stratejisi

- Mevcut çalışan baseline hangi GLES sürümüyse önce parity o sürümde sağlanır.
- GLES3 capability probe ile açılır.
- GLES3 context creation başarısızsa güvenli GLES2 fallback yapılır.
- ES3-only scene için basitleştirilmiş fallback tanımlanır.

---

## 12. Texture, shader ve asset pipeline

## 12.1 Texture decode

- `InputStream.use` zorunlu.
- Bounds decode ile hedef boyut belirlenir.
- Decode queue bounded olur.
- Aynı asset için in-flight request deduplication uygulanır.
- Scene generation değişince eski decode sonucu discard edilir.
- Upload sonrası CPU bitmap hızlı bırakılır.
- Path string’i yerine `TextureHandle` kullanılır.

## 12.2 Texture upload bütçesi

`GLUtils.texImage2D` büyük texture için frame spike üretebilir. Upload işleri:

- kritik ve opsiyonel olarak sınıflandırılır,
- görünür ilk frame’den önce kritik set hazırlanır,
- opsiyonel upload frame bütçesine bölünür,
- tek frame’e büyük transfer yığılmaz.

## 12.3 Scene resource cache

Scene switch sırasında bütün texture pool’u kör biçimde temizlemek yerine reference counting/generation uygulanır. Ortak assetler tekrar decode/upload edilmez.

## 12.4 Shader source ve program

- Shader source metni process cache’inde tutulabilir.
- GL program ID yalnız EGL session’da yaşar.
- Uniform location’ları program init sırasında çözülür.
- Compile görünür hot frame’e bırakılmaz.
- Compile/link failure current scene’i bozmaz.

## 12.5 Program binary cache

Yalnız GLES3 ve ölçüm sonrası:

```text
source hash
+ renderer version
+ GL_VENDOR
+ GL_RENDERER
+ GL_VERSION
+ binary format
+ app version
```

Binary yükleme başarısızsa normal compile fallback yapılır.

---

## 13. Transactional scene activation

```text
Selection request
  -> entitlement
  -> asset availability
  -> definition load/migrate/validate
  -> CPU compile
  -> critical asset prepare
  -> GL program/texture creation
  -> validation render
  -> first successful swap
  -> atomic active scene commit
  -> selected/lastSuccessful persistence
```

Hata durumunda:

```text
Optional layer failure -> layer disable + telemetry
Required layer failure -> candidate reject
Candidate reject -> current scene korunur
Restore failure -> last successful
Son fallback -> base APK safe wallpaper
```

GL init tamamlanmadan “last successful” yazılmaz.

---

## 14. Ana ekran ve Compose optimizasyonları

- `collectAsStateWithLifecycle` kullanılmalı.
- Katalog UI model immutable ve stable olmalı.
- `LazyColumn/LazyRow` key/contentType doğru tutulmalı.
- Thumbnail decode hedef piksel boyutuna göre yapılmalı.
- Shared minute ticker dışında kart başına zaman objesi üretilmemeli.
- GL preview yalnız lease sahibi kartta mount edilmeli.
- Scroll sırasında blur, shadow ve pahalı chrome efektleri azaltılabilir.
- Preview callbackleri Compose state’ini her render frame değiştirmemeli.
- Day-progress badge callback’i 250–500 ms aralığında olabilir; renderer kendi 60/120 FPS döngüsünü UI recomposition’a bağlamaz.
- Baseline Profile app launch, katalog, detail ve fullscreen preview akışlarını kapsamalı.

---

## 15. Observability ve ölçüm

### Structured eventler

```text
EglFailure
ContextLost
SceneRejected
ShaderFailed
TextureDecodeFailed
TextureUploadSpike
QualityChanged
FrameRateChanged
FrameBudgetMiss
SensorRateChanged
LocationFallbackUsed
PreviewLeaseChanged
```

### Minimum runtime metrikleri

- runtime mode,
- active scene/backend,
- target/effective FPS,
- display refresh,
- render scale,
- CPU submission p50/p95/p99,
- deadline miss ratio,
- sensor callbacks/s ve accepted samples/s,
- frame request/draw/skip reasons,
- context/surface recreation count,
- texture decode/upload süreleri,
- memory estimates,
- thermal/power state,
- fallback reason.

Crashlytics’e her frame non-fatal gönderilmez; fingerprint rate limiting uygulanır.

---

## 16. Test stratejisi

## 16.1 Unit test

```text
RenderCommandReducerTest
RenderSessionIsolationTest
SceneSchedulerSessionKeyTest
ParallaxLatestWinsTest
SensorEpsilonFilterTest
FrameDemandControllerTest
AdaptiveFrameRateGovernorTest
DisplayCompatibleFpsTest
LocationFallbackPolicyTest
LocationRefreshThrottleTest
DefinitionMigrationTest
DefinitionValidatorTest
SceneCompilerTest
TransactionalSceneSwitchTest
FboResizeInvalidationTest
```

## 16.2 Integration/instrumentation

```text
LiveAndPreviewSameSceneIsolationTest
FullscreenPreview120FpsCapabilityTest
CatalogPreview60FpsCapabilityTest
CatalogScrollStopsPreviewTest
SingleCatalogPreviewLeaseTest
SurfaceCreateResizeDestroyRecreateTest
ContextLossRecoveryTest
VisibilityStopsAllWorkTest
PowerSaverTransitionTest
ThermalTransitionTest
GpsOffUsesLastKnownTest
ProviderReenabledRefreshTest
PreviewOpenCloseLeakTest
RapidWallpaperSwitchTest
```

## 16.3 Visual regression

Deterministic değerlerle:

```text
00:00
sunrise - 10 min
sunrise
noon
sunset
sunset + 20 min
parallax center/min/max
quality low/balanced/high
9:16, 9:20, tablet, landscape
```

Eski Lumisky, V4 legacy backend ve yeni layered backend arasında toleranslı image diff uygulanır.

---

## 17. Performans kabul kriterleri

## 17.1 Genel invariant’lar

| Metrik | Gate |
|---|---:|
| Görünmez render callback | 0 |
| Görünmez swapBuffers | 0 |
| Görünmez sensor subscriber | 0 |
| Render hot-path JSON parse | 0 |
| Render hot-path asset I/O | 0 |
| GL call render thread dışında | 0 |
| Aynı anda katalog preview renderer | <= 1 |
| Scroll sırasında katalog GL preview | 0 |
| Candidate failure current scene kaybı | 0 |
| Context loss sonrası stale handle | 0 |
| App-controlled steady-state allocation | 0 B hedef |

## 17.2 Fullscreen preview

- Mutlak üst sınır: **120 FPS**.
- 120 Hz güçlü cihaz + hafif scene: p95 frame time ≤ 8.33 ms hedef.
- 90 FPS: p95 ≤ 11.11 ms.
- 60 FPS: p95 ≤ 16.67 ms.
- Governor stabil değilse otomatik alt basamağa düşer.
- 120 FPS kaliteyi veya thermal stabiliteyi bozuyorsa zorlanmaz.

## 17.3 Katalog preview

- Mutlak üst sınır: **60 FPS**.
- Scroll sırasında 0 FPS/mounted renderer yok.
- 60 FPS için p95 ≤ 16.67 ms.
- Orta cihazda 30–45 FPS kabul edilir.
- Thumbnail’dan ilk canlı frame’e geçiş frame swap ile doğrulanır.

## 17.4 Live wallpaper

- Varsayılan üst sınır 30 FPS.
- Sabit sahnede continuous 30 FPS zorunlu değildir.
- Battery mode 8–15 FPS veya on-demand.
- Görünmez 30 dakikalık testte render/sensor işi sıfır.
- Eski Lumisky ile aynı cihaz/brightness koşulunda enerji tüketimi daha kötü olmamalı.

---

## 18. İçerik kaynak modeli ve wallpaper türleri

V8 ile wallpaper tanımı yalnız shader layer listesinden ibaret olmayacaktır. İçeriğin nasıl üretildiği ve hangi runtime backend’ine ihtiyaç duyduğu açıkça modellenecektir.

### 18.1 Desteklenen kaynak türleri

```kotlin
sealed interface WallpaperSource {
    val kind: WallpaperSourceKind

    data class LayeredImage(
        val baseAssetId: String?,
        val depthMapAssetId: String?,
    ) : WallpaperSource {
        override val kind = WallpaperSourceKind.LAYERED_IMAGE
    }

    data class Hybrid(
        val compositionId: String,
    ) : WallpaperSource {
        override val kind = WallpaperSourceKind.HYBRID
    }

    data class Video(
        val videoAssetId: String,
        val fallbackPosterAssetId: String,
    ) : WallpaperSource {
        override val kind = WallpaperSourceKind.VIDEO
    }

    data class Procedural(
        val shaderGraphId: String,
    ) : WallpaperSource {
        override val kind = WallpaperSourceKind.PROCEDURAL
    }
}
```

```kotlin
enum class WallpaperSourceKind {
    LAYERED_IMAGE,
    HYBRID,
    VIDEO,
    PROCEDURAL,
}
```

### 18.2 İçerik türü seçme kuralı

| Görsel davranış | Tercih edilen kaynak |
|---|---|
| Statik sahne, derinlik ve hafif kamera hareketi | `LAYERED_IMAGE` |
| Statik taban + sis, glow, parçacık, bulut, timeline | `HYBRID` |
| Kompleks karakter/kumaş/kamera hareketi | `VIDEO` |
| Tamamen matematiksel/generative efekt | `PROCEDURAL` |
| Video tabanı + interaktif ön plan/efekt | `HYBRID` içinde `VideoOesLayer` |

Karar ilkesi:

```text
Resim ve shader ile aynı kalite üretilebiliyorsa video kullanılmaz.
Hareketin layer’a ayrılması kaliteyi bozuyor veya üretim maliyetini aşırı yükseltiyorsa video kullanılır.
```

### 18.3 Runtime capability çözümü

Definition, istediği özellikleri bildirir; cihaz ve runtime profile etkin yolu seçer:

```text
Definition requirements
  + GL extensions
  + decoder capability
  + memory class
  + thermal/power state
  + runtime mode
  -> EffectiveScenePlan
```

Örnek fallback:

```text
HYBRID + video OES destekli      -> video + shader + overlay
HYBRID + decoder başarısız       -> poster + shader + overlay
PROCEDURAL ağır / low-tier       -> pre-rendered texture variant
Layered image memory yetersiz    -> düşük çözünürlük asset varyantı
```

### 18.4 Preview ve live için kaynak politikası

Aynı definition farklı runtime profillerinde farklı asset veya layer seçebilir:

| Runtime | Politika |
|---|---|
| Katalog thumbnail | Statik WebP; renderer yok |
| Katalog aktif preview | Tek lease; low/balanced asset; video en fazla kaynak FPS’i ve 60 FPS üst sınırı |
| Fullscreen preview | High asset; max 120 FPS compositor; video kare hızı yapay olarak 120’ye çoğaltılmaz |
| Live wallpaper | Pil öncelikli; genelde 15/24/30/45/60; source yeni frame üretmedikçe video update edilmez |

---

## 19. Hibrit sahne ve layer graph mimarisi

### 19.1 Kesin karar: monolitik `HybridLayer` yok

“Hybrid” kavramı tek bir dev layer olarak uygulanmamalıdır. Doğru yapı:

```text
HybridSceneRendererBackend
  -> CompiledLayerGraph
      -> TextureLayer
      -> ShaderEffectLayer
      -> ParticleLayer
      -> TimelineTransformLayer / animation tracks
      -> VideoOesLayer (opsiyonel)
      -> CompositePass
```

Gerekirse birden fazla layer’ı ortak transform, opacity veya clipping altında toplamak için hafif bir `LayerGroupNode` kullanılır. Bu node yeni bir görsel motor değildir; yalnız child state ve render pass sınırı sağlar.

### 19.2 Backend sözleşmesi

```kotlin
interface HybridSceneRendererBackend : SceneRendererBackend {
    fun applyCompiledGraph(graph: CompiledLayerGraph): SceneApplyResult
    fun onExternalFrameAvailable(layerId: String)
    fun trimMemory(level: Int)
}
```

```kotlin
data class CompiledLayerGraph(
    val orderedPasses: Array<CompiledRenderPass>,
    val layersByIndex: Array<CompiledLayer>,
    val animationPlan: CompiledAnimationPlan,
    val resourcePlan: SceneResourcePlan,
    val fallbackPlan: SceneFallbackPlan,
)
```

### 19.3 Temel layer seti

İlk production slice yalnız şu layer’larla başlamalıdır:

```text
TextureLayer
ShaderEffectLayer
ParticleLayer
VideoOesLayer
Color/GradientLayer
LayerGroupNode
```

İleri/deneysel:

```text
MeshWarpLayer
SpriteSequenceLayer
DepthMapWarpLayer
LutPostProcessLayer
Blur/Bloom multi-pass
```

Deneysel layer’lar ilk wallpaper migration’ını bloke etmemelidir.

### 19.4 Update ile draw ayrımı

Her layer şu iki kararı ayrı verir:

```text
needsUpdate(frame)  -> CPU/state değişmeli mi?
needsDraw(frame)    -> final frame’e tekrar çizilmeli mi?
```

Örnek:

```text
Static texture:
  update = yalnız init/resize
  draw = scene composite gerektiğinde

VideoOesLayer:
  update = yeni decoder frame’i varsa updateTexImage
  draw = yeni video frame’i, overlay/parallax veya full composite gerektiğinde

Fog shader:
  update = fixed 15 FPS
  draw = cache geçersizse veya final composite gerekiyorsa
```

### 19.5 Render pass modeli

```kotlin
enum class RenderPass {
    BACKGROUND,
    WORLD,
    ATMOSPHERE,
    SUBJECT,
    FOREGROUND,
    POST_PROCESS,
    UI_DEBUG,
}
```

Sıra:

```text
pass -> zIndex -> declarationOrder
```

Transparent layer’lar batching amacıyla yeniden sıralanmaz.

### 19.6 Animasyon saatleri

Tüm animation track’leri session-owned monotonic clock kullanır:

```kotlin
interface AnimationClock {
    val elapsedNanos: Long
    val deltaNanos: Long
    fun seekTo(progress: Float)
    fun pause()
    fun resume()
}
```

- `System.currentTimeMillis()` animasyon clock’u değildir.
- Preview time-lapse ile gerçek daylight clock ayrıdır.
- Scene pause/resume sonrası büyük delta clamp edilir.
- Timeline loop boundary’de seamless olmalıdır.

### 19.7 Dirty reason sistemi

```kotlin
enum class FrameDirtyReason {
    INITIAL_FRAME,
    SURFACE_RESIZE,
    SCENE_SWITCH,
    PARALLAX,
    TIMELINE,
    VIDEO_FRAME,
    PARTICLE_TICK,
    SHADER_TICK,
    DAYLIGHT_TICK,
    QUALITY_CHANGE,
    EXTERNAL_EVENT,
}
```

Frame scheduler bu nedenlerden en az biri varsa çalışır; görünmez durumda bütün nedenler bekletilir veya policy’ye göre discard edilir.

---

## 20. `VideoOesLayer` mimarisi

### 20.1 Veri akışı

```text
Media3 ExoPlayer
    -> android.view.Surface
    -> SurfaceTexture
    -> GL_TEXTURE_EXTERNAL_OES
    -> VideoOesLayer shader
    -> Hybrid compositor
```

### 20.2 Thread ownership

```text
Player/decoder callback thread:
  yalnız atomic frameAvailable = true
  markDirty(VIDEO_FRAME)

Render thread + current EGL context:
  if frameAvailable:
      surfaceTexture.updateTexImage()
      surfaceTexture.getTransformMatrix(reusableMatrix)
      frameAvailable = false
  draw OES texture
```

`updateTexImage()` callback thread’inde çağrılmaz. SurfaceTexture ve OES texture create/attach/detach/release işlemleri açık lifecycle state machine ile yönetilir.

### 20.3 Layer sözleşmesi

```kotlin
interface ExternalFrameLayer : RenderLayer {
    fun onFrameAvailable()
    fun hasPendingExternalFrame(): Boolean
}

class VideoOesLayer(
    private val mediaController: VideoMediaController,
    private val frameSignal: ExternalFrameSignal,
) : ExternalFrameLayer {
    // Bütün GL state render thread owner'dır.
}
```

### 20.4 Video lifecycle state’i

```kotlin
sealed interface VideoLayerState {
    data object Empty : VideoLayerState
    data object Preparing : VideoLayerState
    data object ReadyPaused : VideoLayerState
    data object Playing : VideoLayerState
    data object SurfaceLost : VideoLayerState
    data object ContextLost : VideoLayerState
    data class Failed(val reason: VideoFailure) : VideoLayerState
    data object Released : VideoLayerState
}
```

Kurallar:

- Wallpaper görünmez olduğunda player pause edilir; ürün policy’sine göre decoder tamamen release edilebilir.
- Katalog lease kaybolduğunda player ve Surface hızlı bırakılır.
- Fullscreen preview arka plana gittiğinde playback durur.
- Context loss sonrası yeni OES texture oluşturulur ve `SurfaceTexture` yeniden bağlanır veya tamamen yeniden kurulur.
- Audio track oynatılmaz; kaynak video mute/strip edilir.
- Seek/loop işlemleri frame callback ve generation token ile eski session’a kare sızdırmaz.

### 20.5 FPS gerçeği

30 FPS bir videoyu compositor’ı 120 FPS çalıştırarak gerçek 120 FPS video yapmak mümkün değildir.

```text
Video source FPS = 30
Fullscreen compositor = 120
```

Bu durumda:

- yeni video karesi 30 kez/s gelir,
- shader/parallax/overlay 120 FPS güncellenebilir,
- aynı decoder karesi gereksiz biçimde 4 kez `updateTexImage()` yapılmaz,
- yalnız final composite gerekiyorsa mevcut OES texture tekrar çizilir.

### 20.6 Codec ve kalite profili

Güvenli baseline:

```text
Container: MP4
Video: H.264/AVC
Audio: yok
Loop: 3–8 saniye tercih edilir
Color: SDR/sRGB baseline
```

Opsiyonel varyant:

```text
HEVC/H.265 -> capability-gated
HDR        -> ayrı ürün/cihaz testi
AV1        -> yalnız decoder coverage ölçüldüğünde
```

Runtime çözünürlüğü cihazın fiziksel ekranından kör biçimde alınmaz. Asset seçimi:

```text
surface size
+ render scale
+ device decoder capability
+ memory/thermal tier
-> video variant
```

Örnek varyantlar:

```text
720x1600  low
1080x2400 balanced/high
1440x3200 ultra, yalnız gerekli cihazlarda
```

### 20.7 Seamless loop

İçerik pipeline’ı şu üç yöntemden birini işaretler:

```text
NATIVE_LOOP       -> ilk ve son frame görsel olarak eşleşiyor
CROSSFADE_LOOP    -> kısa overlap/crossfade encode edilmiş
PING_PONG         -> yalnız ters oynatma görsel olarak kabul ediliyorsa
```

Runtime’da pahalı crossfade üretmek yerine mümkün olduğunca loop encode aşamasında hazırlanır.

### 20.8 Hata ve fallback

```text
Video prepare failure
  -> poster texture
  -> opsiyonel shader/particle layer devam eder
  -> telemetry

Required video + poster yok
  -> candidate scene reject
  -> current scene korunur
```

### 20.9 İçerik hakkı

Instagram, Pinterest veya başka bir üreticinin görsel/video paylaşımını indirmek teknik olarak mümkün olsa bile bu içerik üzerinde kullanım, yeniden dağıtım veya satış hakkı vermez. Uygulamaya yalnız:

- kendi ürettiğin,
- açık lisans koşullarına uyduğun,
- yazılı kullanım izni aldığın,
- ticari lisans satın aldığın

assetler eklenmelidir.

---

## 21. Katmanlı görsel ve mikro-animasyon sistemi

### 21.1 Katman rolleri

```text
BACKGROUND       uzak gökyüzü/duvar/şehir
FAR_MIDGROUND    dağ, uzak bina, bulut
MIDGROUND        dekor, ikinci karakter, ağaç
SUBJECT          ana karakter/nesne
FOREGROUND       çiçek, kaya, çerçeve, yakın parçalar
EFFECT_MASK      glow, fog, light, emission mask
DEPTH_MAP        opsiyonel continuous depth
```

Bir sahne için 3–8 ana görsel layer genellikle yeterlidir. Çok sayıda küçük layer draw call, alpha overdraw ve içerik bakım maliyetini artırır.

### 21.2 Transform animation

```kotlin
data class TransformState(
    var x: Float,
    var y: Float,
    var scaleX: Float,
    var scaleY: Float,
    var rotationDegrees: Float,
    var opacity: Float,
)
```

Desteklenecek track hedefleri:

```text
position.x / position.y
scale.x / scale.y / scale.uniform
rotation
opacity
uv.offset.x / uv.offset.y
shader.<compiledParameter>
particle.<compiledParameter>
```

İlk sürüm easing seti:

```text
linear
smoothstep
sine-in-out
cubic-in-out
hold
```

### 21.3 Parallax

Parallax, layer’ın kendi animasyonundan ayrı uygulanır:

```text
finalTransform = baseTransform
               + timelineTransform
               + parallaxTransform
               + eventTransform
```

Depth değeri normalize edilir:

```text
0.0 -> uzak arka plan
0.5 -> orta plan
1.0 -> yakın ön plan
```

Clamp ve safe overscan içerik pipeline’ında hesaplanır; hareket sırasında boş şeffaf alan görünmemelidir.

### 21.4 Micro-loop seçenekleri

Hafif hareketler için tercih sırası:

1. UV scroll veya shader noise
2. Transform timeline
3. Küçük sprite sequence
4. Sınırlı mesh warp
5. Video

Örnek:

```text
Bulut kayması      -> UV scroll / transform
Glow nefesi        -> shader param timeline
Sis                -> noise shader
Yıldız parlaması   -> particle/shader
Saç ucu hareketi   -> mesh warp veya küçük sprite loop
Kompleks karakter  -> video
```

### 21.5 `MeshWarpLayer` sınırı

Mesh warp ilk production MVP’sinin zorunlu parçası değildir. Eklendiğinde:

- düşük vertex count,
- önceden hazırlanmış control points,
- CPU’da her frame büyük mesh üretmeme,
- low-tier cihazlarda disable/fallback,
- screenshot parity

şartları uygulanır.

---

## 22. Wallpaper içerik paketi ve build-time compiler

### 22.1 Kaynak paket yapısı

```text
content-src/<wallpaper-id>/
  recipe.yaml
  source/
    master.psd veya master.png
    source_video.mov/mp4
  layers/
    background.png
    subject.png
    foreground.png
  masks/
    glow.png
    depth.png
  shaders/
    fog.frag
  video/
    loop_master.mp4
  metadata/
    license.txt
    attribution.json
```

Runtime’a doğrudan bu kaynak dosyalar verilmez.

### 22.2 Compiler çıktısı

```text
app/src/main/assets/wallpapers/<wallpaper-id>/
  definition.json
  thumbnail.webp
  poster.webp
  textures/
    background_low.webp
    background_high.webp
    subject_low.webp
    subject_high.webp
  video/
    loop_low.mp4
    loop_high.mp4
  shaders/
    fog.frag
  generated/
    asset-index.json
    content-hash.txt
```

### 22.3 `WallpaperPackCompiler` görevleri

```text
recipe parse
-> source/license validation
-> layer dimensions and alpha validation
-> transparent-edge bleed
-> overscan validation
-> image resize/format variants
-> video transcode variants
-> thumbnail/poster generation
-> definition generation or validation
-> shader validation
-> asset index/content hash
-> catalog index update
```

### 22.4 Runtime’da yapılmayacak işler

```text
AI segmentation
background removal
inpainting
video transcode
thumbnail generation
full JSON migration of arbitrary user content
texture atlas generation
```

Bunlar offline veya build-time araçlarda yapılır. Runtime yalnız doğrulanmış paketi yükler.

### 22.5 Quality variant çözümü

```kotlin
enum class AssetTier { LOW, BALANCED, HIGH, ULTRA }
```

Asset seçim anahtarı:

```text
assetId + effectiveTier + targetSizeBucket + formatCapability
```

Aynı layer’ın farklı varyantları definition’da asset group ile belirtilir; renderer dosya adına göre heuristik yapmaz.

### 22.6 İçerik maliyet raporu

Compiler her paket için rapor üretir:

```text
encoded size
estimated decoded texture bytes
alpha fullscreen layer count
estimated overdraw
video bitrate/resolution/fps
shader pass count
required extensions
fallback completeness
```

Budget aşımı warning veya CI failure üretir.

---

## 23. Güncellenmiş klasör ve sınıf yapısı

```text
core/
  content/
    WallpaperSourceKind.kt
    ContentLicenseInfo.kt
    AssetTier.kt
  runtime/
    RenderCommand.kt
    RuntimeHealthSnapshot.kt
  location/
    LocationLightingCoordinator.kt
    LocationRefreshPolicy.kt
    OfflineTimeZoneResolver.kt

engine/
  backend/
    SceneRendererBackend.kt
    LegacySkyRendererBackend.kt
    HybridSceneRendererBackend.kt
  session/
    RenderEngineSession.kt
    RenderSessionState.kt
    AnimationClock.kt
  scene/
    SceneCompiler.kt
    CompiledWallpaperScene.kt
    CompiledLayerGraph.kt
    CompiledRenderPass.kt
    SceneResourcePlan.kt
    SceneFallbackPlan.kt
  layer/
    RenderLayer.kt
    TextureLayer.kt
    ShaderEffectLayer.kt
    ParticleLayer.kt
    ColorGradientLayer.kt
    LayerGroupNode.kt
    video/
      VideoOesLayer.kt
      VideoMediaController.kt
      ExternalFrameSignal.kt
      VideoCapabilityResolver.kt
      VideoVariantSelector.kt
    experimental/
      MeshWarpLayer.kt
      SpriteSequenceLayer.kt
  animation/
    CompiledAnimationPlan.kt
    AnimationTrack.kt
    KeyframeEvaluator.kt
    Easing.kt
  scheduler/
    SceneScheduler.kt
    FrameDemandController.kt
    AdaptiveFrameRateGovernor.kt
    DisplayCompatibleFpsResolver.kt
  gl/
    SessionGlResources.kt
    EglSession.kt
    TextureUploadScheduler.kt
    TextureUnitAllocator.kt
    ExternalOesProgram.kt
  asset/
    RuntimeAssetResolver.kt
    AssetVariantSelector.kt
    SceneAssetPreloader.kt

wallpaper/
  service/
    WallpaperRenderController.kt
    RenderCommandMailbox.kt
    SkyWallpaperService.kt
  engine/
    LiveRenderSessionAdapter.kt

app/
  catalog/
    WallpaperCatalogScreen.kt
    CatalogThumbnailMemoryCache.kt
  preview/
    FullscreenPreviewController.kt
    CatalogPreviewController.kt
    PreviewLeaseCoordinator.kt
    PreviewRuntimeProfiles.kt
  content/
    WallpaperDefinitionRepository.kt
    WallpaperPackRepository.kt

build-logic/
  lumisky-content-plugin/
    WallpaperPackCompilerTask.kt
    DefinitionValidationTask.kt
    ShaderValidationTask.kt
    ImageVariantTask.kt
    VideoVariantTask.kt
    CatalogIndexTask.kt

content-src/
  <wallpaper-id>/
    recipe.yaml
    source/
    layers/
    masks/
    video/
    metadata/

benchmark/
  FullscreenPreviewBenchmark.kt
  CatalogPreviewBenchmark.kt
  CatalogScrollBenchmark.kt
  VideoWallpaperLongRunBenchmark.kt
  HybridWallpaperLongRunBenchmark.kt
  BaselineProfileGenerator.kt
```

### 23.1 Dependency yönü

```text
app -> engine contracts/content repositories
wallpaper -> engine runtime
engine -> core models
build-logic -> shared schema/validator library

engine runtime -X-> app UI
core -X-> Android GL/media implementation
layer factory -X-> global GL handles
```

### 23.2 Registry kapsamı

```kotlin
interface LayerFactoryRegistry {
    fun factoryFor(type: CompiledLayerType): RenderLayerFactory?
}
```

Registry singleton olabilir; factory’ler GL handle veya mutable session state tutamaz. Üretilen layer instance’ları session-bound’dır.


---

## 24. Aşamalı uygulama planı

## Faz 0 — Baseline ve koruma ağı

### İşler

- İki repo için aynı cihazlarda Perfetto/AGI baseline al.
- Görsel screenshot harness kur.
- Sensor callback, render request, draw ve swap sayaçları ekle.
- Fullscreen/katalog/live senaryolarını ayrı ölç.
- Location davranış matrisi çıkar.

### Exit criteria

- En az iki gerçek cihaz ve iki GPU ailesinde baseline mevcut.
- Donma senaryosu tekrar üretilebilir veya telemetry ile izlenebilir.
- Eski Lumisky ile V4 karşılaştırma metriği kayıtlı.

---

## Faz 1 — Session izolasyonu ve command bridge

### İşler

- `SceneScheduler` ve mutable `ParallaxController` singleton olmaktan çıkar.
- `RenderCommand` mailbox eklenir.
- Sensor/UI/service doğrudan engine çağrıları kaldırılır.
- Latest-wins coalescing uygulanır.
- Render-thread assertion eklenir.

### Exit criteria

- Live + preview aynı wallpaper ile birbirini etkilemez.
- Tüm GL/EGL çağrıları render thread’de.
- Event backlog oluşmaz.

---

## Faz 2 — Sensör ve frame-demand sistemi

### İşler

- Sensör remap, smoothing, epsilon, rate limit.
- `FrameDemandController` ve dirty reasons.
- Touch MOVE ve parallax latest-wins.
- Görünmezken callback ve sensor stop.
- Preview parallax değişiminde anlık frame demand.

### Exit criteria

- Sabit cihazda parallax dirty event sıfıra yakın.
- Hareket sırasında backlog yok.
- Invisible state tamamen sessiz.

---

## Faz 3 — Zero-allocation compiled runtime ve source modeli

### İşler

- `SceneCompiler` ve `CompiledWallpaperScene`.
- `WallpaperSourceKind`: layered image, hybrid, video ve procedural.
- JSON uniform/texture binding compile edilir.
- Typed `CompiledLayerGraph`, animation plan ve fallback plan üretilir.
- Time/daylight cache.
- Mutable celestial output.
- Hot path string/map/object temizliği.

### Exit criteria

- App kontrollü render allocation sıfıra yakın; motor kaynaklı collection/data-class allocation yok.
- JSON/map/string policy lookup render içinde yok.
- Aynı compiler layered ve hybrid definition’ı doğrulayabiliyor.
- Frame profiler eski V4’e göre belirgin CPU düşüşü gösterir.

---

## Faz 4 — EGL ve transactional resource loading

### İşler

- Surface/context state machine.
- Texture decode/upload bütçesi.
- Shader warmup ve fallback.
- FBO resize/quality invalidation.
- First-swap commit.
- GLES3 capability path ve GLES2 fallback hazırlığı.

### Exit criteria

- 100 surface lifecycle döngüsü crash/leak yok.
- Bozuk shader current scene’i bozmaz.
- Scene switch main thread’i bloklamaz.

---

## Faz 5 — Adaptif preview FPS

### İşler

- `AdaptiveFrameRateGovernor`.
- Display-compatible basamaklar.
- Fullscreen 120 FPS max profile.
- Katalog 60 FPS max profile.
- Hızlı degrade/yavaş promote hysteresis.
- FPS + renderScale + quality ortak karar sistemi.
- Frame rate telemetry.

### Exit criteria

- Güçlü 120 Hz cihazda hafif fullscreen scene 120 FPS’e çıkabilir.
- Katalog preview hiçbir koşulda 60 FPS’i aşmaz.
- Thermal/power state’te doğru otomatik düşüş.
- FPS oscillation yok.

---

## Faz 6 — Konum parity migration

### İşler

- Last-known provider kapalıyken kullanılabilir.
- DEVICE tercihi korunur.
- Foreground stale refresh.
- Request throttle/cancellation.
- Reverse geocoder async cache.
- Offline timezone resolver.
- Date/time/timezone events.
- Polar day/night.

### Exit criteria

- Eski Lumisky konum davranış matrisi V4’te geçer.
- GPS kapalı/cache var senaryosu doğru.
- Daylight değişimi wallpapera anında yansır.

---

## Faz 7 — Hybrid backend, katmanlı görsel ve scheduler

### İşler

- `HybridSceneRendererBackend` ve typed layer graph.
- `TextureLayer`, `ShaderEffectLayer`, `ParticleLayer`, `LayerGroupNode`.
- Animation clock, keyframe evaluator ve transform/parallax birleşimi.
- AGI ile monolitik sky shader profili.
- Atmosphere/cloud/celestial/foreground cadence ayrımı.
- Profiler-gated half-res cloud FBO.
- Scene-level ve layer-level scheduler ayrımı.
- Render scale’in gerçekten viewport/FBO’ya uygulanması.
- İlk katmanlı görsel ve ilk hybrid wallpaper migration’ı.

### Exit criteria

- En az bir layered-image ve bir hybrid wallpaper görsel parity gösterir.
- Yeni wallpaper engine kodu değiştirilmeden yalnız paket/definition ile eklenebilir.
- Sabit ana ekranda gereksiz continuous draw azalır.
- Eski Lumisky’den daha kötü enerji/thermal sonucu yok.

---

## Faz 8 — `VideoOesLayer` ve medya lifecycle

### İşler

- Media3/ExoPlayer media controller.
- `SurfaceTexture` ve `GL_TEXTURE_EXTERNAL_OES` renderer.
- Frame-available latest flag ve `VIDEO_FRAME` dirty reason.
- Context/surface loss restore.
- Poster fallback ve decoder capability resolver.
- Low/high video variant selection.
- Invisible/lease-lost pause-release policy.
- Seamless loop doğrulaması.
- Uzun süreli battery/thermal test.

### Exit criteria

- Callback thread’inde GL çağrısı yok.
- Görünmez durumda decoder/render işi yok.
- 100 surface/context döngüsünde video restore oluyor.
- Decoder başarısızlığında poster/hybrid fallback çalışıyor.
- 30 FPS video, compositor 120 FPS olsa da gereksiz 120 decoder update üretmiyor.

---

## Faz 9 — Katalog lease, UI ve release hardening

### İşler

- Tek `PreviewLeaseCoordinator`.
- Scroll sırasında mount/release.
- First successful swap callback.
- UI callback throttling.
- Thumbnail memory LRU.
- Baseline Profile ve Macrobenchmark.

### Exit criteria

- Aynı anda en fazla bir preview.
- Scroll jank hedefi karşılanır.
- 50 lease değişiminde GL/thread leak yok.

---

## Faz 10 — İçerik compiler, CI, rollout ve legacy kaldırma

### İşler

- `WallpaperPackCompiler` build-logic plugin’i.
- Definition/shader/image/video/license/asset validator.
- Catalog index ve content hash üretimi.
- Macrobenchmark ve device benchmark.
- Feature flags: hybrid, video, mesh-warp, high-refresh.
- Closed testing kademeli rollout.
- Legacy backend kaldırma gate’i.

### Exit criteria

- Aktif katalog yeni definition hattında.
- Safe fallback testli.
- İki release boyunca kritik regression yok.

---

## 25. İlk uygulanacak 35 görev — kesin sıra

1. `RenderThreadGuard` ekle.
2. `RenderCommand` sealed interface oluştur.
3. `SceneScheduler` singleton kapsamını kaldır.
4. `ParallaxController` mutable state’ini session içine taşı.
5. Sensor direct event/render çağrılarını kaldır.
6. Latest-wins command mailbox ekle.
7. Touch/parallax coalescing uygula.
8. Sensor epsilon ve rate limiter ekle.
9. `FrameDemandController` ve dirty reasons ekle.
10. Preview parallax dirty render düzelt.
11. Görünmezken render/sensor/video invariant testi ekle.
12. `WallpaperSourceKind` ve source envelope oluştur.
13. `SceneCompiler` iskeletini oluştur.
14. JSON uniformlarını compiled binding’e çevir.
15. Texture path yerine typed asset handle modeli ekle.
16. `CompiledLayerGraph`, animation plan ve fallback plan ekle.
17. Wall-clock/daylight cache ekle.
18. Celestial output allocation’ını kaldır.
19. EGL surface/context state machine yaz.
20. First successful swap event’i ekle.
21. Transactional scene activation uygula.
22. `AdaptiveFrameRateGovernor` ekle.
23. Fullscreen preview 120 FPS profile ekle.
24. Katalog preview 60 FPS profile ekle.
25. `PreviewLeaseCoordinator` ekle.
26. Konum parity coordinator’ı taşı.
27. `HybridSceneRendererBackend` ekle.
28. `TextureLayer` ve `ShaderEffectLayer` ekle.
29. `ParticleLayer`, `LayerGroupNode` ve animation tracks ekle.
30. İlk layered-image wallpaper paketini migrate et.
31. İlk hybrid wallpaper paketini migrate et.
32. `VideoOesLayer` + Media3 controller proof-of-concept yap.
33. Video poster fallback, variant selector ve context restore ekle.
34. `WallpaperPackCompiler` ile image/video/definition validation hattını kur.
35. Hybrid ve video uzun süreli AGI/Perfetto/battery testlerini CI release gate’ine bağla.

Öncelik sırası bilinçlidir. Video, mesh warp, genel FBO pool, PAD veya shader binary cache; session ownership, compiled runtime ve EGL lifecycle tamamlanmadan ana geliştirme konusu yapılmamalıdır.

---

## 26. Önerilen dosya/değişiklik haritası

Ayrıntılı hedef yapı Bölüm 23’te verilmiştir. İlk uygulama için öncelikli dosyalar:

```text
core/
  content/WallpaperSourceKind.kt
  content/AssetTier.kt
  runtime/RenderCommand.kt
  runtime/RuntimeHealthSnapshot.kt
  location/LocationLightingCoordinator.kt

engine/
  backend/SceneRendererBackend.kt
  backend/HybridSceneRendererBackend.kt
  session/RenderEngineSession.kt
  session/RenderSessionState.kt
  session/AnimationClock.kt
  scheduler/FrameDemandController.kt
  scheduler/AdaptiveFrameRateGovernor.kt
  scene/SceneCompiler.kt
  scene/CompiledWallpaperScene.kt
  scene/CompiledLayerGraph.kt
  layer/TextureLayer.kt
  layer/ShaderEffectLayer.kt
  layer/ParticleLayer.kt
  layer/LayerGroupNode.kt
  layer/video/VideoOesLayer.kt
  layer/video/VideoMediaController.kt
  layer/video/ExternalFrameSignal.kt
  gl/SessionGlResources.kt
  gl/EglSession.kt
  gl/TextureUploadScheduler.kt
  gl/ExternalOesProgram.kt
  asset/RuntimeAssetResolver.kt
  asset/AssetVariantSelector.kt

wallpaper/
  service/WallpaperRenderController.kt
  service/RenderCommandMailbox.kt
  service/SkyWallpaperService.kt
  engine/LiveRenderSessionAdapter.kt

app/
  preview/FullscreenPreviewController.kt
  preview/CatalogPreviewController.kt
  preview/PreviewLeaseCoordinator.kt
  preview/PreviewRuntimeProfiles.kt
  catalog/WallpaperCatalogScreen.kt
  content/WallpaperDefinitionRepository.kt

build-logic/
  lumisky-content-plugin/WallpaperPackCompilerTask.kt
  lumisky-content-plugin/ImageVariantTask.kt
  lumisky-content-plugin/VideoVariantTask.kt
  lumisky-content-plugin/DefinitionValidationTask.kt

benchmark/
  FullscreenPreviewBenchmark.kt
  CatalogPreviewBenchmark.kt
  VideoWallpaperLongRunBenchmark.kt
  HybridWallpaperLongRunBenchmark.kt
```

Gerçek repo paketlerine göre isimler uyarlanabilir; ownership ve dependency sınırları değiştirilmemelidir.

---

## 27. Örnek preview runtime profilleri

```kotlin
object PreviewRuntimeProfiles {
    fun fullscreen(): RuntimeProfile = RuntimeProfile(
        mode = RuntimeMode.PREVIEW_FULLSCREEN,
        absoluteMaxFps = 120,
        preferredFpsSteps = intArrayOf(120, 90, 72, 60, 48, 45, 40, 36, 30, 24, 20, 15),
        maxRenderScale = 1.0f,
        allowHighRefresh = true,
        allowSensorParallax = true,
        allowPostProcess = true,
    )

    fun catalog(): RuntimeProfile = RuntimeProfile(
        mode = RuntimeMode.PREVIEW_CATALOG,
        absoluteMaxFps = 60,
        preferredFpsSteps = intArrayOf(60, 48, 45, 40, 36, 30, 24, 20, 15, 10),
        maxRenderScale = 0.75f,
        allowHighRefresh = true,
        allowSensorParallax = true,
        allowPostProcess = false,
    )
}
```

Liste doğrudan uygulanmadan önce display-compatible resolver tarafından filtrelenir.

---

## 28. Mimari kabul checklist’i

### Threading

- [ ] GL/EGL yalnız render thread.
- [ ] Scheduler/parallax/session mutable state singleton değil.
- [ ] Sensor/UI/service yalnız command üretir.
- [ ] Latest-wins coalescing var.
- [ ] Shutdown bounded ve testli.

### Preview FPS

- [ ] Fullscreen preview max 120 FPS.
- [ ] Katalog preview max 60 FPS.
- [ ] Display-compatible pacing uygulanıyor.
- [ ] Power Saver ve thermal durumda otomatik degrade.
- [ ] Promote yavaş, degrade hızlı.
- [ ] FPS kararı render scale/quality ile birlikte veriliyor.

### Catalog

- [ ] Thumbnail-first.
- [ ] Scroll sırasında aktif GL preview yok.
- [ ] Aynı anda maksimum bir preview lease.
- [ ] First-frame başarılı swap ile doğrulanıyor.

### Rendering ve içerik türleri

- [ ] `LAYERED_IMAGE`, `HYBRID`, `VIDEO`, `PROCEDURAL` typed source modeli var.
- [ ] `HybridSceneRendererBackend` monolitik mega-layer değil, compiled graph yönetiyor.
- [ ] JSON ve asset I/O hot path dışında.
- [ ] App allocation 0 B hedef.
- [ ] Layer update ve render cadence ayrı.
- [ ] FBO yalnız ölçümle.
- [ ] High refresh scene limitini kör biçimde aşmıyor.
- [ ] Video source FPS ile compositor FPS ayrı izleniyor.
- [ ] `VideoOesLayer.updateTexImage()` yalnız current GL context’li render thread’de.
- [ ] Video görünmezken player/decoder policy’ye göre duruyor.
- [ ] Layered image overscan/parallax boşluk testi geçiyor.

### EGL/resources

- [ ] Surface/context lifecycle ayrı.
- [ ] Context loss recovery testli.
- [ ] Texture upload frame bütçeli.
- [ ] Transactional scene switch.
- [ ] Safe wallpaper base APK’da.

### Location

- [ ] GPS kapalıyken last-known kullanılabilir.
- [ ] DEVICE tercihi korunur.
- [ ] Timezone koordinata göre çözülür.
- [ ] Date/time/timezone değişimleri işlenir.
- [ ] Polar state modellenir.

### İçerik pipeline

- [ ] Wallpaper paketi source/license metadata içeriyor.
- [ ] Runtime’da segmentation, inpainting veya transcode yapılmıyor.
- [ ] Build-time compiler thumbnail/poster/asset variants üretiyor.
- [ ] JSON Schema ve semantic validator birlikte çalışıyor.
- [ ] Decoder/video fallback poster testli.

### Quality

- [ ] Perfetto live trace.
- [ ] AGI GPU profile.
- [ ] Macrobenchmark app UI.
- [ ] En az iki GPU ailesi gerçek cihaz testi.
- [ ] Feature flag rollback.

---

## 29. Son karar

Lumisky V4’ün doğru hedefi yalnızca yüksek FPS değildir. Doğru hedef:

```text
Görünür ve kullanıcı etkileşimindeyken cihazın kaldırdığı en yüksek stabil kalite/FPS,
görünmez veya sabit durumdayken mümkün olan en düşük CPU/GPU/sensör çalışması.
```

Fullscreen preview güçlü cihazlarda **120 FPS**, katalogdaki tek aktif preview **60 FPS** çalışabilir. Ancak bu değerler sabit zorlamalar değildir. Runtime governor sahne maliyeti, display pacing, thermal durum, Power Saver, render scale ve gerçek deadline istikrarına göre doğru basamağı seçer.

Öncelik sırası değişmez:

```text
1. Session ownership ve thread correctness
2. Sensör/render fırtınasının durdurulması
3. Allocation-free compiled hot path
4. EGL ve transactional resource lifecycle
5. Adaptif 120/60 preview FPS
6. Konum parity migration
7. Sky/layer GPU optimizasyonu
8. UI, benchmark ve rollout hardening
```

Bu sıra uygulanırsa V4, eski Lumisky’nin akıcılığını ve konum güvenilirliğini korurken katmanlı resim, hibrit animasyon, procedural efekt ve optimize video wallpaper’ları aynı typed içerik hattında çalıştırabilen bir production motoruna dönüşebilir.

V8’in içerik kararı:

```text
Varsayılan üretim yolu: layered image + shader/timeline hybrid
Kompleks hareket: VideoOesLayer
Tam generative sahne: procedural
Her durumda: thumbnail-first catalog, transactional activation, visible-only work ve cihaz-adaptif kalite
```
