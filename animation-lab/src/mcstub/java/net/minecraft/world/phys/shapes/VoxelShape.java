package net.minecraft.world.phys.shapes;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;

/** A block's shape: the lab's blocks are either solid cubes or have none ({@link Shapes}). */
public final class VoxelShape {
    private final boolean empty;
    /** Where it is, for the shapes {@code Level.getCollisions} finds (a cube in the world); else null. */
    private final AABB bounds;

    VoxelShape(boolean empty) {
        this.empty = empty;
        this.bounds = null;
    }

    /** Lab: a cube at {@code bounds}, as the game's collision shapes are placed in the world. */
    public VoxelShape(AABB bounds) {
        this.empty = false;
        this.bounds = bounds;
    }

    public boolean isEmpty() {
        return empty;
    }

    /** The highest coordinate along the axis; only for a shape placed in the world. */
    public double max(Direction.Axis axis) {
        return switch (axis) {
            case X -> bounds.maxX;
            case Y -> bounds.maxY;
            case Z -> bounds.maxZ;
        };
    }
}
