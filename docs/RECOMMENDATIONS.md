# Lumisky V2 - İyileştirme ve Özellik Önerileri

Lumisky V2 projesinin güncel mimarisi ve `OPTIMIZATIONS.md` denetim raporları doğrultusunda, uygulamayı "Premium" bir seviyeye taşıyacak spesifik iyileştirme tavsiyeleri aşağıda listelenmiştir.

### 🚀 1. Performans ve Akıcılık İyileştirmeleri (Performance)
Projede şu an motor akıcı bir şekilde çalışsa da, uygulama arayüzü ve başlangıç işlemleri donanımı yorabiliyor.

* **Ağ ve Başlangıç Boğulmasını Engelleme (Startup Jank):** `HomeViewModel` başlar başlamaz arka planda yedek şehirler (25 civarı şehir) için Güneş konumlarını (API istekleri) çekmeye çalışıyor. Bu durum uygulamanın ilk açılış saniyelerindeki akıcılığı mahveder. Veri çekme işlemleri uygulama tamamen açılıp "idle" (bekleme) konumuna geçtikten sonraya veya Android `WorkManager` üzerinden tamamen arka plana ertelenmelidir.
* **Çöp Toplayıcı (Garbage Collector) Anlık Takılmaları:** Motor her saniyede 60 kare (FPS) yenilenirken `SkyRenderer` ve `SceneStateHasher` içinde `listOf(...).hashCode()` kullanılıyor. Sürekli olarak liste ve nesne üretilmesi saniye başı çöp toplayıcının çalışmasına sebep olur, bu da duvar kağıdında mikro-takılmalar (**micro-stuttering**) yapar. *Tavsiye:* Bu alanlar nesne üretmeyen, manuel bir sayısal hesaplama mantığı ile değiştirilmelidir.
* **Gereksiz Dosya Okumaları (Double I/O):** `RenderAssetCache` dosyası, WebP kaplamaları yüklerken dosyanın "var olup olmadığını" ve "okunmasını" aynı anda yürütüyor. Özellikle tema geçişlerinde tek bir görsel kaplama arka arkaya iki kez okunabiliyor; buralarda bellekte ufak bir önizleme önbelleği (`LruCache`) kullanılmalı.

### 🎨 2. Görsellik ve Estetik (Visuals)
Bir canlı duvar kağıdının en vurucu noktası kullanıcısına yaşattığı "Premium" görsel histir.

* **Jiroskop / Paralaks (Depth) Etkisi:** `core/location` modülünün yanına bir `core/sensors` (Jiroskop sensör okuyucusu) yapısı eklenebilir. Kullanıcı telefonu sağa, sola eğdiğinde arka plan sabit kalırken, bulutlar ve ön plan asimetrik olarak hareket etmeli. Bu, sahneye inanılmaz bir **3D Derinlik (Parallax)** katar.
* **Canlı Hava Durumu (Live Weather Integration):** Uygulama zaten güneş saatlerini biliyor. Buna harici bir "Weather API" bağlayarak o an gerçek hayatta yağmur veya kar yağıyorsa, bunu `engine` içindeki shader'lara parametre olarak gönderip gökyüzü dokusu üzerinde yağmur partikülü oluşturabilirsiniz.
* **Haptikler ve Dokunma Etkileşimi (Touch & Ripples):** `SkyWallpaperService` üzerinden Android'in kullanıcı ekrana dokunduğu piksel noktalarını algılayabiliyoruz. Ekrana dokunulduğunda veya kaydırıldığında o bölgede gökyüzü bulutlarının dağılması, küçük kuyruklu yıldız efektleri veya su dalgalanmaları (ripple) gibi donanım hızlandırmalı interaktif geri dönüşler eklenebilir. 
* **Temalar Arası Pürüzsüz Geçiş (Cross-fade Transition):** Farklı bir temaya tıklandığında siyah ekran ya da anında geçiş yerine, OpenGL shader kullanılarak mevcut temanın içerisinden bir pikselleşme, bulanıklaşıp netleşme veya yavaşça harmanlanarak (Alpha blending) yeni temaya geçmesi üst düzey hissettirecektir.

### ⚙️ 3. İşlevsellik ve Kullanıcı Deneyimi (Functionality)
Duvar kağıdının güzel görünmesi kadar işletim sistemiyle dostu olması da önemlidir.

* **Batarya Tasarruf Optimizasyonu (Power Save Mode Bindings):** Canlı duvar kağıtları fazla pil tüketebilir. Android işletim sistemi "Güç Tasarruf Moduna" girdiğinde `core` modülü bunu dinlemeli ve otomatik olarak duvar kağıdındaki hedef kare hızını (FPS) 60'tan 30'a çekmeli veya yoğun bulanıklıklaştırma - partikül oluşturma efektlerini kapamalıdır.
* **Ana Ekran Hızlı Eylemleri (Double Tap Gestures):** Cihazın kendi ana ekranındayken ekrana çift veya üçlü dokunma (Double-Tap) hareketi bağlayıp; "Gündüz - Gece döngüsünü tetikleme" ya da "Hızlıca Rastgele Tema Seçme" gibi kısayol eylemleri oluşturulabilir.
* **Dinamik Render Yöneticisi:** `wallpaper` modülündeki sürekli çalışma mantığı şu an düz metin (string) eşleşmelerine dayanıyor. Ancak her duvar kağıdı için bir JSON özelliği ekleyip o duvar kağıdının "Statik", "Saniye Başı Bir Kare" ya da "Akıcı 60fps" davranması koda gömülmek yerine veriye dayalı (data-driven) hale getirilmelidir.
