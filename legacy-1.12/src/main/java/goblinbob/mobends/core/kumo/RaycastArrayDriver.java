package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.PointTemplate;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

/**
 * {@code {"core:raycast_array": {...}}}: {@code core:raycast} as a shotgun spread. It casts
 * {@code count} rays from {@code from}, in a cone {@code spread} degrees round {@code direction},
 * and of those that hit something (on a surface facing {@code surface}, if it is given) keeps the
 * one closest to {@code direction}: a better chance at finding somewhere to put a hand than one
 * ray, which misses a ledge that is a little off where it was expected. Its outputs are
 * {@code core:raycast}'s, of the ray it keeps.
 *
 * <p>The rays go out the same way every frame: the first along {@code direction}, then each
 * further from it than the one before, turning round it by the golden angle, the last at the
 * edge of the cone (the seeds of a sunflower, looked at down the middle). So the first ray that
 * counts, in that order, is the closest.
 */
public final class RaycastArrayDriver
{

    /** The most rays one cast sends out. */
    public static final int MAX_COUNT = 64;

    /** Turns round the middle between one ray and the next, in radians: the golden angle. */
    private static final double GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5));

    public static class Template extends RaycastDriver.Template
    {
        /** How many rays it casts, 1 to {@value #MAX_COUNT}. */
        public int count = 16;
        /** The angle from {@code direction} to the edge of the cone, in degrees. */
        public float spread = 15F;
        /**
         * Only hits on surfaces facing this way (within 60°) count: number expressions, of any
         * length; {@code {"y": 1}}, a top to stand or put a hand on. Left out, any hit counts.
         */
        public PointTemplate surface;
        // debug (from core:raycast) draws every ray: the one kept green, other hits yellow, hits on
        // a surface facing the wrong way red, misses grey.
    }

    public static final KumoDriver<Template> DRIVER = KumoDriver.of("core:raycast_array", Template.class, RaycastArrayDriver::bind);

    private RaycastArrayDriver()
    {
    }

    private static DriverEvaluator bind(Template template, DriverBindArgs args) throws MalformedKumoTemplateException
    {
        if (template.from == null || template.from.x == null || template.from.y == null || template.from.z == null)
        {
            throw args.error("needs 'from', {\"x\": ..., \"y\": ..., \"z\": ...}, in the world.");
        }
        if (template.direction == null)
        {
            throw args.error("needs a 'direction', {\"x\": ..., \"y\": ..., \"z\": ...}.");
        }
        if (template.count < 1 || template.count > MAX_COUNT)
        {
            throw args.error("'count' is how many rays it casts, 1 to " + MAX_COUNT + ".");
        }
        if (template.spread < 0 || template.spread >= 90)
        {
            throw args.error("'spread' is the angle to the edge of the cone, at least 0 and under 90 degrees.");
        }
        DoubleInput fromX = args.doubleNumber("from.x", template.from.x);
        DoubleInput fromY = args.doubleNumber("from.y", template.from.y);
        DoubleInput fromZ = args.doubleNumber("from.z", template.from.z);
        NumberInput directionX = args.number("direction.x", template.direction.x, 0);
        NumberInput directionY = args.number("direction.y", template.direction.y, 0);
        NumberInput directionZ = args.number("direction.z", template.direction.z, 0);
        NumberInput distance = args.number("distance", template.distance);
        NumberInput[] surface = template.surface == null ? null : new NumberInput[] {
                args.number("surface.x", template.surface.x, 0),
                args.number("surface.y", template.surface.y, 0),
                args.number("surface.z", template.surface.z, 0)};
        boolean liquids = template.liquids;
        boolean debug = template.debug;
        RaycastDriver.HitOutputs out = new RaycastDriver.HitOutputs(args, template.out);

        // Each ray's angle from the middle (growing as the square root, so they cover the cone
        // evenly) and round it, worked out once.
        final int count = template.count;
        final double[] cosOff = new double[count], sinOff = new double[count], cosRound = new double[count], sinRound = new double[count];
        final double spread = Math.toRadians(template.spread);
        for (int i = 0; i < count; i++)
        {
            double off = count == 1 ? 0 : spread * Math.sqrt(i / (double) (count - 1));
            double round = i * GOLDEN_ANGLE;
            cosOff[i] = Math.cos(off);
            sinOff[i] = Math.sin(off);
            cosRound[i] = Math.cos(round);
            sinRound[i] = Math.sin(round);
        }

        return (context, pose) -> {
            double x = fromX.get(context), y = fromY.get(context), z = fromZ.get(context);
            double dx = directionX.get(context), dy = directionY.get(context), dz = directionZ.get(context);
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double reach = Math.max(0, distance.get(context));
            if (length < 1e-9 || reach <= 0)
            {
                out.publish(context, null, x, y, z);
                return;
            }
            dx /= length;
            dy /= length;
            dz /= length;
            double sx = 0, sy = 0, sz = 0;
            if (surface != null)
            {
                sx = surface[0].get(context);
                sy = surface[1].get(context);
                sz = surface[2].get(context);
                double surfaceLength = Math.sqrt(sx * sx + sy * sy + sz * sz);
                if (surfaceLength > 1e-9)
                {
                    sx /= surfaceLength;
                    sy /= surfaceLength;
                    sz /= surfaceLength;
                }
            }

            // Two directions across the middle one: the axis it is least along, crossed with it.
            double ax = Math.abs(dx) < 0.9 ? 1 : 0, ay = ax == 1 ? 0 : 1;
            double ux = ay * dz, uy = -ax * dz, uz = ax * dy - ay * dx;
            double uLength = Math.sqrt(ux * ux + uy * uy + uz * uz);
            ux /= uLength;
            uy /= uLength;
            uz /= uLength;
            double vx = dy * uz - dz * uy, vy = dz * ux - dx * uz, vz = dx * uy - dy * ux;

            World world = RaycastDriver.world(context);
            RayTraceResult kept = null;
            for (int i = 0; i < count; i++)
            {
                double across = sinOff[i];
                double rx = dx * cosOff[i] + (ux * cosRound[i] + vx * sinRound[i]) * across;
                double ry = dy * cosOff[i] + (uy * cosRound[i] + vy * sinRound[i]) * across;
                double rz = dz * cosOff[i] + (uz * cosRound[i] + vz * sinRound[i]) * across;
                double endX = x + rx * reach, endY = y + ry * reach, endZ = z + rz * reach;
                RayTraceResult hit = RaycastDriver.cast(world, x, y, z, endX, endY, endZ, liquids);
                RayGizmos.Kind kind;
                if (hit == null)
                {
                    kind = RayGizmos.Kind.MISS;
                }
                else if (surface != null && !faces(hit, sx, sy, sz))
                {
                    kind = RayGizmos.Kind.WRONG_SURFACE;
                }
                else
                {
                    kind = kept == null ? RayGizmos.Kind.KEPT : RayGizmos.Kind.HIT;
                    if (kept == null)
                    {
                        kept = hit;
                    }
                }
                if (debug)
                {
                    RaycastDriver.gizmo(kind, x, y, z, hit, endX, endY, endZ);
                }
                else if (kept != null)
                {
                    // Only the gizmos need the rays after the one kept.
                    break;
                }
            }
            if (kept != null)
            {
                out.publish(context, kept, 0, 0, 0);
                return;
            }
            // Nothing counted: a miss along the middle.
            out.publish(context, null, x + dx * reach, y + dy * reach, z + dz * reach);
        };
    }

    /** Whether the surface {@code hit} faces the unit vector {@code s}, within 60°. */
    private static boolean faces(RayTraceResult hit, double sx, double sy, double sz)
    {
        Vec3i normal = hit.sideHit.getDirectionVec();
        return normal.getX() * sx + normal.getY() * sy + normal.getZ() * sz >= 0.5;
    }

}
