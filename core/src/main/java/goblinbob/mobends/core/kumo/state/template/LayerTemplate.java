package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import goblinbob.mobends.core.kumo.pose.Pose;

import java.util.Map;

/** A layer: the outermost machine of its nodes, composited onto the layers before it. */
public class LayerTemplate extends MachineTemplate
{

    /** Restricts the bones the layer may write. */
    public ArmatureMask mask;

    /** How this layer's output combines with the layers before it. */
    public LayerMode mode = LayerMode.OVERRIDE;

    /** Composition space for ADDITIVE layers: default and per-bone overrides. */
    public SpaceTemplate additiveSpace;

    /** Default damping for bones this layer writes; nodes can override per bone. */
    public DampingTemplate damping;

    /** Optional condition; while it does not hold the layer writes nothing (bones hold their targets). */
    public ExpressionTemplate when;

    /** Layer variables with their initial values (e.g. a combo counter); nodes can set them on entry. */
    public Map<String, Float> variables;

    /** Left-right mirroring rule for items that set {@code "@mirror": true}. */
    public MirrorTemplate mirror;

    public enum LayerMode
    {
        OVERRIDE,
        ADDITIVE,
    }

    public Pose.Space defaultAdditiveSpace()
    {
        if (mode != LayerMode.ADDITIVE)
        {
            return Pose.Space.OVERRIDE;
        }
        return additiveSpace == null || additiveSpace.defaultSpace == null ? Pose.Space.PRE : additiveSpace.defaultSpace;
    }

}
