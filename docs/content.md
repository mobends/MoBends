# Content: Types, Extensions, Model Definitions

Everything that decides how an entity looks and moves is assets, found in the
`assets/<namespace>/bends/` folder of the mod, any other mod, or any resource pack. The file
formats are in [`misc/kumo-format.md`](../misc/kumo-format.md); this page is how the pieces fit.

| Asset | Folder | Loaded by |
|-------|--------|-----------|
| Animators | `bends/animators/` | `AnimatorResources` (`core/kumo`) |
| Clips | `bends/animations/` | `AnimatorResources` (`core/kumo`) |
| Entity types | `bends/types/` | `EntityTypeRegistry` (`core/types`) |
| Extensions | `bends/extensions/` | `EntityTypeRegistry` (`core/types`) |
| Model definitions | `bends/models/` | `ModelDefinitions`, `DefinedBenders` (`core/definition`, `core/client/definition`) |

## Entity Types

A *type* decides which model and animator an entity gets, while its selector holds (an
expression over the entity type, the player's name or UUID, the skin variant, and whatever
selector-safe operations addons register: see `misc/kumo-format.md`, *Selectors*). Every entity bender registered in code also
gets a built-in type; the mobs made from model definitions get theirs from the type files in
`assets/mobends/bends/types/`. A type file adds or overrides.

When several types match an entity, the winner is picked by (`TypeOrder`):
1. the user's rank (Settings → *Order*, stored in the client config),
2. the number of conditions (more specific first),
3. the id.

Entity data is keyed by the type's data factory, so an entity that changes type gets fresh data.
An untrusted type (see below) that names its own model definition is ignored while the server
limits resource packs.

## Extensions

Extensions replaced bends packs. An extension names a type and an animator; its animator's layers
are stacked on top of the type's own animator. Extensions for one type are ordered like types,
without the specificity step (user rank from Settings → *Extensions*, then id), and the first in
that order ends up on top, i.e. its layers are added last (`Extension.layerOrder`).

A `core:fallthrough` node poses nothing, and transitions into or out of it fill whatever one side
doesn't pose from the layers below; that is how an extension fades in and out over the animation
it extends. A `core:vanilla` node goes further: while it is current, the entity is drawn by
vanilla (see [rendering.md](rendering.md)).

## Model Definitions (Mobs Without Code)

A model definition names an entity, the vanilla model parts that become bones, optional parents
and rest rotations, and *splits*: a vanilla box cut into segments along an axis, each a further
bone with the matching strip of texture (how the quadrupeds get knees). From a definition the mod
builds:

* `DefinedEntityData` — one transform per bone, the animator, and the entity scope the
  definition's `@define` / `@on` declare (`entity.` names, which only it may compute from the
  entity's fields with `field`; `DefinedEntityFields` resolves them);
* `DefinedMutator` — copies each vanilla part's boxes into bends parts, splits them
  (`BoxSplitter`), applies rest rotations, and replaces every field or array slot that held the
  vanilla part, found by identity so it works in an obfuscated game;
* `DefinedRenderer`, made by `DefinedBenders` for every model definition a type file names.

**Limitation.** Render layers that copy the model's angles (the sheep's wool, the charged
creeper's armour) are not covered and still animate vanilla-style, which is why the sheep has no
definition.

### Vanilla field names in production

Definitions name fields by their development (MCP) names, which don't exist in a production
(SRG) runtime. `src/tools/kotlin/.../GenVanillaFields.kt` (`gradle generateVanillaFields`) reads
the SRG Forge jar and the mappings from `build/fg_cache` and generates `core/vanilla/VanillaModelParts`
and `core/vanilla/VanillaEntityFields`: a string switch from `owner#name` to a lambda that reads
the field directly, so reobfuscation renames every access. Non-public fields are opened by a
generated section of `META-INF/accesstransformer.cfg`, below a marker line. `DefinedFields` looks
a field up in the generated table for each class in the hierarchy first and falls back to
reflection, which is how fields of modded entities and models are found. The build fails when the
generated files were made for another Minecraft, Forge or mappings version (`checkVanillaFields`).

## What Resource Packs May Do

Content is either *trusted* — the mod, other mods, vanilla, and the server's own resource pack —
or *untrusted*: anything supplied by a resource pack the player enabled, including their version
of a trusted file (`PackTrust`). The server decides what untrusted content may do with
`resourcePackAnimation` (see [server-networking.md](server-networking.md)):

| Policy | Effect |
|--------|--------|
| `ALLOW` | Everything is used as is. Always the case in singleplayer. |
| `LIMITED` (default) | Untrusted animation is clamped: every part offset and both body vectors stay within `maxPartOffset` / `maxBodyOffset` of what the trusted animation gives them (`AnimationLimits`); rotations are free. Untrusted model geometry is refused, so only trusted model definitions are served. |
| `DENY` | Only trusted content is loaded. |

`KumoAnimatorState` knows each layer's trust (its declaring file and its clips), notes the pose
before the first untrusted layer and clamps around it after the last. A policy change reloads the
animation (`CoreClient.reloadAnimation()`), and defined mobs re-read their definition, so it takes
effect immediately.

