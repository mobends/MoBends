# TODO

## Web animation editor

The *Customize* entry of the Mo' Bends menu (`GuiBendsMenu`) is meant to open the web animation
editor, which is in the works. It opens the `officialAnimationEditorUrl` of
[static-api.json](https://github.com/mobends/mobends-resources/blob/master/static-api.json)
(`WebLink.EDITOR`), currently `https://mobends.com`; it falls back to `https://mobends.com/roadmap`.

- [ ] Point `officialAnimationEditorUrl` at the web editor once it is live (no release needed).
- [ ] Decide how animations made in the editor get into the game (a resource pack to download, or
      something the mod fetches).

## Supporters

The *Supporters* entry of the Mo' Bends menu (`GuiSupportersWindow`) shows every supporter
accessory on the player. Its *Support Mo' Bends* button opens the `supportUrl` of
[static-api.json](https://github.com/mobends/mobends-resources/blob/master/static-api.json)
(`WebLink.SUPPORT`). The `accountUrl` (`WebLink.ACCOUNT`) is opened by the linked account's name
on the Mo' Bends menu (`GuiBendsMenu`) and by *Open Dashboard* on the linking screen
(`GuiLinkAccount`). Both fall back to `https://mobends.com`.

- [ ] Show the tiers (one-time collection, subscription, editor tier) once they exist.

## Legacy Mo' Bends pack

CI builds and signs the pack on release tags of `main` and attaches it to the GitHub release; its
Modrinth and CurseForge upload (the `publish-legacy-pack` job of `.github/workflows/release.yml`) is
switched off for now.

- [ ] Once the pack has its Modrinth and CurseForge pages (around the alpha's release), set the
      `LEGACY_PACK_MODRINTH_PROJECT_ID` and `LEGACY_PACK_CURSEFORGE_PROJECT_ID` repository variables
      and switch the job back on: delete its `if: false`. Check the first upload on CurseForge,
      where a resource pack takes no loader.

## Animations

- [ ] IK for cows and sheep (feet planted on the ground), switched off while they run.
- [ ] Poses for the items 1.12 didn't have (spyglass, goat horn, brush, crossbow, trident). Left for
      a later alpha.
- [ ] Babies with a model of their own on 26.x. 26.x draws most babies (wolf pups, baby zombies and
      villagers, the dev pack's animals) with their own model and texture, shaped unlike the adult
      the definitions were made for; they're drawn as vanilla for now (`LivingEntityRendererMixin`).
      They need definitions of their own. Left for after 2.0.0-alpha.4.

## Multiple Minecraft versions and loaders

The plan is to build every modern Minecraft version and loader from one source tree, sharing the
engine (`core/`) with the 1.12.2 branch. They are 2.X like 1.12.2 (see CONTRIBUTING.md): the same
animation system and pack formats.

### Layout

| Part | What it contains | Depends on Minecraft? |
|---|---|---|
| `core/` | The animation engine: Kumo, math, the animator format. Plain Java 8. | **No** |
| `src/` | The mod for every version and loader, one Stonecutter source tree (below). | Yes |

Inside `src/`, everything connects the core to vanilla models, entity state and rendering, mostly via
Mixins, with no loader API. What needs a loader (entrypoints, events, networking, config) lives in
`goblinbob.mobends.fabric` / `goblinbob.mobends.neoforge` (later `.forge`), each file fenced whole by
loader, behind the `goblinbob.mobends.platform.Platform` interface. Kept as small as possible. CI checks
that no other package uses a loader API (see CONTRIBUTING.md, *Loader code*).

(This was first planned as separate `common/`, `fabric/`, `neoforge/` Gradle modules. Stonecutter is
built around one source tree, which also handles the loaders with the same `//? if` fences as the
versions, so the split was dropped.)

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
- **Only commit while the default version is active** (`vcsVersion`, now `1.21.1-fabric`). Switching
  the active version in your IDE rewrites the fenced comments in your working copy. Switch back to the
  default before committing, otherwise the diff fills up with comment noise. CI rejects commits made
  with another version active.
- Stonecutter 0.10 needs Gradle to run on JDK 25; `gradle/gradle-daemon-jvm.properties` has Gradle
  download it.

### Branches

```
main                     all modern Minecraft versions × all loaders
 ├─ feature/<name>       short-lived, merged via pull request
 ├─ fix/<name>
forge-1.12               1.12.2
```

- `forge-1.12` does **not** merge from `main`. It shares the engine by depending on a **published,
  pinned release of `core`** (e.g. `goblinbob.mobends:mobends-core:2.0.0`) and shading it into its
  jar. To bring an engine fix to 1.12.2: fix it in `core/` on `main`, release a new `core` version,
  and bump the dependency on `forge-1.12`. Nothing is cherry-picked or merged between branches;
  what else `forge-1.12` has to follow (a shared asset, a new definition key the mod reads) is done
  there by hand, from the steps in its TODO.md, *Keeping up with `main`* (see CONTRIBUTING.md,
  *Branches*). An engine change goes through `core`, never by copying `core/` code.
- A modern release ships **all targets at once**. Jars are named
  `mobends-<version>+<minecraft>-<loader>.jar`, e.g. `mobends-2.0.0+1.21.1-neoforge.jar`, tagged `v2.0.0`.
- CI builds every version × loader combination, and uploads the jars of a tag to Modrinth and
  CurseForge.
- Dropping a Minecraft version: remove its Stonecutter target after a release. If it later needs a
  critical fix, create a branch from its last release tag (e.g. `release/2.3-1.20.1`), fix,
  release, and leave the branch as is.

### Migration plan

- [ ] **Add targets:** 1.20.1 (Forge + Fabric), then the latest Minecraft version.
  - [ ] A resource pack's definition of a list-drawn mob (the fox) on 1.20.1.

### Other tasks

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
