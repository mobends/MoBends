package net.minecraft.world.phys;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BlockHitResult extends HitResult {
    private final Direction direction;
    private final BlockPos blockPos;
    private final boolean miss;

    public BlockHitResult(Vec3 location, Direction direction, BlockPos blockPos, boolean inside) {
        this(false, location, direction, blockPos);
    }

    private BlockHitResult(boolean miss, Vec3 location, Direction direction, BlockPos blockPos) {
        super(location);
        this.miss = miss;
        this.direction = direction;
        this.blockPos = blockPos;
    }

    /** Nothing was hit on the way to {@code location}. */
    public static BlockHitResult miss(Vec3 location, Direction direction, BlockPos blockPos) {
        return new BlockHitResult(true, location, direction, blockPos);
    }

    /** The face that was hit. */
    public Direction getDirection() {
        return direction;
    }

    public BlockPos getBlockPos() {
        return blockPos;
    }

    @Override
    public Type getType() {
        return miss ? Type.MISS : Type.BLOCK;
    }
}
