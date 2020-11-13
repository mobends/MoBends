package goblinbob.mobends.core.kumo.keyframe.node;

import goblinbob.mobends.core.serial.ISerialInput;
import goblinbob.mobends.core.serial.ISerialOutput;

public class StandardKeyframeNodeTemplate extends KeyframeNodeTemplate
{
    public static final String TYPE = "core:standard";

    private String animationKey;

    private int startFrame = 0;

    private float playbackSpeed = 1;

    private boolean looping = false;

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

    public static StandardKeyframeNodeTemplate deserialize(ISerialInput in)
    {
        String animationKey =   in.readString();
        int startFrame =        in.readInt();
        float playbackSpeed =   in.readFloat();
        boolean looping =       in.readBoolean();

        StandardKeyframeNodeTemplate template = new StandardKeyframeNodeTemplate(animationKey, startFrame, playbackSpeed, looping);

        return template;
    }
}
