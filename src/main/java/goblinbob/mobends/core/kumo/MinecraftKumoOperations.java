package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import static goblinbob.mobends.core.kumo.expr.ExpressionOperations.choice;
import static goblinbob.mobends.core.kumo.expr.ExpressionOperations.params;
import static goblinbob.mobends.core.kumo.expr.ExpressionOperations.string;

/**
 * Adds the Kumo operations that need Minecraft, which the Minecraft-free core can't provide itself.
 * Must run before any animator is loaded.
 */
public final class MinecraftKumoOperations
{

    private MinecraftKumoOperations()
    {
    }

    public static void register()
    {
        EntityBuiltIns.register();

        // {"core:equipment_name": ["head", "^Notch.*"]}: whether the display name of what a player
        // wears or holds in the slot matches the pattern (as a whole).
        String[] slots = new String[EntityEquipmentSlot.values().length];
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values())
        {
            slots[slot.ordinal()] = slot.getName();
        }
        ExpressionOperations.register("core:equipment_name", params(choice("slot", slots), string("pattern")), false, args -> {
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
            return new EquipmentName(slot, pattern);
        });
    }

    private static final class EquipmentName extends Expression.BooleanExpression
    {
        private final EntityEquipmentSlot slot;
        private final Pattern pattern;

        EquipmentName(EntityEquipmentSlot slot, Pattern pattern)
        {
            this.slot = slot;
            this.pattern = pattern;
        }

        @Override
        public boolean test(ITriggerConditionContext context)
        {
            if (!(context.getSubject() instanceof EntityData))
            {
                return false;
            }
            Entity entity = ((EntityData<?>) context.getSubject()).getEntity();
            return entity instanceof EntityPlayer
                    && pattern.matcher(((EntityPlayer) entity).getItemStackFromSlot(slot).getDisplayName()).matches();
        }
    }

}