## Authoring Recipes

Patterns the shipped animators use, as a starting point for new ones. Field details are in
[`misc/kumo-format.md`](../misc/kumo-format.md); `bends/animators/biped.json` and `player.json`
show all of them at scale.

### An idle that breathes

Play a clip on the global clock, looped, so every entity breathes even while its node is new:

```json
{"core:clip": {"animationKey": "mobends:bends/animations/biped/stand.json",
               "frame": {"mod": [{"mul": ["ticks", 0.1]}, "clipLength"]}}}
```

Use `nodeTicksElapsed` instead of `ticks` when the motion should start from the beginning each time the
node is entered.

### A walk cycle that follows the legs

Drive the clip by the distance walked, not by time, so feet never slide; `0.6662` is the rate
vanilla legs swing at. Split the cycle into the pose that is always there and the swing, and
weight the swing by how fast the entity walks, so a slow walk swings less:

```json
{"core:clip": {"animationKey": "…/walk_base.json",  "frame": {"mod": [{"mul": ["entityLimbSwing", 0.6662]}, "clipLength"]}}},
{"core:clip": {"animationKey": "…/walk_swing.json", "frame": {"mod": [{"mul": ["entityLimbSwing", 0.6662]}, "clipLength"]},
               "weight": "entityLimbSwingAmount"}, "@space": "POST"}
```

`weight` scales angles, which is exact for rotations about one axis; keep swings to one axis per
bone, or accept that a mixed swing bends slightly at partial weights.

### Looking where the entity looks

Put the look on top of the clip with two drivers, yaw in the parent's space and pitch in the
head's own space, after any clip that poses the head:

```json
{"core:axis_rotate": {"bone": "head", "axis": "Y", "angle": "entityHeadYaw"},   "@space": "PRE"},
{"core:axis_rotate": {"bone": "head", "axis": "X", "angle": "entityHeadPitch"}, "@space": "POST"}
```

A slight body twist towards the look is a third driver on `body` with
`{"clamp": [{"mul": ["entityHeadYaw", -0.1]}, -10, 10]}`.

### A one-shot on an entity event

For something that follows an event the entity counts (landing, attacking, taking off), play a
clip with the counter as its frame, only while the counter is small:

```json
{"@when": {"lt": ["entityTicksAfterTouchdown", 6.67]},
 "core:clip": {"animationKey": "…/kneel.json", "frame": "entityTicksAfterTouchdown"},
 "@vectorModes": {"root": "SNAP"}}
```

The clip holds its last keyframe once the frame passes its end, so the `@when` decides when it
lets go. Counters include `entityTicksAfterTouchdown`, `entityTicksInAir`, `entityTicksAfterAttack`.

### Starting a state in a pose

`enterPose` items are evaluated once when the node is entered and their bones snap there, so the
node's own pose then smooths away from that starting pose (a jump starts crouched, then extends):

```json
"jump": {"core:pose": {"enterPose": [{"core:clip": {"animationKey": "…/jump_enter.json"}}], "pose": [ … ]}}
```

`snapOnEnter: ["body"]` snaps the listed bones straight to the node's own pose instead.

### How fast bones follow

The animator sets targets; when a bone's target changes, the bone moves there from where it is
and arrives after 1 / damping ticks (1 = within a tick, 0.1 = in ten). Set it per node (`damping`
in `core:pose`) or item (`@damping`):
`{"body": 0.5, "rightArm": 0.8, "root": [null, 0.6, null]}`. A bone you don't list keeps
the rate it had, so set damping where a state should feel different, not everywhere. For the
entity-level vectors (`root`, `localOffset`) pick an `@vectorModes` entry: `SLIDE` for a move to a
new resting place, `RETARGET` for a target that changes every frame (a bob), `SNAP` for no
smoothing at all.

### Choosing between states

Write the layer's `select`: a decision tree, most important state first. Each branch only
applies when the ones before it don't, so each branch states only its own condition, and the layer
stays in a node for as long as the tree keeps choosing it:

```json
"@define": {"jumping": {"live": {"or": [{"not": ["entityIsOnGround"]}, {"lt": ["entityTicksAfterTouchdown", 1]}]}}},
"select": [
  {"when": "layer.jumping", "then": "jump"},
  {"when": "entityIsStandingStill", "then": "stand"},
  {"then": "walk"}
]
```

A branch whose `then` is a list decides inside it (the player's airborne states: flying, falling,
sprint-jumping, jumping). Leave out the last `then` to let a node hold on until a branch applies:
a mob that starts walking above one speed and stops below a lower one keeps doing what it did in
between. Name the conditions you use more than once: a live definition in the layer's `@define`,
read as `layer.jumping`.
A condition is an expression that is true or false: a built-in such as `entityIsStandingStill`, a
comparison (`{"lt": ["entityTicksAfterTouchdown", 1]}`), `and`, `or`, `not` (see
`misc/kumo-format.md`, *Expressions*).
Use `transitionDuration` (ticks) on a branch to crossfade; an interrupted crossfade continues from
what is on screen.

