# TODO

## Web animation editor

The *Customize* entry of the Mo' Bends menu (`GuiBendsMenu`) is meant to open the web animation
editor, which is in the works. It opens the `officialAnimationEditorUrl` of
[static-api.json](https://github.com/mobends/mobends-resources/blob/master/static-api.json)
(`EditorLink`), currently `https://mobends.com/roadmap`, which is also the fallback.

- [ ] Point `officialAnimationEditorUrl` at the web editor once it is live (no release needed).
- [ ] Decide how animations made in the editor get into the game (a resource pack to download, or
      something the mod fetches).

## Multiple Minecraft versions and loaders

The plan is to build every modern Minecraft version and loader from one source tree, sharing the
engine (`core/`) with the 1.12.2 branch. They are 2.X like 1.12.2 (see CONTRIBUTING.md): the same
animation system and pack formats.

### Layout

| Layer | What it contains | Depends on Minecraft? |
|---|---|---|
| `core/` | The animation engine: Kumo, math, the animator format. Plain Java 8. | **No** |
| `common/` | Connects the core to vanilla models, entity state and rendering, mostly via Mixins. | Yes, vanilla only (no loader APIs) |
| `fabric/`, `neoforge/`, `forge/` | Entrypoints, events, networking, config for each loader. Kept as small as possible. | Yes |

- `common/` must never import a loader API. Anything loader-specific goes behind an interface in
  `common/` and is implemented in each loader module (looked up with `java.util.ServiceLoader`).

### Targets

| Minecraft | Loaders |
|---|---|
| 1.20.1 | Forge, Fabric |
| 1.21.1 | NeoForge, Fabric |
| Latest (26.x) | Fabric, NeoForge |

### Stonecutter

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

### Branches

```
main                     all modern Minecraft versions × all loaders
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
forge-1.12               1.12.2
```

- `forge-1.12` does **not** merge from `main`. It shares the engine by depending on a **published,
  pinned release of `core`** (e.g. `goblinbob.mobends:mobends-core:2.1.0`) and shading it into its
  jar. To bring an engine fix to 1.12.2: fix it in `core/` on `main`, release a new `core` version,
  and bump the dependency on `forge-1.12`. No cherry-picking or copying code between branches.
- A modern release ships **all targets at once**. Jars are named
  `mobends-<version>+<minecraft>-<loader>.jar`, e.g. `mobends-2.1.0+1.21.1-neoforge.jar`, tagged `v2.1.0`.
- CI builds every version × loader combination, and uploads the jars of a tag to Modrinth and
  CurseForge.
- Dropping a Minecraft version: remove its Stonecutter target after a release. If it later needs a
  critical fix, create a branch from its last release tag (e.g. `release/2.3-1.20.1`), fix,
  release, and leave the branch as is.

### Migration plan

1. [x] **Extract the core.** Move `kumo` and `math` into a `core/` Gradle module with `git mv`
   (keeps `git log --follow` history).
2. [ ] **Create `main`.** Branch from `forge-1.12`, remove the 1.12-specific code, add the
   Stonecutter multi-loader layout, and port the Minecraft-facing code, starting with 1.21.1
   (NeoForge + Fabric).
3. [ ] **Publish `core`.** Release the first `core` version and switch `forge-1.12` to depend on it,
   deleting its in-tree copy.
4. [ ] **Switch the default branch** on GitHub to `main`, and archive the 1.16 branches
   (`2.X/forge-1.16`, `master-1.16.3`) as tags (`archive/2.X-forge-1.16`, `archive/master-1.16.3`).
5. [ ] **Add targets:** 1.20.1 (Forge + Fabric), then the latest Minecraft version.

### Other tasks

- [ ] Send a checksum to the ping server to better differentiate between the official releases and forks.
- [ ] Make `field` resolve every vanilla field in production. The generated accessors
  (`VanillaEntityFields`) cover numeric fields only, so a vanilla boolean field (`isCharging`) or a
  step through a vanilla object field (`ridingEntity`) is found by reflection, under its
  development name: it resolves in development only, and in the game takes its `@fallback`.
  Generating accessors for boolean and object fields too (`GenVanillaFields.kt`) might be the
  answer. Nothing shipped reads one yet.
- [ ] Add a `machineTicksElapsed` built-in if machines that keep their own clock become common.
  Today a machine that needs one keeps it as `machine.` state advanced in its `update` list; the
  built-in would be named for its scope, as `nodeTicksElapsed` and `layerTicksElapsed` are, not
  an operation taking the scope as an argument.
- [ ] Consider an `@override` modifier on definitions, so an animator that `extends` another can
  redeclare one of the parent's `animator.` names (today that is an error). A definition's other
  `@` keys are where such per-definition rules go, as might what a type publishes to its
  extensions.
