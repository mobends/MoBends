package goblinbob.mobends.core.kumo.state.template.pose;

import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.Map;

/**
 * {@code {"core:anchor": {...}}}: keeps the entity's origin (where {@code entityWorldX},
 * {@code entityWorldY}, {@code entityWorldZ} are) drawn at a point in the world, whatever the
 * entity does meanwhile: a sequence staged in the world, such as climbing onto a block, plays out
 * where it should while the entity's own movement gets ahead of it or lags behind.
 *
 * <p>The point is a block and an offset in it. The block is {@link #block}, relative to the block
 * the entity's feet are in when the node starts, taken then; the offset, in blocks from the block's
 * lowest corner, is read every frame, so the point can move along a path ({@code {"x": 0.5, "y": 1,
 * "z": 0.5}} is the middle of the block's top; outside 0..1 is outside it). The driver moves the
 * whole model by the difference ({@link #bone}, in the body's frame), past its damping.
 */
public class AnchorTemplate extends DriverItemTemplate
{

    /** Three expressions, {@code {"x": ..., "y": ..., "z": ...}}; an axis left out is 0. */
    public static class Point
    {
        public ExpressionTemplate x;
        public ExpressionTemplate y;
        public ExpressionTemplate z;
    }

    /** The block, relative to the one the feet are in when the node starts; read then, and floored. */
    public Point block;

    /** Where in the block, in blocks from its lowest corner; read every frame. */
    public Point offset;

    /** How much of the way to the point the entity is drawn, 0..1 (to ease into and out of it). */
    public ExpressionTemplate weight;

    /**
     * The states its outputs go to: {@code positionDifference}, how far the entity really is
     * from the point, in blocks (whatever the weight): what tells a sequence to give up, when the
     * entity has gone elsewhere.
     */
    public Map<String, String> out;

    /** The whole-model offset it moves, applied in the body's frame before the body is turned. */
    public String bone = "root";

    /** The variables holding the body's yaw and the entity's position, as the renderer has them. */
    public String yawVariable = "entityBodyYaw";
    public String xVariable = "entityWorldX";
    public String yVariable = "entityWorldY";
    public String zVariable = "entityWorldZ";

    /** Model units per block. */
    public float unitsPerBlock = 16F;

}
