package com.adnan.lumisky.engine.pipeline;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010%\n\u0002\u0010\u000e\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0016\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u0012\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u0011J\u0006\u0010\u0013\u001a\u00020\rR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\b\u001a\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/engine/pipeline/CachedLayerRenderer;", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "<init>", "(Lcom/adnan/lumisky/engine/gl/GlResourceManager;Lcom/adnan/lumisky/assets/ShaderSourceLoader;)V", "fboCache", "", "", "Lcom/adnan/lumisky/engine/gl/GlFramebuffer;", "refresh", "", "layer", "Lcom/adnan/lumisky/layers/RenderLayer;", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "compositeLastTexture", "clear", "app_release"})
public final class CachedLayerRenderer {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.gl.GlResourceManager gl = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.Map<java.lang.String, com.adnan.lumisky.engine.gl.GlFramebuffer> fboCache = null;
    
    public CachedLayerRenderer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        super();
    }
    
    public final void refresh(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.layers.RenderLayer layer, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
    
    public final void compositeLastTexture(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.layers.RenderLayer layer, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
    
    public final void clear() {
    }
}