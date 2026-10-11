package net.minecraft.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scriptable stand-in for the client world: a sparse block map and a flat floor. Stone in the
 * map is solid (a full cube), as the floor is; every other block is passable.
 */
public class World implements IBlockAccess
{
    public boolean isRemote = true;

    private final Map<Long, IBlockState> blocks = new HashMap<>();
    private final List<Entity> entities = new ArrayList<>();

    /** Every block at or below this Y is solid (used for the on-ground collision test). */
    public double floorY = 0.0D;

    private static long key(int x, int y, int z)
    {
        return ((long) x & 0x3FFFFFFL) << 38 | ((long) z & 0x3FFFFFFL) << 12 | ((long) y & 0xFFFL);
    }

    public void setBlockState(BlockPos pos, IBlockState state)
    {
        blocks.put(key(pos.getX(), pos.getY(), pos.getZ()), state);
    }

    public IBlockState getBlockState(BlockPos pos)
    {
        IBlockState state = blocks.get(key(pos.getX(), pos.getY(), pos.getZ()));
        if (state != null) return state;
        return pos.getY() < floorY ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState();
    }

    public boolean isAirBlock(BlockPos pos)
    {
        return getBlockState(pos).getBlock() == Blocks.AIR;
    }

    public List<AxisAlignedBB> getCollisionBoxes(Entity entity, AxisAlignedBB box)
    {
        List<AxisAlignedBB> result = new ArrayList<>();
        if (box.minY < floorY)
        {
            result.add(new AxisAlignedBB(box.minX, floorY - 1, box.minZ, box.maxX, floorY, box.maxZ));
        }
        result.addAll(stoneIn(box));
        return result;
    }

    /** Lab only: the cubes of the stone blocks that overlap {@code box}. */
    public List<AxisAlignedBB> stoneIn(AxisAlignedBB box)
    {
        List<AxisAlignedBB> result = new ArrayList<>();
        for (Map.Entry<Long, IBlockState> block : blocks.entrySet())
        {
            if (block.getValue().getBlock() != Blocks.STONE) continue;
            long k = block.getKey();
            int x = (int) (k >> 38), z = (int) (k << 26 >> 38), y = (int) (k & 0xFFF);
            if (y >= 0x800) y -= 0x1000;
            AxisAlignedBB cube = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            if (cube.intersects(box)) result.add(cube);
        }
        return result;
    }

    /** Lab only: the top of the floor, or of the stone under {@code box} whose top is no higher than {@code maxY}. */
    public double groundUnder(AxisAlignedBB box, double maxY)
    {
        double ground = floorY;
        for (AxisAlignedBB cube : stoneIn(new AxisAlignedBB(box.minX, floorY, box.minZ, box.maxX, maxY, box.maxZ)))
        {
            if (cube.maxY <= maxY) ground = Math.max(ground, cube.maxY);
        }
        return ground;
    }

    /**
     * The first solid face the segment from {@code start} to {@code end} crosses (the stone cubes
     * and the top of the floor), or null; the flags are vanilla's, and every solid block here has
     * a box, so they change nothing.
     */
    public RayTraceResult rayTraceBlocks(Vec3d start, Vec3d end, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock)
    {
        double dx = end.x - start.x, dy = end.y - start.y, dz = end.z - start.z;
        double best = Double.POSITIVE_INFINITY;
        EnumFacing side = null;
        // The floor: everything below floorY is solid, so a ray going down meets its top.
        if (dy < 0 && start.y >= floorY && end.y < floorY)
        {
            best = (floorY - start.y) / dy;
            side = EnumFacing.UP;
        }
        // A hair larger, so a ray along a face still finds the cube.
        AxisAlignedBB around = new AxisAlignedBB(Math.min(start.x, end.x) - 1e-7, Math.min(start.y, end.y) - 1e-7, Math.min(start.z, end.z) - 1e-7,
                Math.max(start.x, end.x) + 1e-7, Math.max(start.y, end.y) + 1e-7, Math.max(start.z, end.z) + 1e-7);
        for (AxisAlignedBB cube : stoneIn(around))
        {
            // Slabs: where the ray enters the cube, and through which face.
            double enter = 0, leave = 1;
            EnumFacing face = null;
            double[] from = {start.x, start.y, start.z}, along = {dx, dy, dz};
            double[] min = {cube.minX, cube.minY, cube.minZ}, max = {cube.maxX, cube.maxY, cube.maxZ};
            EnumFacing[][] faces = {{EnumFacing.WEST, EnumFacing.EAST}, {EnumFacing.DOWN, EnumFacing.UP}, {EnumFacing.NORTH, EnumFacing.SOUTH}};
            boolean misses = false;
            for (int i = 0; i < 3 && !misses; i++)
            {
                if (Math.abs(along[i]) < 1e-12)
                {
                    misses = from[i] < min[i] || from[i] > max[i];
                    continue;
                }
                double t0 = (min[i] - from[i]) / along[i], t1 = (max[i] - from[i]) / along[i];
                EnumFacing near = along[i] > 0 ? faces[i][0] : faces[i][1];
                if (t0 > t1) { double t = t0; t0 = t1; t1 = t; }
                if (t0 > enter) { enter = t0; face = near; }
                leave = Math.min(leave, t1);
                misses = enter > leave;
            }
            if (!misses && face != null && enter < best)
            {
                best = enter;
                side = face;
            }
        }
        if (side == null) return null;
        return new RayTraceResult(new Vec3d(start.x + dx * best, start.y + dy * best, start.z + dz * best), side);
    }

    public void addEntity(Entity entity) { entities.add(entity); }

    public Entity getEntityByID(int id)
    {
        for (Entity e : entities) if (e.getEntityId() == id) return e;
        return null;
    }
}
