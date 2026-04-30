# Manifest entry points and the live-wallpaper wrapper are kept explicit for
# release builds. Android Gradle Plugin already keeps manifest classes, but the
# wrapper/base service boundary is important for wallpaper restore/apply flows.
-keep class com.example.lumisky.LumiskyApplication { *; }
-keep class com.example.lumisky.MainActivity { *; }
-keep class com.example.wallpaper.SkyWallpaperService { *; }
-keep class com.example.wallpaper.service.SkyWallpaperService { *; }
-keep class com.example.wallpaper.service.WallpaperRestoreReceiver { *; }

# WorkManager's Room database implementation is created by reflection in release builds.
-keepclassmembers class androidx.work.impl.WorkDatabase_Impl {
    public <init>();
}
