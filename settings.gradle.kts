pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }
includeBuild("build-logic")
plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}
dependencyResolutionManagement { repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS); repositories { google(); mavenCentral() } }
rootProject.name = "Lumisky"
include(":app")
// Play Asset Delivery packs can be added later after engine MVP is stable.
// include(":assetpacks:nature_pack")
// include(":assetpacks:city_pack")
// include(":assetpacks:space_pack")
// include(":assetpacks:premium_pack")
