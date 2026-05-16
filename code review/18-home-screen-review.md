# HomeScreen Review

📄 Dosya: `app/src/main/java/com/example/lumisky/ui/home/HomeScreen.kt`

Genel Değerlendirme:  
Ana ekran Compose UI’sini, kategori gruplamayı, focus seçimini, snapshot/live preview geçişini, render asset prewarm akışını ve kart badge’lerini yöneten büyük ekran dosyası. Kullanıcı deneyimi açısından zengin bir yapı kuruyor; ancak UI, render lifecycle koordinasyonu ve iş kuralları aynı dosyada yoğunlaştığı için bakım maliyeti yüksek.

Tespit Edilen Sorunlar:  
- Dosya çok büyük ve SRP zayıf: kategori çözümleme, focus orchestration, preview renderer kurulumu, bitmap yükleme, badge kararları ve placeholder tasarımı aynı dosyada.
- `resolveCategory()` wallpaper id prefix’lerine göre UI içinde iş kuralı uyguluyor. Manifest/catalog tarafı değişirse kategori davranışı UI kodunda unutulabilir.
- `rememberSnapshotPreviewBitmap()` içinde `loadBitmap(...)` doğrudan `produceState` bloğunda çağrılıyor. Loader senkron asset/bitmap decode yapıyorsa main thread üzerinde pahalı iş riski var.
- Focus state akışı birden fazla `LaunchedEffect`, delay ve scroll state’e bağlı. Yarış durumlarında eski focus adayı geç dispatch edilebilir veya gereksiz preview lifecycle değişimi tetiklenebilir.
- `FocusedWallpaperPreview()` AndroidView factory içinde renderer, handler, atomics ve texture loader kuruyor. Bu render kurulumu UI composable içinde fazla detaylı ve test etmesi zor.
- `contentDescription = "App Logo"` hard-coded ve lokalize değil. Ayrıca dekoratif logo ise contentDescription null olmalı.
- Badge görünürlüğü için `item.config.id == "sky"` veya shader path contains kontrolü kullanılıyor. Feature bilgisi config/capability alanından gelmeli.
- Kart boyutu sabit `276.dp` üzerinden kuruluyor. Dar ekranlarda yatay taşma ve erişilebilir font ölçeğiyle metin sıkışması riski var.

🛠️ Geliştirme Planı:  
1. Dosyayı davranış değiştirmeden parçalara ayır: kategori modelleme, focus coordinator, preview card ve badge helper’ları ayrı küçük dosyalara taşınabilir.
2. `resolveCategory()` kuralını catalog/manifest katmanına taşı; UI sadece hazır kategori alanını render etsin.
3. Snapshot bitmap yüklemesini `withContext(Dispatchers.IO)` ile güvenceye al veya loader API’sini async/IO-safe hale getir.
4. Focus akışını tek state holder/helper altında topla; `LaunchedEffect` sayısını azaltıp delay iptallerini daha deterministik yap.
5. `FocusedWallpaperPreview` renderer factory kurulumunu küçük bir helper/factory fonksiyonuna çıkar; Composable sadece AndroidView bağlasın.
6. Logo contentDescription değerini string resource’a taşı veya dekoratifse `null` yap.
7. Parallax badge kararını id/path sezgisi yerine config capability/feature alanına bağla.
8. Card width için responsive clamp kullan; küçük ekran ve büyük font ölçeğinde başlık/badge taşmasını test et.
9. Test hedefleri: kategori gruplama, focus dispatch gecikmesi, snapshot yükleme thread’i, live preview rebuild, dar ekran layout ve accessibility text scale.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `SettingsScreen.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
