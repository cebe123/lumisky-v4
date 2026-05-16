# MainActivity Review

📄 Dosya: `app/src/main/java/com/example/lumisky/MainActivity.kt`

Genel Değerlendirme:  
Uygulamanın ana lifecycle, Compose navigation, tema/dil uygulama, startup cache warmup, wallpaper apply flow, system location panel, lock-screen sharing restore ve in-app review akışlarını yöneten merkezi Activity. Uygulama orkestrasyonu burada doğal olarak toplanmış; ancak dosya çok fazla platform ve domain sorumluluğu taşıyor, bu da test edilebilirliği ve lifecycle güvenliğini zayıflatıyor.

Tespit Edilen Sorunlar:  
- Activity SRP açısından çok geniş: UI navigation, ViewModel sahipliği, wallpaper apply, sun-times refresh, lock-screen policy, locale, startup cache ve review flow aynı sınıfta.
- `HomeViewModel` manuel oluşturuluyor ve gerçek lifecycle ViewModel kullanılmıyor. Configuration change ve `onDestroy()` timing durumlarında state/release davranışı kırılgan.
- `applyWallpaperWithFreshSunTimes()` executor içinde repository async API’sini latch ile bloklayarak bekliyor. Callback gelmezse timeout var ama thread contract ve cancellation net değil.
- `applyingWallpaper` `@Volatile` Boolean ile korunuyor ama check/set atomik değil. Çok hızlı çift tıklamada yarış ihtimali kalır.
- `sunTimesRepository.release()` hem `MainActivity.onDestroy()` içinde hem de `HomeViewModel.release()` -> coordinator -> repository zincirinde çağrılabilir. Repository sahipliği belirsiz.
- `notifyWallpaperConfigChanged()` internal broadcast’i sadece package ile sınırlandırıyor; explicit component/receiver contract yok.
- `warmHomeStartupCaches()` tüm item snapshot’larını sırayla yüklüyor. Wallpaper sayısı artarsa startup gate gereğinden uzun sürebilir.
- `statusBarPaintMethod` reflection ile status bar color çağırıyor. Normal public API kullanılabiliyorsa reflection gereksiz karmaşıklık.

🛠️ Geliştirme Planı:  
1. Activity’yi küçük coordinator sınıflarına böl: wallpaper apply coordinator, lock-screen restore coordinator, startup warmup coordinator, location panel handler.
2. `HomeViewModel` için gerçek `androidx.lifecycle.ViewModel` + factory kullan; manuel `mutableStateOf<HomeViewModel?>` sahipliğini azalt.
3. `applyingWallpaper` için `AtomicBoolean.compareAndSet(false, true)` kullan; apply flow girişini atomik hale getir.
4. Sun-times fresh resolve için latch yerine suspend/timeout tabanlı API veya tek senkron repository metodu kullan; callback thread bağımlılığını kaldır.
5. `SunTimesRepository` sahipliğini tek yere indir; Activity ve coordinator aynı instance’ı ayrı ayrı release etmesin.
6. Internal wallpaper config update broadcast’ini explicit component veya açık tek alıcı contract ile gönder.
7. Startup warmup için üst limit uygula: tüm snapshot yerine ilk görünür kategori/kartlar ve asset warm limit ayrı ayrı kontrol edilsin.
8. Status/navigation bar update için public `window.statusBarColor` / `navigationBarColor` API yeterliyse reflection helper’ı kaldır.
9. Test hedefleri: çift wallpaper apply tıklaması, apply timeout fallback, Activity recreate, startup warmup süresi, external chooser dönüşü, lock-screen restore preference.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `TiltParallaxTracker.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
