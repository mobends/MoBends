package goblinbob.mobends.core.animation.keyframe;

import org.apache.http.util.ByteArrayBuffer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class BinaryAnimationLoader
{

    private static final byte HAS_POSITION = 1;
    private static final byte HAS_ROTATION = 2;
    private static final byte HAS_SCALE = 4;

    public static KeyframeAnimation loadFromBinaryInputStream(InputStream stream) throws IOException
    {
        KeyframeAnimation animation = new KeyframeAnimation();
        animation.bones = new HashMap<>();

        DataInputStream dataInputStream = new DataInputStream(stream);

        int version = dataInputStream.readInt();
        int amountOfKeyframes = dataInputStream.readInt();
        int amountOfBones = dataInputStream.readInt();

        for (int i = 0; i < amountOfBones; ++i)
        {
            ByteArrayBuffer buffer = new ByteArrayBuffer(16);
            byte character = dataInputStream.readByte();
            while (character != '\0')
            {
                buffer.append(character);
                character = dataInputStream.readByte();
            }

            String boneName = new String(buffer.toByteArray(), "UTF-8");
            Bone bone = new Bone();
            bone.keyframes = new ArrayList<>();

            for (int j = 0; j < amountOfKeyframes; ++j)
            {
                Keyframe frame = new Keyframe();

                byte flags = dataInputStream.readByte();
                if ((flags & HAS_POSITION) != 0)
                {
                    frame.position = new float[] {
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                    };
                }
                else
                {
                    frame.position = new float[] { 0, 0, 0 };
                }

                if ((flags & HAS_ROTATION) != 0)
                {
                    frame.rotation = new float[] {
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                    };
                }
                else
                {
                    frame.rotation = new float[] { 0, 0, 0, 1 };
                }

                if ((flags & HAS_SCALE) != 0)
                {
                    frame.scale = new float[] {
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                            dataInputStream.readFloat(),
                    };
                }
                else
                {
                    frame.scale = new float[] { 1, 1, 1 };
                }

                bone.keyframes.add(frame);
            }

            animation.bones.put(boneName, bone);
        }

        return animation;
    }

}
