# Wallpapers Index Review

📄 Dosya: `app/src/main/assets/wallpapers/index.json`

Genel Değerlendirme:  
Wallpaper katalog manifestlerinin yükleme sırasını belirleyen küçük asset index dosyası. Yapı basit ve okunabilir; `WallpaperManifestCatalogSource` için tek giriş noktası olarak görev yapıyor. Ana riskler schema doğrulama eksikliği, metadata yetersizliği ve kategori/sıralama gibi kullanıcıya görünen kararların başka katmanlara dağılmış olması.

Tespit Edilen Sorunlar:  
- Dosyada sadece `id` ve `manifest` var. Kategori, görünürlük, sıralama ağırlığı veya feature flag gibi katalog kararları burada açık değil.
- Duplicate `id` veya duplicate `manifest` için asset seviyesinde koruma yok. Parser tarafı da bunu sıkı doğrulamazsa katalog davranışı belirsizleşebilir.
- Index `id` ile manifest içindeki gerçek `id` farklı olabilir; bu dosya bunu önleyemez ve farkın bilinçli override mı hata mı olduğu görünmez.
- Manifest path’leri string olarak tekrar ediyor ve path varlığı bu dosyada doğrulanmıyor.
- Liste sırası kullanıcıya görünen sıralamayı etkiliyorsa bu contract dosyada belgelenmemiş.
- JSON schema/version alanı yok. Gelecekte index formatı genişlerse backward compatibility kararı belirsizleşir.

🛠️ Geliştirme Planı:  
1. Index formatına küçük bir `schemaVersion` alanı eklemeyi değerlendir; parser eski formatsız dosyayı v1 kabul edebilir.
2. Parser tarafında duplicate `id` ve duplicate `manifest` için warning üret.
3. Index `id` ile manifest `id` farklıysa warning veya explicit `overrideIdAllowed` benzeri net bir politika kullan.
4. Kullanıcıya görünen sıralama bu listeye bağlıysa bunu kısa bir dokümantasyon/comment karşılığıyla parser/test tarafında sabitle.
5. Kategori ve feature kararlarını UI prefix kuralları yerine manifest/catalog metadata alanlarına taşımayı planla.
6. Test hedefleri: boş index, eksik manifest path, duplicate id, index-manifest id mismatch ve sıralamanın korunması.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `app/src/main/assets/shaders/city/fragment_shader.glsl` dosyasına geçiş yapmamı onaylıyor musunuz?
