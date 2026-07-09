package com.adnan.lumisky.ui.settings;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0010\u0006\n\u0002\b\u0006\n\u0002\u0010\u000b\n\u0002\b\n\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0011\b\u0007\u0018\u00002\u00020\u0001B\u0019\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0004\b\u0006\u0010\u0007J\u000e\u0010&\u001a\u00020\'2\u0006\u0010(\u001a\u00020\nJ\u000e\u0010)\u001a\u00020\'2\u0006\u0010*\u001a\u00020\nJ\u001e\u0010+\u001a\u00020\'2\u0006\u0010,\u001a\u00020\u00122\u0006\u0010-\u001a\u00020\u00122\u0006\u0010.\u001a\u00020\nJ\u000e\u0010/\u001a\u00020\'2\u0006\u00100\u001a\u00020\u0019J\u000e\u00101\u001a\u00020\'2\u0006\u00100\u001a\u00020\u0019J\u000e\u00102\u001a\u00020\'2\u0006\u0010*\u001a\u00020\nJ\u000e\u00103\u001a\u00020\'2\u0006\u0010*\u001a\u00020\nJ\u000e\u00104\u001a\u00020\'2\u0006\u00105\u001a\u00020\nJ\u0006\u00106\u001a\u00020\'J\u0006\u00107\u001a\u00020\u0019R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0017\u0010\r\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u0017\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\fR\u0017\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\fR\u0017\u0010\u0014\u001a\b\u0012\u0004\u0012\u00020\u00120\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0015\u0010\fR\u0017\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0017\u0010\fR\u0017\u0010\u0018\u001a\b\u0012\u0004\u0012\u00020\u00190\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001a\u0010\fR\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00190\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001c\u0010\fR\u0017\u0010\u001d\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\fR\u0017\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\fR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\n0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b\"\u0010\fR\u0019\u0010#\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010$0\t\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\f\u00a8\u00068"}, d2 = {"Lcom/adnan/lumisky/ui/settings/SettingsViewModel;", "Landroidx/lifecycle/ViewModel;", "settingsRepository", "Lcom/adnan/lumisky/data/SettingsRepository;", "deviceLocationProvider", "Lcom/adnan/lumisky/device/DeviceLocationProvider;", "<init>", "(Lcom/adnan/lumisky/data/SettingsRepository;Lcom/adnan/lumisky/device/DeviceLocationProvider;)V", "selectedWallpaperId", "Lkotlinx/coroutines/flow/StateFlow;", "", "getSelectedWallpaperId", "()Lkotlinx/coroutines/flow/StateFlow;", "qualityTier", "getQualityTier", "locationMode", "getLocationMode", "manualLatitude", "", "getManualLatitude", "manualLongitude", "getManualLongitude", "manualTimeZone", "getManualTimeZone", "previewTimeSimulation", "", "getPreviewTimeSimulation", "highRefreshEnabled", "getHighRefreshEnabled", "performanceMode", "getPerformanceMode", "appThemeMode", "getAppThemeMode", "languageTag", "getLanguageTag", "deviceLocationSnapshot", "Lcom/adnan/lumisky/data/DeviceLocationSnapshot;", "getDeviceLocationSnapshot", "setQualityTier", "", "tier", "setLocationMode", "mode", "setManualLocation", "latitude", "longitude", "timeZoneId", "setPreviewTimeSimulation", "enabled", "setHighRefreshEnabled", "setPerformanceMode", "setAppThemeMode", "setLanguageTag", "tag", "refreshDeviceLocation", "isDeviceLocationAvailable", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel()
public final class SettingsViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.data.SettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull()
    private final com.adnan.lumisky.device.DeviceLocationProvider deviceLocationProvider = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> selectedWallpaperId = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> qualityTier = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> locationMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> manualLatitude = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Double> manualLongitude = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> manualTimeZone = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> previewTimeSimulation = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> highRefreshEnabled = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> performanceMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> appThemeMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> languageTag = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.adnan.lumisky.data.DeviceLocationSnapshot> deviceLocationSnapshot = null;
    
    @javax.inject.Inject()
    public SettingsViewModel(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.data.SettingsRepository settingsRepository, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.device.DeviceLocationProvider deviceLocationProvider) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSelectedWallpaperId() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getQualityTier() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLocationMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Double> getManualLatitude() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Double> getManualLongitude() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getManualTimeZone() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getPreviewTimeSimulation() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getHighRefreshEnabled() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getPerformanceMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getAppThemeMode() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getLanguageTag() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.adnan.lumisky.data.DeviceLocationSnapshot> getDeviceLocationSnapshot() {
        return null;
    }
    
    public final void setQualityTier(@org.jetbrains.annotations.NotNull()
    java.lang.String tier) {
    }
    
    public final void setLocationMode(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void setManualLocation(double latitude, double longitude, @org.jetbrains.annotations.NotNull()
    java.lang.String timeZoneId) {
    }
    
    public final void setPreviewTimeSimulation(boolean enabled) {
    }
    
    public final void setHighRefreshEnabled(boolean enabled) {
    }
    
    public final void setPerformanceMode(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void setAppThemeMode(@org.jetbrains.annotations.NotNull()
    java.lang.String mode) {
    }
    
    public final void setLanguageTag(@org.jetbrains.annotations.NotNull()
    java.lang.String tag) {
    }
    
    public final void refreshDeviceLocation() {
    }
    
    public final boolean isDeviceLocationAvailable() {
        return false;
    }
}