# Mo' Bends
[![CurseForge Downloads](http://cf.way2muchnoise.eu/231347.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends) [![Mod Versions](http://cf.way2muchnoise.eu/versions/231347.svg)](https://www.curseforge.com/minecraft/mc-mods/mo-bends) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/dc7fa82e8d904f65b33b948ed093c21f)](https://app.codacy.com/gh/mobends/MoBends?utm_source=github.com&utm_medium=referral&utm_content=mobends/MoBends&utm_campaign=Badge_Grade_Dashboard)


![GitHub code size in bytes](https://img.shields.io/github/languages/code-size/mobends/MoBends.svg?style=for-the-badge)
[![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=for-the-badge)](http://makeapullrequest.com)
[![GitHub pull requests](https://img.shields.io/github/issues-pr/mobends/MoBends.svg?style=for-the-badge)](https://github.com/mobends/MoBends/pulls)
[![GitHub issues](https://img.shields.io/github/issues-raw/mobends/MoBends.svg?style=for-the-badge)](https://github.com/mobends/MoBends/issues)

A Minecraft mod that adds more realistic looking animations to the inhabitants of your blocky world.

## Discord
If you'd like to be a part of Mo' Bends' development, see the progress, or just hang out, join our Discord server!

[![Discord](https://img.shields.io/discord/386940930739011584.svg?style=for-the-badge&logo=discord&logoColor=white)](https://discord.gg/JqgWRgdkvx)

Say you came from GitHub if you decide to come by! Hope to see you there

## Local Development Setup
Install a Java Development Kit (JDK) appropriate for the Minecraft version you are developing for. For Minecraft 1.12.2, use JDK 8.
I personally use the [Eclipse Temurin JDK](https://adoptium.net/temurin/releases?version=8&os=any&arch=any).

Run the game with `./gradlew runClient` (or `./gradlew runServer`), or from IntelliJ IDEA with the run configurations
`./gradlew genIntellijRuns` makes. `./gradlew build` builds the mod (`build/libs/*-all.jar` is the one to install) and runs
the tests; the animation lab (`animation-lab/`, see its README) checks the animations against recorded traces.
On Apple Silicon, [run-arm64/](run-arm64/README.md) runs the client natively.
[CONTRIBUTING.md](CONTRIBUTING.md) and [docs/](docs/) explain how the project is put together.

### Playing under a fixed name
The development client picks a random name (`Player123`) on every launch. To test entity types that
select players by name (see `misc/examples/player-name-type`), pick one:
- Gradle: `./gradlew runClient -Pmc_username=BendyTester` (also works with `run-arm64/runClient.sh -Pmc_username=BendyTester`).
- IntelliJ IDEA: add `--username BendyTester` to the *Program arguments* of the client run configuration.

### Troubleshooting
- IntelliJ freaks out and can't find symbols in the project, but compiles fine.
    - `File > Invalidate Caches/Restart` works like a charm <3
- Crashing on a NullPointerException inside FML.
    - Click "Download Sources" in the Gradle sidebar

## Creating addons
Addons are a way to extend the functionality of Mo' Bends, e.g. adding support for new mobs.

There's an example addon that I set up a while back, but the sources for CustomNPCs are
no longer available, which makes it impossible to use. I'm looking for a replacement soon.

[CustomNPCs Support Addon](https://github.com/mobends/mobends-addon-customnpcs)
