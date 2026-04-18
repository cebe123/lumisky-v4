# Lumisky V2 - İyileştirme ve Özellik Önerileri

Lumisky projesinin güncel mimarisi ve `OPTIMIZATIONS.md` denetim raporları doğrultusunda, uygulamayı "Premium" bir seviyeye taşıyacak spesifik iyileştirme tavsiyeleri aşağıda listelenmiştir.

### 🚀 1. Performans ve Akıcılık İyileştirmeleri (Performance)
Projede şu an motor akıcı bir şekilde çalışsa da, uygulama arayüzü ve başlangıç işlemleri donanımı yorabiliyor.

* **Ağ ve Başlangıç Boğulmasını Engelleme (Startup Jank):** `HomeViewModel` başlar başlamaz arka planda yedek şehirler (25 civarı şehir) için Güneş konumlarını (API istekleri) çekmeye çalışıyor. Bu durum uygulamanın ilk açılış saniyelerindeki akıcılığı mahveder. Veri çekme işlemleri uygulama tamamen açılıp "idle" (bekleme) konumuna geçtikten sonraya veya Android `WorkManager` üzerinden tamamen arka plana ertelenmelidir.
* **Çöp Toplayıcı (Garbage Collector) Anlık Takılmaları:** Motor her saniyede 60 kare (FPS) yenilenirken `SkyRenderer` ve `SceneStateHasher` içinde `listOf(...).hashCode()` kullanılıyor. Sürekli olarak liste ve nesne üretilmesi saniye başı çöp toplayıcının çalışmasına sebep olur, bu da duvar kağıdında mikro-takılmalar (**micro-stuttering**) yapar. *Tavsiye:* Bu alanlar nesne üretmeyen, manuel bir sayısal hesaplama mantığı ile değiştirilmelidir.
* **Gereksiz Dosya Okumaları (Double I/O):** `RenderAssetCache` dosyası, WebP kaplamaları yüklerken dosyanın "var olup olmadığını" ve "okunmasını" aynı anda yürütüyor. Özellikle tema geçişlerinde tek bir görsel kaplama arka arkaya iki kez okunabiliyor; buralarda bellekte ufak bir önizleme önbelleği (`LruCache`) kullanılmalı.

### 🎨 2. Görsellik ve Estetik (Visuals)
Bir canlı duvar kağıdının en vurucu noktası kullanıcısına yaşattığı "Premium" görsel histir.

* **Jiroskop / Paralaks (Depth) Etkisi:** `core/location` modülünün yanına bir `core/sensors` (Jiroskop sensör okuyucusu) yapısı eklenebilir. Kullanıcı telefonu sağa, sola eğdiğinde arka plan sabit kalırken, bulutlar ve ön plan asimetrik olarak hareket etmeli. Bu, sahneye inanılmaz bir **3D Derinlik (Parallax)** katar.
* **Canlı Hava Durumu (Live Weather Integration):** Uygulama zaten güneş saatlerini biliyor. Buna harici bir "Weather API" bağlayarak o an gerçek hayatta yağmur veya kar yağıyorsa, bunu `engine` içindeki shader'lara parametre olarak gönderip gökyüzü dokusu üzerinde yağmur partikülü oluşturabilirsiniz.

### ⚙️ 3. İşlevsellik ve Kullanıcı Deneyimi (Functionality)
Duvar kağıdının güzel görünmesi kadar işletim sistemiyle dostu olması da önemlidir.

