package goblinbob.mobends.core.animation.keyframe;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;
import goblinbob.mobends.core.serial.ISerializable;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class KeyframeAnimation implements ISerializable
{
    private final int LATEST_FORMAT_VERSION = 1;
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
        out.writeInt(LATEST_FORMAT_VERSION);

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
        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();

        int version =           in.readInt();
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
