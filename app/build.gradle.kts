import org.gradle.api.GradleException
import org.gradle.api.tasks.Sync
import java.io.File
import java.util.Properties
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
val appId = "com.example.lumisky"
val appProjectDir = project.projectDir
val rootProjectDir = rootProject.projectDir
val phoneRunPreferencesFile = rootProject.layout.projectDirectory
	.file(".codex-local/phone-runner.properties")
	.asFile

android {
	namespace = "com.example.lumisky"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		applicationId = appId
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
	testImplementation(libs.junit)

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

val validateShaderCelestialMotionContinuity by tasks.registering {
	group = "verification"
	description = "Fails the build when a shader reintroduces noon-only celestial position locks that cause visible jumps."

	val shaderFiles = fileTree(layout.projectDirectory.dir("src/main/assets/shaders")) {
		include("**/*.glsl")
	}

	inputs.files(shaderFiles)

	doLast {
		val disallowedPatterns = listOf(
			Regex("""\bsunZenithLock\b""") to "sunZenithLock marker",
			Regex("""abs\s*\(\s*u_Minute\s*-\s*u_SolarNoon\s*\)""") to "minute-to-solarNoon lock window"
		)
		val violations = buildList {
			shaderFiles.files
				.sortedBy { it.invariantSeparatorsPath.lowercase() }
				.forEach { shaderFile ->
					val shaderSource = shaderFile.readText()
					disallowedPatterns.forEach { (pattern, reason) ->
						if (pattern.containsMatchIn(shaderSource)) {
							add("${shaderFile.relativeTo(projectDir).invariantSeparatorsPath}: $reason")
						}
					}
				}
		}
		if (violations.isNotEmpty()) {
			throw GradleException(
				buildString {
					appendLine("Detected discontinuous celestial-motion shader logic:")
					violations.forEach { violation -> appendLine("- $violation") }
				}
			)
		}
	}
}

val syncZenithPreviewAssets by tasks.registering {
	group = "assets"
	description = "Normalizes local zenith snapshot PNGs and converts them to WebP preview assets."

	val rawSnapshotsDir = rootProject.layout.projectDirectory.dir("snapshot-output/zenith-snapshots").asFile
	val normalizedPngDir = rootProject.layout.projectDirectory.dir("snapshot-output/zenith-png").asFile
	val previewAssetsDir = layout.projectDirectory.dir("src/main/assets/previews/zenith").asFile
	val sourceTree = fileTree(rawSnapshotsDir) {
		include("**/*.png")
	}

	inputs.files(sourceTree)
	outputs.dir(normalizedPngDir)
	outputs.dir(previewAssetsDir)

	doLast {
		if (!rawSnapshotsDir.exists()) {
			throw GradleException(
				"Zenith snapshot source not found at ${rawSnapshotsDir.path}. Run GenerateWallpaperZenithSnapshots.cmd first."
			)
		}

		val files = sourceTree.files.sortedBy { it.absolutePath.lowercase() }
		if (files.isEmpty()) {
			throw GradleException(
				"No zenith snapshot PNG files found at ${rawSnapshotsDir.path}."
			)
		}

		if (normalizedPngDir.exists() && !normalizedPngDir.deleteRecursively()) {
			throw GradleException("Failed to clear ${normalizedPngDir.path}.")
		}
		if (!normalizedPngDir.exists() && !normalizedPngDir.mkdirs()) {
			throw GradleException("Failed to create ${normalizedPngDir.path}.")
		}

		if (previewAssetsDir.exists() && !previewAssetsDir.deleteRecursively()) {
			throw GradleException("Failed to clear ${previewAssetsDir.path}.")
		}
		if (!previewAssetsDir.exists() && !previewAssetsDir.mkdirs()) {
			throw GradleException("Failed to create ${previewAssetsDir.path}.")
		}

		val normalizedIds = HashSet<String>(files.size)
		var converted = 0
		var failed = 0

		files.forEach { source ->
			val normalizedId = source.nameWithoutExtension
				.replace(Regex("^\\d+-"), "")
				.replace(Regex("-zenith$"), "")
				.trim()
			if (normalizedId.isBlank()) {
				logger.warn("syncZenithPreviewAssets: could not normalize ${source.name}")
				failed += 1
				return@forEach
			}
			if (!normalizedIds.add(normalizedId)) {
				logger.warn("syncZenithPreviewAssets: duplicate normalized id $normalizedId")
				failed += 1
				return@forEach
			}

			val normalizedPng = File(normalizedPngDir, "$normalizedId.png")
			runCatching {
				if (normalizedPng.exists() && !normalizedPng.delete()) {
					throw GradleException("Failed to replace ${normalizedPng.path}.")
				}
				source.copyTo(normalizedPng, overwrite = true)
				normalizedPng.setLastModified(source.lastModified())
			}.onFailure { error ->
				logger.warn("syncZenithPreviewAssets: failed to copy ${source.path}", error)
				failed += 1
				return@forEach
			}

			val target = File(previewAssetsDir, "$normalizedId.webp")
			if (target.exists() && !target.delete()) {
				logger.warn("syncZenithPreviewAssets: failed to replace ${target.path}")
				failed += 1
				return@forEach
			}
			val image = runCatching { ImageIO.read(source) }.getOrNull()
			if (image == null) {
				logger.warn("syncZenithPreviewAssets: failed to read ${source.path}")
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
				ImageIO.createImageOutputStream(target).use { output ->
					writer.output = output
					val params = writer.defaultWriteParam.apply {
						if (canWriteCompressed()) {
							compressionMode = javax.imageio.ImageWriteParam.MODE_EXPLICIT
							compressionTypes?.firstOrNull()?.let { type ->
								compressionType = type
							}
							compressionQuality = 0.92f
						}
					}
					writer.write(null, IIOImage(image, null, null), params)
					output.flush()
				}
				target.setLastModified(source.lastModified())
			}.onSuccess {
				if (target.length() <= 0L) {
					target.delete()
					logger.warn("syncZenithPreviewAssets: empty output ${target.path}")
					failed += 1
				} else {
					converted += 1
				}
			}.onFailure { error ->
				logger.warn("syncZenithPreviewAssets: failed ${source.path} -> ${target.path}", error)
				failed += 1
			}.also {
				writer.dispose()
			}
		}

		logger.lifecycle("syncZenithPreviewAssets: converted=$converted failed=$failed")
		if (failed > 0) {
			throw GradleException("Zenith snapshot sync failed for $failed file(s).")
		}
	}
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
	dependsOn(validateShaderCelestialMotionContinuity)
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

tasks.register("deployDebugToConnectedDevice") {
	group = "deployment"
	description = "Assembles, installs, and launches the debug build on a connected device. Prefers a physical phone over an emulator."
	dependsOn("assembleDebug")
	notCompatibleWithConfigurationCache("Uses adb-driven deployment against a live device.")

	doLast {
		val adb = resolveAdbExecutable(rootProjectDir)
		val preferences = loadPhoneRunPreferences(phoneRunPreferencesFile)
		val selectedSerial = selectPreferredDeviceSerial(
			workingDir = appProjectDir,
			adb = adb,
			preferredSerial = preferences.preferredSerial
		)
			?: throw GradleException(
				"No connected Android device found. Connect a phone with USB debugging enabled or start an emulator."
			)
		val apk = layout.buildDirectory.file("outputs/apk/debug/app-debug.apk").get().asFile
		if (!apk.isFile) {
			throw GradleException("Debug APK not found at ${apk.path}. Run :app:assembleDebug first.")
		}

		runCommand(
			workingDir = appProjectDir,
			command = listOf(adb.absolutePath, "-s", selectedSerial, "wait-for-device")
		)
		runCommand(
			workingDir = appProjectDir,
			command = listOf(adb.absolutePath, "-s", selectedSerial, "install", "-r", "-d", apk.absolutePath)
		)
		runCommand(
			workingDir = appProjectDir,
			command = listOf(
				adb.absolutePath,
				"-s",
				selectedSerial,
				"shell",
				"am",
				"start",
				"-n",
				"$appId/.MainActivity"
			)
		)

		savePhoneRunPreferences(
			file = phoneRunPreferencesFile,
			preferences = preferences.copy(preferredSerial = selectedSerial)
		)
		logger.lifecycle("deployDebugToConnectedDevice: deployed to $selectedSerial")
	}
}

tasks.register("enablePhoneAutoRun") {
	group = "deployment"
	description = "Turns on persisted phone auto-run preference for local Codex sessions."

	doLast {
		val preferences = loadPhoneRunPreferences(phoneRunPreferencesFile)
		savePhoneRunPreferences(
			file = phoneRunPreferencesFile,
			preferences = preferences.copy(autoRunOnPhone = true)
		)
		logger.lifecycle("enablePhoneAutoRun: autoRunOnPhone=true preferredSerial=${preferences.preferredSerial.orEmpty()}")
	}
}

tasks.register("disablePhoneAutoRun") {
	group = "deployment"
	description = "Turns off persisted phone auto-run preference for local Codex sessions."

	doLast {
		val preferences = loadPhoneRunPreferences(phoneRunPreferencesFile)
		savePhoneRunPreferences(
			file = phoneRunPreferencesFile,
			preferences = preferences.copy(autoRunOnPhone = false)
		)
		logger.lifecycle("disablePhoneAutoRun: autoRunOnPhone=false preferredSerial=${preferences.preferredSerial.orEmpty()}")
	}
}

tasks.register("showPhoneRunPreferences") {
	group = "deployment"
	description = "Prints the persisted preferred device serial and phone auto-run flag."

	doLast {
		val preferences = loadPhoneRunPreferences(phoneRunPreferencesFile)
		logger.lifecycle(
			"showPhoneRunPreferences: autoRunOnPhone=${preferences.autoRunOnPhone} preferredSerial=${preferences.preferredSerial.orEmpty()}"
		)
	}
}

private fun resolveAdbExecutable(rootDir: File): File {
	val sdkDir = System.getenv("ANDROID_SDK_ROOT")
		?.takeIf { it.isNotBlank() }
		?.let(::File)
		?: System.getenv("ANDROID_HOME")
			?.takeIf { it.isNotBlank() }
			?.let(::File)
		?: run {
			val localProperties = File(rootDir, "local.properties")
			if (!localProperties.isFile) return@run null
			val properties = Properties()
			localProperties.inputStream().use(properties::load)
			properties.getProperty("sdk.dir")
				?.takeIf { it.isNotBlank() }
				?.let(::File)
		}
		?: throw GradleException(
			"Android SDK path not found. Set ANDROID_SDK_ROOT/ANDROID_HOME or define sdk.dir in local.properties."
		)

	val adbFileName = if (System.getProperty("os.name").startsWith("Windows", ignoreCase = true)) {
		"adb.exe"
	} else {
		"adb"
	}
	return File(sdkDir, "platform-tools/$adbFileName").takeIf { it.isFile }
		?: throw GradleException("adb not found under ${sdkDir.path}/platform-tools.")
}

private fun selectPreferredDeviceSerial(
	workingDir: File,
	adb: File,
	preferredSerial: String? = null
): String? {
	val connectedSerials = runCommand(
		workingDir = workingDir,
		command = listOf(adb.absolutePath, "devices", "-l")
	)
	.lineSequence()
		.drop(1)
		.map { it.trim() }
		.filter { it.isNotBlank() }
		.mapNotNull { line ->
			val columns = line.split(Regex("\\s+"))
			if (columns.size < 2 || columns[1] != "device") {
				return@mapNotNull null
			}
			columns[0]
		}
		.toList()
	if (connectedSerials.isEmpty()) return null

	return connectedSerials.firstOrNull { serial -> serial == preferredSerial }
		?: connectedSerials.firstOrNull { serial -> !serial.startsWith("emulator-") }
		?: connectedSerials.first()
}

private fun loadPhoneRunPreferences(file: File): PhoneRunPreferences {
	if (!file.isFile) return PhoneRunPreferences()
	val properties = Properties()
	file.inputStream().use(properties::load)
	return PhoneRunPreferences(
		preferredSerial = properties.getProperty("preferredSerial")
			?.trim()
			?.takeIf { it.isNotEmpty() },
		autoRunOnPhone = properties.getProperty("autoRunOnPhone")
			?.trim()
			?.equals("true", ignoreCase = true)
			?: false
	)
}

private fun savePhoneRunPreferences(
	file: File,
	preferences: PhoneRunPreferences
) {
	file.parentFile?.mkdirs()
	val properties = Properties()
	preferences.preferredSerial?.let { properties.setProperty("preferredSerial", it) }
	properties.setProperty("autoRunOnPhone", preferences.autoRunOnPhone.toString())
	file.outputStream().use { output ->
		properties.store(output, "Local phone deployment preferences for Codex")
	}
}

private fun runCommand(
	workingDir: File,
	command: List<String>
): String {
	val process = ProcessBuilder(command)
		.directory(workingDir)
		.redirectErrorStream(true)
		.start()
	val output = process.inputStream.bufferedReader().use { it.readText() }
	val exitCode = process.waitFor()
	if (exitCode != 0) {
		throw GradleException(
			"Command failed (${command.joinToString(" ")}).\n$output"
		)
	}
	return output
}

private data class PhoneRunPreferences(
	val preferredSerial: String? = null,
	val autoRunOnPhone: Boolean = false
)
