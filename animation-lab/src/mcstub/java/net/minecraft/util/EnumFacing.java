package net.minecraft.util;

import net.minecraft.util.math.Vec3i;

public enum EnumFacing
{
    DOWN(new Vec3i(0, -1, 0), -1),
    UP(new Vec3i(0, 1, 0), -1),
    NORTH(new Vec3i(0, 0, -1), 2),
    SOUTH(new Vec3i(0, 0, 1), 0),
    WEST(new Vec3i(-1, 0, 0), 1),
    EAST(new Vec3i(1, 0, 0), 3);

    private final Vec3i directionVec;
    private final int horizontalIndex;

    EnumFacing(Vec3i directionVec, int horizontalIndex)
    {
        this.directionVec = directionVec;
        this.horizontalIndex = horizontalIndex;
    }

    public Vec3i getDirectionVec() { return directionVec; }
    public int getHorizontalIndex() { return horizontalIndex; }

    /** Same formula as the game: (horizontalIndex & 3) * 90. */
    public float getHorizontalAngle() { return (float) ((horizontalIndex & 3) * 90); }
}
