package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

public class AnimatorTemplate
{

    /** The format's version; 2 is the only one. */
    public int formatVersion = 2;

    /** Key of a parent animator whose layers come first; this animator's layers are appended. */
    @com.google.gson.annotations.SerializedName("extends")
    public String extendsAnimator;

    public List<LayerTemplate> layers;

    /** Named expressions, visible to everything inside (see misc/kumo-format.md, "Expressions"). */
    public java.util.Map<String, goblinbob.mobends.core.kumo.expr.ExpressionTemplate> expressions;

}
