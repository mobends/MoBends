# Porting Mo' Bends to the modern versions

Where the port of Mo' Bends from 1.12.2 to the modern Minecraft versions stands, how I'm doing it,
and what comes next. The plan and its checklist are in [TODO.md](../TODO.md) (*Multiple Minecraft
versions and loaders*); the project's rules are in [CONTRIBUTING.md](../CONTRIBUTING.md). Read both
first.

## Where it stands

`main` builds Minecraft 1.21.1 for Fabric and NeoForge from one source tree (`src/`, Stonecutter).
Ported so far, in this order:

| PR | What |
|---|---|
| #381 | The player bends: its model definition, entity data, the KUMO glue, and the bent model (split elbows and knees) drawn instead of vanilla's. |
| #382 | The player's render layers follow the bent body: held items, vanilla armour, custom head, spyglass, cape, elytra. |
| #383 | The other mobs: zombie (husk, drowned), skeleton, zombie villager, creeper, iron golem, villager, witch, spider, squid, wolf, the dev pack's animals; type files, extensions, `field` expressions; the `core:minecraft_version` condition and `@variants` in model definitions. |
| #386 | The sword trail and the cape wave. |
| #388 | The first-person hand stays vanilla's; `renderer.firstPersonRest` is gone. |
| #398 | GeckoLib armour bends with the body (when GeckoLib is installed). |
| #405 | The addon API: other mods add mobs, layers, components, drivers and KUMO operations (docs/addons.md). |
| #407 | GeckoLib armour: a mod's own bones beside the armour bones (Fantasy Armor's leg cloth) bend too. |
| #410 | The Mo' Bends menu opens from the mod list only in a world. |
| #413 | The Legacy Mo' Bends pack is built, signed and released from `main`. |
| #416, #417, #418, #420 | The rest of 1.12: errors told in chat, `core:equipment_name` and `core:ledge_ahead`, NeoForge's update check, the credits, two tests. `legacy-1.12/` is gone. |
| #421, #422, #423, #424 | Model definitions bend any model, a mod's too, and any mob, named by registry id. |

I've checked the player, its layers and the other mobs (the zombie, skeleton, zombie villager,
creeper, golem, villager, witch, spider, squid, and the wolf, adult and pup, side by side with vanilla)
in the game on both loaders, as well as GeckoLib armour (with Fantasy Armor) and the addon API (with
a test addon in its own jar).

## What's next

1. The 1.20.1 and latest targets.

Left for a later alpha: poses for the items 1.12 didn't have (spyglass, goat horn, brush, crossbow,
trident: they now just keep the current pose).

## How the port is done

- **The 1.12.2 code is the reference**, on the `forge-1.12` branch. Everything from it is ported
  (or dropped as not needed), and `main`'s copy (`legacy-1.12/`) is gone. Classes kept their 1.12
  package and name in `src/` where they kept their role, so the two are easy to compare.
- **The animation lab compiles the animation code from `src/`** (and `core/`), against stand-ins for
  the 1.21.1 classes it touches (`animation-lab/src/mcstub`). A slice that has that code use another
  Minecraft class or member adds it to the stand-ins; one that changes how an entity animates on
  purpose re-records the goldens it changes and says why in `docs/testing.md`.
- **The asset format doesn't change.** Assets (`src/main/resources/assets/mobends/bends/`) are
  shared with `forge-1.12`. Definitions keep their 1.12 class and field names
  (`net.minecraft.client.model.ModelPlayer`, `bipedRightArm`); `core/vanilla/VanillaNames` translates
  them per model for the running version. New keys are fine if 1.12.2 can ignore them (its Gson
  skips unknown keys), like `@variants`.
- **`core/` stays Minecraft-free and Java 8**; it's published and shared with `forge-1.12`. Keep
  changes to its API additive where possible; see *Keeping forge-1.12 up to date* below.
- **Hooks are vanilla Mixins** (`goblinbob.mobends.mixin`, client), so they're written once for both
  loaders. Prefer `@WrapOperation`/`@Inject` that keep other mods' wrappers running. Anything Mo'
  Bends can't bend must draw exactly as vanilla, never twice.
- **Drawing:** `ModelPart` is final on 1.21.1, so instead of swapping parts as 1.12 did, a bent copy of
  the model is built per vanilla model (`DefinedModelBuilder`, `DefinedModel`) and drawn inside the
  model's own `renderToBuffer` (`AgeableListModelMixin`, `HierarchicalModelMixin`), which the renderer
  marks (`BendsRenderer.markBentDraw`). Layers get a bone's frame from `DefinedModel.applyVanillaFrame`
  after `DefinedRenderer.transformModel`; the bones come from the definition's `layers` block
  (`DefinedLayers`).
- **Other mods are optional.** GeckoLib is a compile-only dependency; its mixins
  (`mixin.geckolib`) are in their own config, `mobends.geckolib.mixins.json`, which lists them only
  when GeckoLib is installed and isn't required. Nothing outside that package touches GeckoLib's
  classes. `-Pgeckolib` puts it (and, on Fabric, the Fabric API it needs) in `runClient`.
- **Addons** (docs/addons.md) keep 1.12's `IAddon` and `AddonAnimationRegistry`. Mo' Bends'
  own content is registered through it (`DefaultAddon`); new content of the mod goes there, not
  into `CoreClient`.
- **Fabric bundles two Fabric API modules** (resource loader and networking, jar-in-jar), because
  Fabric Loader alone doesn't load mods' assets or send their payloads. Use nothing else from Fabric API.

## Checking work

- `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`, then `.github/scripts/check-sources.sh` and
  `./gradlew build` (both nodes, core's tests). If core, the animators or the animation code in
  `src/` change: `cd animation-lab && ./gradlew test`.
- **Geometry and transforms can be checked without the game**: so far each slice had a standalone
  program, run against the built classes and the real vanilla meshes, comparing the bent parts with
  vanilla's at rest (every vertex and UV) and the layers' frames with the drawn parts. Do the same for
  new geometry; it catches most mistakes before the game does.
- **Then check it in the game.** Start the client into the test world with
  `./gradlew :1.21.1-fabric:runClient --args="--quickPlaySingleplayer 'New World'"`. On NeoForge,
  `--args` breaks the dev launcher: run `:1.21.1-neoforge:runClient` plain and pick the world. Only one
  client can hold the world: close earlier test clients first (a `session.lock` error in the log means
  one is still open). To compare a mob with vanilla, a resource pack with an extension whose only layer
  is a `core:vanilla` node, on for every other second (`"@when"` on `entityTicksExisted`), switches it
  back and forth in place.
- CI runs `build` (and `parity` when the lab's inputs change). Codacy fails on every PR; nobody has
  looked into why.

## Pull requests

- **Descriptions: stupidly simple and clear.** A sentence of what it does, a short *What changed*
  list, *Not yet*, *Tested*. No implementation detail.
- Nothing goes straight to `main`: every slice is a PR. Tags and releases are mine to push.
- Say plainly what was and wasn't checked in the game, and on which loader.

## Keeping forge-1.12 up to date

What `forge-1.12` has to do to follow `main` is in its TODO.md, *Keeping up with `main`*; #383's
steps (`core` 2.0.0-alpha.3, the wolf) went there in #384. Every PR here that changes something it has to
follow comes with a PR there adding the steps (CONTRIBUTING.md, *Branches*).
