package com.adnan.lumisky.layers;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u0018\u0010\u001e\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!2\u0006\u0010\"\u001a\u00020#H&J \u0010$\u001a\u00020\u001f2\u0006\u0010\"\u001a\u00020#2\u0006\u0010%\u001a\u00020\u00072\u0006\u0010&\u001a\u00020\u0007H&J\u0010\u0010\'\u001a\u00020\u001f2\u0006\u0010(\u001a\u00020)H&J\u0010\u0010*\u001a\u00020\u001f2\u0006\u0010+\u001a\u00020,H&J\u0010\u0010-\u001a\u00020\u001f2\u0006\u0010+\u001a\u00020,H&J\u0010\u0010.\u001a\u00020\u001f2\u0006\u0010/\u001a\u000200H&J\u0010\u00101\u001a\u00020\u001f2\u0006\u0010 \u001a\u00020!H&R\u0012\u0010\u0002\u001a\u00020\u0003X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0004\u0010\u0005R\u0012\u0010\u0006\u001a\u00020\u0007X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\tR\u0012\u0010\n\u001a\u00020\u000bX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\rR\u0012\u0010\u000e\u001a\u00020\u000fX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0010\u0010\u0011R\u0012\u0010\u0012\u001a\u00020\u0013X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0014\u0010\u0015R\u0012\u0010\u0016\u001a\u00020\u0017X\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0018\u0010\u0019R\u0012\u0010\u001a\u001a\u00020\u001bX\u00a6\u0004\u00a2\u0006\u0006\u001a\u0004\b\u001c\u0010\u001d\u00a8\u00062\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/layers/RenderLayer;", "", "id", "", "getId", "()Ljava/lang/String;", "zIndex", "", "getZIndex", "()I", "renderPass", "Lcom/adnan/lumisky/engine/pipeline/RenderPass;", "getRenderPass", "()Lcom/adnan/lumisky/engine/pipeline/RenderPass;", "blendMode", "Lcom/adnan/lumisky/engine/pipeline/BlendMode;", "getBlendMode", "()Lcom/adnan/lumisky/engine/pipeline/BlendMode;", "renderTargetMode", "Lcom/adnan/lumisky/engine/pipeline/RenderTargetMode;", "getRenderTargetMode", "()Lcom/adnan/lumisky/engine/pipeline/RenderTargetMode;", "framePolicy", "Lcom/adnan/lumisky/definition/LayerFramePolicyDefinition;", "getFramePolicy", "()Lcom/adnan/lumisky/definition/LayerFramePolicyDefinition;", "parallaxDepth", "", "getParallaxDepth", "()F", "onCreateGl", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "onSurfaceChanged", "width", "height", "onEvent", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "update", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "render", "onQualityChanged", "profile", "Lcom/adnan/lumisky/definition/QualityProfile;", "onDestroyGl", "app_release"})
public abstract interface RenderLayer {
    
    @org.jetbrains.annotations.NotNull()
    public abstract java.lang.String getId();
    
    public abstract int getZIndex();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.pipeline.RenderPass getRenderPass();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.pipeline.BlendMode getBlendMode();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.pipeline.RenderTargetMode getRenderTargetMode();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.definition.LayerFramePolicyDefinition getFramePolicy();
    
    public abstract float getParallaxDepth();
    
    public abstract void onCreateGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context);
    
    public abstract void onSurfaceChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context, int width, int height);
    
    public abstract void onEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event);
    
    public abstract void update(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame);
    
    public abstract void render(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame);
    
    public abstract void onQualityChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityProfile profile);
    
    public abstract void onDestroyGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl);
}