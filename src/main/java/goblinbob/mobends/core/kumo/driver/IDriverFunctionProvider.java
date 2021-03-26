package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;

public interface IDriverFunctionProvider<D extends IEntityData>
{
    IDriverNumberFunction<D> getDriverNumberFunction(String name);

    IDriverBooleanFunction<D> getDriverBooleanFunction(String name);
}
