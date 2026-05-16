# WallpaperRenderController Review

📄 Dosya: `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRenderController.kt`

Genel Değerlendirme:  
Live wallpaper render lifecycle’ını yöneten kritik koordinasyon sınıfı. Sorumluluk doğru yerde: surface, visibility, config, scheduler ve render loop kararlarını tek noktada topluyor. Ancak sınıf render thread ile servis/main thread arasında fazla paylaşımlı state taşıyor; bu da okunabilirlik ve lifecycle güvenliği açısından ana risk.

Tespit Edilen Sorunlar:  
- Thread confinement net değil. `visible`, `surfaceAttached`, `currentConfig`, `pendingConfig`, `surfaceGeneration`, `pendingFullRedraw` farklı thread’lerden okunup yazılıyor; bazıları `@Volatile`, bazıları değil.
- `postRenderTaskBlocking()` render thread’den çağrılırsa kendi kuyruğunu bekleyerek timeout’a düşebilir. Bu küçük ama lifecycle sırasında ciddi gecikme riski.
- `setParallaxOffset()` diğer render işlemlerinin aksine doğrudan `renderEngine` üstünden çağrılıyor. Eğer engine GL/render-thread state’ine dokunuyorsa thread güvenliği bozulabilir.
- `updateRenderLoopsLocked()` / `stopRenderLoopsLocked()` isimleri lock varmış gibi davranıyor ama gerçek lock yok. Bu Clean Code açısından yanıltıcı.
- `attachSurface()` başarılı/başarısız durumunu controller seviyesinde modellemiyor. Controller `surfaceAttached = true` durumuna erken geçiyor; attach başarısız olursa scheduler/render state iyimser kalabilir.

🛠️ Geliştirme Planı:  
1. Controller state sahipliğini netleştir: render thread’e ait state’leri yalnızca `renderHandler.post {}` içinde değiştir; servis thread’den okunan minimum state’i `@Volatile` veya `Atomic*` ile sınırla.
2. `postRenderTaskBlocking()` içine aynı looper kontrolü ekle: çağrı zaten render thread’deyse task’ı direkt çalıştır.
3. `setParallaxOffset()` çağrısını render thread’e taşı veya `WallpaperRenderEngine.setParallaxOffset()` gerçekten thread-safe ise bunu kısa yorum/isimlendirme ile açık hale getir.
4. `Locked` suffix’li fonksiyonları gerçek davranışa göre yeniden adlandır: örn. `updateRenderLoopsOnRenderThread`, `stopRenderLoopsOnRenderThread`.
5. Orta vadede `attachSurface()` için başarı durumu döndüren küçük bir sözleşme düşün; başarısız attach durumunda `surfaceAttached` ve scheduler state’i geri alınmalı.
6. Bu değişikliklerden sonra özellikle şu senaryolar için mevcut unit testlere ek test ekle: destroy sırasında blocking post, hızlı surface destroy/create, config değişirken visibility değişimi.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperRenderEngine.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
