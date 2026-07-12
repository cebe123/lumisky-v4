# V8 kısa referans — preview ve performans

- Live: başlangıç 30 FPS; pil öncelikli; özel scene/device dışında kör 60 yok.
- Fullscreen: başlangıç 60, mutlak max 120.
- Catalog active preview: başlangıç 30, mutlak max 60; max 1 lease; scroll sırasında 0.
- Display-compatible FPS basamakları filtrelenir.
- Degrade hızlı: deadline miss/thermal/power -> FPS, render scale veya quality düşür.
- Promote yavaş: uzun stabil pencere sonrası tek basamak artır.
- Video source FPS compositor FPS değildir.
- Thumbnail ilk frame başarılı swap'e kadar görünür.
- Ölçüm: Macrobenchmark UI; Perfetto/AGI/batterystats live/GPU/pil.
