package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000^\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\n\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0018\u0018\u0000 G2\u00020\u0001:\u0006BCDEFGB\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J$\u0010\u0004\u001a\u00020\u00052\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u000bJ$\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u000f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u00112\u0006\u0010\u0012\u001a\u00020\tH\u0002J \u0010\u0013\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\t2\u0006\u0010\u0017\u001a\u00020\tH\u0002J\u0018\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0016\u001a\u00020\tH\u0002J\u0018\u0010\u0019\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\r2\u0006\u0010\u0017\u001a\u00020\tH\u0002J\u0010\u0010\u001a\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\rH\u0002J\u0018\u0010\u001b\u001a\u00020\t2\u0006\u0010\u0016\u001a\u00020\t2\u0006\u0010\u001c\u001a\u00020\tH\u0002J\u0018\u0010\u001d\u001a\u00020\t2\u0006\u0010\u0017\u001a\u00020\t2\u0006\u0010\u001e\u001a\u00020\u001fH\u0002J\"\u0010 \u001a\u00020!2\b\u0010\u0006\u001a\u0004\u0018\u00010\u00072\u0006\u0010\"\u001a\u00020!2\u0006\u0010#\u001a\u00020!H\u0002J(\u0010$\u001a\u00020\t2\u0006\u0010%\u001a\u00020\t2\u0006\u0010&\u001a\u00020\t2\u0006\u0010\'\u001a\u00020\t2\u0006\u0010(\u001a\u00020\tH\u0002J \u0010)\u001a\u00020*2\u0006\u0010+\u001a\u00020\t2\u0006\u0010,\u001a\u00020\t2\u0006\u0010-\u001a\u00020\tH\u0002J \u0010.\u001a\u00020\t2\u0006\u0010+\u001a\u00020\t2\u0006\u0010,\u001a\u00020\t2\u0006\u0010-\u001a\u00020\tH\u0002J(\u0010/\u001a\u0002002\u0006\u00101\u001a\u00020\u00112\u0006\u0010+\u001a\u00020\t2\u0006\u0010,\u001a\u00020\t2\u0006\u0010-\u001a\u00020\tH\u0002J \u00102\u001a\u00020\t2\u0006\u00103\u001a\u00020\t2\u0006\u00104\u001a\u00020\t2\u0006\u00105\u001a\u00020\tH\u0002J\u0010\u00106\u001a\u00020\t2\u0006\u00107\u001a\u00020\tH\u0002J\u0010\u00108\u001a\u00020\t2\u0006\u0010+\u001a\u00020\tH\u0002J\u0018\u00109\u001a\u00020\t2\u0006\u0010+\u001a\u00020\t2\u0006\u0010:\u001a\u00020\tH\u0002J\u0010\u0010;\u001a\u00020\t2\u0006\u0010<\u001a\u00020\tH\u0002J \u0010=\u001a\u00020\t2\u0006\u0010>\u001a\u00020\t2\u0006\u0010?\u001a\u00020\t2\u0006\u0010@\u001a\u00020\tH\u0002J\f\u0010A\u001a\u00020**\u00020\tH\u0002\u00a8\u0006H"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController;", "", "<init>", "()V", "resolve", "Lcom/adnan/lumisky/engine/CelestialUniformState;", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "dayProgress", "", "daylightOverride", "Lcom/adnan/lumisky/engine/DaylightOverride;", "resolveOrbit", "Lcom/adnan/lumisky/engine/CelestialMotionController$ResolvedOrbit;", "explicit", "Lcom/adnan/lumisky/definition/CelestialOrbitDefinition;", "pathType", "", "fallbackPeakY", "resolveVisiblePosition", "Lcom/adnan/lumisky/engine/CelestialMotionController$Vec2;", "orbit", "horizonY", "phaseProgress", "resolveHiddenPosition", "resolveVisibleX", "resolveHiddenX", "resolvePeakY", "peakY", "applyCurve", "curve", "Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;", "resolveSolarNoon", "", "sunriseMinute", "sunsetMinute", "resolvePeakAlignedPhaseProgress", "currentMinute", "startMinute", "peakMinute", "endMinute", "isDaytime", "", "minute", "sunrise", "sunset", "calculateNightAmount", "resolveSunColor", "Lcom/adnan/lumisky/engine/CelestialMotionController$Vec3;", "wallpaperId", "smoothstep", "edge0", "edge1", "value", "minuteOfDay", "progress", "normalizeMinute", "normalizeMinuteForward", "anchorMinute", "mapToLegacyShaderY", "engineY", "lerp", "start", "end", "t", "isFiniteValue", "ResolvedOrbit", "Vec2", "Vec3", "PathType", "OrbitCurve", "Companion", "app_debug"})
public final class CelestialMotionController {
    @java.lang.Deprecated()
    public static final int MINUTES_PER_DAY = 1440;
    @java.lang.Deprecated()
    public static final int HALF_DAY_MINUTES = 720;
    @java.lang.Deprecated()
    public static final float DEFAULT_HORIZON_Y = 0.2F;
    @java.lang.Deprecated()
    public static final float DEFAULT_PEAK_Y = 0.9F;
    @java.lang.Deprecated()
    public static final float VERTICAL_PATH_X = 0.5F;
    @java.lang.Deprecated()
    public static final float ARC_PATH_START_X = 0.0F;
    @java.lang.Deprecated()
    public static final float ARC_PATH_END_X = 1.0F;
    @java.lang.Deprecated()
    public static final float MIN_PEAK_DELTA = 0.05F;
    @java.lang.Deprecated()
    public static final float HIDDEN_DEPTH = 0.75F;
    @java.lang.Deprecated()
    public static final float HIDDEN_Y_MAX = -0.15F;
    @java.lang.Deprecated()
    public static final int MAX_MINUTE_WRAP_ITERATIONS = 3;
    @java.lang.Deprecated()
    public static final float NIGHT_TRANSITION_AFTER_SUNSET_MIN = 20.0F;
    @java.lang.Deprecated()
    public static final float NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN = 30.0F;
    @java.lang.Deprecated()
    public static final float NIGHT_TRANSITION_AFTER_SUNRISE_MIN = 10.0F;
    @org.jetbrains.annotations.NotNull()
    private static final com.adnan.lumisky.engine.CelestialMotionController.Companion Companion = null;
    
