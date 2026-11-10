# Addons

How another mod adds to Mo' Bends in code, on the modern versions (1.21.1, Fabric and NeoForge).

You don't need an addon for animations, entity types, extensions or model definitions: a mod
ships those in its resources (`assets/<modid>/bends/`), like a resource pack (see
[content.md](content.md)). An addon is for what needs Java:

| Register | So that | Method |
|---|---|---|
| A mob | the entities a model definition of yours names are animated, as the default for their class | `registerDefinedEntity("my_mob")` (`assets/<modid>/bends/models/my_mob.json`) |
| A KUMO operation | animators can use `{"mymod:name": [...]}` in expressions | `registerOperation`, `registerFunction`, `registerEntityFloatReader` (and `Double`, `Boolean`) |
| A driver | animators can use a pose item computed in Java | `registerDriver` |
| A component | model definitions can give an entity's data a component (their `components`) | `registerComponent` |
| A layer | model definitions can name a render layer (their `layers`), which then follows the bones | `registerLayer`, `registerCopyLayer`, `registerImplicitLayer` |

Everything is named without a namespace and registered under your mod id: `registerOperation`
of `wetness` from `mymod` is `mymod:wetness`. Operations and drivers are described in
[animation.md](animation.md), *Operations in Java*.

## Writing one

Implement `goblinbob.mobends.core.addon.IAddon`, with a public constructor that takes nothing:

```java
@MoBendsAddon // for NeoForge
public class MyAddon implements IAddon {
    @Override
    public void registerContent(AddonAnimationRegistry registry) {
        registry.registerDefinedEntity("my_mob");
        registry.registerEntityFloatReader("wetness", MyMob.class, MyMob::getWetness);
    }

    @Override
    public String getDisplayName() {
        return "My Mod";
    }
}
```

Then tell Mo' Bends about it:

- **Fabric**: list it under the `mobends` entrypoint of your `fabric.mod.json`:
  ```json
  "entrypoints": {
    "mobends": ["com.example.mymod.MyAddon"]
  }
  ```
- **NeoForge**: put `@MoBendsAddon` on the class. Mo' Bends finds it in your jar.

A mod on both loaders does both. Your mod's id is the one in `fabric.mod.json` or
`neoforge.mods.toml` (on NeoForge, the jar's first mod). Make Mo' Bends an optional dependency
(`suggests` / `type = "optional"`): without it, the addon class is never loaded.

Addons only run on the client. Their content is registered at start-up, before any animator,
model definition or type file loads; registering later throws.

## A mob of your own

A mob is a model definition (`assets/<modid>/bends/models/my_mob.json`, see
[content.md](content.md)) registered with `registerDefinedEntity`. It names your entity and model
by their full class names, which loaders don't rename, and each bone's part by its path in your
model's part tree (the names you gave `addOrReplaceChild`), or by your model's `ModelPart` field:

```json
{
  "formatVersion": 2,
  "entity": "com.example.mymod.entity.MyMob",
  "model": "com.example.mymod.client.MyMobModel",
  "animator": "mymod:bends/animators/my_mob.json",
  "bones": [
    {"name": "body", "vanilla": {"part": "body"}},
    {"name": "head", "vanilla": {"part": "body/head"}, "parent": "body"},
    {"name": "leftArm", "vanilla": {"field": "leftArm"}, "parent": "body",
     "split": {"at": [0.5], "names": ["leftForeArm"]}}
  ]
}
```

Your model must be one Mo' Bends draws from: a `HierarchicalModel` (as most are), or a
`HumanoidModel`. A model that extends `EntityModel` and draws its parts itself is drawn as vanilla.

## Callbacks

`IAddon` also has, all optional, on the client's thread:

- `onRenderTick(partialTicks)`: each frame in a world, unless the game is paused.
- `onClientTick()`: each client tick in a world, unless the game is paused.
- `onRefresh()`: on every resource reload, the Mo' Bends menu's Refresh and a change to its
  settings. Free what you cache here.

## Layers

A layer your definition names follows the bones when it asks where to draw. Add the layer to the
renderer as you would without Mo' Bends, register its class (`registerLayer("glow", GlowLayer.class)`),
name it in your definition's `layers` (`"mymod:glow": {}`), and in its `render`:

```java
if (BendsLayers.translateToBone(entity, "mymod:glow", "head", poseStack)) {
    // poseStack is now in the bent head's frame
}
```

It returns false when the entity isn't bent or its definition doesn't name the layer; draw as
you would without Mo' Bends then. A layer that draws its own copy of the entity's model is
registered with `registerCopyLayer` instead, and drawn bent with no change to it.

## Coming from 1.12

The API is 1.12's `IAddon` and `AddonAnimationRegistry`, with these differences:

- **Found, not registered.** Addons are found through the entrypoint or the annotation;
  `AddonHelper.registerAddon` still works, but isn't needed.
- **No Java benders.** `registerNewEntity` and `registerEntity` are gone: every mob is a model
  definition now (`registerDefinedEntity`, which no longer returns a key).
- **Layers.** `registerLayer` takes the layer class model definitions name, not a factory that
  replaces a vanilla layer: the vanilla layer stays and follows the bones. Layers drawing a copy
  of the model have `registerCopyLayer`.
- Mo' Bends' own content is registered as an addon too (`standard/DefaultAddon.java`), the
  fullest example.
