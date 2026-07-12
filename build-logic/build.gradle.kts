plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

gradlePlugin {
    plugins {
        create("wallpaperPackCompiler") {
            id = "com.example.lumisky.wallpaper-pack-compiler"
            implementationClass = "com.example.lumisky.buildlogic.WallpaperPackCompilerPlugin"
        }
    }
}
