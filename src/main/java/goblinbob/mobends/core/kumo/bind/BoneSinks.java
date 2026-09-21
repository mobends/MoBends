package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;

/** Wraps the mod's part objects into sinks. */
public class BoneSinks
{

    public static IBoneSink wrap(Object part)
    {
        if (part == null)
        {
            return null;
        }
        if (part instanceof IModelPart)
        {
            IModelPart modelPart = (IModelPart) part;
            return new OrientationSink(modelPart.getRotation(), modelPart.getOffset());
        }
        if (part instanceof SmoothOrientation)
        {
            return new OrientationSink((SmoothOrientation) part, null);
        }
        if (part instanceof SmoothVector3f)
        {
            return new VectorSink((SmoothVector3f) part);
        }
        return null;
    }

}
