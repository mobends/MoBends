# Example: vanilla swimming

A resource pack with one extension (`mobends_vanilla_swim:vanilla_swim`) on the built-in player
type (`mobends:player`): while a player is in water, they are drawn with the vanilla model and
vanilla animation; out of water, Mo' Bends animates them as usual. The switch is immediate.

* `assets/mobends_vanilla_swim/bends/extensions/vanilla_swim.json`: the extension.
* `assets/mobends_vanilla_swim/bends/animators/vanilla_swim.json`: one layer whose `when` is the
  `IN_WATER` state and whose only node is a `core:vanilla` node. While the condition holds the
  layer asks for vanilla; while it doesn't, the layer is off and asks for nothing. The player's
  own animator keeps running underneath the whole time, so leaving the water brings back the
  animated model in the pose it would have had.

The same shape works for any condition: swap `IN_WATER` for another state (`RIDING`,
`SNEAKING`, ...), or give the layer more nodes and let connections decide when to go vanilla.

To try it, copy or link this folder into the game's `resourcepacks` folder and enable it.
