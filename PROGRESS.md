# Progress: asset-driven animation (KUMO) migration

Companion to [`misc/kumo-migration-plan.md`](misc/kumo-migration-plan.md). Each entry is one
committed, self-contained step. Newest at the bottom.

## Ground rules

* The mod's original procedural animation code is **the reference**. It is never edited for
  behaviour; the lab compiles it verbatim and records what it does.
* Every change to the animation core has to keep `animation-lab` green:
  `cd animation-lab && gradle test`.
* Golden traces are regenerated only on purpose (`gradle record`), and the reason is written here.

## 1. Animation lab: reference harness and golden traces

**What.** A standalone Gradle project (`animation-lab/`, JDK 21, no Forge) that:

* compiles the mod's animation sources straight from `../src/main` against ~50 tiny stubs of the
  `net.minecraft` / `org.lwjgl` classes they touch (`src/mcstub`), plus seven shims for mod
  classes that need the client (`DataUpdateHandler`, `AnimationLoader`, `GsonResources`,
  `SwordTrail`, `SupporterContent`, `ModConfig`, `Core`) — the reference code itself is untouched;
* drives scripted entities through the mod's exact per-frame pipeline order (client tick →
  `EntityData.update` → vanilla model inputs → `controller.perform`) with a lab clock that feeds
  the same global timing statics the game does;
* captures every named part (smoothed rotation, rotation *target*, offsets, entity-level
  vectors) per frame into a `PoseTrace`, stored gzipped under `golden/<entity>/<scenario>.json.gz`;
* compares traces with a numerically stable quaternion angle metric and prints a per-bone report;
* has 18 scenarios across all 7 animated entity kinds (biped locomotion, jumping, head look,
  attacks, sword combo, torch, bow, eating, flying, swimming, wolf sit/walk, spider IK, squid).

**Result.** `ReferenceStabilityTest` re-records all 18 scenarios and matches the committed
goldens (18/18). Two nondeterminism sources in the reference were pinned in the lab only: entity
ids (they select the zombie animation set) and the unseeded `Random` in `ZombieDataBase`.

**Known approximations in the stubs.** `MathHelper.sin/cos` replicate the game's 65536-entry
table exactly; `MathHelper.atan2` is exact instead of the game's approximation (only
`FlyingAnimationBit` uses it).

**How to use.**

```
cd animation-lab
gradle test            # reference stability (and later: KUMO parity)
gradle record          # regenerate golden traces from the reference code
gradle record --args="golden zombie"   # one entity only
```

## 2. KUMO core v2: Minecraft-agnostic pose pipeline

**What.** `core/kumo` was rewritten around a pose buffer instead of writing straight into bones.
Public entry points used elsewhere in the mod (`KumoSerializer.INSTANCE.gson`, `AnimatorTemplate`,
`LayerTemplate.validate`, `KumoAnimatorState`, `TriggerConditionRegistry.register`) kept their
shape. Existing animator JSON (wolf, bends packs) loads unchanged.

* **Subject abstraction.** `IKumoSubject` is all KUMO knows about an entity: `getBone(name)` →
  `IRotationSink` / `IVectorSink`, plus named numeric variables and boolean states. `EntityData`
  implements it; each data class registers what it exposes (`registerVariable`, `registerState`),
  e.g. `limbSwing`, `headYaw`, `ticksAfterTouchdown`, `animationSet`, `SITTING`. No KUMO class
  imports Minecraft any more except the player-only `EquipmentNameCondition`.
* **Pose pipeline.** Layers evaluate into a `Pose` (per-bone rotation / offset / vector targets,
  damping, snap flags, bound once to sinks by index); layers composite in order as `OVERRIDE` or
  `ADDITIVE` (`PRE` = `rotate*`, `POST` = `localRotate*`, per bone); the final pose is written
  once per frame. Writes go through `SmoothOrientation.target()` / `SmoothVector3f.retarget()`,
  i.e. the exact smoothing math of the procedural code, so **damping is a KUMO property**:
  per-node `damping` tables (unlisted bone = keep its current rate, like a bit that never calls
  `setSmoothness`), `snapOnEnter` for the `orientInstant` idiom, `RETARGET` / `SLIDE` / `SNAP`
  vector modes.
