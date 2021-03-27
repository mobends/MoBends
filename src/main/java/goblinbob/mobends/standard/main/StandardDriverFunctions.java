package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.driver.DriverFunctionRegistry;
import goblinbob.mobends.core.kumo.driver.IParamStack;
import goblinbob.mobends.core.BasePropertyKeys;
import goblinbob.mobends.forge.EntityData;

public class StandardDriverFunctions
{
    static float sin(IKumoContext<EntityData> context, IParamStack<EntityData> params)
    {
        return (float) Math.sin(params.popNumber(context));
    }

    static float lifetime(IKumoContext<EntityData> context, IParamStack<EntityData> params)
    {
        return context.getEntityData().getPropertyStorage().getFloatProperty(BasePropertyKeys.LIFETIME);
    }

    public static void popualteRegistry(DriverFunctionRegistry<EntityData> registry)
    {
        registry.registerFunction("sin", StandardDriverFunctions::sin);
        registry.registerFunction("lifetime", StandardDriverFunctions::lifetime);
    }
}
