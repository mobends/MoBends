package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState;
import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.keyframe.KeyframeLayerTemplate;

import java.util.Collection;

/**
 * Represents the state of a KUMO animation layer. A layer evaluates into a pose each frame and
 * composites it onto the animator's pose.
 *
 * @author Iwo Plaza
 */
public interface ILayerState
{

    void start(IKumoContext context);

    /**
     * Evaluates the layer for this frame and composites its output into {@code animatorPose}.
     */
    void update(IKumoContext context, float deltaTime, Pose animatorPose) throws MalformedKumoTemplateException;

    /** Ticks elapsed since the layer started. */
    float getElapsedTicks();

    /** The tags of the layer's current node (bends packs see them as "actions"). */
    Collection<String> getActions();

    static ILayerState createFromTemplate(IKumoInstancingContext context, Skeleton skeleton, LayerTemplate template) throws MalformedKumoTemplateException
    {
        if (template.getLayerType() == null)
        {
            throw new MalformedKumoTemplateException("A layer has no type.");
        }

        switch (template.getLayerType())
        {
            case KEYFRAME:
                return KeyframeLayerState.createFromTemplate(context, skeleton, (KeyframeLayerTemplate) template);
            default:
                throw new MalformedKumoTemplateException(String.format("Unsupported layer type: %s", template.getLayerType()));
        }
    }

}
