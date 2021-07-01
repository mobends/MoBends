package goblinbob.mobends.core.kumo.driver.instruction;

import goblinbob.bendslib.math.vector.IVec3f;
import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.driver.expression.ExpressionTemplate;
import goblinbob.mobends.core.kumo.driver.expression.IExpression;

public class SetPositionInstruction<D extends IEntityData> implements IInstruction<D>
{
    private String partName;
    private IExpression<D> expressionX;
    private IExpression<D> expressionY;
    private IExpression<D> expressionZ;

    public SetPositionInstruction(SetPositionInstructionTemplate template, IKumoInstancingContext<D> context)
    {
        this(template.partName, template.expressionX, template.expressionY, template.expressionZ, context);
    }

    public SetPositionInstruction(String partName, ExpressionTemplate x, ExpressionTemplate y, ExpressionTemplate z, IKumoInstancingContext<D> context)
    {
        this.partName = partName;
        this.expressionX = x.instantiate(context);
        this.expressionY = y.instantiate(context);
        this.expressionZ = z.instantiate(context);
    }

    @Override
    public void perform(IKumoContext<D> context)
    {
        IEntityData data = context.getEntityData();
        IModelPart part = data.getPartForName(partName);

        if (part == null)
        {
            throw new AnimationRuntimeException(String.format("Trying to set position of a non-existent part: '%s'", partName));
        }

        IVec3f offset = part.getOffset();

        if (this.expressionX != null)
        {
            offset.setX(this.expressionX.resolveNumber(context));
        }

        if (this.expressionY != null)
        {
            offset.setY(this.expressionY.resolveNumber(context));
        }

        if (this.expressionZ != null)
        {
            offset.setZ(this.expressionZ.resolveNumber(context));
        }
    }
}
