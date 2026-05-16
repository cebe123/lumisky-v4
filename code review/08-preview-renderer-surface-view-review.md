# PreviewRendererSurfaceView Review

📄 Dosya: `app/src/main/java/com/example/lumisky/ui/common/PreviewRendererSurfaceView.kt`

Genel Değerlendirme:  
Compose/Android View tarafındaki preview yüzeyi ile `PreviewGlRenderer` arasındaki lifecycle, playback, dirty render ve parallax tetikleme köprüsünü yönetiyor. Dosya küçük ve amaca odaklı; ancak UI thread, GL thread ve sensör lifecycle etkileşimi daha açık hale getirilmeli.

Tespit Edilen Sorunlar:  
- `private var parallaxEnabled: Boolean = parallaxEnabled` constructor parametresiyle aynı isimli property kullanıyor. Çalışsa bile okunabilirlik zayıf ve yanlış anlaşılmaya çok açık.
- `onPlaybackStateChanged` callback’i `queueEvent` ile GL thread’de çalıştırılıyor. Callback UI/Compose state’e dokunursa thread hatası oluşabilir.
- `queueEvent` çağrıları `runCatching` ile sessizce yutuluyor; yüzey kapanırken callback/release başarısızlıkları loglanmadan kaybolabilir.
- `onAttachedToWindow()` içinde `parallaxEnabled` true ise görünürlük kontrolü olmadan sensör tracker başlatılıyor. View attach olup henüz görünür değilse gereksiz sensör kullanımı olabilir.
- `onDetachedFromWindow()` içinde tracker sadece `parallaxEnabled` true ise kapatılıyor. Tracker nesnesi her zaman oluşturulduğu için `close()` idempotent ise koşulsuz kapatmak daha güvenli.
- `frameLoop` UI Choreographer tarafında `previewRenderer.nextFrameDelayMs()` ve `shouldContinueRendering()` çağırıyor. Renderer state GL thread’de güncellendiği için thread contract belirsiz.
- Warmup frame sayacı frame request gönderildiğinde azalıyor; gerçek frame çizimi başarısız olsa bile warmup tamamlanmış sayılabilir.

🛠️ Geliştirme Planı:  
1. Constructor parametresini `initialParallaxEnabled` gibi ayrı isimle değiştir; property initializer okunabilir olsun.
2. `onPlaybackStateChanged` callback thread contract’ını netleştir. UI callback ise `queueEvent` yerine main thread’de çağır; GL callback ise isim/KDoc ile belirt.
3. `queueEvent` başarısızlıklarını sessiz yutma yerine kısa `Logger.w` ile kaydet.
4. `onAttachedToWindow()` içinde parallax başlatma koşuluna `windowVisibility == View.VISIBLE` ekle.
5. `onDetachedFromWindow()` içinde `tiltParallaxTracker.close()` çağrısını koşulsuz ve idempotent kabul ederek yap.
6. `PreviewGlRenderer` ile thread sınırını sadeleştir: frame delay ve continue kararları UI thread’den güvenli okunacak immutable/volatile state’e indirgensin.
7. Warmup tamamlanma sinyalini mümkünse `onFrameDrawn` gibi gerçek draw callback’iyle ilişkilendir.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `HomeViewModel.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
