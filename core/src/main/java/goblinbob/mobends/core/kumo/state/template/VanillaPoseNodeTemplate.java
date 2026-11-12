package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

/**
 * {@code {"core:vanilla_pose": {...}}}: a pose node whose stack starts with the pose the entity's
 * own (vanilla) model has this frame, as the game and the mods that pose that model left it; its
 * {@code pose} items build on it, as do the layers above. It takes {@code core:pose}'s fields.
 */
public class VanillaPoseNodeTemplate extends PoseNodeTemplate
{

    /** The bones it takes from the vanilla pose; null for every bone the vanilla pose has. */
    public List<String> bones;

    public void validate() throws MalformedKumoTemplateException
    {
        if (bones != null)
        {
            for (String bone : bones)
            {
                if (bone == null)
                {
                    throw new MalformedKumoTemplateException(String.format("The vanilla pose node '%s' lists a bone that is no name.", name));
                }
                if (goblinbob.mobends.core.kumo.pose.Skeleton.isEntityBone(bone))
                {
                    throw new MalformedKumoTemplateException(String.format("The vanilla pose node '%s' lists '%s': the vanilla model doesn't pose the entity's own bones.", name, bone));
                }
            }
        }
    }

}
