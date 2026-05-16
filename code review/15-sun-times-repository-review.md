# SunTimesRepository Review

📄 Dosya: `core/src/main/java/com/example/core/api/SunTimesRepository.kt`

Genel Değerlendirme:  
Güneş doğuş/batış verisini aday konum önceliğine göre API’den alan, aktif/backup cache katmanlarını yöneten ve fallback daylight üreten repository sınıfı. İş mantığı güçlü ve cihaz çevrimdışı/konum fallback senaryolarını düşünmüş; ancak concurrency, callback thread contract’ı ve executor lifecycle tarafında ciddi sadeleştirme ihtiyacı var.

Tespit Edilen Sorunlar:  
- `onUpdated` callback’i bazen çağıran thread’de, bazen `refreshExecutor` thread’inde çalışıyor. UI/ViewModel tarafında state güncelleniyorsa thread davranışı belirsiz ve riskli.
- Network isteklerinde timeout yok. `future.get()` ilk öncelikli candidate için takılırsa daha düşük öncelikli candidate sonucu hazır olsa bile kullanılmaz.
- Her repository instance’ı üç ayrı executor oluşturuyor. `release()` çağrısı kaçarsa thread sızıntısı oluşabilir.
- `prefetchBackupAsync()` executor shutdown kontrolü yapmıyor; release sonrası çağrılırsa `RejectedExecutionException` riski var.
- `prefetchBackupInternal()` bazı yerlerde `System.currentTimeMillis()` kullanıyor, repository’nin `nowProvider` bağımlılığını atlıyor. Test edilebilirlik ve zaman tutarlılığı bozuluyor.
- `buildCandidates()` timezone’u distinct key’e dahil ederken backup prefetch sadece latitude/longitude ile unique yapıyor. Aynı koordinat farklı timezone ile gelirse backup cache davranışı tutarsızlaşabilir.
- Hata yakalama çok genel `catch (e: Exception) { null }` şeklinde; cancellation, timeout ve gerçek network hatası ayrışmıyor.
- Sınıf çok geniş: candidate normalization, cache policy, async orchestration, network fetch, fallback logging ve timezone helpers aynı yerde.

🛠️ Geliştirme Planı:  
1. `onUpdated` callback thread contract’ını tekleştir: ya her zaman caller-provided executor/main dispatcher üzerinden dön ya da her zaman background callback olduğunu belgeleyin.
2. Network fetch için timeout politikası ekle; candidate önceliğini korurken tek takılan future’ın tüm refresh’i bloke etmesini engelle.
3. Executor lifecycle’ı için `released` guard ekle; `refreshAsync...` ve `prefetchBackupAsync()` release sonrası güvenli no-op dönsün.
4. `prefetchBackupInternal()` içinde tüm zaman okumalarını `nowProvider()` üzerinden yap.
5. Candidate normalization için tek helper kullan; active ve backup prefetch aynı distinct key politikasını paylaşsın.
6. Fetch sonucunu küçük result tipine ayır: success, cancelled, timeout, failed. Log ve fallback kararları buna göre daha okunabilir olur.
7. Cache policy’yi küçük bir internal helper’a çıkar; repository async orchestration ve public API’ye odaklansın.
8. Test hedefleri: cache hit callback thread’i, release sonrası prefetch, ilk candidate timeout/ikinci candidate success, timezone farklı candidate key’i, stale request cancellation.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `LocationSunTimesCoordinator.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
