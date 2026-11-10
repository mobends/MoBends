# Mo' Bends — Technical Docs

These docs describe the mod on `main` (the modern versions, `src/`), the engine (`core/`) and the
asset formats, which every version shares. The 1.12.2 mod lives on the `forge-1.12` branch; where a
doc still describes a 1.12.2 detail, that branch's code is what it means.

| Doc | Contents |
|-----|---------|
| [overview.md](overview.md) | Gradle modules, package layout, module system, addon extension point |
| [animation.md](animation.md) | The KUMO runtime: subjects, animators, poses, the per-frame pipeline |
| [addons.md](addons.md) | For other mods: adding mobs, layers, components, drivers and KUMO operations in Java (modern versions) |
| [content.md](content.md) | Entity types, extensions, model definitions, and what resource packs may do |
| [rendering.md](rendering.md) | How bent models are drawn (Mixins on the modern versions, Forge events on 1.12.2), mutation model, skeleton/model parts |
| [testing.md](testing.md) | The animation lab, golden traces, and where animators deliberately differ from them |
| [server-networking.md](server-networking.md) | Server-enforced rules, message protocol |
| [api-communication.md](api-communication.md) | Analytics, cosmetics, asset download endpoints |
| [configuration.md](configuration.md) | Config files and their schemas |

The animator and asset formats themselves are documented in [`misc/kumo-format.md`](../misc/kumo-format.md).
