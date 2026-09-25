# KUMO animator format, version 2

An animator is a JSON asset (`assets/mobends/bends/animators/<entity>.json`) that says how an
entity's bones get their targets every frame. The entity data (the model's bones with their
per-bone smoothing) is the *subject*; the animator only ever writes targets, the subject
smooths them. Everything the old procedural controllers did is expressed with the pieces below.

```json
{
  "formatVersion": 2,
  "extends": "mobends:bends/animators/biped.json",
  "layers": [ ... ]
}
```

`extends` puts a parent animator's layers first. Format 1 files (node arrays, index targets)
still load; the wolf's clip layers are one.

## Layers

Layers evaluate in order into one pose. Each has a node graph; the current node's pose stack
produces the layer's pose, which composites onto the result.

| field | meaning |
|---|---|
| `type` | `KEYFRAME` |
| `mode` | `OVERRIDE` (default: what the layer writes replaces) or `ADDITIVE` |
| `additiveSpace` | for additive layers: `"PRE"` / `"POST"`, or `{"default": "PRE", "body": "POST"}` |
| `when` | a condition; while it does not hold the layer writes nothing and its clocks pause |
| `variables` | layer variables and their initial values, e.g. `{"combo": 0}` |
| `damping` | default damping for the bones the layer writes (nodes and items override) |
| `mask` | an armature mask (legacy) |
| `mirror` | `{"when": <condition>, "pairs": [["leftArm","rightArm"], ...], "negate": ["headYaw"]}`: the rule items with `"mirror"` / `"swapSides"` follow |
| `entryNode`, `nodes` | the entry node's name and a map of nodes by name |

## Nodes

```json
"walk": {
  "type": "core:pose",
  "tags": ["walk"],
  "pose": [ ...items... ],
  "enterPose": [ ...items evaluated once on entry; their bones snap to it... ],
  "snapOnEnter": ["body"],
  "damping": {...},
  "set": {"combo": 0},
  "connections": [ {"target": "jump", "triggerCondition": {...}, "transitionDuration": 0, "transitionEasing": "EASE_IN_OUT", "set": {"combo": 1}} ]
}
```

* `tags` are the layer's *actions* (bends packs and `core:action` see them).
* Connections are checked before the node is posed, so a state change shows the same frame.
  Every condition is evaluated each frame (edge triggers stay fresh); the first one met fires.
  A connection's `set` assigns layer variables when it fires. `transitionDuration` crossfades
  (an interrupted crossfade continues from what was on screen); easings `LINEAR`, `EASE_IN`,
  `EASE_OUT`, `EASE_IN_OUT`, `EXPONENTIAL`.
* Legacy nodes `core:standard` (`animationKey`, `playbackSpeed`, `looping`) and `core:movement`
  (played by limb swing) still work and hard-set their bones.

## Pose items

A node's `pose` is a stack; later items compose over earlier ones. Common fields:

| field | meaning |
|---|---|
| `space` | `OVERRIDE` (replace), `PRE` (rotate in the parent's space, the `rotate*` idiom), `POST` (the bone's own space, the `localRotate*` idiom). Default: OVERRIDE for clips, PRE for drivers. |
| `when` | a condition; the item is skipped while it does not hold |
| `damping` | smoothing rate per bone written, `{"body": 0.5, "root": [null, 0.6, null]}`; a value source is allowed; unlisted bones keep their rate (a bit that never called `setSmoothness`) |
| `vectorModes` | for offset vectors: `SLIDE` (tween restarted when the target changes), `RETARGET` (exponential approach re-aimed every frame), `SNAP` |
| `snap` | the written bones jump to their target this frame (`orientInstant`, `finish`) |
| `mirror` | evaluate as the left-right mirror image while the layer's mirror condition holds (paired bones swapped, Y and Z rotations negated, the layer's `negate` inputs negated) |
| `swapSides` | like `mirror`, but only the paired bones swap; rotations and inputs are kept |

### Clips

```json
{"animationKey": "mobends:bends/animations/biped/walk_base.json", "time": {"variable": "limbSwing", "scale": 0.6662},
 "weight": {"variable": "limbSwingAmount"}, "bones": ["leftLeg", "rightLeg"], "duration": 6.283, "loop": true}
```

