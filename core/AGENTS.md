# Core modülü kuralları

- Sorumluluk: immutable/typed domain modelleri, contracts, config, source kinds, asset tiers, location/daylight ve saf policy.
- GL/EGL ID, Android View, SurfaceTexture veya renderer implementation içermez.
- Mutable runtime state ve session cache singleton yapılmaz.
- `WallpaperSourceKind`: `LAYERED_IMAGE`, `HYBRID`, `VIDEO`, `PROCEDURAL` değerlerini koru.
- Definition/schema modelleri backward-compatible migration ve explicit validation sonucu üretmelidir; silent null/drop yok.
- Location DEVICE tercihi korunur; provider kapalıyken izinli last-known politikası, timezone ve polar durum typed modellenir.
- Policy fonksiyonları deterministic ve unit-test edilebilir olmalıdır.

İlk doğrulama: ilgili unit test + `:core:compileDebugKotlin`.
