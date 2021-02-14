package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.List;

public class MovementKeyframeNodeTemplate extends KeyframeNodeTemplate
{
    public final String animationKey;
    public final int startFrame;
    public final float playbackSpeed;

    public MovementKeyframeNodeTemplate(String type, List<ConnectionTemplate> connections, String animationKey, int startFrame, float playbackSpeed)
    {
        super(type, connections);
        this.animationKey = animationKey;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
    }

    @Override
    public <D extends IEntityData> INodeState<D> instantiate(IKumoInstancingContext<D> context)
    {
        return new MovementKeyframeNode<>(context,this);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeString(animationKey);
        out.writeInt(startFrame);
        out.writeFloat(playbackSpeed);
    }

    public static <D extends IEntityData> MovementKeyframeNodeTemplate deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
    {
        List<ConnectionTemplate> connections = SerialHelper.deserializeList(context, ConnectionTemplate::deserialize, in);
        String animationKey =   in.readString();
        int startFrame =        in.readInt();
        float playbackSpeed =   in.readFloat();

        return new MovementKeyframeNodeTemplate(type, connections, animationKey, startFrame, playbackSpeed);
    }
}
