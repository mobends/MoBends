# Changelog

## Unreleased

### Added

- A type file can make its model the default model of the entities of the class its definition
  names, and of its subclasses (`"defaultModel": true`), as a mob registered in code was.

### Changed

- Every mob Mo' Bends animates, the player included, is a type file now (`bends/types/`), not
  registered in code. Their ids are the same, so ranks, switches and options set before still apply.
- A type file can name a model by key whatever order the packs were read in.

### Removed

- `AddonAnimationRegistry.registerDefinedEntity`, `registerNewEntity` and `registerEntity`: an addon's
  mobs are type files and model definitions in its resources, as on the modern versions. Mobs
  animated by Java benders (1.X's way) and their built-in types are gone.

## 2.0.0-alpha.3

Built on the same engine as 2.0.0-alpha.3 for Minecraft 1.21.1 (`mobends-core` 2.0.0-alpha.3).

### Added

- Animators and model definitions can tell the Minecraft versions apart with
  `core:minecraft_version` (`{"core:minecraft_version": [">", "1.12.2"]}`), and model definitions
  can change with them through `@variants`: a list of an `@when` and the fields it merges into the
  definition when that holds. One pack can then fit 1.12.2 and the later versions alike.
- A box's face in a model definition can be mirrored with `"flip": true`.

### Changed

- The wolf matches vanilla: its head, legs and tail sit and turn where vanilla's do, a pup's head
  is where vanilla puts it, and its mouth follows the head.
- The first-person hand is always vanilla's arm and sleeve, posed as vanilla poses them, never
  bent.
- Breaking: the `renderer.firstPersonRest` key of model definitions is gone, with the bent
  first-person hand it posed.

## 2.0.0-alpha.2

### Added

- Skeletons stand on their feet, as players and zombies do: standing still, the feet stay planted
  and they turn by stepping, quickly and lightly, and stop with their feet where the walk left
  them. Walking, strafing, attacking and aiming a bow pose the legs as before.
- Settings has an _Options_ button for mobs with options: settings their animation offers, such as
  the player's spin attack. Mods and resource packs can give their models, types and extensions
  options of their own (`options` in the file), which their animators read as `option.<name>`.
- Animators can scale a model's parts with the `core:scale` driver (`x`, `y`, `z`, or `all` for
  every axis): it multiplies the part's scale by default, or replaces it with `"@space":
  "override"`, and transitions crossfade it. What the part carries (child parts, a held item,
  armor) is scaled with it.
- Servers that limit resource packs (`LIMITED`) keep a resource pack's scaling within
  `maxPartScale` (default 1.5) of the trusted animation's, up or down, so a pack can't hide parts
  or blow them up.

### Changed

- Animation layers that switch on and off (sneaking, holding a torch, a skeleton strafing, a zombie
  pigman's slash and stance) fade in and out instead of jumping, and start their animation over
  when they come back. Animators set how long with a layer's `@fade` (ticks).
- A transition that interrupts another no longer freezes the animation fading out: it keeps
  moving (looking around, walking) until it has faded. A node that starts over (a transition to
  itself with a duration) fades from where it was instead of jumping.
- An animation value that isn't a number (a division by zero in a resource pack's animator, say)
  puts the part at rest instead of freezing it where it was.
- Poses are declarative: a part's rotation, position or scale that no animation writes in a frame
  goes back to rest, instead of staying where an earlier node left it, and transitions fade such
  parts to rest (or to the layers below). A scaled part no longer stays scaled after its node, a
  player's riding dip no longer sticks once they slow down, and the spider no longer keeps its
  body's sway while crawling or dying. The spider looks around while it jumps.
- The Mo' Bends menu is more compact: a smaller title, with Link account under it and the
  sections (Settings, Customize, Supporters) beside it as slim buttons. Hovering one sends a glint
  through it, which leaves the section's color behind and brings in its icon (Settings has a gear
  now). More Settings moved into Settings.
- The heads of players, zombies and skeletons no longer turn with the body: they look where the
  creature looks however its body bends or sways, walking, sprinting, sneaking, riding or aiming a
  bow. The poses that turn the head on purpose still do (the sword's stance and slashes, the
  punches and the guard, eating, falling, gliding), the stumbling zombie's head still hangs with its
  body, and a player climbing over a ledge still bows their head with it. The Legacy Mo' Bends
  pack keeps the old heads, which turn with the body.
- Model definitions can say a bone doesn't turn with its parent (`"inheritRotation": false`): it
  stays where its parent puts it, but its rotation is relative to the whole model.
- The Mo' Bends menu has a Refresh button in its bottom-left corner, in place of the F10 key: it
  reloads the animations and fetches the account, assets and supporter accessories again.
- The Config button for Mo' Bends in Forge's Mods list opens the Mo' Bends menu, so it can be
  reached from the title screen. Forge's screen for the trails and item options is behind the
  menu's new More Settings button.
- _Perform Spin Attack_ moved from Forge's mod options to the player's options in the Mo' Bends
  menu. If you had turned it off, it stays off.
- Only players end the sword combo with a spin attack; skeletons repeat the four slashes.
- Breaking: the `mobends:spin_attack_enabled` operation is gone. A player animator reads
  `option.spinAttack` instead.
- Breaking: the whole model hangs from two bones of the entity's own, `root` and `center`, in
  place of `globalOffset`, `renderRotation`, `localOffset` and `centerRotation`. `root` moves the
  model, then turns it about the feet; `center`, its child, moves it, then turns it about its
  pivot (half the entity's height, or a model definition's `center.pivot`). Animators, extensions
  and model definitions name `root` for `globalOffset` and `renderRotation`, and `center` for
  `localOffset` and `centerRotation`. The center's position is no longer turned by its own rotation,
  and the root's rotation now turns the center's, so a body leaned and turned at once looks a
  little different. A clip's `root` or `center` track holds both a rotation and a position, and
  writes only what its keyframes give. A clip item can apply one of them alone
  (`"bones": ["root.rotation"]`), and a smoothing can give a bone's rotation and position apart
  (`{"root": {"rotation": 0.5, "position": [3, 1.7, 3]}}`), in an item's `@smoothing` and in a
  model definition's `smoothing`, where a part's position can now have a base smoothing too.
  `core:two_bone_ik` and `core:three_bone_ik` take `centerBone` and `centerPivot` in place of
  `height`.
- The iron golem's renderer turns back the rock vanilla gives it while it walks (a model
  definition's `"renderer": {"undoVanillaRock": true}`), in place of its animator turning it back.

### Fixed

- Forge tells you when a newer version of Mo' Bends is out, on the main menu and in the Mods list.
  The check had never been switched on.
- Swimming fast or crawling with Idō no longer crashes the game. While Idō draws the player
  swimming or crawling, it draws them its own way, without Mo' Bends.
- Other players flying in creative or spectator mode are seen flying, not jumping or falling, and
  their capes stream back as they fly. The game only tells you whether you fly yourself, so for
  them it is told from how they move. (#265)

## 2.0.0-alpha.1

### Added

- Creepers, villagers, witches and iron golems are animated, with bending knees (and elbows for
  the iron golem); a charged creeper's armour moves with it.
- Zombie villagers are animated.
- Players stand on their feet: standing still, the feet stay planted and they turn by stepping,
  and stop with their feet where the walk or the sprint left them. In the sword's stance they stand
  on their feet too, wide apart. Sneaking, the attacks themselves, aiming a bow and the rest pose
  the legs as before.
- Villagers stand on their feet, as iron golems do: standing, the feet stay planted on the ground
  and they turn by stepping, with quick, light steps. They walk with a swing of the legs, and when
  they stop, their feet stay where the walk left them, a foot left well out of place stepping
  back under them. Witches, which move as villagers do, too. A villager's nose wobbles with each
  step.
- Zombies and zombie villagers stand on their feet too: standing, the feet stay planted and they
  turn by stepping, heavily; they walk (or stumble) as before, and stop with their feet where the
  walk left them. A leaning zombie stands crouched with its feet on the ground.
- Iron golems jump: the push-off snaps their legs straight, they bend as the golem flies and the
  heavy arms lag behind; landing, it gives under its weight. Jumping onto a block, the golem climbs
  it: it plants its hands on the top, hauls itself up the side, gets a knee up and steps on.
- The Legacy Mo' Bends resource pack brings back how players and zombies (and zombie villagers)
  moved in Mo' Bends 1.X, without standing on their feet. It is a separate download, signed by
  Mo' Bends, so it works on servers that refuse resource packs' animation too.
- Armour made with GeckoLib (3, for 1.12.2) moves with the animation, as vanilla armour does,
  bending at the elbows and knees; its own GeckoLib animation still plays. GeckoLib stays
  optional, and `keepArmorAsVanilla` keeps a piece as GeckoLib draws it.
- A _Supporters_ entry in the Mo' Bends menu shows every supporter accessory on your own
  character, and which ones you have.
- _Link account_ in the Mo' Bends menu links your Minecraft account to your Mo' Bends account
  with a code from the dashboard, so your accessories follow you even after you rename it.
- Settings has _Order_ and _Extensions_ buttons to choose which types and extensions win when
  several apply to the same mob.
- When an animation breaks, the chat says which one and why (once per animation), instead of the
  mob silently standing still.
- Mods and resource packs can add animations on top of a mob's own with extensions in
  `assets/<namespace>/bends/extensions/`, for example making players wave.
- Mods and resource packs can give a mob, or a single player, a different model and animation with
  entity types in `assets/<namespace>/bends/types/`.
- Mods and resource packs can animate new mobs without code, with model definitions in
  `assets/<namespace>/bends/models/`. A model definition can compute the mob's own values from
  its fields, for its animation and every extension to read.
- Every mob Mo' Bends animates, the player included, is a model definition (`bends/models/`),
  so mods and resource packs can change their geometry, layers and values without code.
- A model definition can extend another and change only what differs, as the zombie villager and
  the pig zombie extend the zombie.
- A server's own resource pack can add types and extensions too.
- Resource packs signed by Mo' Bends are trusted as the mod is, under every server policy. The mod
  checks the signature itself, offline, with the keys it ships.
- An animation can hand a mob back to its vanilla animation while a condition holds, for example
  while swimming.
- Entities that share a renderer, such as players, can each look different, one animated and the
  next vanilla.
- Mods can add their own animation logic to the JSON format: operations (values computed in Java,
  such as whether a mob is wet) and drivers (posing computed in Java), registered from an addon.
- `core:step_turn` can take over from a walk posed beneath it (`placeFromPose`): the feet are
  planted where the walk left them, and step back under the body if they are out of place.
  `crouchDrop` sets how quickly the hips go down for reach. The feet are taken where the legs are
  drawn (drivers can read a bone's smoothed rotation, `rotationDrawn`), so a smoothed walk is
  taken over without a jump. `spread` and `stagger` move where the feet stand, `followRoot` keeps
  them on the ground under a lowered whole model, and a turn of the whole model beneath the driver
  is followed (eased in over `beneathTurnTicks`), the feet staying planted.
- Animations can draw a mob at a point in the world while its real position moves on (the
  `core:anchor` driver), to stage a sequence such as climbing onto a block.
- Animations can compute positions in the world in double precision (`entityWorldX`, `toDouble`,
  `toFloat`), so mobs far from the world's origin animate as smoothly as those near it.
- Animations can reach for the world: `core:raycast` finds where a ray through the blocks hits and
  which way the surface faces (`core:raycast_array` casts a spread of them and keeps the closest hit), and `core:two_bone_ik` / `core:three_bone_ik` put the end of an arm
  or a leg on a point in the world, each joint pointing a way in the world. Climbing onto a block,
  the iron golem grabs the top where its hands find it and holds on while it hauls itself up. With `"debug": true`, the
  rays are drawn over the world, colored by what they hit, to see what an animation finds.
- Animations can read the ground: `core:block_below` finds the block under the feet,
  `core:block_gradient` which way the ground rises round it, and `core:step_up` the step one block
  up ahead, with room on it. The iron golem looks ahead for a step as it walks, and climbs when it
  jumps close to one it is walking towards (a jump otherwise); it gives up early, mid-air, if it
  backs away from the ledge.

### Changed

- Servers decide what resource packs may do to animation: allow it, limit how far it moves models
  (the default), or deny it; singleplayer always allows it.
- Resource reloads (F3+T, or changing resource packs) pick up changed animations and models.
- The mod's download is about 1 MB smaller (2.3 MB down to 1.3 MB).
- The _Customize_ entry of the Mo' Bends menu leads to the Mo' Bends website; it will open the new
  web animation editor once it is out.
- Supporter accessories download in the background instead of on every resource reload.
- Every mob is animated from JSON animator files, which resource packs can replace.

### Fixed

- _Perform Spin Attack_ works: turned off, or while riding, the sword combo starts over instead of
  ending with the whirl.
- The player's left shin shows the left leg's skin; it showed the right leg's, so a skin with
  different legs had its left shin swapped.
- The skeleton's left arm and the zombie villager's body show their own skin, as in vanilla (the
  arm showed its skin back to front, and the villager a zombie's body over a villager's skin).
- The right arm of zombies, skeletons and pig zombies is no longer slightly too deep.
- Arrows stuck in an animated mob stay on its parts as they move, instead of floating at the
  parts' joints.
- The squid's tentacles sit around its body where vanilla puts them.
- The wolf's walk no longer lags one frame behind its movement.
- Holding a sword no longer leaves face culling off for whatever renders next.
- The Italian, Norwegian, Polish and Portuguese translations load, and every translation covers
  the whole mod.
- Mo' Bends no longer keeps every entity it has seen in memory until you switch worlds.
- Mobs that aren't animated no longer cost time on every frame.
- The Mo' Bends menu's settings (which mobs are animated, the orders) and the mod options in
  Forge's mod options screen no longer overwrite each other in `config/mobends.cfg`.
- Only the local player joining a world resets the server's settings, not every player that comes
  into view.
- Mo' Bends' messages appear in the game log, not only in the console.

### Removed

- Bends packs, and the _Packs_ section of the Mo' Bends menu; extensions replace them.
- The bends pack editor, which _Customize_ used to open.

**Code:** the animation system was rearchitected: the hand-written animation controllers and bits
are gone, and every mob runs on the new KUMO engine (layers, pose nodes, expressions; see
`misc/kumo-format.md`), which lives in a Minecraft-free `core` module published separately.
Renderers are now swapped per render instead of mutated and demutated. Addons that register
mutators, previewers, value sources or bends pack hooks need updating: entity data names its
animator (`getDefaultAnimator`) instead of returning a controller, `registerNewEntity` no longer
takes alterable parts, and animation editors can no longer be registered. The Java models of the
player and the other mobs (`PlayerBender`, the mutators, the mutated renderers and the data
classes such as `PlayerData` and `BipedEntityData`) are gone: every mob's data is a
`DefinedEntityData`, whose bones and components are found by name. Every asset file
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
