package net.minecraft.core;

public enum Direction {
    DOWN(-1, new Vec3i(0, -1, 0)),
    UP(-1, new Vec3i(0, 1, 0)),
    NORTH(2, new Vec3i(0, 0, -1)),
    SOUTH(0, new Vec3i(0, 0, 1)),
    WEST(1, new Vec3i(-1, 0, 0)),
    EAST(3, new Vec3i(1, 0, 0));

    /** The index among the horizontal directions (south, west, north, east), or -1. */
    private final int data2d;
    private final Vec3i normal;

    Direction(int data2d, Vec3i normal) {
        this.data2d = data2d;
        this.normal = normal;
    }

    public Vec3i getNormal() {
        return normal;
    }

    /** The yaw that faces this way: (index among the horizontal directions & 3) * 90, as the game has it. */
    public float toYRot() {
        return (float) ((data2d & 3) * 90);
    }
}
