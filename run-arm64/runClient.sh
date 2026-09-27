#!/bin/zsh
# Launch the Forge 1.12.2 client natively on Apple Silicon (see arm64.gradle and README.md).
cd "$(dirname "$0")/.."
export JAVA_HOME=${JAVA_HOME_8:-/Library/Java/JavaVirtualMachines/temurin-8.jdk/Contents/Home}  # Gradle 4.9 itself needs JDK 8

# sidefix/: Forge's two-constant Side enum, taken from the Forge universal jar ForgeGradle downloads.
side=run-arm64/sidefix/net/minecraftforge/fml/relauncher/Side.class
if [[ ! -f $side ]]; then
  mc=$(sed -n 's/^minecraft_version=//p' gradle.properties)
  forge=$(sed -n 's/^forge_version=//p' gradle.properties)
  jar=${GRADLE_USER_HOME:-$HOME/.gradle}/caches/forge_gradle/maven_downloader/net/minecraftforge/forge/$mc-$forge/forge-$mc-$forge-universal.jar
  if [[ ! -f $jar ]]; then
    echo "Missing $jar: run ./gradlew build once so ForgeGradle downloads Forge." >&2
    exit 1
  fi
  unzip -q -o "$jar" net/minecraftforge/fml/relauncher/Side.class -d run-arm64/sidefix || exit 1
fi

exec ./gradlew runClient -I run-arm64/arm64.gradle "$@"
