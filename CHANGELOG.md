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
- Animator values (angles, weights, offsets, damping rates, ...) are expressions: JSON trees of
  operations such as `{"add": [{"mul": ["limbSwing", 0.6662]}, 3.14]}`, which can be nested
  freely, and named expressions declared on the animator, a layer or a node. See
  `misc/kumo-format.md` ("Expressions").
- Extensions: a mod or a resource pack can add layers on top of an entity type's animator with a
  JSON file in `assets/<namespace>/bends/extensions/`, changing part of how an entity moves while
  the rest of its animation (and other packs' extensions) carry on. Their `core:fallthrough`
  nodes let the animation below show, and fade to and from it. They replace bends packs. See
  `misc/kumo-format.md` ("Extensions") and the example packs in `misc/examples/wave-extension`
  and `misc/examples/dance-extension` (cows and chickens dance to a beat).
- When an entity has several extensions, Settings shows an *Extensions* button for it: move them
  up and down to decide which goes on top.
- A keyframe in a clip file can leave out `position`, `rotation` or `scale` when it has none.

### Changed

- In animators, a clip's `time` and `loop` are replaced by `frame` (an expression: where in the
  clip it is) and `duration` (how long the clip runs, in ticks). A clip loops by wrapping its
  frame, `{"mod": [..., "clipLength"]}`, and holds its last frame otherwise; it counts as finished
  once its duration has passed. `elapsed` (ticks since the node started) can be used in any
  expression. Clip files no longer have `loop`. Custom animators need updating; see
  `misc/kumo-format.md` ("Clips").

- The wolf is animated like every other entity now (one animator, JSON clips). Its walk no longer
  lags one frame behind its movement.
- Only one animator format is left. Keyframe layers name their nodes (`nodes` is an object and
  `entryNode` and connection targets are names), `core:standard` and `core:movement` nodes are
  gone (use `core:pose` nodes with clips), and clips are JSON files (binary `.bendsanim` clips no
  longer load). A node without a `type` is a `core:pose` node.

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
- Mobs described by model definitions: the lower half of a split limb (villager, witch, cow, pig
  and golem knees and elbows) and bones drawn inside another (the chicken's bill and chin) no
  longer get their parent's transform twice, which pushed them away from where they belong;
  inflated vanilla boxes (the villager's and the witch's robe) keep their inflation instead of
  z-fighting with the body; the villager's and the witch's arms sit where vanilla puts them.
- Knees and elbows of those mobs hinge at the edge of the joint (`"hinge"` in a model
  definition's `split`), so a bent leg no longer opens a gap at the knee.
- The head of a villager, witch, cow, pig, creeper, chicken or iron golem no longer spins while
  it walks or jumps: those poses now set the head's rotation instead of adding to last frame's.
- Those mobs' legs (and the iron golem's arms) settle back to rest when they stop walking,
  instead of freezing mid-stride.
- Mo' Bends' messages (such as a type or an extension that fails to load) now appear in the game
  log (`latest.log`), not only in the console.

### Breaking changes for addons

- `IMutatorFactory.createMutator()` no longer takes the entity's data factory, and `Mutator`'s
  constructor no longer takes one either: the entity's type decides which data an entity gets,
  not its mutator. Register mutators as `YourMutator::new` with a constructor without arguments.
- Mutators no longer undo themselves: `demutate`, `applyVanillaModel`, `deswapLayer`,
  `postRefresh` and `getOrMakeData` are gone. Mo' Bends captures a renderer's vanilla state
  before the first mutation and puts it back itself.
- The `registerNewEntity(..., IPreviewer, ...)` overloads are gone, along with the previewers.
- `ValueSource` and `ValueTemplate` are replaced by `Expression` and `ExpressionTemplate`
  (`core.kumo.expr`). A custom driver compiles its template's expressions with
  `Expression.compile(template.field, context.getExpressionScope(), fallback)`, and `ItemEffects`
  now takes the expression scope as its first argument.

- The old animation system is gone: the hand-written animation controllers, animation bits and
  layers (`core.animation.bit`, `core.animation.layer`, `standard.animation`) and the legacy
  animator nodes. Every entity animates from an animator asset.
- Bends packs are gone (`core.pack`, `EntityData.packAnimationState`,
  `NetworkConfiguration.areBendsPacksAllowed` / `isMovementLimited`, and the `bendsPacksAllowed`
  and `movementLimited` server settings). Use extensions.
- `AnimationLoader` only loads JSON clips from resources (`loadFromFile`, `loadFromString` and
  the internal registry are gone).
- `BipedActionController.getItemUseAction` / `getItemAttackAction` / `armPoseOf` moved to
  `standard.ItemActions`.

### Removed

- The unused animation previewers.
- Bends packs, and the *Packs* section of the Mo' Bends menu. Extensions take their place.
