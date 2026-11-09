// The Minecraft-free animation engine. Must not depend on Minecraft, a mod loader or LWJGL,
// so every Minecraft version can share it. Java 8, so the 1.12.2 branch can use it too.
plugins {
    `java-library`
    `maven-publish`
}

group = "goblinbob.mobends"
base.archivesName = "mobends-core"
// Release builds get their version from the core-<version> tag (see .github/workflows/publish-core.yml).
version = findProperty("coreVersion") ?: "dev"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
    withSourcesJar()
}

tasks.withType<JavaCompile>().configureEach {
    // Compiled against the Java 8 API, which keeps newer language features and APIs out.
    options.release = 8
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:-options")
}

repositories {
    mavenCentral()
}

dependencies {
    // Shipped by Minecraft itself (these are the versions 1.12.2 ships, the oldest target), so not bundled.
    compileOnly("com.google.code.gson:gson:2.8.0")
    compileOnly("com.google.code.findbugs:jsr305:3.0.1")

    testImplementation("com.google.code.gson:gson:2.8.0")
    testImplementation("com.google.code.findbugs:jsr305:3.0.1")
    testImplementation("junit:junit:4.13.2")
}

tasks.test {
    useJUnit()
}

publishing {
    publications {
        create<MavenPublication>("core") {
            artifactId = "mobends-core"
            from(components["java"])
        }
    }
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/mobends/MoBends")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

// Build-time tools (src/tools), not part of the published core: they sign Mo' Bends' own resource
// packs (core/pack/PackSignature), so servers that refuse resource packs' animation still trust
// them. The private key never enters the repository: CI reads it from the MOBENDS_PACK_SIGNING_KEY
// secret, and locally it is a file given with -Ppack_signing_key=<file>.
val tools by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output
    runtimeClasspath += sourceSets.main.get().output
}

dependencies {
    "toolsImplementation"("com.google.code.gson:gson:2.8.0")
}

// The packs are named after the mod's version, which stonecutter.properties.toml holds.
val modVersion: String = Regex("""(?m)^mod\.version\s*=\s*"([^"]*)"""")
    .find(rootProject.file("stonecutter.properties.toml").readText())
    ?.groupValues?.get(1)
    ?: throw GradleException("Couldn't read mod.version from stonecutter.properties.toml")

/** The private key's file, from -Ppack_signing_key (relative to the repository), or none. */
val packSigningKeyFile: Provider<File> = rootProject.layout.projectDirectory.let { root ->
    providers.gradleProperty("pack_signing_key").map { root.file(it).asFile }
}

// The Legacy Mo' Bends resource pack (misc/legacy-pack), for 1.12.2 and the modern versions alike:
// the player's and the zombie's animations as they were in 1.X. Built into build/packs/, signed
// with the key in -Ppack_signing_key, or else the MOBENDS_PACK_SIGNING_KEY environment variable.
tasks.register<JavaExec>("legacyPack") {
    description = "Builds the signed Legacy Mo' Bends resource pack from misc/legacy-pack into build/packs/."
    group = "build"
    classpath = tools.runtimeClasspath
    mainClass = "goblinbob.mobends.tools.SignPack"
    val pack = rootProject.layout.projectDirectory.dir("misc/legacy-pack").asFile
    val out = rootProject.layout.buildDirectory.file("packs/Legacy-Mo-Bends-$modVersion.zip")
    val key = packSigningKeyFile
    inputs.dir(pack)
    outputs.file(out)
    argumentProviders.add(CommandLineArgumentProvider {
        key.map { listOf("--key-file", it.absolutePath) }.getOrElse(emptyList()) +
            listOf(pack.absolutePath, out.get().asFile.absolutePath)
    })
}

// Makes a new pack signing key pair: the private key goes to the -Ppack_signing_key file, the
// public key (printed) into core/pack/PackSigningKeys.
tasks.register<JavaExec>("generatePackSigningKey") {
    description = "Makes a new key pair for signing Mo' Bends' own resource packs."
    classpath = tools.runtimeClasspath
    mainClass = "goblinbob.mobends.tools.GenPackSigningKey"
    val key = packSigningKeyFile
    argumentProviders.add(CommandLineArgumentProvider {
        listOf(key.orNull?.absolutePath
            ?: throw GradleException("Say where to write the private key: -Ppack_signing_key=<file> (outside the repository)."))
    })
}

// The tools and what they need, in core/build/tools/, so CI signs a pack with plain `java` and no
// Gradle (nor any plugin's code) in the step that holds the key.
val toolsJar by tasks.registering(Jar::class) {
    archiveClassifier = "tools"
    from(tools.output)
}
tasks.register<Sync>("installTools") {
    description = "Puts the pack signing tools and their classpath in build/tools/."
    from(toolsJar, tasks.jar, configurations[tools.runtimeClasspathConfigurationName])
    into(layout.buildDirectory.dir("tools"))
}
