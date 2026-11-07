# Legacy Mo' Bends

A resource pack that brings back how creatures moved in Mo' Bends 1.X.

Build the pack with `gradle legacyPack -Ppack_signing_key=<file>`. It is written to `build/packs/`
with a signature (`assets/mobends/bends/signature.json`), so servers that refuse resource packs'
animation still trust it (see `docs/content.md`). Don't commit a signature here: it is made for the
files as they are when the pack is built.

To try it unsigned, copy or link this folder into the game's `resourcepacks` folder and enable it. Without
the signature it works in singleplayer and on servers that allow or limit resource packs.
