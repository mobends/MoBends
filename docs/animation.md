# Animation System (KUMO)

Every animated entity is driven by an **animator asset**
(`assets/<namespace>/bends/animators/*.json`) running on the KUMO engine in the `core/` module.
There is no hand-written per-entity animation code any more: what used to be controllers and
animation bits is data, and the few things that must stay code (the spider's leg IK, turning and walking on planted feet, the sword
trail, the cape) are *drivers* an animator places like any other item. The asset format is
documented in [`misc/kumo-format.md`](../misc/kumo-format.md); this page is about how the
runtime is put together.

## Pieces

| Piece | Where | Role |
|-------|-------|------|
| `IKumoSubject` | `core/kumo` | All KUMO knows about an entity: bones resolved by name into rotation/vector sinks, plus named numeric variables and boolean states, and the entity itself for `field` (read through `EntityFields`, which the mod backs with its generated accessors). `EntityData` implements it; data classes register what they expose (`entityLimbSwing`, `entityHeadYaw`, `entityTicksAfterTouchdown`, `SITTING`, ...). |
| `KumoAnimatorState` | `core/kumo/state` | One running animator: its layers, their trust, the resource-pack limits. |
| `LayerState` | `core/kumo/state` | A layer: decides its node each frame, cross-fades between nodes, and composites onto the animator's pose as `OVERRIDE` or `ADDITIVE`. |
| `MachineState` | `core/kumo/state` | A machine (the layer's own, and any nested one): its members, its selector (`Selector`) and its connections. |
| `PoseNode` | `core/kumo/state/node` | A node: an ordered *pose stack* of items (clips, drivers), each with an optional `when`, a composition space and damping. |
| `Pose` | `core/kumo/pose` | Per-bone rotation / offset / vector targets for one frame, bound to the subject's sinks by index. |
| `Expression`, `ExpressionOperations` | `core/kumo/expr` | The expression language: every value and condition an animator computes, typed (number or boolean) and checked when the animator loads. |
| `ExpressionScope` | `core/kumo/expr` | What a place in an animator sees: the scopes around it, whose definitions it reads by scoped name (`layer.combo`), and the built-ins. |
| `DefinitionScope`, `ScopeLists` | `core/kumo/state` | A scope's definitions (constant, state, live) and their values for the entity, and its `enter` / `update` / `exit` statement lists. |
| `VariableTable` | `core/kumo/state` | The names an animator reads and writes, numbered when it is instanced and resolved against the entity on its first frame. |
| `KumoAnimatorController` | `core/kumo` (mod) | Animates one entity data: loads the animator (and its extensions) through `AnimatorResources` and updates it each frame. A broken asset, or one that fails while animating, is logged once and the entity simply doesn't animate. |

## Per-frame Pipeline

1. `DataUpdateHandler.updateAnimations` fires each render tick; `EntityDatabase` updates every
   tracked entity's data.
2. The data's controller (`KumoAnimatorController`) updates the animator state:
   * each layer first decides its transitions (its selectors from the layer inwards, then the
     connections from the node outwards; see below), so a node entered this frame also poses
     this frame; on the first frame, entering the layer takes the place of that decision;
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

## Choosing the Node

A layer chooses its node with a **selector** (a decision tree: first match wins, a taken branch
decides inside it), and uses **connections** for what depends on the node it is in. Nodes that
form a sequence are grouped in a **machine**, with its own selector and connections; the layer
itself is the outermost machine. Each has its runtime counterpart (`MachineState`, `Selector`,
`ConnectionState`):

1. Entering a machine (`LayerState.enter`) starts the conditions of its selector and connections
   over, then follows its selector, or its `defaultOnEntry` where the selector chooses nothing,
   down through the machines it leads into to a node, which it starts. A layer enters its own
   machine when it starts (`LayerState.start`), on its first frame and with nothing to crossfade
   from; a transition into a machine enters it the same way.
2. `LayerState` keeps the path from its machine down to the current node. Every later frame it
   evaluates the selector of every machine on the path, outermost first; the first that leads to a
   member off the path is taken.
3. Otherwise it evaluates the connections of the node, then of each machine on the path, innermost
   first; the first met fires.

Every condition on the path is evaluated each frame, and nothing short-circuits, so edge
triggers (`decreased`, `rose`, `fell`) never miss a frame. A live definition is computed once a
frame (one that remembers something, every frame its scope exists), so its memory is one,
however many places read it.

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
run on a node's clock (`nodeTicksElapsed`), on any variable (`entityLimbSwing`, `entityTicksInAir`, ...) or loop with `mod`. JSON is
the only clip format.

## Operations in Java

A mod (or Mo' Bends itself) brings logic to animators as **registered operations**, named
`namespace:id` (an addon's `AddonAnimationRegistry` adds its mod id; bare names are the language's
and can't be registered). The API lives in `core/kumo/api`, so it names no Minecraft type: the
entity is an opaque `Object` and its `Class<?>`, and the mod layer's helpers add the typed casts.
There are three levels:

```java
// A pure function of numbers: with its argument written out, computed once, when the animator loads.
registry.registerFunction("smoothstep", t -> t * t * (3 - 2 * t));

// Reads the entity. The class is where it applies: an entity of another class takes the
// operation's @fallback, or the animator fails to load. The reader never casts.
registry.registerEntityCondition("is_wet", Entity.class, entity -> entity.isWet());
registry.registerEntityNumber("air", EntityLivingBase.class, entity -> entity.getAir());

// Everything else: the full signature, bind and evaluate, and declared state.
registry.registerOperation(KumoOperation.named("distance_to_nearest")
        .param("entityType", Kind.STRING)
        .returns(Expression.Type.NUMBER)
        .withFallback()
        .bind(args -> {
            Class<? extends Entity> target = EntityList.getClass(new ResourceLocation(args.string(0)));
            if (target == null) throw args.error(0, "is no entity type");
            return (NumberEvaluator) (context, values) -> nearestDistance((Entity) context.entity(), target);
        }));
```

* **The signature** (`KumoOperation`) declares the parameters (`NUMBER` and `BOOLEAN` take any
  expression of that type, `CONSTANT` a number written out, `STRING` a string written out, and
  `choice(...)` one of a set; the last may repeat), the result type, whether it takes a
  `@fallback`, whether it is **pure** (same arguments, same result: with constant arguments it is
  computed once, at load) and whether a type file's selector may use it (**selector-safe**).
  Arguments are checked when the animator loads, and mistakes are reported in the operation's own
  words (`args.error(i, ...)`).
* **Bind** runs once per use, when the animator is loaded for an entity class (`args.entityClass()`):
  it reads the written-out arguments (`string(i)`, `constant(i)`), does its one-time work (an item
  looked up, a pattern compiled) and returns a `NumberEvaluator` or a `BooleanEvaluator`, or null
  where the operation doesn't apply to the class (its `@fallback` is used, or the load fails).
* **Evaluate** runs every frame with the entity (`context.entity()`, an instance of the bound
  class), the frame's length (`context.deltaTime()`) and the arguments, already evaluated
  (`values.number(i)`, `values.bool(i)`: nothing short-circuits). Both views are reused from frame
  to frame, so evaluating allocates nothing.
* **State** is declared at bind, floats only (a boolean is 0 or 1): `args.slot(name, initial)` or,
  sized by what bind saw, `args.slots(name, size, initial)`. Every place the operation is written
  keeps its own, back to its initial values whenever the scope holding that place starts (a node,
  when it is entered).

**Drivers** are registered the same way, with named fields instead of positional arguments: a
Gson template class (extending `DriverItemTemplate`) holds the fields as written, and the binder
turns them into what it evaluates with, each named by its field for the errors:

```java
public class WagTemplate extends DriverItemTemplate
{
    public String bone;
    public ExpressionTemplate speed;
    public Map<String, String> out;
}

registry.registerDriver(KumoDriver.of("wag", WagTemplate.class, (template, args) -> {
    int bone = args.bone("bone", template.bone);                        // a bone, by index
    NumberInput speed = args.number("speed", template.speed, 1);        // an expression, or 1
    StateHandle phase = args.outputs(template.out, "phase").get("phase"); // an output, if mapped
    FloatSlot t = args.slot("t", 0);                                    // declared state
    Quaternion rotation = new Quaternion();
    return (context, pose) -> {
        t.set(context, t.get(context) + speed.get(context) * context.deltaTime());
        if (phase != null) phase.set(context, t.get(context));
        PoseMath.axisAngleDegrees(0, 1, 0, 30 * MathHelper.sin(t.get(context)), rotation);
        pose.rotate(bone, rotation, Pose.Space.PRE);
    };
}));
```

* Inputs (`number`, `bool`) are expressions, evaluated for the frame before the driver runs, every
  one of them. `entityValue` reads a built-in in double precision, for positions in the world.
* A driver writes states two ways: the state it steps, named by `inout` (`args.inout`), and its
  **outputs**, which a file maps to states in `out` (`args.outputs`, which refuses an output the
  driver doesn't have). Either way, a file from a resource pack can't name a trusted file's state.
* It poses through a `PoseWriter`: the bones by index, what the items before it made of them
  (`rotationSoFar`, `vectorSoFar`, ...), writes composed in a space, and `snapRotation` /
  `snapVector` to jump a bone past its damping this frame. A narrow view, so the pose buffers can
  change without breaking drivers.
* Its declared state starts over when its node is entered. `DriverEvaluator.restart` runs then
  too, for what isn't declared state yet (`core:step_turn` publishes its outputs at rest).

`core:spring` and `core:step_turn` are written this way (`SpringDriver`, `StepTurnDriver`).

Registration closes when the first animator loads (`KumoRegistry.close()`): what an animator was
compiled against can't change under it. An addon registered before the client core exists has its
content registered as soon as it does.
