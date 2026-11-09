# Legacy Mo' Bends

A resource pack that brings back how creatures moved in Mo' Bends 1.X.

It is a snapshot of how the player, the zombie and the zombie villager moved before Mo' Bends 2.X,
not a fork: everything it plays is its own copy, so nothing the mod changes reaches it.

* `animators/` and `models/`, at the mod's own keys, replace the mod's player, zombie and zombie
  villager: the animators without the layer that stands them on their feet, and model definitions
  whose heads turn with the body (the mod's don't: `inheritRotation`).
* `animators/legacy/`, `animations/legacy/`: the biped animator the zombie extends and every clip the
  animators play, at keys the mod doesn't have, so the creatures the pack leaves to the mod (the
  skeleton, the zombie pigman) keep the mod's.
* `models/pig_zombie.json` gives the zombie pigman back the mod's head: its definition extends the
  zombie's, which the pack replaces, and it plays the mod's animator.

`LegacyPackTest` (in `animation-lab`) holds the pack to its own goldens and fails if it names a file
it doesn't have.

Build the pack with `./gradlew :core:legacyPack -Ppack_signing_key=<file>`. It is written to `build/packs/`
with a signature (`assets/mobends/bends/signature.json`), so servers that refuse resource packs'
animation still trust it (see `docs/content.md`). Don't commit a signature here: it is made for the
files as they are when the pack is built.

To try it unsigned, copy or link this folder into the game's `resourcepacks` folder and enable it. Without
the signature it works in singleplayer and on servers that allow resource packs. A server that limits
them refuses an unsigned pack's model definitions (geometry), so the heads wouldn't turn with the
body there, while the animators still turn them back from it.
