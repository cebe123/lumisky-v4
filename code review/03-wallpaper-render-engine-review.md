# WallpaperRenderEngine Review

📄 Dosya: `wallpaper/src/main/java/com/example/wallpaper/engine/WallpaperRenderEngine.kt`

Genel Değerlendirme:  
Wallpaper servis render katmanı ile `SkyEngine` ve `WallpaperEglSession` arasında köprü görevi görüyor. Surface attach/detach, config geçişi, shader yükleme, texture byte cache, frame render ve scene hash üretimini aynı sınıfta topluyor. İşlevsel olarak merkezi ve anlaşılır; ancak sınıf sorumluluk yoğunluğu ve EGL başarısızlık durumlarını modele yansıtma konusunda zayıf.

Tespit Edilen Sorunlar:  
- Sınıf SRP açısından fazla geniş: EGL lifecycle, shader asset yükleme, texture cache, frame pacing yardımcıları ve render state üretimi aynı yerde.
- `attachSurface()` içerde başarı/başarısızlık biliyor ama dışarı `Boolean` döndürmüyor. Üst controller attach başarısızlığını doğrudan modelleyemiyor.
- `setConfig()` içinde reconfigure başarısız olup reattach da başarısız olursa `holder` temizlenmiyor. Bu durumda sonraki `renderFrame()` hâlâ surface var sanıp draw denemeye devam edebilir.
- Varsayılan config içinde doğrudan `shaders/opticalsunset/fragment.glsl` ve `lighthouse_like` hard-code edilmiş. Bu, gerçek default davranışı gizli asset bağımlılığına bağlıyor.
- `MIN_REFRESH_RATE_HZ = 60` seçimi 30 Hz cihazları/ayarları 60 Hz gibi ele alır. Controller tarafındaki 30 Hz alt sınırıyla tutarsız bir pacing davranışı doğurabilir.
- `withTextureLoadLock()` lock’u her yükleme sonunda map’ten siliyor. Aynı asset için bekleyen thread varken üçüncü thread yeni lock oluşturursa aynı asset paralel okunabilir.
- `release()` EGL, engine ve texture cache’i temizliyor ama `holder`, preview loop state ve shader override gibi çalışma state’lerini sıfırlamıyor.

🛠️ Geliştirme Planı:  
1. `attachSurface(surfaceHolder)` fonksiyonunu `Boolean` dönecek şekilde düzenle; başarılıysa `holder` ata, başarısızsa `holder = null` bırak ve sonucu controller’a taşı.
2. `setConfig()` içindeki reconfigure/reattach başarısızlık yolunda `holder = null` yap veya attach sonucunu üst katmana bildiren tek bir küçük helper kullan.
3. Shader yükleme tekrarını `loadFragmentShaderFor(config)` gibi küçük private helper’a çıkar; davranışı değiştirmeden okunabilirliği artır.
4. Hard-code default shader yerine gerçek `WallpaperConfig.default(...)` davranışını kullan; gerekiyorsa fallback shader seçimini tek bir açık fallback helper’da topla.
5. Refresh-rate clamp değerlerini controller ile hizala; 30 Hz desteklenecekse bu dosyadaki minimumu da 30 yap.
6. Texture load lock stratejisini sadeleştir: asset başına kalıcı lock map veya referans sayımlı silme kullan; bekleyen thread varken yeni lock oluşmasını engelle.
7. `release()` sonunda `holder = null`, `fragmentShaderOverride = null`, `previewLoopStartNanos = 0L` ve gerekiyorsa cache/lock state resetlerini netleştir.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperEglSession.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
