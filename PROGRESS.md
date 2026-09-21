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
