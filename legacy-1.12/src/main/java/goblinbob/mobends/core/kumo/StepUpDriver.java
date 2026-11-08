package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.DoubleStateHandle;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PointTemplate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * {@code {"core:step_up": {...}}}: the step up ahead of the entity, every frame its node is posed:
 * what a mob walking that way would climb onto. From the entity's position it goes along
 * {@code direction} (across the ground, its Y left out), column of blocks by column, up to
 * {@code distance} blocks. The first column with a block at the height of its feet is the step;
 * a column whose ground is gone (a drop) ends the search with none. The step counts if it is one
 * block up, with room on top for the entity ({@code height} blocks), and room above the entity's
 * own head to rise onto it.
 *
 * <p>Its outputs are the step's block, and the way across its near face, along an axis: the face
 * the search went through, so walking at an angle into a ledge, it is the ledge's face.
 */
public final class StepUpDriver
{

    public static final String FOUND = "found", STEP_X = "stepX", STEP_Y = "stepY", STEP_Z = "stepZ", FACE_X = "faceX", FACE_Z = "faceZ";

    /** What the search found. */
    public enum Result
    {
        /** A step one block up, with room on it. */
        STEP,
        /** A block ahead that can't be climbed onto: higher than one block, or without room. */
        BLOCKED,
        /** Nothing within the distance, or the ground dropped away first. */
        NONE
    }

    public static class Template extends DriverItemTemplate
    {
        /** Which way to look, across the ground: number expressions (of any length; Y is left out). */
        public PointTemplate direction;
        /** How far to look, in blocks: a number expression. */
        public ExpressionTemplate distance;
        /** How tall the entity is, in blocks: the room it needs on the step. A number expression. */
        public ExpressionTemplate height;
        /** Whether only blocks something collides with count (not water, tall grass or a torch). */
        public boolean solid = false;
        /**
         * Draws the search over the world (see {@link RayGizmos}): green up to a step it found,
         * red up to a block it can't climb, grey its whole length where it found nothing.
         */
        public boolean debug = false;
        /**
         * The states its outputs go to: {@code found} (a number), 1 where it found a step, 0
         * where it didn't; {@code stepX}, {@code stepY}, {@code stepZ} (doubles), the step's block;
         * {@code faceX}, {@code faceZ} (numbers), the way into its near face: one is 1 or -1, the
         * other 0. Where it found none, it leaves all but {@code found} as they were.
         */
        public Map<String, String> out;
    }

    public static final KumoDriver<Template> DRIVER = KumoDriver.of("core:step_up", Template.class, StepUpDriver::bind);

    private StepUpDriver()
    {
    }

    private static DriverEvaluator bind(Template template, DriverBindArgs args) throws MalformedKumoTemplateException
    {
        if (template.direction == null)
        {
            throw args.error("needs a 'direction', {\"x\": ..., \"z\": ...}.");
        }
        NumberInput directionX = args.number("direction.x", template.direction.x, 0);
        NumberInput directionZ = args.number("direction.z", template.direction.z, 0);
        NumberInput distance = args.number("distance", template.distance);
        NumberInput height = args.number("height", template.height);
        boolean solid = template.solid;
        boolean debug = template.debug;
        DriverBindArgs.Outputs out = args.outputs(template.out, FOUND, STEP_X, STEP_Y, STEP_Z, FACE_X, FACE_Z);
        StateHandle found = out.get(FOUND), faceX = out.get(FACE_X), faceZ = out.get(FACE_Z);
        DoubleStateHandle stepX = out.getDouble(STEP_X), stepY = out.getDouble(STEP_Y), stepZ = out.getDouble(STEP_Z);
        int[] step = new int[5];
        return (context, pose) -> {
            Object entity = context.entity();
            Result result = Result.NONE;
            if (entity instanceof Entity)
            {
                Entity e = (Entity) entity;
                result = search(e.world, e.posX, e.posY, e.posZ, directionX.get(context), directionZ.get(context),
                        distance.get(context), height.get(context), solid, step);
                if (debug)
                {
                    gizmo(result, e.posX, e.posY, e.posZ, directionX.get(context), directionZ.get(context), distance.get(context), step);
                }
            }
            if (found != null) found.set(context, result == Result.STEP ? 1 : 0);
            if (result != Result.STEP)
            {
                return;
            }
            if (stepX != null) stepX.set(context, step[0]);
            if (stepY != null) stepY.set(context, step[1]);
            if (stepZ != null) stepZ.set(context, step[2]);
            if (faceX != null) faceX.set(context, step[3]);
            if (faceZ != null) faceZ.set(context, step[4]);
        };
    }

