# WallpaperConfigStore Review

📄 Dosya: `engine/src/main/java/com/example/engine/config/WallpaperConfigStore.kt`

Genel Değerlendirme:  
Seçili ve preview wallpaper config’lerini SharedPreferences üzerinde credential/device protected storage’a yazan kalıcılık katmanı. Reboot/direct boot senaryosu için device protected kopya tutulması doğru bir mimari karar. Ancak dosya store sorumluluğu ile büyük JSON codec/migration sorumluluğunu aynı yerde taşıyor; hata görünürlüğü, schema evrimi ve doğrulama tarafı zayıf.

Tespit Edilen Sorunlar:  
- Dosya çok büyük ve iki ayrı sorumluluk içeriyor: persistence store ve `WallpaperConfigJsonCodec`. Bu SRP ve okunabilirlik açısından zayıf.
- `decode()` tüm hataları `getOrNull()` ile sessizce yutuyor. Bozuk kayıt siliniyor ama neden bozulduğu loglanmıyor.
- Persisted JSON’da açık schema/version alanı yok. Yeni alanlar veya enum/key değişikliklerinde migration kararları dağınık fallback’lere kalıyor.
- Enum parsing `valueOf(raw)` ile case-sensitive; eski/küçük harfli kayıtlar sessizce fallback’e düşebilir.
- Numeric alanlarda kapsam/NaN validasyonu sınırlı. Horizon, süre, effect intensity/density ve uniform değerleri geçersiz şekilde modele taşınabilir.
- `encodeOrbit()` nested orbit içinde `KEY_SUN_PATH_TYPE` kullanıyor. Hem sun hem moon orbit için aynı key semantik olarak kafa karıştırıcı; `pathType` gibi orbit’e özel key daha temiz olurdu.
- `writeEncoded()` / `removeEncoded()` içinde `commit()` başarısız olursa aynı editor’a `apply()` çağrılıyor ama sonuç loglanmıyor; kalıcılık hatası görünmez kalıyor.
- Credential ve device protected kayıtlar farklı geçerlilikteyse seçim politikası device protected kaydı öncelikli alıyor. Bu doğru olabilir ama timestamp/version olmadan “hangisi daha güncel” bilinmiyor.

🛠️ Geliştirme Planı:  
1. `WallpaperConfigJsonCodec` sınıfını ayrı dosyaya taşı; store dosyası sadece read/write/promote akışını yönetsin.
2. `decode()` hata yakaladığında kısa `Logger.w` bas; key veya config id biliniyorsa log’a ekle.
3. JSON’a `schemaVersion` alanı ekle ve backward compatibility kararlarını merkezi migration helper’da topla.
4. Enum parser’larını normalize et veya persisted formatı açıkça büyük harfli enum ismi olarak belgeleyin.
5. Numeric decode için clamp/validate helper’ları ekle; NaN ve sonsuz değerler fallback’e dönsün.
6. Orbit JSON anahtarını uzun vadede `pathType` olarak standardize et; eski `sunPathType` key’ini backward-compatible fallback olarak okumaya devam et.
7. SharedPreferences write/remove başarısızlıklarını logla; device protected ve credential write sonucunu ayrı görünür hale getir.
8. Device protected ve credential kayıtlar için gerekirse `updatedAt` veya schema/version tabanlı seçim politikası ekle.
9. Test hedefleri: bozuk JSON siliniyor/loglanıyor mu, eski animation key migration, invalid numeric fallback, orbit key compatibility, device-vs-credential önceliği.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperSelectionState.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
