package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.KumoRegistry;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations.Kind;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * The {@code core:} operations: what vanilla's classes tell, which the Minecraft-free core can't
 * read itself, and the entity built-ins. Must run before any animator is loaded.
 */
public final class MinecraftKumoOperations
{

    private static final String[] HANDS = {"main_hand", "off_hand"};

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
        KumoRegistry.registerEntityCondition("core:is_flying", EntityPlayer.class, player -> player.capabilities.isFlying);
    }

    /** Whether the entity animated is a {@code type}. */
    public static boolean applies(Class<?> type, BindArgs args)
    {
        return args.entityClass() != null && type.isAssignableFrom(args.entityClass());
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
