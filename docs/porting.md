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

I've checked the player, its layers, the zombie, skeleton, zombie villager and the wolf (adult and
pup, side by side with vanilla) in the game on Fabric. The creeper, golem, villager, witch, spider and
squid are still to be checked. On NeoForge I've only checked the player (#381).

## What's next

In this order:

1. **The rest of the player** (TODO.md): the inventory player's head following the mouse (1.12's `lookFurther`), stuck arrows and bee stingers
   on the bent limbs, armour from other mods' models, and poses for the items 1.12 didn't have
   (spyglass, goat horn, brush, crossbow, trident: they now just keep the current pose).
2. **The pig zombie** (stays vanilla: it's drawn with `PiglinModel` now, which its definition can't
   map onto without changing the asset; `@variants` might now make that possible).
3. **The config, the Mo' Bends menu, server rules, pack trust, supporters and the mobends.com API.**
4. The `.lang` translations to `.json`, a release workflow, then the 1.20.1 and latest targets.

## How the port is done

- **The 1.12.2 code is the reference**, in `legacy-1.12/` (not built). Classes keep their 1.12
  package and name in `src/` when they keep their role, so the two are easy to compare. Don't edit
  or move `legacy-1.12/`: the animation lab compiles from it, so each slice **copies** what it ports.
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
- **Fabric bundles one Fabric API module** (resource loader, jar-in-jar), because Fabric Loader alone
  doesn't load mods' assets. Use nothing else from Fabric API.

## Checking work

- `export JAVA_HOME=$(/usr/libexec/java_home -v 21)`, then `.github/scripts/check-sources.sh` and
  `./gradlew build` (both nodes, core's tests). If core or the animators change:
  `cd animation-lab && ./gradlew test`.
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
