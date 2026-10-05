package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The {@code core:} operations: what vanilla's classes tell, which the Minecraft-free core can't
 * read itself, and the entity built-ins. Must run before any animator is loaded.
 */
public final class MinecraftKumoOperations
{

    private static final String[] HANDS = {"main_hand", "off_hand"};

    /** Players have no entity registry id; this one stands for them in {@code core:entity_type}. */
    public static final ResourceLocation PLAYER = new ResourceLocation("minecraft", "player");

    private MinecraftKumoOperations()
    {
    }

    public static void register()
    {
        EntityBuiltIns.register();

        // {"core:holds_item": ["main_hand", "minecraft:torch"]}: the hand holds that item.
        KumoRegistry.registerOperation(KumoOperation.named("core:holds_item")
                .choice("hand", HANDS).param("item", Kind.STRING)
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(EntityLivingBase.class, args)) return null;
                    EnumHand hand = hand(args.string(0));
                    String item = args.string(1);
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        return entity != null && item.equals(itemName(entity.getHeldItem(hand)));
                    };
                }));
        // {"core:holds_any_item": ["off_hand"]}: the hand holds anything.
        KumoRegistry.registerOperation(KumoOperation.named("core:holds_any_item")
                .choice("hand", HANDS)
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(EntityLivingBase.class, args)) return null;
                    EnumHand hand = hand(args.string(0));
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        return entity != null && itemName(entity.getHeldItem(hand)) != null;
                    };
                }));
        // {"core:active_hand_side": ["left"]}: the hand on that side is the one using an item.
        KumoRegistry.registerOperation(KumoOperation.named("core:active_hand_side")
                .choice("side", "left", "right")
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(EntityLivingBase.class, args)) return null;
                    EnumHandSide side = "left".equals(args.string(0)) ? EnumHandSide.LEFT : EnumHandSide.RIGHT;
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        if (entity == null) return false;
                        EnumHandSide active = entity.getActiveHand() == EnumHand.MAIN_HAND ? entity.getPrimaryHand() : entity.getPrimaryHand().opposite();
                        return active == side;
                    };
                }));

        // {"core:equipment_name": ["head", "^Notch.*"]}: the display name of what the entity wears or
        // holds in the slot matches the pattern (as a whole).
        String[] slots = new String[EntityEquipmentSlot.values().length];
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
        {
            slots[slot.ordinal()] = slot.getName();
        }
        KumoRegistry.registerOperation(KumoOperation.named("core:equipment_name")
                .choice("slot", slots).param("pattern", Kind.STRING)
                .returns(Expression.Type.BOOLEAN).withFallback()
                .bind(args -> {
                    if (!applies(EntityLivingBase.class, args)) return null;
                    EntityEquipmentSlot slot = EntityEquipmentSlot.fromString(args.string(0));
                    Pattern pattern;
                    try
                    {
                        pattern = Pattern.compile(args.string(1));
                    }
                    catch (PatternSyntaxException e)
                    {
                        throw args.error(1, "is not a valid pattern: " + e.getDescription());
                    }
                    return (BooleanEvaluator) (context, values) -> {
                        EntityLivingBase entity = (EntityLivingBase) context.entity();
                        return entity != null && pattern.matcher(entity.getItemStackFromSlot(slot).getDisplayName()).matches();
                    };
                }));

        // {"core:is_flying": []}: a player flying (creative or spectator flight, not an elytra).
        KumoRegistry.registerEntityBooleanReader("core:is_flying", EntityPlayer.class, player -> player.capabilities.isFlying);
        // {"core:ledge_ahead": []}: a block in front of the entity's feet it can climb onto (what a
        // mob jumps onto as it walks), read once on liftoff to tell a vault from a jump.
        KumoRegistry.registerEntityBooleanReader("core:ledge_ahead", EntityLivingBase.class, MinecraftKumoOperations::ledgeAhead);

        // What a type file's selector reads: the entity alone, any entity (false where it doesn't apply).
        // {"core:entity_type": ["minecraft:zombie", "minecraft:husk"]}: the entity's registry id is one of these.
        KumoRegistry.registerOperation(KumoOperation.named("core:entity_type")
                .param("entityType", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<ResourceLocation> types = new HashSet<>();
                    for (int i = 0; i < args.count(); i++)
                    {
                        types.add(new ResourceLocation(args.string(i)));
                    }
                    return (BooleanEvaluator) (context, values) -> {
                        Object entity = context.entity();
                        ResourceLocation id = entity instanceof EntityPlayer ? PLAYER : entity instanceof Entity ? EntityList.getKey((Entity) entity) : null;
                        return id != null && types.contains(id);
                    };
                }));
        // {"core:player_name": ["Notch"]}: a player whose profile name (never the display name) is one of these, ignoring case.
        KumoRegistry.registerOperation(KumoOperation.named("core:player_name")
                .param("name", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<String> names = new HashSet<>();
                    for (int i = 0; i < args.count(); i++)
                    {
                        names.add(args.string(i).toLowerCase(Locale.ROOT));
                    }
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof EntityPlayer
                            && names.contains(((EntityPlayer) context.entity()).getGameProfile().getName().toLowerCase(Locale.ROOT));
                }));
        // {"core:player_uuid": ["069a79f4-44e9-4726-a5be-fca90e38aaf5"]}: a player with one of these UUIDs, which stay the same when it renames.
        KumoRegistry.registerOperation(KumoOperation.named("core:player_uuid")
                .param("uuid", Kind.STRING).repeatsLast()
                .returns(Expression.Type.BOOLEAN).selectorSafe(true)
                .bind(args -> {
                    Set<UUID> uuids = new HashSet<>();
                    for (int i = 0; i < args.count(); i++)
                    {
                        try
                        {
                            uuids.add(UUID.fromString(args.string(i)));
                        }
                        catch (IllegalArgumentException e)
                        {
                            throw args.error(i, "is not a UUID: '" + args.string(i) + "'.");
                        }
                    }
                    return (BooleanEvaluator) (context, values) -> context.entity() instanceof EntityPlayer
                            && uuids.contains(((EntityPlayer) context.entity()).getGameProfile().getId());
                }));
    }

    /** Whether the entity animated is a {@code type}. */
    public static boolean applies(Class<?> type, BindArgs args)
    {
        return args.entityClass() != null && type.isAssignableFrom(args.entityClass());
    }

    /** How far in front of the entity {@code core:ledge_ahead} looks, in blocks. */
    private static final double LEDGE_REACH = 0.5D;
    /** The highest ledge {@code core:ledge_ahead} counts, above the feet: a jump's height. */
    private static final double LEDGE_MAX_HEIGHT = 1.5D;

    /**
     * Whether a block stands in front of the entity's feet, no higher than {@link #LEDGE_MAX_HEIGHT},
     * with room for the entity on top. Front is the way the entity moves, or faces when it doesn't
     * (pressed against the block it is about to climb).
     */
    public static boolean ledgeAhead(EntityLivingBase entity)
    {
        if (entity.world == null) return false;
        double dx = entity.posX - entity.prevPosX;
        double dz = entity.posZ - entity.prevPosZ;
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 0.01D)
        {
            float yaw = entity.renderYawOffset * 0.017453292F;
            dx = -MathHelper.sin(yaw);
            dz = MathHelper.cos(yaw);
            length = 1;
        }
        dx *= LEDGE_REACH / length;
        dz *= LEDGE_REACH / length;

        AxisAlignedBB box = entity.getEntityBoundingBox();
        AxisAlignedBB ahead = box.offset(dx, 0, dz);
        // Above the ground the entity stands on (a hair over its feet), up to a jump's height.
        AxisAlignedBB feet = new AxisAlignedBB(ahead.minX, box.minY + 0.01D, ahead.minZ, ahead.maxX, box.minY + LEDGE_MAX_HEIGHT, ahead.maxZ);
        double top = box.minY;
        for (AxisAlignedBB hit : entity.world.getCollisionBoxes(entity, feet))
        {
            top = Math.max(top, hit.maxY);
        }
        if (top <= box.minY + 0.01D || top > box.minY + LEDGE_MAX_HEIGHT) return false;
        return entity.world.getCollisionBoxes(entity, ahead.offset(0, top - box.minY + 0.01D, 0)).isEmpty();
    }

    private static EnumHand hand(String hand)
    {
        return "main_hand".equals(hand) ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
    }

    private static String itemName(ItemStack stack)
    {
        if (stack == null || stack.isEmpty()) return null;
        ResourceLocation key = Item.REGISTRY.getNameForObject(stack.getItem());
        return key == null ? null : key.toString();
    }

}
