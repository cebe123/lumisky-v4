package com.adnan.lumisky.engine;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\n\u0018\u0000 #2\u00020\u0001:\u0001#B\u0007\u00a2\u0006\u0004\b\u0002\u0010\u0003J(\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\t\u001a\u00020\n2\b\b\u0002\u0010\u000b\u001a\u00020\fJ7\u0010\r\u001a\u0004\u0018\u00010\u00072\u0006\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\u00072\u0006\u0010\u000e\u001a\u00020\u000f2\u0006\u0010\u0010\u001a\u00020\u00072\u0006\u0010\u0011\u001a\u00020\u0012H\u0002\u00a2\u0006\u0002\u0010\u0013J\u0010\u0010\u0014\u001a\u00020\u000f2\u0006\u0010\u0015\u001a\u00020\u0007H\u0002J\u0018\u0010\u0016\u001a\u00020\u000f2\u0006\u0010\u0017\u001a\u00020\u000f2\u0006\u0010\u0018\u001a\u00020\u000fH\u0002J\u0010\u0010\u0019\u001a\u00020\u001a2\u0006\u0010\t\u001a\u00020\nH\u0002J\u0010\u0010\u001b\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010\u001d\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010\u001e\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010\u001f\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010 \u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010!\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002J\u0010\u0010\"\u001a\u00020\u00072\u0006\u0010\u001c\u001a\u00020\u0007H\u0002\u00a8\u0006$"}, d2 = {"Lcom/adnan/lumisky/engine/LocationDaylightController;", "", "<init>", "()V", "resolve", "Lcom/adnan/lumisky/engine/DaylightOverride;", "latitude", "", "longitude", "timeZoneId", "", "date", "Ljava/time/LocalDate;", "calculateLocalHour", "dayOfYear", "", "offsetHours", "isSunrise", "", "(DDIDZ)Ljava/lang/Double;", "hourToMinute", "hour", "resolveSolarNoon", "sunriseMinute", "sunsetMinute", "resolveZone", "Ljava/time/ZoneId;", "normalizeDegrees", "value", "normalizeHours", "sinDeg", "cosDeg", "tanDeg", "degToRad", "radToDeg", "Companion", "app_release"})
public final class LocationDaylightController {
    @java.lang.Deprecated()
    public static final int MINUTES_PER_DAY = 1440;
    @java.lang.Deprecated()
    public static final int DEFAULT_SUNRISE = 360;
    @java.lang.Deprecated()
    public static final int DEFAULT_SUNSET = 1080;
    @java.lang.Deprecated()
    public static final int DEFAULT_SOLAR_NOON = 720;
    @org.jetbrains.annotations.NotNull()
    @java.lang.Deprecated()
    public static final java.lang.String DEFAULT_TIME_ZONE = "Europe/Istanbul";
    @java.lang.Deprecated()
    public static final double ZENITH_DEGREES = 90.833;
    @org.jetbrains.annotations.NotNull()
    private static final com.adnan.lumisky.engine.LocationDaylightController.Companion Companion = null;
    
    public LocationDaylightController() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.engine.DaylightOverride resolve(double latitude, double longitude, @org.jetbrains.annotations.NotNull()
    java.lang.String timeZoneId, @org.jetbrains.annotations.NotNull()
    java.time.LocalDate date) {
        return null;
    }
    
    private final java.lang.Double calculateLocalHour(double latitude, double longitude, int dayOfYear, double offsetHours, boolean isSunrise) {
        return null;
    }
    
    private final int hourToMinute(double hour) {
        return 0;
    }
    
    private final int resolveSolarNoon(int sunriseMinute, int sunsetMinute) {
        return 0;
    }
    
    private final java.time.ZoneId resolveZone(java.lang.String timeZoneId) {
        return null;
    }
    
    private final double normalizeDegrees(double value) {
        return 0.0;
    }
    
    private final double normalizeHours(double value) {
        return 0.0;
    }
    
    private final double sinDeg(double value) {
        return 0.0;
    }
    
    private final double cosDeg(double value) {
        return 0.0;
    }
    
    private final double tanDeg(double value) {
        return 0.0;
    }
    
    private final double degToRad(double value) {
        return 0.0;
    }
    
    private final double radToDeg(double value) {
        return 0.0;
    }
    
    @kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\u0006\n\u0000\b\u0082\u0003\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\r"}, d2 = {"Lcom/adnan/lumisky/engine/LocationDaylightController$Companion;", "", "<init>", "()V", "MINUTES_PER_DAY", "", "DEFAULT_SUNRISE", "DEFAULT_SUNSET", "DEFAULT_SOLAR_NOON", "DEFAULT_TIME_ZONE", "", "ZENITH_DEGREES", "", "app_release"})
    static final class Companion {
        
        private Companion() {
            super();
        }
    }
}