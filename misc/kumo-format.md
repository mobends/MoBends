# KUMO animator format, version 2

An animator is a JSON asset (`assets/mobends/bends/animators/<entity>.json`) that says how an
entity's bones get their targets every frame. The entity data (the model's bones with their
per-bone smoothing) is the *subject*; the animator only ever writes targets, the subject
smooths them.

```json
{
  "formatVersion": 2,
  "extends": "mobends:bends/animators/biped.json",
  "expressions": { ... },
  "layers": [ ... ]
}
```

`formatVersion` is required, in animators and in every other Mo' Bends file (types, extensions,
model definitions); each format is numbered on its own, and all of
them are at 2. A file written for another version of its format is refused with a message saying
so (an older one would be upgraded on load, once there is one to upgrade from). `extends` puts a parent animator's layers first; the parent's version is checked too.
`expressions` declares named expressions, numbers and conditions (see *Expressions*); layers,
machines and nodes can declare their own too.

## Layers

Layers evaluate in order into one pose. Each is a state machine of nodes: its current node's pose
stack produces the layer's pose, which composites onto the result. How the layer picks its current
node is in *Choosing the node*.

| field | meaning |
|---|---|
| `mode` | `OVERRIDE` (default: what the layer writes replaces) or `ADDITIVE` |
| `additiveSpace` | for additive layers: `"PRE"` / `"POST"`, or `{"default": "PRE", "body": "POST"}` |
| `when` | a condition (a boolean expression); while it does not hold the layer writes nothing and its clocks pause |
| `variables` | layer variables and their initial values, e.g. `{"combo": 0}` |
| `expressions` | named expressions visible inside the layer (see *Expressions*) |
| `damping` | default damping for the bones the layer writes (nodes and items override) |
| `mask` | `{"mode": "INCLUDE_ONLY", "includedParts": ["mouth"]}` (or `EXCLUDE_ONLY`): the bones the layer may write |
| `mirror` | `{"when": <condition>, "pairs": [["leftArm","rightArm"], ...]}`: the rule items with `"mirror"` / `"swapSides"` follow (see *Mirroring*) |
| `nodes`, `machines` | the layer's nodes and machines, as maps by name |
| `select`, `connections`, `defaultOnEntry` | how the layer picks its node (see *Choosing the node*) |

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
  "connections": [ {"target": "jump", "triggerCondition": "bounced"} ]
}
```

* `type` is `core:pose` (the default), `core:fallthrough` or `core:vanilla`.
* `tags` are the layer's *actions* (`core:action` sees them, in every layer).
* `expressions` declares named expressions visible to the node's items and to the conditions of
  its connections (see *Expressions*).
* `set` assigns layer variables when the node is entered.
* `connections` are the node's own ways out (see *Choosing the node*).
* Operations with a memory (`decreased`, `rose`, `fell`) start over when what they are written on
  is entered: a node for its connections, everything its items compute (their `when`s and their
  own fields, ramps' `when`s included) and the layer's mirror rule; a machine for its selector and
  its connections, before its selector chooses. A layer's own `when` starts with the layer, and
  its selector and connections as a machine's: the layer is entered when it starts (see
  *Machines*).
* A `core:fallthrough` node poses nothing, so the layers below show through; it has tags,
  connections, `set` and `expressions` like any node. A transition into or out of it fades between
  the layer's pose and the one below: what one side poses and the other doesn't is blended
  against the layers below (a full rotation, offset or vector as they have it; a PRE / POST
  rotation or an additive offset as nothing). It is how an extension lets the animation it
  extends show until it has something to add, but any layer can use it.
* A `core:vanilla` node hands the entity back to Minecraft: while any layer is in one (and that
  layer's `when` holds), the entity is drawn with its vanilla model and vanilla animation, and a
  player's first-person hand is vanilla too. The animator keeps running underneath, so its
  layer still decides its node every frame and leaving the node brings the animated model back
  where it would have been. The switch is immediate: the two models can't be blended. It poses nothing
  and has tags, connections, `set` and `expressions` like any node. It is meant for extensions
  that bring back animations made for the vanilla model (another mod's, say) while a condition
  holds:

  ```json
  "select": [{"when": {"core:action": ["..."]}, "then": "theirs"}, {"then": "animated"}],
  "nodes": {
    "animated": {"type": "core:fallthrough"},
    "theirs": {"type": "core:vanilla"}
  }
  ```

  The example pack `misc/examples/vanilla-swim-extension` makes players vanilla while they are in
  water: one layer with `"when": IN_WATER` and a single `core:vanilla` node.

  Rendering: the render puts the mutated model in place and animates as usual, then, if the
  animator asks for vanilla, puts the vanilla renderer state back before the model is drawn (the
  same swap as between entities, see *Rendering: swap per render*).

## Choosing the node

Most of what a layer does is pick one node out of many by the entity's state, in an order of
priority: asleep beats riding beats swimming beats jumping beats walking. A layer says so with a
**selector**, a decision tree of conditions. What depends on where the layer is coming from (a
combo's next move, a jump starting over, a clip that has to finish first) goes in
**connections**. Nodes that belong together (the sword's moves) are grouped in a **machine**, which
has a selector and connections of its own.

```json
{
  "expressions": {
    "jumping": {"or": ["AIRBORNE", {"lt": ["ticksAfterTouchdown", 1]}]}
  },
  "select": [
    {"when": "SLEEPING", "then": "sleeping"},
    {"when": "jumping", "then": [
      {"when": "FLYING", "then": "flying"},
      {"then": "jump"}
    ]},
    {"when": "STANDING_STILL", "then": "stand"},
    {"then": "walk", "transitionDuration": 4}
  ],
  "nodes": {"stand": {...}, "walk": {...}, "jump": {...}, "flying": {...}, "sleeping": {...}}
}
```

### Selectors

`select` is an ordered list of branches. A branch has a `when` (a condition; left out, it always
holds) and a `then`: the name of a node or a machine, or a list of branches of its own. The first
branch whose `when` holds is taken; a list in its `then` is chosen from the same way. Once a
branch is taken the choice is made inside it: if nothing in its list holds, the selector chooses
nothing, rather than going on to the branches after it. So a branch never has to repeat the
conditions of the branches before it, and those after it only apply when the ones before don't.

* Where the selector leads to what the layer is already in (the node, or the machine it is in),
  the layer stays. Where it leads somewhere else, the layer goes there. Where it chooses nothing,
  the layer stays too: leave out the last `then` to let a node hold on until one of the branches
  applies (the iron golem walks from 0.02 blocks a tick and stops under 0.01; in between it keeps
  doing what it was doing).
* A branch can carry `transitionDuration`, `transitionEasing` and `set`, as a connection does. In
  a nested list, the branches inside take what their enclosing branches set unless they set it
  themselves; `set`s add up, the inner ones last.
* A selector only names the members of its own machine (for a layer: its nodes and machines,
  not those inside its machines).

### Machines

`machines` is a map of machines by name, next to `nodes`. A machine has `nodes` and `machines` of
its own, and its own `select`, `connections`, `defaultOnEntry`, `expressions` and `conditions`. A layer
is a machine too, with the extra fields in the table under *Layers*. Every node and machine of a
layer has a name of its own, whatever machine it is in.

* The layer is in one node at a time, and so in every machine around it. Each of them checks its
  selector, from the layer inwards: an outer selector decides between the machines, an inner one
  between what is inside one.
* A layer or machine is entered the same way whether the layer is starting or a branch or a
  connection leads into it: the conditions of its selector and connections start over, then its
  selector chooses where it goes. Where the selector chooses nothing, its `defaultOnEntry` does: one
  of its own nodes or machines, by default the first node it declares (its first machine if it
  has no nodes). A machine it goes to is entered the same way, down to a node.
* A layer is entered when it starts, on the animator's first frame (whether or not its `when`
  holds). That is the frame's decision, and there is nothing to crossfade from: a branch's
  `transitionDuration` and `transitionEasing` don't apply there, its `set` does. The node's
  connections, and the selectors again, are checked from the next frame on. So `defaultOnEntry`
  only matters where the selectors choose nothing; to open on a node the selector wouldn't choose
  (an intro), give it a branch of its own, first, and connections out of it.

### Connections

Connections are the ways out that depend on where the layer is. A node's `connections` lead out of
that node, a machine's lead out of any node inside it, and a layer's out of any of its nodes.

```json
{"target": "slash_down", "triggerCondition": "attacked", "transitionDuration": 0,
 "transitionEasing": "EASE_IN_OUT", "set": {"combo": 2}}
