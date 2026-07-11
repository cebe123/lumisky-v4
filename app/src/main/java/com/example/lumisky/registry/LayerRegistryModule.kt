/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Lumisky v5 Registry katmanı bileşeni.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Lumisky v5 Registry katmanı bileşeni.
 */
package com.example.lumisky.registry

import com.example.lumisky.assets.ShaderSourceLoader
import com.example.lumisky.definition.LayerDefinition
import com.example.lumisky.layers.*
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import dagger.multibindings.StringKey
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LayerRegistryModule {

    @Provides
    @IntoMap
    @StringKey("ShaderLayer")
    fun provideShaderLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = ShaderLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("TextureLayer")
    fun provideTextureLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = TextureLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("VideoOesLayer")
    fun provideVideoOesLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = VideoOesLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("CloudLayer")
    fun provideCloudLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = CloudLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("FogLayer")
    fun provideFogLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = FogLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("ForegroundLayer")
    fun provideForegroundLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = ForegroundLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("MoonLayer")
    fun provideMoonLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = MoonLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("RainLayer")
    fun provideRainLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = RainLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("StarsLayer")
    fun provideStarsLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = StarsLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("SunLayer")
    fun provideSunLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = SunLayer(definition, shaderSourceLoader)
    }

    @Provides
    @IntoMap
    @StringKey("AnimationLayer")
    fun provideAnimationLayerFactory(shaderSourceLoader: ShaderSourceLoader): LayerFactory = object : LayerFactory {
        override fun create(definition: LayerDefinition): RenderLayer = AnimationLayer(definition, shaderSourceLoader)
    }
}
