# Moving Mo' Bends fully to KUMO — feasibility study and plan

Status: proposal (static analysis of the 1.2.2 codebase on `1.X/forge-1.12`; nothing here has been run in-game yet).

## 0. Verdict

Moving every animation to KUMO assets while keeping today's look is **feasible**, but not by
emulating the smooth vectors and quaternions *purely* with crossfade transitions. The smoothing
in `SmoothOrientation` / `SmoothVector3f` is a per-bone exponential low-pass filter that is
re-targeted every frame, and a large share of the animations feed it *continuously moving*
targets (head look, limb swing amplitude, momentum, IK). A fixed-duration crossfade can only
reproduce the filter's response to a *step* change, and even then only with one rate per
connection instead of one rate per bone.

The recommendation is therefore:

1. Keep the existing filter math, but make it a first-class KUMO concept ("damping": a
   per-bone smoothness table on nodes/layers). KUMO then writes *targets* into the bones instead
   of hard-setting them. With damping = old smoothness and transition duration = 0, the output
   is bit-for-bit what the procedural bits produce today.
2. Keep KUMO's crossfade transitions as an authoring tool on top (they compose with damping),
   and fix them so they transition from a snapshot of the current output pose (no pops when a
   transition is interrupted).
3. Add a small, registry-based set of *driver nodes* for the genuinely procedural remainder
   (look-at, two-bone IK, momentum tilt, cape, ladder facing), parameterised from JSON.
4. Migrate entity by entity by **baking** the old bits into `.bendsanim` clips and verifying
   against recorded golden poses, not by hand-authoring.

Rough size: KUMO core work is the smallest part (a few thousand lines touched); the long tail is
migrating ~4,100 lines of procedural code across 50 bit/controller files for 7 entity types and
proving parity. Phased estimates are in section 8.

## 1. What exists today

### 1.1 Frame pipeline (client render tick)

1. `DataUpdateHandler.updateAnimations` (RenderTickEvent START) computes `ticksPerFrame`
   (clamped 0..1) and calls `EntityDatabase.updateRender` → `EntityData.update` →
   `updateParts(ticksPerFrame)` → every bone's `SmoothOrientation.update` / `SmoothVector3f.update`.
2. During entity rendering, `EntityBender.applyMutation` → `Mutator.performAnimations` →
   `IAnimationController.perform(data)`. Controllers pick bits via `HardAnimationLayer`s and bits
   write **targets** into the bones (`orientX`, `rotateZ`, `slideY`, ...).
3. `BendsPackPerformer` runs the equipped bends pack's `KumoAnimatorState` on top (this is the
   only place a *user* can override animation today).
4. `Mutator.syncUpWithData` copies the data transforms into the render `ModelPart`s, which render
   `rotation.getSmooth()`.

Consequence: on frame N the rendered pose is the smoothed value computed from the targets that
were written on frame N-1.

### 1.2 Exact semantics of the smoothing primitives

`SmoothOrientation` holds `start`, `end`, `smooth`, `progress`, `smoothness`.

| Call | Effect |
|---|---|
| `orient*(a)` | `start = smooth; end = axisAngle(a); progress = 0` |
| `rotate*(a)` | `end = R(a) * end` (world-space pre-multiply); start/progress untouched |
| `localRotate*(a)` | `end = end * R(a)` |
| `orientInstant*` / `rotateInstant*` | snap: `start = smooth = end` |
| `finish()` | snap to `end` |
| `orientZero()` | `orient` to identity |
| `update(dt)` | `progress = min(progress + dt * smoothness, 1); smooth = normalize(nlerp(start, end, progress))` |

Because every procedural bit calls `orient*` on its bones **every frame**, `progress` is reset to
0 each frame and the effective per-frame update is

```
smooth_N = normalize( nlerp( smooth_{N-1}, target_{N-1}, ticksPerFrame_N * smoothness ) )
```

i.e. an exponential moving average toward the target with rate `smoothness` per tick
(≈ `1 - e^{-smoothness · t}` step response). `smoothness = 1.0` is *not* instant: at 60 fps it
moves one third of the remaining distance per frame. Only `orientInstant`/`finish` are instant.

