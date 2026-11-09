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

- [x] Add `supportUrl` (the Patreon page) and `accountUrl` (the mobends.com dashboard) to static-api.json.
- [ ] Show the tiers (one-time collection, subscription, editor tier) once they exist.

## Legacy Mo' Bends pack

- [x] Built, signed and released from `main` (for 1.12.2 and the modern versions alike), whose
      TODO.md tracks its upload. The signing tools moved there from `src/tools`.

## Animations

- [ ] IK for cows and sheep (feet planted on the ground), switched off while they run.

## Mo' Bends menu

- [x] Improve the look of the *Settings* submenu (`GuiSettingsWindow`, `GuiBenderList`): each mob
      shown with an orthographic render of it as a thumbnail (clipped to its frame), its name, etc.

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

### #383: `core` 2.0.0-alpha.3

Waits for the `core-2.0.0-alpha.3` tag on `main` (see CONTRIBUTING.md there, *Releasing `core`*). The
release only adds to the API (`goblinbob.mobends.core.kumo.MinecraftVersion` and the
`core:minecraft_version` operation); nothing is removed or changed.

- [ ] Set `core_version=2.0.0-alpha.3` in `gradle.properties` (the mod and the animation lab both
      read it).
- [ ] Call `MinecraftVersion.set(ModStatics.MCVERSION)` first thing in `Core.preInit`, before
      `MinecraftKumoOperations.register()` and before any animator or definition loads. Nothing
      breaks without it today, but the first asset that uses `core:minecraft_version` would fail to
      load (the operation fails to compile before the version is set).
- [ ] Optionally, apply `@variants` in model definitions too, instead of ignoring them: port
      `DefinitionMerge.applyVariants` from `main` and call it in `ModelDefinitions.resolve`, after
      `extends` is merged. The shipped assets don't need it: the wolf's only variant is for versions
      after 1.12.2.

### #383: The wolf matches vanilla

Doesn't need a new `core`. On `main` the wolf's head, legs and tail now sit and turn where vanilla's
do, with vanilla's texture on every face (the vanilla wolf's geometry is the same in 1.12.2).

- [ ] Copy `assets/mobends/bends/models/wolf.json` and `assets/mobends/bends/animators/wolf.json` from
      `main`. The animator only changes the tail's angle (measured from the idle clip's tail, 72.93°,
      instead of from 90°). The model's `@variants` is ignored here until it's supported (above), and
      its `childBones` are already the 1.12.2 placement of the pup's head.
- [ ] Teach box faces `"flip": true` (mirrors a face's texture left to right, before it's turned),
      which the wolf's body and mane now use: `BoneDefinition.FaceDefinition.flip`,
      `BoxFactory.TextureFace.flipped` and `BoxFactory.flipTextureQuad`, `MutatedBox` swapping the face's
      `u1` and `u2` when it's flipped, and `DefinedMutator.createBox` applying it. `main`'s
      `BoneDefinition`, `BoxFactory`, `MutatedBox` and `DefinedModelBuilder` have it. Without it, the
      undersides and ends of the body and mane show the wrong part of the texture.
- [ ] Move the inside of the mouth in `LayerWolfMisc` with the head, which now turns 2 px further
      forward and 1 px left of its middle: `mouthInside` at `-0.5F, -2.1F` instead of `-1.5F, -4.1F`
      (as `WolfMiscLayer` on `main`).
- [ ] Re-record the lab's wolf goldens: `./gradlew record --args="$PWD/golden wolf/idle_walk_sit
      wolf/interest_and_shake"` in `animation-lab/`. Only the tail should differ (8.5°, every frame);
      check with `compare` first. Note it in `docs/testing.md`, as on `main`.
- [ ] Check it in the game, side by side with vanilla (an extension whose only layer is a
      `core:vanilla` node, on every other second): the adult and the pup, looking up and around, the
      tail, the legs, and the mouth sitting in the head.

### #388: The first-person hand is vanilla's

Doesn't need a new `core`. On `main` the first-person hand is never bent: it's vanilla's arm and
sleeve, posed as vanilla poses them (its slight outward tilt included). The `renderer.firstPersonRest`
definition key, which only put the bent arm at rest for the hand, is gone from the format. Do the steps
together: without the key, this branch would draw the hand in the player's animated pose.

- [ ] Draw the vanilla hand in first person: `RenderingEventHandler.beforeHandRender` puts the
      renderer's vanilla state in place for the hand (as it already does when the player isn't
      animated), instead of the local player's mutation. Then remove what only served the bent hand:
      `Mutator.poseForFirstPersonView` and `DefinedMutator`'s override, and the stand-ins' drawing of
      their bone alone (`DefinedStandIn.setDrawsAlone`, `DefinedMutator.parentedStandIns`) if nothing
      else uses it.
- [ ] Remove `renderer.firstPersonRest`: the field and its check in `EntityModelDefinition`, the lab's
      `DefinedPositionsTest.theFirstPersonRestNamesBones`, and its mention in `misc/kumo-format.md`'s
      `renderer` row (as on `main`).
- [ ] Copy `assets/mobends/bends/models/player.json` and
      `misc/legacy-pack/assets/mobends/bends/models/player.json` from `main`, or drop
      `"firstPersonRest"` from their `renderer`: that's all that changed in them.
- [ ] Check in the game, in first person: the hand matches vanilla's (an empty hand, holding an item,
      eating, swinging), while standing, sprinting and right after a sword combo; slim and wide skins;
      sleeves on and off. Then in third person, that the player still bends.

### #392: The pig zombie's variant for later versions

Doesn't need a new `core`. On `main` the zombified piglin bends: `pig_zombie.json` (the mod's and the
legacy pack's) got a `@variants` entry that names the piglin's model on versions after 1.12.2. Nothing
changes in 1.12.2: it ignores `@variants`, and the entry's `@when` doesn't hold on 1.12.2 anyway.

- [ ] Copy `assets/mobends/bends/models/pig_zombie.json` and
      `misc/legacy-pack/assets/mobends/bends/models/pig_zombie.json` from `main`.
- [ ] Check that the animation lab passes (`LegacyPackTest` compares the two), and in the game that the
      zombie pigman still bends as before, with the mod and with the legacy pack.

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
