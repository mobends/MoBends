// Builds a Fabric version node (versions/<minecraft>-fabric) from src/.
plugins {
    // Applies the right Fabric Loom for the Minecraft version.
    id("dev.kikugie.loom-back-compat")
}

// DO NOT set group = ...! (Stonecutter nodes would clash.)
version = "${sc.properties["mod.version"]}+${sc.current.version}-fabric"
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

repositories {
    // Mod Menu, whose Config button opens the Mo' Bends menu if it's installed.
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }
    maven("https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/") {
        name = "GeckoLib"
        content {
            includeGroup("software.bernie.geckolib")
            includeGroup("com.geckolib")
            includeGroup("com.eliotlash.mclib")
        }
    }
    // Fantasy Armor and Cloth Config, for trying GeckoLib's armour on in the development client, and
    // Player Animation Library and Better Combat.
    maven("https://api.modrinth.com/maven") {
        name = "Modrinth"
        content { includeGroup("maven.modrinth") }
    }
}

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Mojang's mappings on the obfuscated versions.
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+, loom-back-compat converts them.
    modImplementation("net.fabricmc:fabric-loader:${sc.properties["deps.fabric_loader"]}")

    // Fabric Loader alone doesn't put mods' assets in the resource manager (NeoForge does), nor lets
    // a server send a mod's payloads (the server's rules), so the Fabric API modules that do are
    // bundled into the jar (jar-in-jar), with the base module they need. Players don't have to
    // install Fabric API. On 26.x the resource loader is v1: v0 is only a shim there that needs v1,
    // and without it Fabric Loader drops v0 and the mod's assets aren't loaded.
    val resourceLoader = if (sc.current.parsed >= "26.1") "fabric-resource-loader-v1" else "fabric-resource-loader-v0"
    for (module in listOf(resourceLoader, "fabric-networking-api-v1", "fabric-api-base")) {
        val dependency = fabricApi.module(module, sc.properties["deps.fabric_api"])
        modImplementation(dependency)
        include(dependency)
    }

    // Mod Menu's API, for its Config button (MoBendsModMenu). Optional: the jar doesn't need it.
    modCompileOnly("com.terraformersmc:modmenu:${sc.properties["deps.modmenu"]}") { isTransitive = false }
    // GeckoLib, whose armour Mo' Bends bends when it's installed (mixin.geckolib); not needed. Run the
    // client with -Pgeckolib to have it (and the Fabric API it needs) in the game; on 26.x, with
    // Fantasy Armor too, armour to try on. GeckoLib 5 (26.x) is published under its own group.
    val geckoLibGroup = if (sc.current.parsed >= "26.1") "com.geckolib" else "software.bernie.geckolib"
    val geckoLib = "$geckoLibGroup:geckolib-fabric-${sc.current.version}:${sc.properties["deps.geckolib"]}"
    modCompileOnly(geckoLib) { isTransitive = false }
    if (providers.gradleProperty("geckolib").isPresent) {
        modLocalRuntime(geckoLib) { isTransitive = false }
        modLocalRuntime("net.fabricmc.fabric-api:fabric-api:${sc.properties["deps.fabric_api"]}")
        if (sc.current.parsed >= "26.1") {
            modLocalRuntime("maven.modrinth:fantasy_armor:${sc.properties["deps.fantasy_armor"]}") { isTransitive = false }
        }
        // GeckoLib 4.8 (1.20.1) reads its files with mclib, which its jar bundles but the
        // non-transitive dependency above leaves out of the development client.
        if (sc.current.parsed < "1.21") {
            localRuntime("com.eliotlash.mclib:mclib:20")
        }
    }
    // Player Animation Library, whose pose core:vanilla_pose can build on, and which
    // mobends:compat_pal_animation_playing reads (standard.compat.pal); not needed. 26.x only.
    val pal = { "maven.modrinth:player-animation-library:${sc.properties["deps.player_animation_library"]}" }
    if (sc.current.parsed >= "26.1") {
        modCompileOnly(pal()) { isTransitive = false }
    }
    // Better Combat, which plays its attacks with PAL: run the client with -PbetterCombat to see how
    // they get along with Mo' Bends' (26.x only). Add Mo' Bends Compat+ (misc/compat-plus-pack) to
    // the resource packs to see them played.
    val betterCombat = providers.gradleProperty("betterCombat").isPresent && sc.current.parsed >= "26.1"
    if (betterCombat) {
        modLocalRuntime("maven.modrinth:better-combat:${sc.properties["deps.better_combat"]}") { isTransitive = false }
        modLocalRuntime(pal()) { isTransitive = false }
    }
    // Fabric API and Cloth Config, which Fantasy Armor and Better Combat need.
    if (sc.current.parsed >= "26.1" && (providers.gradleProperty("geckolib").isPresent || betterCombat)) {
        modLocalRuntime("maven.modrinth:cloth-config:${sc.properties["deps.cloth_config"]}") { isTransitive = false }
        if (betterCombat) modLocalRuntime("net.fabricmc.fabric-api:fabric-api:${sc.properties["deps.fabric_api"]}")
    }

    // The core's @Nullable. NeoForge has it on the classpath, Fabric doesn't; not needed at runtime.
    compileOnly("com.google.code.findbugs:jsr305:3.0.2")
}

loom {
    fabricModJsonPath = rootProject.file("src/main/resources/fabric.mod.json")
    accessWidenerPath = sc.process(
        rootProject.file("src/main/resources/mobends.ct"),
        "build/processed.ct"
    )

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Names lambdas, which helps writing mixins.
    }

    runConfigs.all {
        preferGradleTask = true
        generateRunConfig = true
        runDirectory = rootProject.file("run") // Shared by every version and loader.
        jvmArguments.add("-Dmixin.debug.export=true") // Exports the transformed classes, for debugging.
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
            register("loader", Regex("\\d+\\.\\d+").find(sc.properties["deps.fabric_loader"])!!.value)
            register("geckolib", sc.properties["mod.geckolib_compat"])
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml", "META-INF/mods.toml", "pack.mcmeta")
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
        // loomx.mod(Sources)Jar is the jar task of the applied Loom.
        from(loomx.modJar.flatMap { it.archiveFile }, loomx.modSourcesJar.flatMap { it.archiveFile })
        into(rootProject.layout.buildDirectory.dir("libs/${sc.properties["mod.version"]}"))
    }

    // What the release workflow (.github/workflows/release.yml) uploads this node's jar as.
    register("releaseInfo") {
        group = "build"
        description = "Writes the node's jar name, loader and Minecraft releases to build/release/<node>.json."

        val info = mapOf(
            "jar" to loomx.modJar.flatMap { it.archiveFileName }.get(),
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
