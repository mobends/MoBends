# Contributing to Mo' Bends

Thanks for helping out! This document explains how the repository is organised and how releases are
made.

The version line is about what players and pack authors get: **2.X** is Mo' Bends with the
reworked animation system (Kumo animators, entity types, extensions, model definitions), 1.X the
mod before it. Mo' Bends for Minecraft 1.12.2 (Forge) lives on the `forge-1.12` branch and is
2.X from 2.0.0 on; the builds for other Minecraft versions and loaders planned in
[TODO.md](TODO.md) will be 2.X too.

## Project layout

| Part | What it contains | Depends on Minecraft? |
|---|---|---|
| `core/` | The animation engine: Kumo, math, the animator format. Plain Java 8. | **No** |
| root (`src/`) | The Forge 1.12.2 mod: connects the core to the vanilla models, entity state and rendering; the entities, menus, networking and config. Bundles `core/` into its jar. | Yes |
| `animation-lab/` | A standalone test harness (JDK 21, no Forge) that runs the animators through scripted scenarios and checks them against recorded traces. | No (it stubs the few Minecraft classes it needs) |

Rules of thumb:

- If code doesn't need Minecraft, it belongs in `core/`. It must stay free of any `net.minecraft`
  import, so other Minecraft versions can share it.
- Code the lab compiles (see `modIncludes` in `animation-lab/build.gradle.kts`) must not use newer
  Java than 8; the lab compiles it with `--release 8` to catch that.

## Branches

```
forge-1.12               Mo' Bends 2.X for Minecraft 1.12.2
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
```

- Branch off `forge-1.12`, open a pull request back into `forge-1.12`.
- CI builds the mod and runs the `core/` tests and the animation lab. All of them must pass.
- `1.X/forge-1.12` holds the 1.X line (up to 1.2.2) and is no longer developed.

## Versioning and releases

| What | Version line | Tag | Released from |
|---|---|---|---|
| The mod for 1.12.2 | 2.X | `v2.x.y-1.12.2` | `forge-1.12` |
| The engine library | follows the mod | `core-2.x.y` | `forge-1.12` |

1.12.2's tags carry the Minecraft version, so they never clash with the tags of the other
versions' releases.

- The mod's version is `mod_version` in `gradle.properties` (and `ModStatics.VERSION`).
- `core` gets a new release when its API changes, or when another project needs a change from it.

### Releasing `core`

Push a tag named `core-<version>` (e.g. `git tag core-2.0.0 && git push origin core-2.0.0`).
CI (`.github/workflows/publish-core.yml`) tests `core/` and publishes it to GitHub Packages as
`goblinbob.mobends:mobends-core:<version>`. A version can't be published twice, so bump it for every release.

Reading from GitHub Packages needs a GitHub token, even for public packages. In CI the built-in
`GITHUB_TOKEN` does; locally, use a personal access token with the `read:packages` scope.