```

* `target` is any node or machine of the layer: a machine is entered as above. A connection to
  where the layer already is starts that node over (a jump bouncing into another jump).
* `set` assigns layer variables when the connection fires. `transitionDuration` (ticks)
  crossfades (an interrupted crossfade continues from what was on screen); easings `LINEAR`,
  `EASE_IN`, `EASE_OUT`, `EASE_IN_OUT` (the default), `EXPONENTIAL`. Where one side of a crossfade
  poses a bone absolutely and the other only relatively (PRE / POST, additive), the relative one
  is first resolved against the layers below, so both are blended as absolute values.

### Every frame

Every frame but the one the layer starts on, before the layer is posed, so a change shows on the
frame it happens:

1. The selectors of the layer and of every machine the node is in are checked, from the layer
   inwards. The first that leads somewhere else than where the layer is wins.
2. If none did, the connections are checked: the node's, then those of the machines around it,
   from the innermost out to the layer's. The first one met fires.

Every condition of those selectors and connections is evaluated every frame, whatever is chosen
(edge triggers such as `decreased` stay fresh). When a branch or a connection leads into a
machine, the transition (its duration, easing and `set`) is that of the branch or the connection;
the branches the selectors of the machines entered take on the way in add their `set`s, outermost
first, and the node entered applies its own `set` last. Each selector sees the `set`s before it.

## Pose items

A node's `pose` is a stack; later items compose over earlier ones. Common fields:

| field | meaning |
|---|---|
| `space` | `OVERRIDE` (replace), `PRE` (rotate in the parent's space, the `rotate*` idiom), `POST` (the bone's own space, the `localRotate*` idiom). Default: OVERRIDE for clips, PRE for drivers. |
| `when` | a condition; the item is skipped while it does not hold |
| `damping` | smoothing rate per bone written, `{"body": 0.5, "root": [null, 0.6, null]}`; an expression is allowed (a name or an operation, evaluated every frame); unlisted bones keep their rate |
| `vectorModes` | for offset vectors: `SLIDE` (tween restarted when the target changes), `RETARGET` (exponential approach re-aimed every frame), `SNAP` |
| `snap` | the written bones jump to their target this frame (`orientInstant`, `finish`) |
| `mirror` | evaluate as the left-right mirror image while the layer's mirror condition holds (paired bones swapped, Y and Z rotations and X offsets negated; see *Mirroring*) |
| `swapSides` | like `mirror`, but only the paired bones swap; rotations and offsets are kept |

### Mirroring

A layer's `mirror` rule says when its mirrored items mirror, and which bones pair up:

```json
"mirror": {
  "when": "LEFT_HANDED",
  "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"],
            ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"]]
}
```

While the rule's `when` holds, an item with `mirror` or `swapSides` runs on a reflection of the
pose built so far, and its result is reflected back. Reflecting twice gives back the original, so
the item's composition rules don't change. An item that sets either without a rule on its layer
is a load error.

| | paired bones swap | Y and Z rotations, X offset negated |
|---|---|---|
| `mirror` | yes | yes: the item's left-right mirror image |
| `swapSides` | yes | no: the same motion, moved to the other side |

**Values are never negated**: every variable and expression reads the same, mirrored or not.
What an item does mirrored follows from how it is marked:

- **`mirror`** for motion that belongs to a side: swings, clips, a head turned toward the weapon
  (`head`, Y, `-30` becomes `+30`).
- **Neither** for an item on an unpaired bone that follows a world direction. The head turned by
  `headYaw` must look where the entity looks, so it isn't mirrored.
- **`swapSides`** for an item on a paired bone that follows a world direction: the right arm
  turned by `headYaw` becomes the left arm turned by the same angle, still pointing where the
  entity looks.
- An item that mixes the two is split into two items.

(Earlier versions negated a rule's `negate` list of variables inside mirrored items; a rule
with `negate` is now a load error.)

### Clips

```json
{"animationKey": "mobends:bends/animations/biped/walk_base.json",
 "frame": {"mod": [{"mul": ["limbSwing", 0.6662]}, "clipLength"]},
 "weight": "limbSwingAmount", "bones": ["leftLeg", "rightLeg"]}
