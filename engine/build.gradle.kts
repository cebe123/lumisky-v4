plugins {
	alias(libs.plugins.android.library)
	alias(libs.plugins.kotlin.serialization)
}

android {
	namespace = "com.example.engine"
	compileSdk {
		version = release(36)
	}

	defaultConfig {
		minSdk = 28
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		isCoreLibraryDesugaringEnabled = true
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	testImplementation(libs.junit)

	implementation(project(":core"))
	implementation(libs.kotlinx.serialization.json)
	coreLibraryDesugaring(libs.desugar.jdk.libs)
}
