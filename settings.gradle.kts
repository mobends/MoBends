pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
        maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
        maven("https://maven.kikugie.dev/releases") { name = "KikuGie Releases" }
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    // Builds every Minecraft version and loader from the one src/ tree (see CONTRIBUTING.md).
    id("dev.kikugie.stonecutter") version "0.10"

    // Applies the right Fabric Loom for the Minecraft version (obfuscated before 26.1, not after).
    id("dev.kikugie.loom-back-compat") version "0.4.3"

    // Downloads the JDKs the build asks for (Gradle itself runs on 25, see gradle/gradle-daemon-jvm.properties).
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

stonecutter {
    create(rootProject) {
        /**
         * Creates a version node `versions/<version>-<loader>` for each loader, built by
         * `build.<loader>.gradle.kts`.
         */
        fun match(version: String, vararg loaders: String) {
            for (loader in loaders) version("$version-$loader", version).buildscript("build.$loader.gradle.kts")
        }

        // The targets, see TODO.md. The sources in src/ are always committed with vcsVersion active.
        match("1.21.1", "fabric", "neoforge")
        vcsVersion = "1.21.1-fabric"
    }
}

// The Minecraft-free animation engine (Kumo, math). Plain Java 8, shared with the 1.12.2 branch
// as a published library, and compiled into every version's jar here. See CONTRIBUTING.md.
include("core")

rootProject.name = "mobends"