`SmoothVector3f` is different: `slideTo(target, s)` only restarts (`start = current, completion = 0`)
when the target *changes*, so for a step target it is a **linear tween** of duration `1/s` ticks,
and for a continuously changing target it degenerates into the same exponential filter.
`setX/Y/Z` snap a component.

Two more properties matter for parity:

* A bone that no bit writes to this frame keeps interpolating toward its last target and then
  holds it (e.g. `TorchHoldingAnimationBit` returns early when no torch is held).
* `smoothness` is itself animated in places: `FallingAnimationBit` ramps it from 0 to 0.9 over
  80 ticks, `SwimmingAnimationBit` keeps its own `transformTransition` and mixes it into targets.

Usage across `standard/animation` + Kotlin (grep counts): 340 `orient*`, 254 `rotate*`,
299 `setSmoothness` (values 0.05 … 1.0, `.3F` alone 106 times), 37 `orientInstant`,
27 `localRotate`, 51 `orientZero`, 28 `slideTo`, 20 `slideY`, 13 `finish()`, 17 `onPlay`
overrides.

### 1.3 What a "transition" is today

There is no crossfade anywhere in the procedural path. `HardAnimationLayer.playBit` just swaps
`performedBit` (`previousBit` is stored and never used). The transition *is* the bone filter: the
new bit starts writing new targets and each bone converges at the rate the **destination** bit
asked for. Different bones in the same state use different rates (walk: arms 0.8, legs 1.0,
fore-legs 0.5, body 0.5; jump: arms 0.05). Some bits additionally pre-pose in `onPlay`
(`JumpAnimationBit` snaps with `orientInstant`, `StandAnimationBit` sets a crouch when landing
recently, `SpiderDeathAnimationBit` resets its wiggle state).

### 1.4 KUMO as it stands (`core/kumo`, ~1,800 lines)

Works today (the wolf uses it via `bends/animators/wolf.json` plus `.bendsanim` clips; bends
packs use the same template classes):

* `AnimatorTemplate` → layers (`KEYFRAME`, `DRIVER` stub) → nodes (`core:standard`,
  `core:movement`) → connections with `triggerCondition`, `transitionDuration`, `transitionEasing`.
* Conditions: `core:or/and/not/state/ticks_passed/equipment_name/animation_finished`, addon
  registrable (`mobends:wolf_state`).
* `KeyframeLayerState.update`: reset affected bones to rest, write
  `(1-t)·prevNodePose + t·currentNodePose` (eased), advance **all** nodes, then evaluate the
  current node's connections. Bones are addressed by name via `EntityData.getPartForName`
  (`IModelPart`, or `SmoothOrientation` for `renderRotation`/`centerRotation`, or
  `SmoothVector3f` for `root`/`globalOffset`).
* Loaders: JSON and binary `.bendsanim` (`BinaryAnimationLoader`), resource + pack registries.

Latent issues found while reading it (all worth fixing before building on it):

| # | Issue | Where |
|---|---|---|
| K1 | `"additive": true` in `wolf.json` is silently ignored; `KeyframeLayerTemplate` has no such field. The second wolf layer is really a masked override layer. | `KeyframeLayerTemplate` |
| K2 | Rotation blending is done by `set(0,0,0,0)` then component-wise `add()` of weighted quaternions; the result is only normalised when the bone later hits `updateSmooth()`/`finish()`. No hemisphere (dot-product sign) check, so antipodal keyframes/nodes blend through zero and flip. | `KeyframeLayerState.applyRestPose/applyKeyframeAnimation`, `KeyframeUtils` |
| K3 | An interrupted transition drops the in-progress blend: `previousNode` becomes the node being left, so the pose pops to that node's un-blended pose. | `KeyframeLayerState.update` |
| K4 | Non-looping nodes clamp `progress` to `duration-2`, so the final keyframe is never shown at full weight, and `animation_finished` fires one frame early. | `StandardKeyframeNode` |
| K5 | All nodes advance every frame even when inactive; `start()` resets them so it is harmless today, but it is wasted work and will matter for driver nodes with state. | `KeyframeLayerState.update` |
| K6 | `getPartForName` string lookups and `entrySet` iteration per bone per frame. Fine for a wolf, wasteful at scale; bones should be bound once at instancing. | `KeyframeLayerState` |
| K7 | `TicksPassedCondition` uses the global `DataUpdateHandler.getTicks()`, which restarts on world change (`onTicksRestart`). Node-local time is safer. | `TicksPassedCondition` |
| K8 | `DriverLayerState` is an empty stub; `KumoVariableRegistry` registers variables that nothing reads. | `core/kumo/state`, `core/kumo/variable` |
| K9 | Keyframe interpolation is linear in quaternion components with no normalisation between frames (fine for dense Blender exports, wrong for sparse hand-authored keys). | `applyKeyframeAnimation` |
| K10 | KUMO output hard-sets bones (`finish()`), so a pack layer that targets a bone kills the procedural smoothing on it. This is the exact behaviour the migration must change. | `KeyframeUtils.tweenVectorAdditive`, `SmoothOrientation.add` |

