package goblinbob.mobends.standard.kumo;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.api.NumberEvaluator;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.standard.ItemActions;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.client.player.AbstractClientPlayer;
//? if >=1.21 {
import net.minecraft.client.resources.PlayerSkin;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Spider;

import java.util.Locale;

/**
 * The {@code mobends:} operations: Mo' Bends' own classifications of what an entity holds, the
 * player's skin, and what the mobs it animates in code (the wolf, the spider) tell.
 */
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
                    //? if >=26.1 {
                    /*net.minecraft.world.entity.player.PlayerModelType variant = "slim".equals(args.string(0))
                        ? net.minecraft.world.entity.player.PlayerModelType.SLIM : net.minecraft.world.entity.player.PlayerModelType.WIDE;
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof AbstractClientPlayer player
                            && player.getSkin().model() == variant;
                    *///?} else if >=1.21 {
                    PlayerSkin.Model variant = "slim".equals(args.string(0)) ? PlayerSkin.Model.SLIM : PlayerSkin.Model.WIDE;
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof AbstractClientPlayer player
                            && player.getSkin().model() == variant;
                    //?} else {
                    /*// 1.20.1 names the skin's model "slim" or "default".
                    String variant = args.string(0);
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof AbstractClientPlayer player
                            && variant.equals(player.getModelName());
                    *///?}
                }));

        // The wolf's own: vanilla's angles, in degrees, interpolated by partialTicks where vanilla does.
        // 1.12's isSitting was the sitting pose (not the order to sit), today's isInSittingPose.
        KumoRegistry.registerEntityBooleanReader("mobends:is_sitting", Wolf.class, Wolf::isInSittingPose);
        // 1.12's getInterestedAngle, the head's tilt when it begs: today's getHeadRollAngle.
        KumoRegistry.registerEntityFloatReader("mobends:wolf_interested_angle", Wolf.class,
                wolf -> wolf.getHeadRollAngle(DataUpdateHandler.partialTicks) * Mth.RAD_TO_DEG);
        // 1.12's getTailRotation: today's getTailAngle.
        KumoRegistry.registerEntityFloatReader("mobends:wolf_tail_rotation", Wolf.class, wolf -> wolf.getTailAngle() * Mth.RAD_TO_DEG);
        // {"mobends:wolf_shake_angle": [-0.08]}: how far a part shakes off water, a part further back
        // lagging by its offset (1.12's getShakeAngle: today's getBodyRollAngle).
        KumoRegistry.registerOperation(KumoOperation.named("mobends:wolf_shake_angle")
                .param("offset", Kind.CONSTANT)
                .returns(Expression.Type.NUMBER).withFallback()
                .bind(args -> {
                    if (!MinecraftKumoOperations.applies(Wolf.class, args)) return null;
                    float offset = args.constant(0);
                    //? if >=26.1 {
                    /*return (NumberEvaluator) (context, values) -> context.entity() instanceof Wolf wolf
                            ? wolfBodyRoll(wolf.getShakeAnim(DataUpdateHandler.partialTicks), offset) * Mth.RAD_TO_DEG : 0;
                    *///?} else {
                    return (NumberEvaluator) (context, values) -> context.entity() instanceof Wolf wolf
                            ? wolf.getBodyRollAngle(DataUpdateHandler.partialTicks, offset) * Mth.RAD_TO_DEG : 0;
                    //?}
                }));

        // The spider's: it is beside a block it climbs (1.12's isBesideClimbableBlock: today's isClimbing).
        KumoRegistry.registerEntityBooleanReader("mobends:is_beside_climbable", Spider.class, Spider::isClimbing);
        // The horizontal angle of the wall the spider climbs, 0 when it climbs none.
        KumoRegistry.registerEntityFloatReader("mobends:spider_wall_rotation", Spider.class, MoBendsOperations::wallRotation);
    }

    /** The walls 1.12 looked for, in its order: the first one found is the one climbed. */
    private static final Direction[] WALLS = { Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST };

    /**
     * The angle of the first block beside the climbing spider's feet (a wall it climbs), as 1.12
     * measured it (its {@code EnumFacing.getHorizontalAngle}, today's {@code toYRot}); 0 when it
     * climbs none.
     */
    private static float wallRotation(Spider spider) {
        if (!spider.onClimbable()) {
            return 0;
        }
        BlockPos position = BlockPos.containing(spider.getX(), spider.getY(), spider.getZ());
        for (Direction wall : WALLS) {
            if (!spider.level().getBlockState(position.relative(wall)).isAir()) {
                return wall.toYRot();
            }
        }
        return 0;
    }

    //? if >=26.1 {
    /*/^* A shaking wolf's roll (radians), as 26.x's WolfRenderState#getBodyRollAngle works it out. ^/
    private static float wolfBodyRoll(float shakeAnim, float offset) {
        float progress = Mth.clamp((shakeAnim + offset) / 1.8F, 0.0F, 1.0F);
        return Mth.sin(progress * (float) Math.PI) * Mth.sin(progress * (float) Math.PI * 11.0F) * 0.15F * (float) Math.PI;
    }
    *///?}
}
