package goblinbob.mobends.core.kumo.driver.expression;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.driver.IDriverBooleanFunction;
import goblinbob.mobends.core.kumo.driver.ParamStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BooleanFunctionCall<D extends IEntityData> implements IExpression<D>
{
    IDriverBooleanFunction<D> driverFunction;
    private List<IExpression<D>> parameters = new ArrayList<>();

    public BooleanFunctionCall(Template template, IKumoInstancingContext<D> context)
    {
        this.driverFunction = context.getDriverBooleanFunction(template.functionName);

        for (ExpressionTemplate param : template.parameters)
        {
            this.parameters.add(param.instantiate(context));
        }
    }

    @Override
    public boolean resolveBoolean(IKumoContext<D> context)
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
            return new BooleanFunctionCall<D>(this, context);
        }

        @Override
        public void serialize(ISerialOutput out)
        {
            super.serialize(out);

            out.writeString(functionName);
            SerialHelper.serializeList(parameters, out);
        }

        public static <D extends IEntityData, C extends ISerialContext<C, D>> Template deserialize(C context, ISerialInput in) throws IOException
        {
            String functionName = in.readString();

            List<ExpressionTemplate> parameters = SerialHelper.deserializeList(context,
                    ExpressionTemplate::deserializeGeneral, in);

            return new Template(functionName, parameters);
        }
    }
}
