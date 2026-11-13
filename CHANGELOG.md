# Changelog

## Unreleased

Mo' Bends 2.X on Minecraft 1.8.9, ported from the version for 1.12.2: the first release for 1.8 since
0.21.3. It is built on the same engine as 2.0.0-alpha.4 for the modern versions (`mobends-core`
2.0.0-alpha.4). Everything below is new since 0.21.3.

### Added

- Runs on Minecraft 1.8.9 (Forge 11.15.1.2318).
- Skeletons, wither skeletons, zombie villagers, zombie pigmen, squids, wolves, creepers, villagers,
  witches and iron golems are animated, as well as players, zombies and spiders. Wolves sit, stand up
  and breathe; a charged creeper's armour moves with it.
- Players, zombies, skeletons, villagers, witches and iron golems stand on their feet: standing still,
  the feet stay planted and they turn by stepping, and when they stop walking their feet stay where the
  walk left them. In the sword's stance a player stands with their feet wide apart.
- Iron golems jump, and climb onto blocks: they grab the top, haul themselves up and step on.
- Heads look where the creature looks, however its body bends or sways.
- Players animate climbing, riding, sitting, falling, flying, eating, breaking blocks and blocking with
  a sword, and players flying in creative or spectator mode are seen flying. Capes stream behind them.
- More weapon styles: the sword combo, punches and tool swings are each animated on their own, and
  the config can put any item in a weapon or use style.
- Armour and the second skin layer bend with the body, and swords and arrows leave trails.
- Animation layers and transitions fade into each other instead of jumping.
- The Mo' Bends menu (G) turns animation on or off for each mob, and has the options their animations
  offer (the player's spin attack), the order of types and extensions, and a Refresh button. Forge's
  Config button for Mo' Bends opens it.
- Supporter accessories are drawn on players. _Supporters_ in the menu shows every one on your own
  character, and _Link account_ links your Minecraft account to your Mo' Bends account.
- Resource packs and mods can change everything without code: every mob is an animator and a model
  definition in `assets/<namespace>/bends/`, and types and extensions give a mob, or one player, a
  different model or animations on top of its own. Mods can add operations and drivers in Java.
- Servers decide what resource packs may do to animation: allow it, limit how far it moves models (the
  default), or deny it. Packs signed by Mo' Bends are always trusted.
- When an animation breaks, the chat says which one and why.
- Forge tells you when a newer version of Mo' Bends is out.
- French, Italian, Norwegian, Polish, Brazilian Portuguese and Russian translations.

### Changed

- Every mob runs on the KUMO animation engine (see `misc/kumo-format.md`).
- Only players end the sword combo with a spin attack, and it is turned off in the player's options in
  the Mo' Bends menu.

### Removed

- Bends Packs: resource packs and extensions replace them.

What 1.8.9 lacks, Mo' Bends lacks too: the off hand is always empty and everyone is right-handed, only
players use items (a skeleton never draws its bow, as in vanilla 1.8.9), and there's no elytra and no
GeckoLib armour.

## 0.21.3 (2015-12-25)

The last version for Minecraft 1.8, and the last before the rewrite for 1.12.2.

### What it had

- Players animate running, sneaking, jumping, swimming and riding, aiming a bow, punching from a
  boxing stance, and a three-hit sword combo ending in a spin attack.
- Alex's skin model and every skin layer.
- Zombies and spiders are animated; spiders jump at their target and climb walls.
- Player animations and the spin attack can be turned off while playing.
- Bends Packs change animations.

### Added

- Bends Packs can change the attack stance, the three sword attacks, the punching stance and the punch
  (`attack_stance`, `attack_0`, `attack_1`, `attack_2`, `punch_stance`, `punch`).

### Fixed

- Faulty configuration files.

---

Versions before 0.21.3 predate this repository's history.
