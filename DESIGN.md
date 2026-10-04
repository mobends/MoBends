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

- **Type files** say which model and animator an entity gets while a selector holds. The
  selector is an expression over operations that need no entity data (`core:entity_type`,
  `core:player_name`, `core:player_uuid`, `mobends:skin_variant`).
- **Model definitions** describe a mob's bones over its vanilla model, and the values the mob
  exposes to animators: they are the only files that declare `entity.` definitions.
- **Animators** are layers of state machines whose nodes pose the bones.
- **Extensions** add layers on top of a type's animator.

**Type selector precedence** counts conditions as today, over expressions: an operation that
isn't `and`, `or`, `not` or `if` counts 1 (comparisons included); `and` counts the sum of its
operands, `or` the fewest of any operand (it holds only as narrowly as its broadest branch),
`not` 1, and `if` its condition plus the fewer of its two branches.

**Every mob is described by files.** Mo' Bends' own mobs, the player included, are type files
plus model definitions like any mod's or resource pack's: `mobends:player` is a type file Mo'
Bends ships. A mod or pack needs Java only to bring new operations or drivers.

## How constructs are written

Pose items and nodes follow the one-key rule, with `@` modifiers and scope keys, `@comment`
everywhere and unknown keys refused (`misc/kumo-format.md`, *How it is written*). What is still
to come:

- **Operations** take `@fallback` where they declare it:
  `{"mobends:is_sitting": [], "@fallback": false}`.
- Operations and drivers share one registry namespace: `core:spring` names one thing.

## Values and expressions

### Names

Scoped names are in the spec (*Definitions and statements*). Still to come:

- **Bare names are only built-ins** (`nodeTicksElapsed`, `partialTicks`, `entityIsOnGround`):
  what Mo' Bends provides, always available, no arguments, independent of the entity's class. A
  bare name that isn't a built-in is a load error, and adding a built-in never collides with
  anything a file declares. Today a bare name is also any variable or state the entity's data
  class registers (task 8 renames them).
- `entity.` names, declared by the mob's model definition (*Entity-level definitions*).

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
| choice | one of a fixed set of strings | `"main_hand"` / `"off_hand"`, `"X"` |

- A wrong argument fails with the operation's own words: `'core:holds_item' argument 2 (item)
  must be a string, got an expression`.
- An operation takes a few arguments (about three at most). Anything with more configuration is
  a typed object instead: a driver or a statement.

**`@fallback`** is the modifier of operations that may not apply to the entity's class: when the
operation can't bind to the class, the fallback (any expression of the same type) is compiled
instead. Without one, that is a load error.

### Reading the entity: `field`

`{"field": ["ridingEntity", "motionX"]}` reads `entity.ridingEntity.motionX`: the array is a
**path**, one field per step, starting at the entity.

**`field` is written only in entity-level definitions** (a model definition's `@define` and
`@on`); anywhere else it is a load error. Animators read `entity.` names and stay portable: how a
mob provides a value stays in its own files. Model definitions are trusted-only, so a resource
pack can't read arbitrary fields. The restriction can be lifted later without breaking a file.

- Resolved at load: each step is looked up on the declared type of the step before it, starting
  at the entity's class and walking up superclasses. The last field must be a number or a
  boolean, which is the expression's type.
- Fields are named by their development (MCP) names. Vanilla classes go through generated
  accessors (`GenVanillaFields.kt`), anything else through reflection.
- `@fallback` takes any expression of the same type: another field, a name, a constant, an
  operation. If the path can't be resolved on the entity's class, the fallback is compiled
  instead; if a step is `null` at runtime (not riding anything), the fallback is evaluated for
  that frame. Without a fallback, a path that can't be resolved is a load error.

  ```json
  {"field": ["destPos"], "@fallback": {"field": ["flapTarget"]}}
  {"field": ["ridingEntity", "renderYawOffset"], "@fallback": 0}
  {"field": ["isCharging"], "@fallback": false}
  ```
- Interpolating between ticks is written out:
  `{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}`.

### Functions (after v2)

Functions are not in v2, but the format keeps room for them. A function is a definition with
parameters, for an expression repeated with different inputs:

```json
"@functions": {
  "wobble": {"params": {"t": "number", "amount": "number"},
             "body": {"mul": [{"sin": [{"mul": ["arg.t", 6.28]}]}, "arg.amount"]}}
},
...
{"core:axis_rotate": {"bone": "head", "axis": "Z",
                      "angle": {"animator.wobble": [{"div": ["nodeTicksElapsed", 20]}, 5]}}}
