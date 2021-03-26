package goblinbob.mobends.core.kumo.driver.expression;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoContext;

public interface IExpression<D extends IEntityData>
{
    default float resolveNumber(IKumoContext<D> context)
    {
        throw new AnimationRuntimeException("This expression doesn't resolve to a float.");
    }

    default boolean resolveBoolean(IKumoContext<D> context)
    {
        throw new AnimationRuntimeException("This expression doesn't resolve to a boolean.");
    }
}
