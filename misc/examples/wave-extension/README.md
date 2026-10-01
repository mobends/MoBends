# Example: an extension

A resource pack that adds one extension (`mobends_wave:wave`) to the built-in player type: while a
player stands still they raise their right arm and wave; while they move, their animation is
untouched. Unlike a type, an extension doesn't replace the player's animator, it adds layers on
top of it, so it combines with the player's own animation and with other extensions.

* `assets/mobends_wave/bends/extensions/wave.json`: the extension. `type` is the id of the type it
  extends; a built-in type's id is its model's key (`mobends:player`).
* `assets/mobends_wave/bends/animators/wave.json`: the layer. Its selector chooses `wave` while the
  player stands still, and `through` otherwise: a `core:fallthrough` node, which poses nothing, so
  the player's own pose shows. The transitions between them fade between the waving arm and the
  player's own arm.

To try it, copy or link this folder into the game's `resourcepacks` folder and enable it.
