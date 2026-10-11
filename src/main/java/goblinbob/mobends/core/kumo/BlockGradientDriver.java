package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PointTemplate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.Map;

/**
 * {@code {"core:block_gradient": {...}}}: which way the ground rises round a block, every frame its
 * node is posed. It looks at the 3x3x3 blocks round {@code at} (a block the entity stands on, from
 * {@code core:block_below}): the blocks that count lean the surface, as a plane fitted through
 * them would, away from where they are. Its outputs are the surface's normal and the way up it,
 * the direction of quickest ascent: on flat ground the normal is straight up and there is no way
 * up; beside a step up, the way up points at the step.
 *
 * <p>The normal is the opposite of the blocks' first moment round the middle (each block that
 * counts, by where it is from the middle), which is, but for its length, the gradient of a plane
 * fitted to them by least squares. The way up is "up" along the surface, the world's up with the
 * normal taken out of it: its length is the sine of the slope, 0 on flat ground, 1 on a wall.
 */
public final class BlockGradientDriver
{

    public static final String GRADIENT_X = "gradientX", GRADIENT_Y = "gradientY", GRADIENT_Z = "gradientZ";
    public static final String NORMAL_X = "normalX", NORMAL_Y = "normalY", NORMAL_Z = "normalZ";

    public static class Template extends DriverItemTemplate
    {
        /** The block in the middle, in the world: double expressions, in blocks (a point in it will do). */
        public PointTemplate at;
        /** Whether only blocks something collides with count (not water, tall grass or a torch). */
        public boolean solid = false;
        /**
         * The states its outputs go to (numbers): {@code gradientX}, {@code gradientY},
         * {@code gradientZ}, the way up the surface (zero on flat ground, or where no block counts);
         * {@code normalX}, {@code normalY}, {@code normalZ}, which way the surface faces, a unit
         * vector (zero where the blocks that count lean it no way: none, or all round).
         */
        public Map<String, String> out;
    }

    public static final KumoDriver<Template> DRIVER = KumoDriver.of("core:block_gradient", Template.class, BlockGradientDriver::bind);

    private BlockGradientDriver()
    {
    }

    private static DriverEvaluator bind(Template template, DriverBindArgs args) throws MalformedKumoTemplateException
    {
        if (template.at == null || template.at.x == null || template.at.y == null || template.at.z == null)
        {
            throw args.error("needs 'at', {\"x\": ..., \"y\": ..., \"z\": ...}, the block in the middle, in the world.");
        }
        DoubleInput atX = args.doubleNumber("at.x", template.at.x);
        DoubleInput atY = args.doubleNumber("at.y", template.at.y);
        DoubleInput atZ = args.doubleNumber("at.z", template.at.z);
        boolean solid = template.solid;
        DriverBindArgs.Outputs out = args.outputs(template.out, GRADIENT_X, GRADIENT_Y, GRADIENT_Z, NORMAL_X, NORMAL_Y, NORMAL_Z);
        StateHandle[] handles = {out.get(GRADIENT_X), out.get(GRADIENT_Y), out.get(GRADIENT_Z), out.get(NORMAL_X), out.get(NORMAL_Y), out.get(NORMAL_Z)};
        double[] result = new double[6];
        return (context, pose) -> {
            BlockPos middle = new BlockPos((int) Math.floor(atX.get(context)), (int) Math.floor(atY.get(context)), (int) Math.floor(atZ.get(context)));
            compute(RaycastDriver.world(context), middle, solid, result);
            for (int i = 0; i < handles.length; i++)
            {
                if (handles[i] != null) handles[i].set(context, (float) result[i]);
            }
        };
    }

    /**
     * Into {@code result}: the way up the surface round {@code middle} (x, y, z), then its normal
     * (x, y, z); all zero where the blocks that count (see {@code core:block_below}'s) lean it no way.
     */
    public static void compute(World world, BlockPos middle, boolean solid, double[] result)
    {
        double mx = 0, my = 0, mz = 0;
        if (world != null)
        {
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++)
                    for (int dz = -1; dz <= 1; dz++)
                    {
                        if ((dx | dy | dz) != 0 && BlockBelowDriver.counts(world, middle.add(dx, dy, dz), solid))
                        {
                            mx += dx;
                            my += dy;
                            mz += dz;
                        }
                    }
        }
        double length = Math.sqrt(mx * mx + my * my + mz * mz);
        if (length < 1e-9)
        {
            Arrays.fill(result, 0);
            return;
        }
        double nx = -mx / length, ny = -my / length, nz = -mz / length;
        // Up, less its part along the normal.
        result[0] = -nx * ny;
        result[1] = 1 - ny * ny;
        result[2] = -nz * ny;
        result[3] = nx;
        result[4] = ny;
        result[5] = nz;
    }

}
