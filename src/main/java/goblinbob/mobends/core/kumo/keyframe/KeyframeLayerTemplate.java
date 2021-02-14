package goblinbob.mobends.core.kumo.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.kumo.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.ILayerState;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.Arrays;

public class KeyframeLayerTemplate extends LayerTemplate
{
    public final int entryNode;
    public final ArmatureMask mask;
    public final KeyframeNodeTemplate[] nodes;

    public KeyframeLayerTemplate(String type, int entryNode, ArmatureMask mask, KeyframeNodeTemplate[] nodes)
    {
        super(type);
        this.entryNode = entryNode;
        this.mask = mask;
        this.nodes = nodes;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof KeyframeLayerTemplate))
            return false;
        if (obj == this)
            return true;

        KeyframeLayerTemplate other = (KeyframeLayerTemplate) obj;
        return other.entryNode == this.entryNode &&
                (other.mask == this.mask || other.mask.equals(this.mask)) &&
                Arrays.equals(other.nodes, this.nodes);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeInt(this.entryNode);
        SerialHelper.serializeNullable(this.mask, out);
        SerialHelper.serializeArray(this.nodes, out);
    }

    @Override
    public <D extends IEntityData> ILayerState<D> instantiate(IKumoInstancingContext<D> context)
    {
        return new KeyframeLayerState<>(this, context);
    }

    public static <D extends IEntityData> LayerTemplate deserialize(ISerialContext<D> context, String type, ISerialInput in) throws IOException
    {
        int entryNode = in.readInt();
        ArmatureMask mask = SerialHelper.deserializeNullable(context, ArmatureMask::deserialize, in);
        KeyframeNodeTemplate[] nodes = SerialHelper.deserializeArray(context, KeyframeNodeTemplate::deserializeGeneral, new KeyframeNodeTemplate[0], in);

        return new KeyframeLayerTemplate(type, entryNode, mask, nodes);
    }
}
