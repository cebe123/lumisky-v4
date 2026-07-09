package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000F\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\u0018\u0000 \u00192\u00020\u0001:\u0001\u0019B\u0017\u0012\u000e\b\u0002\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003\u00a2\u0006\u0004\b\u0005\u0010\u0006J\u001a\u0010\u0007\u001a\u00020\b2\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fJ\u001e\u0010\u0007\u001a\u00020\b2\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010J\u0010\u0010\u0012\u001a\u00020\u000e2\u0006\u0010\u0013\u001a\u00020\u0014H\u0002J\u001c\u0010\u0015\u001a\u00020\u00142\b\u0010\t\u001a\u0004\u0018\u00010\n2\b\u0010\u000b\u001a\u0004\u0018\u00010\fH\u0002J \u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u00102\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0011\u001a\u00020\u0010H\u0002R\u0014\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001a"}, d2 = {"Lcom/adnan/lumisky/engine/LiveWallpaperCatchUpController;", "", "nowProvider", "Lkotlin/Function0;", "", "<init>", "(Lkotlin/jvm/functions/Function0;)V", "resolveWindow", "Lcom/adnan/lumisky/engine/LiveWallpaperCatchUpWindow;", "definition", "Lcom/adnan/lumisky/definition/WallpaperDefinition;", "daylightOverride", "Lcom/adnan/lumisky/engine/DaylightOverride;", "nowProgress", "", "sunriseMinute", "", "sunsetMinute", "currentDayProgress", "zoneId", "Ljava/time/ZoneId;", "resolveZoneId", "isDaytime", "", "nowMinute", "Companion", "app_release"})
public final class LiveWallpaperCatchUpController {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function0<java.lang.Long> nowProvider = null;
    @java.lang.Deprecated()
    public static final int MINUTES_PER_DAY = 1440;
    @java.lang.Deprecated()
    public static final int DEFAULT_SUNRISE_MINUTE = 360;
    @java.lang.Deprecated()
    public static final int DEFAULT_SUNSET_MINUTE = 1080;
    @org.jetbrains.annotations.NotNull()
    @java.lang.Deprecated()
    public static final java.lang.String DEFAULT_TIME_ZONE = "Europe/Istanbul";
    @java.lang.Deprecated()
    public static final long NANOS_PER_DAY = 86400000000000L;
    @org.jetbrains.annotations.NotNull()
    private static final com.adnan.lumisky.engine.LiveWallpaperCatchUpController.Companion Companion = null;
    
    public LiveWallpaperCatchUpController(@org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<java.lang.Long> nowProvider) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.LiveWallpaperCatchUpWindow resolveWindow(@org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.definition.WallpaperDefinition definition, @org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.engine.DaylightOverride daylightOverride) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.LiveWallpaperCatchUpWindow resolveWindow(float nowProgress, int sunriseMinute, int sunsetMinute) {
        return null;
    }
    
    private final float currentDayProgress(java.time.ZoneId zoneId) {
        return 0.0F;
    }
    
    private final java.time.ZoneId resolveZoneId(com.adnan.lumisky.definition.WallpaperDefinition definition, com.adnan.lumisky.engine.DaylightOverride daylightOverride) {
        return null;
    }
    
    private final boolean isDaytime(int nowMinute, int sunriseMinute, int sunsetMinute) {
        return false;
    }
    
    public LiveWallpaperCatchUpController() {
        super();
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\tX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\f"}, d2 = {"Lcom/adnan/lumisky/engine/LiveWallpaperCatchUpController$Companion;", "", "<init>", "()V", "MINUTES_PER_DAY", "", "DEFAULT_SUNRISE_MINUTE", "DEFAULT_SUNSET_MINUTE", "DEFAULT_TIME_ZONE", "", "NANOS_PER_DAY", "", "app_release"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}