* **Batarya Tasarruf Optimizasyonu (Power Save Mode Bindings):** Canlı duvar kağıtları fazla pil tüketebilir. Android işletim sistemi "Güç Tasarruf Moduna" girdiğinde `core` modülü bunu dinlemeli ve otomatik olarak duvar kağıdındaki hedef kare hızını (FPS) 60'tan 30'a çekmeli veya yoğun bulanıklıklaştırma - partikül oluşturma efektlerini kapamalıdır.
* **Ana Ekran Hızlı Eylemleri (Double Tap Gestures):** Cihazın kendi ana ekranındayken ekrana çift veya üçlü dokunma (Double-Tap) hareketi bağlayıp; "Gündüz - Gece döngüsünü tetikleme"  eylemi oluşturulabilir.
* **Dinamik Render Yöneticisi:** `wallpaper` modülündeki sürekli çalışma mantığı şu an düz metin (string) eşleşmelerine dayanıyor. Ancak her duvar kağıdı için bir JSON özelliği ekleyip o duvar kağıdının "Statik", "Saniye Başı Bir Kare" ya da "Akıcı 60fps" davranması koda gömülmek yerine veriye dayalı (data-driven) hale getirilmelidir.

### 🛒 4. Google Play Store'da Öne Çıkma Stratejileri (ASO & Görünürlük)
Uygulamanızın kalabalığın arasından sıyrılıp, Play Store algoritmalarında ön plana çıkması için sadece kodun değil iletişim ve sunumun da kusursuz olması gerekir:

* **"Battery-Friendly" (Pil Dostu) Vurgusu:** Kullanıcıların canlı duvar kağıdı indirmekten çekinmesinin en büyük sebebi "şarjı hızlı bitirir mi" korkusudur. Açıklama kısmında, uygulama fotoğraflarında ve videoda uygulamanın ne kadar optimize edildiğini, gereksiz arka plan işlemleri yapmadığını (örneğin ekran kapalıyken %0 CPU kullanımı gibi) vurgulamalısınız.
* **Büyüleyici Bir Promosyon Videosu:** Durağan fotoğraflar bir canlı duvar kağıdını asla tam yansıtamaz. Mağaza girişinde ilk 5 saniyesiyle "Vay canına!" dedirtecek hareketli bir tanıtım videosu hazırlayın. Videoda cihazın paralaks etkisiyle nasıl şık durduğunu ve gökyüzündeki gündüz/gece döngüsünü sergileyin.
* **Doğru Zamanlanmış İnceleme İsteği (Review Prompt):** Algoritmalarda yükselmenin sırrı 5 yıldızlı yüksek hacimli yorumlardır. Kullanıcıdan asla uygulamayı ilk açtığında yorum istemeyin. **Kullanıcı ilk duvar kağıdını başarıyla ana ekranına kurup uygulamanızdan mutlu ayrıldığı ilk 5. dakikada** Android'in uygulama içi değerlendirme API'sini tetikleyerek muazzam oranlarda 5 yıldız toplayabilirsiniz.
* **Uzun Kuyruklu Anahtar Kelime Optimizasyonu (ASO):** Mağaza başlığında ve açıklamasında genel kelimeler ("Live Wallpaper") yerinde boğulmak yerine; _"Aesthetic Wallpaper"_, _"Live Sky Simulator"_, _"Pixel Art Backgrounds"_, _"Dynamic Weather Wallpaper"_ gibi uzun, doğrudan aratılan spesifik alanlara (nişlere) odaklanın. Ayrıca halihazırda var olan dil konfigürasyonlarını mağaza giriş sayfası çevirileri (Localization) ile mutlaka destekleyin.
* **Android "Material You" Uyumu:** Uygulamanızın Android'in modern **Material You / Dynamic Colors** sistemleriyle tam uyumlu çalıştığı bilgisini öne çıkarın. Google Play Editörleri, yeni Android API'leriyle paralel olarak estetiğini koruyan uygulamaları "Önerilenler" kısmına koymayı severler.
* **Kullanıcı Tetikleyicileri (Retention Elements):** Bir kullanıcı duvar kağıdını ayarladıktan sonra genelde uygulamaya tekrar girmez. Aktif kullanıcı sayısını canlı tutmak için "Günün Teması" ya da anlık doğa olaylarını bildiren (Örn. "İçinde bulunduğun şehirde şuan bir güneş tutulması var, bunu ekranında gör") gibi kullanıcıları ana uygulamaya tekrar sokacak ufak haberler veya bildirim motivasyonları ekleyin.
