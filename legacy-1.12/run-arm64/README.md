# Running the 1.12.2 client natively on Apple Silicon

`./gradlew runClient` starts Minecraft 1.12.2 with x86 LWJGL natives, which an arm64 JDK can't
load. `arm64.gradle` (a Gradle init script) swaps in an arm64 JDK 8, arm64 natives and a few
libraries; `runClient.sh` runs the client with it:

```sh
run-arm64/runClient.sh
```

Gradle itself still runs on any JDK 8 (`JAVA_HOME_8`, or Temurin 8 at its default location).

## What's tracked

- `arm64.gradle`, `runClient.sh`: the script and the launcher.

`runClient.sh` also fills `sidefix/` (ignored by git) on its first run: Forge's real two-constant
`Side` enum, taken from the Forge universal jar ForgeGradle downloads. The mapped Forge jar of
ForgeGradle 3 ships a three-constant `Side` (with `BUKKIT`), which throws in
`NetworkRegistry.newChannel`; `sidefix/` goes first on the classpath.

## What you download (ignored by git)

Run these from the repository root.

### `jdk8-arm64/`: an arm64 JDK 8

Temurin has no JDK 8 for macOS on arm64; Azul Zulu does. Download the macOS ARM 64-bit JDK 8
`.tar.gz` from <https://www.azul.com/downloads/?version=java-8-lts&os=macos&architecture=arm-64-bit&package=jdk>
and put its `zulu-8.jdk` folder here as `jdk8-arm64` (so that
`run-arm64/jdk8-arm64/Contents/Home/bin/java` exists). Tested with Zulu 8.96.0.205 (1.8.0_504).

### `lib/`: libraries that work on arm64

```sh
mkdir -p run-arm64/lib && cd run-arm64/lib
curl -LO https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl/2.9.4-nightly-20150209/lwjgl-2.9.4-nightly-20150209.jar
curl -LO https://libraries.minecraft.net/org/lwjgl/lwjgl/lwjgl_util/2.9.4-nightly-20150209/lwjgl_util-2.9.4-nightly-20150209.jar
curl -LO https://libraries.minecraft.net/com/mojang/text2speech/1.11.3/text2speech-1.11.3.jar
curl -LO https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar
curl -LO https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar
curl -L -o java-objc-bridge-1.1-mmachina.jar https://github.com/MinecraftMachina/Java-Objective-C-Bridge/releases/download/1.1.0-mmachina.1/java-objc-bridge-1.1.jar
cd ../..
```

SHA-1 of each:

```
697517568c68e78ae0b4544145af031c81082dfe  lwjgl-2.9.4-nightly-20150209.jar
d51a7c040a721d13efdfbd34f8b257b2df882ad0  lwjgl_util-2.9.4-nightly-20150209.jar
f378f889797edd7df8d32272c06ca80a1b6b0f58  text2speech-1.11.3.jar
1200e7ebeedbe0d10062093f32925a912020e747  jna-5.13.0.jar
88e9a306715e9379f3122415ef4ae759a352640d  jna-platform-5.13.0.jar
369a83621e3c65496348491e533cb97fe5f2f37d  java-objc-bridge-1.1-mmachina.jar
```

### `natives/`: arm64 LWJGL 2 and OpenAL

From the MinecraftMachina build of LWJGL 2 (the one Prism Launcher uses):

```sh
mkdir -p run-arm64/natives && cd run-arm64/natives
curl -L -o natives.jar https://github.com/MinecraftMachina/lwjgl/releases/download/2.9.4-20150209-mmachina.2/lwjgl-platform-2.9.4-nightly-20150209-natives-osx.jar
unzip -o natives.jar && rm natives.jar
cd ../..
```

That gives `liblwjgl.dylib` (SHA-1 `fb4696a83c290bb79d52485811e4fbeba08a68fa`) and `openal.dylib`
(`034e01a72f66e5200928be84581160bc90a4c56e`).

The setup this was written from also had a universal `libjcocoa.dylib` and `libjinput-osx.jnilib`
in `natives/`, whose source isn't recorded (they don't match the copies in the Objective-C bridge
jar or in JInput 2.0.9 / 2.0.10). If the client fails to start without them, copy them from a
working arm64 1.12.2 instance of Prism Launcher.
