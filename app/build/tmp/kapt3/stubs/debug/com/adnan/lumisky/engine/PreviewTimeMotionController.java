package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\n\u0018\u0000 #2\u00020\u0001:\u0001#B\u0017\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\u0010\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\bJ\"\u0010\u0012\u001a\u00020\n2\b\u0010\u0013\u001a\u0004\u0018\u00010\u00142\u0006\u0010\u0015\u001a\u00020\n2\b\b\u0002\u0010\u0016\u001a\u00020\nJ\u001e\u0010\u0017\u001a\u00020\f2\u0006\u0010\u0018\u001a\u00020\n2\u0006\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\u001b\u001a\u00020\u001aJ\u0012\u0010\u001c\u001a\u00020\n2\b\u0010\u0013\u001a\u0004\u0018\u00010\u0014H\u0002J \u0010\u001d\u001a\u00020\n2\u0006\u0010\u001e\u001a\u00020\n2\u0006\u0010\u001f\u001a\u00020\n2\u0006\u0010 \u001a\u00020\nH\u0002J\u0010\u0010!\u001a\u00020\n2\u0006\u0010\"\u001a\u00020\nH\u0002R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0012\u0010\r\u001a\u0004\u0018\u00010\nX\u0082\u000e\u00a2\u0006\u0004\n\u0002\u0010\u000e\u00a8\u0006$"}, d2 = {"Lcom/adnan/lumisky/engine/PreviewTimeMotionController;", "", "nowProvider", "Lkotlin/Function0;", "", "<init>", "(Lkotlin/jvm/functions/Function0;)V", "activeWallpaperId", "", "animationElapsedSeconds", "", "window", "Lcom/adnan/lumisky/engine/PreviewFocusCatchUpWindow;", "completedProgress", "Ljava/lang/Float;", "reset", "", "wallpaperId", "resolveDayProgress", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "deltaTimeSeconds", "durationSeconds", "resolveFocusCatchUpWindow", "nowProgress", "sunriseMinute", "", "sunsetMinute", "currentDayProgress", "lerp", "start", "end", "t", "wrapDayProgress", "value", "Companion", "app_debug"})
public final class PreviewTimeMotionController {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<java.lang.Long> nowProvider = null;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String activeWallpaperId;
    private float animationElapsedSeconds = 0.0F;
    @org.jetbrains.annotations.NotNull()
    private com.adnan.lumisky.engine.PreviewFocusCatchUpWindow window;
    @org.jetbrains.annotations.Nullable()
    private java.lang.Float completedProgress;
    private static final float DEFAULT_FOCUS_CATCH_UP_SECONDS = 4.0F;
    private static final float MIN_FOCUS_CATCH_UP_SECONDS = 0.3F;
    private static final int MINUTES_PER_DAY = 1440;
    private static final int DEFAULT_SUNRISE_MINUTE = 360;
    private static final int DEFAULT_SUNSET_MINUTE = 1080;
    private static final long NANOS_PER_DAY = 86400000000000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.engine.PreviewTimeMotionController.Companion Companion = null;
    
    public PreviewTimeMotionController(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<java.lang.Long> nowProvider) {
        super();
    }
    
    public final void reset(@org.jetbrains.annotations.Nullable()
    java.lang.String wallpaperId) {
    }
    
    public final float resolveDayProgress(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.WallpaperDefinition definition, float deltaTimeSeconds, float durationSeconds) {
        return 0.0F;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.PreviewFocusCatchUpWindow resolveFocusCatchUpWindow(float nowProgress, int sunriseMinute, int sunsetMinute) {
        return null;
    }
    
    private final float currentDayProgress(com.adnan.lumisky.definition.WallpaperDefinition definition) {
        return 0.0F;
    }
    
    private final float lerp(float start, float end, float t) {
        return 0.0F;
    }
    
    private final float wrapDayProgress(float value) {
        return 0.0F;
    }
    
    public PreviewTimeMotionController() {
        super();
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\"\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\bX\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/adnan/lumisky/engine/PreviewTimeMotionController$Companion;", "", "<init>", "()V", "DEFAULT_FOCUS_CATCH_UP_SECONDS", "", "MIN_FOCUS_CATCH_UP_SECONDS", "MINUTES_PER_DAY", "", "DEFAULT_SUNRISE_MINUTE", "DEFAULT_SUNSET_MINUTE", "NANOS_PER_DAY", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}