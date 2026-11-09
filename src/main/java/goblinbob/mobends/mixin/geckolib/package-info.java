/**
 * Mixins into GeckoLib, which Mo' Bends doesn't need: listed in mobends.geckolib.mixins.json, which
 * isn't required, and each {@code @Pseudo}, so nothing fails without GeckoLib. Nothing outside this
 * package touches GeckoLib's classes. Like the rest of the loader-independent code, they must not use
 * a loader API.
 */
package goblinbob.mobends.mixin.geckolib;
