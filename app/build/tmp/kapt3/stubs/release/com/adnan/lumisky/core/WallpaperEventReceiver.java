package com.adnan.lumisky.core;

@dagger.hilt.android.AndroidEntryPoint()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u00002\u00020\u0001B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u001c\u0010\n\u001a\u00020\u000b2\b\u0010\f\u001a\u0004\u0018\u00010\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000fH\u0016J\u000e\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u0011\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\rR\u001e\u0010\u0004\u001a\u00020\u00058\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0006\u0010\u0007\"\u0004\b\b\u0010\t\u00a8\u0006\u0012"}, d2 = {"Lcom/adnan/lumisky/core/WallpaperEventReceiver;", "Landroid/content/BroadcastReceiver;", "<init>", "()V", "engineController", "Lcom/adnan/lumisky/core/EngineController;", "getEngineController", "()Lcom/adnan/lumisky/core/EngineController;", "setEngineController", "(Lcom/adnan/lumisky/core/EngineController;)V", "onReceive", "", "context", "Landroid/content/Context;", "intent", "Landroid/content/Intent;", "register", "unregister", "app_release"})
public final class WallpaperEventReceiver extends android.content.BroadcastReceiver {
    @javax.inject.Inject()
    public com.adnan.lumisky.core.EngineController engineController;
    
    public WallpaperEventReceiver() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.core.EngineController getEngineController() {
        return null;
    }
    
    public final void setEngineController(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.EngineController p0) {
    }
    
    @java.lang.Override()
    public void onReceive(@org.jetbrains.annotations.Nullable()
    android.content.Context context, @org.jetbrains.annotations.Nullable()
    android.content.Intent intent) {
    }
    
    public final void register(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void unregister(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
}