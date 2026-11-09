package net.minecraft.world.level.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

public class LadderBlock extends Block {
    /** The way the ladder faces, away from the wall it's on. */
    public static final DirectionProperty FACING = DirectionProperty.create("facing");

    public LadderBlock(String name) {
        super(name);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }
}
