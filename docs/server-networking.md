# Server-Side Rules & Networking

## Purpose

The server decides what players' resource packs may do to the animation. Rules are transmitted
on world join; the client applies them until it leaves.

## Enforced Properties (`NetworkConfiguration`)

| Property | Type | Default | Effect |
|----------|------|---------|--------|
| `resourcePackAnimation` | String | `LIMITED` | `ALLOW`, `LIMITED` or `DENY`: what untrusted content (enabled resource packs) may do, see [content.md](content.md) |
| `maxPartOffset` | Float | `4` | `LIMITED` only: how far (model units, 1/16 block) untrusted animation may move a single part from where the trusted animation puts it |
| `maxBodyOffset` | Float | `16` | `LIMITED` only: the same for the whole model |

Singleplayer is always `ALLOW`. On joining a world the client falls back to the defaults, so a
server without Mo' Bends (which never answers) limits resource packs. When the effective values
change, `NetworkConfiguration.applyChanges` reloads the animation (`CoreClient.reloadAnimation()`).

## Message Flow

```
Local player joins a world
  → WorldJoinHandler (EntityJoinWorldEvent for EntityPlayerSP only)
  → NetworkConfiguration.onWorldJoin(): defaults until the server answers
  → sends MessageConfigRequest (empty payload) to server
  → server responds with MessageConfigResponse (NBT-serialized SharedConfig)
  → client applies received SharedProperty<T> values
```

## Key Classes

| Class | Location | Role |
|-------|----------|------|
| `NetworkConfiguration` | `core/network/` | Holds the shared properties; turns them into `AnimationLimits` |
| `ResourcePackPolicy` | `core/network/` | `ALLOW` / `LIMITED` / `DENY` |
| `SharedProperty<T>` | `core/network/` | Generic network-synced property (`SharedStringProp`, `SharedFloatProp`, `SharedBooleanProp`) |
| `SharedConfig` | `core/network/` | Container; serializes/deserializes via NBT |
| `MessageConfigRequest` | `core/network/msg/` | Client→Server, no payload |
| `MessageConfigResponse` | `core/network/msg/` | Server→Client, NBT config blob |
| `CoreServerConfig` | `core/configuration/` | Server-side config values |