* **Format 2 nodes** (`"type": "core:pose"`): an ordered *pose stack* of clips and drivers, each
  with a time source (`elapsed`, or a variable such as `limbSwing` / `ticks` / `ticksInAir`), a
  weight (`limbSwingAmount`-style amplitude scaling, exact for single-axis rotations), a
  composition space and an optional `when` condition (the in-bit `if` blocks). Nodes are named,
  carry `tags` (the old `getActions()` strings) and connections address targets by name.
* **Drivers.** `DriverRegistry` (`core:axis_rotate` so far: bone, axis, angle from a variable
  with scale/offset/clamp, PRE or POST) for the procedural remainder; addons register their own.
* **Conditions.** `core:compare` (variable op constant); `core:state` now resolves any subject
  state by name; `core:ticks_passed` uses the layer clock instead of the global one (K7).
* **Clips.** Optional `duration` / `loop` / `interpolation` (`STEP`) metadata on
  `KeyframeAnimation` for format-2 clips.

**Bugs fixed** (numbers from the plan): K1 additive layers now exist; K2 hemisphere-correct nlerp
instead of summing into a zero quaternion; K3 interrupted transitions blend from a snapshot of
the on-screen pose instead of popping; K4 non-looping clips reach their last keyframe and
`animation_finished` fires at the true end; K5 only active nodes advance; K6 bones bound by
index; K7 as above; K8 the dead `DriverLayerState` / `NodeAnimationLayer` stubs are gone.

**Legacy fidelity.** `core:standard` / `core:movement` keep their exact old playback rules,
including two quirks the lab uncovered: the movement node samples `limbSwing` one frame late
(its progress was updated after the pose was written) and legacy nodes hard-set bones (no
smoothing). With those replicated, the wolf scenario matches the 1.2.2 output on every frame
except the three frames at the end of `wolf_standing_up` where K4 applies (max 5.4° on a fore
leg, half a tick). The wolf golden was re-recorded with that change; from now on it guards the
legacy-node path of the new core.

**Verification.** 18/18 scenarios pass; all seven procedural entities are bit-identical to the
goldens (the `EntityData` changes are additive), the wolf differs only as described above.

## 3. First migrated entity: the zombie runs on KUMO with parity

**What.** `bends/animators/zombie.json` (format 2) plus 18 baked clips under
`bends/animations/zombie/` reproduce `ZombieController` and its five bits (stand, walk, jump,
lean, stumbling). `KumoParityTest` replays the three zombie scenarios with the animator in place
of the controller and compares against the golden traces.

**Result.** All three scenarios within tolerance (0.1° / 0.01 units): worst rotation error
0.027°, worst offset error 0.0026 units, over 630 frames including stand ↔ walk ↔ jump switches,
landing kneels, mid-walk jumps, head look, and both zombie animation sets.

**How the mapping works** (this is the recipe for every other biped):

* *Cycles* (walk, stumble) are baked over the limb-swing phase and split into a constant part
  (`walk_base`), a stepped part (`walk_forelegs`, `STEP` interpolation, sampled at interval
  midpoints), and an amplitude part (`walk_swing`, the POST-space delta between amplitude 0 and 1,
  played with `weight = limbSwingAmount`). The baker verifies the decomposition is linear
  (0.0000° nonlinearity for every clip).
* *Idles on the global clock* (stand breathing) use `time: {variable: "ticks", scale: 0.1}`.
* *One-shots on entity counters* (landing kneel over `ticksAfterTouchdown`, jump body lean over
  `ticksInAir`) are clips whose time source is that variable, gated by a `when` condition.
