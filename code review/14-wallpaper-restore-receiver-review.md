# WallpaperRestoreReceiver Review

📄 Dosya: `wallpaper/src/main/java/com/example/wallpaper/service/WallpaperRestoreReceiver.kt`

Genel Değerlendirme:  
Boot, user unlock ve app replace sonrası seçili wallpaper config’ini restore etmeyi tetikleyen `BroadcastReceiver`. Dosya küçük ve sorumluluğu belirgin; restore isteğini doğrudan iç broadcast ile uygulama içine yönlendiriyor. Kritik alanlar duplicate action akışı, Direct Boot/storage sınırları ve restore sonucunun doğrulanmaması.

Tespit Edilen Sorunlar:  
- `ACTION_BOOT_COMPLETED`, `ACTION_USER_UNLOCKED` ve bazı cihazlarda app replace akışları birbirine yakın tetiklenebilir; duplicate restore broadcast’leri için debounce/idempotency guard yok.
- `storedConfig` sadece varlık kontrolü ve log için okunuyor; restore broadcast’i config’i taşımaz. Alıcı config’i tekrar okuyorsa arada state değişimi/race oluşabilir.
- `onReceive()` içinde SharedPreferences okuma ve lock-screen paylaşım temizliği doğrudan yapılıyor. Şu an hafif görünse de BroadcastReceiver süresi ve platform kısıtları açısından yan etkiler sınırlı tutulmalı.
- `restoreLockScreenSharingIfNeeded()` sadece `ACTION_LOCKED_BOOT_COMPLETED` için erken çıkıyor. `ACTION_BOOT_COMPLETED` bazı cihazlarda kullanıcı unlock olmadan gelirse credential-protected ayarlara erişim davranışı belirsizleşebilir.
- Restore isteği `setPackage(appContext.packageName)` ile sınırlanmış ama explicit component kullanılmıyor. Paket içinde birden fazla matching receiver varsa hedef davranış manifest’e bağlı kalır.
- Restore broadcast gönderildikten sonra restore’un gerçekten uygulanıp uygulanmadığı doğrulanmıyor; failure sadece broadcast gönderme hatasıyla sınırlı.
- `AppSettingsRepository` ve `WallpaperConfigStore` doğrudan receiver içinde oluşturuluyor; test edilebilirlik ve lifecycle davranışını izole etmek zor.

🛠️ Geliştirme Planı:  
1. Restore action’ları için kısa süreli debounce veya son restore action/time kaydı ekle; aynı boot/unlock zincirinde tekrarlı apply tetiklenmesin.
2. Restore broadcast hedefini mümkünse explicit component ile belirt; internal action’ın tek alıcıya gittiği garanti olsun.
3. `storedConfig.id` ile birlikte restore request id veya timestamp logla; alıcı tarafında da aynı id ile takip edilebilir olsun.
4. Direct Boot sınırını netleştir: credential-protected ayar okuma gerektiren lock-screen restore işlemini yalnızca user unlocked durumda çalıştır.
5. Yan etkili lock-screen sharing temizliğini küçük bir helper/service sınıfına çıkar; receiver sadece action routing yapsın.
6. Restore sonucunu izlemek için alıcı tarafta başarı/başarısızlık log contract’ı oluştur; receiver yalnızca request gönderdiğini loglamalı.
7. Test hedefleri: stored config yokken no-op, boot+unlock duplicate tetikleme, locked boot sırasında ayar erişimi, MY_PACKAGE_REPLACED default lock-screen restore davranışı.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `SunTimesRepository.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
