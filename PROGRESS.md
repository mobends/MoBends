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
