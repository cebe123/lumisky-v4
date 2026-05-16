# LastKnownLocationProvider Review

📄 Dosya: `core/src/main/java/com/example/core/location/LastKnownLocationProvider.kt`

Genel Değerlendirme:  
Fused Location Provider üzerinden izin seviyesi, last-known/current location, passive updates ve reverse-geocode şehir etiketi çözümünü sağlayan Android konum adaptörü. Platform API sınırını tek sınıfta toplaması doğru; ancak callback thread contract’ı, cancellation ve geocoder bloklama davranışı daha açık yönetilmeli.

Tespit Edilen Sorunlar:  
- `requestLastKnownLocation()` ve `requestCurrentLocation()` callback thread’i açık değil. Listener’lar main thread veya Play Services internal thread üzerinde dönebilir; caller tarafında state güncelleme riski oluşur.
- `requestCurrentLocation()` içinde `CancellationTokenSource` oluşturuluyor ama dışarı cancel handle dönmüyor. Caller lifecycle bittiğinde aktif current-location isteğini iptal edemez.
- `requestCurrentLocation()` priority seçimi hassas: precise izin olsa bile default `PRIORITY_BALANCED_POWER_ACCURACY`; gerçek "current" beklentisi varsa doğruluk düşük kalabilir.
- `startPassiveLocationUpdates()` başarı listener’ı çalışana kadar `passiveLocationCallback` null kalıyor. Bu arada ikinci çağrı ikinci listener başlatabilir.
- Passive update callback’i `Looper.getMainLooper()` ile geliyor; `onLocation` ağır iş yaparsa main thread etkilenebilir. Contract görünür değil.
- `resolveCityOrDistrict()` senkron çalışıyor ve Android 13+ için latch ile 2 saniyeye kadar bloklayabiliyor. Yanlış thread’den çağrılırsa UI donması riski var.
- Geocoder cache key sadece koordinatı içeriyor, locale içermiyor. Dil değişince eski dilde çözülmüş locality label dönebilir.
- `Location.toSnapshot()` timezone olarak her zaman `ZoneId.systemDefault().id` kullanıyor. Konum cihaz timezone’undan farklıysa API adayının timezone’u başlangıçta yanlış olabilir.

🛠️ Geliştirme Planı:  
1. Tüm callback’ler için thread contract’ı belirle: main thread mi, caller executor mı, background mı. KDoc ile açık yaz.
2. `requestCurrentLocation()` için küçük bir cancelable handle döndür veya coordinator release sırasında iptal edilecek şekilde token sahipliğini yukarı taşı.
3. Priority seçimini access level ve çağrı niyetine göre netleştir; precise/current isteniyorsa `PRIORITY_HIGH_ACCURACY` opsiyonunu kontrollü destekle.
4. Passive listener başlangıcında callback’i lock içinde önce set et; request başarısız olursa temizle. Böylece yarışta çift listener oluşmaz.
5. `resolveCityOrDistrict()` sadece background thread’den çağrılmalıysa bunu KDoc ile belirt; mümkünse suspend/async API’ye ayır.
6. Geocoder cache key’e locale/language tag ekle veya dil değişiminde cache clear API’si sun.
7. Snapshot timezone’unun cihaz timezone’u olduğunu açıkça belirt; gerçek lokasyon timezone’u daha sonra sun-times API ile resolve edilecekse bu contract görünür olsun.
8. Test hedefleri: izin yokken no-op, passive double-start yarışı, geocoder timeout, locale değişiminde cache davranışı, current location cancel/lifecycle.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `HomeScreen.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
