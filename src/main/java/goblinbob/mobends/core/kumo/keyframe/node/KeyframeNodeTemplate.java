package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.*;

import java.io.IOException;
import java.util.List;

public abstract class KeyframeNodeTemplate implements ISerializable
{
    private final String type;
    public List<ConnectionTemplate> connections;

    public KeyframeNodeTemplate(String type, List<ConnectionTemplate> connections)
    {
        this.type = type;
        this.connections = connections;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof KeyframeNodeTemplate))
            return false;

        KeyframeNodeTemplate other = (KeyframeNodeTemplate) obj;
        return other.type.equals(this.type) &&
                other.connections.equals(this.connections);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(type);
        SerialHelper.serializeList(connections, out);
    }

    public abstract <D extends IEntityData> INodeState<D> instantiate(IKumoInstancingContext<D> context);

    public static <D extends IEntityData> KeyframeNodeTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<KeyframeNodeTemplate, D> deserializer = context.getKeyframeNodeDeserializer();

        return deserializer.deserialize(context, type, in);
    }
}
