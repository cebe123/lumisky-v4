# Lumisky: Performans İyileştirme Planı (İlk 3 Madde)

Bu doküman, projede tespit edilen en kritik 3 performans sorununu (**Startup Jank, GC Stuttering, Double I/O**) ortadan kaldırmak için adım adım uygulanacak teknik planı içerir. 

---

## 🎯 Hedef 1: Ağ ve Başlangıç Boğulmasını Engelleme (Startup Jank)
**Açıklama:** Uygulama açılışında (`HomeViewModel` başlatıldığında) arka planda ~25 farklı şehir için güneş durumunu (API'den) çeken `prefetch` işlemi tetikleniyor. Bu işlem ağ trafiğini artırıp UI thread üzerinde yük yaratarak başlangıç animasyonlarının takılmasına yol açar.

### 🛠 Eylem Planı:
1. **İlgili Dosyalar:** 
   * `app/src/main/java/com/example/lumisky/viewmodel/HomeViewModel.kt`
2. **Adım 1:** `HomeViewModel` içindeki `init` bloğunu ve başlangıçtaki sabit zamanlı tetikleyiciyi (`startupBackupPrefetchRunnable` ve ~`2_000ms` gecikmesini) tespit edin.
3. **Adım 2:** Prefetch mantığını doğrudan ViewModel'in başlamasından ayırın.
4. **Adım 3 (Uygulama):** SunTimesRepository çağrısını ağ engeli yaşatmamak adına Android'in **WorkManager** sistemine (OneTimeWorkRequest ile) devredin veya uygulama tamamen *Idle* (Kullanıcı arayüzünde scroll yapmayı bıraktığı bir an vs.) duruma geçene kadar erteleyin.
   * *Not:* En kolayı, `prefetchBackupCityCache` fonksiyonunun çağrılmasını bir `Delay(15_000)` gibi bir beklemeden ziyade mimari olarak daha stabil olan `WorkManager` enqueue işlemine çevirmektir.

---

## 🎯 Hedef 2: Çöp Toplayıcı (Garbage Collector) Anlık Takılmaları
**Açıklama:** Ekrandaki bulutlar haraket ederken motor (engine) pikselleri saniyede 60 kere çizer. Mevcut durumda sahnenin değişip değişmediğini kontrol etmek için her karede (frame) geçici listeler (`listOf(...).hashCode()`) oluşturuluyor. Her saniye 60 kez liste üretilip çöpe atılması Garbage Collector (GC) kramplarına (micro-stutter) neden olur.

### 🛠 Eylem Planı:
1. **İlgili Dosyalar:**
   * `engine/src/main/java/com/example/engine/renderer/SkyRenderer.kt`
   * `wallpaper/src/main/java/com/example/wallpaper/render/SceneStateHasher.kt` (ve bağlı controller vb.)
2. **Adım 1:** `stateHash`, `sceneFingerprint()` gibi fonksiyonların içerisindeki `listOf(...).hashCode()` ve varsa `buildString {...}` yapılarını bulun.
3. **Adım 2 (Uygulama):** List objesi üreten kod bloklarını, obje üretmeyen (Zero-allocation) **Rolling Hash** (Manuel Hash) yöntemine dönüştürün:
   ```kotlin
   // ESKİ KOD (Her frame için liste aloke eder)
   val hash = listOf(mode.ordinal, progress, sunX, skyColor).hashCode()

   // YENİ KOD (Zero allocation / GC Dostu)
   var result = 17
   result = 31 * result + mode.ordinal
   result = 31 * result + progress.toBits().toInt() // float ise
   result = 31 * result + sunX.toBits().toInt()
   result = 31 * result + skyColor
   return result
   ```
4. **Adım 3:** Yapılan bu değişimin duvar kağıdı güncellenmesini bozmadığından (farklı verilerde doğru hash değişimi) emin olmak için bir Unit test eklenmesi.

---

## 🎯 Hedef 3: Gereksiz Dosya Okumaları (Double I/O - WebP Sorunu)
**Açıklama:** Klasörlerdeki pikseller (örneğin `.png` varlıkları) derleme anında `.webp`'e çevrilir. Uygulama çalışma zamanında kaplamaları yüklerken "Önce `.webp` var mı diye dosyayı oku, varsa `.webp` bytes'larını yükle, yoksa asıl yolu yükle" şeklinde bir yol izler. Bu işlem diskten aynı dosyanın iki kere sondajlanması gibi kümülatif I/O israfı oluşturur.

### 🛠 Eylem Planı:
1. **İlgili Dosyalar:**
   * `engine/src/main/java/com/example/engine/shader/PreviewSkyProgram.kt` (veya ilgili texture loader'lar)
   * `app/src/main/java/com/example/lumisky/shader/RenderAssetCache.kt`
2. **Adım 1:** WebP probe (deneme) mantığını bulun: (`resolvePreferredTexturePath` veya `assetExists`).
3. **Adım 2 (Uygulama):** Dosya varlığı kontrolünü ve byte okuma fonksiyonunu birleştirin. Uygulama sondaj yapmak yerine direkt `readBytes()` yapsın; başarısız olursa diğer formata geçsin (Try-Catch stream binding).
4. **Adım 3:** Sondaj başarılı olduğunda okunan baytları çöp etmeyin, geri döndürün (`Pair<ResolvedPath, ByteArray>`). 
5. **Adım 4 (Önbellek):** Ufak bir `LruCache<String, String>` (örneğin: `"background/mars.png" -> "background/mars.webp"`) belleği tasarlayın. Böylece uygulama bir temanın `.webp` formatında saklandığını öğrendiğinde bir daha asla "sondaj" yapmakla uğraşmaz, disk okumaları %50 azalır.

---

Bu plana dair hazırlıklara katılıyorsanız, **kodlamaya Hedef 2 (GC Stuttering)'den** (çünkü en hızlı ve net gözlemlenebilir performansı verir) başlayalım mı? Hangi maddeden başlamak istediğinizi belirtmeniz yeterli!