* *Head look* is two `core:axis_rotate` drivers (yaw PRE, pitch POST) around the clip's head
  pose; the walk body twist is a third driver with scale −0.1 and a ±10° clamp.
* *`onPlay` snaps* become `enterPose` (jump: body 20°, arms ±2°, forearms −20°; stand: body 20°
  when landing recently).
* *Per-bone smoothness* is copied from the `setSmoothness` calls into item-level `damping`;
  unlisted bones keep their rate, as in the bits. Vector writes carry their mode (`SLIDE` for
  `slideToZero`, `RETARGET` for per-frame bobs, `SNAP` for `setY`).
* *Animation sets* are two extra layers with `when: animationSet == 0/1`: lean is `ADDITIVE`
  (body in POST space, everything else PRE, root overridden), stumble overrides.
* Discontinuities (the stumble sawtooth) are handled by explicit keyframe `times`: the baker
  bisects to the jump and inserts a keyframe pair around it (69 keyframes instead of a dense
  ramp).

**Core additions in this step.** Item-level `damping` / `vectorModes`, `enterPose`, layer `when`,
per-slot composition spaces (an OVERRIDE item inside an additive layer replaces), explicit
keyframe `times`, transitions evaluated before posing (a state change is visible on the frame it
happens, as with `playOrContinueBit`; the wolf golden was re-recorded for this), and a "snap
from" / "vector start" so an entry pose followed by a target in the same frame matches the
`orientInstant`-then-`orient` sequence exactly.

**Lab additions.** `BakeRig` / `Baker` / `ClipBuilder` (sample a bit's *targets* under scripted
inputs; marker rotations and vectors detect which bones a bit actually writes), `bakeZombie`,
`KumoSession`, `compare` CLI, `KumoParityTest`.

**Known gap.** `ZombieLeanAnimationBit` writes only the Y axis of the global offset
(`slideY(-3)`); the animator writes the whole vector with `[null, 0.6, null]` damping, which is
equivalent while the base layer keeps X and Z at zero.

## 4. Skeleton and pig zombie on KUMO; shared biped animator

**What.** The locomotion the three mobs share now lives once in `bends/animators/biped.json`
(clips under `bends/animations/biped/`); `zombie.json`, `skeleton.json` and `pig_zombie.json`
`"extends"` it and add their own layers:

* zombie: the two animation-set layers (unchanged from step 3, paths moved);
* skeleton: a strafing layer (`when: walk && STRAFING`) that overrides the legs with the Z-axis
  swing of `skeleton.WalkAnimationBit`, base + amplitude part as before;
* pig zombie: a hunched-pose layer (body rotated in POST *and* PRE space, the rest PRE, root
  overridden to −3 while standing / a `|sin|` bob while walking) and a slash-attack layer
  (`when: entitySwingProgress > 0`) baked from `AttackSlashInwardAnimationBit` over
  `ticksAfterAttack`, with the standing-still overlay and the every-frame item snap.

The four animator files are generated by `animation-lab/tools/gen_animators.py`
(`gradle generateAnimators`) so the shared structure is written once; clips come from
`gradle bakeBipeds`.

**Core additions.** Rotation slots now keep *pre* and *post* relative parts next to an absolute
value (`pre * beneath * post`), so a bit that does `localRotateX(20).rotateZ(-10)` on one bone
fits in one layer; `core:action` condition (a node tag on any layer); animator `extends`
(`IKumoInstancingContext.getAnimator`); per-item `snap`; a same-frame snap followed by another
vector write restarts the interpolation from the snapped value (the `setY` then `slideY` pattern
of the landing kneel under a lean); the "undamped vector snaps" rule now checks all three axes.

**Scenarios added** (goldens recorded from the reference): `skeleton/strafe`,
`zombie/lean_walk_jump` (forces animation set 0, which the entity-id-derived set never gave us).

**Result.** 12/12 parity scenarios pass: worst rotation error 0.027° (zombie) / 0.012° (others),
worst offset error 0.0026 units. 20/20 reference stability scenarios pass.

