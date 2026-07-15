# Product

Android live wallpaper app written in Kotlin, using Jetpack Compose, `WallpaperService`, OpenGL ES, modular scenes/layers and build-time generated wallpaper catalog metadata.

# Agent policy

- Make the smallest change that satisfies the task; do not refactor, reformat or document unrelated code.
- Inspect only relevant files. Use `rg` or symbol search before opening files; read targeted line ranges in large files.
- Do not scan `.gradle`, `.idea`, `build`, generated outputs, caches or binary assets unless directly relevant.
- Prefer existing architecture, utilities and dependencies; add dependencies only when required.
- Filter large command output and inspect only the first relevant error with nearby context.
- Do not paste complete files or repeat visible code/diffs. Final response: changed files, key decision, validation and unresolved risks; maximum 8 lines unless detail is requested.
- Stop when the stated acceptance criteria are satisfied.

# Git policy

Git commands are allowed when reviewing current changes, preserving user edits or investigating a regression.

Prefer targeted commands:

```bash
git status --short
git diff --stat
git diff -- <relevant-paths>
git diff --cached -- <relevant-paths>
git log -n 5 --oneline -- <relevant-paths>
git show <commit> -- <relevant-paths>
```

- Do not inspect `.git` contents or read full history when targeted commands are sufficient.
- Before editing a modified file, inspect its targeted diff and preserve unrelated user changes.
- Do not repeatedly run full-repository diffs after every edit.
- Never discard changes or run `reset --hard`, `clean`, `restore`, `checkout --`, `rebase`, `commit` or `push` unless explicitly requested.

# Architecture constraints

- Do not modify `engine-core` when adding a wallpaper.
- Register new wallpapers through a wallpaper manifest.
- Domain modules must not depend on Android UI modules.
- Renderers must not access repositories or ViewModels.
- OpenGL calls may run only on the GL thread.
- Parse runtime configuration before rendering; never read JSON from `onDrawFrame`.
- Avoid allocations inside render/update loops.
- Extend shared layers through parameters, composition or decorators; never add wallpaper-specific conditions to shared renderers.

# Rendering and performance

- Rendering is event-driven; static scenes use `RENDERMODE_WHEN_DIRTY`.
- Each layer declares its update interval; render only when a visible layer is dirty.
- Shader compilation failures use a safe fallback; explicitly release shader and texture resources.
- Catalog cards use generated thumbnails, not one `GLSurfaceView` per card.
- Run full GL only on the preview screen.
- Do not decode textures or upload to GPU during scrolling.
- Load large textures on demand and release them through an LRU policy.
- Avoid per-frame allocations, collection rebuilding, JSON access, bitmap decoding and unnecessary GL state changes.

# Validation

Run the smallest relevant check first, for example:

```bash
./gradlew :app:compileDebugKotlin
./gradlew :<module>:testDebugUnitTest --tests "*RelevantTest"
```

Use only applicable project checks:

```bash
./gradlew ktlintCheck
./gradlew detekt
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew validateWallpaperPacks
./gradlew validateShaders
```

Run the complete set only for shared/cross-module engine changes, manifests/catalog generation, shaders, releases or when explicitly requested. Fix only failures caused by the current change and report the first relevant unresolved failure.

# Lumisky V8 Toolkit ekleri

## V8 çalışma değişmezleri

- EGL ve tüm GL handle'ları yalnız render thread owner'ıdır.
- Compose, sensor, broadcast ve `WallpaperService.Engine` yalnız immutable `RenderCommand` üretir.
- JSON, asset I/O, shader source okuma ve texture decode render hot path dışında yapılır.
- Görünmez wallpaper/preview için render, sensor, video ve scheduler işi sıfırdır.
- Scene geçişi transactionaldır; candidate ilk başarılı swap'i üretmeden current/last-successful yapılmaz.
- Surface ve EGL context lifecycle ayrıdır; context-loss eski GL ID kullanmaz.
- Katalog thumbnail-first'tür; scroll sırasında aktif GL preview 0, durduğunda aynı anda en fazla 1 lease.
- Video `updateTexImage()` yalnız current EGL context'li render thread'de çağrılır.

## V8 modül sınırları

- `app`: Compose UI, katalog, seçim, detail/fullscreen preview ve lease istemcisi.
- `core`: typed modeller, contractlar, config, location/daylight ve saf policy'ler.
- `engine`: compiler, compiled scene, layer graph, scheduler, frame demand ve renderer backend.
- `wallpaper`: `WallpaperService` köprüsü, command mailbox ve render thread/EGL lifecycle.
- `build-logic`: wallpaper pack, schema/asset/shader/video/license validation ve generated index.
- `benchmark`: ölçüm ve release gate; production davranışını değiştirmez.

## V8 çalışma yöntemi

- Her görevde önce `git status --short`, sonra hedefli `rg` araması ve en fazla gerekli dosya okunur.
- Kullanıcının mevcut değişiklikleri korunur; broad rewrite, format-only diff ve gereksiz dependency eklenmez.
- Değişiklikten sonra en dar ilgili compile/test/validator çalıştırılır.

## Kullanıcı kilitli ürün davranışları

- Wallpaper kartı yalnız preview adayı hazırlayıp doğrudan Android sistem live wallpaper picker'ını açar; sistem picker'daki Set düğmesine basılmadan aktif wallpaper değişmez ve kullanıcı istemeden uygulama içi ara set/preview ekranı eklenmez.
- Katalog fokus animasyonu sistem picker/canlı wallpaper animasyonundan ayrıdır; v2 gibi wallpaper'ın günışığı `sunriseMinute` başlangıcını ve gündüz/gece sarma kuralını kullanır, 4 saniye sürer ve kullanıcı istemeden değiştirilmez.
- Sistem picker önizlemesi v2 gibi kesintisiz 8 saniyelik tam gün döngüsü oynatır; kart fokusu yapay gecikmesiz başlar ve animasyon saati ilk render edilebilir kareyle birlikte başlatılır.
- Canlı wallpaper kilit ekranı uyandırıldığında veya başka bir uygulamadan ana ekrana dönüldüğünde 2 saniyelik catch-up animasyonunu yalnız güncel zamanın gündüz/gece modu içinde oynatır; ilk görünür kare karşı moda veya sınır karesine düşmez ve kullanıcı istemeden değiştirilmez.
- Uygulama başlangıç ikonu, katalog verisi ve thumbnail cache'i hazırlanıp ana ekran iki başarılı Compose karesi üretmeden kaldırılmaz; kullanıcı istemeden değiştirilmez.
- Cihaz konumu etiketi ters geocode edilmiş yer adı gösterir; koordinatlar kullanıcıya konum adı olarak gösterilmez.
- APK çalıştırma, cihaz seçme ve UI incelemede kuruluysa resmi Android CLI (`android run`, `android layout`) tercih edilir; APK üretimi Gradle ile yapılır.
