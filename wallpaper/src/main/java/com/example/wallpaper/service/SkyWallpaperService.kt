package com.example.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.engine.config.WallpaperConfigStore
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher

open class SkyWallpaperService : WallpaperService() {

	override fun onCreateEngine(): Engine {
		return SkyWallpaperEngine()
	}

	private inner class SkyWallpaperEngine : Engine() {
		private val configStore = WallpaperConfigStore(this@SkyWallpaperService.applicationContext)
		private val renderController = WallpaperRenderController(
			renderEngine = WallpaperRenderEngine(this@SkyWallpaperService.applicationContext),
			scheduler = MinuteTickScheduler(),
			hasher = SceneStateHasher()
		)
		private var configRefreshReceiverRegistered: Boolean = false
		private var lastAppliedConfigSignature: String? = null
		private val configRefreshReceiver = object : BroadcastReceiver() {
			override fun onReceive(context: Context?, intent: Intent?) {
				if (intent?.action != ACTION_APPLY_STORED_WALLPAPER_CONFIG) return
				Logger.d("SkyWallpaperService", "applyStoredConfig broadcast received")
				applyStoredConfig()
			}
		}

		override fun onCreate(surfaceHolder: SurfaceHolder) {
			super.onCreate(surfaceHolder)
			Logger.d("SkyWallpaperService", "Engine.onCreate")
			registerConfigRefreshReceiver()
			renderController.setPreviewMode(isPreview)
			applyStoredConfig()
			renderController.onCreate()
		}

		override fun onVisibilityChanged(visible: Boolean) {
			super.onVisibilityChanged(visible)
			renderController.setPreviewMode(isPreview)
			if (visible) {
				applyStoredConfig()
			}
			renderController.onVisibilityChanged(visible)
		}

		override fun onSurfaceCreated(holder: SurfaceHolder) {
			super.onSurfaceCreated(holder)
			renderController.setPreviewMode(isPreview)
			applyStoredConfig()
			renderController.onSurfaceCreated(holder)
		}

		override fun onSurfaceDestroyed(holder: SurfaceHolder) {
			renderController.onSurfaceDestroyed()
			super.onSurfaceDestroyed(holder)
		}

		override fun onDestroy() {
			unregisterConfigRefreshReceiver()
			renderController.onDestroy()
			super.onDestroy()
		}

		private fun applyStoredConfig() {
			configStore.loadSelected()?.let { config ->
				val signature = buildConfigSignature(
					id = config.id,
					isPreview = isPreview,
					sunrise = config.daylight.sunriseMinute,
					sunset = config.daylight.sunsetMinute
				)
				if (lastAppliedConfigSignature == signature) {
					Logger.d("SkyWallpaperService", "apply_stored_config dedupe skip signature=$signature")
					return
				}
				lastAppliedConfigSignature = signature
				Logger.event(
					"SkyWallpaperService",
					"apply_stored_config",
					"id" to config.id,
					"isPreview" to isPreview,
					"sunrise" to config.daylight.sunriseMinute,
					"sunset" to config.daylight.sunsetMinute
				)
				renderController.setConfig(config)
			} ?: run {
				Logger.w("SkyWallpaperService", "apply_stored_config skipped: no saved config")
			}
		}

		private fun buildConfigSignature(
			id: String,
			isPreview: Boolean,
			sunrise: Int,
			sunset: Int
		): String {
			return "$id|$isPreview|$sunrise|$sunset"
		}

		private fun registerConfigRefreshReceiver() {
			if (configRefreshReceiverRegistered) return
			val filter = IntentFilter(ACTION_APPLY_STORED_WALLPAPER_CONFIG)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
				registerReceiver(configRefreshReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
			} else {
				@Suppress("DEPRECATION")
				registerReceiver(configRefreshReceiver, filter)
			}
			configRefreshReceiverRegistered = true
		}

		private fun unregisterConfigRefreshReceiver() {
			if (!configRefreshReceiverRegistered) return
			runCatching { unregisterReceiver(configRefreshReceiver) }
			configRefreshReceiverRegistered = false
		}
	}
}
