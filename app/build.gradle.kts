plugins {
    id("com.example.lumisky.wallpaper-pack-compiler")
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "com.example.lumisky"
    compileSdk = 35
    defaultConfig {
        applicationId = "com.example.lumisky"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }
}

val validateWallpaperDefinitions = tasks.register("validateWallpaperDefinitions") {
    group = "verification"
    description = "Validates v5 wallpaper definitions and referenced asset paths."

    val assetsDir = layout.projectDirectory.dir("src/main/assets")
    inputs.dir(assetsDir.dir("wallpapers"))
    inputs.dir(assetsDir.dir("shaders"))

    doLast {
        val root = assetsDir.asFile.canonicalFile
        val parser = groovy.json.JsonSlurper()
        val errors = mutableListOf<String>()
        val supportedLayerTypes = setOf(
            "AnimationLayer",
            "CloudLayer",
            "FogLayer",
            "ForegroundLayer",
            "MoonLayer",
            "RainLayer",
            "ShaderLayer",
            "StarsLayer",
            "SunLayer",
            "TimeSliceTextureLayer",
            "TextureLayer",
            "VideoOesLayer"
        )
        val supportedRenderPasses = setOf(
            "BACKGROUND",
            "OPAQUE",
            "ALPHA_TESTED",
            "TRANSPARENT",
            "POST_PROCESS",
            "OVERLAY"
        )
        val supportedBlendModes = setOf("NONE", "ALPHA", "ADDITIVE", "MULTIPLY", "SCREEN")
        val supportedRenderTargets = setOf("DIRECT", "OFFSCREEN_FBO", "CACHED_TEXTURE", "POST_PROCESS")
        val supportedFrameModes = setOf(
            "STATIC",
            "ON_DEMAND",
            "MINUTE_TICK",
            "ONE_FPS",
            "FIXED_FPS",
            "MATCH_SCENE",
            "CONTINUOUS",
            "VIDEO_SYNC",
            "EVENT_BASED"
        )
        val supportedCacheModes = setOf("NONE", "CPU_STATE_ONLY", "FBO_CACHE", "STATIC_TEXTURE")
        val builtInShaderRefs = mapOf(
            "common.gradient.v1" to "shaders/common/gradient.frag",
            "common.video.oes" to "shaders/common/video_oes.frag",
            "common.texture2d" to "shaders/common/texture2d.frag"
        )

        fun assetFile(path: String): File {
            return root.resolve(path.replace('\\', '/')).canonicalFile
        }

        fun assetExists(path: String): Boolean {
            val resolved = assetFile(path)
            return resolved.toPath().startsWith(root.toPath()) && resolved.isFile
        }

        fun mapValue(value: Any?): Map<*, *>? = value as? Map<*, *>

        fun listValue(value: Any?): List<*> = value as? List<*> ?: emptyList<Any>()

        fun stringValue(value: Any?): String = value as? String ?: ""

        fun validateAsset(owner: String, kind: String, path: String) {
            if (path.isBlank()) {
                errors.add("$owner: blank $kind path")
            } else if (!assetExists(path)) {
                errors.add("$owner: missing $kind asset $path")
            }
        }

        fun validateDefinition(expectedId: String, definitionPath: String) {
            val definitionFile = assetFile(definitionPath)
            if (!definitionFile.toPath().startsWith(root.toPath()) || !definitionFile.isFile) {
                errors.add("$expectedId: missing definition $definitionPath")
                return
            }
            val definition = mapValue(parser.parse(definitionFile))
            if (definition == null) {
                errors.add("$expectedId: definition is not a JSON object")
                return
            }
            if ((definition["schemaVersion"] as? Number)?.toInt() != 5) {
                errors.add("$expectedId: definition must use schemaVersion 5")
            }
            val id = stringValue(definition["id"])
            if (id != expectedId) {
                errors.add("$expectedId: definition id mismatch $id")
            }
            if (stringValue(definition["name"]).isBlank()) {
                errors.add("$expectedId: blank wallpaper name")
            }
            if (stringValue(definition["category"]).isBlank()) {
                errors.add("$expectedId: blank wallpaper category")
            }
            val preview = mapValue(definition["preview"])
            if (preview == null) {
                errors.add("$expectedId: missing preview object")
            } else {
                validateAsset(expectedId, "preview thumbnail", stringValue(preview["thumbnail"]))
                if (stringValue(preview["cardMode"]) != "THUMBNAIL") {
                    errors.add("$expectedId: catalog preview cardMode must be THUMBNAIL")
                }
            }

            val layers = listValue(definition["layers"])
            layers.forEachIndexed { index, layerValue ->
                val layer = mapValue(layerValue)
                if (layer == null) {
                    errors.add("$expectedId: layer[$index] is not a JSON object")
                    return@forEachIndexed
                }
                val layerOwner = "$expectedId:${stringValue(layer["id"]).ifBlank { "layer[$index]" }}"
                val layerType = stringValue(layer["type"])
                if (layerType !in supportedLayerTypes) {
                    errors.add("$layerOwner: unsupported layer type $layerType")
                }
                val renderPass = stringValue(layer["renderPass"]).ifBlank { "BACKGROUND" }
                if (renderPass !in supportedRenderPasses) {
                    errors.add("$layerOwner: unsupported renderPass $renderPass")
                }
                val blendMode = stringValue(layer["blendMode"]).ifBlank { "NONE" }
                if (blendMode !in supportedBlendModes) {
                    errors.add("$layerOwner: unsupported blendMode $blendMode")
                }
                val renderTarget = stringValue(layer["renderTarget"]).ifBlank { "DIRECT" }
                if (renderTarget !in supportedRenderTargets) {
                    errors.add("$layerOwner: unsupported renderTarget $renderTarget")
                }
                val shaderRef = stringValue(layer["shaderRef"])
                if (shaderRef.isNotBlank()) {
                    validateAsset(layerOwner, "shader", builtInShaderRefs[shaderRef] ?: shaderRef)
                }
                val source = stringValue(layer["source"])
                if (layerType == "VideoOesLayer" && source.isBlank()) {
                    errors.add("$layerOwner: VideoOesLayer requires a video source")
                }
                if (source.isNotBlank()) {
                    validateAsset(layerOwner, "source", source)
                }
                val timeSlices = listValue(layer["timeSlices"])
                if (layerType == "TimeSliceTextureLayer" && timeSlices.isEmpty()) {
                    errors.add("$layerOwner: TimeSliceTextureLayer requires timeSlices")
                }
                timeSlices.forEachIndexed timeSliceLoop@{ sliceIndex, sliceValue ->
                    val slice = mapValue(sliceValue)
                    if (slice == null) {
                        errors.add("$layerOwner: timeSlices[$sliceIndex] is not a JSON object")
                        return@timeSliceLoop
                    }
                    val minute = (slice["minute"] as? Number)?.toInt()
                    if (minute == null || minute !in 0..1439) {
                        errors.add("$layerOwner: timeSlices[$sliceIndex] invalid minute")
                    }
                    validateAsset(layerOwner, "time slice", stringValue(slice["path"]))
                }
                val framePolicy = mapValue(layer["framePolicy"])
                if (framePolicy != null) {
                    val mode = stringValue(framePolicy["mode"]).ifBlank { "MATCH_SCENE" }
                    if (mode !in supportedFrameModes) {
                        errors.add("$layerOwner: unsupported framePolicy mode $mode")
                    }
                    val cacheMode = stringValue(framePolicy["cacheMode"]).ifBlank { "NONE" }
                    if (cacheMode !in supportedCacheModes) {
                        errors.add("$layerOwner: unsupported framePolicy cacheMode $cacheMode")
                    }
                }
                listValue(layer["textures"]).forEachIndexed textureLoop@{ textureIndex, textureValue ->
                    val texture = mapValue(textureValue)
                    if (texture == null) {
                        errors.add("$layerOwner: texture[$textureIndex] is not a JSON object")
                        return@textureLoop
                    }
                    if (stringValue(texture["uniform"]).isBlank()) {
                        errors.add("$layerOwner: texture[$textureIndex] blank uniform")
                    }
                    validateAsset(layerOwner, "texture", stringValue(texture["path"]))
                }
            }
            if (stringValue(definition["sourceKind"]) == "VIDEO") {
                val enabledLayers = layers.mapNotNull(::mapValue).filter { it["enabled"] != false }
                if (enabledLayers.none { stringValue(it["type"]) == "VideoOesLayer" }) {
                    errors.add("$expectedId: VIDEO wallpaper requires an enabled VideoOesLayer")
                }
                if (enabledLayers.none {
                        stringValue(it["type"]) == "TextureLayer" && stringValue(it["source"]).isNotBlank()
                    }
                ) {
                    errors.add("$expectedId: VIDEO wallpaper requires an enabled TextureLayer poster fallback")
                }
            }
        }

        val catalogFile = assetFile("wallpapers/index.json")
        if (!catalogFile.isFile) {
            errors.add("catalog: missing wallpapers/index.json")
        } else {
            val catalog = mapValue(parser.parse(catalogFile))
            if (catalog == null) {
                errors.add("catalog: index.json is not a JSON object")
            } else {
                if ((catalog["schemaVersion"] as? Number)?.toInt() != 5) {
                    errors.add("catalog: index.json must use schemaVersion 5")
                }
                listValue(catalog["wallpapers"]).forEachIndexed { index, itemValue ->
                    val item = mapValue(itemValue)
                    if (item == null) {
                        errors.add("catalog: wallpapers[$index] is not a JSON object")
                        return@forEachIndexed
                    }
                    val id = stringValue(item["id"])
                    val definitionPath = stringValue(item["definitionPath"])
                    validateAsset("catalog:$id", "thumbnail", stringValue(item["thumbnail"]))
                    validateDefinition(id, definitionPath)
                }
            }
        }

        validateDefinition("starter_gradient", "wallpapers/starter_gradient.json")

        if (errors.isNotEmpty()) {
            throw GradleException(errors.joinToString(separator = System.lineSeparator()))
        }
    }
}

tasks.named("preBuild") {
    dependsOn(validateWallpaperDefinitions)
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:runner:1.6.2")

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.datastore:datastore-preferences:1.1.1")
    implementation("com.google.dagger:hilt-android:2.57.1")
    ksp("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.media3:media3-exoplayer:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // Compose Dependencies
    implementation("androidx.compose.ui:ui:1.7.5")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.5")
    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("androidx.compose.runtime:runtime:1.7.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.5")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("com.google.android.play:review-ktx:2.0.1")
    
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.1.2"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-crashlytics")
}
