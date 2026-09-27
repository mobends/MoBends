# Configuration

## Files

| File | Format | Purpose |
|------|--------|---------|
| `~/.minecraft/config/mobends.cfg` | Forge INI | The mod's options (`ModConfig`, editable in Forge's mod options screen, category `general`), and the client settings made in the Mo' Bends menu (per-entity enable, type and extension order) |
| `config/mobends.cfg` (server) | Forge INI | The same file on a server: also what the server allows resource packs, sent to players (see [server-networking.md](server-networking.md)) |
| `~/.minecraft/config/mobends/env.json` | JSON | Optional: override `apiUrl` |
| `~/.minecraft/config/mobends/assets/` | binary | Downloaded remote assets |
| `~/.minecraft/config/mobends/asset_manifest.json` | JSON | Version hashes for downloaded assets |

Animation content (types, extensions, animators, clips, model definitions) comes from mods and
resource packs, not from config files; see [content.md](content.md).

Forge's `@Config` and the Mo' Bends menu share one `Configuration` object for `mobends.cfg`
(`CoreConfig` takes the one Forge keeps), each with categories of its own, so a save from either
writes both. Two `Configuration`s on the file would each overwrite the other's changes.

## Mod Options (`ModConfig`)

| Option | Default | Effect |
|--------|---------|--------|
| `showArrowTrails` | `true` | Arrows leave a trail |
| `showSwordTrail` | `true` | Swords leave a trail |
| `performSpinAttack` | `true` | The player's sword combo ends with a spin attack (whirl slash), except while riding |
| `itemUseClassifications` | none | `item=classification` entries (wildcards allowed) deciding how using an item is animated |
| `itemAttackClassifications` | none | The same for attacking with an item |
| `keepArmorAsVanilla` | none | Armor items (patterns) drawn with their vanilla model |
| `keepEntityAsVanilla` | none | Entity types (patterns) that are never animated |

Changing them refreshes Mo' Bends (`CoreClient.refresh()`).

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
