package goblinbob.mobends.core.kumo.state.template;

import java.util.List;

/**
 * {@code {"core:vanilla_pose": {...}}}: a pose node whose stack starts with the pose the entity's
 * own (vanilla) model has this frame, as the game and the mods that pose that model left it; its
 * {@code pose} items build on it, as do the layers above. It takes {@code core:pose}'s fields.
 */
public class VanillaPoseNodeTemplate extends PoseNodeTemplate
{

    /**
     * The bones it takes from the vanilla pose; null for every bone the animator names. The entity's
     * own bones ({@code root}, {@code center}) among them are put at rest.
     */
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
            }
        }
    }

}
