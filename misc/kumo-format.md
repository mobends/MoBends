# KUMO animator format, version 2

An animator is a JSON asset (`assets/mobends/bends/animators/<entity>.json`) that says how an
entity's bones get their targets every frame. The entity data (the model's bones with their
per-bone smoothing) is the *subject*; the animator only ever writes targets, the subject
smooths them.

```json
{
  "formatVersion": 2,
  "extends": "mobends:bends/animators/biped.json",
  "@define": { ... },
  "@on": { ... },
  "layers": [ ... ]
}
```

`formatVersion` is required, in animators and in every other Mo' Bends file (types, extensions,
model definitions); each format is numbered on its own, and all of
them are at 2. A file written for another version of its format is refused with a message saying
so (an older one would be upgraded on load, once there is one to upgrade from). `extends` puts a parent animator's layers first; the parent's version is checked too.
`@define` declares the animator's definitions and `@on` its statement lists (see *Definitions and
statements*); layers, machines and nodes have their own too.

## How it is written

Pose items and nodes are written the same way: an object with **exactly one key that doesn't
start with `@`**. That key names what it is (`core:clip`, `core:axis_rotate`, `core:pose`), and
its value is what that takes:

```json
{"@when": "entityIsSwinging", "core:axis_rotate": {"bone": "head", "axis": "y", "angle": -30}, "@space": "pre"}
```

Every other key starts with `@`: what the engine does with it (an item's `@when`, `@space`,
`@damping`, ...), or what a scope has whatever else it is (`@define`, `@functions`, `@on`, `@connections`). These
are a closed set, so an addon's driver or node type can have any field of its own without ever
colliding with them. Structured objects (layers, machines, selector branches, connections, the
mirror rule) keep fixed keys; their own conditions are `@when` on layers and the mirror rule, and
`when` on branches and connections, which are `{"when": ..., "then": ...}`.

How the three kinds of construct compare:

| | operation | statement | driver |
|---|---|---|---|
| produces | a value | a change to state | a contribution to the pose (and side effects) |
| written | `{"name": [args]}`, positional | `{"set": ["layer.x", <value>]}` | `{"name": {named fields}}` |
| appears in | any expression | a scope's `@on` lists, a transition's `do` | a node's `pose` (and `enterPose`) |
| order | doesn't matter: expressions change nothing | list order | pose-stack order |
| state | an edge trigger's memory, per place | the state it names | its own, and the states its `inout` / `out` name |

**The format's words are lower case**: every fixed set of values (`@space` `pre`, an `axis` `x`,
a layer `mode` `additive`, a `transitionEasing` `ease_in_out`, a hinge `front`) is written in
lower_snake_case, as operation names and their choices are; `"PRE"` is an error. A map from bones
to values (`@damping`, `additiveSpace`) names the value for every other bone `@default`, so any
bone name can be a key.

**A key an object doesn't take is an error** when the animator loads, in every object of the
file: a misspelt key never passes silently. The one key every object takes is **`@comment`**, a
string the engine ignores. Put `@when` first when there is one, so a reader knows something
applies conditionally before reading what (JSON objects are unordered; this is a convention, not
something the loader checks).

## Layers

Layers evaluate in order into one pose. Each is a state machine of nodes: its current node's pose
stack produces the layer's pose, which composites onto the result. How the layer picks its current
node is in *Choosing the node*.

| key | meaning |
|---|---|
| `mode` | `override` (default: what the layer writes replaces) or `additive` |
| `additiveSpace` | for additive layers: `"pre"` / `"post"`, or `{"@default": "pre", "body": "post"}` |
| `@when` | a condition (a boolean expression); while it does not hold the layer writes nothing and its clocks pause |
| `@define`, `@on` | the layer's definitions and statement lists (see *Definitions and statements*) |
| `damping` | default damping for the bones the layer writes (nodes and items override) |
| `mask` | `{"mode": "include_only", "includedParts": ["mouth"]}` (or `exclude_only`): the bones the layer may write |
| `mirror` | `{"@when": <condition>, "pairs": [["leftArm","rightArm"], ...]}`: the rule items with `@mirror` / `@swapSides` follow (see *Mirroring*) |
| `nodes`, `machines` | the layer's nodes and machines, as maps by name |
| `select`, `@connections`, `defaultOnEntry` | how the layer picks its node (see *Choosing the node*) |

## Nodes

```json
"walk": {
  "core:pose": {
    "pose": [ ...items... ],
    "enterPose": [ ...items evaluated once on entry; their bones snap to it... ],
    "snapOnEnter": ["body"],
    "damping": {...}
  },
  "@define": {"stride": {"live": {"mul": ["entityLimbSwing", 2]}}},
  "@on": {"enter": [{"set": ["layer.combo", 0]}]},
  "@connections": [{"when": "layer.bounced", "then": "jump"}]
}
```

* The node's one key without `@` is its type: `core:pose`, `core:fallthrough` or
  `core:vanilla`. `core:pose` takes the pose stack and what goes with it (`pose`, `enterPose`,
  `snapOnEnter`, `damping`); the other two pose nothing and take `{}`.
* `@define` and `@on` are the node's definitions and statement lists (see *Definitions and
  statements*): `node.` names, and what runs when the node is entered, every frame it is posed
  and when it is left.
* `@connections` are the node's own ways out (see *Choosing the node*).
* Operations with a memory (`decreased`, `rose`, `fell`) start over when what they are written on
  is entered: a node for its connections, everything its items compute (their `@when`s and their
  own fields) and the layer's mirror rule; a machine for its selector and
  its connections, before its selector chooses. A layer's own `@when` starts with the layer, and
  its selector and connections as a machine's: the layer is entered when it starts (see
  *Machines*).