**Noted, not fixed (reference behaviour, preserved).** `BipedEntityData.updateParts` advances
`globalOffset`, `renderRotation` and the item rotations twice per frame (once via `super`, once
itself), so those smooth at twice the nominal rate on bipeds. Both paths share the data class,
so parity holds; worth a deliberate decision when the old controllers are retired.

## 5. Player on KUMO, stage A: base, sneak, torch and cape layers

**What.** `bends/animators/player.json` (`extends` the biped animator) now covers every state of
`PlayerController.perform` except the action controller: stand / walk / sprint, jump, sprint jump
(one node per leading leg), falling, creative flight (sprint, hover, moving), swimming (surface and
underwater), ladder climbing (with the ledge pull-up), elytra, riding a living mount, sitting on a
vehicle and sleeping; plus the sneak overlay, the torch-holding layer and the cape. Clips come from
`gradle bakePlayer` (`PlayerBake`); the animator is generated by `gen_animators.py`.

* The controller's if/else chain becomes one connection list per node, in priority order, where
  every branch is guarded by the negation of the branches above it. A node whose own condition
  still holds must never fall through to a lower branch: the first version let `jump` bounce to
  `stand` and back every other frame while airborne in place.
* Bits that mix an absolute pose with the look direction are split by space: e.g. the fast riding
  head is `Rx(-body) * Ry(yaw) * Rx(pitch)`, so the baked `Rx(-body)` part is a PRE-space item on
  the head after the look drivers, and the "moving" clip is restricted to body and arms.
* Ramp variables (`core:ramp`) gained `readBeforeAdvance`: `SwimmingAnimationBit` eases its
  transition value before moving it, `SprintJumpAnimationBit` after; the one-frame difference is
  visible.
* New driver `core:vector` (vector bones fed from value sources, e.g. the swim dive offset) and
  vector bones in the skeleton (`root`, `localOffset`) usable from clips and drivers alike.

**Scenarios added** (goldens recorded from the reference): `player/ladder_climb`,
`player/riding`, `player/sleep_sit_elytra`; `player/fly_and_swim` now dives into deep water.

**Lab additions.** `PlayerBake`, `Baker.curve` (sample a bit over a parameter sweep),
`BakeRig.ride()` / ladder and water columns (`LabWorlds`), scripted mounts and vehicles in
`ScriptedEntity`, `LabBootstrap` registering the cape driver before animators load.

**Result.** 7/7 stage-A player scenarios pass (worst 0.036° / 0.007 units); 18/18 parity
scenarios pass overall, 23/23 stability. `player/sword_combo` and `player/bow_and_eat` are listed
as pending in `KumoParityTest` until the action layer (stage B) lands.

**Known gaps.** Left-handed players are not mirrored yet (clips are baked right-handed).
`PunchAnimationBit`'s fist toggle can desync from `PlayerData.fistPunchArm` after item switches in
the reference; to be decided when the action layer is migrated.

## 6. Player on KUMO, stage B: the action layer

**What.** The `BipedActionController` and its item actions are now a fifth layer of
`player.json`, between the torch and cape layers: sword combos (`SwordAction`: five slashes in
order, the still and sprinting stances, the 20-tick combo window), bare-fist punches
(`PunchingAction`: alternating fists, the guard) and the tool swing, plus the use actions eating,
bow drawing and shield blocking, one node per active hand so an off-hand shield or snack plays
on the correct arm. The reference's per-action-instance state (`moveId`, `lastTicksAfterAttack`,
`punchingFist`, the bring-up ramps) is expressed as layer variables (`combo`, `fist`) set by the
connections that fire, `core:decreased` edge triggers on `ticksAfterAttack`, node-local ramps
and a `core:set` driver that clears the combo once enough ticks have passed. A switch of item
type re-enters the family through its entry connections (a fresh instance in the reference),
which reset the variables and pick the stance if its window is already open.

