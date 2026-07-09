package com.adnan.lumisky.ui.settings;

@kotlin.Metadata(mv = {2, 2, 0}, k = 2, xi = 48, d1 = {"\u0000\u0086\u0001\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0011\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u000e\n\u0002\u0010$\n\u0000\u001a\u001e\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0007\u001a\u0016\u0010\u0006\u001a\u00020\u00012\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001aH\u0010\u0007\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\u000b2\u0006\u0010\f\u001a\u00020\u000b2\u0012\u0010\r\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010\u000f\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001ax\u0010\u0010\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\u000b2\u0006\u0010\u0014\u001a\u00020\t2\b\u0010\u0015\u001a\u0004\u0018\u00010\u000b2\u0006\u0010\u0016\u001a\u00020\u00172\u0012\u0010\u0018\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\u000e2\f\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\u0012\u0010\u001a\u001a\u000e\u0012\u0004\u0012\u00020\u001b\u0012\u0004\u0012\u00020\u00010\u000e2\u0006\u0010\u001c\u001a\u00020\u001bH\u0003\u001al\u0010\u001d\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\u001e\u001a\u00020\u000b2\u0006\u0010\u001f\u001a\u00020\u000b2\u0006\u0010 \u001a\u00020\t2\u0006\u0010!\u001a\u00020\u000b2\u0012\u0010\"\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010#\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\u000e2\u0012\u0010$\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a,\u0010%\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010&\u001a\u00020\t2\u0012\u0010\'\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a\u0010\u0010(\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\tH\u0003\u001a\u0010\u0010)\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u0017H\u0003\u001a\u0010\u0010*\u001a\u00020\u00012\u0006\u0010\u0016\u001a\u00020\u0017H\u0003\u001a9\u0010+\u001a\u00020\u00012\u0006\u0010,\u001a\u00020-2\u0006\u0010.\u001a\u00020/2\u0006\u00100\u001a\u00020/2\u0006\u00101\u001a\u00020/2\b\b\u0002\u00102\u001a\u000203H\u0003\u00a2\u0006\u0004\b4\u00105\u001a*\u00106\u001a\u00020\u00012\u0006\u0010,\u001a\u00020-2\u0006\u00107\u001a\u00020\u000b2\u0006\u00108\u001a\u00020\u000b2\b\b\u0002\u00102\u001a\u000203H\u0003\u001a\u0012\u00109\u001a\u00020:2\b\u0010;\u001a\u0004\u0018\u00010\u000bH\u0003\u001a\u0012\u0010<\u001a\u00020=2\b\u0010;\u001a\u0004\u0018\u00010\u000bH\u0002\u001a\u0010\u0010>\u001a\u00020:2\u0006\u0010?\u001a\u00020=H\u0002\u001a\u0010\u0010@\u001a\u00020A2\u0006\u0010B\u001a\u00020CH\u0002\u001a\u0010\u0010D\u001a\u00020\u000b2\u0006\u0010E\u001a\u00020:H\u0002\u001a+\u0010F\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\u0006\u0010G\u001a\u00020\u000b2\u0011\u0010H\u001a\r\u0012\u0004\u0012\u00020\u00010\u0005\u00a2\u0006\u0002\bIH\u0003\u001a\"\u0010J\u001a\u00020\u00012\u0006\u0010K\u001a\u00020\u000b2\u0006\u00108\u001a\u00020\u000b2\b\u0010L\u001a\u0004\u0018\u00010-H\u0003\u001a<\u0010M\u001a\u00020\u00012\u0006\u0010,\u001a\u00020-2\u0006\u0010K\u001a\u00020\u000b2\u0006\u0010N\u001a\u00020\u000b2\u0006\u0010O\u001a\u00020\t2\u0012\u0010P\u001a\u000e\u0012\u0004\u0012\u00020\t\u0012\u0004\u0012\u00020\u00010\u000eH\u0003\u001a:\u0010Q\u001a\u00020\u00012\u0006\u0010K\u001a\u00020\u000b2\n\b\u0002\u0010N\u001a\u0004\u0018\u00010\u000b2\u0006\u0010,\u001a\u00020-2\u0006\u0010R\u001a\u00020\t2\f\u0010S\u001a\b\u0012\u0004\u0012\u00020\u00010\u0005H\u0003\u001a\b\u0010T\u001a\u00020\u0001H\u0003\u001a\u0010\u0010U\u001a\u00020\u000b2\u0006\u0010V\u001a\u00020\u000bH\u0002\u001a\u0014\u0010W\u001a\u000e\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b0XH\u0002\u00a8\u0006Y"}, d2 = {"SettingsScreen", "", "viewModel", "Lcom/adnan/lumisky/ui/settings/SettingsViewModel;", "onBackClick", "Lkotlin/Function0;", "SettingsTopBar", "AppearanceSection", "isDark", "", "appThemeMode", "", "languageTag", "onThemeModeSelected", "Lkotlin/Function1;", "onLanguageSelected", "LocationSection", "locationMode", "Lcom/adnan/lumisky/data/LocationLightingMode;", "resolvedLabel", "usesDeviceLocation", "deviceSnapshotLabel", "celestialTimeline", "Lcom/adnan/lumisky/ui/settings/CelestialTimelineSnapshot;", "onToggleDevice", "onRefreshDevice", "onManualLocationSelected", "Lcom/adnan/lumisky/data/ManualLocationPreset;", "manualLocation", "WallpaperSection", "selectedWallpaperId", "qualityTier", "highRefreshEnabled", "performanceMode", "onQualitySelected", "onHighRefreshChanged", "onPerformanceModeSelected", "RuntimeEffectsSection", "previewTimeSimulation", "onPreviewTimeSimulationChanged", "SupportAndAboutSection", "CelestialCyclePanel", "CelestialTimelineTrack", "CelestialMarker", "icon", "Landroidx/compose/ui/graphics/vector/ImageVector;", "fillColor", "Landroidx/compose/ui/graphics/Color;", "iconTint", "glowColor", "modifier", "Landroidx/compose/ui/Modifier;", "CelestialMarker-f1JAnFk", "(Landroidx/compose/ui/graphics/vector/ImageVector;JJJLandroidx/compose/ui/Modifier;)V", "TimelineCell", "label", "value", "rememberCurrentMinute", "", "timeZoneId", "resolveTimelineZoneId", "Ljava/time/ZoneId;", "currentMinuteOfDay", "zoneId", "delayUntilNextMinute", "", "now", "Ljava/time/ZonedDateTime;", "formatMinuteLabel", "minute", "SettingsCard", "kicker", "content", "Landroidx/compose/runtime/Composable;", "InfoActionRow", "title", "leadingIcon", "SwitchRow", "subtitle", "checked", "onCheckedChange", "SelectableOptionRow", "selected", "onClick", "SectionDivider", "languageLabel", "tag", "languageOptions", "", "app_release"})
public final class SettingsScreenKt {
    
