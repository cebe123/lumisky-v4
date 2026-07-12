# Lumisky V8 — Codex Çalışma Kuralları

Bu dosya proje kökünde, `settings.gradle.kts`, `build.gradle.kts` ve `gradlew` ile aynı seviyede bulunmalıdır.

## 1. Görev başlangıcı: düşük-token protokolü

Her görevde sırasıyla:

1. Bu dosyayı ve çalışılan dizine en yakın alt `AGENTS.md` dosyasını uygula.
2. Görevi tek bir ana alana sınıflandır: `runtime`, `egl`, `render/layer`, `preview`, `location`, `video`, `content-pack`, `ui`, `build/test`.
3. Önce `git status --short` çalıştır; kullanıcının mevcut değişikliklerini koru.
4. Sembol veya dosya adıyla hedefli `rg` araması yap. İlk aşamada tüm repoyu okumaya çalışma.
5. İlk bağlam bütçesi: en fazla 6 kaynak dosya ve toplam yaklaşık 1200 satır. Kanıtla ihtiyaç oluşursa en fazla 4 dosya daha aç.
6. Bir görevde tercihen 1 ana skill, gerekirse en fazla 1 yardımcı skill kullan. Skill zinciri kurma.
7. Büyük V8 belgesini baştan sona okuma. Önce `.agents/references/` içindeki ilgili kısa özeti kullan; yalnız belirsizlik kalırsa `docs/architecture/lumisky_v8_architecture.md` içindeki hedef bölümü aç.
8. Değişiklikten önce bir cümlelik kapsam yaz: `Scope: <modül/sınıf/davranış>`.

## 2. Bağlam ve token kuralları

- `build/`, `.gradle/`, `.idea/`, generated çıktılar, binary assetler ve büyük loglar görev gerektirmedikçe okunmaz.
- Tam dosya yerine ilgili sembol ve yakın satırlar okunur.
- Aynı dosya veya mimari kural tekrar tekrar özetlenmez.
- Kullanıcı yalnız analiz istediğinde kod değiştirilmez.
- Basit görevlerde plan belgesi üretilmez; doğrudan küçük değişiklik ve doğrulama yapılır.
- Var olan sınıf, factory, policy ve utility tercih edilir; paralel ikinci sistem kurulmaz.
- Yeni dependency ancak mevcut araçlarla çözülemiyorsa ve kullanıcı onayladıysa eklenir.
- Bir hata için en fazla iki makul hipotez aynı anda takip edilir; geniş spekülatif araştırma yapılmaz.
- Subagent yalnız bağımsız iki araştırma alanı gerçekten paralelse kullanılır. Varsayılan maksimum thread sayısı 2'dir.

## 3. V8 mimari değişmezleri

1. Big-bang rewrite yok; strangler migration ve küçük patch.
2. EGL ve bütün GL handle'ları yalnız render thread owner'ıdır.
3. Compose, sensor, broadcast ve `WallpaperService.Engine` yalnız immutable `RenderCommand` üretir.
4. `SceneScheduler`, parallax, frame state, cache ve diğer mutable runtime state session-bound'dır; singleton değildir.
5. JSON, asset I/O, shader source okuma ve texture decode render hot path dışında yapılır.
6. Render hot path typed/compiled olmalı; map/string policy lookup ve motor kaynaklı collection/data-class allocation olmamalı.
7. `update` cadence ile gerçek `draw/swap` cadence ayrı değerlendirilir.
8. Görünmez wallpaper/preview için render, sensor, video ve scheduler işi sıfırdır.
9. Scene geçişi transactionaldır; candidate ilk başarılı swap'i üretmeden current/last-successful yapılmaz.
10. Surface ve EGL context lifecycle ayrıdır; context-loss eski GL ID kullanmaz.
11. Katalog thumbnail-first'tür; scroll sırasında aktif GL preview 0, durduğunda aynı anda en fazla 1 lease.
12. Fullscreen preview mutlak max 120 FPS; katalog preview max 60 FPS. FPS display, scene cost, thermal, battery ve frame istikrarıyla çözülür.
13. FPS düşürme hızlı, yükseltme yavaş ve hysteresis'li olmalıdır.
14. Kaynak türleri typed'dır: `LAYERED_IMAGE`, `HYBRID`, `VIDEO`, `PROCEDURAL`.
15. `HybridSceneRendererBackend` compiled layer graph yönetir; monolitik mega-layer değildir.
16. İlk production layer seti: `TextureLayer`, `ShaderEffectLayer`, `ParticleLayer`, `VideoOesLayer`, `Color/GradientLayer`, `LayerGroupNode`.
17. Transparent layer sırası `pass -> zIndex -> declarationOrder` ile korunur; batching için yeniden sıralanmaz.
18. `VideoOesLayer.updateTexImage()` yalnız current EGL context'li render thread'de çağrılır; callback sadece frame flag/dirty reason üretir.
19. Video source FPS ile compositor FPS ayrıdır; 30 FPS video için 120 decoder update yapılmaz.
20. DEVICE konum tercihi korunur; GPS kapalıysa uygun last-known kullanılabilir; timezone koordinata göre çözülür; polar durum modellenir.
21. Segmentation, inpainting, transcode ve wallpaper paket üretimi runtime'da yapılmaz; offline/build-time compiler işidir.
22. FBO, shader binary cache, PAD, mesh warp ve ağır post-process ancak profiler kanıtıyla eklenir.

