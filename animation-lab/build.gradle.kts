/*
 * Mo' Bends animation lab.
 *
 * A standalone project (JDK 21, no Minecraft, no loader) that compiles the mod's animation code
 * against a small set of Minecraft stand-ins, runs the animators through scripted scenarios, and
 * checks their bone poses against the "golden" traces.
 *
 * Source sets:
 *   mcstub - stand-ins for the Minecraft 1.21.1 classes (Mojang's names) the animation code touches
 *   mod    - the mod's own animation sources, copied verbatim from ../src and ../core/src (plus a few shims
 *            for classes that need the Minecraft client or a loader, see src/mod/java)
 *   main   - the lab harness: scripted entities, recorder, comparator
 *   test   - JUnit 5 tests: KUMO parity, side effects, expressions, clips, types
 */
plugins {
    java
}

repositories {
    maven { url = uri("https://repo1.maven.org/maven2") }
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

// The mod's sources, shared by every modern Minecraft version and loader.
val modSrc = file("../src/main")
// The animators, models and types, shared by every Minecraft version.
val modResources = file("../src/main/resources")
// The Minecraft-free engine (Kumo, math), shared with the 1.12.2 mod (see CONTRIBUTING.md).
val coreSrc = file("../core/src/main")

// Only the animation subset of the mod is compiled: the entity data, the animator glue, the model
// definitions and the types. Everything that needs rendering, GUI, networking or a loader stays out;
// the few classes the subset needs from there are replaced by shims in src/mod/java with the same
// fully qualified name.
val modIncludes = listOf(
    "goblinbob/mobends/core/data/**",
    "goblinbob/mobends/core/kumo/**",
    "goblinbob/mobends/core/definition/**",
    "goblinbob/mobends/core/types/TypeOrder.java",
    "goblinbob/mobends/core/types/selector/**",
    "goblinbob/mobends/core/types/EntityTypeDefinition.java",
    "goblinbob/mobends/core/types/ExtensionDefinition.java",
    "goblinbob/mobends/core/types/Extension.java",
    "goblinbob/mobends/core/client/model/ModelPartTransform.java",
    "goblinbob/mobends/core/client/model/ModelRoots.java",
    "goblinbob/mobends/core/client/model/TreeRoot.java",
    "goblinbob/mobends/core/client/definition/ModelPartLookup.java",
    "goblinbob/mobends/core/util/GsonResources.java",
    "goblinbob/mobends/core/util/Ids.java",
    "goblinbob/mobends/core/util/WildcardPattern.java",
    "goblinbob/mobends/core/vanilla/VanillaNames.java",
    "goblinbob/mobends/core/vanilla/VanillaEntityFields.java",
    "goblinbob/mobends/standard/data/**",
    "goblinbob/mobends/standard/kumo/**",
    "goblinbob/mobends/standard/UseActionType.java",
    "goblinbob/mobends/standard/ItemActions.java",
    "goblinbob/mobends/standard/AttackActionType.java",
)

val modSrcDir = layout.buildDirectory.dir("mod-src")

val syncModJava by tasks.registering(Sync::class) {
    description = "Copies the animation sources of the mod and the engine into the build directory."
    from(modSrc.resolve("java")) {
        include(modIncludes)
    }
    // The whole engine.
    from(coreSrc.resolve("java"))
    into(modSrcDir.map { it.dir("java") })
}

val syncModResources by tasks.registering(Sync::class) {
    from(modResources) {
        include("assets/mobends/bends/**")
    }
    // The legacy resource pack's animators replace the mod's at the same keys in the game; here they
    // sit under their own namespace (mobends_legacy:) beside them, so LegacyPackTest can load both.
    from(file("../misc/legacy-pack/assets/mobends/bends")) {
        include("animators/*.json")
        into("assets/mobends_legacy/bends")
    }
    // What the pack keeps of its own (the old clips, the biped animator they're played by) is under
    // legacy/, at keys the mod doesn't have: the same keys here as in the game.
    from(file("../misc/legacy-pack/assets/mobends/bends")) {
        include("animators/legacy/**", "animations/legacy/**")
        into("assets/mobends/bends")
    }
    into(modSrcDir.map { it.dir("resources") })
}

sourceSets {
    val mcstub by creating {
        java.srcDir("src/mcstub/java")
    }

    val mod by creating {
        java.srcDir("src/mod/java")
        java.srcDir(syncModJava.map { it.destinationDir })
        resources.srcDir(syncModResources.map { it.destinationDir })
        compileClasspath += mcstub.output
        runtimeClasspath += mcstub.output
    }

    main {
        compileClasspath += mcstub.output + mod.output
        runtimeClasspath += mcstub.output + mod.output
    }

    test {
        compileClasspath += mcstub.output + mod.output
        runtimeClasspath += mcstub.output + mod.output
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:-options")
}

dependencies {
    val modImplementation by configurations.getting
    val modRuntimeOnly by configurations.getting
    val mcstubImplementation by configurations.getting

    mcstubImplementation("com.google.code.findbugs:jsr305:3.0.2")

    // What Minecraft 1.21.1 ships with, and the mod's sources use.
    modImplementation("com.google.code.gson:gson:2.10.1")
    modImplementation("com.google.code.findbugs:jsr305:3.0.2")
    modImplementation("org.slf4j:slf4j-api:2.0.9")
    modRuntimeOnly("org.slf4j:slf4j-simple:2.0.9")

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.slf4j:slf4j-api:2.0.9")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.9")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    // Lets tests find the committed golden traces regardless of the working directory.
    systemProperty("mobends.lab.root", projectDir.absolutePath)
}

tasks.register<JavaExec>("record") {
    description = "Accepts the animators' current output as the golden traces of the named scenarios."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.cli.RecordGolden")
    args(projectDir.resolve("golden").absolutePath)
}

tasks.register<Exec>("generateAnimators") {
    description = "Regenerates the animator JSON files and hand-authored clips from tools/gen_animators.ts (needs Bun)."
    group = "lab"
    commandLine("bun", projectDir.resolve("tools/gen_animators.ts").absolutePath, modResources.absolutePath)
}

tasks.register<JavaExec>("compare") {
    description = "Runs the KUMO animators against the golden traces and prints a parity report."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.cli.CompareKumo")
    args(projectDir.resolve("golden").absolutePath)
    if (System.getProperty("lab.debugNodes") != null) systemProperty("lab.debugNodes", System.getProperty("lab.debugNodes"))
}
