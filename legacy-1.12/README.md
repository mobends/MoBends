# The 1.12.2 mod, as a porting reference

These are the sources of Mo' Bends for Minecraft 1.12.2 (Forge) as they were when `main` was split off
`forge-1.12`. **Nothing here is built**, and nothing compiles from here (the animation lab,
`../animation-lab`, compiles the modern code in `../src`). They are the reference for porting the mod to
the modern versions in `../src`.

- **Port a class by moving it** with `git mv legacy-1.12/src/main/java/<path> src/main/java/<path>`, then
  adapting it, so `git log --follow` keeps its history. Delete this directory once it is empty.
- Fixes to the 1.12.2 mod go to the `forge-1.12` branch, not here: this copy isn't kept up to date.
- The animators, models, types, textures and translations (now `.json`) have already moved to
  `../src/main/resources/assets/mobends/`, since every version shares them.
- What the modern code already does, or doesn't need on 1.21.1, is gone; `git log` on `forge-1.12` has
  it. What's left is still to port:

| File | Becomes |
|---|---|
| `core/definition/DefinedFields.java`, `core/vanilla/VanillaModelParts.java` | Model definitions that name other mods' models |
| `standard/main/MoBends.java`, `src/main/resources/mcmod.info` | NeoForge's update check (`updateJSON`) and the credits |
| `src/test/.../WildcardPatternTest.java`, `RemoteFlightTest.java` | Their tests, in the animation lab |
