# HomeViewModel Review

📄 Dosya: `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt`

Genel Değerlendirme:  
Ana ekranın wallpaper listesi, seçim durumu, tema/dil/performans ayarları, konum-güneş verisi koordinasyonu ve backup prefetch tetiklerini yöneten merkezi state sınıfı. MVVM açısından ekran state’i tek noktada toplanmış; ancak sınıf gerçek `androidx.lifecycle.ViewModel` değil, çok fazla sorumluluk taşıyor ve bazı state senkronizasyon yollarında hata riski var.

Tespit Edilen Sorunlar:  
- `HomeViewModel` adı ViewModel olsa da `androidx.lifecycle.ViewModel` extend etmiyor. Lifecycle cleanup manuel `release()` çağrısına bağlı; çağrı kaçarsa listener/executor kaynakları sızabilir.
- `applySettingsSnapshot()` içinde `languageTag` önce güncelleniyor, sonra `newLanguageTag = snapshot.languageTag.takeIf { it != languageTag }` hesaplanıyor. Bu nedenle coordinator’a dil değişimi çoğu durumda iletilmeyebilir.
- Sınıf SRP açısından geniş: catalog, settings, location coordinator, wallpaper selection, live preview ve prefetch orchestration aynı sınıfta.
- `storedWallpaperId` sadece init anında okunuyor. `applySelectedWallpaper()` sonrası kalıcı seçim değişse bile `clearSelection()` eski başlangıç id’sine dönebilir.
- `rebuildCatalog()` için executor işleri release sonrası main thread’e post edebilir; `released` guard yok.
- `items` getter her erişimde `_items.toList()` oluşturuyor. Compose tarafında sık okunuyorsa gereksiz allocation yaratabilir.
- `onCategoryFocused()` parametresinden kategori id’leri hesaplanıyor ama sonuç sadece duplicate prefetch engellemek için kullanılıyor; isimlendirme gerçek davranışı tam anlatmıyor.
- Repository ve worker çağrıları doğrudan ViewModel içinde. Test edilebilirlik için dependency boundary var ama Android lifecycle/coroutine modeliyle bütünleşme zayıf.

🛠️ Geliştirme Planı:  
1. Sınıfı gerçek `ViewModel` yapmak mümkünse `androidx.lifecycle.ViewModel` extend et ve cleanup’ı `onCleared()` içine taşı; değilse `release()` çağrısının zorunlu contract’ını açıkça belgeleyin.
2. `applySettingsSnapshot()` içinde coordinator değişim parametrelerini local state mutasyonlarından önce hesapla; özellikle `newLanguageTag` bug’ını düzelt.
3. `storedWallpaperId` yerine güncel persist edilmiş seçim için mutable `persistedWallpaperId` tut veya `applySelectedWallpaper()` içinde stored referansını güncelle.
4. Release sonrası state post etmeyi engellemek için `released` flag ekle; executor ve `mainHandler.post` callback’lerinde kontrol et.
5. Sınıfı küçük sorumluluklara ayır: selection state, settings state apply, catalog rebuild ve prefetch tetikleri ayrı private helper/state holder olabilir.
6. `items` allocation maliyetini azaltmak için UI’ye doğrudan snapshot-aware read-only liste veya cached immutable liste sunmayı değerlendir.
7. `onCategoryFocused()` davranışını isimle hizala; sadece prefetch debounce yapıyorsa method adı bunu yansıtmalı.
8. Test hedefleri: dil değişimi coordinator’a iletiliyor mu, apply sonrası clear selection doğru id’ye dönüyor mu, release sonrası catalog post state değiştirmiyor mu.

Bu dosya için inceleme tamamlandı. Listeden sonraki dosya olan `WallpaperManifestCatalogSource.kt` dosyasına geçiş yapmamı onaylıyor musunuz?
