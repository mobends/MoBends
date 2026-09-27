# Animation System (KUMO)

Every animated entity is driven by an **animator asset**
(`assets/<namespace>/bends/animators/*.json`) running on the KUMO engine in the `core/` module.
There is no hand-written per-entity animation code any more: what used to be controllers and
animation bits is data, and the few things that must stay code (the spider's leg IK, the sword
trail, the cape) are *drivers* an animator places like any other item. The asset format is
documented in [`misc/kumo-format.md`](../misc/kumo-format.md); this page is about how the
runtime is put together.

## Pieces

| Piece | Where | Role |
|-------|-------|------|
| `IKumoSubject` | `core/kumo` | All KUMO knows about an entity: bones resolved by name into rotation/vector sinks, plus named numeric variables and boolean states. `EntityData` implements it; data classes register what they expose (`limbSwing`, `headYaw`, `ticksAfterTouchdown`, `SITTING`, ...). |
| `KumoAnimatorState` | `core/kumo/state` | One running animator: its layers, their trust, the resource-pack limits. |
| `LayerState` | `core/kumo/state` | A layer: a state machine of nodes with timed cross-fades, composited onto the animator's pose as `OVERRIDE` or `ADDITIVE`. |
| `PoseNode` | `core/kumo/state/node` | A node: an ordered *pose stack* of items (clips, drivers), each with an optional `when`, a composition space and damping. |
| `Pose` | `core/kumo/pose` | Per-bone rotation / offset / vector targets for one frame, bound to the subject's sinks by index. |
| `ExpressionScope` | `core/kumo/expr` | Named expressions, scoped lexically (animator → layer → node). |
| `KumoAnimatorController` | `core/kumo` (mod) | Animates one entity data: loads the animator (and its extensions) through `AnimatorResources` and updates it each frame. A broken asset, or one that fails while animating, is logged once and the entity simply doesn't animate. |

## Per-frame Pipeline

1. `DataUpdateHandler.updateAnimations` fires each render tick; `EntityDatabase` updates every
   tracked entity's data.
2. The data's controller (`KumoAnimatorController`) updates the animator state:
   * each layer first decides its transitions, so a node entered this frame also poses this
     frame;
   * the current node (and, mid-transition, the previous one or a frozen snapshot of the blend)
     evaluates its pose stack into a `Pose`;
   * layers composite in order onto the animator's pose; extensions' layers come last;
   * with an untrusted layer present, the result is clamped around the trusted pose
     (`AnimationLimits`, see [content.md](content.md));
   * the final pose is written once into the subject's sinks.
3. The sinks are the bones' `SmoothOrientation` / `SmoothVector3f`: the animator only sets
   *targets*, and the bones smooth towards them at their damping rate. Damping is therefore a
   property of the animator (per node and per item) rather than of any code.
4. `EntityRenderHandler` puts the entity's mutated model in place before Minecraft renders it
   (see [rendering.md](rendering.md)).

## Where an Entity's Animator Comes From

An entity's *type* decides its model and animator (see [content.md](content.md)); the type's data
factory creates the entity data and gives it the animator, with the extensions for that type
stacked on top (`EntityData.setAnimator(animator, extensions)`). Animators can `extends` a parent
(the zombie, skeleton, pig zombie and player all extend `biped.json`), and every cache involved
is cleared by `CoreClient.reloadAnimation()` after a resource reload.

## Clips

Keyframe clips (`assets/<namespace>/bends/animations/**.json`) are loaded by `AnimatorResources`
into `KeyframeAnimation`s and sampled by `ClipSampler` (hemisphere-corrected, so a track crossing
±180° takes the short way). A clip item's `frame` is an expression in clip units, so a clip can
run on elapsed time, on any variable (`limbSwing`, `ticksInAir`, ...) or loop with `mod`. JSON is
the only clip format.
