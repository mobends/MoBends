package net.minecraft.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

public class Block
{
    private final String name;
    private final IBlockState defaultState;

    public Block(String name)
    {
        this.name = name;
        this.defaultState = new IBlockState(this);
    }

    public String getName() { return name; }
    public IBlockState getDefaultState() { return defaultState; }

    /** The box things collide with at the position, or null: stone's is the full cube, as the world has it. */
    public AxisAlignedBB getCollisionBoundingBox(World world, BlockPos pos, IBlockState state)
    {
        return this == Blocks.stone ? new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1) : null;
    }
}
