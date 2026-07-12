# Hızlı path/sorumluluk yönlendirmesi

| Alan | Öncelikli yollar/semboller |
|---|---|
| Command/session | `wallpaper/**RenderController*`, `engine/**RenderEngineSession*`, `**RenderCommand*`, `**SceneScheduler*` |
| Sensor/parallax | `core/**Sensor*`, `engine/**Parallax*`, `**FrameDemand*` |
| EGL/resources | `wallpaper/**Egl*`, `engine/**Texture*`, `**ShaderProgram*`, `**Framebuffer*` |
| Layer/shader | `engine/**Layer*`, `**SceneCompiler*`, `assets/**.glsl`, `assets/**definition*.json` |
| Preview | `app/**Preview*`, `engine/**Preview*`, `**PreviewLease*`, `**FrameRateGovernor*` |
| Location | `core/**Location*`, `**Daylight*`, `**SunTimes*`, settings location modelleri |
| Video | `engine/**Video*`, `**SurfaceTexture*`, `**MediaController*`, Media3 bağımlılığı |
| Content pack | `build-logic/**`, catalog/definition parser-validator, wallpaper asset klasörleri |

Gerçek repo ağacı bu öneriden farklıysa `settings.gradle.kts` ve mevcut source tree kaynak-of-truth'tur. Dosya adı varsayımıyla yeni paralel sınıf oluşturma.
