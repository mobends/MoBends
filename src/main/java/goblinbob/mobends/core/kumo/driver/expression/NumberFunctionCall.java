package goblinbob.mobends.core.kumo.driver.expression;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.driver.IDriverNumberFunction;
import goblinbob.mobends.core.kumo.driver.ParamStack;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NumberFunctionCall<D extends IEntityData> implements IExpression<D>
{
    IDriverNumberFunction<D> driverFunction;
    private List<IExpression<D>> parameters = new ArrayList<>();

    public NumberFunctionCall(Template template, IKumoInstancingContext<D> context)
    {
        this.driverFunction = context.getDriverNumberFunction(template.functionName);

        for (ExpressionTemplate param : template.parameters)
        {
            this.parameters.add(param.instantiate(context));
        }
    }

    @Override
    public float resolveNumber(IKumoContext<D> context)
    {
        return this.driverFunction.resolve(context, new ParamStack<>(this.parameters));
    }

    public static class Template extends ExpressionTemplate
    {
        private String functionName;
        private List<ExpressionTemplate> parameters;

        public Template(String functionName, List<ExpressionTemplate> parameters)
        {
            this.functionName = functionName;
            this.parameters = parameters;
        }

        @Override
        public <D extends IEntityData> IExpression<D> instantiate(IKumoInstancingContext<D> context)
        {
            return new NumberFunctionCall<>(this, context);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeString(functionName);
            SerialHelper.serializeList(parameters, out);
        }

        public static <D extends IEntityData> Template deserialize(ISerialContext<D> context, ISerialInput in) throws IOException
        {
            String functionName = in.readString();

            List<ExpressionTemplate> parameters = SerialHelper.deserializeList(context,
                    ExpressionTemplate::deserializeGeneral, in);

            return new Template(functionName, parameters);
        }
    }
}
