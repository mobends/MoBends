package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

public class AnimatorTemplate
{

    /** 1 = original format (nodes as arrays, index targets); 2 = named nodes, pose stacks, damping. */
    public int formatVersion = 1;

    /** Key of a parent animator whose layers come first; this animator's layers are appended. */
    @com.google.gson.annotations.SerializedName("extends")
    public String extendsAnimator;

    public List<LayerTemplate> layers;

}
