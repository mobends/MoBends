# Changelog

## 2.0.0 (in development)

### Added

- Cows, mooshrooms, polar bears, pigs, creepers, chickens, villagers, witches and iron golems are
  animated, with bending knees (and elbows for the iron golem).
- Mods and resource packs can add animations on top of a mob's own with extensions in
  `assets/<namespace>/bends/extensions/`, for example making players wave.
- Mods and resource packs can give a mob, or a single player, a different model and animation with
  entity types in `assets/<namespace>/bends/types/`.
- Mods and resource packs can animate new mobs without code, with model definitions in
  `assets/<namespace>/bends/models/`. A model definition can compute the mob's own values from
  its fields, for its animation and every extension to read.
- Every mob Mo' Bends animates, the player included, is a model definition (`bends/models/`),
  so mods and resource packs can change their geometry, layers and values without code.
- Mods can add their own animation logic to the JSON format: operations (values computed in Java,
  such as whether a mob is wet) and drivers (posing computed in Java), registered from an addon.
- Zombie villagers are animated.
- A server's own resource pack can add types and extensions too.
- Settings has *Order* and *Extensions* buttons to choose which types and extensions win when
  several apply to the same mob.
- An animation can hand a mob back to its vanilla animation while a condition holds, for example
  while swimming.
- Entities that share a renderer, such as players, can each look different, one animated and the
  next vanilla.
- When an animation breaks, the chat says which one and why (once per animation), instead of the
  mob silently standing still.

### Changed

- Every mob is animated from JSON animator files, which resource packs can replace.
- Servers decide what resource packs may do to animation: allow it, limit how far it moves models
  (the default), or deny it; singleplayer always allows it.
- Resource reloads (F3+T, or changing resource packs) pick up changed animations and models.
- The mod's download is about 1 MB smaller (2.3 MB down to 1.3 MB).
- Supporter accessories download in the background instead of on every resource reload.
- The *Customize* entry of the Mo' Bends menu leads to the Mo' Bends website; it will open the new
  web animation editor once it is out.

### Fixed

- The Mo' Bends menu's settings (which mobs are animated, the orders) and the mod options in
  Forge's mod options screen no longer overwrite each other in `config/mobends.cfg`.
- The wolf's walk no longer lags one frame behind its movement.
- Mo' Bends no longer keeps every entity it has seen in memory until you switch worlds.
- Mobs that aren't animated no longer cost time on every frame.
- Only the local player joining a world resets the server's settings, not every player that comes
  into view.
- Mo' Bends' messages appear in the game log, not only in the console.
- *Perform Spin Attack* works: turned off, or while riding, the sword combo starts over instead of
  ending with the whirl.
- The Italian, Norwegian, Polish and Portuguese translations load, and every translation covers
  the whole mod.
