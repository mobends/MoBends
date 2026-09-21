/*
 * Mo' Bends animation lab.
 *
 * A standalone project (modern JDK, no Forge) that compiles the mod's animation code
 * against a tiny set of Minecraft stubs, records "golden" bone poses from the original
 * procedural controllers, and validates the asset-driven KUMO animators against them.
 *
 * Source sets:
 *   mcstub    - minimal stand-ins for the net.minecraft / org.lwjgl classes the animation code touches
 *   reference - the mod's own animation sources, copied verbatim from ../src (plus a few shims
 *               for classes that need the Minecraft client, see src/reference/java)
 *   main      - the lab harness: scripted entities, recorder, comparator, baker
 *   test      - JUnit 5 tests: reference stability + KUMO parity
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

// Only the Minecraft-agnostic animation subset of the mod is compiled. Everything that needs
// rendering, GUI, networking or Forge stays out. Classes listed under `exclude` are replaced by
// shims in src/reference/java with the same fully qualified name.
val referenceIncludes = listOf(
    "goblinbob/mobends/core/math/**",
    "goblinbob/mobends/core/util/GUtil.java",
    "goblinbob/mobends/core/util/Tween.java",
    "goblinbob/mobends/core/util/KeyframeUtils.java",
    "goblinbob/mobends/core/util/EnumAxis.java",
    "goblinbob/mobends/core/util/GlHelper.java",
    "goblinbob/mobends/core/util/Color.java",
    "goblinbob/mobends/core/util/IColor.java",
    "goblinbob/mobends/core/util/IColorRead.java",
    "goblinbob/mobends/core/util/ColorReadonly.java",
    "goblinbob/mobends/core/util/SerialHelper.java",
    "goblinbob/mobends/core/animation/**",
    "goblinbob/mobends/core/data/EntityData.java",
    "goblinbob/mobends/core/data/LivingEntityData.java",
    "goblinbob/mobends/core/data/IEntityDataFactory.java",
    "goblinbob/mobends/core/data/OverridableProperty.java",
    "goblinbob/mobends/core/client/model/IBendsModel.java",
    "goblinbob/mobends/core/client/model/IModelPart.java",
    "goblinbob/mobends/core/client/model/ModelPartTransform.java",
    "goblinbob/mobends/core/kumo/**",
    "goblinbob/mobends/core/definition/**",
    "goblinbob/mobends/core/pack/BendsPackData.java",
    "goblinbob/mobends/core/pack/state/PackAnimationState.java",
    "goblinbob/mobends/standard/animation/**",
    "goblinbob/mobends/standard/data/**",
    "goblinbob/mobends/standard/kumo/**",
    "goblinbob/mobends/standard/UseActionType.java",
    "goblinbob/mobends/standard/AttackActionType.java",
    "goblinbob/mobends/standard/main/ModStatics.java",
)

val referenceExcludes = listOf(
    "goblinbob/mobends/core/animation/keyframe/AnimationLoader.java",
)

val referenceSrcDir = layout.buildDirectory.dir("reference-src")

val syncReferenceJava by tasks.registering(Sync::class) {
    description = "Copies the Minecraft-agnostic animation sources of the mod into the build directory."
    from(modSrc.resolve("java")) {
        include(referenceIncludes)
        exclude(referenceExcludes)
    }
    into(referenceSrcDir.map { it.dir("java") })
}

val syncReferenceKotlin by tasks.registering(Sync::class) {
    from(modSrc.resolve("kotlin")) {
        include("goblinbob/mobends/standard/animation/bit/biped/JumpAnimationBit.kt")
    }
    into(referenceSrcDir.map { it.dir("kotlin") })
}

val syncReferenceResources by tasks.registering(Sync::class) {
    from(modSrc.resolve("resources")) {
        include("assets/mobends/bends/**")
    }
    into(referenceSrcDir.map { it.dir("resources") })
}

sourceSets {
    val mcstub by creating {
        java.srcDir("src/mcstub/java")
    }

    val reference by creating {
        java.srcDir("src/reference/java")
        java.srcDir(syncReferenceJava.map { it.destinationDir })
        kotlin.srcDir("src/reference/java")
        kotlin.srcDir(syncReferenceJava.map { it.destinationDir })
        kotlin.srcDir(syncReferenceKotlin.map { it.destinationDir })
        resources.srcDir(syncReferenceResources.map { it.destinationDir })
        compileClasspath += mcstub.output
        runtimeClasspath += mcstub.output
    }

    main {
        compileClasspath += mcstub.output + reference.output
        runtimeClasspath += mcstub.output + reference.output
    }

    test {
        compileClasspath += mcstub.output + reference.output
        runtimeClasspath += mcstub.output + reference.output
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-Xlint:-options")
}

// The mod itself is built with JDK 8 (ForgeGradle for 1.12.2). Compiling its sources with
// --release 8 here keeps the lab from letting newer language features or APIs slip into src/main.
tasks.named<JavaCompile>("compileReferenceJava") {
    options.release.set(8)
}

tasks.named<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>("compileReferenceKotlin") {
    compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
}

dependencies {
    val referenceImplementation by configurations.getting
    val mcstubImplementation by configurations.getting

    mcstubImplementation("com.google.code.findbugs:jsr305:3.0.2")

    referenceImplementation("com.google.code.gson:gson:2.10.1")
    referenceImplementation("org.apache.httpcomponents:httpcore:4.4.16")
    referenceImplementation("com.google.code.findbugs:jsr305:3.0.2")
    referenceImplementation(kotlin("stdlib"))

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
    description = "Re-records the golden pose traces from the reference (procedural) animation code."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.cli.RecordGolden")
    args(projectDir.resolve("golden").absolutePath)
}

tasks.register<JavaExec>("bakeBipeds") {
    description = "Bakes the biped entities' procedural animation bits into format-2 clips in the mod resources."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.bake.BipedBake")
    args(modSrc.resolve("resources").absolutePath)
}

tasks.register<JavaExec>("bakePlayer") {
    description = "Bakes the player's procedural animation bits into format-2 clips in the mod resources."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.bake.PlayerBake")
    args(modSrc.resolve("resources").absolutePath)
}

tasks.register<Exec>("generateAnimators") {
    description = "Regenerates the biped animator JSON files from tools/gen_animators.py."
    group = "lab"
    commandLine("python3", projectDir.resolve("tools/gen_animators.py").absolutePath, modSrc.resolve("resources").absolutePath)
}

tasks.register<JavaExec>("compare") {
    description = "Runs the KUMO animators against the golden traces and prints a parity report."
    group = "lab"
    classpath = sourceSets.main.get().runtimeClasspath
    mainClass.set("goblinbob.mobends.lab.cli.CompareKumo")
    args(projectDir.resolve("golden").absolutePath)
    if (System.getProperty("lab.debugNodes") != null) systemProperty("lab.debugNodes", System.getProperty("lab.debugNodes"))
}
