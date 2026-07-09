package com.adnan.lumisky.core;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001:\u0001\u0006B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\f\u0010\u0004\u001a\u00060\u0005R\u00020\u0001H\u0016\u00a8\u0006\u0007"}, d2 = {"Lcom/adnan/lumisky/core/LumiskyWallpaperService;", "Landroid/service/wallpaper/WallpaperService;", "<init>", "()V", "onCreateEngine", "Landroid/service/wallpaper/WallpaperService$Engine;", "EngineAdapter", "app_release"})
public final class LumiskyWallpaperService extends android.service.wallpaper.WallpaperService {
    
    public LumiskyWallpaperService() {
        super();
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public android.service.wallpaper.WallpaperService.Engine onCreateEngine() {
        return null;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0082\u0004\u0018\u00002\u00060\u0001R\u00020\u0002B\u000f\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\u0004\b\u0005\u0010\u0006J\u0012\u0010\t\u001a\u00020\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0016J\u0010\u0010\r\u001a\u00020\n2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u0010\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\fH\u0016J(\u0010\u0012\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\f2\u0006\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\u00142\u0006\u0010\u0016\u001a\u00020\u0014H\u0016J\u0010\u0010\u0017\u001a\u00020\n2\u0006\u0010\u0011\u001a\u00020\fH\u0016J\u0010\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u001aH\u0016J8\u0010\u001b\u001a\u00020\n2\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001d2\u0006\u0010\u001f\u001a\u00020\u001d2\u0006\u0010 \u001a\u00020\u001d2\u0006\u0010!\u001a\u00020\u00142\u0006\u0010\"\u001a\u00020\u0014H\u0016J\n\u0010#\u001a\u0004\u0018\u00010$H\u0017J\b\u0010%\u001a\u00020\nH\u0016J\b\u0010&\u001a\u00020\nH\u0003R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/adnan/lumisky/core/LumiskyWallpaperService$EngineAdapter;", "Landroid/service/wallpaper/WallpaperService$Engine;", "Landroid/service/wallpaper/WallpaperService;", "entryPoint", "Lcom/adnan/lumisky/core/EngineEntryPoint;", "<init>", "(Lcom/adnan/lumisky/core/LumiskyWallpaperService;Lcom/adnan/lumisky/core/EngineEntryPoint;)V", "delegate", "Lcom/adnan/lumisky/core/LumiskyWallpaperEngine;", "onCreate", "", "surfaceHolder", "Landroid/view/SurfaceHolder;", "onVisibilityChanged", "visible", "", "onSurfaceCreated", "holder", "onSurfaceChanged", "format", "", "width", "height", "onSurfaceDestroyed", "onTouchEvent", "event", "Landroid/view/MotionEvent;", "onOffsetsChanged", "xOffset", "", "yOffset", "xOffsetStep", "yOffsetStep", "xPixelOffset", "yPixelOffset", "onComputeColors", "Landroid/app/WallpaperColors;", "onDestroy", "notifyColorsChangedIfAvailable", "app_release"})
    final class EngineAdapter extends android.service.wallpaper.WallpaperService.Engine {
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.core.LumiskyWallpaperEngine delegate = null;
        
        public EngineAdapter(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.core.EngineEntryPoint entryPoint) {
            super();
        }
        
        @java.lang.Override()
        public void onCreate(@org.jetbrains.annotations.Nullable()
        android.view.SurfaceHolder surfaceHolder) {
        }
        
        @java.lang.Override()
        public void onVisibilityChanged(boolean visible) {
        }
        
        @java.lang.Override()
        public void onSurfaceCreated(@org.jetbrains.annotations.NotNull()
        android.view.SurfaceHolder holder) {
        }
        
        @java.lang.Override()
        public void onSurfaceChanged(@org.jetbrains.annotations.NotNull()
        android.view.SurfaceHolder holder, int format, int width, int height) {
        }
        
        @java.lang.Override()
        public void onSurfaceDestroyed(@org.jetbrains.annotations.NotNull()
        android.view.SurfaceHolder holder) {
        }
        
        @java.lang.Override()
        public void onTouchEvent(@org.jetbrains.annotations.NotNull()
        android.view.MotionEvent event) {
        }
        
        @java.lang.Override()
        public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {
        }
        
        @java.lang.Override()
        @android.annotation.TargetApi(value = android.os.Build.VERSION_CODES.O_MR1)
        @org.jetbrains.annotations.Nullable()
        public android.app.WallpaperColors onComputeColors() {
            return null;
        }
        
        @java.lang.Override()
        public void onDestroy() {
        }
        
        @android.annotation.TargetApi(value = android.os.Build.VERSION_CODES.O_MR1)
        private final void notifyColorsChangedIfAvailable() {
        }
    }
}