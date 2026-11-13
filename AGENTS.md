# Agent instructions: Mo' Bends for Minecraft 1.8.9

This is the `forge-1.8.9` branch. Read CONTRIBUTING.md first; these are the rules that are easiest to
get wrong.

## Shared assets are `main`'s: never edit them here

`src/main/resources/assets/mobends/bends/` and `misc/legacy-pack/assets/` are copied from `main` as
they are, at `shared_assets_ref` (gradle.properties). CI fails if they differ
(`misc/shared-assets.sh check`), and the next copy from `main` would undo any edit anyway.

When an animator, model definition, type or extension doesn't work on 1.8.9:

1. Make the mod read it as it is, in code: `core/compat/SharedAssets.java` (entity classes, layers a
   definition names), `core/compat/EntityIds.java` (entity ids), the other `core/compat` classes.
2. If code can't cover it, don't work around it here. Open an issue on `main` saying what 1.8.9 lacks
   and what the format would need, note it in TODO.md, and tell the user.

Taking newer assets from `main` is `misc/shared-assets.sh copy <commit on main>`, never copying single
files by hand.

## Engine code lives in `core` on `main`

Never copy engine code into this branch: change `core/` on `main`, release it, and bump `core_version`.

## Checking a change

- Gradle runs on JDK 17 or later (`./gradlew build`); the animation lab (`animation-lab/`) on JDK 21.
- `./gradlew runClient` on Apple Silicon needs `-Pclient_java_home=<an x86-64 JDK 8>` (README).
