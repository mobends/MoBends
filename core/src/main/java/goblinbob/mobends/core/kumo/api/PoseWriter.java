package goblinbob.mobends.core.kumo.api;

import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * What a driver sees of the pose being built: the bones by index ({@link DriverBindArgs#bone}),
 * what the items before it made of them, and writes composed in a space. A narrow view, so the
 * pose buffers can change without breaking drivers.
 */
public interface PoseWriter
{

    /** The bone's rotation as the items before this one left it. */
    void rotationSoFar(int bone, Quaternion dest);

    /** The bone's offset as the items before this one left it. */
    void offsetSoFar(int bone, Vec3f dest);

    /** The vector bone's value (a whole-model offset) as the items before this one left it. */
    void vectorSoFar(int bone, Vec3f dest);

    void rotate(int bone, Quaternion rotation, Pose.Space space);

    void offset(int bone, float x, float y, float z, Pose.Space space);

    void vector(int bone, float x, float y, float z, Pose.Space space);

    /** The bone's rotation jumps to its target this frame, past its damping. */
    void snapRotation(int bone);

    /** The vector bone jumps to its target this frame, past its damping. */
    void snapVector(int bone);

}
