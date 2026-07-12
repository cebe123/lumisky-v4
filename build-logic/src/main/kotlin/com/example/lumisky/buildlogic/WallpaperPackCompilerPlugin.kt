package com.example.lumisky.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.security.MessageDigest
import java.io.File

class WallpaperPackCompilerPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val task = project.tasks.register("compileWallpaperPack", CompileWallpaperPackTask::class.java) {
            sourceDir.set(project.layout.projectDirectory.dir("src/main/assets/wallpapers"))
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
    @get:OutputDirectory abstract val outputDirectory: DirectoryProperty

    @TaskAction fun compile() {
        WallpaperPackCompiler.compile(sourceDir.get().asFile, outputDirectory.get().asFile)
    }
}

internal object WallpaperPackCompiler {
    fun compile(root: File, outputDirectory: File) {
        val entries = root.walkTopDown()
            .filter { it.isFile }
            .map { file ->
                ContentHashEntry(
                    path = file.relativeTo(root).invariantSeparatorsPath,
                    sha256 = sha256(file)
                )
            }
            .sortedBy(ContentHashEntry::path)
            .toList()
        val contentHash = MessageDigest.getInstance("SHA-256").apply {
            entries.forEach { entry ->
                update(entry.path.toByteArray(Charsets.UTF_8))
                update(0)
                update(entry.sha256.toByteArray(Charsets.US_ASCII))
                update('\n'.code.toByte())
            }
        }.digest().toHex()

        outputDirectory.deleteRecursively()
        outputDirectory.mkdirs()
        outputDirectory.resolve("content-hashes.txt").writeText(
            entries.joinToString("\n", postfix = if (entries.isEmpty()) "" else "\n") {
                "${it.path} ${it.sha256}"
            }
        )
        outputDirectory.resolve("content-manifest.json").writeText(
            buildString {
                append("{\n  \"schemaVersion\": 1,\n  \"contentHash\": \"")
                append(contentHash)
                append("\",\n  \"files\": [")
                entries.forEachIndexed { index, entry ->
                    if (index > 0) append(',')
                    append("\n    {\"path\": \"")
                    append(entry.path.jsonEscaped())
                    append("\", \"sha256\": \"")
                    append(entry.sha256)
                    append("\"}")
                }
                if (entries.isNotEmpty()) append('\n')
                append("  ]\n}\n")
            }
        )
        root.resolve("index.json").takeIf(File::isFile)
            ?.copyTo(outputDirectory.resolve("index.json"), overwrite = true)
    }

    private fun sha256(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { input ->
            val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
            while (true) {
                val count = input.read(buffer)
                if (count < 0) break
                digest.update(buffer, 0, count)
            }
        }
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { byte ->
        HEX[(byte.toInt() ushr 4) and 0x0f].toString() + HEX[byte.toInt() and 0x0f]
    }

    private fun String.jsonEscaped(): String = buildString(length) {
        this@jsonEscaped.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                else -> append(char)
            }
        }
    }

    private const val HEX = "0123456789abcdef"
}

internal data class ContentHashEntry(val path: String, val sha256: String)