    public CelestialMotionController() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.CelestialUniformState resolve(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.WallpaperDefinition definition, float dayProgress, @org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.DaylightOverride daylightOverride) {
        return null;
    }
    
    private final com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit resolveOrbit(com.adnan.lumisky.definition.CelestialOrbitDefinition explicit, java.lang.String pathType, float fallbackPeakY) {
        return null;
    }
    
    private final com.adnan.lumisky.engine.CelestialMotionController.Vec2 resolveVisiblePosition(com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit orbit, float horizonY, float phaseProgress) {
        return null;
    }
    
    private final com.adnan.lumisky.engine.CelestialMotionController.Vec2 resolveHiddenPosition(com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit orbit, float horizonY) {
        return null;
    }
    
    private final float resolveVisibleX(com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit orbit, float phaseProgress) {
        return 0.0F;
    }
    
    private final float resolveHiddenX(com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit orbit) {
        return 0.0F;
    }
    
    private final float resolvePeakY(float horizonY, float peakY) {
        return 0.0F;
    }
    
    private final float applyCurve(float phaseProgress, com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve curve) {
        return 0.0F;
    }
    
    private final int resolveSolarNoon(com.adnan.lumisky.definition.WallpaperDefinition definition, int sunriseMinute, int sunsetMinute) {
        return 0;
    }
    
    private final float resolvePeakAlignedPhaseProgress(float currentMinute, float startMinute, float peakMinute, float endMinute) {
        return 0.0F;
    }
    
    private final boolean isDaytime(float minute, float sunrise, float sunset) {
        return false;
    }
    
    private final float calculateNightAmount(float minute, float sunrise, float sunset) {
        return 0.0F;
    }
    
    private final com.adnan.lumisky.engine.CelestialMotionController.Vec3 resolveSunColor(java.lang.String wallpaperId, float minute, float sunrise, float sunset) {
        return null;
    }
    
    private final float smoothstep(float edge0, float edge1, float value) {
        return 0.0F;
    }
    
