# Lumisky V2 - Detaylı Proje Ağacı ve Mimarisi

Lumisky V2 projesi, Android işletim sistemi için geliştirilmiş oldukça gelişmiş ve modüler bir Canlı Duvar Kağıdı (Live Wallpaper) uygulamasıdır. Proje, "Separation of Concerns" (Sorumlulukların Ayrılması) prensibine sıkı sıkıya bağlı kalınarak 4 temel modüle 🏗️ ayrılmıştır: `app`, `core`, `engine` ve `wallpaper`. 

Aşağıda hem geliştiriciler hem de yapay zeka asistanları için referans niteliğinde tasarlanmış projenin tüm anatomisini, klasör yapısını ve modüller arası ilişkisini gösteren **kapsamlı ve detaylı proje ağacını** bulabilirsiniz:

### 🌳 Detaylı Proje Ağacı (Project Tree)

```text
Lumisky V2/
│
├── 📱 app/                   (Kullanıcı Arayüzü, Ayarlar ve Tema Yönetimi Modülü)
│   ├── build.gradle.kts      (App modülü bağımlılıkları)
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── assets/           (Shader ve Görsel Varlıklar Klasörü)
│       │   ├── anime/, backgrounds/, flower/, mars/, pixel_forest/ vb. (Gökyüzü temaları)
│       │   ├── shaders/      (GLSL Fragment Shader kodları - Görüntü render mantığı)
│       │   │   ├── anime_sakura/fragment.glsl
│       │   │   ├── city/fragment_shader.glsl
│       │   │   └── ... (Diğer shader kodları)
│       │   └── wallpapers/   (Tema manifest ve konfigürasyonları)
│       │       ├── city_istanbul/manifest.json
│       │       └── pixel_forest/manifest.json vb.
│       ├── java/com/example/lumisky/
│       │   ├── MainActivity.kt   (Uygulamanın ana başlatıcı ekranı)
│       │   ├── ui/               (Kullanıcı arayüzü bileşenleri)
│       │   ├── viewmodel/        (UI durum yönetimi ve iş mantığı)
│       │   ├── data/             (Arayüz odaklı veri işlemleri)
│       │   ├── shader/           (Varlıklardaki shader'ları okuyan yönetici sınıflar)
│       │   └── snapshot/         (Önizleme ve "Snapshot" oluşturucu mantıklar)
│       └── res/              (Android XML Kaynakları)
│           ├── values-*/         (Farklı diller için çoklu dil [i18n] destek klasörleri)
│           └── xml/sky_wallpaper.xml
│
├── ⚙️ engine/                (Görselleştirme ve Render Motoru Modülü - KALP)
│   ├── build.gradle.kts
│   └── src/main/java/com/example/engine/
│       ├── SkyEngine.kt      (Ana render motoru giriş noktası)
│       ├── renderer/         (OpenGL / Canvas merkezli render yöneticileri)
│       ├── shader/           (Shader derleme ve bağlama programları)
│       ├── atmosphere/       (Atmosfer, bulut ve ışık kırma render mantığı)
│       ├── celestial/        (Güneş, Ay vb. gök cisimlerinin renderı)
│       ├── sky/              (Genel gökyüzü renk geçişleri ve gradientler)
│       ├── time/             (Simülasyon zamanlayıcısı ve DeltaT animasyonları)
│       └── config/           (Görsellerin config veri sınıfları)
│
├── 🖼️ wallpaper/             (Android Live Wallpaper Entegrasyon Modülü - KÖPRÜ)
│   ├── build.gradle.kts
│   └── src/main/java/com/example/wallpaper/
│       ├── SkyWallpaperService.kt (Android manifesti için servis tanımlayıcısı)
│       ├── service/               (Gerçek Wallpaper Service implementasyon arka planı)
│       ├── engine/                (Grafik motorunu -engine modülü- sarmalayan kısımlar)
│       └── render/                (Android Duvar Kağıdı olaylarını engine'e taşıyan köprü)
│
├── 🧠 core/                  (Ortak Altyapı ve Paylaşılan Araçlar Modülü)
│   ├── build.gradle.kts
│   └── src/main/java/com/example/core/
│       ├── Logger.kt         (Merkezi terminal/loglama aracı - println yerine kullanılır)
│       ├── TimeProvider.kt   (Uygulama çapında merkezi saat ve zaman üreteci)
│       ├── settings/         (Cihaz depolaması - Settings: AppSettingsRepository vb.)
│       ├── perf/             (Performans ve FPS gözlem araçları)
│       ├── location/         (Kullanıcı konumu - GPS - gerçek zamanlı güneş yönü için)
│       ├── api/              (Uygulama genelinde kullanılan API kontratları)
│       └── assets/           (Dosya sisteminden ortak veri okuma/yazma araçları)
│
├── 📁 benchmark/             (Mikro Benchmark testleri. Performans, yük ve hafıza testleri)
├── 📚 docs/                  (Geliştirme ve Mimari Dökümanları)
│   ├── OPTIMIZATIONS.md      (Motor ve render optimizasyonu stratejileri)
│   ├── PREVIEW_RENDER_OPTIMIZATION_REPORT.md
│   └── ...                   (Eski mimari notlar vb.)
│
├── 🛠️ Root Konfigürasyon ve Scriptler
│   ├── build.gradle.kts / settings.gradle.kts (Zirve Gradle yapılandırması)
│   ├── AGENTS.md             (Yapay zeka araçlarının sıkı sıkıya uyması gereken mimari kurallar)
│   ├── gradle.properties     (Lokal geliştirme ve derleme parametreleri)
│   └── GenerateWallpaperZenithSnapshots.cmd (Duvar kağıtları için önizleme otomasyon betiği)
└── 🎨 images/                (Tasarım çıktıları vb. örn: settings_screen_concept.png)
```

