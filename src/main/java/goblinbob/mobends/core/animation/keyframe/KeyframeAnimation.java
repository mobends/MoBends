package goblinbob.mobends.core.animation.keyframe;

import goblinbob.bendslib.FormatVersion;
import goblinbob.bendslib.serial.ISerialInput;
import goblinbob.bendslib.serial.ISerialOutput;
import goblinbob.bendslib.serial.ISerializable;
import goblinbob.bendslib.serial.SerialHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeyframeAnimation implements ISerializable
{
    private static final String HEADER = "BENDSANIM";
    private static final FormatVersion LATEST_FORMAT_VERSION = new FormatVersion(0, 1, 0);
    public Map<String, Bone> bones;

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof KeyframeAnimation))
            return false;

        KeyframeAnimation other = (KeyframeAnimation) obj;

        return Objects.equals(other.bones, this.bones);
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        SerialHelper.serializeChars(HEADER, out);
        LATEST_FORMAT_VERSION.serialize(out);

        int amountOfKeyframes = 0;
        for (Bone bone : bones.values())
        {
            amountOfKeyframes = Math.max(amountOfKeyframes, bone.getKeyframes().length);
        }

        out.writeInt(amountOfKeyframes);
        out.writeInt(bones.size());

        for (Map.Entry<String, Bone> bone : bones.entrySet())
        {
            out.writeString(bone.getKey());

            Keyframe[] keyframes = bone.getValue().getKeyframes();
            for (int j = 0; j < amountOfKeyframes; ++j)
            {
                keyframes[j].serialize(out);
            }
        }
    }

    public static KeyframeAnimation deserialize(ISerialInput in) throws IOException
    {
        // Checking header
        String header = SerialHelper.deserializeChars(HEADER.length(), in);
        if (!header.equals(HEADER))
        {
            return null;
        }

        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();

        FormatVersion version = FormatVersion.deserialize(in);
        int amountOfKeyframes = in.readInt();
        int amountOfBones =     in.readInt();

        for (int i = 0; i < amountOfBones; ++i)
        {
            String boneName = in.readString();
            Keyframe[] keyframes = new Keyframe[amountOfKeyframes];

            for (int j = 0; j < amountOfKeyframes; ++j)
            {
                keyframes[j] = Keyframe.deserialize(in);
            }

            animation.bones.put(boneName, new Bone(keyframes));
        }

        return animation;
    }
}
