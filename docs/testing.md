# Testing: the Animation Lab

`animation-lab/` is a standalone Gradle project (modern JDK, no Forge) that replays the mod's
animators against scripted entities and compares every bone, every frame, with recorded *golden*
traces. How to run it and add coverage is in [`animation-lab/README.md`](../animation-lab/README.md);
this page is about what the goldens mean.

## What the Goldens Are

The goldens were recorded from the mod's original, hand-written animation code (controllers and
animation bits) before it was replaced by animator assets and deleted. They are now the only
record of how every entity animated in 1.2.2, and the gate (`./gradlew test`) keeps every animator
within 0.1° / 0.01 model units of them. A golden may only change when an animator is changed on
purpose, and the commit should say why.

The lab compiles the Minecraft-free sources (`core/`, the data classes) against small stubs of
the Minecraft classes they touch. It cannot cover client code: mutators and renderers, resource
pack discovery, the settings screens and the server sync are only checked in game.

## The One Golden That No Longer Matches 1.2.2

The wolf already ran on the first version of KUMO in 1.2.2, so `wolf/idle_walk_sit` was
re-recorded from the new engine three times on purpose: non-looping clips now reach their last
keyframe (the last three frames of standing up); transitions are decided before posing, so a
state change shows on the frame it happens; and the old movement node sampled `limbSwing` one
frame late, while the walk now reads the current value (up to 9.5° on a leg). Every other
golden is the original code's output.

## Where the Animators Deliberately Differ From the Original Code

No scenario covers these, because the new behaviour was chosen over the original quirk:

* After sleeping, the original action layer stayed cleared until the held item type changed; the
  animators resume actions on waking.
* A use action started on the same tick as an item switch showed the tool action in the original
  (the attack check ran last); the animators prefer the use action.
* When the active hand changes during an unchanged use action, the original kept the old hand;
  the animators follow the hand.

## Stubs and Determinism

* `MathHelper.sin` / `cos` in the stubs replicate the game's 65536-entry table exactly, so baked
  and generated curves match the game; `atan2` is exact instead of the game's approximation.
* The mod uses unseeded `Random`s for variation (e.g. the zombie's walking state) and entity ids
  pick the zombie's animation set; the lab seeds the former and resets the latter (`Determinism`)
  so every scenario replays identically.
