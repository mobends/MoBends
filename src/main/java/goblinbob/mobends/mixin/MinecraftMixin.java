package goblinbob.mobends.mixin;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.event.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The client ticks, the resource reloads, which bring new definitions and animators, and the Mo' Bends menu's key. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    /** With the game's own reload listeners, before the first reload is started. */
    @Inject(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
            ordinal = 0
        )
    )
    private void mobends$registerReloadListener(CallbackInfo ci) {
        resourceManager.registerReloadListener((ResourceManagerReloadListener) manager -> CoreClient.reloadAnimation());
    }

    /** The key that opens the Mo' Bends menu, with the game's own keys (in the game, no screen open). */
    @Inject(method = "handleKeybinds()V", at = @At("HEAD"))
    private void mobends$handleKeybinds(CallbackInfo ci) {
        KeyboardHandler.onKeybinds();
    }

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void mobends$tick(CallbackInfo ci) {
        DataUpdateHandler.onClientTick();
    }
}
