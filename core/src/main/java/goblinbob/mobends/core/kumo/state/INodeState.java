package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import java.util.Collection;

public interface INodeState
{

    String getName();

    /** Ticks since the node was entered. */
    float getElapsedTicks(ITriggerConditionContext context);

    boolean isAnimationFinished(ITriggerConditionContext context);

    /**
     * Sets the node's clock back to 0: done on entry before its scope is created, so its
     * definitions and {@code enter} list see the node as entered (then {@link #start} runs).
     */
    default void resetClock(ITriggerConditionContext context)
    {
    }

    void start(IKumoContext context) throws MalformedKumoTemplateException;

    /**
     * Starts the node on top of {@code below}: the layers below, as they are posed this frame, or
     * null where they aren't known. What the node poses on entry builds on them.
     */
    default void start(IKumoContext context, Pose below) throws MalformedKumoTemplateException
    {
        start(context);
    }

    /** Writes this node's pose for the current frame into the given (cleared) pose. */
    void evaluate(IKumoContext context, Pose pose) throws MalformedKumoTemplateException;

    /** While a layer is in a node that wants vanilla, the entity is drawn with its vanilla model and animation. */
    default boolean isVanilla()
    {
        return false;
    }

    /** Advances the node's clock; called after evaluation. */
    void advance(IKumoContext context, float deltaTime);

}
