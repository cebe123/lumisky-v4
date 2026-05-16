# WallpaperManifestCatalogSource Review

📄 Dosya: `app/src/main/java/com/example/lumisky/data/WallpaperManifestCatalogSource.kt`

Genel Değerlendirme:  
Asset tabanlı wallpaper index/manifest dosyalarını okuyup `WallpaperCatalogEntry` ve `WallpaperConfig` modeline dönüştüren katalog kaynak katmanı. Dosya uygulamanın veri-konfigürasyon sınırında kritik bir adaptör görevi görüyor. Yapı deterministik ve bağımlılığı düşük; ancak parser çok büyümüş, hataları sessiz geçebiliyor ve bazı iş kuralları manifest şemasından ziyade string sezgilerine bağlı.

Tespit Edilen Sorunlar:  
- `WallpaperManifestParser.parse()` parse hatalarını `getOrNull()` ile sessizce yutuyor. Bozuk manifest katalogdan kaybolur ama hangi dosyanın neden atlandığı görünmez.
- Parser sınıfı çok geniş: schema key’leri, tip dönüştürme, default merge, iş kuralı ve config inşası aynı sınıfta.
- Enum parsing `valueOf(raw)` ile case-sensitive. Manifest değerleri küçük harf veya farklı schema formatıyla gelirse fallback’e sessiz düşer.
- Numeric değerlerde kapsam/NaN validasyonu zayıf. `horizonOffset`, süreler, effect değerleri ve uniform override değerleri geçersiz aralıklarla modele taşınabilir.
- Lighthouse istisnası açık bir manifest alanı yerine `id`, asset path veya texture path içinde `"lighthouse"` aramasıyla belirleniyor. Bu kural kırılgan ve yeni asset isimlerinde yanlış çalışabilir.
- Index ile manifest içindeki `id` farklıysa log/validasyon yok. Bu durum duplicate veya beklenmeyen katalog id’lerine yol açabilir.
- `loadEntries()` duplicate wallpaper id kontrolü yapmıyor; aynı id birden fazla manifestten gelirse sonraki repository davranışı belirsizleşebilir.
- `parseOrThrow()` içinde dışarıdan `runCatching { ... }.getOrThrow()` kullanımı gereksiz; hata yönetimi `parse()` seviyesinde zaten yapılmalı.

🛠️ Geliştirme Planı:  
1. `parse()` içinde hata yakalandığında `sourceAssetPath` ve `fallbackId` ile kısa `Logger.w` bas; manifest sessiz kaybolmasın.
2. Enum parser’larını normalize et: `trim().uppercase()` gibi tek politika kullan veya schema değerlerini açıkça doğrula.
3. Numeric alanlar için küçük clamp/validate helper’ları ekle; süreler minimuma, oranlar beklenen aralığa, NaN değerler fallback’e dönsün.
4. Lighthouse/linear orbit istisnasını uzun vadede açık bir manifest capability/policy alanına taşı; mevcut string sezgisini sadece backward-compatible fallback olarak bırak.
5. `loadEntries()` içinde duplicate `entry.baseConfig.id` tespit edildiğinde logla ve deterministik seçim politikasını uygula.
6. Index `fallbackId` ile manifest `id` farklıysa warning üret; bilinçli override değilse erken görünür olsun.
7. Parser sorumluluklarını küçük helper gruplarına ayır: `decodeTextures`, `decodeShader`, `decodeFeatures`, `decodePolicies`; mevcut dış API değişmeden kalabilir.
8. Test hedefleri: bozuk manifest loglanıyor mu, lowercase enum fallback davranışı, NaN/out-of-range numeric değerler, duplicate id, lighthouse istisnası ve linear orbit kuralı.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperManifest.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
