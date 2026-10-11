package goblinbob.mobends.core.kumo;

import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DoubleStateHandle;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.kumo.state.template.pose.PointTemplate;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

import java.util.Map;

/**
 * {@code {"core:raycast": {...}}}: a ray cast through the blocks of the entity's world, as vanilla
 * casts one for what the crosshair is on (the blocks' own shapes, past the ones that don't collide,
 * such as grass), every frame its node is posed. Its outputs are where it hit, in the world, and
 * which way the surface it hit faces: what to put a hand or a foot on.
 */
public final class RaycastDriver
{

    public static final String HIT_X = "hitX", HIT_Y = "hitY", HIT_Z = "hitZ";
    public static final String NORMAL_X = "normalX", NORMAL_Y = "normalY", NORMAL_Z = "normalZ";

    public static class Template extends DriverItemTemplate
    {
        /** Where the ray starts, in the world: double expressions, in blocks. */
        public PointTemplate from;
        /** Which way it goes, in the world: number expressions (of any length). */
        public PointTemplate direction;
        /** How far it goes, in blocks: a number expression. */
        public ExpressionTemplate distance;
        /** Whether it stops at water and lava. */
        public boolean liquids = false;
        /**
         * Draws the ray over the world every frame it is cast (see {@link RayGizmos}): a hit green, up
         * to where it hit, with the normal of the surface; a miss grey, its whole length.
         */
        public boolean debug = false;
        /**
         * The states its outputs go to: {@code hitX}, {@code hitY}, {@code hitZ} (doubles), where it
         * hit (the end of the ray where it hit nothing); {@code normalX}, {@code normalY},
         * {@code normalZ} (numbers), which way the surface it hit faces, a unit vector (zero where
         * it hit nothing).
         */
        public Map<String, String> out;
    }

    public static final KumoDriver<Template> DRIVER = KumoDriver.of("core:raycast", Template.class, RaycastDriver::bind);

    private RaycastDriver()
    {
    }

    private static DriverEvaluator bind(Template template, DriverBindArgs args) throws goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException
    {
        if (template.from == null || template.from.x == null || template.from.y == null || template.from.z == null)
        {
            throw args.error("needs 'from', {\"x\": ..., \"y\": ..., \"z\": ...}, in the world.");
        }
        if (template.direction == null)
        {
            throw args.error("needs a 'direction', {\"x\": ..., \"y\": ..., \"z\": ...}.");
        }
        DoubleInput fromX = args.doubleNumber("from.x", template.from.x);
        DoubleInput fromY = args.doubleNumber("from.y", template.from.y);
        DoubleInput fromZ = args.doubleNumber("from.z", template.from.z);
        NumberInput directionX = args.number("direction.x", template.direction.x, 0);
        NumberInput directionY = args.number("direction.y", template.direction.y, 0);
        NumberInput directionZ = args.number("direction.z", template.direction.z, 0);
        NumberInput distance = args.number("distance", template.distance);
        boolean liquids = template.liquids;
        boolean debug = template.debug;
        HitOutputs out = new HitOutputs(args, template.out);
        return (context, pose) -> {
            double x = fromX.get(context), y = fromY.get(context), z = fromZ.get(context);
            double dx = directionX.get(context), dy = directionY.get(context), dz = directionZ.get(context);
            double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double reach = Math.max(0, distance.get(context));
            double scale = length > 1e-9 ? reach / length : 0;
            double endX = x + dx * scale, endY = y + dy * scale, endZ = z + dz * scale;
            RayTraceResult hit = scale > 0 ? cast(world(context), x, y, z, endX, endY, endZ, liquids) : null;
            if (debug && scale > 0)
            {
                gizmo(hit == null ? RayGizmos.Kind.MISS : RayGizmos.Kind.KEPT, x, y, z, hit, endX, endY, endZ);
            }
            out.publish(context, hit, endX, endY, endZ);
        };
    }

    /** The world of the entity animated, or null. */
    static World world(goblinbob.mobends.core.kumo.api.EvalContext context)
    {
        Object entity = context.entity();
        return entity instanceof Entity ? ((Entity) entity).world : null;
    }

    /** The block face the segment crosses first, as vanilla traces it; null where it crosses none. */
    static RayTraceResult cast(World world, double x, double y, double z, double endX, double endY, double endZ, boolean liquids)
    {
        if (world == null)
        {
            return null;
        }
        RayTraceResult hit = world.rayTraceBlocks(new Vec3d(x, y, z), new Vec3d(endX, endY, endZ), liquids, true, false);
        return hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK && hit.sideHit != null ? hit : null;
    }

    /** Records the ray for {@link RayGizmos}: up to {@code hit}, or, where it is null, to {@code end}. */
    static void gizmo(RayGizmos.Kind kind, double x, double y, double z, RayTraceResult hit, double endX, double endY, double endZ)
    {
        if (hit == null)
        {
            RayGizmos.add(kind, x, y, z, endX, endY, endZ, 0, 0, 0);
            return;
        }
        Vec3i normal = hit.sideHit.getDirectionVec();
        RayGizmos.add(kind, x, y, z, hit.hitVec.x, hit.hitVec.y, hit.hitVec.z, normal.getX(), normal.getY(), normal.getZ());
    }

    /** The outputs of a ray, mapped to states: where it hit and which way the surface faces. */
    static final class HitOutputs
    {
        private final DoubleStateHandle hitX, hitY, hitZ;
        private final StateHandle normalX, normalY, normalZ;

        HitOutputs(DriverBindArgs args, Map<String, String> map) throws goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException
        {
            DriverBindArgs.Outputs out = args.outputs(map, HIT_X, HIT_Y, HIT_Z, NORMAL_X, NORMAL_Y, NORMAL_Z);
            hitX = out.getDouble(HIT_X);
            hitY = out.getDouble(HIT_Y);
            hitZ = out.getDouble(HIT_Z);
            normalX = out.get(NORMAL_X);
            normalY = out.get(NORMAL_Y);
            normalZ = out.get(NORMAL_Z);
        }

        /** {@code hit}, or, where it is null, a miss ending at {@code end}. */
        void publish(goblinbob.mobends.core.kumo.api.EvalContext context, RayTraceResult hit, double endX, double endY, double endZ)
        {
            Vec3i normal = null;
            if (hit != null)
            {
                endX = hit.hitVec.x;
                endY = hit.hitVec.y;
                endZ = hit.hitVec.z;
                normal = hit.sideHit.getDirectionVec();
            }
            if (hitX != null) hitX.set(context, endX);
            if (hitY != null) hitY.set(context, endY);
            if (hitZ != null) hitZ.set(context, endZ);
            if (normalX != null) normalX.set(context, normal == null ? 0 : normal.getX());
            if (normalY != null) normalY.set(context, normal == null ? 0 : normal.getY());
            if (normalZ != null) normalZ.set(context, normal == null ? 0 : normal.getZ());
        }
    }

}
