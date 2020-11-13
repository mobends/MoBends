package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.ISubTypeDeserializer;

import java.io.IOException;
import java.util.Arrays;

public abstract class KeyframeNodeTemplate implements ISerializable
{
    public ConnectionTemplate[] connections;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof KeyframeNodeTemplate))
            return false;

        KeyframeNodeTemplate other = (KeyframeNodeTemplate) obj;
        return Arrays.equals(other.connections, this.connections);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(getType());
    }

    protected abstract String getType();

    public static KeyframeNodeTemplate deserializeGeneral(ISerialInput in, ISerialContext context) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<KeyframeNodeTemplate> deserializer = context.getKeyframeNodeDeserializer();
        return deserializer.deserialize(type, in);
    }
}
