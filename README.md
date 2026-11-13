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

## Credits
Thanks to [ThatSoulyGuy](https://github.com/ThatSoulyGuy) and [Sxilverr](https://github.com/Sxilverrr) ([YouTube](https://www.youtube.com/@Sxilverr))
for keeping the spirit of Mo' Bends alive through their modern port
([CurseForge](https://www.curseforge.com/minecraft/mc-mods/mo-bends-unofficial-modern-port),
[Modrinth](https://modrinth.com/mod/mo-bends-unofficial-modern-port),
[source](https://github.com/ThatSoulyGuy/MoBends)), and to [Tommsy64](https://github.com/Tommsy64) for coding support.

## Local Development Setup
You only need a Java Development Kit (any recent one, e.g. [Eclipse Temurin](https://adoptium.net/temurin/releases))
to start Gradle: it downloads the JDKs the build needs by itself (JDK 25 to run Gradle, JDK 25 for Minecraft 26.3, JDK 21 for 1.21.1, JDK 17 for 1.20.1).
If you use [mise](https://mise.jdx.dev), `mise.toml` picks that JDK for you, and the one for every other branch when you switch to it.

`main` builds every modern Minecraft version and loader from one source tree with [Stonecutter](https://stonecutter.kikugie.dev/).
`./gradlew build` builds them all (the jars land in `versions/<minecraft>-<loader>/build/libs/`) and runs the tests;
`./gradlew :1.21.1-fabric:runClient` (or `:1.21.1-neoforge:runClient`, `:1.20.1-forge:runClient`, `runServer`) runs the game.
The Stonecutter plugin for IntelliJ IDEA (or the `Set active project to …` Gradle tasks) switches the
version and loader the IDE works on. The animation lab (`animation-lab/`, see its README) checks the animations
against recorded traces.
[CONTRIBUTING.md](CONTRIBUTING.md) and [docs/](docs/) explain how the project is put together.

Mo' Bends for Minecraft 1.12.2 is developed on the [`forge-1.12`](https://github.com/mobends/MoBends/tree/forge-1.12) branch.

### Troubleshooting
- IntelliJ freaks out and can't find symbols in the project, but compiles fine.
    - `File > Invalidate Caches/Restart` works like a charm <3

## Creating addons
Addons are a way to extend the functionality of Mo' Bends, e.g. adding support for new mobs.

There's an example addon that I set up a while back, but the sources for CustomNPCs are
no longer available, which makes it impossible to use. I'm looking for a replacement soon.

[CustomNPCs Support Addon](https://github.com/mobends/mobends-addon-customnpcs)
