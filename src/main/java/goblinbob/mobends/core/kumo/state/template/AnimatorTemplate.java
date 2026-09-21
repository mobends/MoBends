package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

public class AnimatorTemplate
{

    /** 1 = original format (nodes as arrays, index targets); 2 = named nodes, pose stacks, damping. */
    public int formatVersion = 1;

    public List<LayerTemplate> layers;

}
