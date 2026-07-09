package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\bX\n\u0002\u0018\u0002\n\u0002\b)\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B\u00b3\u0002\u0012\b\b\u0002\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0007\u001a\u00020\u0006\u0012\b\b\u0002\u0010\b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\t\u001a\u00020\u0006\u0012\b\b\u0002\u0010\n\u001a\u00020\u000b\u0012\b\b\u0002\u0010\f\u001a\u00020\u0006\u0012\b\b\u0002\u0010\r\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000e\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u000f\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0010\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0011\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0013\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u0014\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0015\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0016\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0017\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0018\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u0019\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u001a\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u001b\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u001c\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u001d\u001a\u00020\u0006\u0012\b\b\u0002\u0010\u001e\u001a\u00020\u0012\u0012\b\b\u0002\u0010\u001f\u001a\u00020\u0012\u0012\b\b\u0002\u0010 \u001a\u00020\u0012\u0012\b\b\u0002\u0010!\u001a\u00020\u0012\u0012\b\b\u0002\u0010\"\u001a\u00020\u0012\u0012\b\b\u0002\u0010#\u001a\u00020\u0012\u00a2\u0006\u0004\b$\u0010%J\u0006\u0010i\u001a\u00020\u0012J\t\u0010r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010s\u001a\u00020\u0003H\u00c6\u0003J\t\u0010t\u001a\u00020\u0006H\u00c6\u0003J\t\u0010u\u001a\u00020\u0006H\u00c6\u0003J\t\u0010v\u001a\u00020\u0006H\u00c6\u0003J\t\u0010w\u001a\u00020\u0006H\u00c6\u0003J\t\u0010x\u001a\u00020\u000bH\u00c6\u0003J\t\u0010y\u001a\u00020\u0006H\u00c6\u0003J\t\u0010z\u001a\u00020\u0006H\u00c6\u0003J\t\u0010{\u001a\u00020\u0006H\u00c6\u0003J\t\u0010|\u001a\u00020\u0006H\u00c6\u0003J\t\u0010}\u001a\u00020\u0006H\u00c6\u0003J\t\u0010~\u001a\u00020\u0012H\u00c6\u0003J\t\u0010\u007f\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u0080\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0081\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0082\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0083\u0001\u001a\u00020\u0003H\u00c6\u0003J\n\u0010\u0084\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0085\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0086\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0087\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0088\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u0089\u0001\u001a\u00020\u0006H\u00c6\u0003J\n\u0010\u008a\u0001\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u008b\u0001\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u008c\u0001\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u008d\u0001\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u008e\u0001\u001a\u00020\u0012H\u00c6\u0003J\n\u0010\u008f\u0001\u001a\u00020\u0012H\u00c6\u0003J\u00b6\u0002\u0010\u0090\u0001\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00062\b\b\u0002\u0010\u0007\u001a\u00020\u00062\b\b\u0002\u0010\b\u001a\u00020\u00062\b\b\u0002\u0010\t\u001a\u00020\u00062\b\b\u0002\u0010\n\u001a\u00020\u000b2\b\b\u0002\u0010\f\u001a\u00020\u00062\b\b\u0002\u0010\r\u001a\u00020\u00062\b\b\u0002\u0010\u000e\u001a\u00020\u00062\b\b\u0002\u0010\u000f\u001a\u00020\u00062\b\b\u0002\u0010\u0010\u001a\u00020\u00062\b\b\u0002\u0010\u0011\u001a\u00020\u00122\b\b\u0002\u0010\u0013\u001a\u00020\u00122\b\b\u0002\u0010\u0014\u001a\u00020\u00062\b\b\u0002\u0010\u0015\u001a\u00020\u00032\b\b\u0002\u0010\u0016\u001a\u00020\u00032\b\b\u0002\u0010\u0017\u001a\u00020\u00032\b\b\u0002\u0010\u0018\u001a\u00020\u00062\b\b\u0002\u0010\u0019\u001a\u00020\u00062\b\b\u0002\u0010\u001a\u001a\u00020\u00062\b\b\u0002\u0010\u001b\u001a\u00020\u00062\b\b\u0002\u0010\u001c\u001a\u00020\u00062\b\b\u0002\u0010\u001d\u001a\u00020\u00062\b\b\u0002\u0010\u001e\u001a\u00020\u00122\b\b\u0002\u0010\u001f\u001a\u00020\u00122\b\b\u0002\u0010 \u001a\u00020\u00122\b\b\u0002\u0010!\u001a\u00020\u00122\b\b\u0002\u0010\"\u001a\u00020\u00122\b\b\u0002\u0010#\u001a\u00020\u0012H\u00c6\u0001J\u0015\u0010\u0091\u0001\u001a\u00020\u00122\t\u0010\u0092\u0001\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\n\u0010\u0093\u0001\u001a\u00020\u0003H\u00d6\u0001J\u000b\u0010\u0094\u0001\u001a\u00030\u0095\u0001H\u00d6\u0001R\u001a\u0010\u0002\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b&\u0010\'\"\u0004\b(\u0010)R\u001a\u0010\u0004\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b*\u0010\'\"\u0004\b+\u0010)R\u001a\u0010\u0005\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b,\u0010-\"\u0004\b.\u0010/R\u001a\u0010\u0007\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b0\u0010-\"\u0004\b1\u0010/R\u001a\u0010\b\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b2\u0010-\"\u0004\b3\u0010/R\u001a\u0010\t\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b4\u0010-\"\u0004\b5\u0010/R\u001a\u0010\n\u001a\u00020\u000bX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b6\u00107\"\u0004\b8\u00109R\u001a\u0010\f\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b:\u0010-\"\u0004\b;\u0010/R\u001a\u0010\r\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b<\u0010-\"\u0004\b=\u0010/R\u001a\u0010\u000e\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b>\u0010-\"\u0004\b?\u0010/R\u001a\u0010\u000f\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b@\u0010-\"\u0004\bA\u0010/R\u001a\u0010\u0010\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bB\u0010-\"\u0004\bC\u0010/R\u001a\u0010\u0011\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bD\u0010E\"\u0004\bF\u0010GR\u001a\u0010\u0013\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0013\u0010E\"\u0004\bH\u0010GR\u001a\u0010\u0014\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bI\u0010-\"\u0004\bJ\u0010/R\u001a\u0010\u0015\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bK\u0010\'\"\u0004\bL\u0010)R\u001a\u0010\u0016\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bM\u0010\'\"\u0004\bN\u0010)R\u001a\u0010\u0017\u001a\u00020\u0003X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bO\u0010\'\"\u0004\bP\u0010)R\u001a\u0010\u0018\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bQ\u0010-\"\u0004\bR\u0010/R\u001a\u0010\u0019\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bS\u0010-\"\u0004\bT\u0010/R\u001a\u0010\u001a\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bU\u0010-\"\u0004\bV\u0010/R\u001a\u0010\u001b\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bW\u0010-\"\u0004\bX\u0010/R\u001a\u0010\u001c\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bY\u0010-\"\u0004\bZ\u0010/R\u001a\u0010\u001d\u001a\u00020\u0006X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b[\u0010-\"\u0004\b\\\u0010/R\u001a\u0010\u001e\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b]\u0010E\"\u0004\b^\u0010GR\u001a\u0010\u001f\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b_\u0010E\"\u0004\b`\u0010GR\u001a\u0010 \u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\ba\u0010E\"\u0004\bb\u0010GR\u001a\u0010!\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bc\u0010E\"\u0004\bd\u0010GR\u001a\u0010\"\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\be\u0010E\"\u0004\bf\u0010GR\u001a\u0010#\u001a\u00020\u0012X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\bg\u0010E\"\u0004\bh\u0010GR\u000e\u0010i\u001a\u00020\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010j\u001a\u00020kX\u0082.\u00a2\u0006\u0002\n\u0000R$\u0010m\u001a\u00020k2\u0006\u0010l\u001a\u00020k8F@FX\u0086\u000e\u00a2\u0006\f\u001a\u0004\bn\u0010o\"\u0004\bp\u0010q\u00a8\u0006\u0096\u0001"}, d2 = {"Lcom/adnan/lumisky/engine/MutableRenderFrameState;", "", "width", "", "height", "timeSeconds", "", "deltaTimeSeconds", "parallaxOffsetX", "parallaxOffsetY", "quality", "Lcom/adnan/lumisky/definition/QualityTier;", "dayProgress", "sunX", "sunY", "moonX", "moonY", "drawSun", "", "isNight", "minute", "sunriseMinute", "sunsetMinute", "solarNoonMinute", "nightAmount", "horizonY", "sunColorR", "sunColorG", "sunColorB", "renderScale", "postProcessEnabled", "particleEffectsEnabled", "videoPlaybackEnabled", "sensorParallaxEnabled", "telemetryEnabled", "thermalEmergency", "<init>", "(IIFFFFLcom/adnan/lumisky/definition/QualityTier;FFFFFZZFIIIFFFFFFZZZZZZ)V", "getWidth", "()I", "setWidth", "(I)V", "getHeight", "setHeight", "getTimeSeconds", "()F", "setTimeSeconds", "(F)V", "getDeltaTimeSeconds", "setDeltaTimeSeconds", "getParallaxOffsetX", "setParallaxOffsetX", "getParallaxOffsetY", "setParallaxOffsetY", "getQuality", "()Lcom/adnan/lumisky/definition/QualityTier;", "setQuality", "(Lcom/adnan/lumisky/definition/QualityTier;)V", "getDayProgress", "setDayProgress", "getSunX", "setSunX", "getSunY", "setSunY", "getMoonX", "setMoonX", "getMoonY", "setMoonY", "getDrawSun", "()Z", "setDrawSun", "(Z)V", "setNight", "getMinute", "setMinute", "getSunriseMinute", "setSunriseMinute", "getSunsetMinute", "setSunsetMinute", "getSolarNoonMinute", "setSolarNoonMinute", "getNightAmount", "setNightAmount", "getHorizonY", "setHorizonY", "getSunColorR", "setSunColorR", "getSunColorG", "setSunColorG", "getSunColorB", "setSunColorB", "getRenderScale", "setRenderScale", "getPostProcessEnabled", "setPostProcessEnabled", "getParticleEffectsEnabled", "setParticleEffectsEnabled", "getVideoPlaybackEnabled", "setVideoPlaybackEnabled", "getSensorParallaxEnabled", "setSensorParallaxEnabled", "getTelemetryEnabled", "setTelemetryEnabled", "getThermalEmergency", "setThermalEmergency", "isGlInitialized", "_gl", "Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "value", "gl", "getGl", "()Lcom/adnan/lumisky/engine/gl/GlResourceManager;", "setGl", "(Lcom/adnan/lumisky/engine/gl/GlResourceManager;)V", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "component8", "component9", "component10", "component11", "component12", "component13", "component14", "component15", "component16", "component17", "component18", "component19", "component20", "component21", "component22", "component23", "component24", "component25", "component26", "component27", "component28", "component29", "component30", "copy", "equals", "other", "hashCode", "toString", "", "app_release"})
public final class MutableRenderFrameState {
    private int width;
    private int height;
    private float timeSeconds;
    private float deltaTimeSeconds;
    private float parallaxOffsetX;
    private float parallaxOffsetY;
    @org.jetbrains.annotations.NotNull()
    private com.adnan.lumisky.definition.QualityTier quality;
    private float dayProgress;
    private float sunX;
    private float sunY;
    private float moonX;
    private float moonY;
    private boolean drawSun;
    private boolean isNight;
    private float minute;
    private int sunriseMinute;
    private int sunsetMinute;
    private int solarNoonMinute;
    private float nightAmount;
    private float horizonY;
    private float sunColorR;
    private float sunColorG;
    private float sunColorB;
    private float renderScale;
    private boolean postProcessEnabled;
    private boolean particleEffectsEnabled;
    private boolean videoPlaybackEnabled;
    private boolean sensorParallaxEnabled;
    private boolean telemetryEnabled;
    private boolean thermalEmergency;
    private boolean isGlInitialized = false;
    private com.adnan.lumisky.engine.gl.GlResourceManager _gl;
    
    public MutableRenderFrameState(int width, int height, float timeSeconds, float deltaTimeSeconds, float parallaxOffsetX, float parallaxOffsetY, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityTier quality, float dayProgress, float sunX, float sunY, float moonX, float moonY, boolean drawSun, boolean isNight, float minute, int sunriseMinute, int sunsetMinute, int solarNoonMinute, float nightAmount, float horizonY, float sunColorR, float sunColorG, float sunColorB, float renderScale, boolean postProcessEnabled, boolean particleEffectsEnabled, boolean videoPlaybackEnabled, boolean sensorParallaxEnabled, boolean telemetryEnabled, boolean thermalEmergency) {
        super();
    }
    
    public final int getWidth() {
        return 0;
    }
    
    public final void setWidth(int p0) {
    }
    
    public final int getHeight() {
        return 0;
    }
    
    public final void setHeight(int p0) {
    }
    
    public final float getTimeSeconds() {
        return 0.0F;
    }
    
    public final void setTimeSeconds(float p0) {
    }
    
    public final float getDeltaTimeSeconds() {
        return 0.0F;
    }
    
    public final void setDeltaTimeSeconds(float p0) {
    }
    
    public final float getParallaxOffsetX() {
        return 0.0F;
    }
    
    public final void setParallaxOffsetX(float p0) {
    }
    
    public final float getParallaxOffsetY() {
        return 0.0F;
    }
    
    public final void setParallaxOffsetY(float p0) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.QualityTier getQuality() {
        return null;
    }
    
    public final void setQuality(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityTier p0) {
    }
    
    public final float getDayProgress() {
        return 0.0F;
    }
    
    public final void setDayProgress(float p0) {
    }
    
    public final float getSunX() {
        return 0.0F;
    }
    
    public final void setSunX(float p0) {
    }
    
    public final float getSunY() {
        return 0.0F;
    }
    
    public final void setSunY(float p0) {
    }
    
    public final float getMoonX() {
        return 0.0F;
    }
    
    public final void setMoonX(float p0) {
    }
    
    public final float getMoonY() {
        return 0.0F;
    }
    
    public final void setMoonY(float p0) {
    }
    
    public final boolean getDrawSun() {
        return false;
    }
    
    public final void setDrawSun(boolean p0) {
    }
    
    public final boolean isNight() {
        return false;
    }
    
    public final void setNight(boolean p0) {
    }
    
    public final float getMinute() {
        return 0.0F;
    }
    
    public final void setMinute(float p0) {
    }
    
    public final int getSunriseMinute() {
        return 0;
    }
    
    public final void setSunriseMinute(int p0) {
    }
    
    public final int getSunsetMinute() {
        return 0;
    }
    
    public final void setSunsetMinute(int p0) {
    }
    
    public final int getSolarNoonMinute() {
        return 0;
    }
    
    public final void setSolarNoonMinute(int p0) {
    }
    
    public final float getNightAmount() {
        return 0.0F;
    }
    
    public final void setNightAmount(float p0) {
    }
    
    public final float getHorizonY() {
        return 0.0F;
    }
    
    public final void setHorizonY(float p0) {
    }
    
    public final float getSunColorR() {
        return 0.0F;
    }
    
    public final void setSunColorR(float p0) {
    }
    
    public final float getSunColorG() {
        return 0.0F;
    }
    
    public final void setSunColorG(float p0) {
    }
    
    public final float getSunColorB() {
        return 0.0F;
    }
    
    public final void setSunColorB(float p0) {
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
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.gl.GlResourceManager getGl() {
        return null;
    }
    
    public final void setGl(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.gl.GlResourceManager value) {
    }
    
    public final boolean isGlInitialized() {
        return false;
    }
    
    public MutableRenderFrameState() {
        super();
    }
    
    public final int component1() {
        return 0;
    }
    
    public final float component10() {
        return 0.0F;
    }
    
    public final float component11() {
        return 0.0F;
    }
    
    public final float component12() {
        return 0.0F;
    }
    
    public final boolean component13() {
        return false;
    }
    
    public final boolean component14() {
        return false;
    }
    
    public final float component15() {
        return 0.0F;
    }
    
    public final int component16() {
        return 0;
    }
    
    public final int component17() {
        return 0;
    }
    
    public final int component18() {
        return 0;
    }
    
    public final float component19() {
        return 0.0F;
    }
    
    public final int component2() {
        return 0;
    }
    
    public final float component20() {
        return 0.0F;
    }
    
    public final float component21() {
        return 0.0F;
    }
    
    public final float component22() {
        return 0.0F;
    }
    
    public final float component23() {
        return 0.0F;
    }
    
    public final float component24() {
        return 0.0F;
    }
    
    public final boolean component25() {
        return false;
    }
    
    public final boolean component26() {
        return false;
    }
    
    public final boolean component27() {
        return false;
    }
    
    public final boolean component28() {
        return false;
    }
    
    public final boolean component29() {
        return false;
    }
    
    public final float component3() {
        return 0.0F;
    }
    
    public final boolean component30() {
        return false;
    }
    
    public final float component4() {
        return 0.0F;
    }
    
    public final float component5() {
        return 0.0F;
    }
    
    public final float component6() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.definition.QualityTier component7() {
        return null;
    }
    
    public final float component8() {
        return 0.0F;
    }
    
    public final float component9() {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.MutableRenderFrameState copy(int width, int height, float timeSeconds, float deltaTimeSeconds, float parallaxOffsetX, float parallaxOffsetY, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.definition.QualityTier quality, float dayProgress, float sunX, float sunY, float moonX, float moonY, boolean drawSun, boolean isNight, float minute, int sunriseMinute, int sunsetMinute, int solarNoonMinute, float nightAmount, float horizonY, float sunColorR, float sunColorG, float sunColorB, float renderScale, boolean postProcessEnabled, boolean particleEffectsEnabled, boolean videoPlaybackEnabled, boolean sensorParallaxEnabled, boolean telemetryEnabled, boolean thermalEmergency) {
        return null;
    }
    
    @java.lang.Override()
    public boolean equals(@org.jetbrains.annotations.Nullable()
    java.lang.Object other) {
        return false;
    }
    
    @java.lang.Override()
    public int hashCode() {
        return 0;
    }
    
    @java.lang.Override()
    @org.jetbrains.annotations.NotNull()
    public java.lang.String toString() {
        return null;
    }
}