* Slashes are baked (`PlayerBake.bakeActions`) with the look direction wrapped around the baked
  head part; the two clocks of `AttackStanceAnimationBit` (`sin(t/5)`, `cos(t/5.7)`) are
  incommensurate, so its breathing is two generated clips composed in PRE space.
* Fist guard, punches, sprint stance, the tool swing curves (dense samples where
  `sin(sqrt(p))` is steep), eating and bow curves are generated analytically in
  `gen_animators.py` from the bits' formulas, with Minecraft's table sine/cosine.
* An action bit's `slideY()` on the global offset restarts every frame because the base layer
  re-slides the same vector; that is an exponential approach, `RETARGET` in the animator.

**Core fixes found by the parity runs.**
* `And`/`Or`/`Not` conditions did not forward `onNodeStarted` to their children and
  short-circuited, so a nested `core:decreased` missed frames; they now evaluate every child.
* Connection evaluation stops at the first match but still evaluates every condition, for the
  same reason.
* Driver items ignored their `when` (only clips and `core:axis_rotate` honoured it); the node now
  wraps any conditional driver (`ConditionalPoseItem`). A ramp's `when` stays its direction switch.
* `ClipSampler` interpolated keyframe quaternions without hemisphere correction: a track that
  crosses the ±180° wrap (the whirl slash's full spin) took the long way round for one frame.
* Connections gained `set` (variables assigned when they fire); `core:ramp` gained
  `readBeforeAdvance`; new `core:set` driver.

**Scenarios.** `player/bow_and_eat` re-recorded: the item is now switched to before it is used,
and use counts follow vanilla (`itemInUseCount` counts down, `getItemInUseMaxCount()` is the
ticks used). New: `player/sword_moves` (combo timeout, sprinting stance, a hit in the air),
`player/punch_and_tool` (punches still and walking, pickaxe swings still and sneaking),
`player/offhand_use` (off-hand shield, slash, off-hand eating).

**Result.** 23/23 parity scenarios pass (worst 0.036° / 0.007 units), 26/26 stability. Nothing
is pending in `KumoParityTest`.

**Deliberate deviations (documented, not replicated).**
* After sleeping, the reference's action layer stays cleared until the held item type changes
  (`clearAction` does not reset the type memo); the animator resumes actions on waking.
* A use action started on the same tick as an item switch shows the tool action in the reference
  (both memos change at once and the attack check runs last); the animator prefers the use action.
* A change of active hand during an unchanged use action keeps the original hand in the
  reference; the animator follows the hand.

**Known gaps.** Left-handed players (the attack bits mirror on the primary hand); the bow's
climbing branch; `ToolAction` only (no separate axe/pickaxe poses exist in the reference either).

## 7. Squid and spider on KUMO

**Squid.** `bends/animators/squid.json`: the vanilla tentacle wave as two generated curve clips
over the interpolated squid rotation (`squidRotation`), each with a rest variant chosen by the
controller's two "not yet wrapped" checks (`SQUID_ROTATION_LOW`, `SQUID_PREV_ROTATION_LOW`).
The tentacle bases (`tentacle_i_0`) are now named bones like the sections; the golden was
re-recorded to include them. Worst error 0.0007°.

**Spider.** The legs are stateful inverse kinematics (each foot remembers a world position,
steps to a neutral spot when overstretched, lifts while it moves), so they stay code: two
drivers, `mobends:spider_idle_legs` and `mobends:spider_moving_legs`, parametrised from
`bends/animators/spider.json` (bob and ground-level value sources, the landing bounce, the
eight limbs' gait table, the "feeling the ground" cadence). The IK itself moved to
`SpiderLegIk`, shared with the old controller. Everything else is data: the jump (a fan-out
pose plus per-leg drivers on the vertical motion), the death (an instant splay, two sway clips
weighted by the limb swing amount, and a wiggle whose speed decays through a
`core:accumulate` phase), crawling (the moving gait on the crawl progress, the wall-facing
render rotation), and the controller's decision chain with a `resetLimbs` layer variable that
the jump's exits set so the feet are re-planted on landing, as `resetAfterJumped` did.

**Core additions.** Value sources take a shaping function (`sin`, `cos`, `mcsin`, `mccos`,
`abs`) and a post multiplier/offset, so a bit's `sin(ticks * 0.1) * 0.5` is one expression;
`core:accumulate` (a node variable that integrates a rate, clamped); ramps take an `initial`
value; a `core:vector` item may leave axes out, which keeps the bone's current target on those
axes (the `slideY()` idiom); drivers expose computed values as node variables (`groundLevel`).

**Scenarios added.** `spider/death`, `spider/crawl` (goldens recorded from the reference).

**Result.** 27/27 parity scenarios pass (spider worst 0.004°), 28/28 stability.

## 8. The mod animates from the assets; zombie villager; sword trail

**What.** `KumoAnimatorController` (core) is an `IAnimationController` that is nothing but an
animator asset: it loads `bends/animators/<entity>.json` through `AnimatorResources` (clips via
`AnimationLoader`, parent animators for `extends` via `GsonResources`), updates the animator
state every frame and returns the current nodes' tags as the actions bends packs see. A broken
asset logs once and animates nothing rather than crashing the render. `ZombieData`,
`ZombieVillagerData`, `SkeletonData`, `PigZombieData`, `PlayerData`, `SpiderData` and
`SquidData` now return it; the procedural controllers stay in the tree as the parity reference
(the lab's `EntityKind` instantiates them explicitly, so the goldens no longer depend on what
the data classes animate with). The wolf keeps its existing KUMO controller with the extra
head and tail code.

* `bends/animators/zombie_villager.json` simply `extends` the zombie's (the two controllers were
  identical); the lab gained the `ZOMBIE_VILLAGER` kind and its three biped scenarios.
* The one side effect the bits had beyond bone targets, the sword trail, is a
  `mobends:sword_trail` driver (feed, clear on node entry, clear each frame, velocity offset)
  placed in the slash and sprint-stance nodes. `SideEffectParityTest` checks the trail is fed
  and cleared on exactly the same frames as by the reference.

**Result.** 30/30 parity scenarios, 31/31 stability, side effects equal.

**Not done here.** The mod cannot be built in this environment (ForgeGradle needs Java 8); the
lab compiles the affected mod sources with `--release 8`, but the first real build of the mod
should be checked in a dev environment. Resource reloads should call
`AnimatorResources.INSTANCE.clearCache()` and each controller's `reload()` (not wired yet).

## 9. Left-handed players: mirroring in data

**What.** The attack bits mirrored themselves through a hand multiplier and a main/off arm
swap. The animator expresses this as a layer rule plus item flags: the player's action layer
declares `mirror: {when: LEFT_HANDED, pairs: [[leftArm, rightArm], ...], negate: [headYaw]}`,
and every item a bit computed with the hand multiplier sets `"mirror": true`. Such an item is
evaluated on the mirror image of the pose (paired bones swapped, Y and Z rotations and X offsets
negated) with the yaw-like inputs negated, and the result is mirrored back, which is an
involution, so the item's own composition rules are untouched and a look driver comes out
right (the look direction is a world input, the body twist is not). Parts a bit applied to its
main hand *without* the multiplier (the stance's breathing sways, the tool swing's arm curves)
set `"swapSides": true` instead: they change arm but keep their sign. The still-standing legs
of the slashes and the fist guard are left alone, as the bits did.

**Scenario added.** `player/left_handed` (sword combo, stances, a pickaxe, bare fists; golden
recorded from the reference with the primary hand set to LEFT). Worst error 0.035°.

**Also.** Resource reloads now clear the animator cache (`MoBends.refreshSystems`).

**Result.** 31/31 parity scenarios, 32/32 stability, side effects equal.

## 10. The wolf: last procedural code into data

**What.** `WolfController` was already a KUMO animator plus hand-written look, shake and tail
code. That code is now a third, additive layer of `bends/animators/wolf.json`: snapping
POST-space drivers on the head (look, interested tilt, shake), mane (shake) and tail (shake,
wag, vanilla tail rotation), fed by variables `WolfData` derives from the vanilla wolf
(`interestedAngle`, `shakeAngleHead/Mane/Tail`, `tailRotation`, `tailWag`, all in degrees), plus a
`core:offset` item that discards the clips' head offset as the controller did. The pup's head
scaling and position are model setup, not animation, and moved into `WolfData.update`. The two
clip layers moved to `wolf_clips.json`, which the animator `extends` and which the reference
`WolfController` keeps loading, so the reference never sees the new layer. `WolfData` animates
through `KumoAnimatorController`: every entity of the mod now animates from its asset.

**Scenario added.** `wolf/interest_and_shake` (golden from the reference).

**Result.** 33/33 parity scenarios (both wolf scenarios exact), 34/34 stability, side effects
equal.

## 11. Continuous validation and documentation

* `.github/workflows/animation-lab.yml` runs the lab's tests (reference stability, animator
  parity, side effects) on JDK 21 for every push or pull request that touches the animation
  sources, the assets or the lab, and uploads the reports and the fresh traces on failure.
