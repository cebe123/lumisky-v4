plugins {
	alias(libs.plugins.android.test)
}

android {
	namespace = "com.example.lumisky.benchmark"
	compileSdk {
		version = release(36)
	}
	targetProjectPath = ":app"
	experimentalProperties["android.experimental.self-instrumenting"] = true

	defaultConfig {
		minSdk = 28
		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
		testInstrumentationRunnerArguments["androidx.benchmark.suppressErrors"] = "EMULATOR"
	}

	buildTypes {
		create("benchmark") {
			isDebuggable = true
			signingConfig = signingConfigs.getByName("debug")
			matchingFallbacks += listOf("benchmark", "release")
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	testOptions {
		animationsDisabled = true
	}
}

androidComponents {
	beforeVariants(selector().all()) { variantBuilder ->
		variantBuilder.enable = variantBuilder.buildType == "benchmark"
	}
}

dependencies {
	implementation("androidx.benchmark:benchmark-macro-junit4:1.3.4")
	implementation("androidx.test.ext:junit:1.2.1")
	implementation("androidx.test:runner:1.6.2")
	implementation("androidx.test.uiautomator:uiautomator:2.3.0")
}
