# Lumisky UML Diyagramları

Bu dosya, uygulamanın ana Android/Compose, MVVM, canlı duvar kağıdı ve OpenGL ES render akışlarını Mermaid UML ile özetler.

## Modül / Bileşen Diyagramı

```mermaid
flowchart LR
    app[":app\nCompose UI + MVVM"] --> core[":core\nAyarlar, konum, güneş zamanları"]
    app --> engine[":engine\nConfig + OpenGL render"]
    app --> wallpaper[":wallpaper\nWallpaperService lifecycle"]
    wallpaper --> core
    wallpaper --> engine

    MainActivity --> HomeViewModel
    HomeViewModel --> WallpaperCatalog
    HomeViewModel --> AppSettingsRepository
    HomeViewModel --> SunTimesRepository
    MainActivity --> WallpaperConfigStore
    MainActivity --> SkyWallpaperService
    SkyWallpaperService --> WallpaperRenderController
    WallpaperRenderController --> WallpaperRenderEngine
    WallpaperRenderEngine --> SkyEngine
    SkyEngine --> SkyRenderer
```

## Ana Sınıf Diyagramı

```mermaid
classDiagram
    class MainActivity {
      -AppSettingsRepository appSettingsRepository
      -WallpaperConfigStore wallpaperConfigStore
      -SunTimesRepository sunTimesRepository
      -HomeViewModel? homeViewModelState
    }
    class HomeViewModel {
      -WallpaperCatalogRepository wallpaperCatalogRepository
      +configFor(wallpaperId) WallpaperConfig?
    }
    class WallpaperCatalog {
      +buildConfigs(context, daylight) List~WallpaperConfig~
      +configById(context, id, daylight) WallpaperConfig?
    }
    class WallpaperCatalogRepository {
      -List~WallpaperCatalogSource~ sources
      +buildConfigs(daylight) List~WallpaperConfig~
      +configById(id, daylight) WallpaperConfig?
    }
    class WallpaperCatalogSource {
      <<interface>>
      +loadEntries() List~WallpaperCatalogEntry~
    }
    class WallpaperManifestCatalogSource
    class WallpaperCatalogEntry {
      +WallpaperConfig baseConfig
      +resolve(daylight) WallpaperConfig
    }
    class WallpaperConfigStore
    class AppSettingsRepository
    class SunTimesRepository
    class WallpaperConfig

    MainActivity --> HomeViewModel
    MainActivity --> WallpaperConfigStore
    MainActivity --> SunTimesRepository
    HomeViewModel --> WallpaperCatalogRepository
    HomeViewModel --> AppSettingsRepository
    HomeViewModel --> WallpaperConfigStore
    WallpaperCatalog --> WallpaperCatalogRepository
    WallpaperCatalogRepository --> WallpaperCatalogSource
    WallpaperManifestCatalogSource ..|> WallpaperCatalogSource
    WallpaperCatalogRepository --> WallpaperCatalogEntry
    WallpaperCatalogEntry --> WallpaperConfig
```

## Canlı Duvar Kağıdı Render Sınıfları

```mermaid
classDiagram
    class SkyWallpaperService {
      +onCreateEngine() Engine
    }
    class SkyWallpaperEngine {
      -WallpaperRenderController renderController
      -WallpaperDaylightSyncCoordinator? daylightSyncCoordinator
      -TiltParallaxTracker tiltParallaxTracker
      +onCreate(surfaceHolder)
      +onVisibilityChanged(visible)
      +onSurfaceCreated(holder)
      +onSurfaceDestroyed(holder)
      +onDestroy()
    }
    class WallpaperRenderController {
      -WallpaperRenderEngine renderEngine
      -MinuteTickScheduler scheduler
      -SceneStateHasher hasher
      +onCreate()
      +onSurfaceCreated(holder)
      +onVisibilityChanged(value)
      +setConfig(config)
      +onDestroy()
    }
    class WallpaperRenderEngine {
      +init()
      +attachSurface(holder) Boolean
      +setConfig(config)
      +renderFrame()
      +release()
    }
    class SkyEngine
    class SkyRenderer
    class MinuteTickScheduler
    class RenderThreadVsyncLoop
    class ServiceRenderPolicyResolver
    class WallpaperConfigStore

    SkyWallpaperService *-- SkyWallpaperEngine
    SkyWallpaperEngine --> WallpaperConfigStore
    SkyWallpaperEngine --> WallpaperRenderController
    WallpaperRenderController --> WallpaperRenderEngine
    WallpaperRenderController --> MinuteTickScheduler
    WallpaperRenderController --> RenderThreadVsyncLoop
    WallpaperRenderController --> ServiceRenderPolicyResolver
    WallpaperRenderEngine --> SkyEngine
    SkyEngine --> SkyRenderer
```

## Duvar Kağıdı Uygulama Akışı

```mermaid
sequenceDiagram
    participant User as Kullanıcı
    participant UI as MainActivity / Compose
    participant VM as HomeViewModel
    participant Store as WallpaperConfigStore
    participant Android as WallpaperManager
    participant Service as SkyWallpaperService
    participant Controller as WallpaperRenderController
    participant GL as WallpaperRenderEngine/SkyEngine

    User->>UI: Duvar kağıdı seçer
    UI->>VM: configFor(wallpaperId)
    VM-->>UI: WallpaperConfig
    UI->>Store: preview/selected config kaydet
    UI->>Android: canlı duvar kağıdı önizleme/seçim intent'i
    Android->>Service: Engine oluşturur
    Service->>Controller: setConfig(config)
    Service->>Controller: onCreate/onSurfaceCreated/onVisibilityChanged
    Controller->>GL: init + attachSurface + renderFrame
    GL-->>Controller: frame tamamlandı
```
