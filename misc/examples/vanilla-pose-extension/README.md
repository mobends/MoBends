# Example: Player Animation Library's poses, bent

A resource pack with one extension (`mobends_vanilla_pose:vanilla_pose`) on the built-in player
type (`mobends:player`): while Player Animation Library (PAL) plays something on a player (Better
Combat's attacks and its weapons' idle poses, an emote), Mo' Bends draws the player in the pose PAL
gives the vanilla model, with elbows and knees bent on top of it. Out of PAL's animations, Mo' Bends
animates them as usual.

* `assets/mobends_vanilla_pose/bends/extensions/vanilla_pose.json`: the extension.
* `assets/mobends_vanilla_pose/bends/animators/vanilla_pose.json`: one layer whose `@when` is
  `mobends:compat_pal_animation_playing` and whose only node is a `core:vanilla_pose` node. The
  node takes every bone the vanilla model poses from it, and its own items bend the forearms and
  the shins, which the vanilla model draws straight. It fades in and out over 2 ticks.

Compare `misc/examples/vanilla-swim-extension`, whose `core:vanilla` node hands the player to the
vanilla model altogether: no bends, and no blending.

PAL is read on Minecraft 26.x; elsewhere the layer never holds (the vanilla pose itself is read on
every version).
To try it, copy this folder into the game's `resourcepacks` folder and enable it. With
`./gradlew :26.3-fabric:runClient -PbetterCombat`, Better Combat and PAL are in the game.
