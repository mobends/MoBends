# Development pack

Content still being refined, kept out of the mod's jar until it's ready for a release:

* `assets/mobends/bends/types/cow.json`, `sheep.json`: the cow and the sheep. Their models and
  animators already ship in `src/main/resources`; without these type files no mob uses them.

To try it, link this folder into the development client's `resourcepacks` folder and enable it:
`ln -s ../../misc/dev-pack run/resourcepacks/local`. To release something, move its files to
the same path under `src/main/resources`.