---

### 🧩 Modüllerin Sorumlulukları ve Katı Mimari Kuralları (Architecture & Ownership)

Proje üzerindeki `AGENTS.md` belgesindeki kurallara dayalı olarak, uygulamanın işleyiş akışı şu prensiplere dayanır:

1. **`app` Modülü (Kullanıcı Deneyimi ve Arayüz)** 📱
   * Uygulamanın beynini kontrol eden UI bölümüdür. Geri kalan kısımlardan (örn: motorun nasıl çalıştığından) tamamen izoledir.
   * Derleme esnasında (build time) içindeki ham assetler (raster görseller/resimler) uygulamanın pipeline'ından geçerek webp gibi formatlara çevrilir. Özel GLSL shader'ları bu pipeline'dan muaf tutulur.

2. **`engine` Modülü (Grafik ve Simülasyon Motoru)** ⚙️
   * Gökyüzü hesaplamaları, fragment shader yüklemeleri, astronomik konumlandırmalar (Ay/Güneş nerede) sadece bu modülün sorumluluğundadır. Android'e özgü duvar kağıdı servislerinden tamamen bihaber şekilde sadece piksel yaratmakla ilgilenir.

3. **`wallpaper` Modülü (Entegrasyon / Service)** 🖼️
   * Android işletim sisteminin özellikleriyle `engine` modülü arasındaki bağlantıyı kurar. Ekran sola mı kaydırıldı, cihazın ekranı mı kapandı, sistemsel olaylar burada dinlenerek Engine'e bildirilir. Uygulanacak özel kurallara göre (`SkyWallpaperService`), hiçbir zaman asıl iş mantığı manifest wrapper sınıfına yazılmaz, hep alttaki dizinlerde tutulur.

4. **`core` Modülü (Ortak ve Güvenli Yaşam Alanı)** 🧠
   * Modüller arası bir paylaşım lazımsa buraya yazılır. Eski `SharedPreferences` erişimleri artık doğrudan yapılmaz; `core/settings` dizinindeki yeni ortak yapı olan `AppSettingsRepository` üzerinden bağlanılır. Hatalar için her zaman `com.example.core.Logger` kullanılır, hiçbir zaman sisteme düz `println` basılmaz.

**🔁 Duvar Kağıdı Uygulama Otomasyonu (Wallpaper Apply Flow):**
Bu dört yapı arasındaki en büyük akış, Duvar kağıdı seçimidir.
`App` üzerinden yeni bir shader teması seçilir -> `WallpaperConfigStore` tetiklenerek bu seçim veri depolarına indirilir -> Seçimden sonra Android genelinde kapsayıcı bir Broadcast eylemi yayınlanır (`ACTION_APPLY_STORED_WALLPAPER_CONFIG`) -> `Wallpaper` servisi bu sinyali yakalayarak durumu okur ve -> `Engine` (Render) servisini yeni shader parametreleriyle başlatır.