    /**
     * The search from the feet at {@code (x, y, z)} along {@code (dx, dz)}; where it ends at a
     * block (a step, or one it can't climb), into {@code step}: the block's x, y, z, then the way
     * into its near face, x and z.
     */
    public static Result search(World world, double x, double y, double z, double dx, double dz, double distance,
                                double height, boolean solid, int[] step)
    {
        double length = Math.sqrt(dx * dx + dz * dz);
        if (world == null || length < 1e-9 || distance <= 0)
        {
            return Result.NONE;
        }
        dx /= length;
        dz /= length;
        // The ground the feet are on: within a block of them.
        BlockPos ground = BlockBelowDriver.find(world, x, y, z, 1, solid);
        if (ground == null)
        {
            return Result.NONE;
        }
        int groundY = ground.getY();
        int room = Math.max(1, (int) Math.ceil(height - 1e-6));
        int column = (int) Math.floor(x), row = (int) Math.floor(z);
        int stepCol = dx > 0 ? 1 : -1, stepRow = dz > 0 ? 1 : -1;
        // Along the line, how far to the next column's edge and the next row's (a grid walk).
        double nextX = Math.abs(dx) < 1e-9 ? Double.POSITIVE_INFINITY : ((dx > 0 ? column + 1 : column) - x) / dx;
        double nextZ = Math.abs(dz) < 1e-9 ? Double.POSITIVE_INFINITY : ((dz > 0 ? row + 1 : row) - z) / dz;
        double perX = Math.abs(dx) < 1e-9 ? Double.POSITIVE_INFINITY : 1 / Math.abs(dx);
        double perZ = Math.abs(dz) < 1e-9 ? Double.POSITIVE_INFINITY : 1 / Math.abs(dz);
        int startColumn = column, startRow = row;
        while (true)
        {
            int faceX = 0, faceZ = 0;
            if (nextX < nextZ)
            {
                if (nextX > distance) return Result.NONE;
                column += stepCol;
                nextX += perX;
                faceX = stepCol;
            }
            else
            {
                if (nextZ > distance) return Result.NONE;
                row += stepRow;
                nextZ += perZ;
                faceZ = stepRow;
            }
            if (BlockBelowDriver.counts(world, new BlockPos(column, groundY + 1, row), solid))
            {
                step[0] = column;
                step[1] = groundY + 1;
                step[2] = row;
                step[3] = faceX;
                step[4] = faceZ;
                // Room on the step, and above the entity's head to rise onto it.
                for (int k = 2; k <= room + 1; k++)
                {
                    if (BlockBelowDriver.counts(world, new BlockPos(column, groundY + k, row), solid)) return Result.BLOCKED;
                }
                if (BlockBelowDriver.counts(world, new BlockPos(startColumn, groundY + room + 1, startRow), solid)) return Result.BLOCKED;
                return Result.STEP;
            }
            if (!BlockBelowDriver.counts(world, new BlockPos(column, groundY, row), solid))
            {
                return Result.NONE;
            }
        }
    }

    /** Records the search for {@link RayGizmos}, half a block above the feet: up to the block it ended at, or its whole length. */
    private static void gizmo(Result result, double x, double y, double z, double dx, double dz, double distance, int[] step)
    {
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 1e-9 || distance <= 0)
        {
            return;
        }
        dx /= length;
        dz /= length;
        double reach = distance;
        if (result != Result.NONE)
        {
            // Where the line crosses the block's near face.
            reach = step[3] != 0
                    ? ((step[3] > 0 ? step[0] : step[0] + 1) - x) / dx
                    : ((step[4] > 0 ? step[2] : step[2] + 1) - z) / dz;
        }
        RayGizmos.Kind kind = result == Result.STEP ? RayGizmos.Kind.KEPT : result == Result.BLOCKED ? RayGizmos.Kind.WRONG_SURFACE : RayGizmos.Kind.MISS;
        RayGizmos.add(kind, x, y + 0.5, z, x + dx * reach, y + 0.5, z + dz * reach, -step[3], 0, -step[4]);
    }

}
