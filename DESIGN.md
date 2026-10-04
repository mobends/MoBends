# KUMO format rework

Working notes for the KUMO animator format and the files around it (type files, model
definitions), before v2 ships. Two parts, and a task list:

1. **The new design**: how the system works, on its own terms.
2. **Migrating from the old design**: why it changes, how each old construct maps onto the new
   one, measurements, and the work to get there.
3. **TODO**: the work, in the order it builds up.

Parts of the format this rework doesn't touch (layers, machines, selectors, connections, pose
composition, masks, mirroring, `extends`, extensions, trust and resource-pack limits) are as
specified in `misc/kumo-format.md`, with conditions written as expressions, and nodes and
connections written as in *Structured objects*. When a decision here
is implemented, the spec is updated and the entry can leave this document.

---

# Part 1: The new design

## Files

- **Type files** say which model and animator an entity gets while a selector holds (spec,
  *Selectors*).
- **Model definitions** describe a mob's bones over its vanilla model, and the values the mob
  exposes to animators: they are the only files that declare `entity.` definitions.
- **Animators** are layers of state machines whose nodes pose the bones.
- **Extensions** add layers on top of a type's animator.

**Every mob is described by files.** Mo' Bends' own mobs, the player included, are type files
plus model definitions like any mod's or resource pack's: `mobends:player` is a type file Mo'
Bends ships. A mod or pack needs Java only to bring new operations or drivers.

## How constructs are written

Pose items and nodes follow the one-key rule, with `@` modifiers and scope keys, `@comment`
everywhere and unknown keys refused (`misc/kumo-format.md`, *How it is written*). What is still
to come:

- **Registered operations** take `@fallback` where they declare it, as `field` does (spec,
  *Reading the entity*): `{"mobends:is_sitting": [], "@fallback": false}`.
- Operations and drivers share one registry namespace: `core:spring` names one thing.

## Values and expressions

### Names

Scoped names are in the spec (*Definitions and statements*). Still to come:

- **Bare names are only built-ins** (spec, *Built-in values*): a bare name that isn't a
  built-in is a load error, and adding a built-in never collides with anything a file declares.
  Today a bare name is also a value specific to a mob that its data class registers (a state if
  in capitals), until those become operations (task 12).

### Operations

An operation is `{"<name>": [arguments...]}`, plus the modifiers it accepts (see *How constructs
are written*). Arguments are positional only.

- **Bare operation names are the language** (`add`, `if`, `rose`): a fixed set, part of the
  format, Mo' Bends' only.
- **`namespace:id` names are registered** Java logic, and the namespace says who registered it:
  `core:` is what Mo' Bends registers directly (operations on vanilla classes, node types,
  drivers), `mobends:` is Mo' Bends' own content registered through the addon API, and an
  addon's operations carry its mod id.
- **Spelling:** language names are short and camelCase (`lt`, `if`, `wrapDegrees`,
  `easeInOut`); registered ids are snake_case, as Minecraft's registry ids are
  (`core:holds_item`).

```json
{"mymod:smoothstep": ["node.t"]}
{"mobends:is_sitting": [], "@fallback": false}
{"core:holds_item": ["main_hand", "minecraft:torch"]}
{"core:equipment_name": ["head", "^Notch.*"]}
{"decreased": ["entityTicksAfterAttack"]}
{"mymod:distance_to_nearest": ["minecraft:zombie"], "@fallback": 100}
```

Each parameter has a kind, checked at load:

| kind | accepts | example |
|---|---|---|
| number | any number expression, evaluated every frame | `"node.t"`, `{"mul": [...]}` |
| boolean | any boolean expression, evaluated every frame | `"nodeIsActive"`, `{"not": [...]}` |
| constant number | a number literal only | a window size |
| string | a string literal only | `"minecraft:torch"`, `"^Notch.*"` |
| choice | one of a fixed set of strings | `"main_hand"` / `"off_hand"`, `"x"` |

