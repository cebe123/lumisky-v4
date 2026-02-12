package com.example.wallpaper.service

import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import com.example.core.Logger
import com.example.wallpaper.engine.WallpaperRenderEngine
import com.example.wallpaper.render.SceneStateHasher

open class SkyWallpaperService : WallpaperService() {

	override fun onCreateEngine(): Engine {
		return SkyWallpaperEngine()
	}

	private inner class SkyWallpaperEngine : Engine() {
		private val renderController = WallpaperRenderController(
			renderEngine = WallpaperRenderEngine(this@SkyWallpaperService.applicationContext),
			scheduler = MinuteTickScheduler(),
			hasher = SceneStateHasher()
		)

		override fun onCreate(surfaceHolder: SurfaceHolder) {
			super.onCreate(surfaceHolder)
			Logger.d("SkyWallpaperService", "Engine.onCreate")
			renderController.onCreate()
		}

		override fun onVisibilityChanged(visible: Boolean) {
			super.onVisibilityChanged(visible)
			renderController.onVisibilityChanged(visible)
		}

		override fun onSurfaceCreated(holder: SurfaceHolder) {
			super.onSurfaceCreated(holder)
			renderController.onSurfaceCreated(holder)
		}

		override fun onSurfaceDestroyed(holder: SurfaceHolder) {
			renderController.onSurfaceDestroyed()
			super.onSurfaceDestroyed(holder)
		}

		override fun onDestroy() {
			renderController.onDestroy()
			super.onDestroy()
		}
	}
}
