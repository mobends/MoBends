package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * One contribution to a node's pose: a keyframe clip or a driver. Items are applied
 * in order; each composes its rotations/offsets into the pose in its configured space.
 */
public interface IPoseItem
{

    void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException;

    /**
     * How long the item runs, in ticks, or NaN if it runs for as long as its node does (drivers,
     * clips without a {@code duration}). A node is finished once each of its timed items is.
     */
    default float getDuration()
    {
        return Float.NaN;
    }

    /** Called when the owning node is (re)entered. */
    void onNodeStarted(IKumoContext context);

    /** Called after the frame's evaluation, with the ticks that passed. */
    void advance(IKumoContext context, float deltaTime);

}
