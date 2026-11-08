package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;

/** Wraps the mod's part objects into sinks the animators write to. */
public final class BoneSinks {
    private BoneSinks() {
    }

    public static IBoneSink wrap(Object part) {
        if (part instanceof ModelPartTransform transform) {
            return new OrientationSink(transform.rotation, transform.offset, transform.smoothOffset, transform.scale);
        }
        if (part instanceof SmoothOrientation orientation) {
            return new OrientationSink(orientation, null);
        }
        if (part instanceof SmoothVector3f vector) {
            return new VectorSink(vector);
        }
        return null;
    }
}
