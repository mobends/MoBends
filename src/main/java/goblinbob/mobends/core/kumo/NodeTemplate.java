package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;

import java.util.List;

public abstract class NodeTemplate implements ISerializable
{
    private final String type;
    public List<ConnectionTemplate> connections;

    public NodeTemplate(String type, List<ConnectionTemplate> connections)
    {
        this.type = type;
        this.connections = connections;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof NodeTemplate))
            return false;

        NodeTemplate other = (NodeTemplate) obj;
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
}
