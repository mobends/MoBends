package goblinbob.mobends.core.kumo.driver.expression;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;

/**
 * This class acts both as a state and a template, because it doesn't hold any mutable internal state.
 * @param <D>
 */
public class NumberLiteral<D extends IEntityData> extends ExpressionTemplate implements IExpression<D>
{
    float value;

    public NumberLiteral(float value)
    {
        this.value = value;
    }

    @Override
    public <D extends IEntityData> IExpression<D> instantiate(IKumoInstancingContext<D> context)
    {
        //noinspection unchecked
        return (IExpression<D>) this;
    }

    @Override
    public float resolveNumber(IKumoContext<D> context)
    {
        return value;
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeFloat(this.value);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> NumberLiteral<D> deserialize(C context, ISerialInput in) throws IOException
    {
        return new NumberLiteral<D>(in.readFloat());
    }
}
