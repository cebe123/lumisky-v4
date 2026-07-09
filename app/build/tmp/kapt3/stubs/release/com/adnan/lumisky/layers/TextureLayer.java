package com.adnan.lumisky.layers;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u0016\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0018\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u0010\u001a\u00020\u000b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Lcom/adnan/lumisky/layers/TextureLayer;", "Lcom/adnan/lumisky/layers/BaseLayer;", "definition", "Lcom/adnan/lumisky/definition/LayerDefinition;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "<init>", "(Lcom/adnan/lumisky/definition/LayerDefinition;Lcom/adnan/lumisky/assets/ShaderSourceLoader;)V", "program", "Lcom/adnan/lumisky/engine/gl/GlProgram;", "onCreateGl", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "render", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "app_release"})
public class TextureLayer extends com.adnan.lumisky.layers.BaseLayer {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader = null;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.gl.GlProgram program;
    
    public TextureLayer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.LayerDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        super(null);
    }
    
    @java.lang.Override()
    public void onCreateGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    @java.lang.Override()
    public void render(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.MutableRenderFrameState frame) {
    }
}