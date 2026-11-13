# TODO

## Web animation editor

The *Customize* entry of the Mo' Bends menu (`GuiBendsMenu`) is meant to open the web animation
editor, which is in the works. It opens the `officialAnimationEditorUrl` of
[static-api.json](https://github.com/mobends/mobends-resources/blob/master/static-api.json)
(`WebLink.EDITOR`), currently `https://mobends.com/roadmap`, which is also the fallback.

- [ ] Point `officialAnimationEditorUrl` at the web editor once it is live (no release needed).
- [ ] Decide how animations made in the editor get into the game (a resource pack to download, or
      something the mod fetches).

## Supporters

The *Supporters* entry of the Mo' Bends menu (`GuiSupportersWindow`) shows every supporter
accessory on the player. Its *Support Mo' Bends* button opens the `supportUrl` of
[static-api.json](https://github.com/mobends/mobends-resources/blob/master/static-api.json)
(`WebLink.SUPPORT`). The `accountUrl` (`WebLink.ACCOUNT`) is opened by the linked account's name
on the Mo' Bends menu (`GuiBendsMenu`) and by *Open Dashboard* on the linking screen
(`GuiLinkAccount`). Both fall back to `https://mobends.com`.

- [ ] Show the tiers (one-time collection, subscription, editor tier) once they exist.

## Animations

- [ ] IK for cows and sheep (feet planted on the ground), switched off while they run.

## Multiple Minecraft versions and loaders

The modern Minecraft versions and loaders are built on `main`, and its TODO.md tracks that plan.
This branch shares only the engine with them: `mobends-core`, released from `core/` on `main` and
used here at `core_version` (gradle.properties).

## Keeping up with `main`

This branch doesn't merge from `main`, so whatever `main` changes that this branch has to follow
comes here as steps: a release of `core` to build with, a change to the shared asset format, an asset
changed on `main` (this branch keeps its own copy of `assets/mobends/bends/`), a new definition or
animator key, goldens the lab has to re-record. Every pull request on `main` that makes such a change
comes with a pull request here that adds its steps below, under the `main` PR's number (see
CONTRIBUTING.md, *Branches*). Steps that need a new `core` wait for its release; the others can be
done right away. Once a group is done and merged here, delete it.

### #392: The pig zombie's variant for later versions

Doesn't need a new `core`. On `main` the zombified piglin bends: `pig_zombie.json` (the mod's and the
legacy pack's) got a `@variants` entry that names the piglin's model on versions after 1.12.2. Nothing
changes in 1.12.2: the entry's `@when` doesn't hold on 1.12.2.

- [ ] Copy `assets/mobends/bends/models/pig_zombie.json` and
      `misc/legacy-pack/assets/mobends/bends/models/pig_zombie.json` from `main`.
- [ ] Check that the animation lab passes (`LegacyPackTest` compares the two), and in the game that the
      zombie pigman still bends as before, with the mod and with the legacy pack.

### #437: Wither skeletons are animated

Doesn't need a new `core`. On `main` the wither skeleton bends as the skeleton does. The skeleton's
definition names `EntitySkeleton`, which the wither skeleton isn't (both extend `AbstractSkeleton`, in
1.12.2 too), so it was left vanilla here as well.

- [ ] Copy `src/main/resources/assets/mobends/bends/models/wither_skeleton.json` from `main`. It
      extends the skeleton's and names `net.minecraft.entity.monster.EntityWitherSkeleton`, a single
      class name, which this branch reads as is.
- [ ] Give it a type file, as the skeleton has: copy
      `src/main/resources/assets/mobends/bends/types/wither_skeleton.json` from `main` (id
      `mobends:wither_skeleton`, selector `{"core:entity_type": ["minecraft:wither_skeleton"]}`).
      Mobs aren't registered in `DefaultAddon` any more (`registerDefinedEntity` is gone).
- [ ] Add `"mobends:wither_skeleton"` to the lab's `DefinedModelsTest` (`theShippedTypesKeepTheirIds`'s
      expected ids, and next to the skeleton in `IDLE_MOVES` and `STAND_WHERE_THEY_STOPPED`), and
      `DefinitionExtendsTest`'s `theWitherSkeletonIsTheSkeletonOfItsOwnEntity`, as on `main`.
- [ ] Mention it in `misc/kumo-format.md` (the list of model definitions, and `extends`) and in
      CHANGELOG.md.
- [ ] Check in the game (`/summon wither_skeleton`): it walks, attacks with its stone sword and stands
      on its feet as the skeleton does, at its bigger size, and the Settings menu lists it with its
      own switch. The skeleton, the stray and the wither (the boss) are unchanged.

### #492: `@when` on type and extension files

Doesn't need a new `core`. On `main` a type or extension file can say which Minecraft versions it's
for (`"@when": {"core:minecraft_version": [">=", "1.10"]}`): on the others it's left out as if it
weren't there, and a type's model isn't read. 1.12.2 reads a file with an `@when` already (Gson skips
the key) but uses it whatever it says, so a file meant for other versions only would load here too.

- [ ] Port it from `main`: `LoadCondition` (shared with `DefinitionMerge.applyVariants`), the `@when`
      field and `applies()` of `EntityTypeDefinition` and `ExtensionDefinition`, and leaving out what
      doesn't apply in `TypeFiles.read` and `loadExtension` (and its caller in the registry).
- [ ] Port the lab's `aTypeWhoseWhenDoesntHoldIsLeftOut` and `anExtensionWhoseWhenDoesntHoldIsLeftOut`,
      with versions around 1.12.2 (`["<", "1.10"]` doesn't hold here).
- [ ] Take `misc/kumo-format.md`'s passages on it from `main`.

## Other tasks

- [ ] Make `field` resolve every vanilla field in production. The generated accessors
  (`VanillaEntityFields`) cover numeric fields only, so a vanilla boolean field (`isCharging`) or a
  step through a vanilla object field (`ridingEntity`) is found by reflection, under its
  development name: it resolves in development only, and in the game takes its `@fallback`.
  Generating accessors for boolean and object fields too (`GenVanillaFields.kt`) might be the
  answer. Nothing shipped reads one yet.
- [ ] Add a `machineTicksElapsed` built-in if machines that keep their own clock become common.
  Today a machine that needs one keeps it as `machine.` state advanced in its `update` list; the
  built-in would be named for its scope, as `nodeTicksElapsed` and `layerTicksElapsed` are, not
  an operation taking the scope as an argument.
- [ ] Consider an `@override` modifier on definitions, so an animator that `extends` another can
  redeclare one of the parent's `animator.` names (today that is an error). A definition's other
  `@` keys are where such per-definition rules go, as might what a type publishes to its
  extensions.
