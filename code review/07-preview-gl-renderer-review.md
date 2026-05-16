# PreviewGlRenderer Review

📄 Dosya: `engine/src/main/java/com/example/engine/preview/PreviewGlRenderer.kt`

Genel Değerlendirme:  
GLSurfaceView preview renderer lifecycle’ını, preview playback modlarını, focus catch-up animasyonunu, adaptive FPS/quality kararlarını ve `PreviewSkyProgram` çizimini koordine ediyor. Dosya işlevsel olarak merkezi ve test edilebilir helper’lar içeriyor; ancak UI thread ile GL thread arasında paylaşılan state, callback thread’i ve sorumluluk yoğunluğu ana riskler.

Tespit Edilen Sorunlar:  
- Sınıf SRP açısından geniş: GL lifecycle, zaman/progress hesaplama, focus animasyonu, thermal/battery FPS policy, adaptive quality ve parallax aynı sınıfta.
- `setFocusPlaybackEnabled()` muhtemelen UI thread’den çağrılabilir; `previewStartMillis`, `focusStartProgress`, `focusTargetProgress`, `focusFinalState` gibi alanlar GL thread tarafından da okunuyor ama thread confinement net değil.
- `onRenderedDayProgressChanged` ve `onFrameDrawn` callback’leri GL thread’den çağrılıyor. Bu callback’ler Compose/UI state’e dokunuyorsa yanlış thread riski oluşur.
- `focusFinalState` mutable `RenderFrameState` referansı tutuyor. Engine state buffer yeniden kullanıyorsa tamamlanan focus state daha sonra değişebilir.
- Adaptive quality `skyProgram.setRenderQuality(...)` çağırıyor; kalite texture seçimini etkiliyorsa mevcut texture’lar yeniden yüklenmeden sadece flag seviyesi değişiyor.
- `MIN_DISPLAY_FPS = 60` düşük yenileme hızlı cihazları veya 30 Hz hedefleri 60 FPS tabanına zorluyor; wallpaper tarafındaki 30 Hz alt sınırıyla tutarsız.
- `nextFrameDelayMs()` integer bölme ile gecikme üretir; 120 FPS için 8 ms gibi hedefin üstüne çıkan efektif FPS oluşabilir.
- `Long.floorMod()` private extension tanımlı ama kullanılmıyor; okunabilirlik açısından ölü kod.

🛠️ Geliştirme Planı:  
1. Thread contract’ı netleştir: public mutator fonksiyonların hangi thread’den çağrılacağı KDoc ile belirtilsin veya GL thread’e post edilen bir komut kuyruğu kullanılsın.
2. UI callback’leri için açık politika belirle: callback’ler GL thread’den çağrılıyorsa isim/KDoc ile belirt; UI tüketicileri main thread’e marshal etmeli.
3. Focus playback state’ini küçük bir state holder’a ayır; mutasyonları tek thread üzerinde topla.
4. `focusFinalState` için mutable referans yerine snapshot/copy kullan veya tamamlanan focus state’i tekrar `sampleAtDayProgress` ile üret.
5. Adaptive FPS/quality kararlarını küçük bir policy sınıfına çıkar; renderer sadece policy sonucunu uygulasın.
6. Display FPS alt sınırını engine/wallpaper tarafıyla hizala; 30 Hz desteklenecekse `MIN_DISPLAY_FPS` değerini de buna göre düşür.
7. `nextFrameDelayMs()` için nanos tabanlı veya yuvarlamalı hesaplama kullan; hedef FPS’i aşmayacak gecikme üret.
8. Kullanılmayan `Long.floorMod()` extension’ını kaldır.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `PreviewRendererSurfaceView.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
