package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;

import java.util.HashMap;
import java.util.Map;

public class DriverFunctionRegistry<D extends IEntityData> implements IDriverFunctionProvider<D>
{
    private Map<String, IDriverNumberFunction<D>> numberFunctionMap = new HashMap<>();
    private Map<String, IDriverBooleanFunction<D>> booleanFunctionMap = new HashMap<>();

    public void registerFunction(String name, IDriverNumberFunction<D> function)
    {
        this.numberFunctionMap.put(name, function);
    }

    public void registerFunction(String name, IDriverBooleanFunction<D> function)
    {
        this.booleanFunctionMap.put(name, function);
    }

    @Override
    public IDriverNumberFunction<D> getDriverNumberFunction(String name)
    {
        return numberFunctionMap.get(name);
    }

    @Override
    public IDriverBooleanFunction<D> getDriverBooleanFunction(String name)
    {
        return booleanFunctionMap.get(name);
    }
}
