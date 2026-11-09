# Mo' Bends — Technical Docs

These docs describe the 1.12.2 mod, whose sources are in [`legacy-1.12/`](../legacy-1.12/README.md) on
`main`, apart from the engine (`core/`) and the asset formats, which every version shares. They are
updated as each part is ported into `src/`.

| Doc | Contents |
|-----|---------|
| [porting.md](porting.md) | The port to the modern versions: where it stands, how it's done, what's next, and where `forge-1.12`'s steps to keep up are |
| [overview.md](overview.md) | Gradle modules, package layout, module system, addon extension point |
| [animation.md](animation.md) | The KUMO runtime: subjects, animators, poses, the per-frame pipeline |
| [addons.md](addons.md) | For other mods: adding mobs, layers, components, drivers and KUMO operations in Java (modern versions) |
| [content.md](content.md) | Entity types, extensions, model definitions, and what resource packs may do |
| [rendering.md](rendering.md) | Forge event hooks, mutation model, skeleton/model parts |
| [testing.md](testing.md) | The animation lab, golden traces, and where animators deliberately differ from them |
| [server-networking.md](server-networking.md) | Server-enforced rules, message protocol |
| [api-communication.md](api-communication.md) | Analytics, cosmetics, asset download endpoints |
| [configuration.md](configuration.md) | Config files and their schemas |

The animator and asset formats themselves are documented in [`misc/kumo-format.md`](../misc/kumo-format.md).
