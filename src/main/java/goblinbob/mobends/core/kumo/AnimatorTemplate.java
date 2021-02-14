package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;
import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;
import goblinbob.mobends.core.serial.SerialHelper;

import java.io.IOException;
import java.util.Arrays;

public class AnimatorTemplate implements ISerializable
{
    public LayerTemplate[] layers;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AnimatorTemplate))
            return false;

        AnimatorTemplate other = (AnimatorTemplate) obj;
        return Arrays.equals(other.layers, this.layers);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        SerialHelper.serializeArray(layers, out);
    }

    public static <D extends IEntityData> AnimatorTemplate deserialize(ISerialContext<D> context, ISerialInput in) throws IOException
    {
        AnimatorTemplate animatorTemplate = new AnimatorTemplate();

        animatorTemplate.layers = SerialHelper.deserializeArray(context, LayerTemplate::deserializeGeneral, new LayerTemplate[0], in);

        return animatorTemplate;
    }
}
