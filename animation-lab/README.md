# Animation lab

A standalone Gradle project (JDK 21, no Minecraft, no loader; run with its own wrapper, `./gradlew`) that replays
the mod's animators against scripted entities and checks their bone poses frame by frame against
recorded *golden* traces.
The goldens were first recorded from the mod's original procedural animation code, so they pin
down how every entity animated before the move to data; since that code is gone, a golden only
changes on purpose: an animator changed, or the modern code doing deliberately what 1.12 didn't (see
`docs/testing.md`).

The mod's animation code (the entity data, the KUMO glue, the model definitions, the types and the
components) is compiled straight from `../src` (the modern mod) and `../core/src` (the engine),
against stand-ins for the Minecraft 1.21.1 classes it touches, under their Mojang names
(`src/mcstub`), and shims for the mod's classes that need the game's client or a loader (`src/mod`).
Nothing in here ships with the mod. The scripts in `tools/` are TypeScript run with [Bun](https://bun.sh).

## Tasks

| task | what it does |
|---|---|
| `./gradlew test` | The gate. `KumoParityTest` runs every scenario through the entity's animator asset and checks it stays within 0.1° / 0.01 model units of the golden. `SideEffectParityTest` checks the sword trail is fed on the same frames. The other tests cover expressions, clips, entity types, extensions, server limits (`AnimationLimitsTest`) and model definitions. |
| `./gradlew compare --args="$PWD/golden [entity[/scenario] ...]"` | Prints the parity report per scenario and writes the animator's traces to `build/kumo-traces/` for `tools/trace_diff.ts`. Add `-Dlab.debugNodes=true` to print every layer's current node per frame. |
| `./gradlew record --args="$PWD/golden entity[/scenario] ..."` | Accepts the animator's current output as the golden of the named scenarios. Only for new scenarios, or when a change to an animator is intended (say why in the commit, and see `docs/testing.md`). |
| `./gradlew record --args="--legacy $PWD/golden/legacy entity[/scenario] ..."` | The same for the Legacy Mo' Bends pack (`../misc/legacy-pack`), whose goldens `LegacyPackTest` checks. |
| `./gradlew generateAnimators` | Regenerates the animator JSON files and the hand-authored clips from `tools/gen_animators.ts`. |

Typical loop after touching an animator or the core:

```
./gradlew generateAnimators
./gradlew compare --args="$PWD/golden player"
bun tools/trace_diff.ts player/sword_combo rightArm --from 80 --to 100
./gradlew test
```

## How a frame is replayed

`KumoSession` drives a `ScriptedEntity` (a small vanilla-like integrator: gravity, drag, limb
swing, arm swing, ladders, water, stone to climb onto, mounts) at a fixed frame rate, and per frame does what the mod
does: tick the entity, `data.updateClient()`, `data.update(partialTicks)`, feed it what the
renderer passes the model (as the mod's `BendsRenderer` captures it), run the animator, then capture every named bone's smoothed rotation and target, offsets
and the entity-level offset vectors into a trace. Determinism comes from reset entity ids and
seeded `Random`s (`Determinism`).

`Scenarios` is the catalogue; `Scripts` has the input helpers. A scenario is a kind, a name, a
frame rate, a tick count and a script `(tick, inputs) -> ...`. Goldens live in
`golden/<entity>/<scenario>.json.gz` (gzipped JSON, six decimals).

## Comparing

`PoseComparator` measures the rotation error as the angle between the two quaternions
(`2·atan2(|a−b|, |a+b|)`, sign-insensitive) and the offset error per component. The report
lists, per bone, the worst and mean error and the frame it happened on. `trace_diff.ts` then
shows the actual values around that frame; `-Dlab.debugNodes=true` shows which nodes were
active.

## Adding coverage

1. Add a scenario in `Scenarios` (and inputs in `EntityInputs` / `ScriptedEntity` if the entity
   needs new state).
2. Check the animator does what it should (`./gradlew compare`, `trace_diff.ts`), then
   `./gradlew record --args="$PWD/golden <entity>/<scenario>"`.
3. `./gradlew test`.

New entities need an `EntityKind` (entity factory and data factory), an entry in `Animators`, and a
stand-in for their class in `src/mcstub` (and its `EntityType`).
