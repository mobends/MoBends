package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

/**
 * One contribution to a node's pose: a keyframe clip or a procedural driver. Items are applied
 * in order; each composes its rotations/offsets into the pose in its configured space.
 */
public interface IPoseItem
{

    void apply(Pose pose, IKumoContext context, float elapsedTicks) throws MalformedKumoTemplateException;

    /** @return true once a non-looping clip has reached its end. Drivers never finish. */
    boolean isFinished(float elapsedTicks);

    /** Called when the owning node is (re)entered. */
    void onNodeStarted(IKumoContext context);

    /** Called after the frame's evaluation, with the ticks that passed. */
    void advance(IKumoContext context, float deltaTime);

}
