package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u0080\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u0018\u00002\u00020\u0001B9\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u00a2\u0006\u0004\b\u000e\u0010\u000fJ\u0016\u0010\"\u001a\u00020#2\u0006\u0010$\u001a\u00020%2\u0006\u0010&\u001a\u00020\'J\u001e\u0010(\u001a\u00020#2\u0006\u0010&\u001a\u00020\'2\u0006\u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020*J\u0016\u0010,\u001a\u00020#2\u0006\u0010-\u001a\u00020\u00132\u0006\u0010&\u001a\u00020\'J\u001e\u0010.\u001a\u00020#2\u0006\u0010/\u001a\u00020\u00192\u0006\u0010-\u001a\u00020\u00132\u0006\u0010&\u001a\u00020\'J\u000e\u00100\u001a\u00020#2\u0006\u00101\u001a\u000202J\u0016\u00103\u001a\u00020#2\u0006\u0010&\u001a\u00020\'2\u0006\u00104\u001a\u000205J\u0006\u00106\u001a\u00020#J\u0006\u00107\u001a\u00020#J\u0010\u00108\u001a\u00020#2\b\u00109\u001a\u0004\u0018\u00010:R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R(\u0010\u0014\u001a\u0004\u0018\u00010\u00132\b\u0010\u0012\u001a\u0004\u0018\u00010\u00138F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u0015\u0010\u0016\"\u0004\b\u0017\u0010\u0018R(\u0010\u001a\u001a\u0004\u0018\u00010\u00192\b\u0010\u0012\u001a\u0004\u0018\u00010\u00198F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\b\u001b\u0010\u001c\"\u0004\b\u001d\u0010\u001eR\u0011\u0010\u001f\u001a\u00020 8F\u00a2\u0006\u0006\u001a\u0004\b\u001f\u0010!\u00a8\u0006;"}, d2 = {"Lcom/adnan/lumisky/engine/LumiskyRenderer;", "", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "scheduler", "Lcom/adnan/lumisky/engine/SceneScheduler;", "eventTriggerSystem", "Lcom/adnan/lumisky/engine/EventTriggerSystem;", "atmosphereController", "Lcom/adnan/lumisky/engine/AtmosphereController;", "parallaxController", "Lcom/adnan/lumisky/engine/ParallaxController;", "qualityController", "Lcom/adnan/lumisky/engine/AdaptiveQualityController;", "<init>", "(Lcom/adnan/lumisky/assets/ShaderSourceLoader;Lcom/adnan/lumisky/engine/SceneScheduler;Lcom/adnan/lumisky/engine/EventTriggerSystem;Lcom/adnan/lumisky/engine/AtmosphereController;Lcom/adnan/lumisky/engine/ParallaxController;Lcom/adnan/lumisky/engine/AdaptiveQualityController;)V", "session", "Lcom/adnan/lumisky/engine/RenderEngineSession;", "value", "Lcom/adnan/lumisky/engine/RuntimeScene;", "activeScene", "getActiveScene", "()Lcom/adnan/lumisky/engine/RuntimeScene;", "setActiveScene", "(Lcom/adnan/lumisky/engine/RuntimeScene;)V", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "activeDefinition", "getActiveDefinition", "()Lcom/adnan/lumisky/definition/WallpaperDefinition;", "setActiveDefinition", "(Lcom/adnan/lumisky/definition/WallpaperDefinition;)V", "isContextCreated", "", "()Z", "onContextCreated", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "onSurfaceChanged", "width", "", "height", "switchScene", "newScene", "switchWallpaper", "definition", "onEvent", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "renderFrame", "inputSnapshot", "Lcom/adnan/lumisky/engine/SceneInputSnapshot;", "onContextLost", "triggerPreviewAnimation", "triggerLiveCatchUp", "daylightOverride", "Lcom/adnan/lumisky/engine/DaylightOverride;", "app_release"})
public final class LumiskyRenderer {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.SceneScheduler scheduler = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.EventTriggerSystem eventTriggerSystem = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.AtmosphereController atmosphereController = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.ParallaxController parallaxController = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.AdaptiveQualityController qualityController = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.RenderEngineSession session = null;
    
    @javax.inject.Inject()
    public LumiskyRenderer(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.SceneScheduler scheduler, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.EventTriggerSystem eventTriggerSystem, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.AtmosphereController atmosphereController, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.ParallaxController parallaxController, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.AdaptiveQualityController qualityController) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.engine.RuntimeScene getActiveScene() {
        return null;
    }
    
    public final void setActiveScene(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.RuntimeScene value) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.definition.WallpaperDefinition getActiveDefinition() {
        return null;
    }
    
    public final void setActiveDefinition(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.WallpaperDefinition value) {
    }
    
    public final boolean isContextCreated() {
        return false;
    }
    
    public final void onContextCreated(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager gl, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    public final void onSurfaceChanged(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context, int width, int height) {
    }
    
    public final void switchScene(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RuntimeScene newScene, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    public final void switchWallpaper(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.WallpaperDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RuntimeScene newScene, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context) {
    }
    
    public final void onEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event) {
    }
    
    public final void renderFrame(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderContext context, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.SceneInputSnapshot inputSnapshot) {
    }
    
    public final void onContextLost() {
    }
    
    public final void triggerPreviewAnimation() {
    }
    
    public final void triggerLiveCatchUp(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.DaylightOverride daylightOverride) {
    }
}