# API Communication

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
| GET | `/api/asset/manifest` | Startup and F10, on a background thread | `AssetManifest` with asset list and base URL |

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
