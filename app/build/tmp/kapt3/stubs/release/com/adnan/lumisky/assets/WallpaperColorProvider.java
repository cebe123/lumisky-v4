package com.adnan.lumisky.assets;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u0011\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0010\u0010\n\u001a\u0004\u0018\u00010\t2\u0006\u0010\u000b\u001a\u00020\fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/adnan/lumisky/assets/WallpaperColorProvider;", "", "thumbnailLoader", "Lcom/adnan/lumisky/assets/ThumbnailLoader;", "<init>", "(Lcom/adnan/lumisky/assets/ThumbnailLoader;)V", "colorsCache", "", "", "Landroid/app/WallpaperColors;", "getColors", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "app_release"})
@android.annotation.TargetApi(value = android.os.Build.VERSION_CODES.O_MR1)
public final class WallpaperColorProvider {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ThumbnailLoader thumbnailLoader = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, android.app.WallpaperColors> colorsCache = null;
    
    @javax.inject.Inject()
    public WallpaperColorProvider(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ThumbnailLoader thumbnailLoader) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final android.app.WallpaperColors getColors(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.WallpaperDefinition definition) {
        return null;
    }
}