`time`: `"elapsed"` (default: ticks since the node started), a number (elapsed × speed), or a
variable with `scale` / `offset`. `weight` scales the rotation angles and offsets. `bones`
restricts the clip. Clip files (`animations/...json`) hold `bones` with keyframes (position,
rotation, scale), and optionally `duration`, `loop`, `interpolation: "STEP"` and explicit
keyframe `times`. Keyframes straddling the ±180° wrap interpolate the short way.

Bone names are the subject's named parts, plus `root` / `globalOffset` and `localOffset` for
the entity-level offset vectors (their keyframe positions are the vector).

### Drivers

| driver | fields |
|---|---|
| `core:axis_rotate` | `bone`, `axis` (`X`/`Y`/`Z`), `angle` (value source, degrees) |
| `core:vector` | `bone`, `x`, `y`, `z` (value sources; an axis left out keeps the bone's current target) |
| `core:offset` | `bone`, `x`, `y`, `z`: a bone's position offset |
| `core:ramp` | `name`, `speed`, `downSpeed` (null = speed, 0 = never down), `when` (its up/down switch), `initial`, `readBeforeAdvance`: a node variable moving 0..1 |
| `core:accumulate` | `name`, `rate` (value source per tick), `initial`, `min`, `max`: a node variable that integrates |
| `core:set` | `variable`, `value`, `scope` (`layer` / `node`): assigns every frame the item is evaluated |
| `mobends:cape` | the player's cape physics |
| `mobends:sword_trail` | `add`, `resetOnEnter`, `resetEachFrame`, `velocity`: feeds the sword trail |
| `mobends:spider_idle_legs`, `mobends:spider_moving_legs` | the spider's inverse-kinematics gaits (see the templates' fields) |

Drivers that compute something for later items publish it as a node variable
(`groundLevel`).

### Value sources

A number, or `{"variable": "headPitch", "scale": 0.5, "offset": -90, "min": -160, "max": 0}`.
The clamp applies after scale and offset unless `clampFirst` is set or an easing is used
(`ease`: `pow`, `ease_in`, `ease_out`, `ease_in_out`, with `power`), in which case the raw
variable is clamped and shaped first. An optional `fn` (`sin`, `cos`, `mcsin`, `mccos`, `abs`)
is applied to the result, then `mul` and `add`.

