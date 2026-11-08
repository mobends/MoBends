package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.ItemActions;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;

import java.util.Locale;

/** The {@code mobends:} operations: Mo' Bends' own classifications of what an entity holds, and the player's skin. */
public final class MoBendsOperations {
    private MoBendsOperations() {
    }

    public static void register() {
        // {"mobends:use_action": ["bow"]}: the item in use is used as a bow, as Mo' Bends classifies
        // it (ItemActions).
        KumoRegistry.registerOperation(KumoOperation.named("mobends:use_action")
                .choice("action", "food", "bow", "shield")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(LivingEntity.class, args)) return null;
                    UseActionType action = UseActionType.valueOf(args.string(0).toUpperCase(Locale.ROOT));
                    return (BooleanEvaluator) (context, values) -> {
                        LivingEntity entity = (LivingEntity) context.entity();
                        return entity != null && action == ItemActions.getItemUseAction(entity.getUseItem(),
                                ItemActions.armPoseOf(entity, entity.getMainHandItem()),
                                ItemActions.armPoseOf(entity, entity.getOffhandItem()));
                    };
                }));
        // {"mobends:attack_action": ["sword"]}: the held item attacks as a sword.
        KumoRegistry.registerOperation(KumoOperation.named("mobends:attack_action")
                .choice("action", "fists", "sword", "tool")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(LivingEntity.class, args)) return null;
                    AttackActionType action = AttackActionType.valueOf(args.string(0).toUpperCase(Locale.ROOT));
                    return (BooleanEvaluator) (context, values) -> {
                        LivingEntity entity = (LivingEntity) context.entity();
                        return entity != null && action == ItemActions.getItemAttackAction(entity.getMainHandItem());
                    };
                }));

        // {"mobends:skin_variant": ["slim"]}: a player whose skin has this arm width. Players read as
        // default until their skin has downloaded, so the answer can change.
        KumoRegistry.registerOperation(KumoOperation.named("mobends:skin_variant")
                .choice("variant", "default", "slim")
                .returns(Expression.Type.BOOLEAN).selectorSafe(false)
                .bind(args -> {
                    PlayerSkin.Model variant = "slim".equals(args.string(0)) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof AbstractClientPlayer player
                            && player.getSkin().model() == variant;
                }));
    }
}
