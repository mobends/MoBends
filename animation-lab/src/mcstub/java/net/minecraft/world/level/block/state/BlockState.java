package net.minecraft.world.level.block.state;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.HashMap;
import java.util.Map;

/** A block with its properties. The game makes every state up front; here they're made as they're set. */
public class BlockState {
    private final Block block;
    private final Map<Property<?>, Comparable<?>> values = new HashMap<>();

    public BlockState(Block block) {
        this.block = block;
    }

    public Block getBlock() {
        return block;
    }

    public boolean isAir() {
        return block == Blocks.AIR;
    }

    /** What things collide with: stone is a full cube, as the level has it; every other block is passable. */
    public VoxelShape getCollisionShape(BlockGetter level, BlockPos pos) {
        return block == Blocks.STONE ? Shapes.block() : Shapes.empty();
    }

    public <T extends Comparable<T>, V extends T> BlockState setValue(Property<T> property, V value) {
        BlockState copy = new BlockState(block);
        copy.values.putAll(values);
        copy.values.put(property, value);
        return copy;
    }

    @SuppressWarnings("unchecked")
    public <T extends Comparable<T>> T getValue(Property<T> property) {
        Comparable<?> value = values.get(property);
        if (value == null) {
            throw new IllegalArgumentException("Cannot get property " + property + " as it does not exist in " + block);
        }
        return (T) value;
    }
}
