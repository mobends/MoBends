# Content: Types, Extensions, Model Definitions

Everything that decides how an entity looks and moves is assets, found in the
`assets/<namespace>/bends/` folder of the mod, any other mod, or any resource pack. The file
formats are in [`misc/kumo-format.md`](../misc/kumo-format.md); this page is how the pieces fit.

| Asset | Folder | Loaded by |
|-------|--------|-----------|
| Animators | `bends/animators/` | `AnimatorResources` (`core/kumo`) |
| Clips | `bends/animations/` | `AnimationLoader` (`core/animation/keyframe`) |
| Entity types | `bends/types/` | `EntityTypeRegistry` (`core/types`) |
| Extensions | `bends/extensions/` | `EntityTypeRegistry` (`core/types`) |
| Model definitions | `bends/models/` | `ModelDefinitions`, `DefinedBenders` (`core/definition`, `core/client/definition`) |

## Entity Types

A *type* decides which model and animator an entity gets, under a selector condition (entity
type, player name or UUID, skin variant, and whatever addons register through
`AddonAnimationRegistry.registerSelectorCondition`). Every entity bender registered in code also
gets a built-in type, so a type file only adds or overrides.

When several types match an entity, the winner is picked by (`TypeOrder`):
1. the user's rank (Settings → *Order*, stored in the client config),
2. the number of conditions (more specific first),
3. the id.

Entity data is keyed by the type's data factory, so an entity that changes type gets fresh data.
An untrusted type (see below) keeps the default model instead of its own model definition.

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

* `DefinedEntityData` — one transform per bone, the declared variables, the animator;
* `DefinedMutator` — copies each vanilla part's boxes into bends parts, splits them
  (`BoxSplitter`), applies rest rotations, and replaces every field or array slot that held the
  vanilla part, found by identity so it works in an obfuscated game;
* `DefinedRenderer`, registered for every definition in `bends/models/index.json` by
  `DefinedBenders`.

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
{"animationKey": "mobends:bends/animations/biped/stand.json",
 "frame": {"mod": [{"mul": ["ticks", 0.1]}, "clipLength"]}}
```

Use `elapsed` instead of `ticks` when the motion should start from the beginning each time the
node is entered.

### A walk cycle that follows the legs

Drive the clip by the distance walked, not by time, so feet never slide; `0.6662` is the rate
vanilla legs swing at. Split the cycle into the pose that is always there and the swing, and
weight the swing by how fast the entity walks, so a slow walk swings less:

```json
{"animationKey": "…/walk_base.json",  "frame": {"mod": [{"mul": ["limbSwing", 0.6662]}, "clipLength"]}},
{"animationKey": "…/walk_swing.json", "frame": {"mod": [{"mul": ["limbSwing", 0.6662]}, "clipLength"]},
 "weight": "limbSwingAmount", "space": "POST"}
```

`weight` scales angles, which is exact for rotations about one axis; keep swings to one axis per
bone, or accept that a mixed swing bends slightly at partial weights.

### Looking where the entity looks

Put the look on top of the clip with two drivers, yaw in the parent's space and pitch in the
head's own space, after any clip that poses the head:

```json
{"driver": "core:axis_rotate", "bone": "head", "axis": "Y", "angle": "headYaw",   "space": "PRE"},
{"driver": "core:axis_rotate", "bone": "head", "axis": "X", "angle": "headPitch", "space": "POST"}
```

A slight body twist towards the look is a third driver on `body` with
`{"clamp": [{"mul": ["headYaw", -0.1]}, -10, 10]}`.

### A one-shot on an entity event

For something that follows an event the entity counts (landing, attacking, taking off), play a
clip with the counter as its frame, only while the counter is small:

```json
{"animationKey": "…/kneel.json", "frame": "ticksAfterTouchdown",
 "when": {"type": "core:compare", "variable": "ticksAfterTouchdown", "op": "<", "value": 6.67},
 "vectorModes": {"root": "SNAP"}}
