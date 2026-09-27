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
| GET | `/api/player/{playerName}/settings` | On-demand per player | `Map<String, AccessorySettings>` — per-player unlock/color/visibility |

Results are cached in `SupporterContent`:
- `accessoryDetailsMap` — all cosmetics
- `accessorySettingsPerPlayer` — keyed by player display name

Fetching is done in a background thread by `PlayerSettingsDownloader`, one `PlayerSettingsTask` per player.

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
