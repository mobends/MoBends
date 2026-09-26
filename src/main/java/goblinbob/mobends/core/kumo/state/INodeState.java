package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeNodeTemplate;

import java.util.Collection;
import java.util.Map;

public interface INodeState
{

    String getName();

    Collection<String> getTags();

    Iterable<ConnectionState> getConnections();

    /** Ticks since the node was entered. */
    float getElapsedTicks();

    boolean isAnimationFinished();

    void parseConnections(Map<String, INodeState> nodesByName, KeyframeNodeTemplate template) throws MalformedKumoTemplateException;

    void start(IKumoContext context);

    /** The node's own variable scope (ramps). */
    VariableScope getScope();

    /** Writes this node's pose for the current frame into the given (cleared) pose. */
    void evaluate(IKumoContext context, Pose pose) throws MalformedKumoTemplateException;

    /** A node that poses nothing, so the layers below show through (and transitions fade to them). */
    default boolean isFallthrough()
    {
        return false;
    }

    /** While a layer is in a node that wants vanilla, the entity is drawn with its vanilla model and animation. */
    default boolean isVanilla()
    {
        return false;
    }

    /** Advances the node's clock; called after evaluation. */
    void advance(IKumoContext context, float deltaTime);

}
