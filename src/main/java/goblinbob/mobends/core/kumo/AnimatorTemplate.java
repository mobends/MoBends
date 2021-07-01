package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;

import java.io.IOException;
import java.util.Arrays;

public class AnimatorTemplate implements ISerializable
{
    public String mutatorId;
    public LayerTemplate[] layers;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AnimatorTemplate))
            return false;

        AnimatorTemplate other = (AnimatorTemplate) obj;
        return other.mutatorId.equals(this.mutatorId) && Arrays.equals(other.layers, this.layers);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        out.writeString(mutatorId);
        SerialHelper.serializeArray(layers, out);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> AnimatorTemplate deserialize(C context, ISerialInput in) throws IOException
    {
        AnimatorTemplate animatorTemplate = new AnimatorTemplate();

        animatorTemplate.mutatorId = in.readString();
        animatorTemplate.layers = SerialHelper.deserializeArray(context, LayerTemplate::deserializeGeneral, new LayerTemplate[0], in);

        return animatorTemplate;
    }
}
