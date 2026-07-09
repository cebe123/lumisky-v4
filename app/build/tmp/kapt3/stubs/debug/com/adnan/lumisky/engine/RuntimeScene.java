package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000H\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u001d\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\u0004\b\u0007\u0010\bJ\u0016\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0012J\u001e\u0010\u0013\u001a\u00020\u000e2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u0015J\u000e\u0010\u0017\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u0019J\u000e\u0010\u001a\u001a\u00020\u000e2\u0006\u0010\u001b\u001a\u00020\u001cJ\u000e\u0010\u001d\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\nR\u0017\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00060\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\f\u00a8\u0006\u001e"}, d2 = {"Lcom/adnan/lumisky/engine/RuntimeScene;", "", "id", "", "layers", "", "Lcom/adnan/lumisky/layers/RenderLayer;", "<init>", "(Ljava/lang/String;Ljava/util/List;)V", "getId", "()Ljava/lang/String;", "getLayers", "()Ljava/util/List;", "onCreateGl", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "onSurfaceChanged", "width", "", "height", "onEvent", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "update", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "onDestroyGl", "app_debug"})
public final class RuntimeScene {
    @org.jetbrains.annotations.NotNull()
    private final java.lang.String id = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.adnan.lumisky.layers.RenderLayer> layers = null;
    
    public RuntimeScene(@org.jetbrains.annotations.NotNull()
    java.lang.String id, @org.jetbrains.annotations.NotNull()
    java.util.List<? extends com.adnan.lumisky.layers.RenderLayer> layers) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String getId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.adnan.lumisky.layers.RenderLayer> getLayers() {
        return null;
    }
    
    public final void onCreateGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    public final void onSurfaceChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context, int width, int height) {
    }
    
    public final void onEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event) {
    }
    
    public final void update(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
    
    public final void onDestroyGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl) {
    }
}