package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.animation.keyframe.ArmatureMask;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;

import java.util.List;

public class KeyframeLayerTemplate extends LayerTemplate
{

    public int entryNode = 0;
    public List<KeyframeNodeTemplate> nodes;
    public ArmatureMask mask;

}
