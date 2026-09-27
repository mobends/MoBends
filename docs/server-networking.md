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

Singleplayer (and the title screen) is always `ALLOW`. On joining a world the client falls back to
the defaults, so a server without Mo' Bends (which never answers) limits resource packs.
`AnimationPolicy` remembers the policy the animation content was loaded under and reloads it
(`CoreClient.reloadAnimation()`) whenever the policy in force differs: on joining a world, and when
the server's answer arrives. The limits themselves are read every frame, so changing them needs
no reload.

## Message Flow

```
Local player joins a world
  → WorldJoinHandler (EntityJoinWorldEvent for EntityPlayerSP only)
  → AnimationPolicy.onWorldJoin(): defaults until the server answers
  → sends MessageConfigRequest (empty payload) to server
  → server responds with MessageConfigResponse (NBT-serialized SharedConfig)
  → on the client thread, AnimationPolicy.onServerConfiguration applies the received values
```

## Key Classes

| Class | Location | Role |
|-------|----------|------|
| `NetworkConfiguration` | `core/network/` | Holds the shared properties (loaded by the server, received by the client) |
| `AnimationPolicy` | `core/client/` | The policy in force on the client, its `AnimationLimits`, and reloading the content when the policy changes |
| `ResourcePackPolicy` | `core/network/` | `ALLOW` / `LIMITED` / `DENY` |
| `SharedProperty<T>` | `core/network/` | Generic network-synced property (`SharedStringProp`, `SharedFloatProp`) |
| `SharedConfig` | `core/network/` | Container; serializes/deserializes via NBT |
| `MessageConfigRequest` | `core/network/msg/` | Client→Server, no payload |
| `MessageConfigResponse` | `core/network/msg/` | Server→Client, NBT config blob |
| `CoreServerConfig` | `core/configuration/` | Server-side config values |
