# Lumisky Codex V8 Toolkit — Kurulum

## Yerleşim

ZIP içeriğini Android proje köküne çıkar. Doğru seviye:

```text
lumisky-v4/
├── AGENTS.md
├── settings.gradle.kts
├── build.gradle.kts
├── gradlew
├── .agents/
├── .codex/
├── app/AGENTS.md
├── core/AGENTS.md
├── engine/AGENTS.md
└── wallpaper/AGENTS.md
```

Mevcut dosyaları otomatik ezmeden önce diff kontrolü yap. Özellikle daha önce oluşturduğun kök `AGENTS.md` varsa iki dosyayı birleştir; V8 invariant'larını ve git güvenlik kurallarını koru.

## Skill kullanımı

Codex repo kökündeki `.agents/skills/*/SKILL.md` dosyalarını otomatik keşfeder. Skill görünmüyorsa Codex oturumunu yeniden başlat.

Açık çağırma örnekleri:

```text
$lumisky-context-scout bu donmanın ilgili dosyalarını bul
$lumisky-runtime-session sensor event backlog sorununu düzelt
$lumisky-diff-review mevcut diff'i V8 invariant'larına göre incele
```

Her görevde tüm skill'leri çağırma. Kök `AGENTS.md` tablosundan bir ana skill seç; yalnız gerçekten gerekiyorsa bir yardımcı skill ekle.

## Project-local Codex ayarı

`.codex/config.toml`:

- AGENTS instruction bütçesini sınırlı tutar.
- Subagent thread/depth sayısını sınırlar.
- Proje Codex tarafından trusted değilse project-local config uygulanmayabilir.

## Kullanıcı profilleri

`codex-profiles/` içindeki dosyaları kullanıcı Codex dizinine kopyala:

Windows PowerShell:

```powershell
New-Item -ItemType Directory -Force "$HOME\.codex" | Out-Null
Copy-Item .\codex-profiles\lumisky-low-token.config.toml "$HOME\.codex\lumisky-low-token.config.toml"
Copy-Item .\codex-profiles\lumisky-implementation.config.toml "$HOME\.codex\lumisky-implementation.config.toml"
Copy-Item .\codex-profiles\lumisky-deep-review.config.toml "$HOME\.codex\lumisky-deep-review.config.toml"
```

Kullanım:

```powershell
codex --profile lumisky-low-token
codex --profile lumisky-implementation
codex --profile lumisky-deep-review
```

- `low-token`: küçük bug, basit edit, hedefli analiz.
- `implementation`: normal feature/fix ve test.
- `deep-review`: salt-okunur mimari, performans veya güvenlik incelemesi.

## Yardımcı scriptler

```powershell
.\.agents\scripts\context-scout.ps1 -Query "SceneScheduler"
.\.agents\scripts\changed-scope.ps1
.\.agents\scripts\select-tests.ps1
.\.agents\scripts\validate-toolkit.ps1
```

`select-tests.ps1` varsayılan olarak komutları yalnız yazdırır. `-Run` verilmeden test çalıştırmaz.

## Mimari doküman kullanımı

Tam V8 dokümanı `docs/architecture/lumisky_v8_architecture.md` altındadır. Kök AGENTS, Codex'in her görevde bu büyük dosyayı okumasını yasaklar. Önce `.agents/references/` içindeki kısa konu özeti okunmalıdır.
