package goblinbob.mobends.core.kumo.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.exceptions.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.*;
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

    public KeyframeLayerTemplate(int entryNode, ArmatureMask mask, KeyframeNodeTemplate[] nodes)
    {
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
        out.writeByte((byte) LayerType.KEYFRAME.ordinal());

        out.writeInt(this.entryNode);
        SerialHelper.serializeNullable(this.mask, out);
        SerialHelper.serializeArray(this.nodes, out);
    }

    @Override
    public ILayerState produceInstance(IKumoInstancingContext context) throws MalformedKumoTemplateException
    {
        return new KeyframeLayerState(context, this);
    }

    public static LayerTemplate deserialize(ISerialInput in, ISerialContext context) throws IOException
    {
        int entryNode = in.readInt();
        ArmatureMask mask = SerialHelper.deserializeNullable(ArmatureMask::deserialize, in);
        KeyframeNodeTemplate[] nodes = SerialHelper.deserializeArray((inIn) -> KeyframeNodeTemplate.deserializeGeneral(inIn, context), new KeyframeNodeTemplate[0], in);

        return new KeyframeLayerTemplate(entryNode, mask, nodes);
    }
}
