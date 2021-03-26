package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.data.IEntityData;

/**
 * Represent the state of a KUMO animation layer. This doesn't have to be keyframe animation,
 * this can be any mutation over time.
 *
 * @author Iwo Plaza
 */
public interface ILayerState<D extends IEntityData>
{
    void start(IKumoContext<D> context);

    void update(IKumoContext<D> context);
}
