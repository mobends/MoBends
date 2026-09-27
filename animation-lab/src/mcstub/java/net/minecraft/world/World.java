package net.minecraft.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scriptable stand-in for the client world: a sparse block map and a flat floor.
 */
public class World
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

    public List<AxisAlignedBB> getCollisionBoxes(Entity entity, AxisAlignedBB box)
    {
        List<AxisAlignedBB> result = new ArrayList<>();
        if (box.minY < floorY)
        {
            result.add(new AxisAlignedBB(box.minX, floorY - 1, box.minZ, box.maxX, floorY, box.maxZ));
        }
        return result;
    }

    public void addEntity(Entity entity) { entities.add(entity); }

    public Entity getEntityByID(int id)
    {
        for (Entity e : entities) if (e.getEntityId() == id) return e;
        return null;
    }
}
