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

- [x] Add the `MOBENDS_PACK_SIGNING_KEY` secret, and have CI build the pack on release tags and
      attach it to the GitHub release (`.github/workflows/release.yml`).
- [x] Have CI upload the pack to Modrinth and CurseForge (the `publish-legacy-pack` job of
      `.github/workflows/release.yml`), switched off for now.
- [ ] Once the pack has its Modrinth and CurseForge pages (around the alpha's release), set the
      `LEGACY_PACK_MODRINTH_PROJECT_ID` and `LEGACY_PACK_CURSEFORGE_PROJECT_ID` repository variables
      and switch the job back on: delete its `if: false`. Check the first upload on CurseForge,
      where a resource pack takes no loader.

## Animations

- [ ] IK for cows and sheep (feet planted on the ground), switched off while they run.

## Mo' Bends menu

- [x] Improve the look of the *Settings* submenu (`GuiSettingsWindow`, `GuiBenderList`): each mob
      shown with an orthographic render of it as a thumbnail (clipped to its frame), its name, etc.

## Multiple Minecraft versions and loaders

The modern Minecraft versions and loaders are built on `main`, and its TODO.md tracks that plan.
This branch shares only the engine with them: `mobends-core`, released from `core/` on `main` and
used here at `core_version` (gradle.properties).

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
