package goblinbob.mobends.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.event.KeyboardHandler;
import goblinbob.mobends.platform.Platform;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** The client ticks, the resource reloads, which bring new definitions and animators, and the Mo' Bends menu's key. */
@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    /**
     * With the game's own reload listeners (just before its first), before the first reload is
     * started. A wrap, not an @Inject: Forge 1.20.1's Mixin injects into a constructor only at its end.
     */
    @WrapOperation(
        method = "<init>",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/packs/resources/ReloadableResourceManager;registerReloadListener(Lnet/minecraft/server/packs/resources/PreparableReloadListener;)V",
            ordinal = 0
        )
    )
    private void mobends$registerReloadListener(ReloadableResourceManager resourceManager, PreparableReloadListener listener,
                                                Operation<Void> original) {
        if (!Platform.INSTANCE.registersReloadListener()) {
            resourceManager.registerReloadListener((ResourceManagerReloadListener) manager -> CoreClient.reloadAnimation());
        }
        original.call(resourceManager, listener);
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
