package net.minecraft.world.level.block;

import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class VineBlock extends Block {
    public static final BooleanProperty NORTH = BooleanProperty.create("north");
    public static final BooleanProperty EAST = BooleanProperty.create("east");
    public static final BooleanProperty SOUTH = BooleanProperty.create("south");
    public static final BooleanProperty WEST = BooleanProperty.create("west");

    public VineBlock(String name) {
        super(name);
        registerDefaultState(defaultBlockState().setValue(NORTH, false).setValue(EAST, false)
                .setValue(SOUTH, false).setValue(WEST, false));
    }
}
