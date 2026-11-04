# Legacy Mo' Bends

A resource pack that brings back how the player and the zombie moved in Mo' Bends 1.X. They don't
stand on their feet (`core:step_turn`), so their legs follow their clips when they stand and turn,
as they did in 1.X. Everything else is Mo' Bends 2.X. The zombie villager, whose animator extends
the zombie's, follows it.

* `assets/mobends/bends/animators/player.json` and `zombie.json` replace the mod's at the same
  keys. They are forks of the mod's animators without the layer that stands them on their feet
  (the `planted` layer). When the mod's animator changes, change the fork with it.
  `LegacyPackTest` in the animation lab fails until you do.

Build the pack with `gradle legacyPack -Ppack_signing_key=<file>`. It is written to `build/packs/`
with a signature (`assets/mobends/bends/signature.json`), so servers that refuse resource packs'
animation still trust it (see `docs/content.md`). Don't commit a signature here: it is made for the
files as they are when the pack is built.

To try it unsigned, copy or link this folder into the game's `resourcepacks` folder and enable it. Without
the signature it works in singleplayer and on servers that allow or limit resource packs.
