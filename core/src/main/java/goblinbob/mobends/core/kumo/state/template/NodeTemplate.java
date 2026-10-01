package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.util.List;
import java.util.Map;

/** Base of every node template; the concrete class is chosen by {@code type} (see {@code NodeRegistry}). */
public class NodeTemplate
{

    private String type = "core:pose";

    /** The node's name, used by selectors, connections and {@code defaultOnEntry}. */
    public String name;

    /** Exposed as the layer's current actions ({@code core:action} conditions test them). */
    public List<String> tags;

    public List<ConnectionTemplate> connections;

    /** Damping per bone while this node is active; unlisted bones keep their previous damping. */
    public DampingTemplate damping;

    /** Bones that jump to their target on the frame the node is entered. */
    public List<String> snapOnEnter;

    /**
     * A pose evaluated once when the node is entered; the bones it writes are snapped to it
     * before this frame's regular target applies.
     */
    public List<PoseItemTemplate> enterPose;

    /** Layer variables to set when the node is entered. */
    public Map<String, Float> set;

    /** Named expressions, visible to everything inside (see misc/kumo-format.md, "Expressions"). */
    public Map<String, ExpressionTemplate> expressions;

    /** Named conditions, visible to everything inside (see misc/kumo-format.md, "Conditions"). */
    public Map<String, TriggerConditionTemplate> conditions;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    /** For node types that pose nothing: fails when the template has pose-related fields. */
    protected void requirePosesNothing(String what) throws MalformedKumoTemplateException
    {
        if (damping != null || snapOnEnter != null || enterPose != null)
        {
            throw new MalformedKumoTemplateException(String.format("The %s node '%s' poses nothing: it can't have \"damping\", \"snapOnEnter\" or \"enterPose\".", what, name));
        }
    }

}