    private final float minuteOfDay(float progress) {
        return 0.0F;
    }
    
    private final float normalizeMinute(float minute) {
        return 0.0F;
    }
    
    private final float normalizeMinuteForward(float minute, float anchorMinute) {
        return 0.0F;
    }
    
    private final float mapToLegacyShaderY(float engineY) {
        return 0.0F;
    }
    
    private final float lerp(float start, float end, float t) {
        return 0.0F;
    }
    
    private final boolean isFiniteValue(float $this$isFiniteValue) {
        return false;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u0007\n\u0002\b\f\b\u0082\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\bX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$Companion;", "", "<init>", "()V", "MINUTES_PER_DAY", "", "HALF_DAY_MINUTES", "DEFAULT_HORIZON_Y", "", "DEFAULT_PEAK_Y", "VERTICAL_PATH_X", "ARC_PATH_START_X", "ARC_PATH_END_X", "MIN_PEAK_DELTA", "HIDDEN_DEPTH", "HIDDEN_Y_MAX", "MAX_MINUTE_WRAP_ITERATIONS", "NIGHT_TRANSITION_AFTER_SUNSET_MIN", "NIGHT_TRANSITION_BEFORE_SUNRISE_WIDE_MIN", "NIGHT_TRANSITION_AFTER_SUNRISE_MIN", "app_debug"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;", "", "<init>", "(Ljava/lang/String;I)V", "LINEAR", "EASE_IN_OUT", "app_debug"})
    static enum OrbitCurve {
        /*public static final*/ LINEAR /* = new LINEAR() */,
        /*public static final*/ EASE_IN_OUT /* = new EASE_IN_OUT() */;
        
        OrbitCurve() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve> getEntries() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0005\b\u0082\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003j\u0002\b\u0004j\u0002\b\u0005\u00a8\u0006\u0006"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$PathType;", "", "<init>", "(Ljava/lang/String;I)V", "VERTICAL", "ARC", "app_debug"})
    static enum PathType {
        /*public static final*/ VERTICAL /* = new VERTICAL() */,
        /*public static final*/ ARC /* = new ARC() */;
        
        PathType() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.adnan.lumisky.engine.CelestialMotionController.PathType> getEntries() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0016\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\u0007\u001a\u00020\u0005\u0012\b\u0010\b\u001a\u0004\u0018\u00010\u0005\u0012\u0006\u0010\t\u001a\u00020\n\u00a2\u0006\u0004\b\u000b\u0010\fJ\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\u0010\u0010\u0019\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0010J\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0010J\t\u0010\u001b\u001a\u00020\u0005H\u00c6\u0003J\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0010J\t\u0010\u001d\u001a\u00020\nH\u00c6\u0003JP\u0010\u001e\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\u0007\u001a\u00020\u00052\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u00052\b\b\u0002\u0010\t\u001a\u00020\nH\u00c6\u0001\u00a2\u0006\u0002\u0010\u001fJ\u0013\u0010 \u001a\u00020!2\b\u0010\"\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010#\u001a\u00020$H\u00d6\u0001J\t\u0010%\u001a\u00020&H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\u000eR\u0015\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u0011\u001a\u0004\b\u000f\u0010\u0010R\u0015\u0010\u0006\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u0011\u001a\u0004\b\u0012\u0010\u0010R\u0011\u0010\u0007\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014R\u0015\u0010\b\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\n\n\u0002\u0010\u0011\u001a\u0004\b\u0015\u0010\u0010R\u0011\u0010\t\u001a\u00020\n\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\u0017\u00a8\u0006\'"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$ResolvedOrbit;", "", "pathType", "Lcom/adnan/lumisky/engine/CelestialMotionController$PathType;", "startX", "", "endX", "peakY", "hiddenY", "curve", "Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;", "<init>", "(Lcom/adnan/lumisky/engine/CelestialMotionController$PathType;Ljava/lang/Float;Ljava/lang/Float;FLjava/lang/Float;Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;)V", "getPathType", "()Lcom/adnan/lumisky/engine/CelestialMotionController$PathType;", "getStartX", "()Ljava/lang/Float;", "Ljava/lang/Float;", "getEndX", "getPeakY", "()F", "getHiddenY", "getCurve", "()Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "(Lcom/adnan/lumisky/engine/CelestialMotionController$PathType;Ljava/lang/Float;Ljava/lang/Float;FLjava/lang/Float;Lcom/adnan/lumisky/engine/CelestialMotionController$OrbitCurve;)Lcom/adnan/lumisky/engine/CelestialMotionController$ResolvedOrbit;", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    static final class ResolvedOrbit {
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.engine.CelestialMotionController.PathType pathType = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Float startX = null;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Float endX = null;
        private final float peakY = 0.0F;
        @org.jetbrains.annotations.Nullable()
        private final java.lang.Float hiddenY = null;
        @org.jetbrains.annotations.NotNull()
        private final com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve curve = null;
        
        public ResolvedOrbit(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.engine.CelestialMotionController.PathType pathType, @org.jetbrains.annotations.Nullable()
        java.lang.Float startX, @org.jetbrains.annotations.Nullable()
        java.lang.Float endX, float peakY, @org.jetbrains.annotations.Nullable()
        java.lang.Float hiddenY, @org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve curve) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.PathType getPathType() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float getStartX() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float getEndX() {
            return null;
        }
        
        public final float getPeakY() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float getHiddenY() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve getCurve() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.PathType component1() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float component2() {
            return null;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float component3() {
            return null;
        }
        
        public final float component4() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.Nullable()
        public final java.lang.Float component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.ResolvedOrbit copy(@org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.engine.CelestialMotionController.PathType pathType, @org.jetbrains.annotations.Nullable()
        java.lang.Float startX, @org.jetbrains.annotations.Nullable()
        java.lang.Float endX, float peakY, @org.jetbrains.annotations.Nullable()
        java.lang.Float hiddenY, @org.jetbrains.annotations.NotNull()
        com.adnan.lumisky.engine.CelestialMotionController.OrbitCurve curve) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0007\n\u0002\b\n\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\t\u0010\n\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000b\u001a\u00020\u0003H\u00c6\u0003J\u001d\u0010\f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\r\u001a\u00020\u000e2\b\u0010\u000f\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0010\u001a\u00020\u0011H\u00d6\u0001J\t\u0010\u0012\u001a\u00020\u0013H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\t\u0010\b\u00a8\u0006\u0014"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$Vec2;", "", "x", "", "y", "<init>", "(FF)V", "getX", "()F", "getY", "component1", "component2", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    static final class Vec2 {
        private final float x = 0.0F;
        private final float y = 0.0F;
        
        public Vec2(float x, float y) {
            super();
        }
        
        public final float getX() {
            return 0.0F;
        }
        
        public final float getY() {
            return 0.0F;
        }
        
        public final float component1() {
            return 0.0F;
        }
        
        public final float component2() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.Vec2 copy(float x, float y) {
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
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0007\n\u0002\b\r\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0082\b\u0018\u00002\u00020\u0001B\u001f\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\u0004\b\u0006\u0010\u0007J\t\u0010\f\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u000e\u001a\u00020\u0003H\u00c6\u0003J\'\u0010\u000f\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0014H\u00d6\u0001J\t\u0010\u0015\u001a\u00020\u0016H\u00d6\u0001R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\t\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/engine/CelestialMotionController$Vec3;", "", "r", "", "g", "b", "<init>", "(FFF)V", "getR", "()F", "getG", "getB", "component1", "component2", "component3", "copy", "equals", "", "other", "hashCode", "", "toString", "", "app_debug"})
    static final class Vec3 {
        private final float r = 0.0F;
        private final float g = 0.0F;
        private final float b = 0.0F;
        
        public Vec3(float r, float g, float b) {
            super();
        }
        
        public final float getR() {
            return 0.0F;
        }
        
        public final float getG() {
            return 0.0F;
        }
        
        public final float getB() {
            return 0.0F;
        }
        
        public final float component1() {
            return 0.0F;
        }
        
        public final float component2() {
            return 0.0F;
        }
        
        public final float component3() {
            return 0.0F;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.adnan.lumisky.engine.CelestialMotionController.Vec3 copy(float r, float g, float b) {
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
}