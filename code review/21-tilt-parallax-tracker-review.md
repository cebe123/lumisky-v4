# TiltParallaxTracker Review

📄 Dosya: `core/src/main/java/com/example/core/motion/TiltParallaxTracker.kt`

Genel Değerlendirme:  
Gravity/accelerometer sensörlerinden cihaz eğimini okuyup parallax offset üreten küçük Android motion adapter sınıfı. Dosya kompakt, allocation üretmeyen ve render tarafına sade `x/y` callback’i veren pratik bir yapı. Ana riskler sensor callback thread’i, lifecycle idempotency ve sensör hızı/batarya politikasıyla ilgili.

Tespit Edilen Sorunlar:  
- `registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)` handler verilmeden çağrılıyor; callback thread’i çağıran thread’in looper’ına bağlı olabilir. `onParallaxChanged` hangi thread’de çalışacak net değil.
- `stop()` her zaman `dispatch(0f, 0f, force = true)` yapıyor. View detach/close sırasında renderer release edilmişse geç callback zinciri oluşabilir.
- `start()` sensör yoksa her çağrıda `dispatch(0,0)` yapıyor. Sensörsüz cihazda repeated start/stop gereksiz render tetikleyebilir.
- `SensorEvent.values[0..2]` doğrudan okunuyor. Pratikte gravity/accelerometer 3 değer döner ama defensive length kontrolü yok.
- `active` ve filtre state alanları thread-safe değil. Sınıf tek UI thread’den start/stop ve sensor thread’den callback alıyorsa data race ihtimali var.
- Sensör hızı sabit `SENSOR_DELAY_GAME`. Preview kartları için daha düşük hız veya runtime ayarı batarya açısından daha uygun olabilir.
- Orientation/rotation farkları hesaba katılmıyor. Cihaz yatay/dikey kullanımda parallax yönü bazı cihazlarda sezgisel olmayabilir.

🛠️ Geliştirme Planı:  
1. Sensor callback thread contract’ını netleştir; gerekiyorsa `Handler(Looper.getMainLooper())` veya dedicated handler ile register et.
2. `onParallaxChanged` callback’inin lifecycle güvenliğini caller tarafıyla sözleşmeye bağla; close sonrası dispatch yapılmaması gerekiyorsa `closed` flag ekle.
3. Sensör yoksa sadece ilk start’ta reset dispatch et veya son gönderilen değer zaten 0 ise tekrar dispatch etme.
4. `onSensorChanged()` başında `event.values.size >= 3` guard’ı ekle.
5. Start/stop ve sensor callback farklı threadlerde çalışacaksa `active` için volatile/atomic ya da tek-thread kullanım contract’ı ekle.
6. Sensör hızını constructor parametresi veya mode üzerinden ayarlanabilir yap; preview için `SENSOR_DELAY_UI` yeterli olabilir.
7. Orientation-aware parallax gerekiyorsa display rotation bilgisini ayrı bir mapper helper’da ele al.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `app/src/main/assets/wallpapers/index.json` dosyasına geçiş yapmamı onaylıyor musunuz?
