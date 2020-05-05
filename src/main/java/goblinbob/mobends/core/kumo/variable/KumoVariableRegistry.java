package goblinbob.mobends.core.kumo.variable;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;

import java.util.HashMap;

public class KumoVariableRegistry
{

    public static final KumoVariableRegistry instance = new KumoVariableRegistry();

    private EntityData<?> tempData;
    private HashMap<String, KumoVariableEntry> variables = new HashMap<>();

    public void provideTemporaryData(EntityData<?> tempData)
    {
        this.tempData = tempData;
    }

    // Static methods

    static
    {
        registerVariable("ticks", DataUpdateHandler::getTicks);
        registerVariable("random", Math::random);
        registerVariable("ticksAfterPunch", () -> {
            if (instance.tempData instanceof LivingEntityData)
                return ((LivingEntityData<?>) instance.tempData).getTicksAfterAttack();
            return 0;
        });
        registerVariable("health", () -> {
            Entity entity = instance.tempData.getEntity();
            if (entity instanceof EntityLivingBase)
                return ((EntityLivingBase) entity).getHealth();
            return 0;
        });
    }

    private static void registerVariable(String key, IKumoVariable variable)
    {
        instance.variables.put(key, new KumoVariableEntry(variable, key));
    }

}
