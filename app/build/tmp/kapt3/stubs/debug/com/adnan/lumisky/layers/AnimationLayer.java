package com.adnan.lumisky.layers;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000:\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u0010\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u000fH\u0016J\u0010\u0010\u0010\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016J\u0010\u0010\u0013\u001a\u00020\r2\u0006\u0010\u0011\u001a\u00020\u0012H\u0016R\u000e\u0010\b\u001a\u00020\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/layers/AnimationLayer;", "Lcom/adnan/lumisky/layers/ShaderLayer;", "definition", "Lcom/adnan/lumisky/definition/LayerDefinition;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "<init>", "(Lcom/adnan/lumisky/definition/LayerDefinition;Lcom/adnan/lumisky/assets/ShaderSourceLoader;)V", "animationTime", "", "isPlaying", "", "onEvent", "", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "update", "frame", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "render", "app_debug"})
public final class AnimationLayer extends com.adnan.lumisky.layers.ShaderLayer {
    private float animationTime = 0.0F;
    private boolean isPlaying = false;
    
    public AnimationLayer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.LayerDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        super(null, null);
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
}