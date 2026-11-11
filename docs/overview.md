# Mo' Bends — Codebase Overview

**Root package:** `goblinbob.mobends`

## Gradle Modules

| Module | Purpose |
|--------|---------|
| `core/` | The Minecraft-free animation engine: KUMO (`core/kumo`), maths (`core/math`). Java 8, no Minecraft, Forge or LWJGL, so every Minecraft version can share it. Published as `goblinbob.mobends:mobends-core` (see `CONTRIBUTING.md`). |
| root | The Forge 1.12.2 mod. Depends on `core/` and bundles it into its jar. |

The few KUMO pieces that need Minecraft (`KumoAnimatorController`, `AnimatorResources`,
`EquipmentNameCondition`, `mcsin`/`mccos`) stay in the mod and are registered into the core by
`MinecraftKumoOperations` at `preInit`.

## Packages

| Package | Purpose |
|---------|---------|
| `core` | Framework: animation, entity types, rendering hooks, networking, API |
| `standard` | Default addon: vanilla entity support (player, zombie, spider, etc.) |

## Entry Points

- `Core` — abstract base; manages module lifecycle (`preInit`, `onRefresh`)
- `CoreClient` — registers Forge event handlers and the client modules
- `CoreServer` — minimal server-side init, handles network channel registration
- `CoreClient.reloadAnimation()` — reloads everything asset-driven (clip, animator and model
  definition caches, entity data, mutators, entity types); runs after every resource reload and
  when the server changes what resource packs may do
- `CoreClient.refresh()` — the same plus the addons and modules; run by the menu's Refresh button and
  when the config changes

## Module System

Modules implement `IModule` with `preInit()` and `onRefresh()`. Active modules:
- `EnvironmentModule` — loads `env.json` API URL override
- `ConnectionManager` — manages background HTTP threads
- `AssetsModule` — downloads remote textures/models in the background and caches them
- `SupporterContent` — fetches cosmetic definitions and player settings

## Addon Extension Point

On the modern versions, see [addons.md](addons.md); what follows is 1.12.2's.

Third-party addons implement `IAddon`:
- `registerContent(AddonAnimationRegistry)` — register KUMO operations (see
  [animation.md](animation.md), *Operations in Java*), the selector-safe ones among them, which
  type files' selectors read, drivers, components and layers (mobs are type files and model
  definitions, not code). An addon registered before the client core exists has its content registered as
  soon as it does; registering after the first animator has loaded is refused
- `onRenderTick`, `onClientTick`, `onRefresh` — lifecycle callbacks

`DefaultAddon` (standard module) registers Mo' Bends' operations, drivers, components and layers.
Every mob it animates, the player included, comes from a type file in `bends/types/` that names its
model definition. Everything else a mob needs (its animator, its type,
extensions) is assets; see [content.md](content.md).