```

* `frame` (an expression) is where in the clip it is, in the clip's own units: from 0 to
  `clipLength` (the clip file's `duration`, or its keyframe count − 1 if it has none). A frame
  before 0 or past `clipLength` holds the first or last keyframe; a clip loops by wrapping its
  frame with `mod`, as above.
* `duration` (optional, in ticks) is how long the item runs, whatever its frame does: the clip
  is finished (`nodeIsFinished`) once `elapsed` reaches it. Without one it never
  finishes.
* The default `frame` is `{"mul": [{"div": ["elapsed", "duration"]}, "clipLength"]}` with a
  `duration` (the clip is fitted to it), and `"elapsed"` without one (a unit per tick).
* Inside `frame`, `clipLength` and (when the item has one) `duration` are names like any other.
  `{"mod": ["elapsed", "clipLength"]}` with `"duration": 30` loops for 30 ticks, then finishes.

`weight` (an expression) is how much of the clip is applied. For a bone the clip writes relatively
(`PRE` / `POST`) it scales the rotation angle and the offset; for a bone it replaces (`OVERRIDE`)
it blends from what the bone has so far this frame (from the layers below and the items before)
to the clip, or from the rest pose where nothing has written the bone yet. `bones` restricts the
clip.
Clip files (`animations/...json`) hold `bones` with keyframes (`position`, `rotation` as a
quaternion `[x, y, z, w]`; each may be left out for no offset, no rotation),
and optionally `duration`, `interpolation: "STEP"` and explicit keyframe `times` (in the clip's
units). Keyframes straddling the ±180° wrap interpolate the short way.

Bone names are the subject's named parts, plus `root` / `globalOffset` and `localOffset` for
the entity-level offset vectors (their keyframe positions are the vector).

### Drivers

| driver | fields |
|---|---|
| `core:axis_rotate` | `bone`, `axis` (`X`/`Y`/`Z`), `angle` (expression, degrees) |
| `core:vector` | `bone`, `x`, `y`, `z` (expressions; an axis left out keeps the bone's current target) |
| `core:offset` | `bone`, `x`, `y`, `z`: a bone's position offset |
| `core:ramp` | `name`, `speed`, `downSpeed` (null = speed, 0 = never down), `when` (its up/down switch), `initial`, `readBeforeAdvance`: a node variable moving 0..1 |
| `core:accumulate` | `name`, `rate` (expression, per tick), `initial`, `min`, `max`: a node variable that integrates |
| `core:set` | `variable`, `value` (expression), `scope` (`layer` / `node`): assigns every frame the item is evaluated |
| `core:spring` | `name`, `target` (expression), `stiffness` (per tick²), `friction` (per tick), `initial`: a node variable pulled towards `target` like a mass on a spring, so it lags, overshoots and settles (follow-through) |
| `core:step_turn` | the body stands, turns and walks on feet planted in the world (see *Turning on the feet*) |
| `mobends:cape` | the player's cape physics |
| `mobends:sword_trail` | `add`, `resetOnEnter`, `resetEachFrame`, `velocity`: feeds the sword trail |
| `mobends:spider_idle_legs`, `mobends:spider_moving_legs` | the spider's inverse-kinematics gaits (see the templates' fields) |

Drivers that compute something for later items publish it as a node variable
(`groundLevel`).

### Turning on the feet

`core:step_turn` takes over which way the body faces. Vanilla turns a standing mob's body
smoothly towards its head; the driver instead keeps the yaw the body is *shown* at, turns the
model back from vanilla's yaw to it (`rotationBone`, `renderRotation` by default, put before
whatever lies beneath), and plants the feet in the world. When vanilla's body yaw gets
`turnThreshold` degrees ahead of a foot, that foot steps (at most `maxStepAngle` past the shown
body, re-aimed while it rises), the leg on the side of the turn first; the body turns towards the
mean of where its feet point, like a mass on a spring (`bodyStiffness`, `bodyDamping`), so it
follows once the foot is down; the next foot lifts once the body has settled to within
`settleAngle`. For `finishWindow` ticks after a landing a foot already steps at `finishThreshold`
degrees, so a turn ends squared up. A foot pushed `driftThreshold` model units from under the
body steps back (`finishDrift` within the finish window, so a walk ends with the feet together);
past `resetDistance` every foot is placed anew.

It walks the same way. The foot left behind by the moving body steps to where it will be under the
body once it has landed and stood half its time, judged by the entity's velocity (measured from
its position, smoothed over `velocitySmoothing` ticks), so it lands as far ahead as it lifts
behind; walking, the feet don't wait for the body to settle. The faster it goes, the shorter the
steps (down to `minStepDuration`) and the pauses after them, so a foot travels at most
`strideLength` model units a step, and a step in the air hurries when a planted foot is about to
be left out of its leg's reach (setting off, say). The hips go down a further `walkCrouch` while
walking, for reach.

Going `runSpeed` blocks a tick or faster (easing in from 80% of it; 0, the default, never), it
runs: the body turns the way it goes (not vanilla's yaw) instead of waiting for its feet, on a
stiffer spring (`runBodyStiffness`, `runBodyDamping`), the feet catching up with the next steps;
the steps lengthen to `runStrideLength` with no pause between them and lift `runStepHeight`. Only
one foot is up at a time, so there is no moment with both off the ground. The speed is smoothed
apart from the direction, so a mob swinging round keeps running, and the way it goes is smoothed
over `runFacingSmoothing` ticks, so a path zigzagging between blocks doesn't swing the body about.
A hurried step never takes less than 60% of `minStepDuration`: faster than the legs can keep up
with, the planted foot drags rather than the feet flickering.

```json
{"driver": "core:step_turn", "weight": "standing",
 "legs": [{"upper": "leftLeg", "lower": "leftForeLeg", "hip": [-4, 11, 0], "knee": [0, 5, -3], "foot": [-0.5, 8, 2.5]},
          {"upper": "rightLeg", "lower": "rightForeLeg", "hip": [5, 11, 0], "knee": [0, 5, -3], "foot": [-0.5, 8, 2.5]}]}
