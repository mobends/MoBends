package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.mobends.core.BasePropertyKeys;
import goblinbob.mobends.core.IModelPart;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.exceptions.AnimationRuntimeException;
import goblinbob.mobends.core.kumo.ConnectionState;
import goblinbob.mobends.core.kumo.IKumoContext;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.NodeState;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.util.GUtil;

public class LookAroundDriverNode<D extends IEntityData> extends NodeState<D> implements IDriverNodeState<D>
{
    private final String headPartName;

    public LookAroundDriverNode(IKumoInstancingContext<D> context, LookAroundDriverNodeTemplate nodeTemplate)
    {
        this(nodeTemplate.headPartName);
    }

    public LookAroundDriverNode(String headPartName)
    {
        this.headPartName = headPartName;
    }

    @Override
    public void start(IKumoContext<D> context)
    {
        for (ConnectionState<D> connection : connections)
        {
            connection.triggerCondition.onNodeStarted(context);
        }
    }

    @Override
    public void update(IKumoContext<D> context)
    {
    }

    @Override
    public void applyTransform(IKumoContext<D> context)
    {
        IEntityData data = context.getEntityData();
        IModelPart headPart = data.getPartForName(this.headPartName);

        if (headPart == null)
        {
            throw new AnimationRuntimeException(String.format("Missing part with name: '%s'", this.headPartName));
        }

        float headPitch = data.getPropertyStorage().getFloatProperty(BasePropertyKeys.HEAD_PITCH);
        float headYaw = data.getPropertyStorage().getFloatProperty(BasePropertyKeys.HEAD_YAW);

        Quaternion q = new Quaternion(Quaternion.IDENTITY);
        q.setFromAxisAngle(1, 0, 0,  headPitch * GUtil.DEG_TO_RAD);
        q.rotate(0, 1, 0, headYaw * GUtil.DEG_TO_RAD);

        Quaternion.mul(headPart.getRotation(), q, headPart.getRotation());
    }
}
