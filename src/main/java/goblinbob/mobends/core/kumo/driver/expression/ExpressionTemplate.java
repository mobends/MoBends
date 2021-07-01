package goblinbob.mobends.core.kumo.driver.expression;

import goblinbob.bendslib.InvalidFormatException;
import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;

public abstract class ExpressionTemplate implements ISerializable
{
    private ExpressionType type;

    public abstract <D extends IEntityData> IExpression<D> instantiate(IKumoInstancingContext<D> context);

    @Override
    public void serialize(ISerialOutput out)
    {
        SerialHelper.serializeEnum(this.type, out);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> ExpressionTemplate deserializeGeneral(C context, ISerialInput in) throws IOException
    {
        ExpressionType expressionType = SerialHelper.deserializeEnum(ExpressionType.values(), in);

        if (expressionType == null)
        {
            throw new InvalidFormatException("Unknown expression type");
        }

        switch (expressionType)
        {
            case NUMBER_FUNCTION_CALL:
                return NumberFunctionCall.Template.deserialize(context, in);
            case BOOLEAN_FUNCTION_CALL:
                return BooleanFunctionCall.Template.deserialize(context, in);
            case NUMBER_LITERAL:
                return NumberLiteral.deserialize(context, in);
            case BOOLEAN_LITERAL:
                return BooleanLiteral.deserialize(context, in);
            default:
                throw new InvalidFormatException("Unknown expression type");
        }
    }
}
