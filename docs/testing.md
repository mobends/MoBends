# Testing: the Animation Lab

`animation-lab/` is a standalone Gradle project (JDK 21, no Minecraft, no loader) that replays the mod's
animators against scripted entities and compares every bone, every frame, with recorded *golden*
traces. How to run it and add coverage is in [`animation-lab/README.md`](../animation-lab/README.md);
this page is about what the goldens mean.

## What the Goldens Are

The goldens were recorded from the mod's original, hand-written animation code (controllers and
animation bits) before it was replaced by animator assets and deleted. They are now the only
record of how every entity animated in 1.2.2, and the gate (`./gradlew test`) keeps every animator
within 0.1° / 0.01 model units of them. A golden may only change on purpose (an animator changed,
or the modern code doing deliberately what 1.12 didn't, see below), and the commit should say why.

The lab compiles the engine (`core/`) and the mod's animation code from `src/` (the entity data,
the KUMO glue, the model definitions, the types) against stand-ins for the Minecraft 1.21.1 classes
they touch. It cannot cover client code: the renderers and the bent models, resource pack discovery,
the settings screens and the server sync are only checked in game.

## The Goldens That No Longer Match 1.2.2

The wolf already ran on the first version of KUMO in 1.2.2, so `wolf/idle_walk_sit` was
re-recorded from the new engine three times on purpose: non-looping clips now reach their last
keyframe (the last three frames of standing up); transitions are decided before posing, so a
state change shows on the frame it happens; and the old movement node sampled `limbSwing` one
frame late, while the walk now reads the current value (up to 9.5° on a leg). Both wolf scenarios were
re-recorded once more when the tail took vanilla's angle: the vanilla tail angle is now measured
from the idle clip's tail (72.93°) instead of from 90° (8.5° on the tail, every frame).

The iron golem's goldens were re-recorded as it learned to jump and climb, and the zombie's, the
zombie villager's, the skeleton's and the player's when they came to stand on their feet
(`core:step_turn` takes the legs over whenever they stand still, and the player's and the
skeleton's only where nothing else poses them; how they move otherwise is unchanged). The heads of
the player, the zombies, the skeleton and the zombie pigman were re-recorded once more (not the
legacy pack's, which keeps heads that turn with the body) when they
stopped turning with the body (`inheritRotation`): their rotation is now relative to the model, so
the pose they are drawn in changed only where the animators no longer turn them back from the
body's bend, or turn them with it on purpose. When poses became declarative (a bone nothing writes
goes back to rest), the player's ladder and riding goldens (the mod's and the legacy pack's) and
the spider's were re-recorded: the original code left the ladder's lean, the riding dip and the
spider's sway where the last node put them, and the spider's head still while it jumped. The
scenarios where a layer switches on or off (the player's sneak, torch and action layers, the
skeleton's strafe, the zombie pigman's stance and slash) were re-recorded when those layers got a
`@fade`. Every other golden is the original code's output.

Mo' Bends Compat+ (`misc/compat-plus-pack`) has no goldens: `CompatPlusPackTest` loads its extension
on the player's animator with and without the mods it is for installed, and checks it leaves the
player to Mo' Bends while they play nothing. (The lab has no Player Animation Library, so handing the
player to vanilla while it plays is tried in the game.)

The Legacy Mo' Bends pack (`misc/legacy-pack`) is a snapshot of the player's, the zombie's and the
zombie villager's animators without the layer that stands them on their feet, with their model
definitions as they were (heads that turn with the body) and its own copies of every clip they play
(see its README). `LegacyPackTest` holds it to its own goldens, in `golden/legacy/`, recorded with
`./gradlew record --args="--legacy $PWD/golden/legacy <entity>[/scenario]"`, and checks that the pack
names no file it doesn't have. It doesn't follow the mod: a change to the mod's animators leaves it
as it is. The legacy goldens don't match 1.2.2's either. They include the engine changes the mod's
goldens were re-recorded for (smoothing in ticks, a bone's base smoothing, interpolated entity
values).

When the lab moved from the 1.12.2 code to the modern code in `src/`, two sets of goldens were
re-recorded, as the modern code does on purpose what 1.21.1's vanilla does (`forge-1.12` keeps its
own, as 1.12.2 doesn't):

* Every player scenario (the mod's and the legacy pack's), for the cape, over its first 25 frames
  only. `CapeDriver` computes the cape as 1.21.1's `CapeLayer` does, which keeps its sideways sway
  within 20 degrees and its lift within 150; 1.12's didn't. The lab's player starts with its cloak
  position at the origin, half a block from where it stands, so its cape swings out as the scenario
  starts, past those limits.
* `spider/death`, for the legs once it has died. 1.21.1's renderer passes the model no limb swing
  for a dead entity, and the mod animates with what vanilla passes (`BendsRenderer`); 1.12's kept
  the walk's swing.

## Where the Animators Deliberately Differ From the Original Code

No scenario covers these, because the new behaviour was chosen over the original quirk:

* After sleeping, the original action layer stayed cleared until the held item type changed; the
  animators resume actions on waking.
* A use action started on the same tick as an item switch showed the tool action in the original
  (the attack check ran last); the animators prefer the use action.
* When the active hand changes during an unchanged use action, the original kept the old hand;
  the animators follow the hand.

## Stand-ins and Determinism

* `Mth.sin` / `cos` in the stand-ins replicate the game's 65536-entry table exactly, so baked
  and generated curves match the game; `atan2` is exact instead of the game's approximation.
* The stand-ins (`animation-lab/src/mcstub`) are the 1.21.1 classes, under their Mojang names, with
  only what the mod's animation code uses. Their entities are scripted: what the game works out
  itself (touching water, the item in use, a ladder at the feet) the lab sets each tick. Every
  entity is 0.6 by 1.8 blocks but the iron golem and the villagers, as the 1.12 stubs had them.
* The mod uses unseeded `Random`s for variation (e.g. the zombie's walking state) and entity ids
  pick the zombie's animation set; the lab seeds the former and resets the latter (`Determinism`)
  so every scenario replays identically.
