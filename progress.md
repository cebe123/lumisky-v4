# Lumisky Progress Plan

## Amaç

Bu doküman, Lumisky projesini fazlara bölerek:

- Android canlı duvar kağıdı (telefon ana ekranı) hedefini
- düşük batarya tüketimi + yüksek akıcılık dengesini
- yüzlerce wallpaper ölçeğinde bozulmadan çalışacak mimariyi
- wallpaper bazlı bağımsız override sistemini
- modüler efekt ekleme (güneş/bulut/yağmur vb.) altyapısını

adım adım ve kontrollü şekilde geliştirmek için hazırlanmıştır.

## AGENTS.md Uyum Notları (Repo-Seviyesi Çalışma Kuralları)

Bu plan uygulanırken aşağıdaki repo kuralları sabit kabul edilir:

### Kaynak Gerçeklik (Source of Truth)

- Modül kararlarında öncelik: `settings.gradle.kts` + gerçek kaynak ağacı
- `docs/PROJECT_STRUCTURE.md` yalnızca yardımcı doküman; sapma olabilir

### Modül Sınırları (Korunacak)

- `app` -> UI / Activity / kullanıcı akışı / wallpaper seçimi
- `core` -> shared helper / settings / location / API / logger
- `engine` -> render logic / domain / GL çekirdeği
- `wallpaper` -> `WallpaperService` lifecycle / render controller / EGL entegrasyonu

Kural:
- Faz çalışmalarında gereksiz çapraz-modül taşma yapılmayacak.

### Wallpaper Service Mimari Kuralları (Korunacak)

- Manifest servis sınıfı `com.example.wallpaper.SkyWallpaperService` wrapper olarak kalır
- Gerçek implementasyon `wallpaper.service.SkyWallpaperService` içinde kalır
- Wallpaper config apply akışı `WallpaperConfigStore` + `ACTION_APPLY_STORED_WALLPAPER_CONFIG` broadcast patternini korur
- Ağır işler (sun-times fetch / wallpaper apply / benzeri IO) UI thread dışında kalır

### Asset Pipeline Kuralları (Kritik)

- `:app` içinde `preBuild -> prepareFilteredAssets -> convertWallpaperTexturesToWebp` zinciri korunur
- Paketleme kaynağı `app/build/generated/filteredAssets/main` olur
- `assets/shaders/**` texture conversion dışında kalır
- `app/build/**` altındaki generated asset'ler elle düzenlenmez
- Yeni raster texture eklendiğinde mümkünse `.webp` çıktısı üretilir ve doğrulanır

### Kodlama Kuralları (Repo-Özel)

