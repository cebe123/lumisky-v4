/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - Layer type string -> LayerFactory map. GL handle tutmaz.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: Layer type string -> LayerFactory map. GL handle tutmaz.
 */
package com.adnan.lumisky.registry

import com.adnan.lumisky.definition.LayerDefinition
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class LayerRegistry @Inject constructor(
    private val factories: Map<String, @JvmSuppressWildcards Provider<LayerFactory>>
) {
    fun create(definition: LayerDefinition, required: Boolean = true): LayerCreateResult {
        val factory = factories[definition.type]?.get()
            ?: return LayerCreateResult.UnknownType(
                layerId = definition.id,
                type = definition.type,
                required = required
            )

        return try {
            LayerCreateResult.Created(factory.create(definition))
        } catch (e: Throwable) {
            LayerCreateResult.CreateFailed(
                layerId = definition.id,
                type = definition.type,
                required = required,
                cause = e
            )
        }
    }
}
