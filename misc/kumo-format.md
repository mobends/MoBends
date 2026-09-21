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