- Yeni loglar için `com.example.core.Logger` kullanılır (`println` yok)
- Ayarlar için doğrudan dağınık `SharedPreferences` erişimi açılmaz; `AppSettingsRepository` kullanılır
- Android lifecycle register/unregister patternleri (özellikle receiver kayıtları ve API 33+ branch'leri) korunur
- Kotlin/Gradle dosyalarında mevcut stil (çoğunlukla tab indentation) korunur

### Doğrulama / Teslim Kuralı

- Her önemli faz sonunda ilgili modülde en az build/test doğrulaması yapılır
- Referans komutlar (PowerShell):
  - `.\gradlew :engine:testDebugUnitTest`
  - `.\gradlew :wallpaper:testDebugUnitTest`
  - `.\gradlew :app:testDebugUnitTest`
  - `.\gradlew :app:assembleDebug`
- `build/`, `.gradle/`, `.idea/` çıktıları commit kapsamına alınmaz

## Repo İncelemesi Sonucu (Düzeltmeler + Tamamlanan Maddeler)

### Genel Mimari (Mevcut Durum)

- Platform: `Android`
- Stack: `Kotlin + OpenGL ES 2.0 + Compose (app UI)`
- Modüller (gerçek repo): `:app`, `:core`, `:engine`, `:wallpaper`
- Canlı duvar kağıdı: gerçek `WallpaperService` kullanılıyor (`wallpaper` modülü)

Not:
- `docs/PROJECT_STRUCTURE.md` içinde `:snapshot` modülü geçiyor ama repo güncel durumunda yok.

### Soru Cevaplarının Tamamlanmış Hali (1-15)

1. Hedef platform: `Android` (doğrulandı)
2. Live wallpaper tipi: `Telefon ana ekranında gerçek canlı duvar kağıdı` (doğrulandı)
3. Teknoloji: `Kotlin + OpenGL` (doğrulandı, OpenGL ES 2.0 pipeline var)
4. Wallpaper yapısı (repo inceleme sonucu):
   - Şu an ağırlıklı yapı `shader + texture overlay` (fragment shader temelli)
   - `video / Lottie` tabanlı değil
   - `WallpaperConfig` ile texture, feature flag, shader, daylight, horizon vb. yönetiliyor
   - Kısmen data-driven, kısmen kod içinde preset/hardcode
5. Yeni wallpaper ekleme süreci:
   - Şu an pratikte `kod + config/preset` karışık (`WallpaperCatalog`)
   - Hedefte `sadece config/json ile` + gerektiğinde `kod override` desteklenecek (senin istediğin doğru)
6. Override seviyesi: `Hepsi` (pozisyon, eğri, hız, renk, zamanlama, efekt aç/kapat vb.) hedeflenecek
7. İlk efektler: `Hepsi ama sırayla` (doğru yaklaşım)
8. İçerik dağıtımı: `Hibrit`, ilk fazda `local-first` (uygun)
9. Performans hedefi (repo gerçekleriyle düzeltilmiş):
   - Uygulama içi preview/home: düşük cihazlarda `>= 30 FPS` hedefi (mantıklı)
   - Ana ekran wallpaper (statik/minute-tick çalışanlar): sürekli 30 FPS gerekmez
   - Repo’da zaten minute boundary scheduler var (`MinuteTickScheduler`)
   - Bazı wallpaper’larda (ör. dinamik texture/shader) continuous render gerekebilir
   - Şu an continuous render kararları config yerine `config.id` string hint ile yapılıyor (geliştirilecek)
10. Düşük cihaz hedef profili (repo + hedeflere göre öneri):
   - Destek tabanı: `Android 9+ (minSdk = 28)`
   - OpenGL: `ES 2.0 destekli cihaz`
   - İlk optimize hedef profil: `3 GB RAM`, giriş/orta segment GPU (Adreno 5xx / Mali G5x sınıfı)
   - Stretch hedef (sonraki faz): `2 GB RAM` cihazlarda “Battery mode” ile kabul edilebilir deneyim
11. Uygulama ekranları (repo inceleme sonucu):
   - Var: `Home` (kategori bazlı liste + kart içi GL canlı preview)
   - Var: `Preview` (tam ekran önizleme + uygula)
   - Var: `Settings` (tema, dil, performans modu, konum/şehir)
   - Yok / erken aşama: `arama`, `favoriler`, `indirme`, `uzak katalog`, gelişmiş filtreleme
12. Görsel stil: `minimal + glassmorphism` (uygun, mevcut UI buna evrilebilir)
13. Akıllı özellikler:
   - `Gün/gece otomatik` zaten kısmen var (sunrise/sunset + location)
   - `Hava durumuna bağlı tema` henüz yok (gelecek faz)
   - Diğer interaktif özellikler sonra (uygun)
14. Offline/online strateji:
   - İlk aşama `local-first` (doğru)
   - Wallpaper sayısı arttıkça hibrit katalog/asset delivery’ye geçilecek
15. `progress.md` formatı:
   - Her faz için `hedef`, `çıktı`, `risk`, `kabul kriteri`, `tahmini süre` olacak (bu dokümanda var)

## Mevcut Güçlü Yanlar (Başlangıç Avantajları)

- `engine` ve `wallpaper` ayrımı doğru; render logic ile service lifecycle ayrışmış.
- `WallpaperRenderController` içinde:
  - visible/surface state yönetimi
  - minute-tick scheduler
  - gerektiğinde continuous loop
  - state hash ile gereksiz render skip
- `PreviewGlRenderer` içinde:
  - `AUTO/SMOOTH/BATTERY` performans modları
  - adaptive FPS
  - thermal / power saver farkındalığı
  - adaptive quality scale
- Asset pipeline:
  - build sırasında texture’ları WebP’ye dönüştüren görevler
  - kaynak asset ile paketlenen asset ayrımı
- `WallpaperConfigStore` var; config persist/restore altyapısı hazır.

## Mevcut Kritik Boşluklar (Hedefe Gitmek İçin Refactor Gerekenler)

1. `WallpaperCatalog` ölçeklenebilir değil (preset kod içinde)
- `DEFAULT_COUNT = 120` olsa da gerçek preset sayısı sınırlı.
- Yüzlerce wallpaper hedefi için manifest/index tabanlı katalog gerekiyor.

2. Wallpaper bazlı override sınırlı
- `WallpaperConfig` güçlü ama güneşin yatay doğuş konumunu serbestçe değiştirecek alan yok.
- `CelestialCalculator` içinde `VERTICAL -> x=0.5`, `ARC -> x=phaseProgress`.
- Senin verdiğin “tek wallpaper’da güneş daha sağdan doğsun” örneği için yeni orbit/offset parametreleri şart.

3. Efekt sistemi modüler değil
- `PreviewSkyProgram` çok fazla sorumluluk taşıyor (shader uniform, texture tuning, legacy theme davranışları vb.)
- Yeni efekt eklemek (bulut/yağmur/kar) şu an diğer temaları etkileme riski taşıyor.

4. Continuous render kararı data-driven değil
- Şu an `requiresContinuousRendering()` sadece `config.id` string match ile çalışıyor (`warrior` gibi).
- Bu karar config capability / effect runtime policy üzerinden verilmelidir.

5. Wallpaper service performans politikası preview kadar gelişmiş değil
- Preview tarafında adaptive FPS/quality var, wallpaper service tarafında daha basit bir model var.
- Uzun vadede service için de thermal/battery-aware policy gerekli.

## Mimari Hedef (Kilitlemek İstediğimiz Tasarım)

### 1) Data-driven Wallpaper Paket Yapısı

Her wallpaper için:

- `manifest.json` (veya Kotlin DSL karşılığı)
- shader referansları
- texture referansları
- feature listesi
- override blokları (celestial / atmosphere / layers / animation)
- performance policy

Opsiyonel:

- özel kod modülü (yalnızca gerekirse)

### 2) Override Sistemi (Wallpaper Bazlı, Bağımsız)

Örnek override alanları:

- `sun.orbit.startX`, `sun.orbit.endX`
- `sun.orbit.peakY`
- `sun.orbit.curve`
- `moon.orbit.*`
- `timing.daylightRemap`
- `effects.clouds.enabled`
- `effects.clouds.speed`
- `effects.stars.density`
- `shader.uniformOverrides`

### 3) Modüler Efekt Pipeline

Efekt ekleme hedefi:

- Bir efekt eklenince diğer efektleri kırmamalı
- Her efektin:
  - config schema’sı
  - runtime state’i
  - update() / draw() sorumluluğu
  - performance cost profili
  - capability flag’i olmalı

## Fazlar (Detaylı Yol Haritası)

Not:
- Süre tahminleri `tek geliştirici` ve mevcut kod tabanına göre verilmiştir.
- Paralel iş yapılırsa toplam süre kısalır.

## Faz 0 - Baseline ve Teknik Kriterleri Kilitleme

### Hedef

Mevcut sistemi ölçülebilir hale getirip, sonraki refactor’ların kalite hedeflerini netleştirmek.

### Kapsam

- Performans KPI’larının net tanımı
- Düşük cihaz hedef profili dokümantasyonu
- Wallpaper tip sınıfları (static / minute-tick / dynamic) tanımı
- Mevcut mimarinin kısa teknik envanteri
- `docs/PROJECT_STRUCTURE.md` sapmalarını not etmek

### Çıktılar

- `docs/perf_targets.md`
- `docs/wallpaper_types.md`
- ölçüm senaryoları listesi (home/preview/service)
- `progress.md` revizyonu (güncel durum)

### Riskler

- Hedef FPS/batarya kriterleri muğlak kalırsa sonraki fazlarda gereksiz refactor olur
- “Akıcı” tanımı ekip içinde farklı anlaşılabilir

### Kabul Kriterleri

- `Home`, `Preview`, `WallpaperService` için ayrı KPI listesi yazılmış olmalı
- `static/minute-tick/dynamic` wallpaper sınıfları tanımlanmış olmalı
- Düşük cihaz profili ve test cihaz listesi belirlenmiş olmalı

### Tahmini Süre

- `0.5 - 1 gün`

## Faz 1 - Wallpaper Config V2 ve Override Sistemi

### Hedef

Wallpaper başına bağımsız override yapılabilecek, ileriye dönük genişletilebilir config şemasını kurmak.

### Kapsam

- `WallpaperConfig` için V2 model tasarımı
- `CelestialConfig` genişletme:
  - orbit başlangıç/bitiş X
  - peakY / horizon bağlantısı
  - curve/easing tipi
  - görünür/gizli pozisyon parametreleri
- Feature/effect override blokları
- Shader uniform override alanı
- Config codec migration (`WallpaperConfigJsonCodec`) geriye uyumluluk
- Capabilities/performance policy alanları (`requiresContinuousRendering` yerine)
- `WallpaperConfigStore` persist/decode akışını bozmadan migration yapmak

### Önerilen Yeni Alanlar (örnek)

- `renderPolicy.mode = MINUTE_TICK | CONTINUOUS | EVENT_DRIVEN`
- `renderPolicy.targetFps`
- `renderPolicy.dynamicTextureFps`
- `celestial.sunOrbit`
- `celestial.moonOrbit`
- `effects: Map<String, EffectConfig>`

### Çıktılar

- `engine/config/WallpaperConfig.kt` V2
- `WallpaperConfigJsonCodec` migration desteği
- mevcut presetlerin yeni modele taşınması
- birkaç örnek override preset (örn. güneş sağdan doğan tema)

### Riskler

- Persist edilmiş eski config’ler bozulabilir
- Şema çok erken aşamada gereğinden büyük tasarlanırsa bakım yükü artar

### Kabul Kriterleri

- En az 2 wallpaper, kod değiştirmeden yalnız config override ile farklı güneş/moon davranışı verebilmeli
- Eski persist edilmiş config decode olmaya devam etmeli
- `requiresContinuousRendering` string-hint bağımlılığı kaldırılmalı veya deprecated olmalı
- Config apply akışı (`WallpaperConfigStore` + broadcast) çalışmaya devam etmeli

### Tahmini Süre

- `2 - 4 gün`

## Faz 2 - Data-Driven Katalog ve Yerel Manifest Pipeline (Local-First)

### Hedef

Yeni wallpaper eklemeyi preset kodundan çıkarıp manifest tabanlı hale getirmek; yüzlerce wallpaper ölçeğine hazırlık yapmak.

### Kapsam

- `WallpaperCatalog` refactor:
  - kod içi hardcoded preset listesi -> manifest/index okuma
- Yerel katalog formatı:
  - `assets/wallpapers/index.json`
  - `assets/wallpapers/<id>/manifest.json`
- Asset referans standardı
- kategori/tag alanları
- sürüm/hash alanları (gelecekte hibrit/remote için)
- fallback: manifest bozuksa güvenli skip
- mevcut asset pipeline (`prepareFilteredAssets` / WebP dönüşümü) ile uyumlu path kuralları

### Çıktılar

- Manifest schema (doküman + parser)
- Local katalog loader
- mevcut wallpaper’ların manifest’e taşınmış hali
- `WallpaperCatalog` içinde backward-compatible fallback (geçiş sürecinde)

### Riskler

- Asset path kırılmaları
- Manifest parse maliyeti açılış süresini etkileyebilir

### Kabul Kriterleri

- Yeni bir wallpaper, sadece asset + manifest eklenerek listede görünebilmeli
- Uygulama bozuk bir manifest yüzünden çökmeden diğer wallpaper’ları göstermeye devam etmeli
- Home açılış süresi kabul edilebilir kalmalı (ölçüm yapılmış olmalı)
- `app/build/generated/**` elle düzenleme gerektirmeyen bir içerik akışı tanımlanmış olmalı

### Tahmini Süre

- `3 - 5 gün`

## Faz 3 - Modüler Efekt Sistemi (Çekirdek Pipeline Refactor)

### Hedef

Bulut, yağmur, kar, yıldız, sis gibi efektleri birbirini bozmadan ekleyebilecek modüler render/update altyapısını kurmak.

### Kapsam

- `PreviewSkyProgram` sorumluluklarını ayırma
- Efekt arabirimi tasarımı (ör. `SkyEffect`, `EffectRenderer`, `EffectState`)
- Update pipeline (time/weather/config input)
- Draw order / z-order sistemi
- Capability flags + graceful degradation
- Legacy shader temaları için compatibility katmanı
- Modül sınırı korunumu (`engine` render logic, `wallpaper` service integration)

### Olası Mimari Parçalar

- `engine/effects/api`
- `engine/effects/builtin/clouds`
- `engine/effects/builtin/stars`
- `engine/effects/builtin/fog`
- `engine/render/pipeline/*`

### Çıktılar

- Modüler efekt API’si
- İlk 1-2 efektin yeni pipeline ile çalışması (örn. `stars`, `clouds`)
- Mevcut temaların bozulmadan çalıştığını gösteren geçiş katmanı

### Riskler

- Shader/GL kodu refactor sırasında regressions
- Performans düşüşü (fazla draw call / state change)

### Kabul Kriterleri

- Bir wallpaper’da `clouds` açılıp diğerinde kapatıldığında başka temalar etkilenmemeli
- Efekt ekleme işlemi tek bir monolitik shader dosyasını değiştirmeyi gerektirmemeli (en azından yeni sistemde)
- Geçiş sonrası mevcut temaların büyük çoğunluğu render olmaya devam etmeli
- Yeni debug/telemetry logları varsa `Logger` üzerinden olmalı

### Tahmini Süre

- `1 - 2 hafta`

## Faz 4 - Performans Katmanı (Service + Preview + Home) ve Düşük Cihaz Stratejisi

### Hedef

Düşük cihazlarda da stabil çalışan, batarya dostu ve ölçeklenebilir runtime politikalarını standartlaştırmak.

### Kapsam

- Wallpaper service için performance mode/policy entegrasyonu
- Theme/effect bazlı FPS caps (config-driven)
- dynamic texture FPS ayrı kontrolü
- thermal/power save farkındalığını service tarafına taşıma
- home preview prewarm bütçesi ve cache limitleri
- texture çözünürlük seçimi (DPI + device tier) aktif kullanımı
- memory budget ve cache eviction tuning
- Service lifecycle receiver/scheduler patternlerini koruyarak implementasyon

### Çıktılar

- `renderPolicy` config alanlarının runtime’da kullanılması
- service tarafı adaptive render policy (en az sınırlı versiyon)
- düşük cihaz profili için kalite düşürme stratejileri
- ölçüm raporu (önce/sonra)

### Riskler

- Fazla agresif optimizasyon görsel kaliteyi düşürür
- Çok fazla policy kombinasyonu hata ayıklamayı zorlaştırır

### Kabul Kriterleri

- Düşük hedef profilde app preview/home `>= 30 FPS` (kritik senaryolarda)
- static/minute-tick wallpaper’larda gereksiz continuous render kapalı
- dynamic wallpaper’larda config’de tanımlı FPS cap uygulanıyor
- ciddi jank/regression gözlenmiyor
- `WallpaperService` lifecycle regressions (visible/surface/create/destroy) gözlenmiyor

### Tahmini Süre

- `1 - 2 hafta`

## Faz 5 - Modern UI/UX (Minimal + Glassmorphism) ve Ölçeklenebilir Katalog Deneyimi

### Hedef

Uygulamayı modern, kullanışlı ve yüzlerce wallpaper ile yönetilebilir hale getirmek.

### Kapsam

- Home ekranı görsel iyileştirme (minimal + glassmorphism)
- arama / filtre / tag sistemi
- favoriler
- kategori deneyimi ölçeklendirme
- performans odaklı listeleme (paging/lazy loading)
- preview ekranı gelişmiş kontroller (örn. hız, kalite, bilgi paneli)

### Çıktılar

- Yeni UI tasarım sistemi (token/color/spacing)
- `Search` / `Favorites` akışı (en az local)
- katalogda yüzlerce item için akıcı scroll

### Riskler

- UI güzelleştirme performansı bozabilir
- Tasarım değişimi mevcut kullanıcı akışını karmaşıklaştırabilir

### Kabul Kriterleri

- Home ekranında büyük katalogda scroll stabil
- Kullanıcı arama/filtre ile wallpaper bulabiliyor
- Tasarım düşük cihazda da kabul edilebilir akıcılıkta

### Tahmini Süre

- `1 - 2 hafta`

## Faz 6 - Akıllı Tema Özellikleri (Gün/Gece + Hava Durumu + Genişletilebilir Context)

### Hedef

Wallpaper davranışını çevresel bağlama (zaman, konum, hava durumu) göre güvenli şekilde uyarlamak.

### Kapsam

- mevcut sun-times akışını sağlamlaştırma
- weather provider abstraction (ilk aşama opsiyonel/plug-in)
- hava durumu fallback stratejisi (offline-safe)
- weather-based theme/effect mapping
- izin/ayar UX’i
- `AppSettingsRepository` merkezli ayar yönetimi korunumu

### Çıktılar

- `WeatherRepository` arayüzü
- weather -> theme/effect override eşleme katmanı
- devre dışı/erişilemeyen durumda fallback davranışları

### Riskler

- Ağ gecikmesi/servis kesintisi
- izin reddi senaryoları
- batarya tüketim artışı (çok sık fetch yapılırsa)

### Kabul Kriterleri

- İnternet yokken wallpaper çalışmaya devam eder
- Hava verisi varsa belirlenen temalarda effect/theme override uygulanır
- Weather özelliği kapatıldığında runtime maliyeti minimum olur

### Tahmini Süre

- `4 - 7 gün`

## Faz 7 - Hibrit İçerik Dağıtımı (Local -> Remote Katalog Geçişi)

### Hedef

İlk local-first yapıyı koruyarak, ileride yüzlerce wallpaper için uzaktan katalog ve içerik indirme altyapısını eklemek.

### Kapsam

- remote katalog manifest formatı (local ile aynı şema)
- versioning/hash doğrulama
- indirilen asset cache klasör yapısı
- local + remote merge stratejisi
- rollback/fallback

### Çıktılar

- hibrit katalog loader
- cache invalidation stratejisi
- local-only fallback garantisi

### Riskler

- Disk kullanımı büyümesi
- bozuk indirme/corrupt asset
- sürüm uyumsuzluğu

### Kabul Kriterleri

- Remote katalog kapalıyken local deneyim bozulmaz
- Yeni wallpaper’lar uzaktan eklenebilir
- Bozuk/eksik asset durumunda app crash olmaz

### Tahmini Süre

- `1 - 2 hafta`

## Faz 8 - Test, Stabilizasyon, Release Hazırlığı

### Hedef

Performans ve kalite hedeflerini güvence altına alıp release’e hazır hale gelmek.

### Kapsam

- Unit test genişletme (config codec, celestial overrides, policy logic)
- Instrumentation test (kritik akışlar)
- cihaz matrisi testleri
- jank/battery smoke test checklist
- crash/ANR odaklı hardening
- Faz bazlı doğrulama komutlarının standardizasyonu (AGENTS.md komutları)
- Play Store release uyumluluğu:
  - privacy policy URL ve metni hazırlama
  - location permission disclosure akışı ekleme
  - Data safety formunda konum/veri paylaşımı beyanını netleştirme
  - minimum gerekli permission kapsamını gözden geçirme (`COARSE` / `FINE`)
  - store listing / app content / content rating checklist'i oluşturma
  - gerekiyorsa yeni kişisel hesap için closed testing (`12 tester / 14 gün`) hazırlığı

### Çıktılar

- test coverage artışı (özellikle config/policy/celestial)
- release checklist
- known issues listesi
- modül bazlı build/test komut listesi (PowerShell) release checklist içine ekli
- Play Store submission checklist ve policy-risk notları

### Riskler

- Görsel render hataları testte yakalanması zor olabilir
- OEM bazlı wallpaper service davranış farkları
- Gizlilik/policy beyanı eksik veya yanlış olursa teknik olarak sağlam build bile review'da red alabilir

### Kabul Kriterleri

- Kritik kullanıcı akışları test edilmiş
- Düşük/orta/yüksek cihaz profillerinde smoke test geçmiş
- Bilinen blocker bug kalmamış
- Play Console gönderimi için privacy policy, disclosure ve data safety girişleri hazır

### Tahmini Süre

- `4 - 7 gün`

## Önerilen İlk Uygulama Sırası (Pratik)

1. Faz 0
2. Faz 1
3. Faz 2
4. Faz 4 (erken performans kazanımları için, Faz 3 ile paralel ilerleyebilir)
5. Faz 3
6. Faz 5
7. Faz 6
8. Faz 7
9. Faz 8

Not:
- Teoride Faz 3 (modüler efekt sistemi) Faz 4’ten önce gibi görünür, fakat pratikte Faz 4’ün bir kısmı erken yapılırsa refactor sırasında performans regressions daha hızlı yakalanır.

## Faz Uygularken Sabit Kurallar (Kısa Checklist)

- Minimum kapsamlı değişiklik: ilgili modül içinde çöz, gerekmedikçe çapraz modüle yayma
- `Logger` kullan, `println` ekleme
- Ayarlar için `AppSettingsRepository` dışı dağınık `SharedPreferences` açma
- Receiver register/unregister ve API 33+ branch patternlerini bozma
- `WallpaperConfigStore` + broadcast apply akışını koru
- `app/build/**` generated asset'leri elle düzenleme
- Faz sonunda en az etkilenen modül build/test doğrulaması yap

## İlk Sprint İçin Net Görevler (Öneri)

### Sprint 1 (başlamak için en doğru yer)

- `WallpaperConfig` V2 taslağı çıkar
- `CelestialOrbit` override alanlarını ekle
- `CelestialCalculator`’ı yeni alanlarla çalışacak şekilde güncelle
- 2 örnek wallpaper’da farklı güneş doğuş konumu göster
- `requiresContinuousRendering()` string hint yerine config `renderPolicy` alanını ekle (başta compatibility ile)

### Sprint 1 Kabul Kriteri

- Senin verdiğin örnek senaryo çalışmalı:
  - Aynı engine üzerinde bir wallpaper’da güneş ortadan
  - Diğerinde daha sağdan veya özel path ile doğmalı
- Bu değişiklik diğer mevcut wallpaper’ları bozmamalı

## Açık Kararlar (Sonraki Mesajda Netleştirilebilir)

1. Override şeması JSON tabanlı mı başlasın, yoksa önce Kotlin data class + codec ile mi?
2. Modüler efekt sistemi shader-pass tabanlı mı olsun, yoksa CPU update + sprite layer karışık mı?
3. Weather entegrasyonu için ilk sağlayıcı stratejisi ne olacak (tam local mock / gerçek API / opsiyonel modül)?

# Lumisky Progress (Updated for Play Store Compliance)

## Current Status (Reality Check)

* Total presets: ~14
* True unique experiences: ~9–10 (due to shared shaders)
* Risk level: ⚠️ Medium (borderline Play Store rejection risk)

Main issue:

> Some wallpapers are perceived as variations (same shader + different textures)

---

## 🎯 Goal Before Re-Submission

### Target Metrics

* Total presets: **16–18+**
* Truly unique experiences: **12+**
* Categories: **5+ strong distinct categories**

---

## 🔴 Critical Problems Identified

### 1. City Wallpapers Are Too Similar

Current:

* Istanbul
* New York
* Tokyo
* Paris

Problem:

* Same shader (`city/fragment_shader.glsl`)
* Only texture changes

➡️ Play Store may treat these as **1 wallpaper, not 4**

---

### 2. Perceived Content Value Is Low

Even with 14 presets:

* Not all feel unique
* Lacks strong differentiation signals

---

### 3. Features Not Visible Enough to User

We have:

* Location-based sun
* Performance modes
* Render strategies

BUT:

* User does not clearly see these as value

---

## ✅ Action Plan (Must Complete)

### 🔧 1. Fix City Wallpapers (HIGH PRIORITY)

Make each city **behaviorally unique**:

* Istanbul → Fog layer + birds
* Tokyo → Neon + rain animation
* New York → Moving clouds + light flicker
* Paris → Warm sunset haze

Goal:

> Each city = unique shader behavior

---

### 🎨 2. Add 4–6 New Unique Wallpapers

Add these categories:

#### Weather-based

* Snow (falling particles)
* Rain (dynamic drops + ripple)
* Fog (depth fade)

#### Space-based

* Galaxy (parallax stars)
* Nebula (color shifting)

#### Premium Minimal

* Gradient horizon
* Glass glow effect

---

### ⚙️ 3. Add Metadata Per Wallpaper

Each wallpaper must expose:

* Battery mode (Saver / Smooth)
* Animation type (Continuous / Static)
* Effects (Stars, Fog, Rain, etc.)

UI Example:

```
🌙 Night Sky
⭐ Stars Enabled
🔋 Battery Saver
```

---

### 🧠 4. Improve First Screen Experience

Add sections:

* Featured
* Dynamic Wallpapers
* Battery Friendly
* Location-Based

Goal:

> Avoid "empty list" feeling

---

### 🧩 5. Strengthen Categories

Required categories:

* Nature
* Cities
* Space
* Minimal
* Special / Effects

---

### ⚡ 6. Surface Engine Power to User

Expose clearly:

* Real-time day/night cycle
* Location-based sun
* Performance modes

These should be visible in:

* UI
* Store listing

---

### 🧱 7. Improve WallpaperCatalog Structure

Current problem:

* Hardcoded
* Not scalable

Next step:

* Move to structured data (JSON / DB / config)

---

### 🔄 8. Show Render Mode Differences

Expose visually:

* "Smooth Animation"
* "Battery Saver"

This increases perceived value

---

### 🧪 9. Ensure Each Wallpaper Feels Different

Checklist per wallpaper:

* Different shader logic
* Different animation
* Different color mood
* Different foreground composition

If NOT → it does NOT count as new content

---

### 📱 10. Store Listing Optimization

Must show:

* Different wallpapers visually
* Feature highlights

Use phrases:

* Dynamic OpenGL wallpapers
* Real-time day/night cycle
* Battery optimized
* Location-aware lighting

---

## 🚀 Final Target State

When ALL completed:

* 16–18 wallpapers
* 12+ unique behaviors
* Strong UI categories
* Clear feature communication

➡️ Result:

> High chance of Play Store approval

---

## ⚠️ Final Rule

> Texture change ≠ new wallpaper

Only count if:

* Behavior changes
* Visual logic changes
* Experience changes

---

## 📌 Summary

Current:

* Good architecture ✅
* Decent UI ✅
* Weak content differentiation ❌

After update:

* Strong product perception
* High approval probability

---

## Next Step

Implement in this order:

1. Fix city shaders
2. Add 4 new wallpapers
3. Add metadata system
4. Improve home screen sections
5. Update store listing

---

END
