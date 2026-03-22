plugins {
	alias(libs.plugins.android.library)
}

android {
	namespace = "com.example.core"
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
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	implementation(libs.androidx.core.ktx)
	implementation(libs.play.services.location)
}
