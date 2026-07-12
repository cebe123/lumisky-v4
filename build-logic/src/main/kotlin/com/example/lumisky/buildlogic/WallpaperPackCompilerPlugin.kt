package com.example.lumisky.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest

class WallpaperPackCompilerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val task = project.tasks.register("compileWallpaperPack", CompileWallpaperPackTask::class.java) {
            sourceDir.set(project.layout.projectDirectory.dir("src/main/assets/wallpapers"))
            outputFile.set(project.layout.buildDirectory.file("generated/wallpapers/content-hashes.txt"))
            outputDirectory.set(project.layout.buildDirectory.dir("generated/wallpapers"))
        }
        project.tasks.matching { it.name == "preBuild" }.configureEach { dependsOn(task) }
    }
}

@CacheableTask
abstract class CompileWallpaperPackTask : org.gradle.api.DefaultTask() {
    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceDir: DirectoryProperty
    @get:OutputFile abstract val outputFile: RegularFileProperty
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction fun compile() {
        val root = sourceDir.get().asFile
        val files = root.walkTopDown().filter { it.isFile }.sortedBy { it.relativeTo(root).invariantSeparatorsPath }.toList()
        val lines = files.map { file ->
            val path = file.relativeTo(root).invariantSeparatorsPath
            val hash = MessageDigest.getInstance("SHA-256").digest(file.readBytes()).joinToString("") { "%02x".format(it) }
            "$path $hash"
        }
        outputFile.get().asFile.apply { parentFile.mkdirs(); writeText(lines.joinToString("\n", postfix = if (lines.isEmpty()) "" else "\n")) }
        root.resolve("index.json").takeIf { it.isFile }?.copyTo(outputDirectory.file("index.json").get().asFile, overwrite = true)
    }
}
