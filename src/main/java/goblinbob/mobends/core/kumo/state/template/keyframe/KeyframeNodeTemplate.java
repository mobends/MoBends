package goblinbob.mobends.core.kumo.state.template.keyframe;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.template.DampingTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.util.List;

public class KeyframeNodeTemplate
{

    private String type = "core:standard";

    /** Format 2: node name, used by connections and for the entry node. */
    public String name;

    /** Exposed as the layer's current actions (bends packs react to them). */
    public List<String> tags;

    public List<ConnectionTemplate> connections;

    /** Damping per bone while this node is active; unlisted bones keep their previous damping. */
    public DampingTemplate damping;

    /** Bones that jump to their target on the frame the node is entered (the {@code orientInstant} idiom). */
    public List<String> snapOnEnter;

    /**
     * A pose evaluated once when the node is entered; the bones it writes are snapped to it
     * before this frame's regular target applies (the {@code onPlay} + {@code orientInstant} idiom).
     */
    public List<PoseItemTemplate> enterPose;

    /** Layer variables to set when the node is entered. */
    public java.util.Map<String, Float> set;

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        if (connections != null)
        {
            for (ConnectionTemplate connectionTemplate : connections)
            {
                connectionTemplate.validate(context);
            }
        }
    }

}
