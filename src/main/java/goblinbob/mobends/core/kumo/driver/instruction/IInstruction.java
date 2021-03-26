package goblinbob.mobends.core.kumo.driver.instruction;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;

public interface IInstruction<D extends IEntityData>
{
    void perform(IKumoContext<D> context);
}
