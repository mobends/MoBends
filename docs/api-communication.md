# API Communication

## On the modern versions (`main`)

The endpoints, the payloads and what's cached are the same as on 1.12.2 (below). What differs:

- Requests go through Java's `HttpClient` (`core/util/ConnectionHelper`), each on a background thread;
  what comes back is handed to the render thread. Nothing waits for the API, and nothing is called
  on the render thread.
- As the game starts: the join (then a ping every `pingInterval` seconds, the first right away), the
  accessory catalogue (`/api/accessory/details`) and the asset manifest (`/api/asset/manifest`, then
  any new assets). Nothing else is called until a player is drawn.
- A player's settings (`/api/accessory`) are fetched the first time the player is drawn bent with the
  `mobends:accessories` layer, or swings a sword with a trail, and only if the API answered the
  join (a refused join still counts: the API is up). Only players are fetched: any other entity
  wears nothing and has a white trail. A failed fetch is tried again after 30 s at the soonest.
- The join's `version` is `<mod>/<minecraft>-<loader>` (`2.0.0-alpha.1/1.21.1-fabric`), as 1.12.2's
  `VERSION_STRING` (`2.0.0-alpha.2/1.12.2-forge`). The API's version check refuses both today (it
  takes no `.` after the `-`, nor this layout); see TODO.md, *Supporters*.
- The `checksum` is the SHA-256 of the mod's jar (`Platform.modFile()`), `dev` in development.
- The textures and models of the downloaded assets are read from disk as they're first drawn
  (`AssetTextures`, `AssetModels`), not through the resource manager, so a resource reload doesn't
  touch them. New assets are read again once downloaded.

What the Mo' Bends menu uses:

| Class | Method | What |
|-------|--------|------|
| `MoBendsAccount` | `getCurrent()` | The signed-in Mo' Bends account (`Session.displayName()`), or null |
| | `verify(onChange)` | Checks the session as the menu opens; `onChange` runs if it signed out or renamed |
| | `logOut()` | Signs out and revokes the token |
| `MinecraftLink` | `start(code, onStarted, onFailed)` | Step 1: the account the code belongs to (`Start.displayName()`) |
| | `confirm(start, onLinked, onFailed)` | Step 2: links and signs in; `Failure.translationKey()` for the error |
| `SupporterContent` | `getAccessories()` | The catalogue, by key, in the API's order (`AccessoryDetails`) |
| | `getAccessoryDetailsRevision()`, `fetchAccessoryDetails()` | Notice a new catalogue; fetch it again |
| | `getAccessorySettingsMapFor(entity)`, `getSettingsFor(entity, key)` | What a player wears (`AccessorySettings`) |
| | `setPreviewAccessories(entity, settings)` | Dresses a preview entity in chosen accessories |
| | `forgetPlayer(name)` | Fetches a player's settings again |
| `ConnectionManager` | `refresh()` | The menu's refresh: joins again if needed, fetches the catalogue, assets and settings anew |
| `WebLink` | `EDITOR`, `SUPPORT`, `ACCOUNT`: `fetch(onUrl)`, `open()` | The static API's links |

Every callback runs on the render thread.

The rest of this page describes the 1.12.2 mod.

Base URL default: `https://mobends.com` (overridable via `~/.minecraft/config/mobends/env.json` → `apiUrl`).

HTTP calls use plain `HttpURLConnection` + Gson via `ConnectionHelper`. Background threads are daemon threads managed by `ConnectionManager`.

## Endpoints

### Activity / Analytics

| Method | Path | When | Payload |
|--------|------|------|---------|
| POST | `/api/activity/join` | Mod init (once) | `{app: "mobends", version: "<version>"}` |
| POST | `/api/activity/ping` | Every `pingInterval` ms | *(empty)* |

`/join` response returns `pingInterval` (ms); `PingTask` (on a thread of `ConnectionManager`) uses it to schedule recurring `/ping` calls.

### Cosmetics / Accessories

| Method | Path | When | Response |
|--------|------|------|---------|
| GET | `/api/accessory/details` | `FMLPreInitializationEvent` | `Map<String, AccessoryDetails>` — all cosmetic definitions |
| GET | `/api/accessory?username={name}&uuid={uuid}` | On-demand per player | `Map<String, AccessorySettings>` — per-player unlock/color/visibility |

Results are cached in `SupporterContent`:
- `accessoryDetailsMap` — all cosmetics
- `accessorySettingsPerPlayer` — keyed by player display name

Fetching is done in a background thread by `PlayerSettingsDownloader`, one `PlayerSettingsTask` per player.

`uuid` is the player's profile UUID (left out for non-players). It finds an account linked through the mod even after
the player renamed it; the API falls back to the usernames typed in on the dashboard.

### Account Linking

| Method | Path | When | Payload → Response |
|--------|------|------|---------|
| POST | `/api/minecraft-link/start` | The player enters a dashboard code | `{code}` → `{serverId, account: {displayName}}` |
| POST | `/api/minecraft-link/confirm` | Right after joining `serverId` on Mojang's session server | `{code, username}` → `{uuid, username, token, account: {displayName}}` |
| GET | `/api/minecraft-link/session` | The Mo' Bends menu opens | `Authorization: Bearer <token>` → `{account: {displayName}}`, 401 `invalid_token` once revoked |
| DELETE | `/api/minecraft-link/session` | Log out | `Authorization: Bearer <token>` → 204 |

Mo' Bends menu (G) → **Link account** (`GuiLinkAccount`). The player enters the code, confirms the Mo' Bends account
it belongs to, and `MinecraftLink` joins the `serverId` through authlib (`joinServer`), the way the game proves its
session to an online-mode server, then calls `/confirm`. The access token never leaves the game. Both steps run on
a thread of their own. The menu is only reachable in a world, so no server connection is being made meanwhile.
Errors come as `{error, message}` (`ApiErrorException`), and the screen shows each code as a message for the player.

Linking signs the game in to the Mo' Bends account: `MoBendsAccount` keeps the token `/confirm` hands out in
`config/mobends/account.json` (readable by the player only), for each Minecraft account the game is played with.
Once signed in, the menu shows the account's name (opening the dashboard) and Log out, which revokes the token. The
menu checks the session as it opens: a 401 signs out (the account was unlinked or linked elsewhere on the
dashboard), anything else keeps the session. An API without tokens leaves the game signed in by name only.
The API's spec is `docs/minecraft-link-api.md` in mobends-api.

### Assets

| Method | Path | When | Response |
|--------|------|------|---------|
| GET | `/api/asset/manifest` | Startup and the menu's Refresh button, on a background thread | `AssetManifest` with asset list and base URL |

Assets are downloaded to `~/.minecraft/config/mobends/assets/` and compared by version hash. Manifest cached as `asset_manifest.json`.

## Data Models

```
AccessoryDetails
  displayName: String
  parts: List<AccessoryPart>
    bindPoint: BindPoint   // which skeleton bone
    asset references

AccessorySettings
  unlocked: Boolean
  hidden: Boolean
  color: Color (RGBA)
```

## Error Handling

- `HttpHostConnectException` → silently ignored (offline play unaffected)
- JSON parse errors → logged, not rethrown
- An error status in a POST → `ApiErrorException` with the status and the `error` code
