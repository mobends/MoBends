# Rendering & Mutation System

## How It Hooks Into Minecraft

All hooks use Forge's `MinecraftForge.EVENT_BUS`, registered in `CoreClient.init()`.

| Event | Handler | What It Does |
|-------|---------|-------------|
| `RenderLivingEvent.Pre` (lowest priority) | `EntityRenderHandler` | Animates the entity and puts its bender's mutated state in place, or vanilla; sets up GL transforms |
| `RenderLivingEvent.Post` | `EntityRenderHandler` | Reverts GL state; always puts the vanilla state back |
| `TickEvent.RenderTickEvent` | `DataUpdateHandler` | Advances animation frames, updates `ticksPerFrame` |
| `TickEvent.ClientTickEvent` (END) | `DataUpdateHandler` | Updates entity motion/velocity state |
| `InputEvent.KeyInputEvent` | `KeyboardHandler` | G → Mo' Bends menu; F10 → `MoBends.refreshSystems()` |
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

## Skeleton / Model Parts

`IModelPart` — a node in the animated skeleton:
- `applyPreTransform()`, `applyLocalTransform()`, `applyPostTransform()`
- `propagateTransform()` — recursive to children
- `getRotation()` returns a `SmoothOrientation` (quaternion with interpolation)

`ModelPartTransform` is the concrete impl with smooth `SmoothVector3f` interpolation for position/scale.

## Entity Data (Pose State)

`EntityData<E>` holds all per-entity render state and is the KUMO *subject* (`IKumoSubject`,
see [animation.md](animation.md)):
- `positionX/Y/Z`, `motionX/Y/Z`, `onGround`, `renderRotation`, offsets
- `nameToPartMap` — bone name → `IModelPart`
- the animator (`setAnimator(animator, extensions)`), and the variables and states it exposes

`LivingEntityData<E>` extends it with animation timing (`ticksInAir`, `ticksAfterAttack`, etc.), limb swing overrides, and climbing logic.

`EntityDatabase` is the singleton that tracks all active entities and drives `updateRender()` / `updateClient()`.
