package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.pose.AnchorTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;

/** See {@link AnchorTemplate}: the entity drawn at a point in the world. */
public final class AnchorDriver
{

    public static final String POSITION_DIFFERENCE = "positionDifference";

    public static final KumoDriver<AnchorTemplate> DRIVER = KumoDriver.of("core:anchor", AnchorTemplate.class, (template, args) -> {
        DoubleInput x = args.doubleNumber("x", template.x);
        DoubleInput y = args.doubleNumber("y", template.y);
        DoubleInput z = args.doubleNumber("z", template.z);
        if (template.point == null)
        {
            throw args.error("needs 'point', {\"x\": ..., \"y\": ..., \"z\": ...}, in the world.");
        }
        // An axis left out follows the entity.
        DoubleInput pointX = template.point.x == null ? x : args.doubleNumber("point.x", template.point.x);
        DoubleInput pointY = template.point.y == null ? y : args.doubleNumber("point.y", template.point.y);
        DoubleInput pointZ = template.point.z == null ? z : args.doubleNumber("point.z", template.point.z);
        NumberInput weight = args.number("weight", template.weight, 1);
        NumberInput yaw = args.number("yaw", template.yaw);
        StateHandle difference = args.outputs(template.out, POSITION_DIFFERENCE).get(POSITION_DIFFERENCE);
        int bone = args.bone("bone", template.bone);
        float units = template.unitsPerBlock;
        Vec3f below = new Vec3f();
        return (DriverEvaluator) (context, pose) -> {
            // In doubles until it's a difference: far from the origin, a float rounds the position.
            double dx = pointX.get(context) - x.get(context);
            double dy = pointY.get(context) - y.get(context);
            double dz = pointZ.get(context) - z.get(context);
            if (difference != null)
            {
                difference.set(context, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
            }

            // The renderer turns the model by -bodyYaw before it applies the offset: turn the
            // difference into that frame (+Z ahead of the body, +X to its left, +Y up).
            float w = Math.max(0F, Math.min(1F, weight.get(context)));
            double yawRad = Math.toRadians(yaw.get(context));
            double cos = Math.cos(yawRad), sin = Math.sin(yawRad);
            double lx = dx * cos + dz * sin;
            double lz = -dx * sin + dz * cos;
            pose.vectorSoFar(bone, below);
            pose.vector(bone, below.x + (float) (lx * units * w), below.y + (float) (dy * units * w), below.z + (float) (lz * units * w), Pose.Space.OVERRIDE);
            pose.snapVector(bone);
        };
    });

    private AnchorDriver()
    {
    }

}
