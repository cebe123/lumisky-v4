package com.adnan.lumisky.data;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003JZ\u0010\u0006\u001a\u00020\u00072\u0006\u0010\b\u001a\u00020\t2\u0006\u0010\n\u001a\u00020\t2\u0006\u0010\u000b\u001a\u00020\f2\b\b\u0002\u0010\r\u001a\u00020\f2\b\b\u0002\u0010\u000e\u001a\u00020\u00052\b\b\u0002\u0010\u000f\u001a\u00020\f2\b\b\u0002\u0010\u0010\u001a\u00020\u00052\b\b\u0002\u0010\u0011\u001a\u00020\u00052\b\b\u0002\u0010\u0012\u001a\u00020\u0013J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\tH\u0002R\u000e\u0010\u0004\u001a\u00020\u0005X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0017"}, d2 = {"Lcom/adnan/lumisky/data/RuntimeSettingsPolicy;", "", "<init>", "()V", "THERMAL_STATUS_SEVERE", "", "resolve", "Lcom/adnan/lumisky/data/RuntimeSettingsPolicyResult;", "qualityTier", "", "performanceMode", "highRefreshEnabled", "", "batterySaver", "thermalStatus", "ambientMode", "sceneMaxFps", "batterySaverSceneMaxFps", "featureFlags", "Lcom/adnan/lumisky/engine/RenderFeatureFlags;", "parseQualityTier", "Lcom/adnan/lumisky/definition/QualityTier;", "value", "app_release"})
public final class RuntimeSettingsPolicy {
    public static final int THERMAL_STATUS_SEVERE = 3;
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.data.RuntimeSettingsPolicy INSTANCE = null;
    
    private RuntimeSettingsPolicy() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.data.RuntimeSettingsPolicyResult resolve(@org.jetbrains.annotations.NotNull()
    java.lang.String qualityTier, @org.jetbrains.annotations.NotNull()
    java.lang.String performanceMode, boolean highRefreshEnabled, boolean batterySaver, int thermalStatus, boolean ambientMode, int sceneMaxFps, int batterySaverSceneMaxFps, @org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.engine.RenderFeatureFlags featureFlags) {
        return null;
    }
    
    private final com.adnan.lumisky.definition.QualityTier parseQualityTier(java.lang.String value) {
        return null;
    }
}