package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

/**
 * Where the golem's hands are drawn in the world, put back together by forward kinematics from
 * the bones' smoothed rotations as the renderer draws them: the arm and forearm about their
 * pivots, the model flipped in Y and Z with its origin 1.501 blocks up (vanilla's rocking of a
 * walking golem turned back by the renderer), the center turned about its pivot and moved, the
 * root turned and moved, and the body's yaw.
 */
final class GolemHands
{

    private GolemHands()
    {
    }

    /** The bottom of the hand: the middle of the arm box's lowest face. */
    static double[] hand(DefinedEntityData<?> data, boolean right)
    {
        Quaternion arm = data.getPart(right ? "rightArm" : "leftArm").rotation.getSmooth();
        Quaternion fore = data.getPart(right ? "rightForeArm" : "leftForeArm").rotation.getSmooth();
        double[] lower = add(new double[] {0, 12.5, 3}, rotate(fore, right ? -11 : 11, 15, -3));
        double[] model = add(new double[] {0, -7, 0}, rotate(arm, lower[0], lower[1], lower[2]));
        return toWorld(data, model);
    }

    /** A point in the model (model units, +Y down, -Z forward) into the world. */
    static double[] toWorld(DefinedEntityData<?> data, double[] model)
    {
        double[] v = {model[0] / 16, 1.501 - model[1] / 16, -model[2] / 16};
        // The center turns about its pivot, then moves; then the root turns, then moves.
        Vec3f pivot = data.getCenterPivot(new Vec3f());
        v = rotate(data.center.rotation.getSmooth(), v[0] - pivot.x, v[1] - pivot.y, v[2] - pivot.z);
        v = add(v, new double[] {pivot.x + data.center.position.getX() / 16, pivot.y + data.center.position.getY() / 16, pivot.z + data.center.position.getZ() / 16});
        v = rotate(data.root.rotation.getSmooth(), v[0], v[1], v[2]);
        v = add(v, new double[] {data.root.position.getX() / 16, data.root.position.getY() / 16, data.root.position.getZ() / 16});
        double yaw = Math.toRadians(data.getVariable("entityBodyYaw")), cos = Math.cos(yaw), sin = Math.sin(yaw);
        return new double[] {
                data.getVariable("entityWorldX") + v[0] * cos - v[2] * sin,
                data.getVariable("entityWorldY") + v[1],
                data.getVariable("entityWorldZ") + v[0] * sin + v[2] * cos};
    }

    static double[] rotate(Quaternion q, double x, double y, double z)
    {
        double tx = 2 * (q.y * z - q.z * y), ty = 2 * (q.z * x - q.x * z), tz = 2 * (q.x * y - q.y * x);
        return new double[] {x + q.w * tx + q.y * tz - q.z * ty, y + q.w * ty + q.z * tx - q.x * tz, z + q.w * tz + q.x * ty - q.y * tx};
    }

    static double[] add(double[] a, double[] b)
    {
        return new double[] {a[0] + b[0], a[1] + b[1], a[2] + b[2]};
    }

    static double distance(double[] a, double[] b)
    {
        double dx = a[0] - b[0], dy = a[1] - b[1], dz = a[2] - b[2];
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

}
