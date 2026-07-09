package com.adnan.lumisky;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u0000 \u00172\u00020\u0001:\u0001\u0017B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0012\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0014J\b\u0010\u0014\u001a\u00020\u0011H\u0002J\u000e\u0010\u0015\u001a\u00020\u0011H\u0082@\u00a2\u0006\u0002\u0010\u0016R\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\tR\u001e\u0010\n\u001a\u00020\u000b8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000f\u00a8\u0006\u0018"}, d2 = {"Lcom/adnan/lumisky/MainActivity;", "Landroidx/activity/ComponentActivity;", "<init>", "()V", "settingsRepository", "Lcom/adnan/lumisky/data/SettingsRepository;", "getSettingsRepository", "()Lcom/adnan/lumisky/data/SettingsRepository;", "setSettingsRepository", "(Lcom/adnan/lumisky/data/SettingsRepository;)V", "wallpaperRepository", "Lcom/adnan/lumisky/data/WallpaperRepository;", "getWallpaperRepository", "()Lcom/adnan/lumisky/data/WallpaperRepository;", "setWallpaperRepository", "(Lcom/adnan/lumisky/data/WallpaperRepository;)V", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "configureEdgeToEdge", "warmHomeStartupThumbnails", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class MainActivity extends androidx.activity.ComponentActivity {
    @javax.inject.Inject()
    public com.adnan.lumisky.data.SettingsRepository settingsRepository;
    @javax.inject.Inject()
    public com.adnan.lumisky.data.WallpaperRepository wallpaperRepository;
    @java.lang.Deprecated()
    public static final int STARTUP_THUMBNAIL_WARM_LIMIT = 3;
    @org.jetbrains.annotations.NotNull()
    private static final com.adnan.lumisky.MainActivity.Companion Companion = null;
    
    public MainActivity() {
        super(0);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.SettingsRepository getSettingsRepository() {
        return null;
    }
    
    public final void setSettingsRepository(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.SettingsRepository p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.WallpaperRepository getWallpaperRepository() {
        return null;
    }
    
    public final void setWallpaperRepository(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.WallpaperRepository p0) {
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void configureEdgeToEdge() {
    }
    
    private final java.lang.Object warmHomeStartupThumbnails(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/adnan/lumisky/MainActivity$Companion;", "", "<init>", "()V", "STARTUP_THUMBNAIL_WARM_LIMIT", "", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}