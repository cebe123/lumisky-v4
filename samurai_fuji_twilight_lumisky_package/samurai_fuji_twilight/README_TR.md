# Samurai — Fuji Alacakaranlığı — Lumisky Paketi

## Seçilen yöntem

**STATIC TIME-SLICE MODE** ana stratejidir.

Kaynak görsel; düz/illustratif renk blokları, özgün ve düzensiz bulut silüetleri, büyük güneş diskinin bulutlarla ve Fuji ile örtüşmesi ve güçlü foreground silüetleri içeriyor. Tam shader yeniden üretimi teknik olarak mümkün olsa da kaynak kompozisyona ve çizim diline yeterince yakın kalmak için gereksiz karmaşıklık ve görsel sapma oluşturacaktı. Bu nedenle altı statik gökyüzü planı, GLES tabanlı yumuşak crossfade ve ortak parallax katmanları kullanıldı.

Ana referans korunur: `composition_sunset.png`, semantic layer compositing ile `source_reference.png` kompozisyonuna piksel düzeyinde yaklaşır. Gece planlarındaki ay kaynak stiline uygun, yumuşak kenarlı illustratif disk olarak prosedürel üretilmiş ve ilgili time-slice texture'larına bake edilmiştir.

## Paket yapısı

```text
samurai_fuji_twilight/
├── README_TR.md
├── assets/
│   ├── reference/source_reference.png
│   ├── preview/
│   │   ├── thumbnail.webp
│   │   ├── full_preview.png
│   │   ├── composition_preview.png
│   │   └── composition_*.png
│   ├── layers/*.png
│   └── time_slices/*.png
├── shaders/
│   ├── gles3/*.vert|*.frag
│   └── gles2/*.vert|*.frag
└── metadata/
    ├── wallpaper_definition.json
    ├── catalog_entry.json
    ├── layer_manifest.json
    └── validation_report.json
```

Bütün yollar paket köküne göre relatiftir. Paket dışı bağımlılık yoktur.

## Katmanlar ve draw order

1. `sky_time_slice`
2. `far_mountains`
3. `main_mountain`
4. `mid_mountains`
5. `clouds_layer`
6. `pagoda_or_architecture`
7. `foreground_cliff`
8. `left_foreground_tree`
9. `foreground_details`
10. `main_subject_samurai`

Katmanlar 1440×2560 full-canvas RGBA PNG'dir. Runtime, `contentBoundsPx` alanını kullanarak texture upload sırasında alpha bounds'a crop etmelidir. Böylece full-canvas kaynak bütünlüğü korunurken GPU belleği düşer.

## Parallax değerleri

Bu pakette proje kuralı uygulanır: **uzak katman daha çok, yakın katman daha az hareket eder**. Dikey tepki yataydan düşüktür.

| Katman | responseX | responseY | clampX | clampY | smoothing | overscanScale |
|---|---:|---:|---:|---:|---:|---:|
| `far_mountains` | 0.03 | 0.012 | 18 | 8 | 0.86 | 1.035 |
| `main_mountain` | 0.025 | 0.01 | 15 | 7 | 0.87 | 1.03 |
| `mid_mountains` | 0.02 | 0.008 | 12 | 6 | 0.88 | 1.025 |
| `clouds_layer` | 0.018 | 0.007 | 11 | 5 | 0.88 | 1.025 |
| `pagoda_or_architecture` | 0.012 | 0.005 | 8 | 4 | 0.9 | 1.02 |
| `foreground_cliff` | 0.004 | 0.002 | 4 | 2 | 0.93 | 1.012 |
| `left_foreground_tree` | 0.008 | 0.003 | 6 | 3 | 0.92 | 1.018 |
| `foreground_details` | 0.005 | 0.002 | 4 | 2 | 0.93 | 1.014 |
| `main_subject_samurai` | 0.0025 | 0.0015 | 3 | 2 | 0.95 | 1.01 |

