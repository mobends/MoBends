package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.INodeState;

public interface IDriverNodeState<D extends IEntityData> extends INodeState<D>
{
    void applyTransform(IKumoContext<D> context);
}
