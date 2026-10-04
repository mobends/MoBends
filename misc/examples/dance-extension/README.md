# Example: dancing cows and chickens

A resource pack with two extensions, one for cows (`mobends:cow`) and one for chickens
(`mobends:chicken`), sharing one animator: while standing still, they dance to a beat
of 120 BPM. On every beat the whole body bounces up and sways to the other side, tilting with
the sway, and the head nods. Walking or jumping stops the dance; it fades out and in over
6 ticks. Every dancing mob follows the game's tick clock, so a field of them dances in sync.

* `assets/mobends_dance/bends/extensions/`: the two extensions. An extension extends one type,
  so each mob gets its own file; both point at the same animator, which only uses bones cows and
  chickens both have (`head`, and the entity-level `globalOffset` and `renderRotation`).
* `assets/mobends_dance/bends/animators/dance.json`: the layer. Live definitions on the animator
  (`animator.beat`, `animator.sway`, `animator.bounce`) keep the moves on one clock. The whole-body moves set the offset and
  tilt outright (nothing in the mobs' own animation touches them), so the `rest` node puts them
  back instead of being a `core:fallthrough` node. The nod is a `POST` rotation on top of the
  head the mob's own animation poses.

To try it, copy or link this folder into the game's `resourcepacks` folder and enable it.
