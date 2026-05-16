# WallpaperManifest Review

📄 Dosya: `engine/src/main/java/com/example/engine/manifest/WallpaperManifest.kt`

Genel Değerlendirme:  
Yeni manifest tabanlı wallpaper modelinin serializable DTO katmanını tanımlıyor. Dosya küçük, okunabilir ve bağımlılığı düşük; engine modülünde ortak veri sözleşmesi olarak konumlanması doğru. Ancak model şu an daha çok ham veri taşıyor; doğrulama, enum tutarlılığı ve mevcut `WallpaperConfig` sistemiyle sınırları netleşmeli.

Tespit Edilen Sorunlar:  
- `SceneConfig.sunPathType`, `moonPathType` ve `RenderConfig.quality` string olarak tutuluyor. Bu alanlar typo’ya açık ve parser/adapter tarafına gereksiz doğrulama yükü bindirir.
- `ShaderMode` için `@SerialName` var, ancak `RenderMode` enum değerleri için yok. Manifest schema küçük harf veya stabil wire format isterse tutarsızlık oluşur.
- Numeric alanlarda model seviyesinde sınır yok: FPS, boyut, horizon ve duration değerleri geçersiz aralıklarla taşınabilir.
- `RenderConfig` hem preview hem live wallpaper hem user override policy alanlarını aynı DTO’da topluyor; sorumluluk büyürse okunabilirlik düşebilir.
- `RendererRuntimeMode` serializable değil. Sadece runtime iç kullanım ise sorun değil; manifest/selection içinde kullanılacaksa contract eksik kalır.
- `UserVariantSelection.enabledEffectIds` `Set` olarak tutuluyor. Kalıcı encode/decode veya diff çıktılarında deterministik sıra gerekiyorsa liste tabanlı model daha güvenli olabilir.
- DTO’lar ile eski `WallpaperConfig` alanları arasındaki dönüşüm kuralı bu dosyada görünmüyor; modelin hangi katmanda doğrulanacağı belirsiz.

🛠️ Geliştirme Planı:  
1. `sunPathType`, `moonPathType` ve `quality` için enum veya value class kullanmayı değerlendir; en azından adapter katmanında merkezi doğrulama şartı koy.
2. `RenderMode` enum değerlerine schema uyumlu `@SerialName` ekle; wire formatı Kotlin enum isimlerinden bağımsızlaştır.
3. FPS, duration, size ve horizon değerleri için validator/normalizer katmanı belirle; DTO saf kalacaksa bu karar KDoc ile açık olsun.
4. `RenderConfig` büyürse preview/live/user override alanlarını küçük alt config’lere ayır.
5. `RendererRuntimeMode` kullanımını kontrol et; serialization sınırına girecekse `@Serializable` ve stabil isimlendirme ekle.
6. `UserVariantSelection` için deterministik kayıt gerekiyorsa `Set` yerine `List` kullan veya encode öncesi sıralama politikasını netleştir.
7. Test hedefleri: eksik alanlarla default decode, enum wire isimleri, geçersiz numeric değerlerin adapter’da normalize edilmesi ve selection encode/decode stabilitesi.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperConfigStore.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
