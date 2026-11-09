package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.BlockState;

/** Stand-in for the game's Block; the lab's are in {@link Blocks}. */
public class Block {
    private final String name;
    private BlockState defaultBlockState;

    public Block(String name) {
        this.name = name;
        this.defaultBlockState = new BlockState(this);
    }

    public final BlockState defaultBlockState() {
        return defaultBlockState;
    }

    protected final void registerDefaultState(BlockState state) {
        this.defaultBlockState = state;
    }

    @Override
    public String toString() {
        return "Block{minecraft:" + name + "}";
    }
}