## 4. Modül sınırları

- `app`: Compose UI, katalog, seçim, detail/fullscreen preview ve lease istemcisi. GL motor state'i sahiplenmez.
- `core`: typed modeller, contractlar, config, location/daylight ve saf policy'ler. GL ID veya Android view içermez.
- `engine`: compiler, compiled scene, layer graph, scheduler, frame demand, renderer backend ve session-bound GL kaynakları.
- `wallpaper`: `WallpaperService` köprüsü, command mailbox, render thread/EGL lifecycle, visibility/power/thermal entegrasyonu.
- `build-logic`: wallpaper pack compiler, schema/asset/shader/video/license validation ve deterministic generated index.
- `benchmark`: ölçüm ve release gate; production davranışını değiştirmez.

Dependency yönü ihlal edilmez. UI katmanı engine implementation'a değil contract/facade'a bağımlı olmalıdır.

## 5. Skill yönlendirme

| Görev | Ana skill |
|---|---|
| Minimum ilgili dosyaları bulma | `$lumisky-context-scout` |
| Küçük ve güvenli kod değişikliği | `$lumisky-surgical-editor` |
| Command/session/scheduler/sensor/frame demand | `$lumisky-runtime-session` |
| EGL, context loss, texture/program/FBO lifecycle | `$lumisky-egl-resources` |
| Shader, layer graph, animation, parallax | `$lumisky-layer-shader` |
| Fullscreen/katalog/live FPS ve preview lease | `$lumisky-preview-performance` |
| Location, timezone, sunrise/sunset, polar state | `$lumisky-location-daylight` |
| Media3, SurfaceTexture, OES video | `$lumisky-video-oes` |
| Wallpaper ZIP/JSON/assets/compiler/validator | `$lumisky-wallpaper-pack` |
| Değişen yola göre test seçimi | `$lumisky-test-selector` |
| Diff ve mimari invariant review | `$lumisky-diff-review` |
| Oturum sonunda kısa devir notu | `$lumisky-session-handoff` |

## 6. Değişiklik politikası

- SMALL PATCH MODE: yalnız görevin gerektirdiği dosyaları değiştir.
- Public API, package yapısı ve mevcut data formatı gereksiz yere değiştirilmez.
- Rename/move işlemi ancak doğrudan görev gerektiriyorsa yapılır.
- Format-only geniş diff oluşturma.
- Kullanıcının mevcut değişikliklerini silme veya üzerine yazma.
- Yeni sistemi eklerken legacy yol için feature flag/fallback korunur.
- Performans iddiası ölçüm veya en azından açık bir doğrulama planı olmadan yapılmaz.
- Shader değişikliğinde visual parity, compile/link ve GLES fallback kontrol edilir.
- JSON/definition değişikliğinde schema + semantic validation birlikte düşünülür.

## 7. Git kullanımı

İzinli ve tercih edilen read-only komutlar:

```text
git status --short
git diff -- <hedef-yollar>
git diff --cached -- <hedef-yollar>
git log -n 5 -- <hedef-yollar>
git show <commit> -- <hedef-yollar>
```

Açık istek olmadan `commit`, `push`, branch oluşturma veya PR açma. `reset`, `clean`, kullanıcı değişikliklerini geri alan `restore/checkout` ve force işlemleri yasaktır.

## 8. Doğrulama politikası

En dar yeterli doğrulamayla başla:

1. Değişen modülün compile görevi.
2. İlgili unit test sınıfı veya modül testi.
3. Shader/definition/asset değiştiyse ilgili validator.
4. Cross-module lifecycle değiştiyse debug assemble + hedef integration testi.
5. Full suite yalnız release gate, geniş refactor veya kullanıcı isteğinde.

Test çalıştırılamadıysa sebebi ve çalıştırılması gereken kesin komutu yaz.

## 9. Cevap formatı

Tamamlama cevabı en fazla şu dört bölümü içerir:

- `Değişti:` dosya ve davranış özeti.
- `Doğrulandı:` çalıştırılan komut/test.
- `Risk:` yalnız gerçek kalan risk.
- `Sonraki adım:` yalnız zorunlu tek adım varsa.

Uzun mimari anlatımı kullanıcı istemedikçe tekrarlama.
