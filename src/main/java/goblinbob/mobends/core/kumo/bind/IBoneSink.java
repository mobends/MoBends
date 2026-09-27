package goblinbob.mobends.core.kumo.bind;

/**
 * A destination KUMO writes per-frame targets into. Two flavours exist: {@link IRotationSink}
 * for model parts and standalone orientations, and {@link IVectorSink} for smoothed vectors
 * such as the entity's global offset.
 */
public interface IBoneSink
{

    /** @return this sink as a rotation sink, or null if it only holds a vector. */
    IRotationSink asRotation();

    /** @return this sink as a vector sink, or null if it holds a rotation. */
    IVectorSink asVector();

}
