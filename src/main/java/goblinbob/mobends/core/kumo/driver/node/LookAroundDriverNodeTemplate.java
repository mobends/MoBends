package goblinbob.mobends.core.kumo.driver.node;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.ConnectionTemplate;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.INodeState;
import goblinbob.mobends.core.kumo.ISerialContext;

import java.io.IOException;
import java.util.List;

public class LookAroundDriverNodeTemplate extends DriverNodeTemplate
{
    protected String headPartName;

    public LookAroundDriverNodeTemplate(String type, List<ConnectionTemplate> connections, String headPartName)
    {
        super(type, connections);
        this.headPartName = headPartName;
    }

    @Override
    public <D extends IEntityData> INodeState<D> instantiate(IKumoInstancingContext<D> context)
    {
        return new LookAroundDriverNode<>(context, this);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeString(this.headPartName);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> LookAroundDriverNodeTemplate deserialize(C context, String type, ISerialInput in) throws IOException
    {
        List<ConnectionTemplate> connections = SerialHelper.deserializeList(context, ConnectionTemplate::deserialize, in);

        String headPartName = in.readString();

        return new LookAroundDriverNodeTemplate(type, connections, headPartName);
    }
}
