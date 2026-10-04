package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberEvaluator;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.util.GUtil;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.ItemActions;
import goblinbob.mobends.standard.UseActionType;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.passive.EntityWolf;

/**
 * The {@code mobends:} operations: Mo' Bends' own classifications and settings, and what the
 * mobs it animates in code tell.
 */
public final class MoBendsOperations
{

    private MoBendsOperations()
    {
    }

    public static void register()
    {
        // {"mobends:use_action": ["bow"]}: the item in use is used as a bow, as Mo' Bends classifies
        // it (ItemActions, which the config can override).
        KumoRegistry.registerOperation(KumoOperation.named("mobends:use_action")
                .choice("action", "food", "bow", "shield")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(EntityLivingBase.class, args)) return null;
                    UseActionType action = UseActionType.valueOf(args.string(0).toUpperCase());
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        return entity != null && action == ItemActions.getItemUseAction(entity.getActiveItemStack().getItem(),
                                ItemActions.armPoseOf(entity, entity.getHeldItemMainhand()),
                                ItemActions.armPoseOf(entity, entity.getHeldItemOffhand()));
                    };
                }));
        // {"mobends:attack_action": ["sword"]}: the held item attacks as a sword.
        KumoRegistry.registerOperation(KumoOperation.named("mobends:attack_action")
                .choice("action", "fists", "sword", "tool")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(EntityLivingBase.class, args)) return null;
                    AttackActionType action = AttackActionType.valueOf(args.string(0).toUpperCase());
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        return entity != null && action == ItemActions.getItemAttackAction(entity.getHeldItemMainhand().getItem());
                    };
                }));
        // {"mobends:spin_attack_enabled": []}: the player's spin attack is switched on in the config.
        KumoRegistry.registerOperation(KumoOperation.named("mobends:spin_attack_enabled")
                .returns(Expression.Type.BOOLEAN)
                .bind(args -> (BooleanEvaluator) (context, values) -> ModConfig.performSpinAttack));

        // The wolf's own: vanilla's angles, in degrees, interpolated by partialTicks where vanilla does.
        KumoRegistry.registerEntityCondition("mobends:is_sitting", EntityWolf.class, EntityWolf::isSitting);
        KumoRegistry.registerEntityNumber("mobends:wolf_interested_angle", EntityWolf.class,
                wolf -> wolf.getInterestedAngle(DataUpdateHandler.partialTicks) * GUtil.RAD_TO_DEG);
        KumoRegistry.registerEntityNumber("mobends:wolf_tail_rotation", EntityWolf.class, wolf -> wolf.getTailRotation() * GUtil.RAD_TO_DEG);
        // {"mobends:wolf_shake_angle": [-0.08]}: how far a part shakes off water, a part further back lagging by its offset.
        KumoRegistry.registerOperation(KumoOperation.named("mobends:wolf_shake_angle")
                .param("offset", Kind.CONSTANT)
                .returns(Expression.Type.NUMBER).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(EntityWolf.class, args)) return null;
                    float offset = args.constant(0);
                    return (NumberEvaluator) (context, values) -> {
                        EntityWolf wolf = (EntityWolf) context.entity();
                        return wolf == null ? 0 : wolf.getShakeAngle(DataUpdateHandler.partialTicks, offset) * GUtil.RAD_TO_DEG;
                    };
                }));

        // The spider's: it is beside a block it climbs.
        KumoRegistry.registerEntityCondition("mobends:is_beside_climbable", EntitySpider.class, EntitySpider::isBesideClimbableBlock);
    }

}
