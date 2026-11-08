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
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Map;

/**
 * {@code {"core:block_below": {...}}}: the block the entity stands on, or the first one below its
 * feet, every frame its node is posed. It looks straight down the column of blocks the entity's
 * position is in, from the one its feet are in (or, standing on a block's top, the one under it)
 * down to {@code distance} blocks below its feet. Its outputs are which block it found, and whether
 * it found one: where to look at the ground around it ({@code core:block_gradient}).
 */
public final class BlockBelowDriver
{

    public static final String BLOCK_X = "blockX", BLOCK_Y = "blockY", BLOCK_Z = "blockZ", FOUND = "found";

    public static class Template extends DriverItemTemplate
    {
        /** How far below the feet the block's top can be, in blocks: a number expression. */
        public ExpressionTemplate distance;
        /** Whether only blocks something collides with count (not water, tall grass or a torch). */
        public boolean solid = false;
        /**
         * The states its outputs go to: {@code blockX}, {@code blockY}, {@code blockZ} (doubles),
         * the block's position (the block the feet are in, where it found none); {@code found}
         * (a number), 1 where it found one, 0 where it didn't.
         */
        public Map<String, String> out;
    }

    public static final KumoDriver<Template> DRIVER = KumoDriver.of("core:block_below", Template.class, BlockBelowDriver::bind);

    private BlockBelowDriver()
    {
    }

    private static DriverEvaluator bind(Template template, DriverBindArgs args) throws MalformedKumoTemplateException
    {
        NumberInput distance = args.number("distance", template.distance);
        boolean solid = template.solid;
        DriverBindArgs.Outputs out = args.outputs(template.out, BLOCK_X, BLOCK_Y, BLOCK_Z, FOUND);
        DoubleStateHandle blockX = out.getDouble(BLOCK_X), blockY = out.getDouble(BLOCK_Y), blockZ = out.getDouble(BLOCK_Z);
        StateHandle found = out.get(FOUND);
        return (context, pose) -> {
            Object entity = context.entity();
            BlockPos block = null, at = null;
            if (entity instanceof Entity)
            {
                Entity e = (Entity) entity;
                block = find(e.world, e.posX, e.posY, e.posZ, Math.max(0, distance.get(context)), solid);
                at = block != null ? block : feetBlock(e.posX, e.posY, e.posZ);
            }
            if (blockX != null) blockX.set(context, at != null ? at.getX() : 0);
            if (blockY != null) blockY.set(context, at != null ? at.getY() : 0);
            if (blockZ != null) blockZ.set(context, at != null ? at.getZ() : 0);
            if (found != null) found.set(context, block != null ? 1 : 0);
        };
    }

    /** The block the feet at {@code (x, y, z)} are in; standing on a block's top, the block under them. */
    private static BlockPos feetBlock(double x, double y, double z)
    {
        return new BlockPos((int) Math.floor(x), (int) Math.floor(y - 1e-3), (int) Math.floor(z));
    }

    /**
     * The first block that counts (see {@link #counts}) down the column from the feet at
     * {@code (x, y, z)}, whose top is no more than {@code distance} below them; null where none is.
     */
    public static BlockPos find(World world, double x, double y, double z, double distance, boolean solid)
    {
        if (world == null)
        {
            return null;
        }
        BlockPos feet = feetBlock(x, y, z);
        for (int blockY = feet.getY(); blockY + 1 >= y - distance; blockY--)
        {
            BlockPos pos = new BlockPos(feet.getX(), blockY, feet.getZ());
            if (counts(world, pos, solid))
            {
                return pos;
            }
        }
        return null;
    }

    /** Whether the block counts as ground: any but air, or, {@code solid}, one something collides with. */
    static boolean counts(World world, BlockPos pos, boolean solid)
    {
        if (solid)
        {
            return world.getBlockState(pos).getCollisionBoundingBox(world, pos) != null;
        }
        return !world.isAirBlock(pos);
    }

}