* `animation-lab/README.md` explains the lab; `misc/kumo-format.md` documents the format-2
  animator (layers, nodes, items, drivers, value sources, conditions, mirroring);
  `animation-lab/tools/trace_diff.py` prints per-frame deviations between a golden and an
  animator trace.

## Where things stand

Every entity of the mod (player, zombie, zombie villager, skeleton, pig zombie, spider, squid,
wolf) animates from `assets/mobends/bends/animators/*.json` through `KumoAnimatorController`.
33 scenarios pin the behaviour to the procedural reference within 0.1° / 0.01 units (most
within 0.04°). The procedural bits and controllers remain in the tree only as that reference.

Open items, in order of value:
1. Build and run the mod itself in a Forge dev environment (not possible here); watch for
   resource-reload behaviour and the bends-pack path (`BendsPackPerformer` reads the actions).
2. Decide the documented deviations of step 6 (the reference's post-sleep and same-tick quirks)
   and whether `BipedEntityData.updateParts` should keep smoothing the offsets twice per frame.
3. The bow's climbing branch and the pig zombie / skeleton left-handed cases have no scenario.
4. Retire the procedural code once the assets have been seen in-game; the lab then needs the
   goldens only.

## 12. Closing the coverage gaps

* **Skeletons draw bows and swing swords.** `SkeletonController` ran the `BipedActionController`
  too; `skeleton.json` now carries the player's action layer (the `useActionType` /
  `attackActionType` properties moved from `PlayerData` to `BipedEntityData`). Scenarios
  `skeleton/bow_and_sword` and `skeleton/bow_and_sword_left_handed`.
* **Left-handed mobs.** Vanilla mobs can be left-handed; the pig zombie's slash layer has the
  mirror rule now (`pig_zombie/walk_attack_left_handed`), and the skeleton's actions inherit the
  player's.
* **The torch follows the primary hand** (`player/right_handed_torch_offhand`, and a torch phase
  at the end of `player/left_handed`).
* **The bow on a ladder**: the body faces the wall (`climbingBodyYaw`), the head only pitches
  (`player/ladder_bow`).
* **A slash from the saddle** (`player/riding` re-recorded with a sword hit).
* **Core:** a vector target replaced by a different one within the same frame restarts the
  slide on those axes (`BoneTarget.restartX/Y/Z`, `IVectorSink.restartSlide`), which is exactly
  what two `slideTo()` calls did. The action layer's offsets are plain `SLIDE`s again and match
  whether or not the base layer writes the same vector that frame.

**Result.** 40/40 parity scenarios (worst 0.036° / 0.007 units), 40/40 stability, side
effects equal. Every hand-dependent branch of the reference now has a left- and a
right-handed scenario.
