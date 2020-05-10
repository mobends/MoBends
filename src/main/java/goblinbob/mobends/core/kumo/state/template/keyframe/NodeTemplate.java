package goblinbob.mobends.core.kumo.state.template.keyframe;

import java.util.List;

public class NodeTemplate
{

    public String animationKey;
    public int startFrame = 0;
    public float playbackSpeed = 1;
    public boolean looping = false;

    public List<ConnectionTemplate> connections;

}
