# AGENTS.md

## Must-follow constraints

- Module source of truth is `settings.gradle.kts` + source tree. `docs/PROJECT_STRUCTURE.md` is stale (`:snapshot` is not a Gradle module).
- Preserve module ownership: `app` (UI/orchestration), `engine` (render), `wallpaper` (service/runtime integration), `core` (shared infra).
- Manifest service entry must remain `com.example.wallpaper.SkyWallpaperService` (wrapper). Real implementation stays in `wallpaper/src/main/java/com/example/wallpaper/service/SkyWallpaperService.kt`.
- Preserve wallpaper apply flow: `WallpaperConfigStore` + broadcast `ACTION_APPLY_STORED_WALLPAPER_CONFIG`.
- New settings storage access must use `AppSettingsRepository` (no new direct `SharedPreferences` usage).
- Use `com.example.core.Logger` for logs; do not add `println`.
- Preserve receiver register/unregister patterns, including API 33+ `registerReceiver(..., Context.RECEIVER_NOT_EXPORTED)` branches.

## Asset pipeline (critical)

- Do not edit generated assets under `app/build/**`.
- Packaged assets come from `app/build/generated/filteredAssets/main`, not directly from `app/src/main/assets`.
- Keep the task chain intact: `preBuild -> prepareFilteredAssets -> convertWallpaperTexturesToWebp`.
- Raster textures (`png/jpg/jpeg`) are converted/filtered during build; `assets/shaders/**` must stay excluded from conversion.
- Do not delete/revert user asset changes under `app/src/main/assets/**` unless explicitly requested.

## Validation before finishing

- Run at least the affected module's validation via `.\gradlew` (PowerShell).
- Unless the user explicitly says not to, deploy and launch the latest app build on the preferred connected phone after each task using `.\gradlew :app:deployDebugToConnectedDevice`; do not wait for the user to remind you again.
- Common commands:
```powershell
.\gradlew :core:testDebugUnitTest
.\gradlew :engine:testDebugUnitTest
.\gradlew :wallpaper:testDebugUnitTest
.\gradlew :app:testDebugUnitTest
.\gradlew :app:assembleDebug
.\gradlew :app:lintDebug
.\gradlew :app:deployDebugToConnectedDevice
```

## Change safety rules

- Make the smallest change in the owning module first; avoid cross-module refactors unless required.
