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

### Releasing the mod

Set `mod_version` in `gradle.properties` (e.g. `2.0.0`, or `2.0.0-alpha.1` for a prerelease), give
`CHANGELOG.md` a `## <version>` section, and push the tag `v<version>-1.12.2`. That runs
`.github/workflows/release.yml`, which:

1. builds the mod's jar, `MoBends_1.12.2-<version>.jar`;
2. builds `Legacy-Mo-Bends-<version>.zip`, signed so that servers that deny resource packs' animation
   trust it (see `docs/content.md`, *Mo' Bends' Own Resource Packs*);
3. attaches both to the tag's GitHub release, creating it (from the changelog section) if there isn't
   one;
4. uploads the jar to Modrinth and CurseForge as version `<version>+1.12.2`, with the changelog
   section as its changelog. A version with `alpha` in it is released as an alpha, with `beta`, `rc`
   or `pre` as a beta, otherwise as a release. (The pack is uploaded too once it has its own
   pages, see [TODO.md](TODO.md); until then upload it by hand from the GitHub release.)

What the workflow needs, under *Settings → Secrets and variables → Actions* (secrets may also go in
the `release` environment, which can be made to wait for a reviewer's approval):

| Name | Kind | What |
|---|---|---|
| `MOBENDS_PACK_SIGNING_KEY` | secret | The pack signing key (the base64 line in the key file). |
| `MODRINTH_TOKEN` | secret | A Modrinth personal access token with *Create versions*, *Read projects* and *Read versions*. |
| `CURSEFORGE_TOKEN` | secret | A CurseForge API token (authors.curseforge.com, *API tokens*). |
| `MODRINTH_PROJECT_ID` | variable | The mod's Modrinth project id (or slug). |
| `CURSEFORGE_PROJECT_ID` | variable | The mod's CurseForge project id (a number). |
| `LEGACY_PACK_MODRINTH_PROJECT_ID` | variable | The pack's Modrinth project id, once it has a page. |
| `LEGACY_PACK_CURSEFORGE_PROJECT_ID` | variable | The pack's CurseForge project id, once it has a page. |

A version can't be uploaded to Modrinth or CurseForge twice. If a release fails partway, re-run the
failed jobs rather than pushing the tag again.

The pack is signed with `MOBENDS_PACK_SIGNING_KEY`. Locally, `gradle legacyPack -Ppack_signing_key=<file>`
does the same into `build/packs/`. The private key is never committed. Keep a copy in a password
manager. A lost key means new packs need a mod release that ships a new public key.

### Releasing `core`

Push a tag named `core-<version>` (e.g. `git tag core-2.0.0 && git push origin core-2.0.0`).
CI (`.github/workflows/publish-core.yml`) tests `core/` and publishes it to GitHub Packages as
`goblinbob.mobends:mobends-core:<version>`. A version can't be published twice, so bump it for every release.

Reading from GitHub Packages needs a GitHub token, even for public packages. In CI the built-in
`GITHUB_TOKEN` does; locally, use a personal access token with the `read:packages` scope.
