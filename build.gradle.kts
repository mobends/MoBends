import org.apache.commons.lang3.SystemUtils
import java.text.SimpleDateFormat
import java.util.Date

plugins {
    java
    id("gg.essential.loom") version "0.10.0.+"
    id("dev.architectury.architectury-pack200") version "0.1.3"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val mod_version: String by project
val minecraft_version: String by project
val forge_version: String by project
val mappings_version: String by project
val core_version: String by project

version = "$mod_version-${SimpleDateFormat("dd.MM.yy").format(Date())}"
group = "goblinbob.mobends"
base.archivesName.set("MoBends_$minecraft_version")

// Minecraft 1.8.9 runs on Java 8, so the mod is compiled for it (Gradle itself runs on 17 or later).
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(8))
}

val accessTransformerFile = file("src/main/resources/META-INF/accesstransformer.cfg")

loom {
    runConfigs {
        named("client") {
            // LWJGL 2 can't open its window with this argument on macOS (for the IDE run configurations).
            if (SystemUtils.IS_OS_MAC_OSX) vmArgs.remove("-XstartOnFirstThread")
        }
        remove(getByName("server"))
    }
    forge {
        pack200Provider.set(dev.architectury.pack200.java.Pack200Adapter())
        accessTransformer(accessTransformerFile)
    }
}

// FML's dev-time resource pack for the mod is rooted at the directory that holds the @Mod class, so the
// resources must be written next to the classes or nothing under assets/ is found when running the client.
sourceSets.main {
    output.setResourcesDir(sourceSets.main.flatMap { it.java.classesDirectory })
}