- A wrong argument fails with the operation's own words: `'core:holds_item' argument 2 (item)
  must be a string, got an expression`.
- An operation takes a few arguments (about three at most). Anything with more configuration is
  a typed object instead: a driver or a statement.

**`@fallback`** is also the modifier of registered operations that may not apply to the entity's
class: when the operation can't bind to the class, the fallback (any expression of the same type)
is compiled instead. Without one, that is a load error. (`field` already works this way.)

## Definitions and scopes

Definitions, scopes and their lifecycles are in the spec (*Definitions and statements*). Machines
have no clock and no phases of their own: the layer crossfades between nodes, never between
machines. A machine that needs a clock keeps one as `machine.` state advanced in its `update`
list. If that becomes common, a built-in is added, named for its scope the way
`nodeTicksElapsed` and `layerTicksElapsed` are (`machineTicksElapsed`), not an operation taking
the scope as an argument. A definition's other keys are `@` modifiers (only `@comment` today):
they are where per-definition rules go if any are needed (an `@override` for `extends`, what a
type publishes to its extensions).

### Entity-level definitions

The entity scope is in the spec (*Model definitions*, *Reading the entity*). Still to come: with
program and state split (see *Runtime*), entity state lives in the entity's state array
next to the animator's. Model definitions get an `extends` when a mob needs to share with
another; most of what every biped has is a built-in.

## Nodes, transitions and time

The node, layer and time built-ins are in the spec (*Nodes, transitions and time*, *Built-in
values*).

## Pose items

Pose items, their modifiers and the drivers' `inout` and `out` are in the spec.

## Entity values

The rule and the entity built-ins are in the spec (*Built-in values*). Still to come: the
player's swing filter moves from `PlayerData` to its model definition, and its animator reads
`entity.ticksAfterAttack` (*Values specific to a mob*), once the player has one (*Moving mobs
out of Java*).

### Values specific to a mob

Class-specific logic is a registered operation; class-specific memory is `entity.` state in the
mob's model definition.

The registered operations are in the spec (*Expressions*), `core:is_flying`, the wolf's and the
spider's among them. Still to come: `mobends:spider_wall_rotation` (the wall the spider crawls
on, which its crawl yaw is measured from), and the type-file selector operations (`core:entity_type`,
`core:player_name`, `core:player_uuid`, `mobends:skin_variant`; task 13), which need no entity
data.

The player's state, for example. Its swing counter ignores a swing within 6 ticks of the last
counted one while the main hand holds an item, so a sword combo isn't restarted:

```json
"@define": {"ticksAfterAttack": {"state": 0}, "capeWavePhase": {"state": 0}},
"@on": {"update": [
  {"set": ["entity.ticksAfterAttack", {"add": ["entity.ticksAfterAttack", "ticksPerFrame"]}]},
  {"@when": {"and": [{"decreased": ["entityTicksAfterAttack"]},
                     {"or": [{"not": [{"core:holds_any_item": ["main_hand"]}]},
                             {"gt": ["entity.ticksAfterAttack", 6]}]}]},
   "set": ["entity.ticksAfterAttack", 0]},
  {"set": ["entity.capeWavePhase",
           {"mod": [{"add": ["entity.capeWavePhase",
                             {"mul": [{"if": [{"and": [{"core:is_flying": []}, "entityIsSprinting"]}, 4, 1]},
                                      "ticksPerFrame"]}]},
                    380]}]}
]}
```

Other mob-specific values in their files:

- player: `flightSpeedFactor` (`clamp(entitySpeed, 0, 0.2) / 0.2`), `flightPitch` (the angle
  between the motion and straight up, `atan2(entityXZSpeed, entityMotionY)` in degrees, times
  `flightSpeedFactor`), both live definitions,
  `canSpinAttack` (`mobends:spin_attack_enabled` and not `entityIsRiding`), `sprintJumpLeg`
  (state);
- squid: its rotation and the two "rotation low" flags (`field` reads);
- wolf: `tailWag` (over `mobends:wolf_interested_angle`, `entityTicksExisted`, `partialTicks`);
- zombie: its animation set (a constant from `entityId`) and walking style (a random value
  re-rolled every 80–100 ticks);
- spider: crawl progress and render yaw (state, or `mobends:` operations).

### Per-entity data

What an entity keeps besides its state: the entity, its bones and their smoothing, and render
outputs (the sword trail, the wolf pup's head size).

None of that differs by Java class, so **one generic data class serves every entity**: the bones
come from the model definition, and per-mob parts are **components** the model definition
switches on (`mobends:sword_trail`, `mobends:cape`). Code that cast to a data class today asks
for a component instead (`data.getComponent(SwordTrail.class)`). This is the end state of
*Moving mobs out of Java*: a mod's mob comes with no Java data class.

## Language reference

The language operations are in the spec (*Expressions*), `mcsin` and `mccos` among them (pure, so
the language's rather than `core:`), and `field` and `exists`, which only model definitions
write (spec, *Reading the entity*).

The built-in values are in the spec too (*Built-in values*).

## Operations and drivers in Java

Registered operations are in `docs/animation.md` (*Operations in Java*), and so is how
registration works. Notes on how they were settled:

- The entity readers are `registerEntityFloatReader` and `registerEntityBooleanReader`, not one
  `registerEntityReader`: a lambda returning a boolean and one returning a number can't overload
  one name in Java.
- A slot is a handle that reads through the context (`slot.get(context)`), so the operations
  written today keep working when state moves into the entity's flat array (task 2); until then
  each use keeps its values itself, an animator being compiled per entity.
- An entity of unknown class (a test with no entity) is one no entity reader applies to.

- Drivers settled on fields that stay as written (`ExpressionTemplate`, bone and state names as
  strings), turned into inputs, bones and states by the binder through `DriverBindArgs`, which
  names the field in its errors; not on typed template fields (`NumberExpr`, `BoolExpr`,
  `BoneRef`) checked by reflection. That leaves generating a driver's documentation from its
  template class for later, if it is wanted: the binder knows each field's kind, the template
  class doesn't.
- A driver has one hook, `DriverEvaluator.restart`, for `core:step_turn`, whose state is still
  in Java fields: it publishes its outputs at rest when its node is entered.

Still to come:

- **Driver state.** `core:step_turn`'s planted feet and the spider's legs move into declared
  state, arrays included (task 11); Mo' Bends' other drivers (`core:axis_rotate`, `core:vector`,
  `core:offset`, `core:accumulate`, the cape, the sword trail, the spider's) move to the API with
  theirs. Feet are positions in the world, which floats round far from the origin (a float has
  1/16 of a block at a million blocks out): declared state keeps them relative to the entity.

## Runtime

Program and per-entity state are in `docs/animation.md` (*Program and Per-entity State*).

---

# Part 2: Migrating from the old design

## Policy

v2 has not shipped. Anything in the format, the serializers and the addon API can change, and no
upgrade path for v2 files is needed.

## Why

An audit of the v2 format (`misc/kumo-format.md`, the deserializers in
`core/kumo/state/serializer`, and the shipped animators) found:

- **Several small languages for one job.** Expressions (numbers), trigger conditions (truth
  values), type-file selector conditions, and the `variables[]` recipe of model definitions
  (`scale`, `offset`, `fn`, `add`, `product`) each compute values in their own syntax.
- **Conditions are the bulk of the animators.** Across the shipped animators: 280 `core:state`,
  154 `core:compare`, 134 `core:and`, 123 `core:not` (92 of them `not(state)`). The longest
  condition (in `player.json`) is 472 characters.
- **Seven ways of deciding what a JSON value is**: a `type` key, a `type` key with a default,
  which key is present (`driver` vs `animationKey`), the one key of an operation object, a
  string meaning a named condition in one place and a named expression or variable in another,
  and maps with a reserved `"default"` key (damping, additive space).
- **Keys with two meanings.** A ramp's `when` is its up/down switch, but every other item's
  `when` skips it (`PoseNode.java` special-cases `RampDriver`). `set` on nodes, branches and
  connections takes constants; the `core:set` driver takes an expression.
- **Meanings with two names.** `triggerCondition` (connections) vs `when` (everything else);
  `name` (ramp, accumulate, spring) vs `variable` (`core:set`); `condition` vs `conditions`,
  `value` vs `values`; `"jumping"` vs `{"type": "core:named", "name": "jumping"}`.
- **Unknown names fail late or not at all.** Variables and states are only checked on the first
  frame; an unknown property silently matches only `unset`; a model definition's missing `field`
  silently becomes the constant `offset`.

## From the old constructs to the new

### Conditions become expressions

Done in the animators (named conditions are named expressions; `core:action` is an operation
until tags go), and in type-file selectors (task 13).

### Named values become definitions

Named expressions are live definitions, layer and node variables states, `set` maps and
`core:set` statements (done). Still to do:

| old | new |
|---|---|
| layer `variables` written only inside one machine (`player.json`'s and `skeleton.json`'s `combo` in `sword`, `fist` in `fists`) | `machine.` state; the layer's branches into the machines reset them to 0, which a machine's state does on entry anyway |
| subject variables and states registered in Java (`registerVariable`, `registerState`), properties (`getProperty`) | built-ins and registered operations (*Entity values from the data classes*) |


### Ramps, springs and accumulators

`core:ramp` is gone (done): the timer ramps (`relax`, `bringUp`, `bringUpPrev`) are `linstep`
over the node's clock, named in the node's expressions, and the switching ramps (`onFeet`,
`deep`) accumulators clamped to 0..1 with a signed rate; `readBeforeAdvance` went with them. A
ramp stepped before the items after it read it, so the eating and shield animations moved a frame
later, and `deep`, which read before advancing, a frame earlier (their goldens re-recorded).

### Drivers

The 12 old drivers did three jobs:

- **pose writers** (`core:axis_rotate`, `core:vector`, `core:offset`, `core:step_turn`, the
  spider legs, `mobends:cape`): stay drivers;
- **value updaters** (`core:ramp`, `core:accumulate`, `core:spring`, `core:set`): the ramp and
  `core:set` are gone, accumulators and springs step a declared state (`inout`), as
  `core:step_turn` and the spider legs write their outputs to one (`out`) (done);
- **side effects** (`mobends:sword_trail`): stays a driver.

Their private state moves from Java fields (`StepTurnDriver`'s planted feet, `SpiderData`'s
`Limb` objects) into declared state.

### Entity values from the data classes

The values every entity has are built-ins (spec, *Built-in values*; task 8), and the wolf's, the
spider's climbing, the player's flying and the spin-attack setting are operations (task 12). Still
to do: the values a mob's data class keeps or computes itself, which move into its own files
(its model definition: *Moving mobs out of Java*).

**Specific to a mob, still to do:**

| old (uses) | new |
|---|---|
| spider: `crawlProgress` (2), `crawlRenderYaw` (1) | spider state, and `mobends:spider_wall_rotation` |
| squid: `squidRotation` (2), `SQUID_ROTATION_LOW` (2), `SQUID_PREV_ROTATION_LOW` (2) | `field` reads in the squid's files |
| player: `flightSpeedFactor` (4), `flightPitch` (2), `SPRINT_JUMP_LEG`, the cape phase | player definitions and state (`flightPitch` is a live definition over the motion built-ins, *Values specific to a mob*) |
| zombie: `animationSet` (2), `currentWalkingState` (1) | zombie state: a constant from `entityId`; a random value on a timer |

The capitals rule for a bare name goes with the last of them.

**The swing filter.** `PlayerData.onAttack` ignores a swing within 6 ticks of the last counted one
while the main hand holds an item, so the player's `entityTicksAfterAttack` differs from every
other entity's. The filter moves to the player's model definition (see *Values specific to a
mob*), `player.json` reads `entity.ticksAfterAttack`, and the built-in counts the same for every
entity.

### Addon API

Drivers keep their Gson template classes, with the typed field set; `IPoseItem`'s `onNodeStarted` / `advance`
and the state in Java fields give way to declared state.

## Background: what data classes do

A data class (`EntityData` and its subclasses) is the companion object of one tracked entity
(`EntityDatabase`, by entity id). Surveyed 2026-10-03, it does seven jobs:

1. **Holds the bones and their smoothing.** `initModelPose` creates the `ModelPartTransform`s and
   the entity-level `globalOffset`, `localOffset`, `renderRotation`, `centerRotation`;
   `updateParts` advances their smoothing every frame. KUMO writes targets through `getBone`;
   rendering reads parts through `getPartForName` (mutators, `BindPoint`).
2. **Senses the world once per game tick** (`updateClient`): its own motion from position
   changes, its own ground check with touchdown and liftoff events, climbing, swing detection.
3. **Keeps clocks and memory every frame** (`update`): `ticksInAir`, `ticksAfterTouchdown`,
   `ticksFalling`, `ticksAfterAttack`, `climbingCycle`; the player's `sprintJumpLeg` and cape
   phase; the zombie's random walking style; the spider's crawl and per-leg IK state.
4. **Defines the animator's vocabulary**: `registerVariable`, `registerState`, the `getProperty`
   switch.
5. **Holds state for code drivers**, which cast to it: the cape driver to `PlayerData`, the sword
   trail to `BipedEntityData.swordTrail`, the spider legs to `SpiderData.limbs`.
6. **Owns the animator**: `setAnimator`, `animate()` (called from `Mutator` while rendering),
   `wantsVanilla()`.
7. **Sets up the model, and some behaviour**: the wolf pup's head size every frame; the player's
   6-tick swing filter (`PlayerData.onAttack`).

Where each goes:

| data-class state | in the new design |
|---|---|
| clocks and memory (3) | the same kind as state slots: `entity…` built-ins when shared by every entity, `entity.` state in a mob's model definition otherwise |
| code drivers' state (5) | driver state, declared |
| vocabulary (4) | built-ins and registered operations |
| bones and smoothing (1) | stays per-entity data; the animator's result is copied into the bones' targets each frame, so per-bone floats exist twice |
| render outputs (sword trail, pup head) | stay per-entity data |
| sensing (2) | built-ins computed by Mo' Bends |

The data-class memory differs from slots in scope (an entity's life, read by every layer and
extension) and rate (some per tick, some per frame); hence entity-level state and the `entity…`
built-ins.

## Moving mobs out of Java

Surveyed 2026-10-03. A model definition can already describe the player's mesh: `ModelPlayer`'s
fields are in the generated tables, each skin variant's `RenderPlayer` gets its own mutator, and
`split` makes elbows and knees. What it can't do is everything around the mesh.

Registration:

- **A definition can't take a built-in key.** `TypeFiles` reuses an existing bender with the same
  key (`TypeFiles.java:48-61`), so a definition keyed `player` is silently ignored while
  `PlayerBender` is registered (`DefaultAddon.java:31`).
- **Defined benders are never an entity's default model.** They are registered with
  `registerTypeBender`, not as defaults (`EntityBenderRegistry.java:75-82`), so they get no
  built-in type, and types or extensions without a `model` can't reach them. Needed: a way for a
  definition or type file to be the default for its entity class, under the built-in id.

Data that code casts to: the biped's and the player's are gone (done: layers and drivers read
bones by name, and the sword trail, held-item orientations and cape ripple are components, see
`docs/animation.md`). Left: `SpiderData` (the spider leg drivers) and `WolfData`
(`LayerWolfMisc`). With every cast gone, one generic data class serves every entity (*Per-entity
data*).

State and logic (the player's states, counters, sprint-jump leg, cape phase, swing filter and
spin-attack config; the zombie's random walking style; the wolf's method-backed angles) are what
definitions, built-ins and operations cover; the zombie's needs `entityId` and a timer.

Rendering, where definitions fall short:

- **Layers** (done for the player's: a definition's `layers` section). The sheep's wool and the
  charged creeper's armour still draw their own unanimated copies: they need layers of their own.
- **Stand-ins, split segments, pivots, overlays** (done: `misc/kumo-format.md`, *Model
  definitions*; the player's definition draws what `PlayerMutator` does, `PlayerGeometryTest`).
- **First person, slim skins, renderer settings** (done: `renderer.firstPersonRest`, positions
  adopted again from each renderer, `renderer.sneakOffset`; the sword trail is a component).

Other mobs:

- **Zombies, skeletons:** the biped points above.
- **Spider:** longer legs rebuilt from constants (definitions can't resize geometry), leg IK state
  on `SpiderData`.
- **Squid:** close; needs the comparisons and a section offset.
- **Wolf:** nose, mouth and ears are cut from one part's boxes (needs bones built from selected
  boxes), texture rotations for the body and mane, and extra textured meshes (`LayerWolfMisc`).

The plan for the player:

1. Stand-ins that forward visibility, rendering and `postRender` to the real part, and
   `postRender` through split segments.
2. Pivots that re-base the boxes; overlay segments attached to another bone's segments.
3. A `layers` section mapping vanilla layers to animated ones, with a bone-name armour wrapper
   (also fixes the sheep's wool and the charged creeper's armour).
4. Components (sword trail, cape, item orientations) instead of casts to data classes.
5. Renderer settings (sneak offset, trail) and the first-person rest pose.
6. Default-model registration for definitions under the built-in id; then delete `PlayerBender`.
7. Adopting pivots again when the renderer changes (slim skins).

Zombies, skeletons and the rest follow the same steps, plus their own items above.

## Smaller renames and cleanups

Decided:

- Singular / plural pairs: none are left. Selectors became expressions, whose arguments are
  always lists (`{"core:player_name": ["Notch", "jeb_"]}`).
- The reserved `"default"` key of per-bone maps (damping, `additiveSpace`) is spelled `@default`
  (`{"@default": "pre", "body": "post"}`): `@` keys are the format's own, so no bone name is
  reserved.
- Every fixed set of words is lower_snake_case, as operation choices and registry ids are:
  spaces (`pre`, `post`, `override`), axes (`x`, `y`, `z`), vector modes (`slide`, `retarget`,
  `snap`), hinges (`front`, `back`, `top`, `bottom`), layer modes (`additive`), mask modes
  (`include_only`), easings (`ease_in_out`).

## Format version

Decided: the format stays `formatVersion: 2`, since v2 hasn't shipped.

---

# TODO

Ordered by what the rest builds on: each task needs only the ones above it, and the list ends with
the additive and smaller ones.

**Engine foundations**

1. [x] **Resolve names at compile time** in the current runtime: node and layer variables
   numbered, subject names indices, unknown names fail before the animator animates.
2. [x] **Split program from state**: an animator compiles once into a program shared by every
   entity of its class; each entity keeps a flat state (*Runtime*).
3. [x] **The expression language**: one typed expression tree (number or boolean, checked at load)
   replacing trigger conditions; the language operations; `decreased`, `rose`, `fell`; nothing
   short-circuits. Subject states are names in capitals until the built-ins (task 8); selector
   conditions are task 13, `variables[]` and `field` task 14.
4. [x] **Scopes and definitions**: animator, layer, machine and node scopes with their
   lifecycles; `@define` with constant, state and live definitions; scoped names with no lookup
   and no shadowing; live definitions computed once per frame; cycles a load error. The entity
   scope comes with task 14.
5. [x] **Statements**: `set`, `@on` lists (`enter`, `update`, `exit`) and a transition's `do`, their
   order in a frame and on a transition, who may set what, and the trust rule.
6. [x] **The one-key syntax**: pose items and nodes as one key plus `@` modifiers, `@connections`
   and `@when` on scopes, `{"when", "then"}` connections, `@comment` everywhere, unknown keys a
   load error. `@fallback` came with `field` (task 14).

**Built-ins and operations**

7. [x] **Node and time built-ins**: the node phases, `nodeTicksElapsed`, `layerTicksElapsed`,
   `nodeFadeProgress`, `nodeIsFinished`, `clipLength` / `clipDuration`. `ticks`, `partialTicks`,
   `ticksPerFrame` and `random` come with task 8.
8. [x] **Entity built-ins**: the `entity…` values, renamed from the data classes; `entitySpeed` /
   `entityXZSpeed` as interpolated magnitudes; `entitySwingProgress` interpolated; `ticks`,
   `partialTicks`, `ticksPerFrame`, `random`. The player's swing filter moves with its model
   definition (*Values specific to a mob*).
9. [x] **The addon API in `core/`**: the opaque entity, float-only state with slot handles, typed
   template fields for drivers, the selector-safe flag; `registerFunction`,
   `registerEntityReader`, `registerOperation`; queued registration (*Operations and drivers in
   Java*). The typed template fields were settled otherwise with task 10.
10. [x] **Prototype the API on `core:spring` and `core:step_turn`**, settling argument passing, what
    bind gets, `PoseWriter` and purity (*Operations and drivers in Java*).
11. [x] **Drivers' private state as declared slots**: `core:step_turn`'s planted feet and the
    spider's ease-in. The spider's legs keep theirs on `SpiderData` until the generic data class
    (task 23).
12. [x] **Registered operations**: `core:holds_item`, `core:holds_any_item`, `core:active_hand_side`, `core:equipment_name`,
    `core:is_flying`; `mobends:use_action`, `mobends:attack_action`, the wolf's and the spider's
    operations, `mobends:spin_attack_enabled` (*Values specific to a mob*). The spider's wall
    rotation goes with its crawl state, into its own files.
13. [x] **Type-file selectors as expressions**: the selector operations (`core:entity_type`,
    `core:player_name`, `core:player_uuid`, `mobends:skin_variant`) and precedence counted over
    expressions (*Files*).

**Format features on top**

14. [x] **Entity-level definitions in model definitions**: `@define` / `@on` with `field`, `exists`
    and `@fallback`, replacing `variables[]`. Entity state in the entity's state array comes with
    task 2. Vanilla boolean and object fields resolving in production is a task in `TODO.md`.
15. [x] **`extends` and extensions as scopes**: the merged `animator.` scope of an `extends` chain;
    an extension's own animator scope reading only `entity.` names, skipped when one is missing
    (spec, *Extensions*).
16. [x] **Mirroring without `negate`** (`misc/kumo-format.md`, *Mirroring*). The rule's `@when`
    comes with the one-key syntax (task 6).
17. [x] **Remove tags and `core:action`**, following other layers through definitions.

**Migrating content**

18. [x] **The generator** (`animation-lab/tools/gen_animators.ts`) and its 11 animators.
19. [ ] **The hand-written files**: done (`iron_golem`, `creeper`, `cow`, `wolf`, the model
    definitions, the type files, the example packs) but for the mob-specific values a data class
    keeps or computes: the player's swing filter, cape phase, `flightSpeedFactor`, `flightPitch`
    and `SPRINT_JUMP_LEG`; the zombie's animation set and walking state; the squid's rotation; the
    spider's crawl. They move into each mob's files with tasks 22–24 (*Entity values from the data
    classes*).
20. [x] **Tests**: the ones with inline animator JSON and the ones on shipped assets; the goldens
    re-recorded where behaviour moved on purpose (the riding threshold is 0.2 blocks per tick).
21. [x] **Docs**: `misc/kumo-format.md`, `docs/animation.md`, `docs/content.md`,
    `docs/overview.md`, `CHANGELOG.md`; the entries of this document leave as they land.

**Moving mobs out of Java**

22. [ ] **The player as files**, in the order of *The plan for the player*: forwarding stand-ins and
    `postRender` through split segments; pivots and overlay segments; the `layers` section and a
    bone-name armour wrapper; components instead of casts; renderer settings and the first-person
    pose; default-model registration under the built-in id; adopting pivots again for slim skins.
23. [ ] **One generic data class**, with per-mob parts as components declared by the model
    (*Per-entity data*).
24. [ ] **Zombies, skeletons and the rest** the same way, plus their own items (*Moving mobs out of
    Java*).

**Smaller**

25. [x] **Rename the held-item bones** `renderLeftItemRotation` / `renderRightItemRotation` to
    `leftHeldItem` / `rightHeldItem`.
26. [x] **The generator writes the format directly**: its builders write one-key items, scoped
    names, `@define` and statements themselves; `kumo_scopes.ts` and the last passes are gone.
27. [x] **The remaining cleanups**, as decided (*Smaller renames and cleanups*): `@default`, and
    lower-case enum values.

**After v2**

28. [x] **Functions** (`misc/kumo-format.md`, *Functions*).
