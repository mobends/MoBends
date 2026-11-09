package net.minecraft.world.phys.shapes;

public final class Shapes {
    private static final VoxelShape BLOCK = new VoxelShape(false);
    private static final VoxelShape EMPTY = new VoxelShape(true);

    private Shapes() {
    }

    public static VoxelShape block() {
        return BLOCK;
    }

    public static VoxelShape empty() {
        return EMPTY;
    }
}
