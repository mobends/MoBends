package net.minecraft.block.state;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

import java.util.HashMap;
import java.util.Map;

/** In the game this is an interface; a class is enough for the lab. */
public class IBlockState
{
    private final Block block;
    private final Map<IProperty<?>, Object> properties = new HashMap<>();

    public IBlockState(Block block) { this.block = block; }

    public Block getBlock() { return block; }

    /** The box things collide with, or null ({@code Block.NULL_AABB}): stone's is the full cube, as the world has it. */
    public AxisAlignedBB getCollisionBoundingBox(IBlockAccess world, BlockPos pos)
    {
        return block == Blocks.STONE ? new AxisAlignedBB(0, 0, 0, 1, 1, 1) : null;
    }

    public <T> IBlockState withProperty(IProperty<T> property, T value)
    {
        IBlockState copy = new IBlockState(block);
        copy.properties.putAll(properties);
        copy.properties.put(property, value);
        return copy;
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(IProperty<T> property)
    {
        Object value = properties.get(property);
        if (value == null)
        {
            throw new IllegalArgumentException("Property " + property.getName() + " not set on " + block.getName());
        }
        return (T) value;
    }
}
