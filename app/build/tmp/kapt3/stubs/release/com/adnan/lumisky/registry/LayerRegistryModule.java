package com.adnan.lumisky.registry;

@dagger.Module()
@kotlin.Metadata(mv = {2, 2, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u000b\b\u00c7\u0002\u0018\u00002\u00020\u0001B\t\b\u0002\u00a2\u0006\u0004\b\u0002\u0010\u0003J\u0010\u0010\u0004\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\t\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\n\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\u000b\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\f\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\r\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\u000e\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\u000f\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\u0010\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007J\u0010\u0010\u0011\u001a\u00020\u00052\u0006\u0010\u0006\u001a\u00020\u0007H\u0007\u00a8\u0006\u0012"}, d2 = {"Lcom/adnan/lumisky/registry/LayerRegistryModule;", "", "<init>", "()V", "provideShaderLayerFactory", "Lcom/adnan/lumisky/registry/LayerFactory;", "shaderSourceLoader", "Lcom/adnan/lumisky/assets/ShaderSourceLoader;", "provideTextureLayerFactory", "provideVideoOesLayerFactory", "provideCloudLayerFactory", "provideFogLayerFactory", "provideForegroundLayerFactory", "provideMoonLayerFactory", "provideRainLayerFactory", "provideStarsLayerFactory", "provideSunLayerFactory", "provideAnimationLayerFactory", "app_release"})
@dagger.hilt.InstallIn(value = {dagger.hilt.components.SingletonComponent.class})
public final class LayerRegistryModule {
    @org.jetbrains.annotations.NotNull()
    public static final com.adnan.lumisky.registry.LayerRegistryModule INSTANCE = null;
    
    private LayerRegistryModule() {
        super();
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "ShaderLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideShaderLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "TextureLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideTextureLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "VideoOesLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideVideoOesLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "CloudLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideCloudLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "FogLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideFogLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "ForegroundLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideForegroundLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "MoonLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideMoonLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "RainLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideRainLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "StarsLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideStarsLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "SunLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideSunLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
    
    @dagger.Provides()
    @dagger.multibindings.IntoMap()
    @dagger.multibindings.StringKey(value = "AnimationLayer")
    @org.jetbrains.annotations.NotNull()
    public final com.adnan.lumisky.registry.LayerFactory provideAnimationLayerFactory(@org.jetbrains.annotations.NotNull()
    com.adnan.lumisky.assets.ShaderSourceLoader shaderSourceLoader) {
        return null;
    }
}