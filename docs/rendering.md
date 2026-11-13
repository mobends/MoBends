# Rendering & Mutation System

## On the modern versions

- **Hooks are vanilla Mixins** (`goblinbob.mobends.mixin`, client), so they're written once for every
  loader. Prefer `@WrapOperation`/`@Inject` that keep other mods' wrappers running. Anything Mo'
  Bends can't bend must draw exactly as vanilla, never twice.
- **Drawing:** `ModelPart` is final on the modern versions, so instead of swapping parts as 1.12 did, a bent copy
  of the model is built per vanilla model (`DefinedModelBuilder`, `DefinedModel`) and drawn inside the
  model's own `renderToBuffer` (`AgeableListModelMixin`, `HierarchicalModelMixin`), which the renderer
  marks (`BendsRenderer.markBentDraw`). Layers get a bone's frame from `DefinedModel.applyVanillaFrame`
  after `DefinedRenderer.transformModel`; the bones come from the definition's `layers` block
  (`DefinedLayers`).
- **Vanilla's pose** (`core:vanilla_pose`): on 26.x the entity is animated once its render state is
  filled (`EntityRendererMixin`, at `createRenderState`'s return, so every subclass's
  `extractRenderState` has run). `BendsRenderer.beforeRender` opens a `VanillaPoseReader` around
  the animation; the first read runs the model's `setupAnim` from the state (and has Player
  Animation Library sample its animation first, `PalOperations.beforeVanillaPose`), then works
  out each bone's rotation, offset and scale from vanilla's part tree (`DefinedModelPart.vanillaChain`).
  The entity's frame is read as the `root` bone's pose: the reader has the renderer's own turns of
  the entity (`setupRotations`, the flip, its `scale`, the 1.501 shift; `BendsRenderer.VanillaModel`)
  worked out twice ahead of the draw, as vanilla makes them and as they're made for the bent
  entity (`DefinedRenderer.framedAs`, which the mixins that leave vanilla's turns out ask through
  `DefinedRenderer.drawnBentBy`: the player's swimming tilt, `PlayerRendererMixin`, and the iron
  golem's rock, `IronGolemRendererMixin`). `DefinedRenderer.vanillaFrameAsRoot` solves for the root
  that makes `transformModel`'s matrix, with the swimming drop and the sneak offsets it adds,
  vanilla's. The renderer itself always draws a bent entity its own way; the root blends into and
  out of vanilla's frame like any bone. The pose also says how much the entity's frame is vanilla's
  (`Pose.setVanillaFrame`, blended by fades and transitions), which the data gets once animated
  (`IKumoSubject.setEntityFrameVanilla`, `DefinedEntityData.getEntityFrameVanilla`): the sword
  trail fades out by it.
  Before 26.1 the entity is animated at `render`'s start (`LivingEntityRendererMixin`), and the
  reader poses the vanilla model there as vanilla will further into `render`: the model's
  `attackTime`, `riding` and `young`, then `prepareMobModel` and `setupAnim` with the renderer's
  `getBob` and the arguments vanilla computes (`BendsRenderer.RenderArguments`, which the data's
  head angles and limb swing are read from too). A subclass's renderer has set what it sets on the
  model by then (the player's arm poses and crouching).

The rest of this doc describes 1.12.2 (the `forge-1.12` branch).

## How It Hooks Into Minecraft (1.12.2)

All hooks use Forge's `MinecraftForge.EVENT_BUS`, registered in `CoreClient.init()`.

| Event | Handler | What It Does |
|-------|---------|-------------|
| `RenderLivingEvent.Pre` (lowest priority) | `EntityRenderHandler` | Animates the entity and puts its bender's mutated state in place, or vanilla; sets up GL transforms |
| `RenderLivingEvent.Post` | `EntityRenderHandler` | Reverts GL state; always puts the vanilla state back |
| `TickEvent.RenderTickEvent` | `DataUpdateHandler` | Advances animation frames, updates `ticksPerFrame` |
| `TickEvent.ClientTickEvent` (END) | `DataUpdateHandler` | Updates entity motion/velocity state |
| `InputEvent.KeyInputEvent` | `KeyboardHandler` | G → Mo' Bends menu |
| `EntityJoinWorldEvent` | `WorldJoinHandler` | When the *local* player joins (other players join all the time): resets the server rules to their defaults and sends `MessageConfigRequest` |

A resource reload listener runs `CoreClient.reloadAnimation()`.

## Mutation Model

`Mutator<D, E, M>` builds custom `IModelPart`s for a vanilla `RenderLivingBase` model (`mutate`).
Renderers are shared: every player is drawn by one of two `RenderPlayer`s, and every entity of a
kind by one renderer, while entities sharing a renderer can have different types. So a renderer
is never left mutated:

* `RendererState` captures a renderer's vanilla state (the model's part fields, the elements of
  its part arrays and lists, its layers) before the first mutation, and each bender's mutated
  state right after mutating;
* `RenderLivingEvent.Pre` puts the chosen bender's state in place (lowest priority, after
  anything that could cancel the render), and `Post` always puts vanilla back;
