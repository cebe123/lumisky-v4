# City Fragment Shader Review

📄 Dosya: `app/src/main/assets/shaders/city/fragment_shader.glsl`

Genel Değerlendirme:  
Şehir silüeti, gökyüzü/ocean renk geçişi, güneş/ay diski, bulut, yıldız ve yansıma efektlerini tek fragment shader içinde üreten yoğun bir görsel shader. Görsel kapsamı zengin ve uniform tabanlı şehir ayarları kullanması doğru; ancak shader çok fazla sorumluluğu tek dosyada taşıyor, bazı app uniformlarını kullanmıyor ve mobil GPU maliyeti açısından riskli bölgeler içeriyor.

Tespit Edilen Sorunlar:  
- Shader sabit `HORIZON_Y` kullanıyor; render pipeline `u_HorizonY` uniformunu geçmesine rağmen bu shader onu kullanmıyor. Manifest/config horizon değişiklikleri bu shader’da etkisiz kalır.
- `u_TouchPosition` ve `u_TouchTime` uniformları tanımlı ama kullanılmıyor. Uniform contract gereği kalsa bile shader içindeki niyet belirsiz.
- `isDay` hesabı `u_Sunrise <= u_Sunset` varsayımına dayanıyor. Gece yarısını aşan veya uç daylight durumlarında geçiş yanlış çalışabilir.
- Tek shader içinde sky, ocean, celestial body, stars, clouds, city mask ve reflection mantığı çok uzun bir `main()` bloğunda birleşmiş. Okunabilirlik ve hata izolasyonu zayıf.
- Gökyüzü piksellerinde `fbm()` ve `getStars()` aynı frame içinde çalışıyor. Özellikle yüksek çözünürlükte 4 octave noise + yıldız hash maliyeti mobil GPU’da pahalı olabilir.
- `discard` son aşamada neredeyse siyah pikseller için kullanılıyor. Tile-based mobil GPU’larda discard erken-z avantajı sağlamadan performansı kötüleştirebilir.
- `u_CityZoom`, offset ve scale değerleri shader içinde clamp edilmiyor. Manifestten hatalı değer gelirse texture sampling ve şehir yerleşimi bozulabilir.
- Çok sayıda yorum geçmiş değişiklik notu gibi duruyor. Üretim shader’ında niyet açıklayan kısa yorumlar kalmalı; eski deneme notları okunabilirliği düşürüyor.

🛠️ Geliştirme Planı:  
1. `HORIZON_Y` sabitini `u_HorizonY` ile değiştir; geriye dönük güvenlik için shader içinde `clamp(u_HorizonY, 0.05, 0.95)` kullan.
2. Kullanılmayan `u_TouchPosition` / `u_TouchTime` uniformları contract gereği kalacaksa kısa yorumla belirt; değilse pipeline ile birlikte kaldır.
3. Day/night geçiş hesabını gece yarısı aşan sunrise/sunset değerlerini destekleyen küçük helper fonksiyona ayır.
4. `main()` içindeki büyük blokları helper fonksiyonlara böl: sky, celestial, city mask, ocean reflection, stars/clouds.
5. Star/cloud maliyetini kalite uniformlarıyla veya `u_HasStars`/`u_CloudAlpha` erken koşullarıyla azalt; gereksiz hash/noise hesaplarını sınırlayın.
6. Final `discard` kullanımını ölçmeden koruma; opak siyah çıktı yeterliyse discard’ı kaldırmayı değerlendir.
7. `u_CityZoom` ve offset değerlerini shader içinde güvenli aralığa clamp et.
8. Yorumları sadeleştir: geçmiş deneme notlarını kaldır, sadece görsel niyeti ve uniform contract’ını anlatan kısa yorumlar bırak.
9. Test hedefleri: farklı horizon değerleri, sunrise/sunset gece yarısı senaryosu, düşük kalite/kapalı yıldız, şehir zoom uç değerleri ve düşük seviye cihaz shader FPS’i.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `app/src/main/assets/shaders/lighthouse/fragment.glsl` dosyasına geçiş yapmamı onaylıyor musunuz?