Gökyüzü time-slice katmanı sabittir. Her semantic katmanda maksimum clamp değerinden büyük, inpaint edilmiş iç reveal strip bulunur; küçük parallax hareketlerinde eski nesne kenarının görünmesini engeller.

## Zaman dilimleri ve geçiş

Planlar:

- `dawn` — sabah
- `noon` — öğlen
- `sunset` — kaynak referans gün batımı
- `moonrise` — ay doğumu
- `midnight` — gece yarısı
- `moonset` — ay batımı

Renderer yalnız mevcut ve sonraki **full-scene** slice texture'ını resident tutar. `u_SliceMix`, `SMOOTHSTEP` eğrisiyle 0→1 ilerler. Ana crossfade sırasında semantic katmanlar çizilmez; böylece flatten edilmiş kaynaktan doğabilecek sınır/halo artefaktları oluşmaz. Bir sonraki texture geçişten 30 saniye önce preload edilir.

Önerilen seçim mantığı:

```kotlin
data class Slice(val anchorMinute: Int, val transitionSeconds: Int)

fun cyclicMinuteDistance(from: Int, to: Int): Int =
    ((to - from) % 1440 + 1440) % 1440

fun smoothstep01(x: Float): Float {
    val t = x.coerceIn(0f, 1f)
    return t * t * (3f - 2f * t)
}
```

## Gün ilerleme, sunrise ve sunset fraction

```kotlin
val minuteOfDay = localTime.hour * 60 + localTime.minute
val dayProgress = minuteOfDay / 1440f

val sunriseProgress = sunriseMinutes / 1440f
val sunsetProgress = sunsetMinutes / 1440f
```

Konum izni yoksa varsayılan 06:00 / 18:00 kullanılır. Konum veya saat dilimi değiştiğinde astronomik hesap yeniden yapılmalıdır. JSON her frame okunmamalıdır; ilk yüklemede parse edilip immutable runtime modele dönüştürülmelidir.

## Moon phase

Ana ay görünümü time-slice texture'larına bake edilmiştir. `u_MoonPhase`, GLES fallback/debug modunda 0.0–1.0 aralığında kullanılır. Gerçek faz verisi varsa texture ay yoğunluğu ve fallback shader parametresi buna göre ölçeklenebilir; bake edilmiş kompozisyonu tamamen bozacak sert maskeler uygulanmamalıdır.

## Shaderlar

- `sky_celestial_combined.frag`: ana iki-texture crossfade shaderı.
- `sky.frag`: procedural düşük maliyetli fallback sky.
- `sun.frag`: procedural güneş fallback/debug.
- `moon.frag`: procedural ay fallback/debug.
- `fullscreen.vert`: fullscreen textured quad.

GLES3 birincil, GLES2 eşdeğerleri zorunlu fallback'tir. Main strategy texture slice olduğu için shader maliyeti stabildir: geçiş dışında 1 texture sample, geçiş sırasında 2 texture sample.

## Android entegrasyon sırası

1. ZIP'i uygulama içi paket dizinine aç.
2. `metadata/wallpaper_definition.json` dosyasını bir kez parse et.
3. `minimumGles` kontrolünü yap; GLES3 yoksa GLES2 shader yollarına geç.
4. Katman PNG'lerini `contentBoundsPx` + padding ile crop ederek decode/upload et.
5. Mevcut ve sonraki time-slice texture'ını yükle.
6. Ana modda full-scene slice quad'ını çiz; `sunset` referans profilinde semantic katmanları isteğe bağlı parallax overlay olarak etkinleştir.
7. Dakika değiştiğinde slice seçimini ve `u_SliceMix` değerini güncelle.
8. Sensör/parallax değişiminde 450 ms render burst başlat; sonra `RENDERMODE_WHEN_DIRTY` durumuna dön.
9. `onVisibilityChanged(false)` geldiğinde render ve sensör aboneliklerini durdur.

Örnek lifecycle iskeleti:

