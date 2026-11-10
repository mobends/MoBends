# Contributing to Mo' Bends

Thanks for helping out! This document explains how the repository is organised and how releases are
made.

The version line is about what players and pack authors get: **2.X** is Mo' Bends with the
reworked animation system (Kumo animators, entity types, extensions, model definitions), 1.X the
mod before it. `main` builds Mo' Bends 2.X for the modern Minecraft versions and loaders (see
[TODO.md](TODO.md) for the targets). Mo' Bends for Minecraft 1.12.2 (Forge) lives on the
`forge-1.12` branch.

## Project layout

| Part | What it contains | Depends on Minecraft? |
|---|---|---|
| `core/` | The animation engine: Kumo, math, the animator format. Plain Java 8, shared with `forge-1.12`. | **No** |
| `src/` | The mod for every modern Minecraft version and loader, as one source tree (see *One source tree* below). Compiled together with `core/` into each jar. | Yes |
| `animation-lab/` | A standalone test harness (JDK 21, no Minecraft) that compiles the mod's animation code from `src/` and `core/`, runs the animators through scripted scenarios and checks them against recorded traces. | No (it has stand-ins for the few 1.21.1 classes that code touches) |

Rules of thumb:

- If code doesn't need Minecraft, it belongs in `core/`. It must stay free of any `net.minecraft`
  import, so every Minecraft version (1.12.2 included) can share it, and it must compile as Java 8
  (`core/` is built with `--release 8`).
- The lab compiles part of `src/` (see `modIncludes` in `animation-lab/build.gradle.kts`) against
  stand-ins for the Minecraft classes it touches (`animation-lab/src/mcstub/java`), and shims for the
  mod's classes it needs but doesn't compile (`animation-lab/src/mod/java`). Code there that starts
  using another Minecraft class or member needs it added to the stand-ins: the lab's build says which.

## One source tree

