# Changelog

## 1.3.0 (in-development)

### Added

- Entity types: a mod or a resource pack can decide which model and animator an entity gets,
  under a condition, with a JSON file in `assets/<namespace>/bends/types/` and no Java code.
  Conditions include the entity type, a player's name or UUID, and the skin variant; for
  example, a pack can give one player an animator of their own. See `misc/kumo-format.md`
  ("Entity types and selectors") and the example pack in `misc/examples/player-name-type`.
- When several types apply to the same entity, Settings shows an *Order* button for it: move the
  types up and down like resource packs to decide which one wins.
- Addons can add their own selector conditions (`AddonAnimationRegistry.registerSelectorCondition`).

### Changed

- Entities that share a renderer (every player, for one) can now look different: one can be
  animated while the next is vanilla, or each can have its own model or animator. Previously a
  renderer was either mutated for everyone or for no one.

### Fixes

- Mo' Bends no longer keeps every entity it has seen in memory until you switch worlds. The
  cache that remembers how each entity is animated now lets go of entities once the game has
  unloaded them.
- Entities that aren't animated no longer look up their animation on every frame.
- Reloading resources (F3+T, or changing resource packs) now picks up changed animators and model
  definitions.
- A JSON resource read by Mo' Bends no longer leaves a file open when it was already cached.

### Breaking changes for addons

- `IMutatorFactory.createMutator()` no longer takes the entity's data factory, and `Mutator`'s
  constructor no longer takes one either: the entity's type decides which data an entity gets,
  not its mutator. Register mutators as `YourMutator::new` with a constructor without arguments.
- Mutators no longer undo themselves: `demutate`, `applyVanillaModel`, `deswapLayer`,
  `postRefresh` and `getOrMakeData` are gone. Mo' Bends captures a renderer's vanilla state
  before the first mutation and puts it back itself.
- The `registerNewEntity(..., IPreviewer, ...)` overloads are gone, along with the previewers.

### Removed

- The unused animation previewers.
