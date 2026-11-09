package goblinbob.mobends.mixin;

import goblinbob.mobends.core.client.event.KeyboardHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Adds Mo' Bends' key to the game's before they're read from {@code options.txt}, so the player's
 * choice for it is read and saved with the others. A vanilla hook, the same on every loader (Fabric
 * API's key binding module, which isn't bundled, does the same).
 */
@Mixin(Options.class)
public abstract class OptionsMixin {
    @Shadow
    @Final
    @Mutable
    public KeyMapping[] keyMappings;

    @Inject(method = "load()V", at = @At("HEAD"))
    private void mobends$addKeyMappings(CallbackInfo ci) {
        keyMappings = KeyboardHandler.withKeyMappings(keyMappings);
    }
}
