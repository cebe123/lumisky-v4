plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("wallpaperPackCompiler") {
            id = "com.example.lumisky.wallpaper-pack-compiler"
            implementationClass = "com.example.lumisky.buildlogic.WallpaperPackCompilerPlugin"
        }
    }
}
