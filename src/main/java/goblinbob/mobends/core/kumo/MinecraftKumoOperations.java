package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import goblinbob.mobends.core.vanilla.VanillaNames;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * The {@code core:} operations (and the drivers that look at the world: the rays and the ground
 * round the entity): what vanilla's classes tell, which the Minecraft-free core can't read itself
 * (among them what a type file's selector reads), and the entity built-ins. Must run before any
 * animator is loaded.
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
        // {"core:raycast": {...}}: a ray cast through the blocks, where it hit and which way the surface faces.
        KumoRegistry.registerDriver(RaycastDriver.DRIVER);
        // {"core:raycast_array": {...}}: a spread of rays round a direction, keeping the hit closest to it.
        KumoRegistry.registerDriver(RaycastArrayDriver.DRIVER);
        // {"core:block_below": {...}}: the block the entity stands on, or the first below its feet.
        KumoRegistry.registerDriver(BlockBelowDriver.DRIVER);
        // {"core:block_gradient": {...}}: which way the ground rises round a block, and its normal.
        KumoRegistry.registerDriver(BlockGradientDriver.DRIVER);
        // {"core:step_up": {...}}: the step up ahead, one block high with room on it, that a mob walking that way would climb.
        KumoRegistry.registerDriver(StepUpDriver.DRIVER);

        // What a type file's selector reads: the entity alone, any entity (false where it doesn't apply).
        // {"core:entity_type": ["minecraft:zombie", "minecraft:husk"]}: the entity's registry id is one
        // of these. Packs write 1.12's ids; those renamed since are read as today's (VanillaNames).
        KumoRegistry.registerOperation(KumoOperation.named("core:entity_type")
                .param("entityType", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<ResourceLocation> types = new HashSet<>();
                    for (int i = 0; i < args.count(); i++) {
                        ResourceLocation id = ResourceLocation.tryParse(args.string(i));
                        if (id == null) {
                            throw args.error(i, "is not an entity id: '" + args.string(i) + "'.");
                        }
                        types.add(VanillaNames.entityType(id));
                    }
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof Entity entity
                            && types.contains(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
                }));
        // {"core:player_name": ["Notch"]}: a player whose profile name (never the display name) is one of these, ignoring case.
        KumoRegistry.registerOperation(KumoOperation.named("core:player_name")
                .param("name", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<String> names = new HashSet<>();
                    for (int i = 0; i < args.count(); i++) {
                        names.add(args.string(i).toLowerCase(Locale.ROOT));
                    }
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof Player player
                            && names.contains(player.getGameProfile().getName().toLowerCase(Locale.ROOT));
                }));
        // {"core:player_uuid": ["069a79f4-44e9-4726-a5be-fca90e38aaf5"]}: a player with one of these UUIDs, which stay the same when it renames.
        KumoRegistry.registerOperation(KumoOperation.named("core:player_uuid")
                .param("uuid", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<UUID> uuids = new HashSet<>();
                    for (int i = 0; i < args.count(); i++) {
                        try {
                            uuids.add(UUID.fromString(args.string(i)));
                        } catch (IllegalArgumentException e) {
                            throw args.error(i, "is not a UUID: '" + args.string(i) + "'.");
                        }
                    }
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof Player player
                            && uuids.contains(player.getGameProfile().getId());
                }));
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
