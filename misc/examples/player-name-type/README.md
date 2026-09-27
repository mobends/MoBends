# Example: an animator for one player name

A resource pack that adds one entity type (`mobends_example:bendy_tester`): players whose profile
name is `BendyTester` walk around with their arms stretched out like a zombie. Everything else
about them, and every other player, animates as usual.

* `assets/mobends_example/bends/types/bendy_tester.json`: the type. Its selector has two
  conditions (the entity is a player, the name matches), one more than the built-in player type,
  so it takes precedence without any ranking.
* `assets/mobends_example/bends/animators/zombie_arms.json`: extends the player animator and adds a
  last layer that holds both arms forward with a slight sway.

To try it, copy or link this folder into the game's `resourcepacks` folder, enable it, and play
as `BendyTester` (see the root README for setting the name in the development environment).
