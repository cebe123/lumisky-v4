package com.adnan.lumisky.core;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&J\b\u0010\u0010\u001a\u00020\u0011H&J\b\u0010\u0012\u001a\u00020\u0013H&\u00a8\u0006\u0014\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/core/EngineEntryPoint;", "", "wallpaperRepository", "Lcom/adnan/lumisky/data/WallpaperRepository;", "lumiskyRenderer", "Lcom/adnan/lumisky/engine/LumiskyRenderer;", "shaderRegistry", "Lcom/adnan/lumisky/registry/ShaderRegistry;", "sensorDispatcher", "Lcom/adnan/lumisky/device/SensorDispatcher;", "sceneFactory", "Lcom/adnan/lumisky/registry/SceneFactory;", "wallpaperColorProvider", "Lcom/adnan/lumisky/assets/WallpaperColorProvider;", "thermalStateController", "Lcom/adnan/lumisky/device/ThermalStateController;", "featureFlagRepository", "Lcom/adnan/lumisky/telemetry/FeatureFlagRepository;", "renderTelemetryLogger", "Lcom/adnan/lumisky/telemetry/RenderTelemetryLogger;", "app_debug"})
@dagger.hilt.EntryPoint()
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract interface EngineEntryPoint {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.data.WallpaperRepository wallpaperRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.LumiskyRenderer lumiskyRenderer();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.registry.ShaderRegistry shaderRegistry();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.device.SensorDispatcher sensorDispatcher();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.registry.SceneFactory sceneFactory();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.assets.WallpaperColorProvider wallpaperColorProvider();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.device.ThermalStateController thermalStateController();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.telemetry.FeatureFlagRepository featureFlagRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.telemetry.RenderTelemetryLogger renderTelemetryLogger();
}