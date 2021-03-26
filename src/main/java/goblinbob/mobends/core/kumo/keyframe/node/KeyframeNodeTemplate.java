package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.NodeTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

import java.io.IOException;
import java.util.List;

public abstract class KeyframeNodeTemplate extends NodeTemplate
{
    public KeyframeNodeTemplate(String type, List<ConnectionTemplate> connections)
    {
        super(type, connections);
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    public static <D extends IEntityData> KeyframeNodeTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<KeyframeNodeTemplate, D> deserializer = context.getKeyframeNodeDeserializer();

        return deserializer.deserialize(context, type, in);
    }
}
