package com.adnan.lumisky.ui.settings;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000*\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\b\u001a\u0018\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u0000\u001a\u0010\u0010\u0006\u001a\u00020\u00072\u0006\u0010\u0002\u001a\u00020\u0003H\u0002\u001a \u0010\b\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\n\u001a\u00020\u00052\u0006\u0010\u000b\u001a\u00020\u0005H\u0002\u001a(\u0010\f\u001a\u00020\t2\u0006\u0010\u0004\u001a\u00020\u00052\u0006\u0010\r\u001a\u00020\u00052\u0006\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u000f\u001a\u00020\u0010H\u0002\u001a \u0010\u0011\u001a\u00020\u00102\u0006\u0010\u0012\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0005H\u0002\u001a\u0018\u0010\u0015\u001a\u00020\u00052\u0006\u0010\u0013\u001a\u00020\u00052\u0006\u0010\u0014\u001a\u00020\u0005H\u0002\u001a\u0010\u0010\u0016\u001a\u00020\u00052\u0006\u0010\u0012\u001a\u00020\u0005H\u0002\"\u000e\u0010\u0017\u001a\u00020\u0005X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"resolveCelestialTimeline", "Lcom/adnan/lumisky/ui/settings/CelestialTimelineSnapshot;", "daylight", "Lcom/adnan/lumisky/engine/DaylightOverride;", "currentMinute", "", "deriveMoonWindow", "Lcom/adnan/lumisky/ui/settings/MoonWindow;", "resolveSunProgress", "", "sunriseMinute", "sunsetMinute", "resolveMoonProgress", "moonriseMinute", "moonsetMinute", "isMoonActive", "", "isInWrappedRange", "minute", "startMinute", "endMinute", "minutesForward", "normalizeMinute", "MINUTES_PER_DAY", "app_debug"})
public final class SettingsCelestialTimelineKt {
    private static final int MINUTES_PER_DAY = 1440;
    
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.ui.settings.CelestialTimelineSnapshot resolveCelestialTimeline(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.DaylightOverride daylight, int currentMinute) {
        return null;
    }
    
    private static final com.adnan.lumisky.ui.settings.MoonWindow deriveMoonWindow(com.adnan.lumisky.engine.DaylightOverride daylight) {
        return null;
    }
    
    private static final float resolveSunProgress(int currentMinute, int sunriseMinute, int sunsetMinute) {
        return 0.0F;
    }
    
    private static final float resolveMoonProgress(int currentMinute, int moonriseMinute, int moonsetMinute, boolean isMoonActive) {
        return 0.0F;
    }
    
    private static final boolean isInWrappedRange(int minute, int startMinute, int endMinute) {
        return false;
    }
    
    private static final int minutesForward(int startMinute, int endMinute) {
        return 0;
    }
    
    private static final int normalizeMinute(int minute) {
        return 0;
    }
}