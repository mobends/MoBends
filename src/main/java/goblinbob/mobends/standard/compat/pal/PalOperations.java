package goblinbob.mobends.standard.compat.pal;

import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
//? if >=26.1 {
/*import goblinbob.mobends.MoBends;
*///?}

/**
 * Player Animation Library (PAL), the library Better Combat, emote mods and others animate the
 * player with. PAL poses the vanilla model, which Mo' Bends doesn't draw: an animator can hand the
 * player to vanilla while PAL plays something ({@code core:vanilla}), or take the vanilla model's
 * pose, PAL's included, as its own ({@code core:vanilla_pose}). The Mo' Bends Compat+ pack does the
 * former.
 * <p>
 * PAL is optional: without it, or before 26.1 (whose PAL Mo' Bends isn't built against), the
 * operations hold for nothing. Only {@link PalAnimations} touches PAL's classes, and it is loaded
 * only when PAL is installed.
 */
public final class PalOperations {
    /** PAL's mod id. */
    public static final String MOD_ID = "player_animation_library";

    private PalOperations() {
    }

    public static void register() {
        // {"mobends:compat_pal_animation_playing": []}: PAL is playing an animation on the player
        // (any of its layers is active: an attack, a weapon's idle pose, an emote).
        KumoRegistry.registerOperation(KumoOperation.named("mobends:compat_pal_animation_playing")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(Player.class, args)) return null;
                    //? if >=26.1 {
                    /*if (MoBends.platform().isModLoaded(MOD_ID)) {
                        return (BooleanEvaluator) (context, values) -> context.entity() instanceof Player player
                                && PalAnimations.isPlaying(player);
                    }
                    *///?}
                    return (BooleanEvaluator) (context, values) -> false;
                }));
    }

    /**
     * Before the vanilla model is posed for {@code entity} ahead of its time (for
     * {@code core:vanilla_pose}): has PAL work out this frame's animation of a player now.
     */
    public static void beforeVanillaPose(LivingEntity entity) {
        //? if >=26.1 {
        /*if (entity instanceof Player player && MoBends.platform().isModLoaded(MOD_ID)) {
            PalAnimations.sample(player);
        }
        *///?}
    }
}
