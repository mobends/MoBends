# Configuration

## Files

| File | Format | Purpose |
|------|--------|---------|
| `~/.minecraft/config/mobends.cfg` | Forge INI | Client settings (per-entity enable, type and extension order) |
| `~/.minecraft/config/mobends/env.json` | JSON | Optional: override `apiUrl` |
| `~/.minecraft/config/mobends/assets/` | binary | Downloaded remote assets |
| `~/.minecraft/config/mobends/asset_manifest.json` | JSON | Version hashes for downloaded assets |

Animation content (types, extensions, animators, clips, model definitions) comes from mods and
resource packs, not from config files; see [content.md](content.md).

## Client Config (`CoreClientConfig`)

```ini
[Animated]
player = true
zombie = true
# ... one entry per registered entity bender

[TypeRanks]
# the order the user gave matching entity types (Settings → Order), by type id; absent means 0

[ExtensionRanks]
# the order the user gave an entity's extensions (Settings → Extensions), by extension id; absent means 0
```

## Environment Override (`EnvironmentModule`)

```json
{ "apiUrl": "http://localhost:3000" }
```

Only loaded if the file exists. Used for development against a local backend.

## Network Configuration (server-sent, not persisted)

Received via `MessageConfigResponse` on each world join. See [server-networking.md](server-networking.md).