repositories {
    mavenCentral()
    // The Minecraft-free animation engine (mobends-core), released from `main`. GitHub Packages needs
    // a token even to read: set gpr.user and gpr.key (a token with read:packages) in
    // ~/.gradle/gradle.properties, or GITHUB_ACTOR and GITHUB_TOKEN as in CI. See CONTRIBUTING.md.
    maven("https://maven.pkg.github.com/mobends/MoBends") {
        name = "GitHubPackages"
        credentials {
            username = (project.findProperty("gpr.user") ?: System.getenv("GITHUB_ACTOR"))?.toString()
            password = (project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN"))?.toString()
        }
    }
}
if ((project.findProperty("gpr.key") ?: System.getenv("GITHUB_TOKEN")) == null) {
    logger.warn("No GitHub token for mobends-core: set gpr.user and gpr.key in ~/.gradle/gradle.properties (see CONTRIBUTING.md).")
}

/** What's bundled into the mod jar. */
val shadowImpl: Configuration by configurations.creating {
    configurations.implementation.get().extendsFrom(this)
}

dependencies {
    minecraft("com.mojang:minecraft:$minecraft_version")
    mappings("de.oceanlabs.mcp:mcp_${mappings_version.substringBefore('_')}:${mappings_version.substringAfter('_')}-$minecraft_version")
    forge("net.minecraftforge:forge:$minecraft_version-$forge_version-$minecraft_version")

    // The Minecraft-free animation engine, bundled into the mod jar. Released from `main`.
    shadowImpl("goblinbob.mobends:mobends-core:$core_version") { isTransitive = false }
    // The core and the mod need Gson 2.8 (1.8.9 ships 2.2.4, without @JsonAdapter among others), so it's
    // bundled too, relocated so it can't clash with the game's.
    shadowImpl("com.google.code.gson:gson:2.8.0")

    testImplementation("junit:junit:4.13.2")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    // No mixins, so no annotation processing (Loom adds the Mixin processor).
    options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-proc:none"))
}

tasks.processResources {
    inputs.property("version", mod_version)
    inputs.property("mcversion", minecraft_version)
    filesMatching("mcmod.info") {
        expand("version" to mod_version, "mcversion" to minecraft_version)
    }
}

tasks.jar {
    archiveClassifier.set("without-deps")
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    manifest.attributes(
        "Specification-Title" to project.name,
        "Specification-Vendor" to "GoblinBob",
        "Implementation-Title" to project.name,
        "Implementation-Version" to project.version,
        "Implementation-Vendor" to "GoblinBob",
        "FMLAT" to "accesstransformer.cfg",
    )
}

// The released jar is the shadow jar (the mod with the core and Gson bundled), remapped to SRG names.
tasks.shadowJar {
    destinationDirectory.set(layout.buildDirectory.dir("intermediates"))
    archiveClassifier.set("non-obfuscated-with-deps")
    configurations = listOf(shadowImpl)
    relocate("com.google.gson", "goblinbob.mobends.lib.gson")
    exclude("META-INF/maven/**")
}

// Loom 0.10 remaps the jar on Gradle 8.8 (the wrapper's version); on later versions it comes out empty.
tasks.remapJar {
    archiveClassifier.set("")
    dependsOn(tasks.shadowJar)
    input.set(tasks.shadowJar.flatMap { it.archiveFile })
}

tasks.assemble { dependsOn(tasks.remapJar) }

val srcJar by tasks.registering(Jar::class) {
    from(sourceSets.main.get().allSource)
    archiveClassifier.set("sources")
    from(file("LICENSE"))
}
tasks.build { dependsOn(srcJar) }

// Build-time tools (src/tools), not part of the mod.
val tools: SourceSet by sourceSets.creating
dependencies {
    "toolsImplementation"("org.ow2.asm:asm:9.7")
}

/** The SRG-named (vanilla) Minecraft jar Loom prepares. */
fun srgJar(): File = fileTree(gradle.gradleUserHomeDir.resolve("caches/essential-loom/$minecraft_version")) {
    include("*mcp_*/minecraft-srg.jar")
}.files.firstOrNull() ?: throw GradleException("Run `./gradlew compileJava` first so Loom prepares the SRG-named Minecraft jar.")

// Regenerates the accessors of the vanilla fields model definitions can name (core/vanilla) and the
// access transformer entries they need, from the SRG-named game jar and MCP's names.
val generateVanillaFields by tasks.registering(JavaExec::class) {
    description = "Regenerates core/vanilla and the generated access transformer section from the Minecraft mappings."
    classpath = tools.runtimeClasspath
    mainClass.set("goblinbob.mobends.tools.GenVanillaFields")
    val mcpZip = configurations.detachedConfiguration(
        dependencies.create("de.oceanlabs.mcp:mcp_${mappings_version.substringBefore('_')}:${mappings_version.substringAfter('_')}-$minecraft_version")
    )
    doFirst {
        args(projectDir.absolutePath, minecraft_version, forge_version, mappings_version, srgJar().absolutePath, mcpZip.singleFile.absolutePath)
    }
}

// Fails the build when the generated vanilla accessors were made for other versions than gradle.properties
// names, since they would then miss or misname fields. Only reads the version stamp in each file.
val checkVanillaFields by tasks.registering {
    description = "Checks that core/vanilla and the generated access transformer section match the configured versions."
    doLast {
        // Keep in sync with the stamp in GenVanillaFields.java.
        val stamp = "generated by generateVanillaFields (src/tools) for $minecraft_version-$forge_version with $mappings_version"
        val stale = listOf(
            "src/main/java/goblinbob/mobends/core/vanilla/VanillaModelParts.java",
            "src/main/java/goblinbob/mobends/core/vanilla/VanillaEntityFields.java",
            "src/main/resources/META-INF/accesstransformer.cfg",
        ).filter { !file(it).isFile || !file(it).readText().lowercase().contains(stamp.lowercase()) }
        if (stale.isNotEmpty())
            throw GradleException("Generated for other Minecraft, Forge or mappings versions: ${stale.joinToString()}. Run `./gradlew generateVanillaFields`.")
    }
}
checkVanillaFields { mustRunAfter(generateVanillaFields) }
tasks.compileJava { dependsOn(checkVanillaFields) }

// Loom 0.10's run task fails Gradle 8's validation of task properties, even disabled, so `runClient` runs this
// instead (settings.gradle.kts swaps the name): the same launch, Loom's dev launcher with the launch.cfg Loom
// writes, from a plain JavaExec.
tasks.register<JavaExec>("launchClient") {
    group = "fabric"
    description = "Starts the Minecraft client with the mod (./gradlew runClient runs this)."
    dependsOn("downloadAssets", tasks.classes)
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("net.fabricmc.devlaunchinjector.Main")
    workingDir = file("run")
    doFirst { workingDir.mkdirs() }
    jvmArgs(
        "-Dfabric.dli.config=${file(".gradle/loom-cache/launch.cfg").absolutePath}",
        "-Dfabric.dli.env=client",
        "-Dfabric.dli.main=net.minecraft.launchwrapper.Launch",
    )
    // Play under a fixed name, e.g. to test name selectors: ./gradlew runClient -Pmc_username=BendyTester
    project.findProperty("mc_username")?.let { args("--username", it.toString()) }
    // LWJGL 2's macOS natives are x86-64 only: on Apple Silicon, run the client with an x86-64 JDK 8 (under
    // Rosetta), e.g. ./gradlew runClient -Pclient_java_home=/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home
    val clientJavaHome = project.findProperty("client_java_home")?.toString()
    javaLauncher.set(
        if (clientJavaHome != null) null
        else javaToolchains.launcherFor { languageVersion.set(JavaLanguageVersion.of(8)) }.get()
    )
    if (clientJavaHome != null) executable = "$clientJavaHome/bin/java"
}
