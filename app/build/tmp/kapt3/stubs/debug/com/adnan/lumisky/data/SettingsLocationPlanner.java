package com.adnan.lumisky.data;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\u0006\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\b\n\u0002\u0010\u000b\n\u0002\b\u0003\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0006\u0010\u0006\u001a\u00020\u0007J\f\u0010\b\u001a\b\u0012\u0004\u0012\u00020\u00070\tJ\u001e\u0010\n\u001a\u00020\u00072\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\f2\u0006\u0010\u000e\u001a\u00020\u000fJ(\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u0006\u0010\u0014\u001a\u00020\u00072\b\u0010\u0015\u001a\u0004\u0018\u00010\u00162\u0006\u0010\u0017\u001a\u00020\u0005J\u001e\u0010\u0018\u001a\u00020\u00192\u0006\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001b2\u0006\u0010\u001d\u001a\u00020\u001bJ\u000e\u0010\u001e\u001a\u00020\u00132\u0006\u0010\u001f\u001a\u00020\u000fJ\u0016\u0010 \u001a\u00020\u000f2\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\fJ\u0010\u0010!\u001a\u00020\u000f2\u0006\u0010\"\u001a\u00020\u001bH\u0002J\u0018\u0010#\u001a\u00020$2\u0006\u0010%\u001a\u00020\f2\u0006\u0010&\u001a\u00020\fH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/adnan/lumisky/data/SettingsLocationPlanner;", "", "<init>", "()V", "DEVICE_LOCATION_MAX_AGE_MS", "", "defaultManualLocation", "Lcom/adnan/lumisky/data/ManualLocationPreset;", "supportedManualLocations", "", "resolveManualLocation", "latitude", "", "longitude", "timeZoneId", "", "resolve", "Lcom/adnan/lumisky/data/ResolvedLocationLighting;", "mode", "Lcom/adnan/lumisky/data/LocationLightingMode;", "manualLocation", "deviceSnapshot", "Lcom/adnan/lumisky/data/DeviceLocationSnapshot;", "nowEpochMs", "timeline", "Lcom/adnan/lumisky/data/CelestialTimelineLabels;", "sunriseMinute", "", "sunsetMinute", "solarNoonMinute", "modeFromStorage", "value", "formatCoordinates", "formatMinute", "minute", "nearlySame", "", "left", "right", "app_debug"})
public final class SettingsLocationPlanner {
    public static final long DEVICE_LOCATION_MAX_AGE_MS = 86400000L;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.data.SettingsLocationPlanner INSTANCE = null;
    
    private SettingsLocationPlanner() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.ManualLocationPreset defaultManualLocation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.util.List<com.adnan.lumisky.data.ManualLocationPreset> supportedManualLocations() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.ManualLocationPreset resolveManualLocation(double latitude, double longitude, @org.jetbrains.annotations.NotNull()
    java.lang.String timeZoneId) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.ResolvedLocationLighting resolve(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.LocationLightingMode mode, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.ManualLocationPreset manualLocation, @org.jetbrains.annotations.Nullable()
    com.adnan.lumisky.data.DeviceLocationSnapshot deviceSnapshot, long nowEpochMs) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.CelestialTimelineLabels timeline(int sunriseMinute, int sunsetMinute, int solarNoonMinute) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.LocationLightingMode modeFromStorage(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatCoordinates(double latitude, double longitude) {
        return null;
    }
    
    private final java.lang.String formatMinute(int minute) {
        return null;
    }
    
    private final boolean nearlySame(double left, double right) {
        return false;
    }
}