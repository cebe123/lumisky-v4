# Wallpaper modülü kuralları

- `WallpaperService.Engine`, sensor, broadcast ve UI yalnız immutable command üretir.
- FIFO: attach/detach/one-shot/stop. Latest-wins: resize/parallax/touch/policy/thermal/power/location/apply hazırlanmamışsa.
- `onVisibilityChanged(false)` sonrası render callback, sensor, video ve scheduler işi sıfır olmalıdır.
- Surface ve EGL context lifecycle ayrıdır; context lost durumunda stale GL ID silmeye çalışma/kullanma.
- Scene activation transactionaldır; first successful swap sonrası persist/commit.
- `onComputeColors()` main thread'de GL readback veya blocking çalışma yapmaz.
- Power saver/thermal state per-frame poll edilmez; event/command ile güncellenir.
- Location/daylight değişiklikleri render state'e command olarak aktarılır.
- Shutdown bounded olmalı; caller thread'den render engine metodu çağrılmamalı.

İlk doğrulama: lifecycle/state-machine testleri + `:wallpaper:compileDebugKotlin`. EGL değişikliğinde surface recreate/context-loss harness zorunludur.
