package com.adnan.lumisky.core;

@javax.inject.Singleton()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0002\b\u0007\u0018\u00002\u00020\u0001B\t\b\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\r\u001a\u00020\u0005J\u000e\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\bR\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0006\u001a\b\u0012\u0004\u0012\u00020\b0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\b0\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u0011"}, d2 = {"Lcom/adnan/lumisky/core/EngineController;", "", "<init>", "()V", "eventQueue", "Lcom/adnan/lumisky/core/EngineEventQueue;", "_engineEvents", "Lkotlinx/coroutines/flow/MutableSharedFlow;", "Lcom/adnan/lumisky/core/WallpaperEvent;", "engineEvents", "Lkotlinx/coroutines/flow/SharedFlow;", "getEngineEvents", "()Lkotlinx/coroutines/flow/SharedFlow;", "getQueue", "postEvent", "", "event", "app_release"})
public final class EngineController {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.core.EngineEventQueue eventQueue = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableSharedFlow<com.adnan.lumisky.core.WallpaperEvent> _engineEvents = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.SharedFlow<com.adnan.lumisky.core.WallpaperEvent> engineEvents = null;
    
    @javax.inject.Inject()
    public EngineController() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.SharedFlow<com.adnan.lumisky.core.WallpaperEvent> getEngineEvents() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.core.EngineEventQueue getQueue() {
        return null;
    }
    
    public final void postEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event) {
    }
}