package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public interface BlockGetter {
    BlockState getBlockState(BlockPos pos);
}
