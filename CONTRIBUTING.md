# Contributing to Mo' Bends

Thanks for helping out! This document explains how the repository is organised, how code moves between
Minecraft versions and mod loaders, and how releases are made.

> **Status:** Mo' Bends is moving from one branch per Minecraft version to a single multi-loader,
> multi-version project (the **2.X** line). Until the [migration](#migration-plan) is finished, active
> development still happens on `1.X/forge-1.12`.

## Project layout

Mo' Bends 2.X is built from three layers:

| Layer | What it contains | Depends on Minecraft? |
|---|---|---|
| `core/` | The animation engine: Kumo, math, flux, the pack/extension format. Plain Java 8. | **No** |
| `common/` | Connects the core to vanilla models, entity state and rendering, mostly via Mixins. | Yes, vanilla only (no loader APIs) |
| `fabric/`, `neoforge/`, `forge/` | Entrypoints, events, networking, config for each loader. Kept as small as possible. | Yes |

Rules of thumb:

- If code doesn't need Minecraft, it belongs in `core/`. It must stay free of any `net.minecraft` import,
  so the 1.12.2 build can use it too.
- `common/` must never import a loader API. Anything loader-specific goes behind an interface in
  `common/` and is implemented in each loader module (looked up with `java.util.ServiceLoader`).

### Supported targets

| Minecraft | Loaders |
|---|---|
| 1.20.1 | Forge, Fabric |
| 1.21.1 | NeoForge, Fabric |
| Latest (26.x) | Fabric, NeoForge |
| 1.12.2 | Forge (maintenance only, separate branch, see below) |

## Multiple Minecraft versions: Stonecutter

All modern versions are built from **one source tree** using
[Stonecutter](https://stonecutter.kikugie.dev/). Version-specific code is fenced with comments:

```java
//? if >=1.21.2 {
applyPose(renderState);
//?} else {
/*applyPose(entity);*/
//?}
```

- A fix is made once, in one PR, for every version.
- Adding a Minecraft version means adding a target in the Stonecutter config, **not** creating a branch.
- **Only commit while the default version is active.** Switching the active version in your IDE
  rewrites the fenced comments in your working copy. Switch back to the default before committing,
  otherwise the diff fills up with comment noise. CI rejects commits made with another version active.

## Branches

```
main                     2.X: all modern Minecraft versions × all loaders (default branch)
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
1.X/forge-1.12           1.12.2 maintenance: bug fixes only
```

- Branch off `main`, open a pull request back into `main`.
- CI builds every version × loader combination and runs the `core/` tests. All of them must pass.
- The old unreleased 1.16 branches (`2.X/forge-1.16`, `master-1.16.3`) are archived as tags
  (`archive/2.X-forge-1.16`, `archive/master-1.16.3`) and no longer developed.

## The 1.12.2 branch

1.12.2 (Java 8, LWJGL 2, old rendering pipeline) can't share a build with modern versions, so it lives
on its own branch, `1.X/forge-1.12`. It does **not** merge from `main`.

Instead, it shares the animation engine by depending on a **published, pinned release of `core`**
(e.g. `goblinbob.mobends:mobends-core:2.1.0`) and shading it into the 1.12.2 jar.

To bring an engine fix to 1.12.2:

1. Fix it in `core/` on `main`.
2. Release a new `core` version (see below).
3. On `1.X/forge-1.12`, bump the `core` dependency version.

No cherry-picking or copying code between branches.

## Versioning and releases

| What | Version line | Tag | Released from |
|---|---|---|---|
| The mod (modern versions) | 2.X | `v2.0.0` | `main` |
| The engine library | follows the mod | `core-v2.0.0` | `main` |
| The mod for 1.12.2 | 1.X | `v1.x.y-1.12.2` | `1.X/forge-1.12` |

- A 2.X release ships **all targets at once**. Jars are named
  `mobends-<version>+<minecraft>-<loader>.jar`, e.g. `mobends-2.0.0+1.21.1-neoforge.jar`.
- CI builds the jars from the tag and uploads them to Modrinth and CurseForge.
- `core` gets a new release whenever the 1.12.2 branch needs a change from it, or when its API changes.

### Dropping a Minecraft version

Remove its Stonecutter target after a release. If it later needs a critical fix, create a branch from
its last release tag (e.g. `release/2.3-1.20.1`), fix, release, and leave the branch as is.

## Migration plan

1. **Extract the core.** On `1.X/forge-1.12`, move `kumo`, `math` and `flux` into a `core/` Gradle
   module with `git mv` (keeps `git log --follow` history).
2. **Create `main`.** Branch from that point, remove the 1.12-specific code, add the Stonecutter
   multi-loader layout, and port the Minecraft-facing code, starting with 1.21.1 (NeoForge + Fabric).
3. **Publish `core`.** Release the first `core` version and switch `1.X/forge-1.12` to depend on it,
   deleting its in-tree copy.
4. **Switch the default branch** on GitHub to `main`, and archive the 1.16 branches as tags.
5. **Add targets:** 1.20.1 (Forge + Fabric), then the latest Minecraft version.
