package goblinbob.mobends.core.kumo;

import goblinbob.bendslib.FormatVersion;
import goblinbob.bendslib.InvalidFormatException;
import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;
import goblinbob.mobends.core.data.IEntityData;

import java.io.IOException;

public class AnimatorFile implements ISerializable
{
    public static final String HEADER = "BENDSANMT";
    public FormatVersion version;
    public AnimatorTemplate template;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof AnimatorFile))
            return false;

        AnimatorFile other = (AnimatorFile) obj;
        return other.version.equals(version) &&
               other.template.equals(this.template);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        SerialHelper.serializeChars(HEADER, out);
        version.serialize(out);
        template.serialize(out);
    }

    public static <D extends IEntityData, C extends ISerialContext<C, D>> AnimatorFile deserialize(C context, ISerialInput in) throws IOException
    {
        AnimatorFile animatorFile = new AnimatorFile();

        String header = SerialHelper.deserializeChars(HEADER.length(), in);
        if (!header.equals(HEADER))
        {
            throw new InvalidFormatException("The header doesn't match the standard file format.");
        }

        animatorFile.version = FormatVersion.deserialize(in);
        animatorFile.template = AnimatorTemplate.deserialize(context, in);

        return animatorFile;
    }
}
