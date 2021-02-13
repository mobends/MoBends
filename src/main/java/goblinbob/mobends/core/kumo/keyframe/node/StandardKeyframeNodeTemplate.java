package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;

import java.io.IOException;

public class StandardKeyframeNodeTemplate extends KeyframeNodeTemplate
{
    public static final String TYPE = "core:standard";

    private String animationKey;
    private int startFrame;
    private float playbackSpeed;
    private boolean looping;

    public StandardKeyframeNodeTemplate(String animationKey, int startFrame, float playbackSpeed, boolean looping)
    {
        this.animationKey = animationKey;
        this.startFrame = startFrame;
        this.playbackSpeed = playbackSpeed;
        this.looping = looping;
    }

    @Override
    public void serialize(ISerialOutput out)
    {
        super.serialize(out);

        out.writeString(animationKey);
        out.writeInt(startFrame);
        out.writeFloat(playbackSpeed);
        out.writeBoolean(looping);
    }

    @Override
    protected String getType()
    {
        return TYPE;
    }

    public static StandardKeyframeNodeTemplate deserialize(ISerialInput in) throws IOException
    {
        String animationKey =   in.readString();
        int startFrame =        in.readInt();
        float playbackSpeed =   in.readFloat();
        boolean looping =       in.readBoolean();

        return new StandardKeyframeNodeTemplate(animationKey, startFrame, playbackSpeed, looping);
    }
}