```

* Each leg is two segments: `hip` is the upper bone's pivot in the model (model units, +Y down,
  -Z forward), `knee` the lower bone's pivot relative to it and `foot` the sole relative to the
  knee, all at rest (a split's joint sits on its cut, at the hinge). `bend` is 1 for a joint that
  bends the foot back (a knee), -1 for one that bends forward; it never bends past its rest pose
  the other way. The IK turns the leg to where its foot points, then swings it onto the foot, and
  the bones snap to it at full weight. It assumes the legs have no `restRotation`.
* The hips are lowered by `crouch`, dip by `impactDepth` a moment after a foot lands and shift
  `weightShift` over the planted feet while one is up, through `offsetBone` (`localOffset`); the
  whole model rolls `weightRoll` degrees over them too, about the entity's origin on the ground
  (through the rotation bone). The legs keep the feet on the ground. The offset is written turned
  back by whatever lies beneath in the rotation bone, so a counter-rotation there (of vanilla
  rocking the model) doesn't carry the hips off.
* `weight` (an expression, 0..1) blends the driver in. At 0 it writes nothing, and the feet are
  placed anew under the body when it goes up again. Run it in its own layer with a ramp that goes
  up while the mob is on the ground, so the jump shows beneath it and the body turns back to
  vanilla's yaw while it plays.
* It reads the variables `yawVariable` (`bodyYaw`), `xVariable` and `zVariable` (`worldX`,
  `worldZ`), and publishes `turnLag` (degrees vanilla's body yaw is ahead of the shown one: what a
  head posed by `headYaw` has to add), `turnSpeed` (degrees per tick the shown body turns),
  `stepLift` (0..1 as the stepping foot rises, negative for a foot on the -X side),
  `stepImpact` (peaking at 1 `impactTime` ticks after a landing; quick landings add up smoothly
  rather than starting it over) and `stride` (model units the
  feet on the +X side are ahead of those on the -X side, halved: what an arm swing follows), all
  scaled by the weight, for the items after it to make the upper body react. `iron_golem.json`
  does it with springs, and turns back vanilla's rocking of a walking golem
  (`RenderIronGolem.applyRotations`) in the render rotation beneath the driver.

### Expressions

Every value an animator computes is an expression: every number an item computes (an angle, a
weight, a vector axis, a damping rate, ...) and every condition (a layer's or an item's `when`, a
branch's `when`, a connection's `triggerCondition`, a mirror rule's `when`). An expression is a
JSON tree, so tools can read and write it without a parser.

| form | meaning |
|---|---|
| a number | a constant: `45` |
| `true`, `false` | a constant condition |
| a string | a name: the innermost named expression called that, a built-in (`elapsed`, `nodeIsFinished`), a state of the subject if written in capitals (`"ON_GROUND"`), or else a variable (`"headYaw"`) |
| an object with one key | an operation; the key is its name, the value the list of its arguments (always a list): `{"sin": ["t"]}` |

**Every expression is a number or a boolean**, and which one is checked when the animator loads:
a number where a condition goes (`"when": "limbSwing"`), or a boolean where a number goes
(`{"add": ["ON_GROUND", 1]}`), is an error. Arithmetic is in single precision (`float`); strings
are never values, only arguments some operations take written out (an item id, a hand).

Operations nest freely:

```json
"angle": {"add": [
  {"mul": [{"sin": [{"mul": ["ticks", 0.1]}]}, 6]},
  {"mul": [{"sin": [{"mul": ["ticks", 0.37]}]}, 2]},
  -85
]},
"when": {"and": ["ON_GROUND", {"not": ["SNEAKING"]}, {"lt": ["ticksAfterAttack", 10]}]}
```

| operation | arguments | value |
|---|---|---|
| `add`, `sub`, `mul`, `div` | two or more numbers, folded left to right: `{"sub": [a, b, c]}` is (a − b) − c | number |
| `min`, `max` | two or more numbers | number |
| `mod`, `pow`, `atan2` | two numbers: `{"atan2": [y, x]}`; `mod` is floored, taking the divisor's sign (`{"mod": [-1, 20]}` is 19) | number |
| `neg`, `abs`, `sqrt`, `floor`, `ceil` | one number | number |
| `sin`, `cos` | one number, in radians; `mcsin`, `mccos` use Minecraft's sine table, like vanilla models | number |
| `clamp` | `[value, min, max]` | number |
| `lerp` | `[from, to, t]` | number |
| `easeIn`, `easeOut`, `easeInOut` | `[t, power]`: shapes a 0..1 value | number |
| `linstep` | `[x, edge0, edge1]`: 0 below `edge0`, 1 above `edge1`, linear between (with the edges equal, a step at them) | number |
| `smoothstep` | `[x, edge0, edge1]`: `linstep` eased (3t² − 2t³) | number |
| `wrapDegrees` | `[a]`: the angle in −180..180 | number |
| `lerpAngle` | `[from, to, t]` in degrees, the short way round (350 to 10 passes 360, not 180) | number |
| `lt`, `le`, `gt`, `ge` | two numbers: <, ≤, >, ≥ | boolean |
| `eq`, `ne` | two numbers or two booleans | boolean |
| `and`, `or` | one or more booleans | boolean |
| `not` | one boolean | boolean |
| `if` | `[condition, then, else]`, the two branches of one type | the branches' |
| `decreased` | one number: holds on the frame it is lower than on the previous evaluation | boolean |
| `rose`, `fell` | one boolean: holds on the frame it turns true / false | boolean |

**Nothing short-circuits**: `and`, `or` and `if` evaluate every argument, both branches of an
`if` included, so an edge trigger inside never misses a frame.

`decreased`, `rose` and `fell` remember their value from the previous evaluation. Each place one
is written keeps its own memory (two uses keep two memories), and it starts over, noting the
value as it is then, when the scope holding the place starts: a node for its items' and
connections' expressions (and its layer's mirror rule), a machine for its selector and
connections, a layer for its `when`. `{"decreased": ["ticksAfterAttack"]}` holds on a new attack.

**Registered operations** have a namespaced name, and take arguments of their own kinds:

| operation | arguments | holds while |
|---|---|---|
| `core:holds_item` | `[hand, item]`: `main_hand` or `off_hand`, an item id | the hand holds that item |
| `core:holds_any_item` | `[hand]` | the hand holds anything |
| `core:active_hand_side` | `[side]`: `left` or `right` | the hand on that side is using an item |
| `core:equipment_name` | `[slot, pattern]`: `mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`, and a regular expression | the display name of what a player has in the slot matches the pattern as a whole |
| `core:action` | `[tag]` | any layer's current node carries the tag |
| `mobends:use_action` | `[action]`: `food`, `bow` or `shield` | the item in use is used as that (Mo' Bends' classification, which the config can change) |
| `mobends:attack_action` | `[action]`: `fists`, `sword` or `tool` | the held item attacks as that |

Mistakes (an unknown operation, a wrong number or kind of arguments, an object with more than one
key, a number where a boolean goes) are reported when the animator loads, in the operation's own
words: `'core:holds_item' argument 1 (hand) must be one of main_hand, off_hand, got 'left_hand'`.

**Named expressions** are declared in an `expressions` object on the animator, a layer, a machine
or a node, and used by name like variables. They can be numbers or booleans:

```json
"expressions": {
  "sway": {"mul": [{"sin": [{"mul": ["ticks", 0.1]}]}, 6]},
  "reach": {"add": ["sway", -85]},
  "jumping": {"or": ["AIRBORNE", {"lt": ["ticksAfterTouchdown", 1]}]},
  "attacked": {"decreased": ["ticksAfterAttack"]},
  "sprintJump": {"and": ["jumping", "SPRINTING"]}
}
```

* A name is visible in the scope that declares it and in every scope inside it (animator →
  layer → machine → node); an inner declaration shadows an outer one, and also shadows a variable
  of the same name.
* A named expression is resolved where it is declared, not where it is used: `reach` above uses
  the `sway` of its own scope even if a node declares another `sway`.
* A named expression that remembers something (`attacked` above) is a copy for each place it is
  used, with its own memory.
* An animator that `extends` another sees the parent's named expressions and can shadow them for
  its own layers; the parent's layers keep using the parent's.
* Names in one scope can use each other in any order; a name that depends on itself is an error.
  Every declaration is checked when the animator loads, used or not.

Built-in names: `elapsed`, the ticks since the current node started, and `nodeIsFinished`, which
holds once every clip of the current node that has a `duration` has run it (a node with no items
always is, one whose items all run forever never is). A named expression can shadow either.

A name in capitals is a **state** of the subject, a boolean (see the data classes'
`registerState` calls: `ON_GROUND`, `SPRINTING`, `LEFT_HANDED`, ...). Any other name nothing
declares is a **variable**, a number: the node's own (written by its ramps, accumulators, springs,
`core:set` and drivers such as `core:step_turn`) once the node has written it, else the layer's
(its `variables`, `set`, `core:set`) once written, else the subject's (see the data classes'
`registerVariable` calls: `limbSwing`, `headYaw`, `ticksAfterAttack`, ...). Which of them a name
can be is worked out once, when the animator is bound to its entity on the first frame, never by
looking the name up while animating. A name that nothing in the animator writes and the subject
doesn't have fails the animator then (logged; the entity isn't animated), even if nothing ever
reads it.

A condition's expressions see the named expressions where it is written: a layer's `when` sees
the layer's; a selector's and a machine's connections see the machine's (the layer's for its
own); an item's, a node's connection and the layer's `mirror` rule see those of the node being
posed.

## Semantics worth knowing

* Transitions are decided before posing; a node entered this frame poses this frame.
* A layer's selectors only move it when they lead somewhere else: the layer stays in a node for
  as long as it is the selector's first match.
* Smoothing lives in the subject's bones. Damping in the animator sets a bone's rate; leave it
  out and the bone keeps the rate it has.
* An offset vector written by two layers in one frame keeps only the last write; a vector
  re-aimed every frame on top of another writer is `RETARGET`.
* A bone written only relatively (PRE / POST, additive) with nothing absolute beneath builds on
  last frame's result, so it keeps turning or moving every frame; to hold a pose, write it
  absolutely in some layer beneath.
* Mirroring is an involution applied around the item, so composition rules do not change.

# Model definitions: mobs without hand-written code

A mob that never had a Mo' Bends treatment is described in
`assets/<namespace>/bends/models/<mob>.json`, and a type file (see *Entity types*) gives it to the
mob. Mo' Bends' own are in `assets/mobends/bends/types/`:

```json
{
  "formatVersion": 2,
  "id": "mobends:cow",
  "selector": {"type": "core:entity_type", "entityType": "minecraft:cow"},
  "model": "mobends:bends/models/cow.json"
}
```

From the definition the mod builds the data class (`DefinedEntityData`), the mutator
(`DefinedMutator`) and the renderer, and registers the entity; the animator asset does the rest.

```json
{
  "formatVersion": 2,
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
| `formatVersion` | `2` (the index, `{"formatVersion": 2, "models": [...]}`, has one too) |
| `entity` | the entity class; `model` (optional) restricts mutation to that model class and its subclasses |
| `animator` | the animator asset; `key` / `unlocalizedName` override the registry's |
| `bones[].vanilla` | the vanilla part the bone takes over: `field` is the model's field by its development (MCP) name (see *Field names* below), `element` its index for array fields, `index` an optional fallback position in the model's box list (creation order). Every field or array slot that holds the part is replaced, found by identity. |
| `bones[].parent` | renders the bone inside another one (an invisible stand-in takes the vanilla slot); `position` is then relative to the parent |
| `bones[].position` | pivot override; default: the vanilla rotation point |
| `bones[].restRotation` | constant X, Y, Z degrees the vanilla model held the part at (`setRotationAngles` constants), applied before the animated rotation |
| `bones[].split` | cuts the part's boxes along `axis` at the `at` fractions; each cut adds a bone named in `names`, a child of the previous segment pivoting at the cut, with the matching strip of the texture. Knees, elbows, tail and tentacle joints. `hinge` puts the joints on an edge of the cut instead of its middle: `FRONT` / `BACK` (-Z / +Z) or `TOP` / `BOTTOM` (-Y / +Y). A joint hinges on the side opposite to where it bends (a knee at the front, an elbow at the back), so the segments stay joined when it bends. |
| `variables[]` | animator variables from numeric entity fields: `field` (candidate names, the first found is used), optional `prevField` for partial-tick interpolation, `scale`, `offset`, `fn`, `add`, or a `product` of other variables |

The shipped definitions (`cow`, `mooshroom`, `polar_bear`, `pig`, `creeper`, `chicken`,
`villager`, `witch`, `iron_golem`) give every leg a knee but the creeper's (and the golem's arms an
elbow) and share three generated animators (`quadruped`, `chicken`, `villager`: stand / walk /
jump with a smooth look, made by the lab's `tools/gen_animators.ts`). The golem's (`iron_golem`:
its attack from its timer, turning on its feet), the creeper's (`creeper`: leaning at the waist)
and the cow's (`cow`, extending `quadruped`) are edited by hand.
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
  "formatVersion": 2,
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
| `formatVersion` | `2` |
| `id` | identifies the type; ranks are stored by it. Two types with the same id: a warning, and the one from the higher-priority pack is used (the server's resource pack, then the enabled resource packs from the top of the list, then mods) |
| `selector` | a condition on the entity; absent means the type applies to every entity |
| `model` | optional: a bender key (`mobends:player`, `mobends:zombie`), a model definition (`yourmod:bends/models/beast.json`), or `vanilla` (the entity stays vanilla). Absent: the model the entity has by default |
| `animator` | optional: the animator asset. Absent: the model's own animator |

A type applies to an entity only if its model fits the entity's class. A type without a `model`
applies only to entities that have a default model.

Every bender an addon registers in code (the player, the zombie, ...) also gets a **built-in
type**. Its id is the bender key, a
resource location: the mod that registered the bender, and the name it gave it or else the
entity's id (`mobends:player`, `mobends:zombie`; an entity of another mod than Minecraft keeps its
namespace in the path, `mobends:othermod/beast`). The type's one condition is "this is the
entity's default model": the addon bender for the entity's exact class, or else the first
registered for a superclass. The bender a type makes from a model definition has the same kind of
key (`mobends:cow`), and Mo' Bends gives its own types the same id, so extensions name either
the same way.

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
2. **the number of conditions** of the selector, higher first: a broad assumption that more
   conditions make a more specific type, knowingly allowing false positives. Every condition
   other than `core:and` / `core:or` / `core:not` counts 1, whatever it means; `core:and` counts
   the sum of its conditions, `core:or` the fewest of any of its conditions (it only holds as
   narrowly as its broadest branch), and `core:not` 1. A type without a selector counts 0;
   built-in types count 1.
3. **id**, plain lexical order, earlier first; the final tie breaker, so the result never depends
   on load order.

Types are not deduplicated: two types with different ids are two entries, even if they do the
same thing.

## Servers: what resource packs may do

Types, extensions, animators and clips can come from anyone's resource pack, and a model drawn far
from its hitbox, or given invisible or giant geometry, is an advantage on a server. So a server
decides what resource packs may do, and sends it to every client with Mo' Bends when they join
(the `Server` category of the server's `config/mobends.cfg`):

| setting | default | meaning |
|---|---|---|
| `resourcePackAnimation` | `LIMITED` | `ALLOW`, `LIMITED` or `DENY` (below) |
| `maxPartOffset` | 4 | `LIMITED`: how far resource packs' animation may move one part from where the trusted animation puts it, in model units (1/16 block) |
| `maxBodyOffset` | 16 | `LIMITED`: the same for the whole model (`globalOffset` / `root`, `localOffset`) |

* **Trusted** content is what comes from Mo' Bends, another mod, vanilla, or the server's own
  resource pack. **Untrusted** is what a resource pack the player enabled supplies, including a
  resource pack's version of a trusted file (an animator, a clip or a model definition it
  replaces). `PackTrust` decides.
* **`ALLOW`**: no limits. Singleplayer always behaves like this, whatever its config says.
* **`LIMITED`**:
  * Every layer from an untrusted source is limited. A layer is untrusted when the file declaring
    it is (an animator's own layers; a parent's through `extends` are judged by the parent's
    file), or when any clip its nodes play is.
  * After the trusted layers, the pose is noted; after the untrusted ones, every part's offset and
    the whole model's offsets are brought back within `maxPartOffset` / `maxBodyOffset` of what
    the trusted layers gave them (their last value if they didn't write it this frame; the
    bone's value when the limits started otherwise). So an extension moves a swimming player's
    arm a little, not the swim; an untrusted animator on its own is held near the rest position.
  * While limited, a relative offset or vector with nothing under it builds on the trusted value,
    not the live one, which the untrusted layers have pushed (else the push would compound).
  * An axis an offset or vector doesn't write (NaN) never widens the limit; an infinite value goes
    back to the trusted one; a part's NaN offset axis and a broken (non-finite) rotation are
    dropped, so no part can be made to vanish.
  * Rotations are otherwise free. Scale needs no limit: animation can't scale parts.
  * Model definitions (geometry) come only from trusted sources: an untrusted type that names one
    is ignored as a whole (so it can't replace a trusted type with the same id, and its animator
    never runs on a model it wasn't made for), and a resource pack's version of a trusted
    definition is skipped for the trusted one.
* **`DENY`**: untrusted types and extensions are ignored, and for every animator, clip and model
  definition the trusted version is loaded (if there's none, it fails to load, with a warning).
* Changing server, or the server's answer arriving, reloads the types, extensions, animators and
  model definitions when the policy or the limits differ. A server without Mo' Bends never
  answers: its players get the defaults (`LIMITED`, 4, 16).

## User control

* **Ranks.** Settings shows an *Order* button next to every entity with more than one type. It
  lists the types in precedence order, and the user moves them up and down like resource packs.
  Each move ranks the whole list (top = highest) and stores the ranks in the client config
  (`TypeRanks`, by type id); *Reset* puts them back to 0.
* **Extension ranks.** Settings shows an *Extensions* button next to every entity whose types
  have more than one extension. It lists them in order (top = on top); moving one ranks the
  whole list and stores the ranks in the client config (`ExtensionRanks`, by extension id);
  *Reset* puts them back to 0.
* **On/off.** The existing per-bender switch (`Animated`) decides whether the chosen type animates
  at all. When the winning type's bender is off, the entity is rendered vanilla.

## Extensions

An extension adds layers on top of the animator of one type, instead of replacing it: a pack can
change part of how an entity moves (the arms while it stands still, a tail) and combine with its
own animation and with other packs' extensions. Extension files are found like type files, in
`assets/<namespace>/bends/extensions/**.json` of any mod or resource pack:

```json
{
  "formatVersion": 2,
  "id": "mobends_wave:wave",
  "type": "mobends:player",
  "animator": "mobends_wave:bends/animators/wave.json"
}
```

| field | meaning |
|---|---|
| `formatVersion` | `2` |
| `id` | identifies the extension (ranks are stored by it); two with one id: the one from the higher-priority pack wins, with a warning (as for types) |
| `type` | the id of the type it extends: a type file's `id`, or a built-in type's, which is its model's key (`mobends:player`, `mobends:zombie`, ...) |
| `animator` | an animator whose layers go on top of the type's animator (or, for a type without one, its model's) |

* The extension's layers come after every layer of the animator it extends (including that
  animator's parents), so they pose over it; their `core:fallthrough` nodes let it show through.
* Extensions of one type are ordered like types (`TypeOrder`): by rank, higher first, then by
  id; the first one goes on top, so its layers are added last. Every extension starts at rank 0;
  only the user sets ranks (see *User control*).
* An extension's animator is an animator of its own: it can `extends` another, and it sees its own
  named expressions, not those of the animator it extends. Its conditions see every layer, so
  `core:action` can follow the extended animator's nodes by their tags.
* An extension targets a type, not an entity: it applies wherever that type is chosen, and not
  when another type wins (see *Precedence*). A type with another model or animator needs its own
  extensions.
* An extension of a model that has no animator asset (an addon's own Java animation) is ignored,
  with a warning.

The example pack `misc/examples/wave-extension` makes players wave while they stand still;
`misc/examples/dance-extension` makes cows and chickens dance to a beat, with one animator shared
by two extensions, and moves the whole body (`globalOffset`, `renderRotation`), which the mobs' own
animators leave alone, so it returns them to rest itself instead of falling through.

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
