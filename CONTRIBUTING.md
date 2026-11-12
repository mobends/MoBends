# Contributing to Mo' Bends

Thanks for helping out! This document explains how the repository is organised and how releases are
made.

The version line is about what players and pack authors get: **2.X** is Mo' Bends with the
reworked animation system (Kumo animators, entity types, extensions, model definitions), 1.X the
mod before it. Mo' Bends for Minecraft 1.8.9 (Forge) lives on the `forge-1.8.9` branch, a port of
the 1.12.2 mod on `forge-1.12`, and is 2.X like it.

## Project layout

| Part | What it contains | Depends on Minecraft? |
|---|---|---|
| `mobends-core` (a dependency) | The animation engine: Kumo, math, the animator format. Plain Java 8. Developed in `core/` on `main`, and used here as a pinned release (`core_version` in `gradle.properties`). | **No** |
| root (`src/`) | The Forge 1.8.9 mod: connects the core to the vanilla models, entity state and rendering; the entities, menus, networking and config. Bundles `mobends-core` into its jar. | Yes |
| `animation-lab/` | A standalone test harness (JDK 21, no Forge) that runs the animators through scripted scenarios and checks them against recorded traces. | No (it stubs the few Minecraft classes it needs) |

Rules of thumb:

- If code doesn't need Minecraft, it belongs in the core. Change it in `core/` on `main`, release it,
  and bump `core_version` here (see *Using a new `core`* below). Never copy engine code into this branch.
- Code the lab compiles (see `modIncludes` in `animation-lab/build.gradle.kts`) must not use newer
  Java than 8; the lab compiles it with `--release 8` to catch that.

## What 1.8.9 lacks

The mod and its animators were written for 1.12.2. What 1.8.9 doesn't have is backported in
`core/compat`, under the names the code and the animators use, so the assets are the same on every
branch:

- **Hands and equipment.** `EnumHand`, `EnumHandSide`, `EntityEquipmentSlot` and `ArmPose` are the later
  versions' enums; `Living` answers what 1.12.2 asks of any living entity. The off hand is always empty,
  every entity is right-handed, only players use items (so a skeleton never draws its bow, as in vanilla
  1.8.9), a sword blocks as a shield does, and nothing flies with an elytra.
- **Entity ids.** Type files and settings name entities as later versions do (`minecraft:zombie_villager`).
  `EntityIds` maps 1.8.9's names to them, and its variants too: a zombie villager is a zombie that is a
  villager, a wither skeleton a skeleton of type 1, and the horses are told apart by type.
- **Events and render state.** 1.8.9 posts ticks, input and config changes on FML's bus (`Events`
  registers a handler on both), and its render events don't carry partial ticks (`RenderPartialTicks`).
- **Gone.** The polar bear, the elytra, the tipped and spectral arrows, and GeckoLib (no 1.8.9 release).

The animation lab plays the scenarios 1.8.9 can; `LaterVersionsOnlyTest` names the ones it can't.

## Branches

```
forge-1.8.9              Mo' Bends 2.X for Minecraft 1.8.9
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
```

- Branch off `forge-1.8.9`, open a pull request back into `forge-1.8.9`.
- CI builds the mod and runs its tests and the animation lab. All of them must pass.
- `forge-1.8.9` was branched from `forge-1.12` and ported; a change made there is ported here by hand.
  Neither merges from `main` (which builds the modern Minecraft versions); they share only the
  released core with it.
- What `main` changes that this branch has to follow is listed in TODO.md, *Keeping up with `main`*.
  A pull request on `main` that changes `core`'s API, the asset format or a shared asset comes with a
  pull request here adding its steps there.
- `1.X/forge-1.12` holds the 1.X line (up to 1.2.2) and is no longer developed.

## Versioning and releases

| What | Version line | Tag | Released from |
|---|---|---|---|
| The mod for 1.8.9 | 2.X | `v2.x.y-1.8.9` | `forge-1.8.9` |
| The engine library | follows the mod | `core-2.x.y` | `main` |

1.8.9's tags carry the Minecraft version, so they never clash with the tags of the other
versions' releases.

- The mod's version is `mod_version` in `gradle.properties` (and `ModStatics.VERSION`).
- `core` gets a new release when its API changes, or when another project needs a change from it.

### Releasing the mod

Set `mod_version` in `gradle.properties` (e.g. `2.0.0`, or `2.0.0-alpha.1` for a prerelease), give
`CHANGELOG.md` a `## <version>` section, and push the tag `v<version>-1.8.9`. That runs
`.github/workflows/release.yml`, which:

1. builds the mod's jar, `MoBends_1.8.9-<version>.jar`;
2. attaches it to the tag's GitHub release, creating it (from the changelog section) if there isn't
   one;
3. uploads the jar to Modrinth and CurseForge as version `<version>+1.8.9`, with the changelog
   section as its changelog. A version with `alpha` in it is released as an alpha, with `beta`, `rc`
   or `pre` as a beta, otherwise as a release.

The Legacy Mo' Bends resource pack (`misc/legacy-pack`), for 1.12.2 and the modern versions alike, is
signed and released from `main`, with its `v<version>` tags (see CONTRIBUTING.md there).

What the workflow needs, under *Settings → Secrets and variables → Actions* (secrets may also go in
the `release` environment, which can be made to wait for a reviewer's approval):

| Name | Kind | What |
|---|---|---|
| `MODRINTH_TOKEN` | secret | A Modrinth personal access token with *Create versions*, *Read projects* and *Read versions*. |
| `CURSEFORGE_TOKEN` | secret | A CurseForge API token (authors.curseforge.com, *API tokens*). |
| `MODRINTH_PROJECT_ID` | variable | The mod's Modrinth project id (or slug). |
| `CURSEFORGE_PROJECT_ID` | variable | The mod's CurseForge project id (a number). |

A version can't be uploaded to Modrinth or CurseForge twice. If a release fails partway, re-run the
failed jobs rather than pushing the tag again.

### Using a new `core`

The core is released from `main` (see CONTRIBUTING.md there) to GitHub Packages as
`goblinbob.mobends:mobends-core:<version>`. To build this branch with a new release, set `core_version`
in `gradle.properties`. The mod and the animation lab both use it.

### A token for GitHub Packages

Reading from GitHub Packages needs a GitHub token, even for public packages. In CI the built-in
`GITHUB_TOKEN` does (the workflows ask for `packages: read`). Locally, create a personal access token
(classic) with the `read:packages` scope and put it in `~/.gradle/gradle.properties`, outside the
repository:

```properties
gpr.user=<your GitHub username>
gpr.key=<the token>
```

Without it, Gradle warns about the missing token and fails to download `mobends-core`.
