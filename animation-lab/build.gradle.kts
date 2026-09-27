/*
 * Mo' Bends animation lab.
 *
 * A standalone project (modern JDK, no Forge) that compiles the mod's animation code
 * against a tiny set of Minecraft stubs, runs the animators through scripted scenarios, and
 * checks their bone poses against the "golden" traces.
 *
 * Source sets:
 *   mcstub - minimal stand-ins for the net.minecraft / org.lwjgl classes the animation code touches
 *   mod    - the mod's own animation sources, copied verbatim from ../src and ../core/src (plus a few shims
 *            for classes that need the Minecraft client, see src/mod/java)
 *   main   - the lab harness: scripted entities, recorder, comparator
 *   test   - JUnit 5 tests: KUMO parity, side effects, expressions, clips, types
 */
plugins {
    java
    kotlin("jvm") version "2.0.21"
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

kotlin {
    jvmToolchain(21)
}

val modSrc = file("../src/main")
// The Minecraft-free engine (Kumo, math), split out of src/main (see CONTRIBUTING.md).
val coreSrc = file("../core/src/main")

// Only the Minecraft-agnostic animation subset of the mod is compiled. Everything that needs
// rendering, GUI, networking or Forge stays out; the few classes the subset needs from there are
// replaced by shims in src/mod/java with the same fully qualified name.
val modIncludes = listOf(
    "goblinbob/mobends/core/math/**",
    "goblinbob/mobends/core/util/GUtil.java",
    "goblinbob/mobends/core/util/Tween.java",
    "goblinbob/mobends/core/util/EnumAxis.java",
    "goblinbob/mobends/core/util/GlHelper.java",
    "goblinbob/mobends/core/util/GsonResources.java",
    "goblinbob/mobends/core/util/Color.java",
    "goblinbob/mobends/core/util/IColor.java",
    "goblinbob/mobends/core/util/IColorRead.java",
    "goblinbob/mobends/core/util/ColorReadonly.java",
    "goblinbob/mobends/core/animation/**",
    "goblinbob/mobends/core/data/EntityData.java",
    "goblinbob/mobends/core/data/LivingEntityData.java",
    "goblinbob/mobends/core/data/IEntityDataFactory.java",
    "goblinbob/mobends/core/client/model/IBendsModel.java",
    "goblinbob/mobends/core/client/model/IModelPart.java",
    "goblinbob/mobends/core/client/model/ModelPartTransform.java",
    "goblinbob/mobends/core/kumo/**",
    "goblinbob/mobends/core/definition/**",
    "goblinbob/mobends/core/types/TypeOrder.java",
    "goblinbob/mobends/core/types/EntityTypeDefinition.java",
    "goblinbob/mobends/core/types/ExtensionDefinition.java",
    "goblinbob/mobends/core/types/Extension.java",
    "goblinbob/mobends/standard/data/**",
    "goblinbob/mobends/standard/kumo/**",
    "goblinbob/mobends/standard/UseActionType.java",
    "goblinbob/mobends/standard/ItemActions.java",
    "goblinbob/mobends/standard/AttackActionType.java",
    "goblinbob/mobends/core/ModStatics.java",
)

val modSrcDir = layout.buildDirectory.dir("mod-src")

val syncModJava by tasks.registering(Sync::class) {
    description = "Copies the Minecraft-agnostic animation sources of the mod into the build directory."
    from(modSrc.resolve("java")) {
        include(modIncludes)
    }
    from(coreSrc.resolve("java")) {
        include(modIncludes)
    }
    into(modSrcDir.map { it.dir("java") })
}

val syncModResources by tasks.registering(Sync::class) {
    from(modSrc.resolve("resources")) {
        include("assets/mobends/bends/**")
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
        kotlin.srcDir("src/mod/java")
        kotlin.srcDir(syncModJava.map { it.destinationDir })
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

// The mod itself is built with JDK 8 (ForgeGradle for 1.12.2). Compiling its sources with
// --release 8 here keeps the lab from letting newer language features or APIs slip into src/main.
tasks.named<JavaCompile>("compileModJava") {
    options.release.set(8)
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileModKotlin") {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
}

dependencies {
    val modImplementation by configurations.getting
    val mcstubImplementation by configurations.getting

    mcstubImplementation("com.google.code.findbugs:jsr305:3.0.2")

    modImplementation("com.google.code.gson:gson:2.10.1")
    modImplementation("org.apache.httpcomponents:httpcore:4.4.16")
    modImplementation("com.google.code.findbugs:jsr305:3.0.2")
    modImplementation(kotlin("stdlib"))

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("org.apache.httpcomponents:httpcore:4.4.16")

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
    commandLine("bun", projectDir.resolve("tools/gen_animators.ts").absolutePath, modSrc.resolve("resources").absolutePath)
}

tasks.register<JavaExec>("compare") {
    description = "Runs the KUMO animators against the golden traces and prints a parity report."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.cli.CompareKumo")
    args(projectDir.resolve("golden").absolutePath)
    if (System.getProperty("lab.debugNodes") != null) systemProperty("lab.debugNodes", System.getProperty("lab.debugNodes"))
}
