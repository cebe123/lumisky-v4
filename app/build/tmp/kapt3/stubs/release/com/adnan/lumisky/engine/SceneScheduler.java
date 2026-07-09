package com.adnan.lumisky.engine;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J*\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00072\b\b\u0002\u0010\r\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\tJ*\u0010\u000f\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u00072\b\b\u0002\u0010\r\u001a\u00020\t2\b\b\u0002\u0010\u000e\u001a\u00020\tR\u001a\u0010\u0004\u001a\u000e\u0012\u0004\u0012\u00020\u0006\u0012\u0004\u0012\u00020\u00070\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0010"}, d2 = {"Lcom/adnan/lumisky/engine/SceneScheduler;", "", "<init>", "()V", "lastUpdateTimes", "", "", "", "shouldUpdate", "", "layer", "Lcom/adnan/lumisky/layers/RenderLayer;", "frameTimeNanos", "batterySaver", "idle", "shouldRefreshCache", "app_release"})
public final class SceneScheduler {
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, java.lang.Long> lastUpdateTimes = null;
    
    @javax.inject.Inject()
    public SceneScheduler() {
        super();
    }
    
    public final boolean shouldUpdate(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.layers.RenderLayer layer, long frameTimeNanos, boolean batterySaver, boolean idle) {
        return false;
    }
    
    public final boolean shouldRefreshCache(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.layers.RenderLayer layer, long frameTimeNanos, boolean batterySaver, boolean idle) {
        return false;
    }
}