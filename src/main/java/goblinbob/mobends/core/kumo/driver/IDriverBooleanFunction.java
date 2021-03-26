package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;

@FunctionalInterface
public interface IDriverBooleanFunction<D extends IEntityData>
{
    boolean resolve(IKumoContext<D> context, IParamStack<D> params);
}
