package goblinbob.mobends.core.kumo.bind;

import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * The pose the subject's own (vanilla) model has this frame, as the game and any mod posing that
 * model left it, given bone by bone in the bones' own terms: what a {@code core:vanilla_pose} node
 * starts from (see {@link goblinbob.mobends.core.kumo.IKumoSubject#getVanillaPose}). Only the host knows how its model is
 * posed and how its parts map onto the bones; the core asks for nothing else.
 */
public interface IVanillaPose
{

    /**
     * Reads bone {@code bone}'s pose: the rotation and the offset that, written as they are
     * ({@code override}), draw the bone where the vanilla model draws its part.
     *
     * @param rotation set to the bone's rotation (after its rest rotation, as a clip's)
     * @param offset   set to the bone's offset from where it sits, in model units
     * @return false where there is no pose to read for the bone (it stands for no part of the
     *         model, or the model isn't posed this frame), and nothing was set
     */
    boolean read(String bone, Quaternion rotation, Vec3f offset);

    /**
     * Told that the entity's own bones ({@code root}, {@code center}) were put at rest for this pose:
     * the entity's frame is vanilla's this frame. Whatever the host does to the whole entity in place
     * of vanilla, for the animator to do instead (a swimming tilt, a sneaking offset), it leaves to
     * vanilla.
     */
    default void onEntityFrameLeftToVanilla()
    {
    }

}
