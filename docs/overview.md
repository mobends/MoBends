# Mo' Bends — Codebase Overview

**Root package:** `goblinbob.mobends`

## Gradle Modules

| Module | Purpose |
|--------|---------|
| `core/` | The Minecraft-free animation engine: KUMO (`core/kumo`), maths (`core/math`), `flux`. Java 8, no Minecraft, Forge or LWJGL, so every Minecraft version can share it. Published as `goblinbob.mobends:mobends-core` (see `CONTRIBUTING.md`). |
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
- `MoBends.refreshSystems()` — the same plus the modules; bound to F10 and run when the config
  changes

## Module System

Modules implement `IModule` with `preInit()` and `onRefresh()`. Active modules:
- `EnvironmentModule` — loads `env.json` API URL override
- `ConnectionManager` — manages background HTTP threads
- `AssetsModule` — downloads and caches remote textures/models
- `SupporterContent` — fetches cosmetic definitions and player settings

## Addon Extension Point

Third-party addons implement `IAddon`:
- `registerContent(AddonAnimationRegistry)` — register entity benders, KUMO drivers and trigger
  conditions, and selector conditions for entity types
- `onRenderTick`, `onClientTick`, `onRefresh` — lifecycle callbacks

`DefaultAddon` (standard module) registers the vanilla entity benders and the model
definitions in `bends/models/index.json`. Everything else a mob needs (its animator, its type,
extensions) is assets; see [content.md](content.md).
