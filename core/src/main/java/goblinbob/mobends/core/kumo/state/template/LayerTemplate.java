package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/** A layer: the outermost machine of its nodes, composited onto the layers before it. */
public class LayerTemplate extends MachineTemplate
{

    /** Restricts the bones the layer may write. */
    public ArmatureMask mask;

    /** Default smoothing for bones this layer writes; nodes can override per bone. */
    public SmoothingTemplate smoothing;

    /** Optional condition; while it does not hold the layer writes nothing (bones hold their targets). */
    public ExpressionTemplate when;

    /** Left-right mirroring rule for items that set {@code "@mirror": true}. */
    public MirrorTemplate mirror;

}
