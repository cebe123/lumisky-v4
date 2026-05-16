# PreviewSkyProgram Review

📄 Dosya: `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt`

Genel Değerlendirme:  
GL shader programı, uniform aktarımı, texture yükleme/binding, fallback texture üretimi ve kalite bazlı efekt kapatma kararlarını yöneten merkezi çizim sınıfı. İşlevsel kapsamı güçlü ama sınıf çok fazla düşük seviye sorumluluğu bir arada taşıyor. En kritik riskler texture unit çakışmaları, GL resource lifecycle belirsizlikleri ve `draw()` metodunun aşırı büyümesi.

Tespit Edilen Sorunlar:  
- SRP zayıf: shader compile/link, uniform lookup, uniform set, texture lifecycle, fallback texture üretimi, quality policy ve legacy adapter kullanımı aynı sınıfta.
- `draw()` metodu çok büyük; hesaplama, uniform hazırlama, texture resolve/bind ve draw çağrısı tek blokta olduğu için hata ayıklama zorlaşıyor.
- Texture unit kullanımı riskli görünüyor. Aynı unit değerleri farklı sampler’lar için tekrar kullanılıyor; son bind önceki sampler’ın gördüğü texture’ı değiştirebilir.
- `release()` current GL context yoksa GL kaynaklarını silmeden handle’ları sıfırlıyor. Bu, aynı context hâlâ yaşıyorsa GPU resource leak riskini gizleyebilir.
- `ensureFallbackTextures()` ilk `draw()` sırasında bitmap oluşturup GL texture üretiyor. İlk frame’de küçük de olsa allocation ve GL iş yükü oluşturuyor.
- `setRenderQuality()` sadece `qualityScale` değerini değiştiriyor; kalite texture seçimini etkiliyorsa mevcut yüklenmiş texture’lar yeniden yüklenmiyor.
- `positionHandle` negatif olsa bile `glEnableVertexAttribArray(positionHandle)` çağrılabilir. Attribute bulunamazsa GL error üretme riski var.
- Kritik GL çağrılarında `glGetError()` ile sınırlandırılmış diagnostic yok; shader/texture/draw kaynaklı sorunlar üretim cihazlarında zor ayrışır.

🛠️ Geliştirme Planı:  
1. Texture unit atamalarını tek bir sabit harita ile netleştir; her sampler için çakışmayan unit kullan ve bind sırasını bu haritaya göre düzenle.
2. `draw()` metodunu davranış değiştirmeden parçalara ayır: `computeShaderInputs`, `applyUniforms`, `bindTextures`, `drawFullscreenQuad`.
3. `positionHandle < 0` ise draw’u güvenli şekilde atla ve kısa log bas.
4. Fallback texture üretimini mümkünse `init()` sonuna taşı; ilk frame allocation riskini azalt.
5. `release()` current context yokken handle sıfırlama davranışını açık politika haline getir; kaynak silinemiyorsa bunu logla.
6. `setRenderQuality()` texture seçimini etkiliyorsa configured texture’ları yeniden yükleyecek veya bu fonksiyonu yalnızca reconfigure öncesi kullanılacak şekilde sınırlandır.
7. Shader compile/link sonrası ve texture yükleme/binding çevresinde düşük frekanslı GL error log helper’ı ekle.
8. Orta vadede uniform handle’ları küçük bir data class altında grupla; alan sayısını azaltıp okunabilirliği artır.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `PreviewGlRenderer.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
