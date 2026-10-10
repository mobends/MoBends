package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.FloatArraySlot;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.AnchorTemplate;
import goblinbob.mobends.core.math.vector.Vec3f;

/** See {@link AnchorTemplate}: the entity drawn at a point in the world. */
public final class AnchorDriver
{

    public static final String POSITION_DIFFERENCE = "positionDifference";

    /** The state: whether the block is taken yet, and the block (whole numbers, exact in a float well past any world's edge). */
    private static final int TAKEN = 0, BLOCK_X = 1, BLOCK_Y = 2, BLOCK_Z = 3, STATE_SIZE = 4;

    public static final KumoDriver<AnchorTemplate> DRIVER = KumoDriver.of("core:anchor", AnchorTemplate.class, (template, args) -> {
        NumberInput[] block = point(args, "block", template.block);
        NumberInput[] offset = point(args, "offset", template.offset);
        NumberInput weight = args.number("weight", template.weight, 1);
        NumberInput yaw = args.entityValue("yawVariable", template.yawVariable);
        NumberInput x = args.entityValue("xVariable", template.xVariable);
        NumberInput y = args.entityValue("yVariable", template.yVariable);
        NumberInput z = args.entityValue("zVariable", template.zVariable);
        StateHandle difference = args.outputs(template.out, POSITION_DIFFERENCE).get(POSITION_DIFFERENCE);
        int bone = args.bone("bone", template.bone);
        float units = template.unitsPerBlock;
        FloatArraySlot state = args.slots("state", STATE_SIZE, 0);
        Vec3f below = new Vec3f();
        return (DriverEvaluator) (context, pose) -> {
            double px = x.getDouble(context), py = y.getDouble(context), pz = z.getDouble(context);
            if (state.get(context, TAKEN) == 0)
            {
                state.set(context, BLOCK_X, (float) (Math.floor(px) + Math.floor(block[0].get(context))));
                state.set(context, BLOCK_Y, (float) (Math.floor(py) + Math.floor(block[1].get(context))));
                state.set(context, BLOCK_Z, (float) (Math.floor(pz) + Math.floor(block[2].get(context))));
                state.set(context, TAKEN, 1);
            }
            double dx = state.get(context, BLOCK_X) + offset[0].getDouble(context) - px;
            double dy = state.get(context, BLOCK_Y) + offset[1].getDouble(context) - py;
            double dz = state.get(context, BLOCK_Z) + offset[2].getDouble(context) - pz;
            if (difference != null)
            {
                difference.set(context, (float) Math.sqrt(dx * dx + dy * dy + dz * dz));
            }

            // The renderer turns the model by -bodyYaw before it applies the offset: turn the
            // difference into that frame (+Z ahead of the body, +X to its left, +Y up).
            float w = Math.max(0F, Math.min(1F, weight.get(context)));
            double yawRad = Math.toRadians(yaw.getDouble(context));
            double cos = Math.cos(yawRad), sin = Math.sin(yawRad);
            double lx = dx * cos + dz * sin;
            double lz = -dx * sin + dz * cos;
            pose.vectorSoFar(bone, below);
            pose.vector(bone, below.x + (float) (lx * units * w), below.y + (float) (dy * units * w), below.z + (float) (lz * units * w), Pose.Space.OVERRIDE);
            pose.snapVector(bone);
        };
    });

    private static NumberInput[] point(DriverBindArgs args, String field, AnchorTemplate.Point point) throws MalformedKumoTemplateException
    {
        if (point == null)
        {
            throw args.error("needs '" + field + "', {\"x\": ..., \"y\": ..., \"z\": ...}.");
        }
        return new NumberInput[] {
                args.number(field + ".x", point.x, 0),
                args.number(field + ".y", point.y, 0),
                args.number(field + ".z", point.z, 0),
        };
    }

    private AnchorDriver()
    {
    }

}
