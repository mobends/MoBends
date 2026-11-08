package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * The {@code core:} operations: what vanilla's classes tell, which the Minecraft-free core can't
 * read itself, and the entity built-ins. Must run before any animator is loaded.
 */
public final class MinecraftKumoOperations {
    private static final String[] HANDS = {"main_hand", "off_hand"};

    private MinecraftKumoOperations() {
    }

    public static void register() {
        EntityBuiltIns.register();

        // {"core:holds_item": ["main_hand", "minecraft:torch"]}: the hand holds that item.
        KumoRegistry.registerOperation(KumoOperation.named("core:holds_item")
                .choice("hand", HANDS).param("item", Kind.STRING)
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(LivingEntity.class, args)) return null;
                    InteractionHand hand = hand(args.string(0));
                    String item = args.string(1);
                    return (BooleanEvaluator) (context, values) -> {
                        LivingEntity entity = (LivingEntity) context.entity();
                        return entity != null && item.equals(itemName(entity.getItemInHand(hand)));
                    };
                }));
        // {"core:holds_any_item": ["off_hand"]}: the hand holds anything.
        KumoRegistry.registerOperation(KumoOperation.named("core:holds_any_item")
                .choice("hand", HANDS)
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(LivingEntity.class, args)) return null;
                    InteractionHand hand = hand(args.string(0));
                    return (BooleanEvaluator) (context, values) -> {
                        LivingEntity entity = (LivingEntity) context.entity();
                        return entity != null && !entity.getItemInHand(hand).isEmpty();
                    };
                }));
        // {"core:active_hand_side": ["left"]}: the hand on that side is the one using an item.
        KumoRegistry.registerOperation(KumoOperation.named("core:active_hand_side")
                .choice("side", "left", "right")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(LivingEntity.class, args)) return null;
                    HumanoidArm side = "left".equals(args.string(0)) ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
                    return (BooleanEvaluator) (context, values) -> {
                        LivingEntity entity = (LivingEntity) context.entity();
                        if (entity == null) return false;
                        HumanoidArm active = entity.getUsedItemHand() == InteractionHand.MAIN_HAND ? entity.getMainArm() : entity.getMainArm().getOpposite();
                        return active == side;
                    };
                }));

        // {"core:is_flying": []}: a player flying (creative or spectator flight, not an elytra); for
        // other players it is read from how they move, as the client isn't told (see RemoteFlight).
        KumoRegistry.registerEntityBooleanReader("core:is_flying", Player.class, RemoteFlight::isFlying);
    }

    /** Whether the entity animated is a {@code type}. */
    public static boolean applies(Class<?> type, BindArgs args) {
        return args.entityClass() != null && type.isAssignableFrom(args.entityClass());
    }

    private static InteractionHand hand(String hand) {
        return "main_hand".equals(hand) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    private static String itemName(ItemStack stack) {
        if (stack.isEmpty()) return null;
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }
}