* A `core:fallthrough` node poses nothing, so the layers below show through; it has
  connections, definitions and statement lists like any node. A transition into or out of it fades between
  the layer's pose and the one below: what one side poses and the other doesn't is blended
  against the layers below (a full rotation, offset or vector as they have it; a `pre` / `post`
  rotation or an additive offset as nothing). It is how an extension lets the animation it
  extends show until it has something to add, but any layer can use it.
* A `core:vanilla` node hands the entity back to Minecraft: while any layer is in one (and that
  layer's `@when` holds), the entity is drawn with its vanilla model and vanilla animation, and a
  player's first-person hand is vanilla too. The animator keeps running underneath, so its
  layer still decides its node every frame and leaving the node brings the animated model back
  where it would have been. The switch is immediate: the two models can't be blended. It poses nothing
  and has connections, definitions and statement lists like any node. It is meant for extensions
  that bring back animations made for the vanilla model (another mod's, say) while a condition
  holds:

  ```json
  "select": [{"when": "entityIsInWater", "then": "theirs"}, {"then": "animated"}],
  "nodes": {
    "animated": {"core:fallthrough": {}},
    "theirs": {"core:vanilla": {}}
  }
  ```

  The example pack `misc/examples/vanilla-swim-extension` makes players vanilla while they are in
  water: one layer with `"@when": "entityIsInWater"` and a single `core:vanilla` node.

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
  "@define": {
    "jumping": {"live": {"or": [{"not": ["entityIsOnGround"]}, {"lt": ["entityTicksAfterTouchdown", 1]}]}}
  },
  "select": [
    {"when": "entityIsSleeping", "then": "sleeping"},
    {"when": "layer.jumping", "then": [
      {"when": {"core:is_flying": []}, "then": "flying"},
      {"then": "jump"}
    ]},
    {"when": "entityIsStandingStill", "then": "stand"},
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
* A branch can carry `transitionDuration`, `transitionEasing` and `do`, as a connection does. In
  a nested list, the branches inside take the transition of their enclosing branches unless they
  have their own; `do` lists add up, the enclosing ones first.
* A selector only names the members of its own machine (for a layer: its nodes and machines,
  not those inside its machines).

### Machines

`machines` is a map of machines by name, next to `nodes`. A machine has `nodes` and `machines` of
its own, and its own `select`, `@connections`, `defaultOnEntry`, `@define` and `@on`. A layer
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
* A layer is entered when it starts, on the animator's first frame (whether or not its `@when`
  holds). That is the frame's decision, and there is nothing to crossfade from: a branch's
  `transitionDuration` and `transitionEasing` don't apply there, its `do` does. The node's
  connections, and the selectors again, are checked from the next frame on. So `defaultOnEntry`
  only matters where the selectors choose nothing; to open on a node the selector wouldn't choose
  (an intro), give it a branch of its own, first, and connections out of it.

### Connections

Connections are the ways out that depend on where the layer is. A node's `@connections` lead out
of that node, a machine's lead out of any node inside it, and a layer's out of any of its nodes.
A connection is written like a selector branch:

```json
{"when": "machine.attacked", "then": "slash_down", "transitionDuration": 0,
 "transitionEasing": "ease_in_out", "do": [{"set": ["layer.combo", 2]}]}
```

* `when` is its condition, and `then` any node or machine of the layer: a machine is entered as
  above. A connection to where the layer already is starts that node over (a jump bouncing into
  another jump).
* `do` is a statement list, run when the connection fires (see *Definitions and statements*).
  `transitionDuration` (ticks)
  crossfades (an interrupted crossfade continues from what was on screen); easings `linear`,
  `ease_in`, `ease_out`, `ease_in_out` (the default), `exponential`. Where one side of a crossfade
  poses a bone absolutely and the other only relatively (`pre` / `post`, additive), the relative one
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
machine, the transition (its duration and easing) is that of the branch or the connection. What
runs, in order: the `exit` lists of what the transition disposes of at once, the transition's own
`do`, then, on the way in, each machine entered (its definitions and `enter` list, then its
selector, whose branch runs its `do`), and last the node entered. Each selector sees what ran
before it.

## Pose items

A node's `pose` is a stack; later items compose over earlier ones. An item is a clip
(`core:clip`) or a driver (`core:axis_rotate`, ...): its one key without `@` names it, and its value
holds the item's own fields. The modifiers, the same for every item:

| modifier | meaning |
|---|---|
| `@when` | a condition; the item is skipped while it does not hold |
| `@space` | `override` (replace), `pre` (rotate in the parent's space, the `rotate*` idiom), `post` (the bone's own space, the `localRotate*` idiom). Default: `override` for clips, `pre` for drivers. |
| `@damping` | smoothing rate per bone written, `{"body": 0.5, "root": [null, 0.6, null]}`; an expression is allowed (a name or an operation, evaluated every frame); unlisted bones keep their rate |
| `@vectorModes` | for offset vectors: `slide` (tween restarted when the target changes), `retarget` (exponential approach re-aimed every frame), `snap` |
| `@snap` | the written bones jump to their target this frame (`orientInstant`, `finish`) |
| `@mirror` | evaluate as the left-right mirror image while the layer's mirror condition holds (paired bones swapped, Y and Z rotations and X offsets negated; see *Mirroring*) |
| `@swapSides` | like `@mirror`, but only the paired bones swap; rotations and offsets are kept |

```json
{"@when": "entityIsSwinging", "core:clip": {"animationKey": "mobends:bends/animations/player/tool_arm.json", "frame": "entitySwingProgress"},
 "@snap": true, "@swapSides": true}
```

### Mirroring

A layer's `mirror` rule says when its mirrored items mirror, and which bones pair up:

```json
"mirror": {
  "@when": "entityIsLeftHanded",
  "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"],
            ["leftLeg", "rightLeg"], ["leftForeLeg", "rightForeLeg"]]
}
```

While the rule's `@when` holds, an item with `@mirror` or `@swapSides` runs on a reflection of the
pose built so far, and its result is reflected back. Reflecting twice gives back the original, so
the item's composition rules don't change. An item that sets either without a rule on its layer
is a load error.

| | paired bones swap | Y and Z rotations, X offset negated |
|---|---|---|
| `@mirror` | yes | yes: the item's left-right mirror image |
| `@swapSides` | yes | no: the same motion, moved to the other side |

**Values are never negated**: every variable and expression reads the same, mirrored or not.
What an item does mirrored follows from how it is marked:

- **`@mirror`** for motion that belongs to a side: swings, clips, a head turned toward the weapon
  (`head`, Y, `-30` becomes `+30`).
- **Neither** for an item on an unpaired bone that follows a world direction. The head turned by
  `entityHeadYaw` must look where the entity looks, so it isn't mirrored.
- **`@swapSides`** for an item on a paired bone that follows a world direction: the right arm
  turned by `entityHeadYaw` becomes the left arm turned by the same angle, still pointing where the
  entity looks.
- An item that mixes the two is split into two items.

(Earlier versions negated a rule's `negate` list of variables inside mirrored items; a rule
with `negate` is now a load error.)

### Clips

```json
{"core:clip": {"animationKey": "mobends:bends/animations/biped/walk_base.json",
               "frame": {"mod": [{"mul": ["entityLimbSwing", 0.6662]}, "clipLength"]},
               "weight": "entityLimbSwingAmount", "bones": ["leftLeg", "rightLeg"]}}
```

A clip's own fields are `animationKey`, `frame`, `duration`, `weight` and `bones`.

* `frame` (an expression) is where in the clip it is, in the clip's own units: from 0 to
  `clipLength` (the clip file's `duration`, or its keyframe count − 1 if it has none). A frame
  before 0 or past `clipLength` holds the first or last keyframe; a clip loops by wrapping its
  frame with `mod`, as above.
* `duration` (optional, in ticks) is how long the item runs, whatever its frame does: the clip
  is finished (`nodeIsFinished`) once `nodeTicksElapsed` reaches it. Without one it never
  finishes.
* The default `frame` is `{"mul": [{"div": ["nodeTicksElapsed", "clipDuration"]}, "clipLength"]}`
  with a `duration` (the clip is fitted to it), and `"nodeTicksElapsed"` without one (a unit per
  tick).
* Inside `frame`, `clipLength` and (when the item has a `duration`) `clipDuration` are built-ins.
  `{"mod": ["nodeTicksElapsed", "clipLength"]}` with `"duration": 30` loops for 30 ticks, then
  finishes.

`weight` (an expression) is how much of the clip is applied. For a bone the clip writes relatively
(`pre` / `post`) it scales the rotation angle and the offset; for a bone it replaces (`override`)
it blends from what the bone has so far this frame (from the layers below and the items before)
to the clip, or from the rest pose where nothing has written the bone yet. `bones` restricts the
clip.
Clip files (`animations/...json`) hold `bones` with keyframes (`position`, `rotation` as a
quaternion `[x, y, z, w]`; each may be left out for no offset, no rotation),
and optionally `duration`, `interpolation: "step"` and explicit keyframe `times` (in the clip's
units). Keyframes straddling the ±180° wrap interpolate the short way.

Bone names are the subject's named parts, plus `root` / `globalOffset` and `localOffset` for
the entity-level offset vectors (their keyframe positions are the vector).

### Drivers

Each driver is `{"<driver>": {fields}}` plus the modifiers.

| driver | fields |
|---|---|
| `core:axis_rotate` | `bone`, `axis` (`x`/`y`/`z`), `angle` (expression, degrees) |
| `core:vector` | `bone`, `x`, `y`, `z` (expressions; an axis left out keeps the bone's current target) |
| `core:offset` | `bone`, `x`, `y`, `z`: a bone's position offset |
| `core:accumulate` | `inout` (a number state), `rate` (expression, per tick, any sign), `min`, `max`: steps the state by the rate; with `min` 0 and `max` 1 and a rate that changes sign with a condition, it ramps up and down |
| `core:spring` | `inout` (a number state), `target` (expression), `stiffness` (per tick²), `friction` (per tick): pulls the state towards `target` like a mass on a spring, so it lags, overshoots and settles (follow-through); its velocity is its own, at rest when the node is entered |
| `core:step_turn` | the body stands, turns and walks on feet planted in the world (see *Turning on the feet*) |
| `mobends:cape` | the player's cape physics |
| `mobends:sword_trail` | `add`, `resetOnEnter`, `resetEachFrame`, `velocity`: feeds the sword trail |
| `mobends:spider_idle_legs`, `mobends:spider_moving_legs` | the spider's inverse-kinematics gaits (see the templates' fields); `out` (`groundLevel`), `reset` (a state that, non-zero when the node starts, re-plants the feet, and is cleared) |

A driver writes the animator's state only where its fields say: `inout` is the state it steps,
`out` maps its outputs to the states they go to (`{"groundLevel": "node.groundLevel"}`). Each is a
number state, declared like any other (see *Definitions and statements*); the driver steps it
once per frame, in pose-stack order, so the items after it read this frame's value. The value
lives where its state is declared, and the driver writes it only while its node is posed.

```json
"@define": {"onFeet": {"state": 0}, "turnSpeed": {"state": 0}, "armLag": {"state": 0}},
"core:pose": {"pose": [
  {"core:accumulate": {"inout": "node.onFeet", "rate": {"if": ["layer.jumping", -0.15, 0.15]}, "min": 0, "max": 1}},
  {"core:step_turn": {"weight": "node.onFeet", "legs": [...], "out": {"turnSpeed": "node.turnSpeed"}}},
  {"core:spring": {"inout": "node.armLag", "target": "node.turnSpeed", "stiffness": 0.12, "friction": 0.3}}
]}
```

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
{"core:step_turn": {"weight": "standing",
  "legs": [{"upper": "leftLeg", "lower": "leftForeLeg", "hip": [-4, 11, 0], "knee": [0, 5, -3], "foot": [-0.5, 8, 2.5]},
           {"upper": "rightLeg", "lower": "rightForeLeg", "hip": [5, 11, 0], "knee": [0, 5, -3], "foot": [-0.5, 8, 2.5]}]}}
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
  placed anew under the body when it goes up again. Run it in its own layer with an accumulator
  that goes up while the mob is on the ground, so the jump shows beneath it and the body turns back to
  vanilla's yaw while it plays.
* It reads the entity's values `yawVariable` (`entityBodyYaw`), `xVariable` and `zVariable` (`entityWorldX`,
  `entityWorldZ`), and its outputs (the states its `out` names) are `turnLag` (degrees vanilla's body yaw is ahead of the shown one: what a
  head posed by `entityHeadYaw` has to add), `turnSpeed` (degrees per tick the shown body turns),
  `stepLift` (0..1 as the stepping foot rises, negative for a foot on the -X side),
  `stepImpact` (peaking at 1 `impactTime` ticks after a landing; quick landings add up smoothly
  rather than starting it over) and `stride` (model units the
  feet on the +X side are ahead of those on the -X side, halved: what an arm swing follows), all
  scaled by the weight, for the items after it to make the upper body react. `iron_golem.json`
  does it with springs, and turns back vanilla's rocking of a walking golem
  (`RenderIronGolem.applyRotations`) in the render rotation beneath the driver.

### Expressions

Every value an animator computes is an expression: every number an item computes (an angle, a
weight, a vector axis, a damping rate, ...) and every condition (a layer's, an item's or a mirror
rule's `@when`, a branch's or a connection's `when`). An expression is a
JSON tree, so tools can read and write it without a parser.

| form | meaning |
|---|---|
| a number | a constant: `45` |
| `true`, `false` | a constant condition |
| a string | a name: a definition, by its scoped name (`"layer.combo"`, see *Definitions and statements*), or a bare name: a built-in (`nodeTicksElapsed`, `entityIsOnGround`, `partialTicks`, see *Built-in values*), or a value specific to a mob, the entity's own (see there) |
| an object with one key | an operation; the key is its name, the value the list of its arguments (always a list): `{"sin": ["t"]}`. Its other keys are modifiers: `@comment` on any operation, `@fallback` on one that takes it (`field`) |

**Every expression is a number or a boolean**, and which one is checked when the animator loads:
a number where a condition goes (`"@when": "entityLimbSwing"`), or a boolean where a number goes
(`{"add": ["entityIsOnGround", 1]}`), is an error. Arithmetic is in single precision (`float`); strings
are never values, only arguments some operations take written out (an item id, a hand).

Operations nest freely:

```json
"angle": {"add": [
  {"mul": [{"sin": [{"mul": ["ticks", 0.1]}]}, 6]},
  {"mul": [{"sin": [{"mul": ["ticks", 0.37]}]}, 2]},
  -85
]},
"@when": {"and": ["entityIsOnGround", {"not": ["entityIsSneaking"]}, {"lt": ["entityTicksAfterAttack", 10]}]}
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
connections, a layer for its `@when`. `{"decreased": ["entityTicksAfterAttack"]}` holds on a new attack.

### Reading the entity: `field`

`{"field": ["ridingEntity", "motionX"]}` reads the entity's `ridingEntity.motionX`: the arguments
are a **path**, one field per step, starting at the entity.

**`field` is written only in a model definition's `@define` and `@on`** (see *Model
definitions*); anywhere else it is a load error. Animators read the `entity.` names a model
definition declares and stay portable: how a mob provides a value stays in its own files.

* Resolved when the animator loads: each step is looked up on the declared type of the step
  before it, starting at the entity's class and walking up superclasses. The last field must be a
  number or a boolean, which is the expression's type. Fields are named by their development
  (MCP) names (see *Field names*).
* `@fallback` takes any expression of the same type: another field, a name, a constant, an
  operation. If the path can't be resolved on the entity's class, the fallback is compiled
  instead; if a step is `null` while animating (not riding anything), the fallback is evaluated
  for that frame (without one, the value is 0, or false). Without a fallback, a path that can't be
  resolved is a load error.

  ```json
  {"field": ["destPos"], "@fallback": {"field": ["flapTarget"]}}
  {"field": ["ridingEntity", "renderYawOffset"], "@fallback": 0}
  {"field": ["isCharging"], "@fallback": false}
  ```
* `{"exists": [path...]}` holds while every step of the path is there (none of them `null`); a
  path the entity's class doesn't have never holds.
* Interpolating between ticks is written out:
  `{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}`.

`@fallback` is refused on an operation that takes none (one that can always be computed).

**Registered operations** have a namespaced name, and take arguments of their own kinds. Each
applies to the entities of a class (and its subclasses): written in an animator for an entity of
another class, it takes its `@fallback`, or the animator fails to load. (Mods add their own: see
`docs/animation.md`, *Operations in Java*.)

| operation | arguments | value | applies to |
|---|---|---|---|
| `core:holds_item` | `[hand, item]`: `main_hand` or `off_hand`, an item id | the hand holds that item | living entities |
| `core:holds_any_item` | `[hand]` | the hand holds anything | living entities |
| `core:active_hand_side` | `[side]`: `left` or `right` | the hand on that side is using an item | living entities |
| `core:equipment_name` | `[slot, pattern]`: `mainhand`, `offhand`, `head`, `chest`, `legs`, `feet`, and a regular expression | the display name of what the entity has in the slot matches the pattern as a whole | living entities |
| `core:is_flying` | none | the player flies (creative or spectator flight, not an elytra) | players |
| `mobends:use_action` | `[action]`: `food`, `bow` or `shield` | the item in use is used as that (Mo' Bends' classification, which the config can change) | living entities |
| `mobends:attack_action` | `[action]`: `fists`, `sword` or `tool` | the held item attacks as that | living entities |
| `mobends:spin_attack_enabled` | none | the config lets the sword combo end with a spin | every entity |
| `mobends:is_sitting` | none | the wolf sits | wolves |
| `mobends:wolf_interested_angle` | none | the head's tilt while begging, in degrees (a number) | wolves |
| `mobends:wolf_shake_angle` | `[offset]`, a number written out: how far behind the head the part shakes (vanilla's: head 0, mane −0.08, tail −0.2) | the part's roll while shaking off water, in degrees (a number) | wolves |
| `mobends:wolf_tail_rotation` | none | the tail's raise, as vanilla's model has it, in degrees (a number) | wolves |
| `mobends:is_beside_climbable` | none | the spider is against a wall it climbs | spiders |

Mistakes (an unknown operation, a wrong number or kind of arguments, an object with more than one
key, a number where a boolean goes) are reported when the animator loads, in the operation's own
words: `'core:holds_item' argument 1 (hand) must be one of main_hand, off_hand, got 'left_hand'`.

### Built-in values

**Bare names are built-ins**: what Mo' Bends provides, always there, with no arguments. They are
the node's and the layer's clocks and phases (see *Nodes, transitions and time*), inside a clip's
`frame` its `clipLength` and `clipDuration` (see *Clips*), the time, and the entity's values. A
built-in's type is its own, whatever its case.

| built-in | value |
|---|---|
| `ticks` | the local player's age in ticks plus `partialTicks`: the same for every entity; it restarts when the local player respawns or changes dimension |
| `partialTicks` | the progress between two game ticks, 0..1 |
| `ticksPerFrame` | the ticks this frame lasted, at most 1; 0 while the game is paused |
| `random` | a random number, 0..1, new on every read |

**The entity's values** are named `entity…`: the ones that hold for every animated entity (every
one is an `EntityLivingBase`), whatever its class. What depends on the class is a registered
operation, or a definition of the mob's model definition (see *Model definitions*).

| built-ins | from |
|---|---|
| `entityLimbSwing`, `entityLimbSwingAmount`, `entitySwingProgress`, `entityHeadYaw`, `entityHeadPitch` | the arguments the vanilla renderer passes to every model (`entitySwingProgress` is vanilla's swing progress interpolated by `partialTicks`, as `getSwingProgress` does) |
| `entityIsSprinting`, `entityIsSneaking`, `entityIsAlive`, `entityIsInWater`, `entityIsRiding` | `Entity` |
| `entityIsChild`, `entityIsLeftHanded`, `entityHealth`, `entityIsSwinging`, `entityIsSleeping`, `entityIsElytraFlying` | `EntityLivingBase` (false or 0 unless a subclass says otherwise; `entityIsSleeping` holds only while alive) |
| `entityId` | vanilla's entity id (a number to vary entities by) |
| `entityTicksExisted`, `entityTicksElytraFlying` | vanilla's age and glide time |
| `entityItemUseTicks`, `entityItemUseTicksLeft` | ticks the item in use has been used so far, and ticks left (vanilla's `getItemInUseMaxCount` and `getItemInUseCount`) |
| `entityIsOnGround`, `entityIsClimbing` | Mo' Bends' own detection: collision boxes and stairs, with liftoff detection; ladders |
| `entityTicksInAir`, `entityTicksAfterTouchdown`, `entityTicksFalling`, `entityClimbingCycle` | Mo' Bends' counters over that detection |
| `entityTicksAfterAttack` | ticks since the last counted swing (below) |
| `entityIsUnderwater`, `entityLedgeHeight`, `entityClimbingRotation` | Mo' Bends' block checks around the entity |
| `entityIsDrawingBow`, `entityIsRidingLiving` | the held items; whether what the entity rides is living |
| `entityIsStandingStill`, `entityIsStrafing` | Mo' Bends' measured motion |
| `entityMotionY`, `entityPrevMotionY`, `entityInterpolatedMotionY`, `entitySpeed`, `entityXZSpeed`, `entityForwardMomentum`, `entitySidewaysMomentum` | Mo' Bends' measured motion: position changes each tick, so it works for every entity (vanilla's motion fields read 0 for the entities the server moves). `entitySpeed` and `entityXZSpeed` are magnitudes interpolated by `partialTicks`; a rider's include what carries it |
| `entityBodyYaw`, `entityWorldX`, `entityWorldY`, `entityWorldZ` | the body yaw and the position, interpolated as the renderer does (`core:step_turn`'s default inputs) |
| `entityRidingRelativeHeadYaw`, `entityRidingRelativeYaw` | relative to the ridden entity (0 unless riding something living) |
| `entityClimbingRenderYaw`, `entityClimbingBodyYaw`, `entityClimbingHeadYaw` | the yaws while climbing |

Values left out are added when something needs them; adding a built-in never breaks a file.

**`entityTicksAfterAttack`** counts swings for every entity: once per game tick it checks
vanilla's `isSwingInProgress`, counts a swing when one starts, and again whenever more than 5
ticks have passed while it keeps going (holding attack, mining). The counter goes back to 0 on a
counted swing and grows by `ticksPerFrame` every frame. `{"decreased": ["entityTicksAfterAttack"]}`
is the edge of a counted swing (`{"rose": ["entityIsSwinging"]}` would miss the re-counts of a
swing that keeps going). The player's ignores a swing within 6 ticks of the last counted one
while its main hand holds an item, so a sword combo isn't restarted.

**Values specific to a mob** are, until they move into the mob's own files, the entity's own,
named by its data class (the squid's `squidRotation`, the spider's `crawlProgress`): a
bare name that isn't a built-in, a **state** (a boolean) if written in capitals, else a
**variable** (a number).

The entity's values are looked up once, when the animator is bound to its entity on the first
frame, never by name while animating; one the entity doesn't have fails the animator then
(logged; the entity isn't animated), even if nothing ever reads it.

## Nodes, transitions and time

Built-in values of the node being evaluated and its layer:

| built-in | value |
|---|---|
| `nodeTicksElapsed` | ticks since the node was entered |
| `layerTicksElapsed` | ticks since the layer started |
| `nodeFadeProgress` | the linear progress of the crossfade the node is part of, the same number on both sides: 0 when the transition starts, 1 when it ends, 1 with no crossfade running. How much of a node is shown is `nodeFadeProgress` fading in and `1 − nodeFadeProgress` fading out; the transition's easing applies to the blend, not to this value |
| `nodeIsFadingIn` | the node is the layer's current node, while the crossfade into it runs |
| `nodeIsActive` | the node is the layer's current node, fully in |
| `nodeIsFadingOut` | the node is the one the layer left, still posed while the crossfade runs (its `update` list still runs, so a statement there can opt out with it) |
| `nodeIsFinished` | the node's timed clips are done (below) |

Exactly one of the three phases holds for a node being evaluated; the current node is
`nodeIsFadingIn` or `nodeIsActive`. Outside any node (an animator's statement lists), the clocks
read 0, `nodeFadeProgress` 1 and the phases don't hold.

The node and layer clocks advance by the ticks the frame lasted after the frame is posed (a node
reads 0 on its first frame) and pause while the layer's `@when` doesn't hold.

**A node never runs twice at once.** A transition to the current node restarts it without a
crossfade; a transition during a crossfade freezes what is on screen into a snapshot and fades
from that, and the node being left is disposed (its `exit` list runs) at once.

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

## Definitions and statements

Every scope (the entity, the animator, a layer, a machine, a node) can declare **definitions** in
its `@define`, read by **scoped name** (`entity.x`, `animator.x`, `layer.x`, `machine.x`,
`node.x`), and run
**statements** in its `@on` lists. Expressions never change anything; statements (and drivers,
see *Drivers*) are the only things that do.

```json
"@define": {
  "startYaw": {"constant": "entityBodyYaw"},
  "combo":    {"state": 0},
  "stride":   {"live": {"mul": ["entityLimbSwing", 2]}}
},
"@on": {
  "enter":  [{"set": ["layer.combo", 0]}],
  "update": [{"@when": {"gt": ["entityTicksAfterAttack", 20]}, "set": ["layer.combo", 0]}]
}
```

A definition is an object with one key, its kind, whose value is an expression (a number or a
boolean, which is the definition's type):

| kind | its value | changed by |
|---|---|---|
| `constant` | computed once, when its scope is created (a node constant can note the body yaw a turn started from) | nothing |
| `state` | its initial value, computed when its scope is created | `set` statements, and drivers that write it |
| `live` | computed once per frame, when it is first read (a live definition that remembers something, such as `{"decreased": [...]}`, every frame its scope exists, so it never misses one) | nothing: it follows what it reads |

**Names carry their scope.** There is no lookup through enclosing scopes and no shadowing: the
prefix says where the value lives, and a name that scope doesn't declare is an error.

* `entity.` is the entity's, declared by its model definition only (see *Model definitions*) and
  read by its animator and every extension; an entity whose model has no definition has no
  `entity.` names.
* `animator.` is the animator's, shared by every layer; `layer.` the layer the name is written
  in; `machine.` the innermost machine around the place the name is written (a layer's own nodes
  and selector are in no machine); `node.` the node the name is written in. A name can only be
  read where its scope exists: a `node.` name inside its node, a `machine.` name inside its
  machine. What two scopes share is declared on the smallest scope around both: the layer for its
  machines, the animator for its layers.
* A name is one prefix and one name: a definition's own name has no dot.
* Definitions can read each other in any order; one that depends on itself is an error. Every
  definition is checked when the animator loads, used or not.
* An animator that `extends` another shares its `animator.` scope with it: the child reads the
  parent's names directly, and declaring a name the parent declares is an error. The parent's
  `@on` lists run before the child's.
* An extension's animator is a scope of its own: it never sees the names of the animator it
  extends.

**Scopes are created when they are entered** and disposed when they are left: the entity's and
the animator's on its first frame; a layer's when it starts; a machine's on every entry into it, so its definitions
start over each time, and it is disposed once the last of its nodes has faded out; a node's on
every entry. A node fading out keeps its scope until its crossfade ends; a node a transition
leaves without a crossfade, or whose crossfade a transition cuts short, is disposed at once.

**Statement lists**, in a scope's `@on`:

* `enter` runs when the scope is created, right after its definitions take their values;
* `update` runs every frame the scope exists, a node fading out included (lists run once per
  frame, not per game tick: a statement that counts or integrates scales by `ticksPerFrame`; an
  entity that isn't drawn isn't animated, so its lists don't run while it is off screen);
* `exit` runs when the scope is disposed.

A selector branch and a connection have a list of their own, `do`, run when they move the layer.
A statement is `{"set": ["<state>", <value>]}`, with an optional `@when`: it sets the state to the
value (an expression of the state's type) while the condition holds.

**Following another layer's choice.** A layer that depends on what another layer chose reads a
definition, never the other layer's node:

* A choice without memory (a function of the frame's state, such as a selector whose every list
  ends with a branch that always holds) is a live definition both layers read: the choosing
  layer's selector, and every layer that follows it. The player's upper-body layers follow its
  locomotion through `animator.standing`, `animator.walking` and `animator.sprinting`, each the
  conditions under which the locomotion selector chooses that node.
* A choice with memory (a selector that holds its node in a dead band, connections, timers) is a
  state the choosing layer publishes, set in the `enter` lists of its nodes (not their `exit`
  lists, which run only when a crossfade ends): the iron golem's `animator.inJump`.

**Who may set what.** A statement sets a state of its own scope or of a scope around it (a node
can set its layer's or its animator's). A file from a resource pack may only set (or have its
drivers write) a state a file from a resource pack declares: it can't steer the trusted layers,
whose output the resource-pack limits are measured from (see *Servers*).

**Order.** Every frame, the entity's `update` list runs first, then the animator's (the
extensions' after it, in their order); then, layer by layer, after the layer has chosen its node and before it is posed:
the layer's, its machines' from the outermost in, the node fading out, the current node. When
several write one state in a frame, the last write wins: later layers see it this frame, earlier
ones the next.

### Functions

A scope can declare **functions** in its `@functions` (the animator, a layer, a machine): an
expression repeated with different inputs, written once.

```json
"@functions": {
  "wobble": {"params": {"t": "number", "amount": "constant"},
             "body": {"mul": [{"sin": [{"mul": ["arg.t", 6.28]}]}, "arg.amount"]}}
},
...
{"core:axis_rotate": {"bone": "head", "axis": "z",
                      "angle": {"animator.wobble": [{"div": ["nodeTicksElapsed", 20]}, 5]}}}
```

* **A call is its body written out** at the call site, with the arguments in place. Every place
  a body reads an argument is the argument written out, so every call, and every read, keeps its
  own memory for edge triggers. A body is an expression: it sets nothing.
* **Called by scoped name**, as the operation's key: `{"animator.wobble": [...]}`, `layer.x` for
  a layer's, `machine.x` for a machine's (the innermost around the call, as for names). Arguments
  are positional.
* **Parameters** are read in the body as `arg.<name>`, and take the kinds operations' arguments
  have: `number` and `boolean` (any expression of that type, written at the call site, which
  reads the call site's names), `constant` (a number written out), `string`, and
  `{"choice": [...]}` (one of those strings). The written-out ones are put in the body as they
  are, so they can stand where an operation takes a string or a constant.
* **A body reads** its parameters, the built-ins, and the names of the scope declaring it: an
  animator's function can't read a layer's names, wherever it is called. It is checked once, where
  it is declared, used or not.
* A function that calls itself, through any number of others, is a load error. A function and a
  definition of one scope can't share a name, nor can two functions (across `extends` too, which
  is how files share functions).

## Semantics worth knowing

* Transitions are decided before posing; a node entered this frame poses this frame.
* A layer's selectors only move it when they lead somewhere else: the layer stays in a node for
  as long as it is the selector's first match.
* Smoothing lives in the subject's bones. Damping in the animator sets a bone's rate; leave it
  out and the bone keeps the rate it has.
* An offset vector written by two layers in one frame keeps only the last write; a vector
  re-aimed every frame on top of another writer is `retarget`.
* A bone written only relatively (`pre` / `post`, additive) with nothing absolute beneath builds on
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
  "selector": {"core:entity_type": ["minecraft:cow"]},
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
     "split": {"axis": "y", "at": [0.5], "names": ["foreLeg1"]}}
  ]
}
```

The chicken's definition also declares the values its animator reads, over the entity's fields:

```json
"@define": {
  "flapWave":  {"live": {"add": [{"mcsin": [{"lerp": [{"field": ["oFlap"]}, {"field": ["wingRotation"]}, "partialTicks"]}]}, 1]}},
  "flapSpeed": {"live": {"lerp": [{"field": ["oFlapSpeed"]}, {"field": ["destPos"]}, "partialTicks"]}},
  "wingAngle": {"live": {"mul": ["entity.flapWave", "entity.flapSpeed", 57.29578]}}
}
```

| field | meaning |
|---|---|
| `formatVersion` | `2` (the index, `{"formatVersion": 2, "models": [...]}`, has one too) |
| `entity` | the entity class; `model` (optional) restricts mutation to that model class and its subclasses |
| `animator` | the animator asset; `key` / `unlocalizedName` override the registry's |
| `bones[].vanilla` | the vanilla part the bone takes over: `field` is the model's field by its development (MCP) name (see *Field names* below), `element` its index for array fields, `index` an optional fallback position in the model's box list (creation order). Every field or array slot that holds the part is replaced, found by identity. |
| `bones[].parent` | renders the bone inside another one; `position` is then relative to the parent. A stand-in takes the vanilla slot: it draws nothing, but what vanilla does through the field reaches the bone (its visibility, such as a player's skin-part toggles, and `postRender`, where vanilla attaches held items and hats) |
| `bones[].position` | where the bone sits (relative to its parent if it has one); moves its boxes with it. Default: its `pivot`, or else the vanilla rotation point, relative to the parent's |
| `bones[].pivot` | where the bone turns, in model units, when not where the vanilla part does: the boxes stay where they are (a body that bends at the hips). What vanilla attaches to the part stays where vanilla puts it |
| `bones[].overlay` | another bone this one lies over (a sleeve over an arm): it turns with that bone, at its pivot, and its boxes are split as that bone's, each piece riding its segment (and open at the joints). It has no `parent`, `position`, `pivot` or `split` of its own |
| `bones[].inflate` | extra inflation of the boxes on top of the vanilla one, `[x, y, z]` per segment (the bone's own first): a hair, so faces that meet don't flicker |
| `bones[].restRotation` | constant X, Y, Z degrees the vanilla model held the part at (`setRotationAngles` constants), applied before the animated rotation |
| `bones[].split` | cuts the part's boxes along `axis` at the `at` fractions; each cut adds a bone named in `names`, a child of the previous segment pivoting at the cut, with the matching strip of the texture. Knees, elbows, tail and tentacle joints. `hinge` puts the joints on an edge of the cut instead of its middle: `front` / `back` (-Z / +Z) or `top` / `bottom` (-Y / +Y). A joint hinges on the side opposite to where it bends (a knee at the front, an elbow at the back), so the segments stay joined when it bends. `caps: true` draws the faces at the cuts, closing each segment (a bent knee shows no gap). What vanilla attaches to the bone (`postRender`) follows every segment: a held item follows the forearm. |
| `@define`, `@on` | the entity scope: what the mob exposes to its animators and extensions (`entity.wingAngle`) and remembers across frames, as an animator's scopes declare theirs (see *Definitions and statements*). The only place `field` and `exists` are written (see *Reading the entity*) |
| `@comment` | a note, ignored |

**The entity scope is the model definition's alone.** It is the one file every type, animator and
extension of the model shares: several types can choose one model (a pack's type for one player
name and the default type), and if types declared entity values, which `entity.` names exist
would depend on which type won. Extensions declare none (what an extension needs for itself is in
its own animator's `@define`), so two extensions never collide on an `entity.` name. Model
definitions have no `extends` (yet). A model definition from a resource pack is untrusted: an
untrusted animator may set its state, never a trusted one's.

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
`field` finds fields the same way. The tables hold numeric fields only, so a vanilla boolean
field, or a step through a vanilla object field (`ridingEntity`), is found in development only:
in the game it takes its `@fallback`.

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
Java addon is needed unless it brings its own operations or drivers.

```json
{
  "formatVersion": 2,
  "id": "yourpack:notch_zombie_arms",
  "selector": {"and": [{"core:entity_type": ["minecraft:player"]}, {"core:player_name": ["Notch"]}]},
  "animator": "yourpack:bends/animators/zombie_arms.json"
}
```

| field | meaning |
|---|---|
| `formatVersion` | `2` |
| `id` | identifies the type; ranks are stored by it. Two types with the same id: a warning, and the one from the higher-priority pack is used (the server's resource pack, then the enabled resource packs from the top of the list, then mods) |
| `selector` | a condition on the entity, an expression (see *Selectors*); absent means the type applies to every entity |
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

## Selectors

A selector is a boolean expression (see *Expressions*), run against the entity before any entity
data exists: it reads no names (no built-ins, no definitions) and only the **selector-safe**
operations, which read the entity alone; anything else in a selector is a load error, and so is
an edge trigger (`decreased`, `rose`, `fell`), which would have nothing to remember. `and`, `or`,
`not` and `if` combine them. The selector-safe operations:

| operation | arguments | holds for |
|---|---|---|
| `core:entity_type` | one or more registry ids; `minecraft:player` stands for players, which have none | an entity of one of these types |
| `core:player_name` | one or more names | a player whose profile name (never the display name) is one of these, ignoring case |
| `core:player_uuid` | one or more UUIDs | a player with one of these UUIDs, which stay the same when it renames |
| `mobends:skin_variant` | `default` or `slim` | a player whose skin has these arms |

```json
"selector": {"and": [{"core:entity_type": ["minecraft:player"]},
                     {"or": [{"core:player_name": ["Notch", "jeb_"]}, {"mobends:skin_variant": ["slim"]}]}]}
```

A mod adds its own (`KumoOperation...selectorSafe(stable)`, see `docs/animation.md`, *Operations
in Java*); they can be used in animators too. An operation says whether its answer can change
during an entity's life: a player's name can't; a skin variant can, as it reads `default` until
the skin downloads. The types whose selector holds are cached per entity (weakly, so unloaded
entities aren't kept alive); types whose selector holds an operation that can change are asked
again every frame, and an entity can change type mid-life. Its data is then made anew by the new
type's factory.

The entity types a selector requires (its `core:entity_type` ids, but not under a `not`) list the
type under those entities in the settings.

## Precedence

When the selectors of several types hold for an entity, exactly one type applies. Candidates are
sorted by these keys, most significant first (`TypeOrder`):

1. **rank**, higher first. Every type starts at rank 0; only the user sets ranks.
2. **the number of conditions** of the selector, higher first: a broad assumption that more
   conditions make a more specific type, knowingly allowing false positives. Every operation
   other than `and`, `or`, `not` and `if` counts 1, whatever it means; `and` counts the sum of
   its conditions, `or` the fewest of any of its conditions (it only holds as narrowly as its
   broadest branch), `not` 1, and `if` its condition plus the fewer of its two branches'. A
   constant counts 0, and so does a type without a selector; built-in types count 1.
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
* An extension's animator is an animator of its own: it can `extends` another, and it has its own
  `animator.` scope, not that of the animator it extends, so extensions never see or pollute each
  other's names or the extended animator's. What it follows of the mob is what the entity gives
  every animator: the built-ins, and the `entity.` names its model definition declares, a
  contract between a mob's files and the extensions written for it.
* An extension that fails to load (it reads an `entity.` name the model doesn't declare, or its
  file is broken) is left out, with an error shown once: the type's animator and the other
  extensions still animate. A name has no fallback; an extension is written for a mob's files.
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
