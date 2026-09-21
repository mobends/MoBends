package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.state.IKumoValidationContext;
import goblinbob.mobends.core.kumo.state.LayerType;
import goblinbob.mobends.core.kumo.pose.Pose;

/**
 * Base of every layer template; the concrete class is chosen by {@code type}.
 */
public class LayerTemplate
{

    private LayerType type = LayerType.KEYFRAME;

    /** How this layer's output combines with the layers before it. */
    public LayerMode mode = LayerMode.OVERRIDE;

    /** Composition space for ADDITIVE layers: default and per-bone overrides. */
    public SpaceTemplate additiveSpace;

    /** Default damping for bones this layer writes; nodes can override per bone. */
    public DampingTemplate damping;

    public enum LayerMode
    {
        OVERRIDE,
        ADDITIVE,
    }

    public LayerType getLayerType()
    {
        return type;
    }

    public Pose.Space defaultAdditiveSpace()
    {
        if (mode != LayerMode.ADDITIVE)
        {
            return Pose.Space.OVERRIDE;
        }
        return additiveSpace == null || additiveSpace.defaultSpace == null ? Pose.Space.PRE : additiveSpace.defaultSpace;
    }

    public void validate(IKumoValidationContext context) throws MalformedKumoTemplateException
    {
        // Does nothing by default.
    }

}