Variables resolve through the node scope, the layer scope, then the subject (see the data
classes' `registerVariable` calls: `limbSwing`, `headYaw`, `ticksAfterAttack`, ...).

### Conditions

`core:state` (`state`: a subject state such as `ON_GROUND`, `SPRINTING`, `LEFT_HANDED`),
`core:compare` (`variable`, `op` one of `< <= > >= == !=`, `value`), `core:decreased`
(`variable`: met on the frame it is lower than on the previous evaluation),
`core:ticks_passed` (`ticksToPass`, on the layer's clock), `core:action` (`tag` of any layer's
current node), `core:property` (`property`, `value` / `values`, or `unset`: string properties
such as `mainHandItem`, `useActionType`, `activeHandSide`), `core:equipment_name`
(`namePattern`, `slot`), `core:animation_finished`, and `core:and` / `core:or` / `core:not`.

## Semantics worth knowing

* Transitions are decided before posing; a node entered this frame poses this frame.
* Smoothing lives in the subject's bones. Damping in the animator is the `setSmoothness` the
  bits called; where they called none, leave it out and the bone keeps its rate.
* An offset vector written by two layers in one frame keeps only the last write; a bit that
  re-slid the same vector every frame on top of another writer is `RETARGET`.
* Mirroring is an involution applied around the item, so composition rules do not change.

# Model definitions: mobs without hand-written code

A mob that never had a Mo' Bends treatment is described in
`assets/mobends/bends/models/<mob>.json` and listed in `bends/models/index.json`. From the
definition the mod builds the data class (`DefinedEntityData`), the mutator (`DefinedMutator`)
and the renderer, and registers the entity; the animator asset does the rest.

```json
{
  "entity": "net.minecraft.entity.passive.EntityCow",
  "model": "net.minecraft.client.model.ModelQuadruped",
  "animator": "mobends:bends/animators/quadruped.json",
  "childScale": 0.5,
  "bones": [
    {"name": "head", "vanilla": {"field": "head", "index": 6}},
    {"name": "body", "vanilla": {"field": "body", "index": 7}, "restRotation": [90, 0, 0]},
    {"name": "leg1", "vanilla": {"field": "leg1", "index": 2},
     "split": {"axis": "Y", "at": [0.5], "names": ["foreLeg1"]}}
  ],
  "variables": [
    {"name": "flapWave", "field": ["wingRotation"], "prevField": ["oFlap"], "fn": "mcsin", "add": 1}
  ]
}
```

| field | meaning |
|---|---|
| `entity` | the entity class; `model` (optional) restricts mutation to that model class and its subclasses |
| `animator` | the animator asset; `key` / `unlocalizedName` override the registry's |
| `bones[].vanilla` | the vanilla part the bone takes over: `field` is the model's field by its development (MCP) name (see *Field names* below), `element` its index for array fields, `index` an optional fallback position in the model's box list (creation order). Every field or array slot that holds the part is replaced, found by identity. |
| `bones[].parent` | renders the bone inside another one (an invisible stand-in takes the vanilla slot); `position` is then relative to the parent |
| `bones[].position` | pivot override; default: the vanilla rotation point |
| `bones[].restRotation` | constant X, Y, Z degrees the vanilla model held the part at (`setRotationAngles` constants), applied before the animated rotation |
| `bones[].split` | cuts the part's boxes along `axis` at the `at` fractions; each cut adds a bone named in `names`, a child of the previous segment pivoting at the cut, with the matching strip of the texture. Knees, elbows, tail and tentacle joints. |
| `variables[]` | animator variables from numeric entity fields: `field` (candidate names, the first found is used), optional `prevField` for partial-tick interpolation, `scale`, `offset`, `fn`, `add`, or a `product` of other variables |

The shipped definitions (`cow`, `mooshroom`, `polar_bear`, `pig`, `creeper`, `chicken`,
`villager`, `witch`, `iron_golem`) give every leg a knee (and the golem's arms an elbow) and
share five generated animators (`quadruped`, `creeper`, `chicken`, `villager`, `iron_golem`:
stand / walk / jump with a smooth look, the golem's attack from its timer).
`DefinedModelsTest` in the lab checks every listed definition builds, covers what its animator
drives, and walks.

### Field names

Fields are always named as in the development environment (MCP names), for vanilla and modded
classes alike; `DefinedFields` walks the class hierarchy from the entity (or model) class up and,
for each class:

* **vanilla classes** are looked up in the generated accessors `core/vanilla/VanillaModelParts`
  (every `ModelRenderer` / `ModelRenderer[]` field of every vanilla model) and
  `core/vanilla/VanillaEntityFields` (every numeric field of `Entity`, `EntityLivingBase` and the
  living entities). They read the fields directly, so reobfuscation renames them for production;
  no SRG name appears in a definition. Regenerate them with `gradle generateVanillaFields`
  (`src/tools/kotlin/.../GenVanillaFields.kt`, which also writes the generated section of the access
  transformer that makes the non-public ones readable) after changing the Minecraft, Forge or
  mappings version; `checkVanillaFields`, run before `compileJava`, fails the build until you do.
* **anything else** (a mod's own entity or model, or its fields on a subclass) is found by
  reflection under the same name, since mods are not obfuscated.

A vanilla field the tables lack falls back to reflection too, which works in development only.

Layers that keep their own copy of a model
(the sheep's wool, a charged creeper's armour) still animate vanilla-style, so the sheep is not
listed yet.

# Entity types and selectors

Decided on 2026-09-25 and implemented in `core/types`. Work on selectors, the entity registry,
mutation or the related GUI has to follow this section; change it here first if the design
changes.

## Type files

A *type* says which model and animator an entity gets while a condition holds for it. Types live
in `assets/<namespace>/bends/types/**.json` of any mod or resource pack. Mo' Bends looks for them
in every pack (mods first, then resource packs from the bottom of the list up, then folders on
the classpath no pack covered, which is how a development environment's resources are found).
Within one pack, files are read in path order. A mod or a pack animates a mob with JSON only; no
Java addon is needed unless it brings its own conditions or drivers.

```json
{
  "id": "yourpack:notch_zombie_arms",
  "selector": {"type": "core:and", "conditions": [
    {"type": "core:entity_type", "entityType": "minecraft:player"},
    {"type": "core:player_name", "name": "Notch"}
  ]},
  "animator": "yourpack:bends/animators/zombie_arms.json"
}
```

| field | meaning |
|---|---|
| `id` | identifies the type; ranks are stored by it. Two types with the same id: a warning, and the first one found is used |
| `selector` | a condition on the entity; absent means the type applies to every entity |
| `model` | optional: a bender key (`mobends-player`, `mobends-minecraft:zombie`), a model definition (`yourmod:bends/models/beast.json`), or `vanilla` (the entity stays vanilla). Absent: the model the entity has by default |
| `animator` | optional: the animator asset. Absent: the model's own animator |

A type applies to an entity only if its model fits the entity's class. A type without a `model`
applies only to entities that have a default model.

Every bender an addon registers (the player, the zombie, the model definitions listed in
`bends/models/index.json`, ...) also gets a **built-in type**. Its id is the bender key, and its
one condition is "this is the entity's default model": the addon bender for the entity's exact
class, or else the first registered for a superclass.

## Selector conditions

Selector conditions have their own registry (`SelectorConditionRegistry`). They are evaluated
against the entity before any entity data exists; they are not KUMO trigger conditions. Built in:

| condition | fields |
|---|---|
| `core:and`, `core:or` | `conditions` |
| `core:not` | `condition` |
| `core:entity_type` | `entityType` or `entityTypes`: registry ids; `minecraft:player` stands for players, which have none |
| `core:player_name` | `name` or `names`: the profile name, ignoring case (never the display name) |
| `core:player_uuid` | `uuid` or `uuids`: stays the same when the player renames |
| `mobends:skin_variant` | `variant`: `default` or `slim` |

Addons add conditions with `AddonAnimationRegistry.registerSelectorCondition`, which prefixes
their key with the mod id.

A condition says whether its answer can change during an entity's life (`isStable`). A player's
name can't. A skin variant can: it reads as `default` until the skin downloads. The types whose
selector holds are cached per entity (weakly, so unloaded entities aren't kept alive); types with
an unstable selector are asked again every frame, and an entity can change type mid-life. Its
data is then made anew by the new type's factory.

## Precedence

When the selectors of several types hold for an entity, exactly one type applies. Candidates are
sorted by these keys, most significant first (`TypeOrder`):

1. **rank**, higher first. Every type starts at rank 0; only the user sets ranks.
2. **the number of conditions** of the selector, higher first. Every condition other than
   `core:and` / `core:or` / `core:not` counts, whatever it means: a broad assumption that more
   conditions make a more specific type, knowingly allowing false positives. A type without a
   selector counts 0; built-in types count 1.
3. **id**, plain lexical order, earlier first; the final tie breaker, so the result never depends
   on load order.

Types are not deduplicated: two types with different ids are two entries, even if they do the
same thing.

## User control

* **Ranks.** Settings shows an *Order* button next to every entity with more than one type. It
  lists the types in precedence order, and the user moves them up and down like resource packs.
  Each move ranks the whole list (top = highest) and stores the ranks in the client config
  (`TypeRanks`, by type id); *Reset* puts them back to 0.
* **On/off.** The existing per-bender switch (`Animated`) decides whether the chosen type animates
  at all. When the winning type's bender is off, the entity is rendered vanilla.

## Rendering: swap per render

Renderers are shared: every player is drawn by one of two `RenderPlayer`s, and every entity of a
kind by one renderer. So a renderer is never mutated for good:

* `RendererState` captures a renderer's vanilla state (the model's part fields, the elements of
  its part arrays and lists, the layers) before its first mutation, and each bender's mutated
  state right after mutating;
* `RenderLivingEvent.Pre` (lowest priority, after anything that could cancel the render) puts the
  chosen bender's state in place, or vanilla; `Post` always puts vanilla back;
* the first-person hand puts the local player's mutation in place for the hand, and the next
  render of that renderer sets its own state.

Mutators only build (`mutate`). Nothing demutates them; refreshing drops the mutators and
restores vanilla. Rebuilding parts per render is not an option: players of different types share
a renderer in the same frame.
