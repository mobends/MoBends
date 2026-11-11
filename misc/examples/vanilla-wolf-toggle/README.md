# Check: the wolf against vanilla

A resource pack with one extension (`mobends_vanilla_wolf:vanilla_wolf`) on the built-in wolf type
(`mobends:wolf`): every other second of a wolf's age, it is drawn with the vanilla model and
vanilla animation; the rest of the time Mo' Bends draws it bent. Flipping between the two in place
shows whether the bent model sits where vanilla's does: the head, legs and tail, the texture on
every face, the pup, the mouth in the head. The same shape works for any mob: change the
extension's `type`.

* `assets/mobends_vanilla_wolf/bends/extensions/vanilla_wolf.json`: the extension.
* `assets/mobends_vanilla_wolf/bends/animators/vanilla_wolf.json`: one layer whose `@when` holds
  for the second half of every 40 ticks of `entityTicksExisted`, and whose only node is a
  `core:vanilla` node. The wolf's own animator keeps running underneath, so the bent model comes
  back in the pose it would have had. The switch is immediate.

Wolves are a tick or more apart in age, so two wolves side by side don't flip together. To try it,
link this folder into the development client's `resourcepacks` folder and enable it:
`ln -s ../../misc/examples/vanilla-wolf-toggle run/resourcepacks/vanilla-wolf-toggle`.
