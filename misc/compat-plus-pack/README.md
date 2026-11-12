# Mo' Bends Compat+

A resource pack that makes Mo' Bends get along with other mods that animate the player. Each mod
gets a layer on top of the player's animator, which holds only where that mod is installed
(`core:mod_loaded`), so the pack can stay enabled whatever the modpack.

* `extensions/player.json`: one extension (`mobends_compat_plus:player`) on the built-in player type.
* `animators/player.json`: its layers.
  * **Better Combat**: Better Combat plays its attacks with Player Animation Library (PAL), on the
    vanilla model, which Mo' Bends doesn't draw. While PAL plays something on a player
    (`mobends:compat_pal_animation_playing`), the layer hands them to vanilla (`core:vanilla`), so
    Better Combat's animation shows; when it stops, Mo' Bends takes over again. PAL is read on
    Minecraft 26.x; elsewhere the layer never holds.

It needs a Mo' Bends with `core:mod_loaded` (2.0.0-alpha.5 and later). On an older one, the extension
fails to load, with an error shown once, and Mo' Bends animates as usual.

Build the pack with `./gradlew :core:compatPlusPack -Ppack_signing_key=<file>`. It is written to
`build/packs/` with a signature (`assets/mobends/bends/signature.json`), so servers that refuse
resource packs' animation still trust it (see `docs/content.md`). Don't commit a signature here: it
is made for the files as they are when the pack is built.

To try it unsigned, copy or link this folder into the game's `resourcepacks` folder and enable it.
With `./gradlew :26.3-fabric:runClient -PbetterCombat`, Better Combat is in the game to try it with.
