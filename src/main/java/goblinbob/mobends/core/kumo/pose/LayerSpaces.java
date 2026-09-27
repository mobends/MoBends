package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.template.LayerTemplate;
import goblinbob.mobends.core.kumo.state.template.SpaceTemplate;

/**
 * The composition space a layer assigns to each bone by default: OVERRIDE for override layers,
 * the (per-bone) additive space for additive layers. Items without an explicit space use it.
 */
public class LayerSpaces
{

    private final Skeleton skeleton;
    private final Pose.Space defaultSpace;
    private final SpaceTemplate perBone;

    /** The layer's mirroring rule, if any. */
    public final LayerMirror mirror;

    public LayerSpaces(Skeleton skeleton, LayerTemplate layer) throws goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException
    {
        this.skeleton = skeleton;
        this.defaultSpace = layer.defaultAdditiveSpace();
        this.perBone = layer.mode == LayerTemplate.LayerMode.ADDITIVE ? layer.additiveSpace : null;
        this.mirror = layer.mirror == null ? null : new LayerMirror(skeleton, layer.mirror);
    }

    public Pose.Space forSlot(int slot)
    {
        if (perBone == null)
        {
            return defaultSpace;
        }
        return perBone.forBone(skeleton.nameOf(slot), defaultSpace);
    }

    public Pose.Space[] resolve(int[] slots, Pose.Space itemSpace)
    {
        Pose.Space[] result = new Pose.Space[slots.length];
        for (int i = 0; i < slots.length; i++)
        {
            result[i] = itemSpace != null ? itemSpace : forSlot(slots[i]);
        }
        return result;
    }

}
