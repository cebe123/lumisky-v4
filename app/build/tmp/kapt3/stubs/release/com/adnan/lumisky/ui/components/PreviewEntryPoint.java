package com.adnan.lumisky.ui.components;

@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\bg\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\b\u0010\u0004\u001a\u00020\u0005H&J\b\u0010\u0006\u001a\u00020\u0007H&J\b\u0010\b\u001a\u00020\tH&J\b\u0010\n\u001a\u00020\u000bH&J\b\u0010\f\u001a\u00020\rH&J\b\u0010\u000e\u001a\u00020\u000fH&J\b\u0010\u0010\u001a\u00020\u0011H&J\b\u0010\u0012\u001a\u00020\u0013H&J\b\u0010\u0014\u001a\u00020\u0015H&J\b\u0010\u0016\u001a\u00020\u0017H&J\b\u0010\u0018\u001a\u00020\u0019H&\u00a8\u0006\u001a\u00c0\u0006\u0003"}, d2 = {"Lcom/adnan/lumisky/ui/components/PreviewEntryPoint;", "", "wallpaperRepository", "Lcom/adnan/lumisky/data/WallpaperRepository;", "shaderRegistry", "Lcom/adnan/lumisky/registry/ShaderRegistry;", "sceneFactory", "Lcom/adnan/lumisky/registry/SceneFactory;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "sceneScheduler", "Lcom/adnan/lumisky/engine/SceneScheduler;", "eventTriggerSystem", "Lcom/adnan/lumisky/engine/EventTriggerSystem;", "atmosphereController", "Lcom/adnan/lumisky/engine/AtmosphereController;", "parallaxController", "Lcom/adnan/lumisky/engine/ParallaxController;", "adaptiveQualityController", "Lcom/adnan/lumisky/engine/AdaptiveQualityController;", "settingsRepository", "Lcom/adnan/lumisky/data/SettingsRepository;", "thermalStateController", "Lcom/adnan/lumisky/device/ThermalStateController;", "sensorDispatcher", "Lcom/adnan/lumisky/device/SensorDispatcher;", "app_release"})
@dagger.hilt.EntryPoint()
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public abstract interface PreviewEntryPoint {
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.data.WallpaperRepository wallpaperRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.registry.ShaderRegistry shaderRegistry();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.registry.SceneFactory sceneFactory();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.SceneScheduler sceneScheduler();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.EventTriggerSystem eventTriggerSystem();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.AtmosphereController atmosphereController();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.ParallaxController parallaxController();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.engine.AdaptiveQualityController adaptiveQualityController();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.data.SettingsRepository settingsRepository();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.device.ThermalStateController thermalStateController();
    
    @org.jetbrains.annotations.NotNull()
    public abstract com.adnan.lumisky.device.SensorDispatcher sensorDispatcher();
}