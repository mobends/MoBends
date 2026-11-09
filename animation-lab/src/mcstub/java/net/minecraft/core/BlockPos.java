package net.minecraft.core;

import net.minecraft.util.Mth;

public class BlockPos extends Vec3i {
    public BlockPos(int x, int y, int z) {
        super(x, y, z);
    }

    /** The block the point is in. */
    public static BlockPos containing(double x, double y, double z) {
        return new BlockPos(Mth.floor(x), Mth.floor(y), Mth.floor(z));
    }

    public BlockPos offset(int dx, int dy, int dz) {
        return dx == 0 && dy == 0 && dz == 0 ? this : new BlockPos(getX() + dx, getY() + dy, getZ() + dz);
    }

    public BlockPos above() {
        return above(1);
    }

    public BlockPos above(int distance) {
        return offset(0, distance, 0);
    }

    public BlockPos below() {
        return below(1);
    }

    public BlockPos below(int distance) {
        return offset(0, -distance, 0);
    }

    public BlockPos relative(Direction direction) {
        Vec3i normal = direction.getNormal();
        return offset(normal.getX(), normal.getY(), normal.getZ());
    }
}
