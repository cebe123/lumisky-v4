package com.adnan.lumisky.layers;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000v\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\b&\u0018\u00002\u00020\u0001B\u000f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0004\u0010\u0005J\u0018\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020)H\u0016J \u0010*\u001a\u00020%2\u0006\u0010(\u001a\u00020)2\u0006\u0010+\u001a\u00020\r2\u0006\u0010,\u001a\u00020\rH\u0016J\u0010\u0010-\u001a\u00020%2\u0006\u0010.\u001a\u00020/H\u0016J\u0010\u00100\u001a\u00020%2\u0006\u00101\u001a\u000202H\u0016J\u0010\u00103\u001a\u00020%2\u0006\u00101\u001a\u000202H\u0016J\u0010\u00104\u001a\u00020%2\u0006\u00105\u001a\u000206H\u0016J\u0010\u00107\u001a\u00020%2\u0006\u0010&\u001a\u00020\'H\u0016R\u0014\u0010\u0002\u001a\u00020\u0003X\u0084\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0006\u0010\u0007R\u0014\u0010\b\u001a\u00020\t8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\n\u0010\u000bR\u0014\u0010\f\u001a\u00020\r8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u000e\u0010\u000fR\u0014\u0010\u0010\u001a\u00020\u00118VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0012\u0010\u0013R\u0014\u0010\u0014\u001a\u00020\u00158VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u0016\u0010\u0017R\u0014\u0010\u0018\u001a\u00020\u00198VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u001a\u0010\u001bR\u0014\u0010\u001c\u001a\u00020\u001d8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\u001e\u0010\u001fR\u0014\u0010 \u001a\u00020!8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b\"\u0010#\u00a8\u00068"}, d2 = {"Lcom/adnan/lumisky/layers/BaseLayer;", "Lcom/adnan/lumisky/layers/RenderLayer;", "definition", "Lcom/adnan/lumisky/definition/LayerDefinition;", "<init>", "(Lcom/adnan/lumisky/definition/LayerDefinition;)V", "getDefinition", "()Lcom/adnan/lumisky/definition/LayerDefinition;", "id", "", "getId", "()Ljava/lang/String;", "zIndex", "", "getZIndex", "()I", "renderPass", "Lcom/adnan/lumisky/engine/pipeline/RenderPass;", "getRenderPass", "()Lcom/adnan/lumisky/engine/pipeline/RenderPass;", "blendMode", "Lcom/adnan/lumisky/engine/pipeline/BlendMode;", "getBlendMode", "()Lcom/adnan/lumisky/engine/pipeline/BlendMode;", "renderTargetMode", "Lcom/adnan/lumisky/engine/pipeline/RenderTargetMode;", "getRenderTargetMode", "()Lcom/adnan/lumisky/engine/pipeline/RenderTargetMode;", "framePolicy", "Lcom/adnan/lumisky/definition/LayerFramePolicyDefinition;", "getFramePolicy", "()Lcom/adnan/lumisky/definition/LayerFramePolicyDefinition;", "parallaxDepth", "", "getParallaxDepth", "()F", "onCreateGl", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "onSurfaceChanged", "width", "height", "onEvent", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "update", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "render", "onQualityChanged", "profile", "Lcom/adnan/lumisky/definition/QualityProfile;", "onDestroyGl", "app_release"})
public abstract class BaseLayer implements com.adnan.lumisky.layers.RenderLayer {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.definition.LayerDefinition definition = null;
    
    public BaseLayer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.LayerDefinition definition) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    protected final com.adnan.lumisky.definition.LayerDefinition getDefinition() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String getId() {
        return null;
    }
    
    @java.lang.Override()
    public int getZIndex() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.adnan.lumisky.engine.pipeline.RenderPass getRenderPass() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.adnan.lumisky.engine.pipeline.BlendMode getBlendMode() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.adnan.lumisky.engine.pipeline.RenderTargetMode getRenderTargetMode() {
        return null;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public com.adnan.lumisky.definition.LayerFramePolicyDefinition getFramePolicy() {
        return null;
    }
    
    @java.lang.Override()
    public float getParallaxDepth() {
        return 0.0F;
    }
    
    @java.lang.Override()
    public void onCreateGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    @java.lang.Override()
    public void onSurfaceChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context, int width, int height) {
    }
    
    @java.lang.Override()
    public void onEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event) {
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
    public void onQualityChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityProfile profile) {
    }
    
    @java.lang.Override()
    public void onDestroyGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl) {
    }
}