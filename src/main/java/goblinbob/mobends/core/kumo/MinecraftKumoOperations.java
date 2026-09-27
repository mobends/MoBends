package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.expr.ExpressionOperations;
import goblinbob.mobends.core.kumo.state.condition.EquipmentNameCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import net.minecraft.util.math.MathHelper;

/**
 * Adds the Kumo operations and trigger conditions that need Minecraft, which the Minecraft-free
 * core can't provide itself. Must run before any animator is loaded.
 */
public final class MinecraftKumoOperations
{

    private MinecraftKumoOperations()
    {
    }

    public static void register()
    {
        // Minecraft's table-based sine and cosine, which vanilla models use.
        ExpressionOperations.unary("mcsin", MathHelper::sin);
        ExpressionOperations.unary("mccos", MathHelper::cos);

        TriggerConditionRegistry.INSTANCE.register("core:equipment_name", EquipmentNameCondition::new, EquipmentNameCondition.Template.class);
    }

}