```

The clip holds its last keyframe once the frame passes its end, so the `when` decides when it
lets go. Counters include `ticksAfterTouchdown`, `ticksInAir`, `ticksAfterAttack`.

### Starting a state in a pose

`enterPose` items are evaluated once when the node is entered and their bones snap there, so the
node's own pose then smooths away from that starting pose (a jump starts crouched, then extends):

```json
"jump": {"enterPose": [{"animationKey": "…/jump_enter.json"}], "pose": [ … ]}
```

`snapOnEnter: ["body"]` snaps the listed bones straight to the node's own pose instead.

### How fast bones follow

The animator sets targets; when a bone's target changes, the bone moves there from where it is
and arrives after 1 / damping ticks (1 = within a tick, 0.1 = in ten). Set it per node or item:
`"damping": {"body": 0.5, "rightArm": 0.8, "root": [null, 0.6, null]}`. A bone you don't list keeps
the rate it had, so set damping where a state should feel different, not everywhere. For the
entity-level vectors (`root`, `localOffset`) pick a `vectorModes` entry: `SLIDE` for a move to a
new resting place, `RETARGET` for a target that changes every frame (a bob), `SNAP` for no
smoothing at all.

### Choosing between states

A node's connections are checked in order and the first one met fires, so list them by
priority. Make sure a node never leaves while its own reason to be there still holds: `jump`
only goes to `stand` when the entity is on the ground *and* has been for a tick *and* is still,
otherwise it would bounce between the two while hopping in place. Use `transitionDuration` (ticks)
to crossfade; an interrupted crossfade continues from what is on screen.

### Variants of one animation

For per-entity variation (the zombie's two walking styles), add a layer per variant, gated by a
variable the data class sets, and let it override or add to the base:

```json
{"mode": "ADDITIVE",
 "additiveSpace": {"default": "PRE", "body": "POST", "root": "OVERRIDE"},
 "when": {"type": "core:compare", "variable": "animationSet", "op": "==", "value": 0},
 "entryNode": "lean", "nodes": { … }}
```

### Combos and other sequences

To react to each new attack, use `core:decreased` on `ticksAfterAttack` (it drops to 0 on every
swing) and count with layer variables set by the connection that fires:

```json
{"target": "slash_up",
 "triggerCondition": {"type": "core:and", "conditions": [
   {"type": "core:decreased", "variable": "ticksAfterAttack"},
   {"type": "core:compare", "variable": "combo", "op": "==", "value": 0}]},
 "set": {"combo": 1}}
```

Declare the variables on the layer (`"variables": {"combo": 0}`), and reset them with a
`core:set` driver or another connection's `set` once the combo window has passed.

### Left- and right-handed

Author for the right hand, then give the layer a mirror rule and mark the items that depend on the
hand:

```json
"mirror": {"when": {"type": "core:state", "state": "LEFT_HANDED"},
           "pairs": [["leftArm", "rightArm"], ["leftForeArm", "rightForeArm"]],
           "negate": ["headYaw"]}
```

An item with `"mirror": true` plays as its mirror image for left-handed entities. Use
`"swapSides": true` for motion that only moves to the other arm without flipping (a breathing
sway of the main arm).

### Easing a motion in and out

A `core:ramp` driver is a node variable that moves from 0 to 1 while its `when` holds and back
otherwise; use it as a later item's `weight` to raise an arm over a few ticks instead of snapping:

```json
{"driver": "core:ramp", "name": "raise", "speed": 0.1,
 "when": {"type": "core:state", "state": "SNEAKING"}},
{"animationKey": "…/raise.json", "weight": "raise"}
```

`core:accumulate` integrates a rate instead (a phase that slows down as it decays), and a
procedural motion can skip clips entirely: a driver's `angle` is any expression, e.g.
`{"mul": [{"sin": [{"mul": ["elapsed", 0.5]}]}, 25]}`.

### An overlay from a resource pack

An extension adds layers on top of an existing animator. Start the layer on a `core:fallthrough`
node, so the entity's own animation shows, and crossfade into your node when it applies:

```json
"nodes": {
  "through": {"type": "core:fallthrough",
              "connections": [{"target": "wave", "transitionDuration": 8,
                               "triggerCondition": {"type": "core:state", "state": "STANDING_STILL"}}]},
  "wave": {"pose": [ … ], "connections": [ … back to "through" … ]}
}
```

`misc/examples/wave-extension` is the complete pack. To hand the entity back to vanilla instead
(for animations made for the vanilla model), use a `core:vanilla` node.

### Clips with a sudden jump

Keyframes are interpolated, so a motion that snaps (a stumble resetting) needs two keyframes
around the snap at almost the same time, using explicit keyframe `times` in the clip file,
rather than many keyframes approximating it.
