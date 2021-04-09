package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISubTypeDeserializer;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.NodeTemplate;

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

    public static <D extends IEntityData, C extends ISerialContext<C, D>> DriverNodeTemplate deserializeGeneral(C context, ISerialInput in) throws IOException
    {
        String type = in.readString();

        ISubTypeDeserializer<DriverNodeTemplate, C> deserializer = context.getDriverNodeDeserializer();

        return deserializer.deserialize(context, type, in);
    }
}
