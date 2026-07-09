package com.adnan.lumisky.core;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a0\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010!\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0010\u0007\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u001a\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0011\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0004\b\b\u0010\tJ\b\u0010c\u001a\u00020\u0017H\u0014J\u000e\u0010d\u001a\u00020\u00172\u0006\u0010e\u001a\u00020fJ\u0016\u0010g\u001a\u00020\u00172\u0006\u0010h\u001a\u00020A2\u0006\u0010i\u001a\u00020AJ\u0016\u0010j\u001a\u00020\u00172\u0006\u0010k\u001a\u00020l2\u0006\u0010m\u001a\u00020nJ\u0006\u0010o\u001a\u00020\u0017J\u000e\u0010p\u001a\u00020\u00172\u0006\u0010q\u001a\u00020\u001eJ\u0006\u0010r\u001a\u00020\u0017J\u000e\u0010s\u001a\u00020\u00172\u0006\u0010t\u001a\u00020\u001cJ\u0018\u0010u\u001a\u00020\u00172\u0006\u0010v\u001a\u00020b2\u0006\u0010w\u001a\u00020\u001eH\u0002J\u0018\u0010x\u001a\u00020\u00172\u0006\u0010h\u001a\u00020A2\u0006\u0010i\u001a\u00020AH\u0002J\u0010\u0010y\u001a\u00020\u00172\u0006\u0010v\u001a\u00020bH\u0002J\b\u0010z\u001a\u00020\u001eH\u0016J\u0016\u0010{\u001a\u00020\u00172\f\u0010|\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016H\u0002J\u0016\u0010}\u001a\u00020\u00172\f\u0010|\u001a\b\u0012\u0004\u0012\u00020\u00170\u0016H\u0002J\b\u0010~\u001a\u00020\u0017H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u000bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000e\u001a\u0004\u0018\u00010\u000fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00160\u0015X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0019X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001c0\u001bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u001d\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u001a\u0010#\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b#\u0010 \"\u0004\b$\u0010\"R\u001a\u0010%\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010 \"\u0004\b\'\u0010\"R\u001a\u0010(\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b*\u0010+\"\u0004\b,\u0010-R\u001a\u0010.\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b/\u0010+\"\u0004\b0\u0010-R\u001a\u00101\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u0010+\"\u0004\b3\u0010-R\u001a\u00104\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b5\u0010+\"\u0004\b6\u0010-R\u001a\u00107\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b8\u0010 \"\u0004\b9\u0010\"R\u001c\u0010:\u001a\u0004\u0018\u00010;X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b<\u0010=\"\u0004\b>\u0010?R\u001a\u0010@\u001a\u00020AX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bB\u0010C\"\u0004\bD\u0010ER\u001a\u0010F\u001a\u00020)X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bG\u0010+\"\u0004\bH\u0010-R\u001a\u0010I\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bJ\u0010 \"\u0004\bK\u0010\"R\u001a\u0010L\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bM\u0010 \"\u0004\bN\u0010\"R\u001a\u0010O\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bP\u0010 \"\u0004\bQ\u0010\"R\u001a\u0010R\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bS\u0010 \"\u0004\bT\u0010\"R\u001a\u0010U\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bV\u0010 \"\u0004\bW\u0010\"R\u001a\u0010X\u001a\u00020\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bY\u0010 \"\u0004\bZ\u0010\"R\u001c\u0010[\u001a\u0004\u0018\u00010\\X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b]\u0010^\"\u0004\b_\u0010`R\u000e\u0010a\u001a\u00020bX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u007f"}, d2 = {"Lcom/adnan/lumisky/core/WallpaperGlThread;", "Landroid/os/HandlerThread;", "context", "Landroid/content/Context;", "renderer", "Lcom/adnan/lumisky/engine/LumiskyRenderer;", "shaderRegistry", "Lcom/adnan/lumisky/registry/ShaderRegistry;", "<init>", "(Landroid/content/Context;Lcom/adnan/lumisky/engine/LumiskyRenderer;Lcom/adnan/lumisky/registry/ShaderRegistry;)V", "handler", "Landroid/os/Handler;", "eglManager", "Lcom/adnan/lumisky/engine/gl/EglManager;", "glManager", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "renderContext", "Lcom/adnan/lumisky/engine/RenderContext;", "frameClock", "Lcom/adnan/lumisky/core/WallpaperFrameClock;", "pendingActions", "Ljava/util/concurrent/ConcurrentLinkedQueue;", "Lkotlin/Function0;", "", "eventQueue", "Lcom/adnan/lumisky/core/EngineEventQueue;", "drainedEvents", "", "Lcom/adnan/lumisky/core/WallpaperEvent;", "hasSurface", "", "getHasSurface", "()Z", "setHasSurface", "(Z)V", "isVisible", "setVisible", "batterySaver", "getBatterySaver", "setBatterySaver", "parallaxX", "", "getParallaxX", "()F", "setParallaxX", "(F)V", "parallaxY", "getParallaxY", "setParallaxY", "touchX", "getTouchX", "setTouchX", "touchY", "getTouchY", "setTouchY", "hasTouch", "getHasTouch", "setHasTouch", "preferredQualityTier", "Lcom/adnan/lumisky/definition/QualityTier;", "getPreferredQualityTier", "()Lcom/adnan/lumisky/definition/QualityTier;", "setPreferredQualityTier", "(Lcom/adnan/lumisky/definition/QualityTier;)V", "maxFps", "", "getMaxFps", "()I", "setMaxFps", "(I)V", "renderScale", "getRenderScale", "setRenderScale", "postProcessEnabled", "getPostProcessEnabled", "setPostProcessEnabled", "particleEffectsEnabled", "getParticleEffectsEnabled", "setParticleEffectsEnabled", "videoPlaybackEnabled", "getVideoPlaybackEnabled", "setVideoPlaybackEnabled", "sensorParallaxEnabled", "getSensorParallaxEnabled", "setSensorParallaxEnabled", "telemetryEnabled", "getTelemetryEnabled", "setTelemetryEnabled", "thermalEmergency", "getThermalEmergency", "setThermalEmergency", "daylightOverride", "Lcom/adnan/lumisky/engine/DaylightOverride;", "getDaylightOverride", "()Lcom/adnan/lumisky/engine/DaylightOverride;", "setDaylightOverride", "(Lcom/adnan/lumisky/engine/DaylightOverride;)V", "lastRenderedFrameTimeNanos", "", "onLooperPrepared", "onSurfaceCreated", "holder", "Landroid/view/SurfaceHolder;", "onSurfaceChanged", "width", "height", "switchScene", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "newScene", "Lcom/adnan/lumisky/engine/RuntimeScene;", "onSurfaceDestroyed", "setVisibility", "visible", "triggerLiveCatchUp", "postEvent", "event", "renderFrameNow", "frameTimeNanos", "visibleForFrame", "applySurfaceSize", "renderFrameIfDue", "quitSafely", "postToGl", "action", "runOnGlBlocking", "drainEvents", "app_release"})
public final class WallpaperGlThread extends android.os.HandlerThread {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.LumiskyRenderer renderer = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.registry.ShaderRegistry shaderRegistry = null;
    @org.jetbrains.annotations.Nullable()
    private android.os.Handler handler;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.gl.EglManager eglManager;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.engine.gl.GlResourceManager glManager;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.engine.RenderContext renderContext = null;
    @org.jetbrains.annotations.Nullable()
    private com.adnan.lumisky.core.WallpaperFrameClock frameClock;
    @org.jetbrains.annotations.NotNull()
    private final java.util.concurrent.ConcurrentLinkedQueue<kotlin.jvm.functions.Function0<kotlin.Unit>> pendingActions = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.core.EngineEventQueue eventQueue = null;
    @org.jetbrains.annotations.NotNull()
    private final java.util.List<com.adnan.lumisky.core.WallpaperEvent> drainedEvents = null;
    @kotlin.jvm.Volatile()
    private volatile boolean hasSurface = false;
    @kotlin.jvm.Volatile()
    private volatile boolean isVisible = false;
    @kotlin.jvm.Volatile()
    private volatile boolean batterySaver = false;
    @kotlin.jvm.Volatile()
    private volatile float parallaxX = 0.0F;
    @kotlin.jvm.Volatile()
    private volatile float parallaxY = 0.0F;
    @kotlin.jvm.Volatile()
    private volatile float touchX = 0.0F;
    @kotlin.jvm.Volatile()
    private volatile float touchY = 0.0F;
    @kotlin.jvm.Volatile()
    private volatile boolean hasTouch = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.adnan.lumisky.definition.QualityTier preferredQualityTier;
    @kotlin.jvm.Volatile()
    private volatile int maxFps = 30;
    @kotlin.jvm.Volatile()
    private volatile float renderScale = 1.0F;
    @kotlin.jvm.Volatile()
    private volatile boolean postProcessEnabled = true;
    @kotlin.jvm.Volatile()
    private volatile boolean particleEffectsEnabled = true;
    @kotlin.jvm.Volatile()
    private volatile boolean videoPlaybackEnabled = true;
    @kotlin.jvm.Volatile()
    private volatile boolean sensorParallaxEnabled = true;
    @kotlin.jvm.Volatile()
    private volatile boolean telemetryEnabled = true;
    @kotlin.jvm.Volatile()
    private volatile boolean thermalEmergency = false;
    @kotlin.jvm.Volatile()
    @org.jetbrains.annotations.Nullable()
    private volatile com.adnan.lumisky.engine.DaylightOverride daylightOverride;
    private long lastRenderedFrameTimeNanos = 0L;
    
