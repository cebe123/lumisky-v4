---
name: android-scope-reader
description: Finds minimal context for Android, Kotlin, Compose, Gradle, Service, OpenGL, or live-wallpaper tasks without loading the whole project.
---
# Android scope reader

## Procedure
1. Identify the affected Gradle module from the file/package/feature.
2. Inspect only its build file when dependency, plugin, SDK, build type, or source-set behavior matters.
3. Search the exact class, composable, resource ID, manifest component, shader asset, or Gradle task.
4. Follow only direct Android links needed by the task:
   - UI: composable/view → state holder/ViewModel → use case/repository
   - Service: manifest → service/engine → renderer/controller
   - OpenGL: renderer → shader/program → texture/config
   - Build: failing task → module build file → version catalog only if referenced
5. Prefer module-scoped commands such as `:app:testDebugUnitTest`, `:app:compileDebugKotlin`, or the corresponding affected module task.

## Avoid
Do not read all manifests, Gradle files, resources, shaders, or architecture layers by default. Do not suggest a broad architecture migration for a localized request.
