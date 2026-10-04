package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class AnimatorTemplate
{

    /**
     * The version of the animator format this build reads. Bump it whenever the format changes in
     * a way old files can't be read as they are, and teach {@code AnimatorTemplateSerializer} to
     * upgrade (or reject) the old version.
     */
    public static final int FORMAT_VERSION = 2;

    /** The format the file was written for (required; see {@link #FORMAT_VERSION}). */
    public int formatVersion;

    /** Key of a parent animator whose layers come first; this animator's layers are appended. */
    @SerializedName("extends")
    public String extendsAnimator;

    public List<LayerTemplate> layers;

    /** The scope's definitions, by name (JSON {@code @define}). */
    public Map<String, DefinitionTemplate> define;
    /** The scope's functions, by name (JSON {@code @functions}). */
    public Map<String, FunctionTemplate> functions;
    /** The scope's statement lists (JSON {@code @on}). */
    public OnTemplate on;

}