    public WallpaperGlThread(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.LumiskyRenderer renderer, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.registry.ShaderRegistry shaderRegistry) {
        super(null);
    }
    
    public final boolean getHasSurface() {
        return false;
    }
    
    public final void setHasSurface(boolean p0) {
    }
    
    public final boolean isVisible() {
        return false;
    }
    
    public final void setVisible(boolean p0) {
    }
    
    public final boolean getBatterySaver() {
        return false;
    }
    
    public final void setBatterySaver(boolean p0) {
    }
    
    public final float getParallaxX() {
        return 0.0F;
    }
    
    public final void setParallaxX(float p0) {
    }
    
    public final float getParallaxY() {
        return 0.0F;
    }
    
    public final void setParallaxY(float p0) {
    }
    
    public final float getTouchX() {
        return 0.0F;
    }
    
    public final void setTouchX(float p0) {
    }
    
    public final float getTouchY() {
        return 0.0F;
    }
    
    public final void setTouchY(float p0) {
    }
    
    public final boolean getHasTouch() {
        return false;
    }
    
    public final void setHasTouch(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.definition.QualityTier getPreferredQualityTier() {
        return null;
    }
    
    public final void setPreferredQualityTier(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.QualityTier p0) {
    }
    
    public final int getMaxFps() {
        return 0;
    }
    
    public final void setMaxFps(int p0) {
    }
    
    public final float getRenderScale() {
        return 0.0F;
    }
    
    public final void setRenderScale(float p0) {
    }
    
    public final boolean getPostProcessEnabled() {
        return false;
    }
    
    public final void setPostProcessEnabled(boolean p0) {
    }
    
    public final boolean getParticleEffectsEnabled() {
        return false;
    }
    
    public final void setParticleEffectsEnabled(boolean p0) {
    }
    
    public final boolean getVideoPlaybackEnabled() {
        return false;
    }
    
    public final void setVideoPlaybackEnabled(boolean p0) {
    }
    
    public final boolean getSensorParallaxEnabled() {
        return false;
    }
    
    public final void setSensorParallaxEnabled(boolean p0) {
    }
    
    public final boolean getTelemetryEnabled() {
        return false;
    }
    
    public final void setTelemetryEnabled(boolean p0) {
    }
    
    public final boolean getThermalEmergency() {
        return false;
    }
    
    public final void setThermalEmergency(boolean p0) {
    }
    
    @org.jetbrains.annotations.Nullable()
    public final com.adnan.lumisky.engine.DaylightOverride getDaylightOverride() {
        return null;
    }
    
    public final void setDaylightOverride(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.DaylightOverride p0) {
    }
    
    @java.lang.Override()
    protected void onLooperPrepared() {
    }
    
    public final void onSurfaceCreated(@org.jetbrains.annotations.NotNull()
    android.view.SurfaceHolder holder) {
    }
    
    public final void onSurfaceChanged(int width, int height) {
    }
    
    public final void switchScene(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.WallpaperDefinition definition, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RuntimeScene newScene) {
    }
    
    public final void onSurfaceDestroyed() {
    }
    
    public final void setVisibility(boolean visible) {
    }
    
    public final void triggerLiveCatchUp() {
    }
    
    public final void postEvent(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.core.WallpaperEvent event) {
    }
    
    private final void renderFrameNow(long frameTimeNanos, boolean visibleForFrame) {
    }
    
    private final void applySurfaceSize(int width, int height) {
    }
    
    private final void renderFrameIfDue(long frameTimeNanos) {
    }
    
    @java.lang.Override()
    public boolean quitSafely() {
        return false;
    }
    
    private final void postToGl(kotlin.jvm.functions.Function0<kotlin.Unit> action) {
    }
    
    private final void runOnGlBlocking(kotlin.jvm.functions.Function0<kotlin.Unit> action) {
    }
    
    private final void drainEvents() {
    }
}