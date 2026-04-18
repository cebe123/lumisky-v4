# Wallpaper Brief Template

Bu metni yeni bir wallpaper isterken doğrudan kullan.

## Kısa İstek Şablonu

Yeni bir wallpaper oluştur.

- Wallpaper id: `[Lisbon]`
- Görünen isim: `[Lisbon]`
- Kategori: `[Cities]`
- Arkaplan texture: `[promptta verilen görsel(Lisbon.png)]`

## Görsel Davranış

- Ufuk çizgisi: `[ekranın ortası]`
- Gökyüzü stili: `[vec3 getLisbonSkyColor(vec2 uv) {
    // uv.y = 0.0 alt, 1.0 üst
    float y = clamp(uv.y, 0.0, 1.0);

    // Ufuk tarafı: açık, hafif turkuazımsı mavi
    vec3 horizonColor = vec3(0.52, 0.76, 0.93);

    // Orta gökyüzü: temiz parlak mavi
    vec3 midColor = vec3(0.30, 0.60, 0.88);

    // Üst gökyüzü: daha doygun ve derin mavi
    vec3 topColor = vec3(0.14, 0.42, 0.76);

    // Alt -> orta
    float t1 = smoothstep(0.0, 0.58, y);
    vec3 sky = mix(horizonColor, midColor, t1);

    // Orta -> üst
    float t2 = smoothstep(0.45, 1.0, y);
    sky = mix(sky, topColor, t2 * 0.85);

    return sky;
} ]`
- Güneş davranışı:
  `[solar ile aynı]`
- Ay davranışı:
  `[solar ile aynı]`
- Gece gökyüzü:
  `[biraz koyu]`
- Foreground gece kararması:
  `[hafif]`
- Yıldızlar:
  `[daha belirgin]`

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

## Performans Template'i

Bu bölüm opsiyoneldir. Sadece istediğim wallpaperlarda uygula.

Kısa komut:

- Bu wallpaperda `preview texture set + build-time preprocess` template'ini uygulama.

Ne anlama gelir:

- Home kart preview'inde ayrı `_preview` texture seti üret ve kullan.
- Wallpaper seçilince / focus olunca full texture setine geç.
- Runtime'da ağır texture düzeltmesi yapma; gerekiyorsa build-time'da üret.
- Build-time preprocess sadece gerçekten border / alpha / üst sınır sorunu olan wallpaperlarda açılsın.
- Sorun yoksa sadece preview texture seti uygula, preprocess ekleme.

Uygun kullanım:

- Büyük texture kullanan wallpaperlar
- İlk preview frame'i geç gelen wallpaperlar
- Şeffaf kenar veya üst sınır border sorunu olan foreground texture'lar

Uygun olmayan kullanım:

- Küçük ve zaten hızlı yüklenen wallpaperlar
- Preprocess uygulanınca kenar rengi veya detay kaybı riski olan temiz asset'ler

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