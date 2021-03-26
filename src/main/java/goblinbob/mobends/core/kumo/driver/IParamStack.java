package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;

public interface IParamStack<D extends IEntityData>
{
    boolean isEmpty();
    float popNumber(IKumoContext<D> context);
    boolean popBoolean(IKumoContext<D> context);
}
