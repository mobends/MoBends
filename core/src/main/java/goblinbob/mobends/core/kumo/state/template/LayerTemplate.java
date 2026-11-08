package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

/** A layer: the outermost machine of its nodes, composited onto the layers before it. */
public class LayerTemplate extends MachineTemplate
{

    /** Restricts the bones the layer may write. */
    public ArmatureMask mask;

    /** Default smoothing for bones this layer writes; nodes can override per bone. */
    public SmoothingTemplate smoothing;

    /**
     * Optional condition: the layer is posed while it holds. When it stops, the layer fades out over
     * {@link #fade} ticks and is left; when it holds again, the layer starts over and fades in.
     */
    public ExpressionTemplate when;

    /** Ticks the layer takes to fade in and out with its {@link #when} (0: at once). */
    public float fade;

    /** Left-right mirroring rule for items that set {@code "@mirror": true}. */
    public MirrorTemplate mirror;

}
