package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u00ba\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\u0018\u00002\u00020\u0001B?\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u0012\u0006\u0010\b\u001a\u00020\t\u0012\u0006\u0010\n\u001a\u00020\u000b\u0012\u0006\u0010\f\u001a\u00020\r\u0012\u0006\u0010\u000e\u001a\u00020\u000f\u00a2\u0006\u0004\b\u0010\u0010\u0011J\u0016\u00104\u001a\u0002052\u0006\u00106\u001a\u0002072\u0006\u00108\u001a\u000209J\u001e\u0010:\u001a\u0002052\u0006\u00108\u001a\u0002092\u0006\u0010;\u001a\u00020<2\u0006\u0010=\u001a\u00020<J\u0016\u0010>\u001a\u0002052\u0006\u0010?\u001a\u00020%2\u0006\u00108\u001a\u000209J\u000e\u0010@\u001a\u0002052\u0006\u0010A\u001a\u00020BJ\u0016\u0010C\u001a\u0002052\u0006\u00108\u001a\u0002092\u0006\u0010D\u001a\u00020EJ\u0006\u0010F\u001a\u000205J\u0006\u0010G\u001a\u000205J\u0010\u0010H\u001a\u0002052\b\u0010I\u001a\u0004\u0018\u00010JJ\u0010\u0010K\u001a\u00020L2\u0006\u0010*\u001a\u00020MH\u0002J\u0012\u0010N\u001a\u00020M2\b\u0010I\u001a\u0004\u0018\u00010JH\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0014\u001a\u00020\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u001c\u001a\u00020\u001d\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001fR\u0011\u0010 \u001a\u00020!\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010#R\u001c\u0010$\u001a\u0004\u0018\u00010%X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\'\"\u0004\b(\u0010)R(\u0010,\u001a\u0004\u0018\u00010+2\b\u0010*\u001a\u0004\u0018\u00010+@FX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b-\u0010.\"\u0004\b/\u00100R\u001e\u00102\u001a\u0002012\u0006\u0010*\u001a\u000201@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b2\u00103\u00a8\u0006O"}, d2 = {"Lcom/adnan/lumisky/engine/RenderEngineSession;", "", "runtimeProfile", "Lcom/adnan/lumisky/engine/RuntimeProfile;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "scheduler", "Lcom/adnan/lumisky/engine/SceneScheduler;", "eventTriggerSystem", "Lcom/adnan/lumisky/engine/EventTriggerSystem;", "atmosphereController", "Lcom/adnan/lumisky/engine/AtmosphereController;", "parallaxController", "Lcom/adnan/lumisky/engine/ParallaxController;", "qualityController", "Lcom/adnan/lumisky/engine/AdaptiveQualityController;", "<init>", "(Lcom/adnan/lumisky/engine/RuntimeProfile;Lcom/adnan/lumisky/assets/ShaderSourceLoader;Lcom/adnan/lumisky/engine/SceneScheduler;Lcom/adnan/lumisky/engine/EventTriggerSystem;Lcom/adnan/lumisky/engine/AtmosphereController;Lcom/adnan/lumisky/engine/ParallaxController;Lcom/adnan/lumisky/engine/AdaptiveQualityController;)V", "finalCompositeRenderer", "Lcom/adnan/lumisky/engine/pipeline/FinalCompositeRenderer;", "celestialMotionController", "Lcom/adnan/lumisky/engine/CelestialMotionController;", "previewTimeMotionController", "Lcom/adnan/lumisky/engine/PreviewTimeMotionController;", "liveWallpaperCatchUpController", "Lcom/adnan/lumisky/engine/LiveWallpaperCatchUpController;", "cachedLayerRenderer", "Lcom/adnan/lumisky/engine/pipeline/CachedLayerRenderer;", "frameState", "Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "getFrameState", "()Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "sceneState", "Lcom/adnan/lumisky/engine/SceneState;", "getSceneState", "()Lcom/adnan/lumisky/engine/SceneState;", "activeScene", "Lcom/adnan/lumisky/engine/RuntimeScene;", "getActiveScene", "()Lcom/adnan/lumisky/engine/RuntimeScene;", "setActiveScene", "(Lcom/adnan/lumisky/engine/RuntimeScene;)V", "value", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "activeDefinition", "getActiveDefinition", "()Lcom/adnan/lumisky/definition/WallpaperDefinition;", "setActiveDefinition", "(Lcom/adnan/lumisky/definition/WallpaperDefinition;)V", "", "isContextCreated", "()Z", "onContextCreated", "", "gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "context", "Lcom/adnan/lumisky/engine/RenderContext;", "onSurfaceChanged", "width", "", "height", "switchScene", "newScene", "onEvent", "event", "Lcom/adnan/lumisky/core/WallpaperEvent;", "renderFrame", "inputSnapshot", "Lcom/adnan/lumisky/engine/SceneInputSnapshot;", "onContextLost", "triggerPreviewAnimation", "triggerLiveCatchUp", "daylightOverride", "Lcom/adnan/lumisky/engine/DaylightOverride;", "layerCacheMode", "Lcom/adnan/lumisky/layers/LayerCacheMode;", "", "resolveSceneTimeZoneId", "app_release"})
public final class RenderEngineSession {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.RuntimeProfile runtimeProfile = null;
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
    private final com.adnan.lumisky.engine.pipeline.FinalCompositeRenderer finalCompositeRenderer = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.CelestialMotionController celestialMotionController = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.PreviewTimeMotionController previewTimeMotionController = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.LiveWallpaperCatchUpController liveWallpaperCatchUpController = null;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.pipeline.CachedLayerRenderer cachedLayerRenderer;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.MutableRenderFrameState frameState = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.SceneState sceneState = null;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.RuntimeScene activeScene;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.definition.WallpaperDefinition activeDefinition;
    private boolean isContextCreated = false;
    
    public RenderEngineSession(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RuntimeProfile runtimeProfile, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.SceneScheduler scheduler, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.EventTriggerSystem eventTriggerSystem, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.AtmosphereController atmosphereController, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.ParallaxController parallaxController, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.AdaptiveQualityController qualityController) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.MutableRenderFrameState getFrameState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.SceneState getSceneState() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.engine.RuntimeScene getActiveScene() {
        return null;
    }
    
    public final void setActiveScene(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.RuntimeScene p0) {
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
    
    private final com.adnan.lumisky.layers.LayerCacheMode layerCacheMode(java.lang.String value) {
        return null;
    }
    
    private final java.lang.String resolveSceneTimeZoneId(com.adnan.lumisky.engine.DaylightOverride daylightOverride) {
        return null;
    }
}