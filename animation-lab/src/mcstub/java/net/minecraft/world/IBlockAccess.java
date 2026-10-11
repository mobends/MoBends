package net.minecraft.world;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;

/** What a block's shape is read from: the world, as in the game. */
public interface IBlockAccess
{
    IBlockState getBlockState(BlockPos pos);

    boolean isAirBlock(BlockPos pos);
}
