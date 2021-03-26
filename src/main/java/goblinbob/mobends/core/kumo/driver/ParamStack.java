package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.driver.expression.IExpression;

import java.util.List;

public class ParamStack<D extends IEntityData> implements IParamStack<D>
{
    private List<IExpression<D>> expressions;
    private int index;

    public ParamStack(List<IExpression<D>> expressions)
    {
        this.expressions = expressions;
    }

    @Override
    public boolean isEmpty()
    {
        return this.index >= this.expressions.size();
    }

    @Override
    public float popNumber(IKumoContext<D> context)
    {
        if (this.isEmpty())
        {
            throw new AnimationRuntimeException("Invalid amount of parameters");
        }

        return this.expressions.get(index++).resolveNumber(context);
    }

    @Override
    public boolean popBoolean(IKumoContext<D> context)
    {
        if (this.isEmpty())
        {
            throw new AnimationRuntimeException("Invalid amount of parameters");
        }

        return this.expressions.get(index++).resolveBoolean(context);
    }
}
