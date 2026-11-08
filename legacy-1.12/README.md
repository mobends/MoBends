# The 1.12.2 mod, as a porting reference

These are the sources of Mo' Bends for Minecraft 1.12.2 (Forge) as they were when `main` was split off
`forge-1.12`. **Nothing here is built.** They are the reference for porting the mod to the modern versions
in `../src`, and the animation lab (`../animation-lab`) still compiles its animation code from here.

- **Port a class by moving it** with `git mv legacy-1.12/src/main/java/<path> src/main/java/<path>`, then
  adapting it, so `git log --follow` keeps its history. Delete this directory once it is empty.
- Fixes to the 1.12.2 mod go to the `forge-1.12` branch, not here: this copy isn't kept up to date.
- The animators, models, types and textures have already moved to `../src/main/resources/assets/mobends/`,
  since every version shares them. The `.lang` translations are still here, until they are converted to
  the modern `.json` format.
- `build.gradle`, `gradle.properties` and `run-arm64/` are the 1.12.2 build, kept for reference only; on
  `forge-1.12` they build the mod.
