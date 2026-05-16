# WallpaperEglSession Review

📄 Dosya: `wallpaper/src/main/java/com/example/wallpaper/render/WallpaperEglSession.kt`

Genel Değerlendirme:  
EGL display/context/surface lifecycle’ını ve `PreviewSkyProgram` çizimini yöneten düşük seviye render sınıfı. Dosya kompakt ve sorumluluğu teknik olarak net; ancak EGL hata yolları, kısmi init temizliği ve GL kaynak release sırası daha sağlam hale getirilmeli.

Tespit Edilen Sorunlar:  
- `attach()` içinde adımlardan biri başarısız olursa o ana kadar oluşturulan EGL kaynakları hemen temizlenmiyor. Sonraki `attach()` başında `release()` bunu toparlayabilir ama başarısız attach sonrası session yarı-açık state’te kalabilir.
- `release()` içinde `skyProgram.release()` çağrısı `eglMakeCurrent(...)` öncesinde yapılıyor. Program release GLES kaynaklarını siliyorsa aktif context garanti edilmeden çağrılması riskli.
- EGL çağrılarında `eglGetError()` tabanlı ayrıntılı hata log’u yok. `attach`, `draw`, `reconfigure` başarısızlıklarında üst katman sadece `false` görüyor, kök neden kayboluyor.
- `draw()` içinde `eglSwapBuffers()` false dönerse surface/context invalid hale gelmiş olabilir; sınıf sadece `false` dönüyor ve state’i temizlemiyor.
- Viewport ilk değerleri `holder.surfaceFrame` üzerinden alınıyor. Frame 0 dönerse `draw()` sırasında query var, fakat `attach()` sonunda program viewport’u 0 ile set edilebilir.
- `preservedSwapBehaviorEnabled` set ediliyor ama sınıf içinde okunmuyor. Debug/diagnostic için değilse state alanı gereksiz.
- Sınıfın thread beklentisi açık değil. EGL context tek thread’e bağlı olduğundan bu sınıfın yalnızca render thread’den çağrılması gerektiği contract olarak görünür değil.

🛠️ Geliştirme Planı:  
1. `attach()` akışını tek success/failure bloğuna çevir; herhangi bir adım false dönerse `release()` çağırıp temiz state ile çık.
2. `release()` içinde önce mümkünse mevcut EGL context’i current yap, sonra `skyProgram.release()` çağır; ardından surface/context/display kaynaklarını yok et.
3. Küçük bir `logEglError(operation)` helper ekle ve kritik false dönüşlerinde operation adıyla hata kodunu logla.
4. `eglSwapBuffers()` başarısızlığında en azından hata logla; `EGL_BAD_SURFACE` / `EGL_CONTEXT_LOST` gibi durumlarda session temizleme politikasını belirle.
5. `attach()` sonunda viewport 0 ise `queryViewportSize()` dene; hâlâ 0 ise program viewport set etmeyi draw zamanına bırak.
6. Kullanılmıyorsa `preservedSwapBehaviorEnabled` alanını kaldır veya debug amaçlı okunabilir bir accessor/log ile anlamlı hale getir.
7. Sınıf başına kısa bir contract yorumu ekle: tüm public fonksiyonlar aynı render thread’den çağrılmalı.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `SkyRenderer.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