```

The intended design:

- **A call is inlined**: it behaves exactly as if the body were written out at the call site
  with the arguments substituted. The body is type-checked once, where it is declared. So every
  call site gets its own slots for the body's stateful operations, a body has no `set` (it is an
  expression), and recursion is a load error.
- **Called by scoped name** as the operation key (`{"animator.wobble": [...]}`). The scope says
  where it is declared, and the key can't collide with language (bare) or registered (`ns:id`)
  names.
- **Parameters** are read as `arg.t`, declared with the kinds operations have (number, boolean,
  constant number, string, choice), and passed positionally.
- **Declared** under `@functions` on the animator, layers and machines. A body reads its
  parameters, built-ins and the definitions of its declaring scope. Files share functions only
  through `extends`.

**Reserved until then:** the `@functions` key, and operation keys of the form `<scope>.<name>`.
Both are load errors in v2, so adding functions breaks no file.

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

The entity scope holds what a mob exposes and remembers. **Only the mob's model definition
declares it**, in its `@define` (and `@on`):

- It names the entity class, which `field` reads and operations bind against.
- It is the one file every type, animator and extension for that model shares. Several types can
  choose one model (a pack's type for one player name and the default type); if types declared
  entity values, which `entity.` names exist would depend on which type won, and a file reading
  `entity.foo` would load for one entity and fail for another of the same class. Type files stay
  selectors: they run before entity data exists.
- Extensions don't declare entity definitions: what an extension needs for itself is in its own
  animator's `@define`. Two extensions can't collide on an `entity.` name, and a duplicate name is
  a load error within one file.
- Model definitions have no `extends` yet. Most of what every biped has is a built-in; sharing
  between definitions is added when a mob needs it.

What it declares:

- **live** definitions over `field`, e.g. the chicken's wings:

  ```json
  "@define": {
    "flapWave":  {"live": {"add": [{"mcsin": [{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}]}, 1]}},
    "flapSpeed": {"live": {"lerp": [{"field": ["oFlapSpeed"]}, {"field": ["destPos"]}, "partialTicks"]}},
    "wingAngle": {"live": {"mul": ["entity.flapWave", "entity.flapSpeed", 57.29578]}}
  }
  ```
- **state** a mob keeps across frames, updated with `set` (see *Entity values*).

With program and state split (see *Runtime*), entity state lives in the entity's state array next
to the animator's.

## Statements

Statements, their lists and their order are in the spec (*Definitions and statements*). Still to
come: the entity's `update` list, which runs first in a frame (*Entity-level definitions*), and
`nodeIsFadingOut`, with which a statement in the `update` list of a node fading out can opt out
of setting a state the current node sets too.

### Extensions

An extension's animator is a scope of its own: its `animator.` names are its own, so extensions
never see or pollute each other's or the extended animator's. What an extension may follow is what
the type publishes at entity level (`entity.walking`, `entity.onFeet`), an explicit contract between
a mob's files and the extensions written for it.

An `entity.` name the model doesn't declare is a load error for the extension that reads it: that
extension is skipped, and the type and the other extensions still animate. There is no fallback
for a name (a name is a string and carries no modifiers); an extension is written for a mob's
files.

## Nodes, transitions and time

### Node phases

Built-in booleans for the node being evaluated; exactly one holds at a time:

| | the node is | ends when |
|---|---|---|
| `nodeIsFadingIn` | the layer's current node, while the crossfade into it runs | the crossfade ends |
| `nodeIsActive` | the layer's current node, fully in | the layer leaves it |
| `nodeIsFadingOut` | the node the layer left, still posed while the crossfade runs | the crossfade ends; the scope is disposed |

The current node is `nodeIsFadingIn or nodeIsActive`.

**A node never runs twice at once.** A transition to the current node restarts it without a
crossfade; a transition during a crossfade freezes what is on screen into a snapshot and fades
from that, and the node being left is disposed (its `exit` list runs) at once.

### Clocks and time

| built-in | value |
|---|---|
| `ticks` | the local player's age in ticks plus `partialTicks`: the same for every entity; it restarts when the local player respawns or changes dimension |
| `partialTicks` | progress between two game ticks, 0..1 |
| `ticksPerFrame` | ticks this frame lasted (capped at 1, 0 while paused) |
| `nodeTicksElapsed` | ticks since the node being evaluated was entered |
| `layerTicksElapsed` | ticks since the layer started |
| `nodeFadeProgress` | the linear progress of the crossfade the node is part of, the same number on both sides: 0 when the transition starts, 1 when it ends, 1 with no crossfade running. How much of a node is shown is `nodeFadeProgress` fading in and `1 − nodeFadeProgress` fading out; the transition's easing applies to the blend, not to this value |
| `nodeIsFinished` | the node's timed clips are done (below) |
| `random` | a random number, 0..1 |

The node and layer clocks advance by `ticksPerFrame` after the frame is posed (a node reads 0 on
its first frame) and pause while the layer's `@when` doesn't hold.

**`nodeIsFinished` holds** from the frame every clip of the node that has a `duration` has run it
(`nodeTicksElapsed >= duration`), on every frame after, until the node is left. It is not an
edge: the frame it becomes true is `{"rose": ["nodeIsFinished"]}`. A node with no items is
finished at once; a node whose items are all untimed never is.

```json
"sit_down": {"core:pose": {"pose": [{"core:clip": {"animationKey": ".../sitting_down.json", "duration": 8}}]},
             "@connections": [{"when": "nodeIsFinished", "then": "sit", "transitionDuration": 1}]}
