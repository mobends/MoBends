pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.architectury.dev/")
        maven("https://maven.fabricmc.net")
        maven("https://maven.minecraftforge.net/")
        maven("https://repo.essential.gg/repository/maven-releases/")
    }
    resolutionStrategy {
        eachPlugin {
            // Essential's fork of Architectury Loom, which still builds Forge mods for 1.8.9.
            when (requested.id.id) {
                "gg.essential.loom" -> useModule("gg.essential:architectury-loom:${requested.version}")
            }
        }
    }
}

plugins {
    // Downloads the JDK 8 the mod is compiled and run with (Gradle itself runs on 17 or later).
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "MoBends"

// Loom 0.10's runClient fails Gradle 8's validation of task properties, and a task can't be removed or replaced,
// so asking for it runs launchClient (build.gradle.kts), which starts the same game.
gradle.startParameter.setTaskNames(gradle.startParameter.taskNames.map { if (it == "runClient" || it == ":runClient") "launchClient" else it })
