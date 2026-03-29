# Wallpaper Brief Template

Bu metni yeni bir wallpaper isterken doğrudan kullan.

## Kısa İstek Şablonu

Yeni bir wallpaper oluştur.

- Wallpaper id: `[ornek_id]`
- Görünen isim: `[Ornek Isim]`
- Kategori: `[special / landscapes / cities / abstract / games]`
- Arkaplan texture: `[dosya_adi.webp]`
- Güneş texture: `[dosya_adi.webp]`
- Ay texture: `[dosya_adi.webp]`
- İsteniyorsa ek texture: `[flare/cloud vb]`

## Görsel Davranış

- Ufuk çizgisi: `[yukarı / aşağı / mevcut / yaklaşık offset]`
- Gökyüzü stili: `[warrior / tablo / city / özel açıklama]`
- Güneş davranışı:
  `[normal / arkada kalsın / dışa glow versin / iç parlaklık düşük olsun]`
- Ay davranışı:
  `[normal / arkada kalsın / glow düşük olsun / daha soluk olsun]`
- Gece gökyüzü:
  `[daha koyu / biraz koyu / mevcut]`
- Foreground gece kararması:
  `[yok / hafif / üstten aşağı / tüm texture hafif kararsın]`
- Yıldızlar:
  `[yok / istanbul mantığı / sabit / %20 twinkle / daha belirgin]`

## Texture ve Geçiş Kuralları

- Texture sınırı görünmesin.
- Gerekirse Cherry Blossom yaklaşımı gibi üst sınır preprocess ile yumuşatılsın.
- Siyah border oluşursa shader ile gizlemek yerine önce texture sınırı / alpha geçişi düzeltilsin.
- Dağ veya foreground geçişi gökyüzüne sert oturmasın; uzaklara gidiyormuş hissi verecek şekilde opacity ve atmosphere karışımı ayarlansın.
- Güneş ve ay, foreground’un önüne taşmamalıysa arkada kalacak şekilde çizilsin.

## Preview ve Snapshot Kuralları

- Home preview gündoğumundan başlasın.
- Home preview hızlı açılsın.
- Kart preview’de preview-quality texture yükle.
- Sadece seçilince / odaklanınca full-quality çalıştır.
- Zenith snapshot güncellensin.
- Snapshot standart pipeline boyutunda olsun; oversized tekil preview bırakılmasın.

## Teknik Kurallar

- Yeni preset `WallpaperCatalog.kt` içine eklensin.
- Gerekirse özel shader `app/src/main/assets/shaders/[id]/fragment.glsl` altında oluşturulsun.
- Teemo/Cherry Blossom’da öğrenilen sınır düzeltme ve preview performans yaklaşımı korunsun.
- Asset pipeline bozulmasın.
- Preview ve gerçek wallpaper davranışı mümkün olduğunca tutarlı olsun.

## Teslim Beklentisi

- Hangi dosyaların değiştiğini yaz.
- Snapshot güncellendiyse belirt.
- Border / preview / glow / yıldız / gece geçişi gibi istenen noktaların ne şekilde uygulandığını kısa özetle.
- Sonunda şu komutlarla doğrula:
  - `.\gradlew :app:testDebugUnitTest`
  - `.\gradlew :app:assembleDebug`
  - `.\gradlew :app:deployDebugToConnectedDevice`

## Doldurulmuş İstek Örneği

Yeni bir wallpaper oluştur.

- Wallpaper id: `game_example`
- Görünen isim: `Example`
- Kategori: `games`
- Arkaplan texture: `example/example.webp`
- Güneş texture: `example/example_sun.webp`
- Ay texture: `example/example_moon.webp`

İstekler:

- Warrior tipi gökyüzü render kullan.
- Ufuk çizgisini biraz aşağı al.
- Güneş ve ay foreground’un arkasında kalsın.
- Güneş hafif dış glow versin, ay glow daha zayıf olsun.
- Gece gökyüzü biraz daha koyu olsun.
- Foreground gece olduğunda hafif kararsın.
- Yıldızlar sabit kalsın ama yaklaşık `%20` kadarı yanıp sönsün.
- Texture sınırı görünmesin, siyah border oluşursa kaldır.
- Home preview gündoğumundan başlasın.
- Home preview hızlı açılsın; preview-quality yükle, seçilince full-quality çalıştır.
- Zenith snapshot güncelle.
- Build al ve telefona deploy et.