```

**Timers are functions of a clock.** A value that rises from 0 to 1 over the first ticks of a
node is `{"linstep": ["nodeTicksElapsed", 0, 10]}`; a wait is `{"ge": ["nodeTicksElapsed", 80]}`.
Neither needs state.

## Pose items

Pose items, their modifiers and the drivers' `inout` and `out` are in the spec. Still to come: a
clip's `duration` read in its `frame` as `clipDuration` (today `duration`), next to `clipLength`.

## Entity values

**The rule:** a value that holds for every entity is a built-in, named `entity…`; one that
depends on the entity's class is a namespaced operation (`core:` for vanilla classes, `mobends:`
for Mo' Bends' own content). Every animated entity is an `EntityLivingBase` (addons can only
register living entities), so "every entity" means anything vanilla declares on `Entity` or
`EntityLivingBase`, or that Mo' Bends computes for every living entity.

### Entity built-ins

| built-ins | from |
|---|---|
| `entityLimbSwing`, `entityLimbSwingAmount`, `entitySwingProgress`, `entityHeadYaw`, `entityHeadPitch` | the arguments the vanilla renderer passes to every model, captured by the mutator (`entitySwingProgress` is vanilla's swing progress interpolated by `partialTicks`) |
| `entityIsSprinting`, `entityIsSneaking`, `entityIsAlive`, `entityIsInWater`, `entityIsRiding` | `Entity` |
| `entityIsChild`, `entityIsLeftHanded`, `entityHealth`, `entityIsSwinging`, `entityIsSleeping`, `entityIsElytraFlying` | `EntityLivingBase` (false or 0 unless a subclass says otherwise) |
| `entityId` | vanilla's entity id (per-entity variety) |
| `entityTicksExisted`, `entityTicksElytraFlying` | vanilla's age and glide time |
| `entityItemUseTicks`, `entityItemUseTicksLeft` | ticks the item in use has been used so far, and ticks left |
| `entityIsOnGround`, `entityIsClimbing` | Mo' Bends' own detection: collision boxes and stairs, with liftoff detection; ladders |
| `entityTicksInAir`, `entityTicksAfterTouchdown`, `entityTicksFalling`, `entityClimbingCycle` | Mo' Bends' counters over that detection |
| `entityTicksAfterAttack` | ticks since the last counted swing (below) |
| `entityIsUnderwater`, `entityLedgeHeight`, `entityClimbingRotation` | Mo' Bends' block checks around the entity |
| `entityIsDrawingBow`, `entityIsRidingLiving` | the held items; what the entity rides |
| `entityIsStandingStill`, `entityIsStrafing` | Mo' Bends' measured motion |
| `entityMotionY`, `entityPrevMotionY`, `entityInterpolatedMotionY`, `entitySpeed`, `entityXZSpeed`, `entityForwardMomentum`, `entitySidewaysMomentum` | Mo' Bends' measured motion: position changes each tick, so it works for every entity. `entitySpeed` and `entityXZSpeed` are the magnitudes interpolated by `partialTicks` (`getInterpolatedMotionMagnitude`, `getInterpolatedXZMotionMagnitude`) |
| `entityBodyYaw`, `entityWorldX/Y/Z` | the body yaw and position, interpolated as the renderer does (`core:step_turn`'s default inputs) |
| `entityRidingRelativeHeadYaw`, `entityRidingRelativeYaw` | relative to the ridden entity (0 unless riding something living) |
| `entityClimbingRenderYaw`, `entityClimbingBodyYaw`, `entityClimbingHeadYaw` | the yaws while climbing |

Values left out are added when something needs them; adding a built-in never breaks a file.

**`entityTicksAfterAttack`** counts swings for every entity: once per game tick it checks
vanilla's `isSwingInProgress`, counts a swing when one starts, and again whenever more than 5
ticks have passed while it keeps going (holding attack, mining). The counter goes back to 0 on a
counted swing and grows by `ticksPerFrame` every frame. `{"decreased": ["entityTicksAfterAttack"]}`
is the edge of a counted swing (`{"rose": ["entityIsSwinging"]}` would miss the re-counts of a
swing that keeps going).

### Values specific to a mob

Class-specific logic is a registered operation; class-specific memory is `entity.` state in the
mob's model definition.

The held-item, use-action and equipment operations are in the spec (*Expressions*). The others:

| operation | |
|---|---|
| `core:is_flying` | `EntityPlayer` flying |
| `mobends:is_sitting`, `mobends:wolf_interested_angle`, `mobends:wolf_shake_angle` [offset], `mobends:wolf_tail_rotation` | the wolf |
| `mobends:is_beside_climbable`, `mobends:spider_wall_rotation` | the spider |
| `mobends:spin_attack_enabled` | the player's spin-attack config option |
| `core:entity_type`, `core:player_name`, `core:player_uuid`, `mobends:skin_variant` | type-file selectors only (no entity data exists yet) |

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
the language's rather than `core:`). Still to come: `field` [path] (+ `@fallback`) and `exists`
[path] (whether a field path reaches a non-null object), written only in model definitions
(*Reading the entity*).

Built-in values: the clocks and node phases (*Clocks and time*, *Node phases*), `clipLength` and
`clipDuration`, the `entity…` built-ins (*Entity built-ins*).

## Operations and drivers in Java

An operation is a signature and up to three steps:

1. **Signature**, declared once and used for checking, error messages and documentation: the
   name (the registry adds the mod id), the parameters (name, kind, whether the last one
   repeats), the result type, the modifiers it accepts (`@fallback`), and whether it is
   **pure** (same arguments, same result, no entity, no state; a pure operation with constant
   arguments is computed once at load).
2. **Bind**, once when the program is compiled for an entity class: it gets the constant
   arguments and the entity class, does its one-time work (looks up `minecraft:torch` as an
   `Item`, compiles a pattern), and returns the evaluator, or says the operation doesn't apply to
   that class (the `@fallback` is used, or the load fails).
3. **Evaluate**, every frame: it gets the context (entity, built-in values, scopes), its evaluated
   number and boolean arguments, and its state.
4. **State**, for stateful operations only: named fields, scalars or **arrays**, with initial
   values. Array sizes can depend on what bind saw (a driver configured with four legs keeps four
   of each per-leg value). They are laid out flat in the entity's state array when the program is
   compiled; every place the operation is written gets its own, reset when the scope holding that
   place is created.

Three levels of registration, all through the registry that adds the mod id:

```java
// A pure function of numbers. Computed once at load when its argument is a literal.
registry.registerFunction("smoothstep", t -> t * t * (3 - 2 * t));

// Reads the entity. The class is the applicability check: other classes get the @fallback or
// a load error, and the operation never casts.
registry.registerEntityReader("is_wet", Entity.class, entity -> entity.isWet());

// Everything else: the full signature, bind, evaluate and state.
registry.registerOperation(Operation.named("distance_to_nearest")
        .param("entityType", Kind.STRING)
        .returns(Type.NUMBER)
        .bind((args, entityClass) -> {
            Class<? extends Entity> target = EntityList.getClass(new ResourceLocation(args.string(0)));
            if (target == null) throw args.error(0, "unknown entity type");
            return context -> nearestDistance(context.entity(), target);
        }));
