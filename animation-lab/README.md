# Animation lab

A standalone Gradle project (JDK 17+, no Forge) that replays the mod's animation code against
scripted entities and checks that the KUMO animator assets reproduce the procedural
controllers frame by frame. It is the safety net for moving Mo' Bends to data-driven animation:
the procedural controllers are the *reference*, the animator assets are what the mod runs.

The mod's Minecraft-agnostic sources (`core/kumo`, `core/math`, `core/data`, the animation bits,
controllers and data classes) are compiled straight from `../src` with `--release 8`, against
small stubs of the Minecraft classes they touch (`src/mcstub`) and shims of the mod's loaders
(`src/reference`). Nothing in here ships with the mod.

## Tasks

| task | what it does |
|---|---|
| `gradle test` | The gate. `ReferenceStabilityTest` re-records every scenario from the procedural code and checks it still matches its golden (detects accidental changes to the reference). `KumoParityTest` runs every scenario through the entity's animator asset and checks it stays within 0.1° / 0.01 model units of the golden. `SideEffectParityTest` checks the sword trail is fed on the same frames. |
| `gradle compare --args="$PWD/golden [entity[/scenario] ...]"` | Prints the parity report per scenario and writes the animator's traces to `build/kumo-traces/` for `tools/trace_diff.py`. Add `-Dlab.debugNodes=true` to print every layer's current node per frame. |
| `gradle record --args="$PWD/golden [entity/scenario ...]"` | Re-records goldens from the reference. Only for new scenarios, or when a change to the reference is intended (say so in `PROGRESS.md`). |
| `gradle bakeBipeds`, `gradle bakePlayer` | Sample the procedural bits into clips under `src/main/resources/assets/mobends/bends/animations/` (`BipedBake`, `PlayerBake`). |
| `gradle generateAnimators` | Regenerates the animator JSON files and the hand-authored clips from `tools/gen_animators.py`. |

Typical loop after touching an animator or the core:

```
gradle generateAnimators
gradle compare --args="$PWD/golden player"
python3 tools/trace_diff.py player/sword_combo rightArm --from 80 --to 100
gradle test
```

## How a frame is replayed

Both sessions (`ReferenceSession`, `KumoSession`) drive the same `ScriptedEntity` (a small
vanilla-like integrator: gravity, drag, limb swing, arm swing, ladders, water, mounts) at a fixed
frame rate, and per frame do what the mod does: tick the entity, `data.updateClient()`,
`data.update(partialTicks)`, feed the vanilla model inputs, run the animation (the entity kind's
procedural controller, or the animator), then capture every named bone's smoothed rotation and
target, offsets and the entity-level offset vectors into a trace. Determinism comes from reset
entity ids and seeded `Random`s (`Determinism`).

`Scenarios` is the catalogue; `Scripts` has the input helpers. A scenario is a kind, a name, a
frame rate, a tick count and a script `(tick, inputs) -> ...`. Goldens live in
`golden/<entity>/<scenario>.json.gz` (gzipped JSON, six decimals).

## Comparing

`PoseComparator` measures the rotation error as the angle between the two quaternions
(`2·atan2(|a−b|, |a+b|)`, sign-insensitive) and the offset error per component. The report
lists, per bone, the worst and mean error and the frame it happened on. `trace_diff.py` then
shows the actual values around that frame; `-Dlab.debugNodes=true` shows which nodes were
active.

## Baking

`Baker` samples a bit's *targets* (not its smoothed output) on a rig whose bones start on
marker values, so it knows exactly which bones and vector axes the bit wrote. It produces
looping cycles (`cycle`, `stepCycle` for stepped forelegs, `adaptiveCycle` for sawtooth
tracks: it bisects to each jump and emits explicit keyframe `times`), parameter sweeps
(`curve`), one-shots (`oneShot`), and the part of a pose that scales with an amplitude
(`amplitudeDelta`, checked for linearity). Constant and analytic poses are written by
`gen_animators.py` directly (`pose_clip`, `cycle_clip`, `curve_clip`), using Minecraft's table
sine so they match the bits bit for bit.

## Adding coverage

1. Add a scenario in `Scenarios` (and inputs in `EntityInputs` / `ScriptedEntity` if the entity
   needs new state).
2. `gradle record --args="$PWD/golden <entity>/<scenario>"`.
3. `gradle compare --args="$PWD/golden <entity>/<scenario>"`, fix the animator (or the core) until
   it is within tolerance, `gradle test`, note it in `PROGRESS.md`.

New entities need an `EntityKind` (entity stub factory, data factory, and the procedural
controller that is the reference) and an entry in `Animators`.