## 2. Can the smoothing be emulated with blending and transitions?

### 2.1 Where it can

For a **step** change of target from pose A to pose B at t = 0 the filter gives
`B + (A − B)·(1 − s·dt)^(t/dt) ≈ B + (A − B)·e^(−s·t)`. A KUMO crossfade of duration D with easing
`e(u)` gives `A + (B − A)·e(t/D)`. These coincide if the crossfade starts from a **snapshot of the
current output pose** (not from the previous node's live animation) and uses an exponential easing
`e(u) = 1 − e^(−s·D·u)` with D ≈ 4/s ticks (98 % converged). So: add an `EXPONENTIAL` easing and
snapshot-based transitions, and every "state switch" in the old code that only sets constant poses
(stand ↔ sneak ↔ riding, attack stance, fist guard, …) maps cleanly to a connection.

### 2.2 Where it cannot

1. **Continuously moving targets.** Head look (`headYaw/headPitch` appear 119 times), walk-cycle
   targets sampled from `limbSwing` (120 uses), momentum-driven flying/swimming, ladder ledge
   height, spider IK. The filter is doing per-frame low-pass work here; there is no transition to
   crossfade. Dropping the filter changes the feel (it is what gives the mod its "weighty" lag).
2. **Per-bone rates inside one state.** One connection has one `transitionDuration`. The old code
   has up to four different rates converging simultaneously in a single state switch.
3. **Interrupted transitions.** Jump → land → walk within a few ticks happens constantly. The
   per-bone filter handles this trivially because its state lives in the bone; KUMO today pops (K3).
4. **Time-varying smoothness** (`FallingAnimationBit`, `SwimmingAnimationBit`) and instant snaps
   in `onPlay` (`JumpAnimationBit`) have no crossfade equivalent.
5. **Bones left untouched hold their last target** and keep converging; a keyframe layer that
   resets affected bones to rest every frame cannot express "leave this bone alone unless X".

### 2.3 Resolution: damping as a KUMO primitive

Make the layer/node output a *target pose* and keep a per-bone damping stage that is exactly the
old `SmoothOrientation.update` math:

```
output[bone] = normalize(nlerp(output[bone], target[bone], dt * damping[bone]))
```

with `damping` from a per-node table (`"damping": { "default": 0.3, "leftArm": 0.8, ... }`),
`"snapOnEnter": ["body", "rightLeg"]` for the `orientInstant` cases, and optional damping
*curves* keyed to a variable for the two time-varying cases. Vectors get the same treatment with
the `SmoothVector3f` rule (restart only on target change) so `globalOffset` tweens stay identical.

This is cheaper to implement than a faithful crossfade emulation, is exactly the old behaviour
when transition durations are 0, and composes with crossfades (crossfade produces the target,
damping filters it) so keyframe-authored packs get both tools.

Trade-off to decide (section 9): the old filter is slightly frame-rate dependent
(`(1 − s·dt)^(1/dt)` varies with `dt`). Keeping it verbatim preserves the look at the frame rates
players actually run; switching to `1 − e^(−s·dt)` is "more correct" but visibly different at
20–30 fps. Recommendation: keep verbatim, expose the exact form as a global option later.

## 3. Inventory: procedural idioms → required KUMO feature

| Idiom in the bits | Examples | KUMO feature needed | Have it? |
|---|---|---|---|
| Constant pose per state | stand legs/forearms, riding, sneak arm angles, torch arm, fist guard | standard node, 1-frame clip | yes |
| Idle oscillation on global ticks | `cos(ticks/10)` breathing, falling flail, swim hover, riding bob | looping clip at fixed speed | yes (phase differs, see 9) |
| Cycle driven by `limbSwing` | walk, sprint, sneak walk, skeleton/pigzombie walk, spider move, ladder (`climbingCycle`) | `core:movement` node with a configurable parameter source | partly (hard-coded to `limbSwing`) |
| Amplitude scaled by `limbSwingAmount` | every walk bit | blend weight from a variable, or additive layer with weight | no |
| Additive on top of another state | sneak `localRotateX` body / head compensation, cape on top, torch arm | additive compositing (quaternion multiply, weighted) | no (K1) |
| Head look with compensation and clamps | 119 uses; `head = pitch − bodyX, yaw − bodyY`, `body.z = clamp(yaw·0.1)` | look-at driver (per-axis scale, offset, clamp, "subtract parent") | no |
| Time since event | `ticksAfterAttack/10`, `ticksAfterTouchdown·0.15` kneel, `ticksInAir·0.1`, `ticksFalling` | one-shot nodes started by event conditions; node time = clip time | conditions missing (`attacked`, `touchdown`, `liftoff`, `motion_y_sign`) |
| Combo / alternation state | `SwordAction.moveId` (5 slashes, reset after 20 ticks), `PunchingAction` alternating fists, `sprintJumpLeg` | plain node graph (`animation_finished`, `ticks_passed`, event) | yes |
| Mirroring by main hand / side | `handDirMtp`, `mainArm/offArm` swaps, `sprintLegSwitch` | node/clip `mirror` flag with a bone-name map (`KeyframeAnimation.mirrorRotationYZ` exists) | partial |
| Item / equipment gating | `BipedActionController` use/attack action types, torch, bow draw count | conditions `item_use_action`, `item_attack_action`, `held_item`, `item_use_count` | partial (`equipment_name`) |
| Entity state gating | sleeping, riding (living or not), elytra > 4 ticks, climbing, in water, underwater, flying, sneaking, sprinting, health ≤ 0, `isBesideClimbableBlock` | more `core:state` values | partial |
| Momentum-driven pose | flying (`forwardMomentum`, `yMomentumAngle`), swimming, sprint-jump lean (`motionY`) | driver with variable inputs, or 1D/2D blend node over variables | no |
| Root/entity transforms | `renderRotation` to ladder facing, `centerRotation` pitch in flight, `globalOffset` bob, `localOffset` | already addressable by name; needs driver for ladder/riding yaw math | partial |
| Two-bone IK against ground | spider `putLimbOnGround`, `SpiderData.Limb` world-anchored feet | IK driver node with per-limb state kept in Java | no |
| Cape physics | `CapeAnimationBit` (vanilla chasing-pos maths) | dedicated driver | no |
| Sword trail, item rotations | `swordTrail.add/reset`, `renderRightItemRotation` | node "effects" hook (`onEnter`/`onUpdate` callbacks) or a driver | no |
| Bit-local counters | `SpiderDeath` wiggle decay, `Swimming.transformTransition`, `SprintJump.relax` | node-local variables (`sinceEnter`) + damping curves | no |
| Shared bits across entities | `WalkAnimationBit<T>` used by zombie, skeleton, pigzombie, zombie villager | animator `extends` / layer includes | no |
| Actions reported to packs | `getActions()` strings (`walk`, `attack_slash_up`, `swimming_deep`) | node `tags`, exposed as `core:action` condition and to packs | no |

Entities: player (largest: 4 layers + action controller, ~25 bits), zombie (+ lean/stumble
set), skeleton, pig zombie, spider (IK), squid, wolf (KUMO already, plus a head/tail driver in
`WolfController`). Zombie villager is registered-out but has a controller.

## 4. Target architecture (KUMO v2)

### 4.1 Pose pipeline

```
for each layer (in order):
    target = layer.evaluate(ctx)                 # Pose: bone -> (Quaternion, Vec3 offset, Vec3 globalOffset)
    output = composite(output, target, layer.mode, layer.mask, layer.weight)   # override | additive
damped = damp(damped, output, dampingTable, dt)   # old SmoothOrientation / SmoothVector3f math
write(damped -> EntityData parts as targets, finished)
```

* `Pose` is a flat array indexed by a `BoneIndex` resolved once per (animator, entity data
  class) at instancing (fixes K6). Unbound bones are skipped with a validation warning.
* Rotation blending uses nlerp with hemisphere correction (fixes K2), slerp optional.
* Layer `evaluate` for keyframe layers: transition source is a **pose snapshot** taken when the
  transition starts (fixes K3); easing gains `EXPONENTIAL`.
* Additive compositing: `q = slerp(identity, layerQ, weight) * baseQ`, offsets added (fixes K1).
* `damp` is per bone; bones with no entry use the layer default; `snapOnEnter` writes through.
* Bones the animator never mentions are left untouched (preserves 1.2-style "hold").

### 4.2 Node types

* `core:standard` (fix K4/K5; add `tags`, `damping`, `snapOnEnter`, `mirror`, `timeSource`:
  `ticks` | `globalTicks` for phase-synchronised idles).
* `core:movement` generalised: `parameter` (`limbSwing`, `climbingCycle`, `crawlProgress`, any
  variable), `amplitude` variable with clamp (replaces the `limbSwingAmount` scaling).
* `core:blend1d`: blend between N clips by a variable (momentum, `motionY`, bow draw).
* `core:driver` (new layer type or node type; registry like `TriggerConditionRegistry`):
  `core:look_at`, `core:copy_rotation` (with scale/offset/clamp, e.g. body Z from head yaw),
  `core:two_bone_ik`, `core:momentum_tilt`, `mobends:cape`, `mobends:ladder_facing`,
  `mobends:spider_limbs`, `mobends:sword_trail`. Drivers may keep per-entity state in the
  `EntityData` (as `SpiderData.Limb` already does). Parameters are JSON with variable references.

### 4.3 Variables and conditions

* `IKumoContext.getVariable(name)` backed by the existing `KumoVariableRegistry` plus entity
  data: `limbSwing`, `limbSwingAmount`, `headYaw`, `headPitch`, `swingProgress`, `motionX/Y/Z`,
  `forwardMomentum`, `sidewaysMomentum`, `ticksInAir`, `ticksAfterTouchdown`, `ticksAfterAttack`,
  `ticksFalling`, `climbingCycle`, `ledgeHeight`, `itemUseCount`, node-local `sinceEnter`.
* Conditions to add: `core:event` (`attacked`, `touchdown`, `liftoff`, `ticks_restart`),
  `core:compare` (variable op constant), `core:item_use_action`, `core:item_attack_action`,
  `core:held_item`, `core:main_hand`, `core:action` (tag of another layer's current node), and the
  missing `core:state` values (`SNEAKING`, `RIDING`, `RIDING_LIVING`, `SLEEPING`, `ELYTRA`,
  `CLIMBING`, `IN_WATER`, `UNDERWATER`, `FLYING`, `DEAD`, `BESIDE_CLIMBABLE`).
* Expression language: **defer**. A fixed driver/condition set covers every idiom in section 3;
  expressions can come later behind the same variable interface.

### 4.4 Assets and packs

* `bends/animators/<entity>.json` becomes the mod's default animator per entity key; the built-in
  set is loaded as an implicit base pack, so `BendsPackData.targets` and defaults share one code
  path and the `IAnimationController` interface reduces to "run this animator + drivers".
* Add `"formatVersion"` to `.bends`, animators and `.bendsanim` headers; keep the v1 loader.
* `"extends": "mobends:bends/animators/biped.json"` with layer-level override/append so the four
  biped entities share one base.
* F10 "Refresh Animations" already clears `AnimationLoader`/`GsonResources` caches; make animators
  re-instance on refresh for live iteration.

## 5. Worked example: biped locomotion layer

Today (`WalkAnimationBit`, abridged):

```java
data.rightArm.rotation.setSmoothness(0.8F).orientX(cos(limbSwing + PI) * armSwingAmount).rotateZ(5);
data.rightLeg.rotation.setSmoothness(1.0F).orientX(-5F + cos(limbSwing) * legSwingAmount).rotateZ(2);
data.body.rotation.setSmoothness(0.5F).orientY(cos(limbSwing) * -20F).rotateX(cos(2*limbSwing)*5F + 3F).rotateZ(-clamp(headYaw*0.1, -10, 10));
data.head.rotation.setSmoothness(0.5F).orientX(headPitch - bodyRotationX).rotateY(headYaw - bodyRotationY);
data.globalOffset.slideY(cos(limbSwing * 2) * 0.6F);
```

Target (sketch, not final schema):

```json
{
  "formatVersion": 2,
  "layers": [
    {
      "type": "KEYFRAME",
      "entryNode": "stand",
      "damping": { "default": 0.5, "rightArm": 0.8, "leftArm": 0.8, "rightLeg": 1.0, "leftLeg": 1.0 },
      "nodes": {
        "stand": { "type": "core:standard", "animationKey": "mobends:bends/animations/biped_stand.bendsanim",
                   "looping": true, "timeSource": "globalTicks", "tags": ["stand"],
                   "connections": [
                     { "target": "walk", "triggerCondition": { "type": "core:state", "state": "MOVING_HORIZONTALLY" } },
                     { "target": "jump", "triggerCondition": { "type": "core:state", "state": "AIRBORNE" } } ] },
        "walk":  { "type": "core:movement", "animationKey": "mobends:bends/animations/biped_walk.bendsanim",
                   "parameter": "limbSwing", "playbackSpeed": 0.6662,
                   "additive": { "animationKey": "mobends:bends/animations/biped_walk_swing.bendsanim",
                                 "weight": { "variable": "limbSwingAmount", "scale": 1.0, "max": 1.0 } },
                   "tags": ["walk"], "connections": [ "..." ] },
        "jump":  { "type": "core:standard", "animationKey": "mobends:bends/animations/biped_jump.bendsanim",
                   "snapOnEnter": ["body", "rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg"],
                   "damping": { "rightArm": 0.05, "leftArm": 0.05 }, "tags": ["jump"], "connections": [ "..." ] }
      }
    },
    { "type": "DRIVER", "nodes": [
        { "type": "core:look_at", "bone": "head", "pitch": "headPitch", "yaw": "headYaw", "subtractBone": "body", "damping": 0.5 },
        { "type": "core:copy_rotation", "bone": "body", "axis": "Z", "from": "headYaw", "scale": -0.1, "min": -10, "max": 10 } ] },
    { "type": "KEYFRAME", "mode": "ADDITIVE", "mask": { "mode": "INCLUDE_ONLY", "includedParts": ["body", "head"] },
      "entryNode": "off", "nodes": { "off": "...", "sneak": { "animationKey": "biped_sneak_lean.bendsanim", "parameter": "limbSwing" } } }
  ]
}
```

`biped_walk.bendsanim` holds the constant part of the pose (`-5°` legs, `+3°` body, `±5°` arm
splay) and `biped_walk_swing.bendsanim` the cosine cycle sampled over one `limbSwing` period; the
baker in section 6 produces both from the existing bit.

## 6. Migration process: bake, record, compare

The bits are the specification. Rather than hand-authoring clips, build three small tools (all
usable from the dev client; the maths parts are plain Java and unit-testable):

1. **Golden recorder.** Drive an `EntityData` with a scripted input timeline (the previewers
   already have the override hooks: `overrideOnGroundState`, `overrideStillness`,
   `limbSwing.override`, `overrideFlyingState`, …) through the *old* controller for N frames at
   a fixed `ticksPerFrame`, and dump per-frame, per-bone quaternions/offsets to JSON.
2. **Baker.** Run a bit with a stub `EntityData` whose bones report their *targets* (`end`,
   not `smooth`) and sample: over one parameter period for cycle bits, over M ticks for idle
   bits, over the event timeline for one-shots. Emit `.bendsanim` plus the damping table (read
   straight from the `setSmoothness` calls) and the `snapOnEnter` list (from `onPlay`).
3. **Comparator.** Run the new animator on the same script and report max/mean angular error per
   bone per state and at every transition. Acceptance: < 0.5° steady state, transitions within
   one frame of the golden curve. Wire the wolf into this first, since it is the regression
   baseline for the KUMO core changes.

In-game: a "ghost" toggle in the previewer that renders the old controller's output semi-
transparent behind the KUMO output for side-by-side inspection.

Migration order (simple → hard, each behind a per-entity config toggle with the old controller as
fallback until the comparator passes): wolf (regression only) → zombie → skeleton → pig zombie →
squid → player → spider.

## 7. Compatibility rules

* Existing `.bends` packs keep loading (v1 loader); v1 animators behave as before except the
  K2/K3/K4 fixes, which are bug fixes.
* `getActions()` strings become node tags with the same names so packs keyed on them keep working.
* Addon API: `AddonAnimationRegistry.registerTriggerCondition` stays; add `registerDriver`,
  `registerVariable`, `registerAnimator`. Addons that subclass `AnimationBit` keep working until
  the procedural path is removed (last phase).
* Bone names (`nameToPartMap`) are the asset contract; do not rename.

## 8. Phased plan

| Phase | Scope | Exit criteria | Size |
|---|---|---|---|
| 0. Toolchain | JDK 8 + Gradle 4.9 build green locally and in CI (`build.yml` already uses Temurin 8); JUnit for `core/math` and `core/kumo` without Minecraft classes (extract `IPose`/`IBoneSink` interfaces so `KeyframeLayerState` does not need `EntityData`). | `./gradlew build` and tests pass | S |
| 1. KUMO core v2 | Pose pipeline, bone binding, hemisphere-correct nlerp, snapshot transitions + `EXPONENTIAL` easing, additive/override compositing with masks and weights, damping stage with per-bone tables and `snapOnEnter`, node tags, K4/K5/K7 fixes, format version. | Wolf animator renders identically to 1.2.2 (golden comparator); wolf head/tail maths moved to drivers or left in `WolfController` unchanged | M |
| 2. Drivers, variables, conditions | Driver registry and the drivers/conditions/variables listed in 4.2–4.3; `core:movement` generalisation; `core:blend1d`; mirror flag; animator `extends`. | Each driver has a unit test against the formula in the corresponding bit | M |
| 3. Tooling | Golden recorder, baker, comparator, previewer ghost, F10 re-instancing. | Zombie stand/walk/jump baked and passing comparator end to end | M |
| 4. Entity migration | Zombie → skeleton → pig zombie → squid → player → spider, each behind a config toggle; player attack combo, item actions, flying, swimming, ladder, cape are the bulk. | All entities pass comparator; old controllers deleted; addon docs updated | L (player and spider dominate) |
| 5. Unify packs and defaults | Built-in animators become the base pack; pack editor/format docs; schema doc for animator JSON. | A user pack can replace or layer on any default animator without Java | S–M |

Sizes are relative (S ≈ days, M ≈ 1–3 weeks, L ≈ 1–2 months of focused work). Phases 1–3 are
sequential; phase 4 entities are independent once 3 lands and can be parallelised.

## 9. Open decisions for you

1. **Filter fidelity vs frame-rate independence** (section 2.3): keep the verbatim
   `(1 − s·dt)` update, or switch to `1 − e^(−s·dt)`? Recommendation: verbatim now, option later.
2. **Global-tick idle phase**: today every biped breathes in sync because idles use
   `DataUpdateHandler.getTicks()`. Node-local time desynchronises them (arguably nicer). The
   `timeSource` option keeps both; pick a default.
3. **Where procedural code is allowed to remain**: the plan keeps IK, cape and ladder maths in
   Java drivers parameterised from JSON. If you want *zero* Java per entity, an expression
   language is the next step; I would not start there.
4. **Zombie villager**: its controller exists but registration is commented out. Migrate or drop.
5. **Kotlin**: `JumpAnimationBit.kt` shows the intended direction. New KUMO core in Kotlin or
   Java? Either works with the shadow-relocated stdlib already in the build.

## 10. What was not verified

* Nothing was compiled or run here: the container only has JDK 21 and the project needs JDK 8
  with ForgeGradle 2.3. All findings come from reading the source; the comparator in phase 3 is
  what turns them into checked facts.
* The exact per-frame ordering in 1.1 was derived from `DataUpdateHandler`, `EntityBender` and
  `Mutator` and should be confirmed with a frame trace before relying on the "N-1 target" rule in
  the comparator.
