package net.minecraft.world.phys.shapes;

/** A block's shape: the lab's blocks are either solid cubes or have none ({@link Shapes}). */
public final class VoxelShape {
    private final boolean empty;

    VoxelShape(boolean empty) {
        this.empty = empty;
    }

    public boolean isEmpty() {
        return empty;
    }
}
