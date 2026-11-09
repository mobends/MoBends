// Builds a NeoForge version node (versions/<minecraft>-neoforge) from src/.
plugins {
    id("net.neoforged.moddev") version "2.0.148"
    // Keeps several versions from decompiling Minecraft at once.
    id("neoforge-mutex")
}

version = "${sc.properties["mod.version"]}+${sc.current.version}-neoforge"
base.archivesName = sc.properties["mod.id"]

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "26.1" -> JavaVersion.VERSION_25
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

// The Minecraft-free engine is compiled into the mod (see settings.gradle.kts).
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
}

neoForge {
    version = sc.properties["deps.neo_loader"]

    mods {
        register(sc.properties["mod.id"]) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run") // Shared by every version and loader.
            client()
        }

        register("server") {
            gameDirectory = rootProject.file("run")
            server()
            programArgument("--nogui")
        }
    }
}

java {
    withSourcesJar()
    targetCompatibility = requiredJava
    sourceCompatibility = requiredJava

    toolchain {
        vendor = JvmVendorSpec.ADOPTIUM
        languageVersion = JavaLanguageVersion.of(requiredJava.majorVersion)
    }
}

tasks {
    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    processResources {
        fun MutableMap<String, String>.register(key: String, value: String) {
            inputs.property(key, value)
            set(key, value)
        }

        val props = buildMap {
            register("id", sc.properties["mod.id"])
            register("name", sc.properties["mod.name"])
            register("version", sc.properties["mod.version"])
            register("minecraft", sc.properties["mod.mc_compat"])
        }

        filesMatching("META-INF/neoforge.mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("fabric.mod.json", "*.ct", "*.classtweaker")
    }

    named("createMinecraftArtifacts") {
        dependsOn("stonecutterGenerate")
    }

    withType<Jar> {
        val name = sc.properties["mod.id"]
        inputs.property("mod_id", name)
        from(rootProject.file("LICENSE")) { rename { "${it}_$name" } }
    }

    register<Copy>("buildAndCollect") {
        group = "build"
        description = "Builds the mod jars and copies them to build/libs/<mod version>/."

        inputs.property("version", sc.properties["mod.version"])
        from(jar.flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${sc.properties["mod.version"]}"))
    }

    // What the release workflow (.github/workflows/release.yml) uploads this node's jar as.
    register("releaseInfo") {
        group = "build"
        description = "Writes the node's jar name, loader and Minecraft releases to build/release/<node>.json."

        val info = mapOf(
            "jar" to jar.flatMap { it.archiveFileName }.get(),
            "minecraft" to sc.current.version,
            "loader" to project.name.substringAfter('-'),
            "gameVersions" to (sc.properties.raw("mod.mc_releases") as List<*>).map { it.toString() },
            "java" to requiredJava.majorVersion,
        )
        val file = rootProject.layout.buildDirectory.file("release/${project.name}.json")
        inputs.property("info", info)
        outputs.file(file)
        doLast { file.get().asFile.writeText(groovy.json.JsonOutput.toJson(info) + "\n") }
    }
}
