package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.*;
import goblinbob.mobends.core.serial.*;

import java.io.IOException;
import java.util.List;

public abstract class DriverNodeTemplate extends NodeTemplate
{
    public DriverNodeTemplate(String type, List<ConnectionTemplate> connections)
    {
        super(type, connections);
    }

    @Override
    public boolean equals(Object obj)
    {
        return super.equals(obj);
    }

    public static <D extends IEntityData> DriverNodeTemplate deserializeGeneral(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<DriverNodeTemplate, D> deserializer = context.getDriverNodeDeserializer();

        return deserializer.deserialize(context, type, in);
    }
}
