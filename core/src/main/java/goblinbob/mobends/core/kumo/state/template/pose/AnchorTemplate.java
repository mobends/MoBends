package goblinbob.mobends.core.kumo.state.template.pose;

import com.google.gson.JsonPrimitive;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;

import java.util.Map;

/**
 * {@code {"core:anchor": {...}}}: keeps the entity's origin (where {@link #x}, {@link #y},
 * {@link #z} are) drawn at a point in the world, whatever the entity does meanwhile: a sequence
 * staged in the world, such as climbing onto a block, plays out where it should while the
 * entity's own movement gets ahead of it or lags behind.
 *
 * <p>The point is {@link #point}, in the world, read every frame (so it can move along a path). It
 * is in doubles: a point taken when the node starts is a node constant, such as
 * {@code {"constant": {"floor": ["entityWorldX"]}}}, the block the feet are in. The driver moves
 * the whole model by the difference ({@link #bone}, in the body's frame), past its damping.
 */
public class AnchorTemplate extends DriverItemTemplate
{

    /** Three double expressions, {@code {"x": ..., "y": ..., "z": ...}}. */
    public static class Point
    {
        public ExpressionTemplate x;
        public ExpressionTemplate y;
        public ExpressionTemplate z;
    }

    /** The point in the world, in blocks; read every frame. An axis left out follows the entity. */
    public Point point;

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

    /** The body's yaw (a number, degrees), as the renderer has it. */
    public ExpressionTemplate yaw = new ExpressionTemplate(new JsonPrimitive("entityBodyYaw"));
    /** The entity's position (doubles, blocks), as the renderer has it. */
    public ExpressionTemplate x = new ExpressionTemplate(new JsonPrimitive("entityWorldX"));
    public ExpressionTemplate y = new ExpressionTemplate(new JsonPrimitive("entityWorldY"));
    public ExpressionTemplate z = new ExpressionTemplate(new JsonPrimitive("entityWorldZ"));

    /** Model units per block. */
    public float unitsPerBlock = 16F;

}