* the first-person hand puts the local player's mutation in place for the hand.

Nothing demutates: refreshing drops the mutators and restores vanilla.

`EntityBender<T>` owns a `Mutator` per renderer. `EntityBenderRegistry` is the singleton registry;
`getForEntity()` walks inheritance to find the right bender. `EntityBender.applyMutation` animates
the entity, then, if its animator is on a `core:vanilla` node (`EntityData.wantsVanilla()`),
restores the vanilla state and reports no mutation, so vanilla draws the entity while the
animation keeps running underneath.

Mobs described by model definitions use `DefinedMutator` and `DefinedRenderer`; see
[content.md](content.md).

**Stuck arrows.** `LayerArrow` picks a random part from the model's `boxList`, a box of it, and a
point on it. A definition replaces its bones' vanilla parts in that list with
`DefinedBoxAnchor`s, one per part with boxes: an anchor draws nothing, its boxes are where the
part's are (a mutated box leaves vanilla's bounds at 0), and its `postRender` is the part's posed
transform, so an arrow sticks where the part is drawn (`ArrowAnchorTest`).

## Armour

`LayerCustomBipedArmor` replaces vanilla's armour layer. The armour model an item asks for
(Forge's `getArmorModel`) is wrapped in an `ArmorWrapper` (one per model, `ArmorModelFactory`),
which follows the entity's `body`, `head`, arms and legs and their fore segments: a part wrapper
per vanilla part (`HumanoidPartWrapper`; `HumanoidLimbWrapper` cuts a limb's boxes at the elbow or
the knee, its *cut plane*) is synced with the entity's parts and swapped into the model while it
renders.

A part that doesn't turn with its parent (`IModelPart.inheritsRotation`, a bone's
`inheritRotation`: the bipeds' heads) turns back by its parents' rotation after its translation
(`applyParentRotationUndone`), so it sits where its parent puts it but turns relative to the model.
Every transform that follows a bone does this, the model's parts, the entity's `ModelPartTransform`s
(accessories) and the armour's part wrappers (which take the flag when they sync up), so a helmet or a
hat stays on the head.

An armour model that doesn't draw its parts' boxes can't be posed that way. An `IArmorGeometry`
draws it instead, into the part wrappers' frames (`IPartWrapper.applyTransform`,
`applyLowerTransform` below the cut plane), with its faces as `ArmorMesh`es cut at the same
planes. GeckoLib's armour (`standard/compat/geckolib`) is one: its faces are put where they are on
a vanilla model at rest, each armour bone (and the bones below it) in its part, and GeckoLib's own
animation of the armour still runs. GeckoLib is optional: only `GeckoLibCompat` is reached
without it, and it reaches the rest when GeckoLib's classes are there. The build compiles against
it (`compileOnly`); `-Pwith_geckolib` adds it, with its example armour, to the development client.

## Skeleton / Model Parts

`IModelPart` — a node in the animated skeleton:
- `applyPreTransform()`, `applyLocalTransform()`, `applyPostTransform()`
- `propagateTransform()` — recursive to children
- `getRotation()` returns a `SmoothOrientation` (quaternion with interpolation)

`ModelPartTransform` is the concrete impl with smooth `SmoothVector3f` interpolation for position/scale.

## Entity Data (Pose State)

`EntityData<E>` holds all per-entity render state and is the KUMO *subject* (`IKumoSubject`,
see [animation.md](animation.md)):
- `positionX/Y/Z`, `motionX/Y/Z`, `onGround`
- `root` and `center`, the entity's own bones (`EntityBone`: a smoothed position and rotation) the
  model hangs from: `MutatedRenderer.beforeRender` moves and turns the model by `root`, then by
  `center` about `getCenterPivot`
- `nameToPartMap` — bone name → `IModelPart`
- the animator (`setAnimator(animator, extensions)`), and the variables and states it exposes

`LivingEntityData<E>` extends it with animation timing (`ticksInAir`, `ticksAfterAttack`, etc.), limb swing overrides, and climbing logic.

`EntityDatabase` is the singleton that tracks all active entities and drives `updateRender()` / `updateClient()`.
It makes an entity's data with the factory of the entity's type and calls `initialize()` on it
(the parts and the animator bindings are made there, once every constructor has run); an entity
that reuses the id of one from another world gets new data.