Every Minecraft version and loader is built from `src/` by [Stonecutter](https://stonecutter.kikugie.dev/).
Each combination is a *version node*, `versions/<minecraft>-<loader>` (generated, not committed), built
by `build.<loader>.gradle.kts`; their versions and dependencies are in `stonecutter.properties.toml`.
Code that differs between them is fenced with comments, which Stonecutter comments in and out:

```java
//? if >=1.21.2 {
applyPose(renderState);
//?} else {
/*applyPose(entity);*/
//?}
```

- `src/` always holds the code of one node, the *active* one (`stonecutter active` in
  `stonecutter.gradle.kts`). The IDE only compiles that one; `./gradlew build` builds them all.
- Switch the active node with the Stonecutter IntelliJ plugin or the `Set active project to <node>`
  Gradle tasks, never by editing the files by hand.
- **Only commit with the default node active** (`vcsVersion` in `settings.gradle.kts`, now
  `1.21.1-fabric`). Switching rewrites the fenced comments in your working copy; switch back
  (`./gradlew "Reset active project"`) before committing, or the diff fills up with comment noise.
  CI rejects it otherwise.
- Where vanilla's API differs between versions, prefer one small helper that fences it once over
  fences at every call: `core.client.VanillaDraw` (vertices, colours, poses; a colour is one ARGB
  `int` everywhere, as 1.21 has it) and `core.util.Ids` (resource locations). Write a condition with
  braces (`//? if >=1.21 {` … `//?}`), even around one line.
- A fix is made once, in one pull request, for every version. Adding a Minecraft version means adding
  a node in `settings.gradle.kts` (and its properties), **not** creating a branch.

### Loader code

Most of the mod talks to vanilla Minecraft only (mostly through Mixins), the same on every loader.
What does need a loader lives in its own package:

| Package | What |
|---|---|
| `goblinbob.mobends` and the rest | Loader-independent. **Must not use a loader API** (`net.fabricmc`, `net.neoforged`). |
| `goblinbob.mobends.platform` | `Platform`: what the loader-independent code needs from the loader. |
| `goblinbob.mobends.fabric` | Fabric's entrypoint and `Platform`. Every file is fenced whole in `//? if fabric {`. |
| `goblinbob.mobends.neoforge` | NeoForge's entrypoint and `Platform`. Every file is fenced whole in `//? if neoforge {`. |

When the loader-independent code needs something from the loader, add it to `Platform` and implement
it in both loader packages. Hooks into the game are vanilla Mixins (`goblinbob.mobends.mixin`,
`mobends.mixins.json`) wherever they can be, so they're written once for every loader.

Fabric Loader alone doesn't put mods' assets in the resource manager (NeoForge does), nor sends a
mod's payloads, so the Fabric jar bundles Fabric API's resource loader and networking modules and
their base module, jar-in-jar (`build.fabric.gradle.kts`). Players don't need to install Fabric API;
nothing else from it is used. Mod Menu's API is compiled against (not bundled), for its Config button
(`fabric.MoBendsModMenu`, the `modmenu` entrypoint); without Mod Menu it's never loaded. CI
(`.github/scripts/check-sources.sh`) checks that no other package
uses a loader API, and that `src/` is committed with the default node active. Run it before
pushing if you like.

## Building and running

Gradle runs on JDK 25 and builds 1.21.1 with JDK 21, 1.20.1 with JDK 17. You don't need to install any: Gradle
downloads them (`gradle/gradle-daemon-jvm.properties`, and the toolchains of the build scripts).

| Command | What |
|---|---|
| `./gradlew build` | Builds every node, and tests `core/`. Jars in `versions/<node>/build/libs/`. |
| `./gradlew buildAndCollect` | The same, then copies every jar into `build/libs/<version>/`. |
| `./gradlew :1.21.1-fabric:runClient` | Runs a node's client (`runServer` for its server). Every node shares the `run/` directory. |
| `./gradlew :1.21.1-fabric:runClient -Pgeckolib` | The same, with GeckoLib (optional for Mo' Bends) installed. |
| `./gradlew :core:test` | Tests the core. |
| `cd animation-lab && ./gradlew test` | Runs the animation lab (see its README). |

## Branches

```
main                     Mo' Bends 2.X for the modern Minecraft versions × loaders
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
forge-1.12               Mo' Bends 2.X for Minecraft 1.12.2
```

- Branch off `main`, open a pull request back into `main`. For 1.12.2, branch off `forge-1.12`
  instead; its CONTRIBUTING.md describes how 1.12.2 is built and released.
- CI builds every node and runs the `core/` tests and the animation lab. All of them must pass.
- `forge-1.12` doesn't merge from `main`. It shares the engine by depending on a published, pinned
  release of `core` (see [TODO.md](TODO.md)): an engine fix is made in `core/` on `main`, released,
  and the dependency bumped on `forge-1.12`.
- A pull request here that changes something `forge-1.12` has to follow comes with a pull request into
  `forge-1.12` that adds the steps to its TODO.md, *Keeping up with `main`*, under this PR's number:
  a change to `core`'s API (and the release it waits for), to the asset format (a new definition or
  animator key), to an asset both branches ship, or to the lab's goldens of a scenario both have. Say
  what to change there and how to check it, not how it was done here. A change that 1.12.2 doesn't
  need (a loader, a Mixin, a mob 1.12.2 doesn't have) needs nothing there.
- `1.X/forge-1.12` holds the 1.X line (up to 1.2.2) and is no longer developed.

## Versioning and releases

| What | Version line | Tag | Released from |
|---|---|---|---|
| The mod for the modern versions | 2.X | `v2.x.y` | `main` |
| The mod for 1.12.2 | 2.X | `v2.x.y-1.12.2` | `forge-1.12` |
| The engine library | follows the mod | `core-2.x.y` | `main` |

- The mod's version is `mod.version` in `stonecutter.properties.toml`. Jars are named
  `mobends-<version>+<minecraft>-<loader>.jar`, e.g. `mobends-2.0.0+1.21.1-neoforge.jar`.
- A modern release ships every node at once (see *Releasing the mod* below).
- `core` gets a new release when its API changes, or when another branch needs a change from it.

### Releasing the mod

Set `mod.version` in `stonecutter.properties.toml` (e.g. `2.0.0`, or `2.0.0-alpha.2` for a
prerelease), give `CHANGELOG.md` a `## <version>` section, merge that into `main`, and push the tag
`v<version>` on `main` (e.g. `git tag v2.0.0 && git push origin v2.0.0`). That runs
`.github/workflows/release.yml`, which:

1. checks the tag matches `mod.version` and is on `main`, and stops otherwise;
2. builds every node (`./gradlew buildAndCollect`), and the signed Legacy Mo' Bends resource pack
   (`misc/legacy-pack`, for 1.12.2 and the modern versions alike), `Legacy-Mo-Bends-<version>.zip`;
3. attaches every jar, `mobends-<version>+<minecraft>-<loader>.jar`, and the pack to the tag's GitHub
   release, creating it (from the changelog section) if there isn't one. A version with a `-` suffix
   is a prerelease;
4. uploads each jar to Modrinth and CurseForge as its own version, `<version>+<minecraft>-<loader>`,
   for its loader and the Minecraft releases in its `mod.mc_releases`, with the changelog section as
   its changelog. A version with `alpha` in it is released as an alpha, with any other `-` suffix as
   a beta, otherwise as a release;
5. uploads the pack to its own Modrinth and CurseForge projects, as `<version>`, for 1.12.2 and every
   node's Minecraft releases. **Also switched off for now** (TODO.md, *Legacy Mo' Bends pack*).

A new node needs no change to the workflow: give its Minecraft version a `mod.mc_releases` in
`stonecutter.properties.toml`.

The uploads use the same repository variables and secrets as the 1.12.2 release (see CONTRIBUTING.md
on `forge-1.12`), under *Settings → Secrets and variables → Actions*:

| Name | Kind | What |
|---|---|---|
| `MODRINTH_TOKEN` | secret | A Modrinth personal access token with *Create versions*, *Read projects* and *Read versions*. |
| `CURSEFORGE_TOKEN` | secret | A CurseForge API token (authors.curseforge.com, *API tokens*). |
| `MODRINTH_PROJECT_ID` | variable | The mod's Modrinth project id (or slug). |
| `CURSEFORGE_PROJECT_ID` | variable | The mod's CurseForge project id (a number). |
| `LEGACY_PACK_MODRINTH_PROJECT_ID` | variable | The Legacy Mo' Bends pack's Modrinth project id (or slug). |
| `LEGACY_PACK_CURSEFORGE_PROJECT_ID` | variable | The Legacy Mo' Bends pack's CurseForge project id (a number). |
| `MOBENDS_PACK_SIGNING_KEY` | secret | The private key that signs Mo' Bends' own resource packs (PKCS#8, base64; see docs/content.md). |

A version can't be uploaded to Modrinth or CurseForge twice. If a release fails partway, re-run the
failed jobs rather than pushing the tag again.

The pack is signed with `MOBENDS_PACK_SIGNING_KEY`. Locally, `./gradlew :core:legacyPack
-Ppack_signing_key=<file>` does the same into `build/packs/`. The private key is never committed. Keep
a copy in a password manager. A lost key means new packs need a mod release that ships a new public
key.

### Releasing `core`

Push a tag named `core-<version>` on `main` (e.g. `git tag core-2.0.0 && git push origin core-2.0.0`).
CI (`.github/workflows/publish-core.yml`) tests `core/` and publishes it to GitHub Packages as
`goblinbob.mobends:mobends-core:<version>`. A version can't be published twice, so bump it for every release.

Reading from GitHub Packages needs a GitHub token, even for public packages. In CI the built-in
`GITHUB_TOKEN` does; locally, use a personal access token with the `read:packages` scope.
