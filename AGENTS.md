# AGENTS.md

Bu dosya `Lumisky` deposunda çalışan ajanlar (Codex vb.) için repo-seviyesi çalışma notlarını içerir.

## Proje Özeti

- Android (multi-module) canlı duvar kağıdı uygulaması.
- Dil/stack: Kotlin + Android SDK + Compose (özellikle `:app` içinde).
- Gradle wrapper kullanılır (`gradlew` / `gradlew.bat`).
- Aktif modüller (`settings.gradle.kts` kaynağı): `:app`, `:core`, `:engine`, `:wallpaper`.

## Modül Sorumlulukları

- `app`: Activity'ler (`MainActivity`, `PreviewActivity`), Compose UI ekranları, kullanıcı akışı, wallpaper seçimi/uygulama.
- `core`: Ortak yardımcılar (logger, settings repository, location provider, sun-times API/repository).
- `engine`: Render/domain çekirdeği (gökyüzü hesapları, renderer, config, preview GL renderer).
- `wallpaper`: `WallpaperService` yaşam döngüsü, render controller/EGL/scheduler, stored config apply akışı.

## Önemli Mimari Notlar

- Manifest, servis olarak `com.example.wallpaper.SkyWallpaperService` sınıfını kullanır.
- Bu sınıf bir wrapper'dır ve gerçek implementasyon `wallpaper.service.SkyWallpaperService` içindedir.
- Wallpaper konfigürasyonu için `WallpaperConfigStore` kullanılır; servis yenilemesi broadcast ile tetiklenir (`ACTION_APPLY_STORED_WALLPAPER_CONFIG`).
- Ağır işler (sun-times fetch, wallpaper apply vb.) UI thread dışında çalıştırılır. Bu pattern'i koru.

## Build / Test Komutları (Windows PowerShell)

- Tüm unit testler: `.\gradlew test`
- Belirli modül testleri:
  - `.\gradlew :engine:testDebugUnitTest`
  - `.\gradlew :core:testDebugUnitTest`
  - `.\gradlew :wallpaper:testDebugUnitTest`
  - `.\gradlew :app:testDebugUnitTest`
- Debug APK: `.\gradlew :app:assembleDebug`
- Lint: `.\gradlew :app:lintDebug`

## Asset Pipeline (Kritik)

- `:app` modülünde `preBuild`, `prepareFilteredAssets` görevine bağlıdır.
- `prepareFilteredAssets`, `convertWallpaperTexturesToWebp` görevini tetikler.
- Kaynak dizin: `app/src/main/assets`
- Paketleme için kullanılan çıktı: `app/build/generated/filteredAssets/main`
- `png/jpg/jpeg` dosyaları build sırasında filtrelenir; `webp` asset'ler paketlenir.
- `assets/shaders/**` dönüşümden hariç tutulur.

Çalışma kuralı:

- `app/build/**` altındaki generated asset'leri elle düzenleme.
- Yeni raster texture ekliyorsan mümkünse `.webp` çıktısını da üret ve doğrula.
- Kullanıcının mevcut asset değişikliklerini (özellikle `app/src/main/assets/**`) izinsiz geri alma/silme.

## Kodlama Kuralları (Repo-Özel)

- Mevcut stil korunmalı: Kotlin/Gradle dosyalarında indentation çoğunlukla `tab`.
- Yeni loglar için `com.example.core.Logger` kullan; `println` ekleme.
- Android lifecycle register/unregister akışlarında mevcut pattern'i koru (özellikle receiver kayıtları ve API 33+ branch'leri).
- Ayarlar için dağınık `SharedPreferences` erişimi açma; `AppSettingsRepository` üzerinden ilerle.
- Modül sınırlarını koru:
  - `app` -> UI/orchestration
  - `engine` -> render logic
  - `wallpaper` -> service/runtime integration
  - `core` -> shared infra/helpers

## Doküman Sapması (Bilinen)

- `docs/PROJECT_STRUCTURE.md` içinde `:snapshot` modülü listeleniyor.
- Güncel Gradle konfigürasyonunda (`settings.gradle.kts`) `:snapshot` modülü yok.
- Modül kararlarında öncelik `settings.gradle.kts` ve gerçek kaynak ağacında olmalı.

## Değişiklik Yaparken

- Önce ilgili modülde minimum kapsamlı değişiklik yap, çapraz modül taşmasını gerekçesiz büyütme.
- İlgili unit test varsa çalıştır; yoksa en azından etkilenen modülü build et.
- `build/`, `.gradle/`, IDE metadata (`.idea/`) dosyalarını gereksiz yere commit'e sokma.

