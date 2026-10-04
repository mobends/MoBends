package goblinbob.mobends.core.kumo.state.template;

import goblinbob.mobends.core.kumo.state.template.pose.PoseItemTemplate;

import java.util.List;

/** {@code {"core:pose": {...}}}: an ordered stack of clips and drivers that together form the pose. */
public class PoseNodeTemplate extends NodeTemplate
{

    public List<PoseItemTemplate> pose;

}
