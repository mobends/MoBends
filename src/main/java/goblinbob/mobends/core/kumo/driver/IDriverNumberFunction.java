package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;

@FunctionalInterface
public interface IDriverNumberFunction<D extends IEntityData>
{
    float resolve(IKumoContext<D> context, IParamStack<D> params);
}
