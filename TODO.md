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

- [x] Add `supportUrl` (the Patreon page) and `accountUrl` (the mobends.com dashboard) to static-api.json.
- [ ] Show the tiers (one-time collection, subscription, editor tier) once they exist.
- [x] Have the API accept the join's `version` of the 2.0.0 pre-releases
      (`2.0.0-alpha.1/1.21.1-fabric`): it refused them with a 400, and now answers the join.

## Legacy Mo' Bends pack

- [x] Add the `MOBENDS_PACK_SIGNING_KEY` secret, and have CI build the pack on release tags and
      attach it to the GitHub release (`.github/workflows/release.yml`).
- [x] Have CI upload the pack to Modrinth and CurseForge (the `publish-legacy-pack` job of
      `.github/workflows/release.yml`), switched off for now.
- [ ] Once the pack has its Modrinth and CurseForge pages (around the alpha's release), set the
      `LEGACY_PACK_MODRINTH_PROJECT_ID` and `LEGACY_PACK_CURSEFORGE_PROJECT_ID` repository variables
      and switch the job back on: delete its `if: false`. Check the first upload on CurseForge,
      where a resource pack takes no loader.

## Animations

- [ ] IK for cows and sheep (feet planted on the ground), switched off while they run.

## Mo' Bends menu

- [x] Improve the look of the *Settings* submenu (`GuiSettingsWindow`, `GuiBenderList`): each mob
      shown with an orthographic render of it as a thumbnail (clipped to its frame), its name, etc.
- [x] Modern versions: wire the hooks `GuiBendsMenu` left for the supporters/API port: Link
      account opens `GuiLinkAccount`, or once linked the account's name (`WebLink.ACCOUNT`) and Log
      out; Supporters opens `GuiSupportersWindow`; Customize opens `WebLink.EDITOR`; the account is
      checked as the menu opens, and read again by Refresh, which also refreshes the API.
- [ ] Modern versions: the previews stand in the client's level, where the player stands, but are never
      added to it (`PreviewWorld`); 1.12 made a world of its own (a flat floor). With no world loaded
      (the menu opened from the mod list on the title screen) they show the mob's spawn egg, and the
      Supporters window's player, who has none, a question mark. A level of its own needs a
      connection on 1.21.1, and tells other mods a level loaded.
- [ ] Modern versions: with no world loaded, a type that picks its entities by entity type (other
      than the player) isn't listed under its mob: what class an entity type's entities are is found
      by making one, which needs a level (`EntityClasses`).
- [x] Modern versions: More Settings has the arrow trails' switch, and the arrow trails are ported
      (checked in the game on Fabric).

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

1. [x] **Extract the core.** Move `kumo` and `math` into a `core/` Gradle module with `git mv`
   (keeps `git log --follow` history).
2. [ ] **Create `main`.** Port the Minecraft-facing code, starting with 1.21.1 (NeoForge + Fabric).
   - [x] Fast-forward `main` to `forge-1.12`.
   - [x] Add the Stonecutter multi-loader layout: a 1.21.1 Fabric + NeoForge skeleton that loads,
     with `core/` compiled in, and CI. The 1.12 sources moved to `legacy-1.12/` (not built) as the
     porting reference; the shared assets moved to `src/main/resources`.
   - [ ] Port `legacy-1.12/` slice by slice, deleting it once empty. Nothing compiles from it (the
     animation lab compiles the animation code from `src/`), so a slice can `git mv` what it ports.
     - [x] **The player bends** (1.21.1, Fabric + NeoForge): the player's model definition, entity
       data and KUMO glue, and its bent model (split elbows and knees) drawn in place of vanilla's
       (`DefinedModelBuilder`, `BendsRenderer`). Checked in the game on both loaders.
     - [x] The player's render layers: held items, vanilla armour, custom head, cape and elytra
       follow the bent player.
     - [x] The sword trail and the cape wave (#386), and the arrow trails.
     - [x] The first-person hand stays vanilla's: it's never bent, and `renderer.firstPersonRest`
       is gone from the definition format.
     - [x] The inventory player's head follows the mouse (1.12's `lookFurther`).
     - [x] Stuck arrows and bee stingers ride the bent parts.
     - [x] Armour from other mods' models bends with the body, cut at the elbows and knees.
     - [x] GeckoLib armour bends with the body, cut at the elbows and knees, as 1.12's
       `GeckoLibCompat` had it (GeckoLib 4.6 and up, optional). Still to be checked in the game
       with a real GeckoLib armour.
     - [ ] Poses for the items 1.12 didn't have (spyglass, goat horn, brush, crossbow, trident).
       Left for a later alpha.
     - [x] The other mobs: zombie, skeleton, zombie villager, creeper, iron golem, villager, witch,
       spider, squid, wolf (and the dev pack's animals), with type files, extensions and `field`
       expressions.
     - [x] The pig zombie (the zombified piglin): its definition names the piglin's model through a
       `@variants` entry for later versions.
     - [x] The client config (`config/mobends.json`, see docs/configuration.md).
     - [x] Server rules (`config/mobends-server.json`, sent to players) and pack trust (signed packs,
       `AnimationPolicy`).
     - [x] Supporters and the mobends.com API: the join and pings, the accessory catalogue and each
       player's settings, the downloaded assets, account linking (without its screen), the
       static API's links, the accessories drawn on the bent player, and the supporters' sword
       trail colour (docs/api-communication.md).
     - [x] The Mo' Bends menu: the main menu, the Settings window (each mob with its preview, the
       order of its types and extensions, its options) and More Settings, the key that opens it (G)
       and the mod list's Config button (NeoForge, and Mod Menu on Fabric). See *Mo' Bends menu*.
     - [x] The menu's Supporters window, its account buttons and screen (Link account), and
       Customize's link.
     - [x] The addon API (docs/addons.md): other mods register mobs, layers, components, drivers
       and KUMO operations, found through a `mobends` entrypoint (Fabric) or `@MoBendsAddon`
       (NeoForge). Mo' Bends' own content is its `DefaultAddon`. Still to be checked with a real
       addon mod, and its tick callbacks in a world on NeoForge.
   - [x] Convert the `.lang` translations to `.json`.
   - [x] A release workflow for the modern versions (all targets at once, Modrinth and CurseForge):
     `.github/workflows/release.yml`, for `v<version>` tags on `main`. Its Modrinth and CurseForge
     upload (the `publish` job) is switched off for now.
   - [ ] Switch the modern Modrinth and CurseForge upload on: check the `MODRINTH_PROJECT_ID` and
     `CURSEFORGE_PROJECT_ID` variables and the `MODRINTH_TOKEN` and `CURSEFORGE_TOKEN` secrets are
     set, and delete the `publish` job's `if: false`. Check the first upload on both sites (loader,
     Minecraft versions).
3. [x] **Publish `core`.** Release the first `core` version and switch `forge-1.12` to depend on it,
   deleting its in-tree copy. (`core-2.0.0-alpha.2` on GitHub Packages; `forge-1.12` uses it since #379.)
4. [x] **Switch the default branch** on GitHub to `main`, and archive the 1.16 branches
   (`2.X/forge-1.16`, `master-1.16.3`) as tags (`archive/2.X-forge-1.16`, `archive/master-1.16.3`).
5. [ ] **Add targets:** 1.20.1 (Forge + Fabric), then the latest Minecraft version.

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
