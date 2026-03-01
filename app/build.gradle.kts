import org.gradle.api.GradleException
import org.gradle.api.tasks.Sync
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO

buildscript {
	repositories {
		google()
		mavenCentral()
	}
	dependencies {
		classpath("org.sejda.imageio:webp-imageio:0.1.6")
	}
}

plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.compose)
}

val lintIncludeTestSources = providers.gradleProperty("lumisky.lint.includeTestSources")
	.map { raw -> raw.equals("true", ignoreCase = true) }
	.orElse(false)

android {
	namespace = "com.example.lumisky"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		applicationId = "com.example.lumisky"
		minSdk = 28
		targetSdk = 36
		versionCode = 1
		versionName = "1.0"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
	}
	lint {
		// Fast local lint by default; enable test-source lint in CI/full runs with:
		// -Plumisky.lint.includeTestSources=true
		checkTestSources = lintIncludeTestSources.get()
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.appcompat)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.compose.ui)
	implementation(libs.androidx.compose.ui.graphics)
	implementation("androidx.compose.animation:animation")
	implementation(libs.androidx.compose.material3)
	implementation(libs.material)
	implementation("androidx.compose.material:material-icons-extended")

	implementation(project(":core"))
	implementation(project(":engine"))
	implementation(project(":wallpaper"))
}

tasks.register("lintDebugLocal") {
	group = "verification"
	description = "Runs :app:lintDebug with test-source lint disabled by default."
	dependsOn("lintDebug")
}

tasks.register("lintDebugFull") {
	group = "verification"
	description = "Runs :app:lintDebug with test-source lint enabled when invoked with -Plumisky.lint.includeTestSources=true."
	dependsOn("lintDebug")
}

val convertWallpaperTexturesToWebp by tasks.registering {
	group = "assets"
	description = "Converts added wallpaper textures in assets to WebP."

	val sourceAssetsDir = layout.projectDirectory.dir("src/main/assets").asFile
	val convertedTexturesDir = layout.buildDirectory.dir("generated/convertedWallpaperTextures/main").get().asFile
	val sourceTree = fileTree(sourceAssetsDir) {
		include("**/*.png", "**/*.jpg", "**/*.jpeg")
		exclude("**/shaders/**")
	}

	inputs.files(sourceTree)
	outputs.dir(convertedTexturesDir)

	doLast {
		val files = sourceTree.files.sortedBy { it.absolutePath.lowercase() }
		if (convertedTexturesDir.exists()) {
			convertedTexturesDir.deleteRecursively()
		}
		convertedTexturesDir.mkdirs()
		if (files.isEmpty()) {
			logger.lifecycle("convertWallpaperTexturesToWebp: no source textures found.")
			return@doLast
		}

		var converted = 0
		var skipped = 0
		var failed = 0

		files.forEach { source ->
			val relativePath = source.relativeTo(sourceAssetsDir).invariantSeparatorsPath
			val relativeTargetPath = relativePath.substringBeforeLast('.') + ".webp"
			val target = File(convertedTexturesDir, relativeTargetPath)

			val image = runCatching { ImageIO.read(source) }.getOrNull()
			if (image == null) {
				logger.warn("convertWallpaperTexturesToWebp: failed to read ${source.path}")
				failed += 1
				return@forEach
			}

			val writer = ImageIO.getImageWritersByMIMEType("image/webp").asSequence().firstOrNull()
				?: ImageIO.getImageWritersBySuffix("webp").asSequence().firstOrNull()
			if (writer == null) {
				throw GradleException(
					"WebP writer not found. Ensure org.sejda.imageio:webp-imageio is available."
				)
			}

			runCatching {
				target.parentFile?.mkdirs()
				ImageIO.createImageOutputStream(target).use { output ->
					writer.output = output
					val params = writer.defaultWriteParam.apply {
						if (canWriteCompressed()) {
							compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
							compressionTypes?.firstOrNull()?.let { type ->
								compressionType = type
							}
							compressionQuality = 0.90f
						}
					}
					writer.write(null, IIOImage(image, null, null), params)
					output.flush()
				}
				target.setLastModified(source.lastModified())
			}.onSuccess {
				if (target.length() <= 0L) {
					target.delete()
					logger.warn("convertWallpaperTexturesToWebp: empty output ${target.path}")
					failed += 1
				} else {
					converted += 1
				}
			}.onFailure { error ->
				logger.warn("convertWallpaperTexturesToWebp: failed ${source.path} -> ${target.path}", error)
				failed += 1
			}.also {
				writer.dispose()
			}
		}

		logger.lifecycle(
			"convertWallpaperTexturesToWebp: converted=$converted skipped=$skipped failed=$failed"
		)
		if (failed > 0) {
			throw GradleException("WebP conversion failed for $failed texture(s).")
		}
	}
}

val filteredAssetsDir = layout.buildDirectory.dir("generated/filteredAssets/main").get().asFile
val prepareFilteredAssets by tasks.registering(Sync::class) {
	group = "assets"
	description = "Copies app assets for packaging without source raster textures."
	dependsOn(convertWallpaperTexturesToWebp)
	// Prefer checked-in .webp assets when both source and generated outputs exist.
	duplicatesStrategy = org.gradle.api.file.DuplicatesStrategy.EXCLUDE
	from(layout.projectDirectory.dir("src/main/assets")) {
		exclude("**/*.png", "**/*.jpg", "**/*.jpeg")
	}
	from(layout.buildDirectory.dir("generated/convertedWallpaperTextures/main"))
	into(filteredAssetsDir)
}

android.sourceSets.getByName("main").assets.setSrcDirs(listOf(filteredAssetsDir))

tasks.matching { task ->
	task.name.startsWith("merge") && task.name.endsWith("Assets")
}.configureEach {
	dependsOn(prepareFilteredAssets)
}

tasks.matching { task ->
	task.name.startsWith("generate") &&
		task.name.contains("Lint") &&
		task.name.endsWith("Model")
}.configureEach {
	dependsOn(prepareFilteredAssets)
}

tasks.matching { task ->
	task.name.startsWith("lint")
}.configureEach {
	dependsOn(prepareFilteredAssets)
}
