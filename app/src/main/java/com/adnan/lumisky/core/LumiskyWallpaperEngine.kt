/**
 * Lumisky Final Mimari v5 (lumisky_mimari.md)
 * 
 * BU DOSYA NE İŞ YAPIYOR:
 * - WallpaperService.Engine lifecycle olaylarını RuntimeSession ve GL thread’e çeviren bridge katmanı.
 * 
 * HEDEF:
 * - Lumisky v5 mimari standartlarına uygun olarak bu dosyanın temel amacı: WallpaperService.Engine lifecycle olaylarını RuntimeSession ve GL thread’e çeviren bridge katmanı.
 */
package com.adnan.lumisky.core

import android.annotation.TargetApi
import android.app.WallpaperColors
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.adnan.lumisky.data.WallpaperRepository
import com.adnan.lumisky.data.SettingsLocationPlanner
import com.adnan.lumisky.data.RuntimeSettingsPolicy
import com.adnan.lumisky.data.LastSuccessfulSceneState
import com.adnan.lumisky.definition.QualityTier
import com.adnan.lumisky.definition.WallpaperDefinition
import com.adnan.lumisky.engine.LocationDaylightController
import com.adnan.lumisky.device.SensorDispatcher
import com.adnan.lumisky.device.ThermalStateController
import com.adnan.lumisky.engine.LumiskyRenderer
import com.adnan.lumisky.engine.RuntimeScene
import com.adnan.lumisky.registry.SceneFactory
import com.adnan.lumisky.registry.ShaderRegistry
import com.adnan.lumisky.telemetry.FeatureFlagRepository
import com.adnan.lumisky.telemetry.RenderTelemetryEvent
import com.adnan.lumisky.telemetry.RenderTelemetryLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LumiskyWallpaperEngine(
    private val service: LumiskyWallpaperService,
    private val entryPoint: EngineEntryPoint,
    private val notifyColorsChanged: () -> Unit
) : SensorDispatcher.SensorListener {

    private val repository: WallpaperRepository = entryPoint.wallpaperRepository()
    private val renderer: LumiskyRenderer = entryPoint.lumiskyRenderer()
    private val shaderRegistry: ShaderRegistry = entryPoint.shaderRegistry()
    private val sensorDispatcher: SensorDispatcher = entryPoint.sensorDispatcher()
    private val sceneFactory: SceneFactory = entryPoint.sceneFactory()
    private val colorProvider = entryPoint.wallpaperColorProvider()
    private val thermalStateController: ThermalStateController = entryPoint.thermalStateController()
    private val featureFlagRepository: FeatureFlagRepository = entryPoint.featureFlagRepository()
    private val telemetryLogger: RenderTelemetryLogger = entryPoint.renderTelemetryLogger()
    private val powerManager = service.getSystemService(Context.POWER_SERVICE) as? PowerManager
    private val locationDaylightController = LocationDaylightController()
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private var glThread: WallpaperGlThread? = null
    private var surfaceHolder: SurfaceHolder? = null
    private var hasSurface = false
    private var isEngineVisible = false
    private var isSensorRegistered = false
    private var surfaceGeneration = 0L
    private var lastSurfaceWidth = 0
    private var lastSurfaceHeight = 0
    private var lastSuccessfulDefinition: WallpaperDefinition? = null
    private var lastSuccessfulScene: RuntimeScene? = null
    private var sensorParallaxEnabled = true
    private var thermalEmergencyLogged = false

    fun onCreate(surfaceHolder: SurfaceHolder?) {
        this.surfaceHolder = surfaceHolder

        glThread = WallpaperGlThread(service.applicationContext, renderer, shaderRegistry).apply {
            start()
        }

        scope.launch {
            repository.settings.selectedWallpaperId.collectLatest { id ->
                applyDefinition(id)
            }
        }

        scope.launch {
            combine(
                repository.settings.qualityTier,
                repository.settings.performanceMode,
                repository.settings.highRefreshEnabled,
                thermalStateController.thermalStatus,
                featureFlagRepository.flags()
            ) { qualityTier, performanceMode, highRefreshEnabled, thermalStatus, featureFlags ->
                RuntimeSettingsPolicy.resolve(
                    qualityTier = qualityTier,
                    performanceMode = performanceMode,
                    highRefreshEnabled = highRefreshEnabled,
                    batterySaver = isPowerSaveMode(),
                    thermalStatus = thermalStatus,
                    featureFlags = featureFlags
                )
            }.collectLatest { policy ->
                glThread?.preferredQualityTier = policy.qualityTier
                glThread?.maxFps = policy.maxFps
                glThread?.batterySaver = policy.maxFps <= 15
                glThread?.renderScale = policy.renderScale
                glThread?.postProcessEnabled = policy.postProcessEnabled
                glThread?.particleEffectsEnabled = policy.particleEffectsEnabled
                glThread?.videoPlaybackEnabled = policy.videoPlaybackEnabled
                glThread?.sensorParallaxEnabled = policy.sensorParallaxEnabled
                glThread?.telemetryEnabled = policy.telemetryEnabled
                glThread?.thermalEmergency = policy.thermalEmergency
                sensorParallaxEnabled = policy.sensorParallaxEnabled
                updateSensorRegistration(isEngineVisible)
                if (policy.thermalEmergency && !thermalEmergencyLogged && policy.telemetryEnabled) {
                    telemetryLogger.log(
                        RenderTelemetryEvent.ThermalEmergencyDegrade(
                            wallpaperId = lastSuccessfulDefinition?.id.orEmpty().ifBlank { "unknown" },
                            thermalStatus = RuntimeSettingsPolicy.THERMAL_STATUS_SEVERE,
                            appliedSceneMaxFps = policy.maxFps
                        )
                    )
                    thermalEmergencyLogged = true
                } else if (!policy.thermalEmergency) {
                    thermalEmergencyLogged = false
                }
            }
        }

        scope.launch {
            combine(
                repository.settings.locationMode,
                repository.settings.manualLatitude,
                repository.settings.manualLongitude,
                repository.settings.manualTimeZone,
                repository.settings.deviceLocationSnapshot
            ) { mode, latitude, longitude, timeZoneId, deviceSnapshot ->
                val manualLocation = SettingsLocationPlanner.resolveManualLocation(
                    latitude = latitude,
                    longitude = longitude,
                    timeZoneId = timeZoneId
                )
                val resolved = SettingsLocationPlanner.resolve(
                    mode = SettingsLocationPlanner.modeFromStorage(mode),
                    manualLocation = manualLocation,
                    deviceSnapshot = deviceSnapshot,
                    nowEpochMs = System.currentTimeMillis()
                )
                locationDaylightController.resolve(
                    resolved.latitude,
                    resolved.longitude,
                    resolved.timeZoneId
                )
            }.collectLatest { daylight ->
                glThread?.daylightOverride = daylight
            }
        }
    }

    fun onVisibilityChanged(visible: Boolean) {
        isEngineVisible = visible
        glThread?.setVisibility(visible)
        updateSensorRegistration(visible)
    }

    fun onSurfaceCreated(holder: SurfaceHolder) {
        surfaceGeneration += 1
        surfaceHolder = holder
        hasSurface = true
        glThread?.onSurfaceCreated(holder)
        replaySurfaceSizeIfKnown()
    }

    fun onSurfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        surfaceHolder = holder
        hasSurface = true
        lastSurfaceWidth = width
        lastSurfaceHeight = height
        glThread?.onSurfaceChanged(width, height)
    }

    fun onSurfaceDestroyed(holder: SurfaceHolder) {
        surfaceGeneration += 1
        surfaceHolder = null
        hasSurface = false
        glThread?.onSurfaceDestroyed()
    }

    fun onTouchEvent(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_DOWN || event.actionMasked == MotionEvent.ACTION_MOVE) {
            glThread?.touchX = event.x
            glThread?.touchY = event.y
            glThread?.hasTouch = true
            glThread?.postEvent(WallpaperEvent.Touch(event.x, event.y))
        } else if (event.actionMasked == MotionEvent.ACTION_UP || event.actionMasked == MotionEvent.ACTION_CANCEL) {
            glThread?.hasTouch = false
        }
    }

    fun onOffsetsChanged(
        xOffset: Float,
        yOffset: Float,
        xOffsetStep: Float,
        yOffsetStep: Float,
        xPixelOffset: Int,
        yPixelOffset: Int
    ) {
        glThread?.postEvent(WallpaperEvent.ParallaxChanged(xOffset, yOffset))
    }

    override fun onSensorValues(x: Float, y: Float) {
        glThread?.parallaxX = x
        glThread?.parallaxY = y
        glThread?.postEvent(WallpaperEvent.ParallaxChanged(x, y))
    }

    fun onComputeColors(): WallpaperColors? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) return null
        val definition = renderer.activeDefinition
            ?: lastSuccessfulDefinition
            ?: runBlocking { repository.getDefinition(FALLBACK_WALLPAPER_ID) }
            ?: return null
        return colorProvider.getColors(definition)
    }

    fun onDestroy() {
        updateSensorRegistration(false)
        scope.cancel()
        glThread?.quitSafely()
        glThread = null
        surfaceHolder = null
        hasSurface = false
    }

    private var lastAppliedId: String? = null

    private suspend fun applyDefinition(selectedId: String) {
        val selectedDefinition = repository.getDefinition(selectedId)
        val definition = selectedDefinition
            ?: repository.getDefinition(FALLBACK_WALLPAPER_ID)
            ?: lastSuccessfulDefinition
            ?: return

        val applyAction = WallpaperApplyPolicy.resolve(
            hasEngineSurface = hasSurface && surfaceHolder != null,
            hasGlSurface = glThread?.hasSurface == true,
            sameWallpaperAlreadyApplied = definition.id == lastAppliedId && lastSuccessfulScene != null
        )
        if (applyAction == WallpaperApplyAction.SKIP) {
            return
        }

        val scene = createScene(definition) ?: lastSuccessfulScene ?: return
        lastSuccessfulDefinition = definition
        lastSuccessfulScene = scene
        lastAppliedId = definition.id
        scope.launch {
            repository.settings.markLastSuccessfulScene(
                LastSuccessfulSceneState(
                    wallpaperId = definition.id,
                    definitionVersion = definition.schemaVersion,
                    qualityTier = glThread?.preferredQualityTier ?: QualityTier.BALANCED,
                    timestampMillis = System.currentTimeMillis()
                )
            )
        }

        val holder = surfaceHolder
        when (applyAction) {
            WallpaperApplyAction.SKIP -> Unit
            WallpaperApplyAction.SWITCH_SCENE -> {
                glThread?.switchScene(definition, scene)
                glThread?.triggerLiveCatchUp()
            }
            WallpaperApplyAction.CREATE_SURFACE -> {
                renderer.activeDefinition = definition
                renderer.activeScene = scene
                if (holder != null) {
                    glThread?.onSurfaceCreated(holder)
                    replaySurfaceSizeIfKnown()
                    glThread?.triggerLiveCatchUp()
                }
            }
            WallpaperApplyAction.STORE_PENDING_SCENE -> {
                renderer.activeDefinition = definition
                renderer.activeScene = scene
            }
        }
        notifyColorsChangedIfAvailable()
    }

    private fun createScene(definition: WallpaperDefinition): RuntimeScene? {
        return try {
            sceneFactory.create(definition)
        } catch (e: Throwable) {
            null
        }
    }

    private fun replaySurfaceSizeIfKnown() {
        if (lastSurfaceWidth > 0 && lastSurfaceHeight > 0) {
            glThread?.onSurfaceChanged(lastSurfaceWidth, lastSurfaceHeight)
        }
    }

    private fun updateSensorRegistration(shouldRegister: Boolean) {
        val shouldUseSensor = shouldRegister && sensorParallaxEnabled
        if (shouldUseSensor && !isSensorRegistered) {
            sensorDispatcher.registerListener(this)
            isSensorRegistered = true
        } else if (!shouldUseSensor && isSensorRegistered) {
            sensorDispatcher.unregisterListener(this)
            isSensorRegistered = false
        }
    }

    private fun isPowerSaveMode(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager?.isPowerSaveMode == true
        } else {
            false
        }
    }

    @TargetApi(Build.VERSION_CODES.O_MR1)
    private fun notifyColorsChangedIfAvailable() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            notifyColorsChanged.invoke()
        }
    }

    private companion object {
        const val FALLBACK_WALLPAPER_ID = "starter_gradient"
    }
}
