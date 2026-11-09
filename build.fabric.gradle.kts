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

dependencies {
    minecraft("com.mojang:minecraft:${sc.current.version}")
    // Mojang's mappings on the obfuscated versions.
    loomx.applyMojangMappings()

    // Use `mod{dependency type}` even on 26.1+, loom-back-compat converts them.
    modImplementation("net.fabricmc:fabric-loader:${sc.properties["deps.fabric_loader"]}")

    // Fabric Loader alone doesn't put mods' assets in the resource manager (NeoForge does), so the
    // Fabric API module that does is bundled into the jar (jar-in-jar), with the base module it needs.
    // Players don't have to install Fabric API.
    for (module in listOf("fabric-resource-loader-v0", "fabric-api-base")) {
        val dependency = fabricApi.module(module, sc.properties["deps.fabric_api"])
        modImplementation(dependency)
        include(dependency)
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
        }

        filesMatching("fabric.mod.json") { expand(props) }

        val mixinJava = "JAVA_${requiredJava.majorVersion}"
        filesMatching("*.mixins.json") { expand("java" to mixinJava) }

        exclude("META-INF/neoforge.mods.toml")
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
