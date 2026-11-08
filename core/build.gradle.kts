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
