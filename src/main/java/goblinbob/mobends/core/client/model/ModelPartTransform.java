package goblinbob.mobends.core.client.model;

import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * The data side of a bone: where it sits and how the animator turns, moves and scales it, with the
 * smoothing toward those targets. It draws nothing; the part drawn for the bone reads it.
 */
public class ModelPartTransform {
    /** Where the bone sits, in model units, relative to its parent's frame. */
    public final Vec3f position = new Vec3f();
    public final Vec3f scale = new Vec3f(1, 1, 1);
    public final Vec3f offset = new Vec3f();
    /** The offset's smoothing. */
    public final SmoothVector3f smoothOffset = new SmoothVector3f();
    public final SmoothOrientation rotation = new SmoothOrientation();
    /** The scale at which the animated offset and the position apply (a baby's bones are closer). */
    public float offsetScale = 1.0F;
    /** An offset applied before the parent's transform. */
    public final Vec3f globalOffset = new Vec3f();

    public final ModelPartTransform parent;
    /**
     * Whether the part turns with its parent. One that doesn't still sits where its parent puts
     * it, but its rotation is relative to the model (a biped's head).
     */
    public boolean inheritRotation = true;

    public ModelPartTransform(ModelPartTransform parent) {
        this.parent = parent;
    }

    /** Moves the rotation and offset on toward their targets by a frame. */
    public void update(float ticksPerFrame) {
        rotation.update(ticksPerFrame);
        // At rest, the offset is whatever was written to it last (an animator can write it as it
        // is, unsmoothed); only a slide carries it.
        if (smoothOffset.isMoving()) {
            smoothOffset.update(ticksPerFrame);
            offset.set(smoothOffset.getX(), smoothOffset.getY(), smoothOffset.getZ());
        }
    }
}
