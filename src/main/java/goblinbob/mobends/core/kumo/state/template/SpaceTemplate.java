package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.pose.Pose;

import java.util.HashMap;
import java.util.Map;

/** JSON: either "PRE" / "POST", or {"default": "PRE", "body": "POST"}. */
public class SpaceTemplate
{

    public Pose.Space defaultSpace;
    public Map<String, Pose.Space> perBone = new HashMap<>();

    public Pose.Space forBone(String bone, Pose.Space fallback)
    {
        Pose.Space space = perBone.get(bone);
        if (space != null) return space;
        return defaultSpace != null ? defaultSpace : fallback;
    }

}