- Holding a sword no longer leaves face culling off for whatever renders next.
- The right arm of zombies, skeletons and pig zombies is no longer slightly too deep.
- The player's left shin, the skeleton's left arm and the zombie villager's body show their own
  skin, as in vanilla (they showed another part's, or a zombie's body over a villager's skin).

### Removed

- Bends packs, and the *Packs* section of the Mo' Bends menu; extensions replace them.
- The bends pack editor, which *Customize* used to open.

**Code:** the animation system was rearchitected: the hand-written animation controllers and bits
are gone, and every mob runs on the new KUMO engine (layers, pose nodes, expressions; see
`misc/kumo-format.md`), which lives in a Minecraft-free `core` module published separately.
Renderers are now swapped per render instead of mutated and demutated. Addons that register
mutators, previewers, value sources or bends pack hooks need updating: entity data names its
animator (`getDefaultAnimator`) instead of returning a controller, `registerNewEntity` no longer
takes alterable parts, and animation editors can no longer be registered. Every asset file
(animators, types, extensions, model definitions) carries a `formatVersion`; one written for
another version of its format is refused with a message saying so. Bender keys are resource
locations (`mobends:zombie` instead of `mobends-minecraft:zombie`); which mobs you turned off
carries over.
The mobs made from model definitions are given to their entities by type files
(`bends/types/`), not a list of definitions. While a server limits resource packs, a pack's type
that brings its own model definition is ignored as a whole.

## 1.2.2 (2025-09-29)

### Added

- French translation.

### Fixed

- Mo' Bends no longer crashes or fills the log alongside GeckoLib-based mods, because its animation
  files now live in their own `bends/` folder.

**Code:** the mod is now built in CI, and the build can compile Kotlin.

## 1.2.1 (2022-02-19)

### Changed

- Skeletons use the same weapon and item animations as players.
- Any animated mob can take the attack stance, not just players.

### Fixed

- Left-handed attack animations play on the correct arm.

## 1.2.0 (2021-12-12)

### Added

- More weapon styles, with the sword combo, punches and tool swings each animated on their own.
- The config can assign any item to a weapon or use style (`itemAttackClassifications`,
  `itemUseClassifications`).

### Fixed

- Held items rotate back to rest after an attack.
- The sword combo resets properly.
- The mod works on dedicated servers again.

**Code:** attack and item-use animations were restructured into an item action system, with one
action per weapon style.

## 1.1.0 (2021-09-28)

### Added

- Supporter accessories, downloaded from the Mo' Bends website, are rendered on players.
- Vines can be climbed with the ladder animation.

### Fixed

- The cape stays attached to the player's back.
- Arrow trails no longer leak memory.

**Code:** a module system was added for the website connection and downloaded assets, whose
versions are tracked separately from the mod's.

## 1.0.0 (2021-08-13)

### Added

- Skeletons are animated.
- The spin attack is back, and can be turned off in the config.
- `toolItems` and `keepEntityAsVanilla` config options choose which items count as tools and which
  entities stay vanilla.
- Bends pack animators can check an item's name (the `core:equipment_name` condition).

### Changed

- Armor looks the same on vanilla and animated models.
- Sneaking, harvesting and the wolf's mouth look better.

### Fixed

- Baby wolves look correct.
- The second skin layer is scaled and placed correctly.
- The elytra and sleeping animations work again.
- Heads no longer spin when turning past a full circle.
- Walking up and down stairs is detected more reliably in multiplayer.
- Changing the mod config takes effect straight away.

**Code:** armor mutation was reworked, and the build moved to a newer Forge and Gradle.

## 1.0.0 beta (2020-06-19)

The first release on CurseForge.

### Added

- Wolves are animated, including sitting, standing up and breathing.
- Players have animated capes.
- New animations for shield blocking, the elytra, harvesting and more sword attacks.
- The config can mark items as weapons.
- Servers can decide whether bends packs are allowed and limit how far they move models.
- The mod tells you when a new version is available.
- The settings list can be filtered by name.
- Invalid bends packs are reported to the player.
- Brazilian Portuguese translation.

### Changed

- The bow animation was reworked.

### Fixed

- The torch-holding animation looks right again.
- Settings are saved properly.

**Code:** the KUMO animation system (state machines of keyframe animations) was introduced and first
used for the wolf, and the root package was renamed to `goblinbob.mobends`.

## 1.0.0 pre-release (2020-03-12)

The first version for Minecraft 1.12.2, released to the Discord community.

### Added

- Players animate walking, sprinting, sneaking, jumping, landing, swimming, climbing, riding,
  sitting, falling and flying.
- Players animate attacks, punches, sword combos, bows, eating and breaking blocks.
- Zombies, zombie pigmen, spiders and squids are animated.
- Spiders plant their legs on the ground as they walk, and can crawl up walls.
- Armor and the second skin layer bend with the body.
- First-person hands are animated.
- Swords and arrows leave trails.
- The Mo' Bends menu (G) can turn animation on or off for each mob.
- Bends packs change animations, and several can be applied at once.
- Animations pause when the game is paused in singleplayer.
- The mod can be installed on servers.
- Russian translation.

**Code:** the mod was rewritten for 1.12.2 around swapping vanilla models for bendable ones,
quaternion rotations and keyframe animations, and split into a `core` framework and a `standard`
addon that other mods can build on.

---

Versions before 1.0.0 (0.x, for earlier Minecraft versions) predate this repository's history.