What depends on where the layer comes from goes in a node's `connections`, checked when the
selectors keep the layer where it is: a jump starting over when the entity bounces, a clip that
has to finish before the next one (`nodeIsFinished`), a sequence like sitting down,
sitting and standing up.

### Variants of one animation

For per-entity variation (the zombie's two walking styles), add a layer per variant, gated by a
variable the data class sets, and let it override or add to the base:

```json
{"mode": "ADDITIVE",
 "additiveSpace": {"default": "PRE", "body": "POST", "root": "OVERRIDE"},
 "@when": {"eq": ["animationSet", 0]},
 "defaultOnEntry": "lean", "nodes": { … }}
```

### Combos and other sequences

Group the nodes of a sequence in a machine. The layer's selector picks the machine; the machine's
own selector picks where to rest inside it, and its connections, which lead out of any of its
nodes, play the sequence. To react to each new attack, use `decreased` on
`entityTicksAfterAttack` (it drops to 0 on every swing) and count with a state of the layer set by the
connection that fires:

```json
"@define": {"combo": {"state": 0}},
"select": [{"when": {"mobends:attack_action": ["sword"]}, "then": "sword", "do": [{"set": ["layer.combo", 0]}]}],
"machines": {"sword": {
  "defaultOnEntry": "sword_idle",
  "@define": {"attacked": {"live": {"decreased": ["entityTicksAfterAttack"]}}},
  "select": [{"when": {"ge": ["entityTicksAfterAttack", 10]}, "then": "sword_idle"}],
  "@connections": [
    {"when": {"and": ["machine.attacked", {"eq": ["layer.combo", 0]}]}, "then": "slash_up", "do": [{"set": ["layer.combo", 1]}]},
    …
  ],
  "nodes": {"sword_idle": {…}, "slash_up": {…}, …}}}
```

The slashes aren't in the machine's selector, which chooses nothing while a slash plays (the
first ten ticks), so the slash holds until the selector chooses `sword_idle` or the next attack
fires a connection. Reset the count once the combo window has passed, with a statement in an
`update` list: `{"@when": {"gt": ["entityTicksAfterAttack", 20]}, "set": ["layer.combo", 0]}`.

### Left- and right-handed

Author for the right hand, then give the layer a mirror rule and mark the items that depend on the
hand:

```json
"mirror": {"@when": "entityIsLeftHanded",
           "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"]]}
```

An item with `"@mirror": true` plays as its mirror image for left-handed entities. Use
`"@swapSides": true` for motion that only moves to the other arm without flipping (a breathing
sway of the main arm). Leave the items that follow where the entity looks unmarked: the head
turned by `entityHeadYaw` looks the same way for either hand.

### Easing a motion in and out

To raise an arm over a few ticks instead of snapping, weight a later item by a value that goes
from 0 to 1. Over the first ticks of a node, that is a function of the node's clock:
`{"linstep": ["nodeTicksElapsed", 0, 10]}` rises over ten ticks. Name it in the node's `@define` when
several items read it:

```json
"raise": {
  "core:pose": {"pose": [{"core:clip": {"animationKey": "…/raise.json", "weight": "node.lift"}}]},
  "@define": {"lift": {"live": {"linstep": ["nodeTicksElapsed", 0, 10]}}}
}
```

To go up while a condition holds and back down when it doesn't, accumulate a rate whose sign
follows the condition, clamped to 0..1:

```json
"@define": {"raise": {"state": 0}},
"core:pose": {"pose": [
  {"core:accumulate": {"inout": "node.raise", "rate": {"if": ["entityIsSneaking", 0.1, -0.1]}, "min": 0, "max": 1}},
  {"core:clip": {"animationKey": "…/raise.json", "weight": "node.raise"}}
]}
```

`core:accumulate` integrates any rate (a phase that slows down as it decays), and a procedural
motion can skip clips entirely: a driver's `angle` is any expression, e.g.
`{"mul": [{"sin": [{"mul": ["nodeTicksElapsed", 0.5]}]}, 25]}`.

### An overlay from a resource pack

An extension adds layers on top of an existing animator. Start the layer on a `core:fallthrough`
node, so the entity's own animation shows, and crossfade into your node when it applies:

```json
"select": [
  {"when": "entityIsStandingStill", "then": "wave", "transitionDuration": 8},
  {"then": "through", "transitionDuration": 8}
],
"nodes": {
  "through": {"core:fallthrough": {}},
  "wave": {"core:pose": {"pose": [ … ]}}
}
```

`misc/examples/wave-extension` is the complete pack. To hand the entity back to vanilla instead
(for animations made for the vanilla model), use a `core:vanilla` node.

### Clips with a sudden jump

Keyframes are interpolated, so a motion that snaps (a stumble resetting) needs two keyframes
around the snap at almost the same time, using explicit keyframe `times` in the clip file,
rather than many keyframes approximating it.
