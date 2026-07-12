# Lumisky — Samurai Sunset Wallpaper Package

## İçerik

- `HYBRID` üretim yöntemi
- Shader tabanlı dinamik gökyüzü, güneş ve ay
- 7 adet full-canvas RGBA katman
- OpenGL ES 3 shader seti
- OpenGL ES 2 fallback shader seti
- Mevcut Lumisky manifest/creator-layer akışıyla uyumlu `manifest.json`
- Gelecekteki typed layer backend için `definition.layered-v1.json`
- 1440×2560 master, 1080×1920 high runtime, 720×1280 low runtime
- Static fallback, thumbnail, checksum ve doğrulama raporu

## Kurulum

```bash
python <paket-klasörü>/install_into_lumisky.py <lumisky-proje-kökü>
```

Mevcut paketi değiştirmek için:

```bash
python <paket-klasörü>/install_into_lumisky.py <lumisky-proje-kökü> --force
```

Manuel kurulumda `app/` ağacını proje köküyle birleştir ve
`install/index_entry.json` girdisini
`app/src/main/assets/wallpapers/index.json` içindeki `wallpapers` dizisine ekle.

## Aktif dosyalar

- Manifest: `app/src/main/assets/wallpapers/samurai_sunset/manifest.json`
- Layered definition: `app/src/main/assets/wallpapers/samurai_sunset/definition.layered-v1.json`
- Legacy/current shader: `app/src/main/assets/shaders/samurai_sunset/fragment.glsl`
- GLES2/GLES3 shaderlar: `app/src/main/assets/shaders/samurai_sunset/gles2|gles3`
- Master katmanlar: `assets/layers/*.png`
- Runtime katmanlar: `assets/runtime/high|low/*.webp`

## Doğrulama

```bash
python tools/validate_package.py
```

Manifest mevcut Lumisky'nin sekiz creator-image katmanı sınırının altında,
toplam yedi katman kullanır.