    @kotlin.OptIn(markerClass = {androidx.compose.material3.ExperimentalMaterial3Api.class})
    @androidx.compose.runtime.Composable()
    public static final void SettingsScreen(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.ui.settings.SettingsViewModel viewModel, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsTopBar(kotlin.jvm.functions.Function0<kotlin.Unit> onBackClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void AppearanceSection(boolean isDark, java.lang.String appThemeMode, java.lang.String languageTag, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onThemeModeSelected, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onLanguageSelected) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void LocationSection(boolean isDark, com.adnan.lumisky.data.LocationLightingMode locationMode, java.lang.String resolvedLabel, boolean usesDeviceLocation, java.lang.String deviceSnapshotLabel, com.adnan.lumisky.ui.settings.CelestialTimelineSnapshot celestialTimeline, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onToggleDevice, kotlin.jvm.functions.Function0<kotlin.Unit> onRefreshDevice, kotlin.jvm.functions.Function1<? super com.adnan.lumisky.data.ManualLocationPreset, kotlin.Unit> onManualLocationSelected, com.adnan.lumisky.data.ManualLocationPreset manualLocation) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void WallpaperSection(boolean isDark, java.lang.String selectedWallpaperId, java.lang.String qualityTier, boolean highRefreshEnabled, java.lang.String performanceMode, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onQualitySelected, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onHighRefreshChanged, kotlin.jvm.functions.Function1<? super java.lang.String, kotlin.Unit> onPerformanceModeSelected) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void RuntimeEffectsSection(boolean isDark, boolean previewTimeSimulation, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onPreviewTimeSimulationChanged) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SupportAndAboutSection(boolean isDark) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CelestialCyclePanel(com.adnan.lumisky.ui.settings.CelestialTimelineSnapshot celestialTimeline) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void CelestialTimelineTrack(com.adnan.lumisky.ui.settings.CelestialTimelineSnapshot celestialTimeline) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void TimelineCell(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String label, java.lang.String value, androidx.compose.ui.Modifier modifier) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final int rememberCurrentMinute(java.lang.String timeZoneId) {
        return 0;
    }
    
    private static final java.time.ZoneId resolveTimelineZoneId(java.lang.String timeZoneId) {
        return null;
    }
    
    private static final int currentMinuteOfDay(java.time.ZoneId zoneId) {
        return 0;
    }
    
    private static final long delayUntilNextMinute(java.time.ZonedDateTime now) {
        return 0L;
    }
    
    private static final java.lang.String formatMinuteLabel(int minute) {
        return null;
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SettingsCard(boolean isDark, java.lang.String kicker, androidx.compose.runtime.internal.ComposableFunction0<kotlin.Unit> content) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void InfoActionRow(java.lang.String title, java.lang.String value, androidx.compose.ui.graphics.vector.ImageVector leadingIcon) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SwitchRow(androidx.compose.ui.graphics.vector.ImageVector icon, java.lang.String title, java.lang.String subtitle, boolean checked, kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onCheckedChange) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SelectableOptionRow(java.lang.String title, java.lang.String subtitle, androidx.compose.ui.graphics.vector.ImageVector icon, boolean selected, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
    
    @androidx.compose.runtime.Composable()
    private static final void SectionDivider() {
    }
    
    private static final java.lang.String languageLabel(java.lang.String tag) {
        return null;
    }
    
    private static final java.util.Map<java.lang.String, java.lang.String> languageOptions() {
        return null;
    }
}