```kotlin
fun onVisibilityChanged(visible: Boolean) {
    if (visible) {
        timeTicker.startMinuteTicks()
        parallaxController.start()
        renderer.requestFrame()
    } else {
        timeTicker.stop()
        parallaxController.stop()
        renderer.cancelBurst()
        textureCache.trimToCurrentScene()
    }
}
```

## FPS davranışı

- Live wallpaper: normal 30 FPS tavan; battery saver 15; thermal severe 8–15; idle continuous render yok.
- Catalog: scroll sırasında aktif renderer 0; durunca yalnız tek renderer; cihaz sınıfına göre 15/30/60 FPS.
- Fullscreen preview: 60 FPS başlangıç; frame deadline stabilse 90/120; sorun varsa hızlı düşüş, yavaş yükseliş.

## Context loss

`EGL_CONTEXT_LOST` sonrası:

1. Program/shader objelerini yeniden oluştur.
2. Mevcut ve sonraki time-slice texture'ını yeniden yükle.
3. Semantic layer texture'larını görünürlük ve z-order sırasıyla lazy restore et.
4. Son bilinen `dayProgress`, slice çifti ve parallax değerini uygula.
5. Tek bir zorunlu redraw iste.

## JSON parser entegrasyonu

- Parse işlemi wallpaper seçildiğinde bir kez yapılmalıdır.
- Relative path normalizasyonundan sonra `..`, mutlak yol ve URI şemaları reddedilmelidir.
- `requiredAssets` listedeki her dosya açılmadan önce package root altında resolve edilmelidir.
- Runtime model immutable tutulmalıdır.
- Her frame JSON/disk okuması yapılmamalıdır.

## Test adımları

1. `validation_report.json` sonucu `PASS` olmalı.
2. `composition_preview.png` ile `source_reference.png` görsel farkı kontrol edilmeli.
3. 00:00→24:00 hızlandırılmış testte bütün slice geçişleri sıçramasız olmalı.
4. Midnight wrap-around test edilmeli.
5. Parallax uç değerlerinde şeffaf kenar/ghosting olmamalı.
6. GLES3 ve GLES2 cihaz/emülatörde shader compile/link testi yapılmalı.
7. Context loss simülasyonunda texture ve programlar geri gelmeli.
8. 720p/1080p/1440p decode profillerinde bellek ölçülmeli.
9. `visible=false` durumunda frame counter ve sensör callback'leri durmalı.
10. Catalog scroll sırasında aktif GL renderer sayısı sıfır olmalı.

## Sınırlamalar

- Kaynak tek, flatten edilmiş raster görseldir; semantic alpha maskeleri otomatik renk/konum analiziyle çıkarılmıştır. İnce saç, çimen ve zırh detayları bağımsız el rotoskopisi kadar kusursuz değildir.
- Dawn/noon/night görüntüleri aynı kaynak stilinden kontrollü color-grade ve prosedürel celestial üretimiyle türetilmiştir; bağımsız sanatçı çizimi değildir.
- Shader dosyaları statik olarak doğrulanmıştır; gerçek cihazda vendor-specific GLES derleyici testi uygulama CI/device farm aşamasında yapılmalıdır.


## Full-scene slice kararı

Ana runtime kaynağı `assets/time_slices/*.png` içindeki birleşik sahnelerdir. Bu karar, tek flatten edilmiş kaynakta otomatik semantic maskelerin farklı color-grade planlarında görünür sınır üretmesini engeller. Katman PNG'leri yine tam teslim edilmiştir; sunset/reference görünümünde veya uygulama içi kalite onayından sonra katmanlı parallax profili açılabilir.


## Full-scene slice kararı

Ana runtime kaynağı `assets/time_slices/*.png` içindeki birleşik sahnelerdir. Bu karar, tek flatten edilmiş kaynakta otomatik semantic maskelerin farklı color-grade planlarında görünür sınır üretmesini engeller. Katman PNG'leri yine tam teslim edilmiştir; sunset/reference görünümünde veya uygulama içi kalite onayından sonra katmanlı parallax profili açılabilir.
