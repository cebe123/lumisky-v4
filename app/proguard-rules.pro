# Manifest entry points and the live-wallpaper wrapper are kept explicit for
# release builds. Android Gradle Plugin already keeps manifest classes, but the
# wrapper/base service boundary is important for wallpaper restore/apply flows.
-keep class com.example.lumisky.LumiskyApplication { *; }
-keep class com.example.lumisky.MainActivity { *; }
-keep class com.example.wallpaper.SkyWallpaperService { *; }
-keep class com.example.wallpaper.service.SkyWallpaperService { *; }
-keep class com.example.wallpaper.service.WallpaperRestoreReceiver { *; }
