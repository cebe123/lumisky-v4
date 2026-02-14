import org.gradle.api.GradleException
import org.gradle.api.tasks.Sync
import java.io.File
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriter

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

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	buildFeatures {
		compose = true
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
	implementation(libs.androidx.compose.ui.tooling.preview)
	implementation(libs.androidx.compose.material3)
	implementation("androidx.compose.material:material-icons-extended")
	testImplementation(libs.junit)
	androidTestImplementation(libs.androidx.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.compose.ui.test.junit4)
	debugImplementation(libs.androidx.compose.ui.tooling)
	debugImplementation(libs.androidx.compose.ui.test.manifest)

	implementation(project(":core"))
	implementation(project(":engine"))
	implementation(project(":wallpaper"))
}

val convertWallpaperTexturesToWebp by tasks.registering {
	group = "assets"
	description = "Converts added wallpaper textures in assets to WebP."

	val assetsDir = layout.projectDirectory.dir("src/main/assets")
	val sourceTree = fileTree(assetsDir) {
		include("**/*.png", "**/*.jpg", "**/*.jpeg")
		exclude("**/shaders/**")
	}

	inputs.files(sourceTree)
	outputs.dir(assetsDir)

	doLast {
		val files = sourceTree.files.sortedBy { it.absolutePath.lowercase() }
		if (files.isEmpty()) {
			logger.lifecycle("convertWallpaperTexturesToWebp: no source textures found.")
			return@doLast
		}

		var converted = 0
		var skipped = 0
		var failed = 0

		files.forEach { source ->
			val target = File(source.parentFile, "${source.nameWithoutExtension}.webp")
			if (target.exists() && target.length() > 0L && target.lastModified() >= source.lastModified()) {
				skipped += 1
				return@forEach
			}

			val image = runCatching { ImageIO.read(source) }.getOrNull()
			if (image == null) {
				logger.warn("convertWallpaperTexturesToWebp: failed to read ${source.path}")
				failed += 1
				return@forEach
			}

			val writer = findWebpWriter()
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
	from(layout.projectDirectory.dir("src/main/assets")) {
		exclude("**/*.png", "**/*.jpg", "**/*.jpeg")
	}
	into(filteredAssetsDir)
}

android.sourceSets.getByName("main").assets.setSrcDirs(listOf(filteredAssetsDir))

tasks.named("preBuild") {
	dependsOn(prepareFilteredAssets)
}

fun findWebpWriter(): ImageWriter? {
	return ImageIO.getImageWritersByMIMEType("image/webp").asSequence().firstOrNull()
		?: ImageIO.getImageWritersBySuffix("webp").asSequence().firstOrNull()
}
