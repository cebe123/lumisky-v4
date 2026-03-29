plugins {
	alias(libs.plugins.android.library)
}

android {
	namespace = "com.example.wallpaper"
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
	testImplementation(libs.junit)

	implementation(project(":core"))
	implementation(project(":engine"))
}