```

- **Drivers** are registered the same way (signature, bind, evaluate, state) but take named
  fields. Their private state (`core:step_turn`'s planted feet, the spider's legs) is declared
  state, arrays included. A driver's signature names its outputs, which a file maps to states in
  `out`, and whether it takes an `inout` state (*Driver outputs, accumulators and springs*).
- A stateful operation or driver gets initial values for its state and no enter / exit hooks;
  hooks are added if something needs them.
- Mo' Bends defines its own operations and drivers with the same API. The language operations
  use the same signature model but live in a table addons can't write to, which keeps bare names
  Mo' Bends'.
- The language needs a little more typing than addons: `if` returns the type of its branches,
  `eq` compares two numbers or two booleans. That stays internal; addon operations have fixed
  types.

### Where the API lives

The operation and driver API is part of `core/`, the engine shared by every Minecraft version, so
it can't name a Minecraft type (`Entity`, `Item`, `ResourceLocation`). Each Minecraft version's
mod layer adds entity-typed helpers on top (`registerEntityReader`, the `Entity`-typed
`distance_to_nearest` above), and `AddonAnimationRegistry` adds the mod id.

### Decided

- **The entity in core** is an opaque `Object` and its `Class<?>`. Bind checks applicability
  with `isAssignableFrom` and returns the evaluator, or says the operation doesn't apply. The mod
  layer's helpers do the typed cast, so addon code never casts.
- **State is floats only.** A boolean is 0 or 1, and state holds no objects, which keeps an
  entity's state one flat `float[]`. A signature declares its slots and gets back handles,
  `FloatSlot` or `FloatArraySlot` (sized at bind: four legs keep four of each per-leg value);
  evaluate reads and writes through them on the entity's state. Everything drivers keep in Java
  fields today (`StepTurnDriver`'s planted feet, the spider's legs) is positions and angles.
  Whatever bind computes that isn't per entity (an `Item` looked up from `minecraft:torch`, a
  compiled `Pattern`) is captured in the evaluator, which is part of the program.
- **Driver fields are Gson template classes**, as today, with field types restricted to a fixed
  set: `NumberExpr`, `BoolExpr`, `StateRef` (for `inout` and `out` targets), `BoneRef`,
  primitives, enums, and lists of nested templates (`core:step_turn`'s `legs`). The loader
  reflects over the class to check kinds, word the errors and generate the docs. Operations keep
  their builder signature: they have a few positional arguments, drivers many named, nested
  fields.
- **Selector-safe operations** carry a flag in their signature: they bind against the entity
  class and evaluate against the selector context (player name, UUID, skin variant), not entity
  data. Any other operation in a type file's selector is a load error. This replaces
  `registerSelectorCondition`.

### To settle while prototyping

Settled by porting `core:spring` and `core:step_turn` to the new API, which will show any gap:

- **Arguments at evaluate** are passed already evaluated, through a reused view (`args.number(0)`,
  `args.bool(1)`) that doesn't allocate. Nothing short-circuits, so an operation never needs its
  arguments lazily.
- **What bind gets**: the constant arguments (`args.string(i)`, `args.choice(i)`,
  `args.constant(i)`), the entity class, `args.error(i, message)` for errors in the operation's
  own words, and, for drivers, bone lookup from name to index.
- **How much of the pose drivers see**: a narrow `PoseWriter` (by bone index, in a space:
  `PRE`, `POST`, `OVERRIDE`) rather than `Pose`, so the pose buffers can change without breaking
  addons.
- **Purity**: `registerFunction` implies it; the full builder opts in with `.pure()`. A pure
  operation that isn't only loses constant folding.

## Runtime

### Program and per-entity state

- **Program**: an animator is compiled into an immutable tree (the node and machine graph,
  expressions, resolved names and field paths, bound operations, bone indices), shared by every
  entity it was compiled for.
- **State**: every stateful element gets a slot at compile time: a node's clock, an
  accumulator's and a spring's values, a spring's velocity, an edge trigger's memory, a layer's
  current node, the definitions' state and constants. An entity owns only a flat state array and
  its pose buffers; evaluation takes the entity's state alongside the program.
- A scope's slots are one contiguous range, reset when the scope is created.
- **Constant folding.** A constant that reads nothing per entity (no entity value, no state, no
  `random`) is computed once and lives in the program, not in every entity's state; so is a pure
  operation with constant arguments. This is the compiler's choice and changes nothing in files.

### What a program is compiled for

A program is cached by everything that changes the compiled result:

- the animator file and its extensions, in order (with their `extends` chains);
- their trust, and the resource-pack limits in force;
- the entity class: `field` paths resolve against it and operations bind against it. Names don't
  depend on the entity: they are declared in files or are built-ins.

The cache is cleared on a resource reload and when the server's policy changes. A path or
operation that fails on one entity class fails that class's program only; failures are reported
once per animator file and extensions.

## Open questions

- **Addon API details.** Argument passing at evaluate, what bind gets, the pose drivers see,
  and purity, to settle while porting `core:spring` and `core:step_turn` (*To settle while
  prototyping*).

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
until tags go). Still to do: type-file selectors (`core:entity_type`, ..., and their own `and` /
`or` / `not`) use the same syntax.

### Named values become definitions

Named expressions are live definitions, layer and node variables states, `set` maps and
`core:set` statements (done). Still to do:

| old | new |
|---|---|
| layer `variables` written only inside one machine (`player.json`'s and `skeleton.json`'s `combo` in `sword`, `fist` in `fists`) | `machine.` state; the layer's branches into the machines reset them to 0, which a machine's state does on entry anyway |
| a model definition's `variables[]` (`field` / `prevField` / `scale` / `offset` / `fn` / `add` / `product`) | entity live definitions over `field` |
| subject variables and states registered in Java (`registerVariable`, `registerState`), properties (`getProperty`) | built-ins and registered operations (*Entity values from the data classes*) |

The old `field` list in model definitions meant fallback names (the first that exists); a `field`
path now means a chain of fields, and fallbacks are the `fallback` option.

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

### Built-ins and time

| old | new |
|---|---|
| `elapsed` | `nodeTicksElapsed` |
| a clip's `clipLength` / `duration` names in `frame` | `clipLength`, `clipDuration` |
| `ticks`, `partialTicks`, `ticksPerFrame`, `random` (subject variables) | built-ins, same names and meaning (`ticks` is still the local player's age, as in `DataUpdateHandler`) |
| the layer's clock (`core:ticks_passed` read it; it is gone, see below) | `layerTicksElapsed` |
| crossfade progress (not readable) | `nodeFadeProgress` |

`core:ticks_passed` is gone. On a node's connections and items it was the node's clock, now
`{"gt": ["elapsed", n]}` (the wolf's breathing); on a layer's selector, connections or `when` it
counted from the layer's start (`layerTicksElapsed` once it exists); on a machine's, from the
machine's entry (a `machine.` state advanced in its `update` list, when something needs it).
`core:animation_finished` is the built-in `nodeIsFinished`.

**Node re-entry.** The rule that a node never runs twice at once is the old engine's
(`LayerState.beginTransition`): a transition to the current node restarted it without a
crossfade, whatever its duration, and an interrupted crossfade froze a snapshot.

### Entity values from the data classes

Every old subject variable, state and property, with its uses in the shipped animators where
counted.

**Built-ins, renamed:**

| old | new |
|---|---|
| `limbSwing`, `limbSwingAmount`, `swingProgress`, `headYaw`, `headPitch` | `entityLimbSwing`, `entityLimbSwingAmount`, `entitySwingProgress`, `entityHeadYaw`, `entityHeadPitch` |
| `SPRINTING`, `SNEAKING`, `ALIVE`, `CHILD`, `LEFT_HANDED`, `health` | `entityIsSprinting`, `entityIsSneaking`, `entityIsAlive`, `entityIsChild`, `entityIsLeftHanded`, `entityHealth` |
| `ON_GROUND`, `CLIMBING`, `UNDERWATER`, `ledgeHeight`, `climbingRotation` | `entityIsOnGround`, `entityIsClimbing`, `entityIsUnderwater`, `entityLedgeHeight`, `entityClimbingRotation` |
| `DRAWING_BOW`, `RIDING_LIVING` | `entityIsDrawingBow`, `entityIsRidingLiving` |
| `SWINGING` (18), `IN_WATER` (2), `RIDING` (23), `SLEEPING` (2), `ELYTRA_FLYING` (1) | `entityIsSwinging`, `entityIsInWater`, `entityIsRiding`, `entityIsSleeping`, `entityIsElytraFlying` |
| `STANDING_STILL` (65), `STRAFING` (1) | `entityIsStandingStill`, `entityIsStrafing` |
| `motionY` (4), `prevMotionY` (2), `interpolatedMotionY` (16), `motionMagnitude` (16), `xzMotionMagnitude` (3), `forwardMomentum` (5), `sidewaysMomentum` (4) | `entityMotionY`, `entityPrevMotionY`, `entityInterpolatedMotionY`, `entitySpeed`, `entityXZSpeed`, `entityForwardMomentum`, `entitySidewaysMomentum` |
| `bodyYaw`, `worldX/Y/Z` (`core:step_turn`'s defaults) | `entityBodyYaw`, `entityWorldX/Y/Z` |
| `ridingRelativeHeadYaw` (1), `ridingRelativeYaw` (1) | `entityRidingRelativeHeadYaw`, `entityRidingRelativeYaw` |
| `climbingRenderYaw` (1), `climbingBodyYaw` (4), `climbingHeadYaw` (1), `climbingCycle` (1) | `entityClimbingRenderYaw`, `entityClimbingBodyYaw`, `entityClimbingHeadYaw`, `entityClimbingCycle` |
| `ticksExisted`, `ticksInAir`, `ticksAfterTouchdown`, `ticksFalling`, `ticksAfterAttack` | `entityTicksExisted`, `entityTicksInAir`, `entityTicksAfterTouchdown`, `entityTicksFalling`, `entityTicksAfterAttack` |
| `itemUseMaxCount`, `itemUseCount` | `entityItemUseTicks`, `entityItemUseTicksLeft`: named for what they hold, not after vanilla's backwards `getItemInUseMaxCount` (ticks used so far) and `getItemInUseCount` (ticks left) |
| (new) | `entityId`, `entityTicksElytraFlying` |

- `swingProgress` was the mutator's interpolated value (vanilla's `getSwingProgress(partialTicks)`,
  a straight line between the last tick's value and this tick's, wrapping when a new swing
  starts); the old `entitySwingProgress` was vanilla's raw field, used once. The name goes to the
  interpolated value, and the one raw use switches to it (reading the raw field would only keep
  its jitter).
- The old `entityXZSpeed` (3 uses) read vanilla's `motionX`/`motionZ`, which the server doesn't
  send for other entities: they read 0 for every player but the local one. It is dropped; its
  uses (the player's `riding` node, choosing `riding_fast` above 0.01, ported from the old
  `RidingAnimationBit`) move to the measured `entityXZSpeed`, with a new threshold: the measured
  speed includes the mount carrying the rider, so 0.01 would pick `riding_fast` whenever the
  mount moves. (So the old split only ever worked for the local player.)

**Dropped, written out where used:**

| old (uses) | instead |
|---|---|
| `AIRBORNE` (8), `MOVING_HORIZONTALLY` (12) | `{"not": ["entityIsOnGround"]}`, `{"not": ["entityIsStandingStill"]}` |
| `headYawAbs` (4) | `{"abs": ["entityHeadYaw"]}` |
| `aimedBowTicks` (16) | `{"min": ["entityItemUseTicks", 15]}`, as an animator definition |
| the biped's `CAN_SPIN_ATTACK` (always true) | nothing |
| `rotationYaw`, `motionX/Z`, `prevMotionX/Z`, `movementAngle` (0) | nothing until needed |

**Specific to a mob:**

| old (uses) | new |
|---|---|
| `FLYING` (1) | `core:is_flying` |
| `core:equipment_name` (a condition) | `core:equipment_name` [slot, pattern] |
| wolf: `SITTING`, `interestedAngle`, `shakeAngleHead` / `Mane` / `Tail`, `tailRotation`, `tailWag` (1) | `mobends:is_sitting`, `mobends:wolf_interested_angle`, `mobends:wolf_shake_angle`, `mobends:wolf_tail_rotation`; `tailWag` a wolf definition |
| spider: `BESIDE_CLIMBABLE`, wall facing, `crawlProgress` (2), `crawlRenderYaw` (1) | `mobends:is_beside_climbable`, `mobends:spider_wall_rotation`; spider state or operations |
| squid: `squidRotation` (2), `SQUID_ROTATION_LOW` (2), `SQUID_PREV_ROTATION_LOW` (2) | `field` reads in the squid's files |
| player: `flightSpeedFactor` (4), `flightPitch` (2), `CAN_SPIN_ATTACK` (4), `SPRINT_JUMP_LEG`, the cape phase | player definitions and state (`flightPitch` is a live definition over the motion built-ins, *Values specific to a mob*); `mobends:spin_attack_enabled` |
| zombie: `animationSet` (2), `currentWalkingState` (1) | zombie state: a constant from `entityId`; a random value on a timer |

**The swing filter.** `LivingEntityData.updateClient` counted swings for every living entity
(once per tick: when `isSwingInProgress` and either not counted yet or more than 5 ticks since,
`onAttack()`), `onAttack` reset `ticksAfterAttack`, and `LivingEntityData.update` advanced it by
`ticksPerFrame` every frame. `PlayerData.onAttack` additionally ignored a swing within 6 ticks of
the last one while the main hand held an item. The built-in keeps the shared counting; the
player's filter moves to its model definition (see *Values specific to a mob*), and `player.json` reads
`entity.ticksAfterAttack` instead of the built-in.

### Addon API

`registerSelectorCondition` goes away (selector operations are flagged operations).
`registerTriggerCondition` is gone, and `registerOperation` registers a typed operation (the
signature of `ExpressionOperations`); what it still lacks is the entity class at bind and declared
state (*Operations and drivers in Java*). Drivers keep their Gson template classes, with the typed field set; `IPoseItem`'s
`onNodeStarted` / `advance` and the state in Java fields give way to declared state.

Registration timing is broken today: `Addons.registerAddon` calls `registerContent` only if
`CoreClient` already exists, and nothing calls it later, so an addon registered too early is
silently dropped. The new registry queues registrations and replays them once the core exists,
and rejects a registration made after the first animator has loaded.

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

## Splitting program from state

### How animators are compiled today

Verified 2026-10-03: **an animator is compiled per entity.** Only the parsed template is shared.

- `AnimatorResources` caches the parsed `AnimatorTemplate` per resource location.
- Every `EntityData` gets its own `KumoAnimatorController` (`EntityData.setAnimator`, called per
  entity by the type's data factory, `EntityType.java:183-186`).
- The controller builds its own `KumoAnimatorState` lazily on the first `animate(subject)`
  (`KumoAnimatorController.ensureLoaded`). That constructor compiles expressions and conditions,
  so every zombie compiles `biped.json` for itself.
- The compiled objects hold that entity's runtime state in their fields (a ramp's value, an edge
  trigger's memory, a node's clock, a layer's current node), which is why they can't be shared.
- Compiling doesn't see the subject: the constructor takes none, and bones are bound on the first
  frame (`Skeleton.bind`). But `ensureLoaded` runs inside `animate(subject)`, so passing the
  subject into compilation is a small change.
- An entity's class can't change under its animator: when its type changes, its data (and so its
  animator) is made anew.

### Measurements

Measured with `misc/bench/InstancingBench.java`: two mock engines running a synthetic animator
shaped like `player.json` (7 layers, 41 nodes, 295 items, 556 condition nodes, 1,043 expression
nodes). Both do the same work per frame and share the expression classes, so only instancing and
the layout of state differ. Java 8 (Minecraft 1.12's runtime), Apple M3 Pro, 2026-10-03.

Instancing, all entities' animators:

| entities | unified (today) | split | speedup |
|---|---|---|---|
| 1 | 0.03 ms | 0.03 ms (the compile, ~28 µs) | 1× |
| 10 | 0.27 ms | 0.04 ms | 7× |
| 100 | 2.7 ms | 0.10 ms | 27× |
| 1,000 | 26.5 ms | 0.55 ms | 48× |
| 10,000 | 639 ms | 16 ms | 40× |

One frame, all entities:

| entities | unified | split | speedup |
|---|---|---|---|
| 1–100 | 8.5 µs per entity | 8.6 µs per entity | 1.0× |
| 1,000 | 9.7 ms | 8.9 ms | 1.09× |
| 10,000 | 108.5 ms | 89.6 ms | 1.21× |

Retained heap for 10,000 animators: 617 MB unified (63 KB per entity), 135 MB split (13.8 KB per
entity, almost all of it the 7 layers × 5 pose buffers both need; the state slots are about
0.3 KB).

The wins:

- **No compile hitches.** Instancing is lazy, on an entity's first frame, so entities appearing
  together (joining a world, loading chunks) compile in the same frame. 100 players cost ~2.7 ms
  of a 16.7 ms frame today; with a cached program, ~0.1 ms. Instancing goes from ~27 µs to ~1 µs
  per entity.
- **About a fifth of the memory.** The unified engine duplicates ~49 KB of compiled tree per
  entity.
- **Runtime unchanged at Minecraft's usual entity counts**, 9–21 % faster from about a thousand
  entities, where the unified trees stop fitting in cache.
- **Cheaper stateful operations.** Per-use memory falls out of slot allocation; named conditions
  today get it by copying the condition at every use.

The mock understates today's instancing cost (no Gson trees, `ExpressionScope` chains, eager
validation or skeleton binding), and its runtime is simpler than KUMO's (names resolved to
indices in both; no previous node evaluated during a crossfade). Read the ratios, not the
absolute times.

Run it with
`javac -d /tmp/bench misc/bench/InstancingBench.java && java -Xmx4g -cp /tmp/bench InstancingBench`
(about 2 minutes). Options: `--shape` prints the generated animator's shape and exits;
`-Dseed=N` generates another animator (40, the default, is the one closest to `player.json`);
`-Dsections=runtime` runs only the runtime measurement; `-Dlookup=string` reads names as today's
runtime does (below).

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

Data that code casts to:

- `BipedEntityData`: the held-item and armour layers, `ArmorWrapper`, the sword trail driver and
  renderer;
- `PlayerData`: the cape driver and layer, elytra, the supporter accessories layer;
- `SpiderData`: the spider leg drivers; `WolfData`: `LayerWolfMisc`.

Needed: these read bones by name (as `BindPoint` already does) and per-entity parts as components
a definition switches on (sword trail, cape, item orientations `rightHeldItem` /
`leftHeldItem`). With every cast gone, one generic data class serves every entity (*Per-entity
data*).

State and logic (the player's states, counters, sprint-jump leg, cape phase, swing filter and
spin-attack config; the zombie's random walking style; the wolf's method-backed angles) are what
definitions, built-ins and operations cover; the zombie's needs `entityId` and a timer.

Rendering, where definitions fall short:

- **Layers.** `DefinedMutator.swapLayer` does nothing (`DefinedMutator.java:169-172`), so a
  defined mob keeps vanilla's armour, held-item and head layers, which draw their own unanimated
  model copies (the sheep's wool and the charged creeper's armour are the same problem). The
  player also needs the cape, elytra and accessories layers. Needed: a `layers` section mapping
  vanilla layers to animated replacements, and an armour wrapper that finds parts by bone name.
- **Stand-ins for parented bones.** A bone with a `parent` leaves an invisible stand-in in the
  vanilla field (`DefinedMutator.java:265-286`). Vanilla code that goes through the field then
  hits the stand-in: held items are placed at the origin (`postRenderArm`), the first-person hand
  renders nothing, and the skin-part toggles (sleeves, jacket, hat) and armour visibility are
  ignored. Needed: stand-ins that forward visibility, rendering and `postRender` to the real part.
- **Items and armour through split segments.** The Java parts pass the transform on to the
  forearm when an item is attached; defined parts don't. Needed: `postRender` through split
  segments, or a per-bone post-offset.
- **Pivots.** The Java biped pivots the body at the hips and offsets its boxes; a definition's
  `position` moves the pivot and the boxes together. Needed: a pivot that re-bases the boxes.
- **Overlay parts.** The player's sleeve and trouser layers (`bipedLeftArmwear`, ...) must split
  and follow the forearm and foreleg. Needed: split segments attached to another bone's segments.
- **First person.** The arm's rest pose for the first-person hand is Java
  (`PlayerMutator.poseForFirstPersonView`). Needed: a list of bones to rest in first person.
- **Slim skins.** Pivots are adopted once from whichever renderer comes first, but the skin
  variant reads `default` until the skin downloads. Needed: adopt again when the renderer
  changes.
- **Renderer settings.** The sneak offset (4 while flying, 5 otherwise) and the sword trail pass.

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

- The item bones `renderLeftItemRotation` / `renderRightItemRotation` are renamed
  `leftHeldItem` / `rightHeldItem`.

Proposed, not yet agreed:

- Singular / plural pairs collapse to the list form (`values`, `entityTypes`, `names`, `uuids`).
- The reserved `"default"` key in damping and additive-space maps can't be a bone name. Either
  move it out of the bone map (`{"default": 0.3, "bones": {...}}`), or spell it `@default`
  (`{"@default": 0.3, "body": 0.5}`), extending `@` to mean "a key the format reserves".
- Enum casing (`EASE_IN_OUT`, `PRE`) vs lower-case registry ids (`core:axis_rotate`).

## Format version

Assumed: the format stays `formatVersion: 2`, since v2 hasn't shipped.

## Migration work

Everything besides the engine that changes with the format (surveyed 2026-10-03):

- **`animation-lab/tools/gen_animators.ts`** (run with `./gradlew generateAnimators` in
  `animation-lab`) generates 11 animators: `biped`, `zombie`, `skeleton`, `pig_zombie`, `player`,
  `squid`, `spider`, `zombie_villager`, `quadruped`, `villager`, `chicken`. Its condition helpers
  (`cmp`, `state`, `AND` / `OR` / `NOT`, `prop`, `dec`, `conn`), clip and driver objects and
  selectors all change. It also holds the riding node's speed threshold, which needs a new value.
- **Animators not generated:** `iron_golem.json`, `creeper.json`, `cow.json` (hand-edited, per
  the generator's header), `wolf.json` (not in the generator's list).
- **Example packs** (`misc/examples/`): the `dance`, `zombie_arms`, `vanilla_swim`, `wave`
  animators; the type files with selectors (`bendy_tester.json`); their READMEs.
- **Type files** with selectors: `src/main/resources/assets/mobends/bends/types/*.json`.
- **Model definitions** using `variables[]`: `chicken.json`, `iron_golem.json`.
- **Tests with inline animator JSON:** `core`: `KumoAnimatorStateTest`, `KumoSerializerTest`
  (`TestSubject` parses); `animation-lab`: `ExtensionsTest`, `ClipFrameTest`,
  `AnimationLimitsTest`, `ExpressionTest` (and its fixture `expressions_parent.json`),
  `EntityTypesTest`.
- **Tests on shipped assets:** `KumoParityTest` (against `animation-lab/golden`),
  `SideEffectParityTest`, `SpinAttackTest`, `DefinedModelsTest`, `StepTurnTest`,
  `DanceExtensionTest`.
- **Parity goldens to re-record** (`./gradlew record` in `animation-lab`), where behaviour moves
  on purpose: the riding scenarios (the measured speed).
- **Lab bootstrap:** `LabBootstrap` mirrors the mod's registrations (`MinecraftKumoOperations`,
  the four `mobends:` drivers) and changes with the registration API.
- **Addon API:** `AddonAnimationRegistry`, `DefaultAddon`, `MinecraftKumoOperations`.
- **The held-item bones** (`leftHeldItem` / `rightHeldItem`): `BipedEntityData`,
  `LayerCustomHeldItem`, `SwordTrail`, `gen_animators.ts`, the `biped`, `player`, `skeleton` and
  `pig_zombie` animators, and 17 clips under `animations/biped`, `animations/player` and
  `animations/pigzombie`.
- **Docs:** `misc/kumo-format.md` (most sections), `docs/animation.md`, `docs/content.md` (the
  authoring recipes), `docs/overview.md` (extension points), `CHANGELOG.md`.

## Open questions about the migration

- **The riding threshold.** A new value for `riding_fast` on the measured speed, picked by running
  the riding scenario in the lab.

---

# TODO

Ordered by what the rest builds on: each task needs only the ones above it, and the list ends with
the additive and smaller ones.

**Engine foundations**

1. [x] **Resolve names at compile time** in the current runtime: node and layer variables
   numbered, subject names indices, unknown names fail before the animator animates.
2. [ ] **Split program from state** (deferred until after the format rework, tasks 3–17: converting
   the condition, ramp and tag classes those tasks delete would be wasted): compile an animator once per animator, extensions, trust and
   entity class into an immutable program; give every stateful element a slot in one flat
   per-entity `float[]`, a scope's slots one contiguous range; cache programs and clear the cache
   on a reload or a policy change (*Runtime*, *Splitting program from state*).
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
   load error. `@fallback` comes with task 12.

**Built-ins and operations**

7. [ ] **Node and time built-ins**: the node phases, `nodeTicksElapsed`, `layerTicksElapsed`,
   `nodeFadeProgress`, `nodeIsFinished`, `clipLength` / `clipDuration`, the clocks
   (*Nodes, transitions and time*).
8. [ ] **Entity built-ins**: the `entity…` values, renamed from the data classes; `entitySpeed` /
   `entityXZSpeed` as interpolated magnitudes; `entityTicksAfterAttack`'s counting for every
   entity; `entitySwingProgress` interpolated (*Entity built-ins*).
9. [ ] **The addon API in `core/`**: the opaque entity, float-only state with slot handles, typed
   template fields for drivers, the selector-safe flag; `registerFunction`,
   `registerEntityReader`, `registerOperation`; queued registration (*Operations and drivers in
   Java*).
10. [ ] **Prototype the API on `core:spring` and `core:step_turn`**, settling argument passing, what
    bind gets, `PoseWriter` and purity (*To settle while prototyping*).
11. [ ] **Drivers' private state as declared slots**: `core:step_turn`'s planted feet and the
    spider legs' (with task 2). `out`, `inout`, and the removal of `core:ramp`, `core:set` and
    `readBeforeAdvance` are done.
12. [ ] **Registered operations**: `field` / `exists` with `@fallback` (model definitions only);
    `core:holds_item`, `core:holds_any_item`, `core:active_hand_side`, `core:equipment_name`,
    `core:is_flying`; `mobends:use_action`, `mobends:attack_action`, the wolf's and the spider's
    operations, `mobends:spin_attack_enabled` (*Values specific to a mob*).
13. [ ] **Type-file selectors as expressions**: the selector operations (`core:entity_type`,
    `core:player_name`, `core:player_uuid`, `mobends:skin_variant`) and precedence counted over
    expressions (*Files*).

**Format features on top**

14. [ ] **Entity-level definitions in model definitions**: `@define` / `@on` with `field`, replacing
    `variables[]`; entity state in the entity's state array (*Entity-level definitions*).
15. [ ] **`extends` and extensions as scopes**: the merged `animator.` scope of an `extends` chain;
    an extension's own animator scope reading only `entity.` names, skipped when one is missing
    (*`extends`*, *Extensions*).
16. [x] **Mirroring without `negate`** (`misc/kumo-format.md`, *Mirroring*). The rule's `@when`
    comes with the one-key syntax (task 6).
17. [x] **Remove tags and `core:action`**, following other layers through definitions.

**Migrating content**

18. [ ] **The generator** (`animation-lab/tools/gen_animators.ts`) and its 11 animators.
19. [ ] **The hand-written files**: `iron_golem`, `creeper`, `cow`, `wolf`; the model definitions
    (`chicken`, `iron_golem`); the type files; the example packs and their READMEs. Includes
    `comment` → `@comment`, dropping `mirror` from the 13 head-yaw items, and the mob-specific
    values (the player's swing filter, cape phase, `flightPitch`; the zombie's, the wolf's, the
    squid's, the spider's) (*Entity values from the data classes*).
20. [ ] **Tests**: the ones with inline animator JSON and the ones on shipped assets; re-record the
    goldens where behaviour moves on purpose; pick the riding threshold in the lab
    (*Migration work*, *Open questions about the migration*).
21. [ ] **Docs**: `misc/kumo-format.md`, `docs/animation.md`, `docs/content.md`,
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

25. [ ] **Rename the held-item bones** `renderLeftItemRotation` / `renderRightItemRotation` to
    `leftHeldItem` / `rightHeldItem` (*Migration work* lists the files).
26. [ ] **The generator writes the format directly**: `gen_animators.ts` builds items, nodes and
    connections the old way and rewrites them in last passes (`oneKeyAnimator`, then
    `kumo_scopes.ts`, which turns bare names into scoped ones); its builders should write the
    format themselves, and `kumo_scopes.ts` then goes.
27. [ ] **Decide the remaining cleanups**: singular / plural pairs, the reserved `"default"` key,
    enum casing (*Smaller renames and cleanups*).

**After v2**

28. [ ] **Functions**, as designed in *Functions (after v2)*.
