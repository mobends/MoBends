// Builds a Forge version node (versions/<minecraft>-forge) from src/. Forge is the loader for 1.20.1,
// before NeoForge split from it; ModDevGradle's legacy plugin builds it as the NeoForge one does.
plugins {
    id("net.neoforged.moddev.legacyforge") version "2.0.148"
    // Keeps several versions from decompiling Minecraft at once.
    id("neoforge-mutex")
}

version = "${sc.properties["mod.version"]}+${sc.current.version}-forge"
base.archivesName = sc.properties["mod.id"]

val requiredJava: JavaVersion = when {
    sc.current.parsed >= "1.20.5" -> JavaVersion.VERSION_21
    else -> JavaVersion.VERSION_17
}

// Forge runs on SRG names, so the mixins' Mojang names are mapped by this refmap, made as they compile.
val refmap = "${sc.properties["mod.id"]}.refmap.json"
val mixinConfigs = listOf("mobends.mixins.json", "mobends.geckolib.mixins.json")

repositories {
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroup("software.bernie.geckolib")
            includeGroup("com.eliotlash.mclib")
        }
    }
}

dependencies {
    // Mixin's annotation processor, which writes the refmap.
    annotationProcessor("org.spongepowered:mixin:0.8.5:processor")

    // MixinExtras (@WrapOperation, @WrapMethod), which Fabric and NeoForge have and Forge 1.20.1
    // doesn't: built into the jar, which Forge loads it from.
    val mixinExtras = sc.properties["deps.mixinextras"]
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtras")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:$mixinExtras")!!)

    // GeckoLib, whose armour Mo' Bends bends when it's installed (mixin.geckolib); not needed. Run the
    // client with -Pgeckolib to have it in the game.
    val geckoLib = "software.bernie.geckolib:geckolib-forge-${sc.current.version}:${sc.properties["deps.geckolib"]}"
    modCompileOnly(geckoLib) { isTransitive = false }
    if (providers.gradleProperty("geckolib").isPresent) {
        modRuntimeOnly(geckoLib) { isTransitive = false }
        // GeckoLib 4.8 reads its files with mclib, which its jar bundles but the development client
        // doesn't load from it.
        runtimeOnly("com.eliotlash.mclib:mclib:20")
    }

    // The core's @Nullable.
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

// The Minecraft-free engine is compiled into the mod (see settings.gradle.kts).
sourceSets.main {
    java.srcDir(rootProject.file("core/src/main/java"))
}

legacyForge {
    version = "${sc.current.version}-${sc.properties["deps.forge"]}"

    mods {
        register(sc.properties["mod.id"]) {
            sourceSet(sourceSets.main.get())
        }
    }

    runs {
        register("client") {
            gameDirectory = rootProject.file("run") // Shared by every version and loader.
            client()
            systemProperty("mixin.debug.export", "true") // Exports the transformed classes, for debugging.
        }

        register("server") {
            gameDirectory = rootProject.file("run")
            server()
            programArgument("--nogui")
        }
    }
}

mixin {
    add(sourceSets.main.get(), refmap)
    mixinConfigs.forEach { config(it) }
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
            register("forge", sc.properties["deps.forge"].substringBefore('.'))
            register("geckolib", sc.properties["mod.geckolib_compat"])
        }

        filesMatching("META-INF/mods.toml") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        val refmapName = refmap // A local, as the configuration cache can't keep the script's.
        inputs.property("refmap", refmapName)
        filesMatching("*.mixins.json") {
            expand("java" to mixinJava)
            // Points each config at the refmap (Fabric's Loom adds its own, NeoForge needs none).
            filter { line -> line.replace(Regex("^(\\s*)\"package\":"), "$1\"refmap\": \"$refmapName\",\n$1\"package\":") }
        }

        // pack.mcmeta: Forge 1.20.1 reads the mod's jar as a resource pack, and warns without one
        // (pack_format 15 is 1.20.1's); the other loaders leave it out.
        exclude("fabric.mod.json", "*.ct", "*.classtweaker", "META-INF/neoforge.mods.toml")
    }

    jar {
        // Where Forge finds the mixin configs outside the development client (ModDevGradle's
        // `mixin` block only hands them to its runs).
        manifest.attributes("MixinConfigs" to mixinConfigs.joinToString(","))
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
        // The reobfuscated jar (SRG names), the one Forge loads.
        from(named<Jar>("reobfJar").flatMap { it.archiveFile }, named<Jar>("sourcesJar").flatMap { it.archiveFile })
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
