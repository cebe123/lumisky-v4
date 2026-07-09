package com.adnan.lumisky.layers;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0014\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0018\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u001c\u001a\u00020\u001d2\u0006\u0010\u001e\u001a\u00020\u001fH\u0016J\u0010\u0010 \u001a\u00020\u00162\u0006\u0010!\u001a\u00020\"H\u0016J\u0010\u0010#\u001a\u00020\u00162\u0006\u0010!\u001a\u00020\"H\u0016J\u0010\u0010$\u001a\u00020\u00162\u0006\u0010\u001c\u001a\u00020\u001dH\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R(\u0010\u0014\u001a\u0010\u0012\u0004\u0012\u00020\u000f\u0012\u0004\u0012\u00020\u0016\u0018\u00010\u0015X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0017\u0010\u0018\"\u0004\b\u0019\u0010\u001a\u00a8\u0006%"}, d2 = {"Lcom/adnan/lumisky/layers/VideoOesLayer;", "Lcom/adnan/lumisky/layers/BaseLayer;", "definition", "Lcom/adnan/lumisky/definition/LayerDefinition;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "<init>", "(Lcom/adnan/lumisky/definition/LayerDefinition;Lcom/adnan/lumisky/assets/ShaderSourceLoader;)V", "program", "Lcom/adnan/lumisky/engine/gl/GlProgram;", "oesTexId", "", "surfaceTexture", "Landroid/graphics/SurfaceTexture;", "surface", "Landroid/view/Surface;", "transformMatrix", "", "frameAvailable", "Ljava/util/concurrent/atomic/AtomicBoolean;", "onSurfaceReady", "Lkotlin/Function1;", "", "getOnSurfaceReady", "()Lkotlin/jvm/functions/Function1;", "setOnSurfaceReady", "(Lkotlin/jvm/functions/Function1;)V", "onCreateGl", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "update", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "render", "onDestroyGl", "app_debug"})
public final class VideoOesLayer extends com.adnan.lumisky.layers.BaseLayer {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader = null;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.gl.GlProgram program;
    private int oesTexId = 0;
    @org.jetbrains.annotations.Nullable()
    private android.graphics.SurfaceTexture surfaceTexture;
    @org.jetbrains.annotations.Nullable()
    private android.view.Surface surface;
    @org.jetbrains.annotations.NotNull()
    private final float[] transformMatrix = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.atomic.AtomicBoolean frameAvailable = null;
    @org.jetbrains.annotations.Nullable()
    private kotlin.jvm.functions.Function1<? super android.view.Surface, kotlin.Unit> onSurfaceReady;
    
    public VideoOesLayer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.LayerDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        super(null);
    }
    
    @org.jetbrains.annotations.Nullable()
    public final kotlin.jvm.functions.Function1<android.view.Surface, kotlin.Unit> getOnSurfaceReady() {
        return null;
    }
    
    public final void setOnSurfaceReady(@org.jetbrains.annotations.Nullable()
    kotlin.jvm.functions.Function1<? super android.view.Surface, kotlin.Unit> p0) {
    }
    
    @java.lang.Override()
    public void onCreateGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    @java.lang.Override()
    public void update(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
    
    @java.lang.Override()
    public void render(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
    
    @java.lang.Override()
    public void onDestroyGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl) {
    }
}