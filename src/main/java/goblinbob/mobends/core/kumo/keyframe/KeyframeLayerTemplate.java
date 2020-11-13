package goblinbob.mobends.core.kumo.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.ISerialContext;
import goblinbob.mobends.core.kumo.LayerTemplate;
import goblinbob.mobends.core.kumo.LayerType;
import goblinbob.mobends.core.kumo.keyframe.node.KeyframeNodeTemplate;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.SerialHelper;

import java.util.Arrays;

public class KeyframeLayerTemplate extends LayerTemplate
{

    private int entryNode = 0;
    private ArmatureMask mask;
    private KeyframeNodeTemplate[] nodes;

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

    public static LayerTemplate deserialize(ISerialInput in, ISerialContext context)
    {
        int entryNode = in.readInt();
        ArmatureMask mask = SerialHelper.deserializeNullable(ArmatureMask::deserialize, in);
        KeyframeNodeTemplate[] nodes = SerialHelper.deserializeArray((inIn) -> KeyframeNodeTemplate.deserializeGeneral(inIn, context), new KeyframeNodeTemplate[0], in);

        return new KeyframeLayerTemplate(entryNode, mask, nodes);
    }
}
