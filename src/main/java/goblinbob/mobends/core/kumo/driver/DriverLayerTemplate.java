package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ILayerState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.DriverNodeTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.Arrays;

public class DriverLayerTemplate extends LayerTemplate
{
    public final int entryNode;
    public final DriverNodeTemplate[] nodes;

    public DriverLayerTemplate(String type, int entryNode, DriverNodeTemplate[] nodes)
    {
        super(type);
        this.entryNode = entryNode;
        this.nodes = nodes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof DriverLayerTemplate))
            return false;
        if (obj == this)
            return true;

        DriverLayerTemplate other = (DriverLayerTemplate) obj;
        return other.entryNode == this.entryNode &&
                Arrays.equals(other.nodes, this.nodes);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeInt(this.entryNode);
        SerialHelper.serializeArray(this.nodes, out);
    }

    @Override
    public <D extends IEntityData> ILayerState<D> instantiate(IKumoInstancingContext<D> context)
    {
        return new DriverLayerState<>(this, context);
    }

    public static <D extends IEntityData> LayerTemplate deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
    {
        int entryNode = in.readInt();
        DriverNodeTemplate[] nodes = SerialHelper.deserializeArray(context, DriverNodeTemplate::deserializeGeneral, new DriverNodeTemplate[0], in);

        return new DriverLayerTemplate(type, entryNode, nodes);
    }
}
