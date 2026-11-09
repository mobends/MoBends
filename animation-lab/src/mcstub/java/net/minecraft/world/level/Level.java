package net.minecraft.world.level;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A scriptable stand-in for the game's level: a sparse block map over a flat floor. Stone in the
 * map is solid (a full cube), as the floor is; every other block is passable.
 */
public abstract class Level implements BlockGetter {
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();

    /** Lab: every block below this Y is stone (used for the on-ground collision test). */
    public double floorY = 0.0;

    public boolean setBlockAndUpdate(BlockPos pos, BlockState state) {
        blocks.put(new BlockPos(pos.getX(), pos.getY(), pos.getZ()), state);
        return true;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = blocks.get(new BlockPos(pos.getX(), pos.getY(), pos.getZ()));
        if (state != null) {
            return state;
        }
        return pos.getY() < floorY ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
    }

    public boolean isEmptyBlock(BlockPos pos) {
        return getBlockState(pos).isAir();
    }

    @Nullable
    public abstract Entity getEntity(int id);

    /** Whether nothing solid overlaps {@code box}: the floor, or the stone. */
    public boolean noCollision(@Nullable Entity entity, AABB box) {
        return box.minY >= floorY && stoneIn(box).isEmpty();
    }

    /** Lab only: the cubes of the stone blocks that overlap {@code box}. */
    /** The collision shapes the box meets, placed in the world: the stone cubes, and the floor. */
    public Iterable<VoxelShape> getCollisions(@Nullable Entity entity, AABB box) {
        List<VoxelShape> shapes = new ArrayList<>();
        if (box.minY < floorY) {
            shapes.add(new VoxelShape(new AABB(box.minX, floorY - 1, box.minZ, box.maxX, floorY, box.maxZ)));
        }
        for (AABB cube : stoneIn(box)) {
            shapes.add(new VoxelShape(cube));
        }
        return shapes;
    }

    public List<AABB> stoneIn(AABB box) {
        List<AABB> result = new ArrayList<>();
        for (Map.Entry<BlockPos, BlockState> block : blocks.entrySet()) {
            if (block.getValue().getBlock() != Blocks.STONE) {
                continue;
            }
            BlockPos pos = block.getKey();
            AABB cube = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            if (cube.intersects(box)) {
                result.add(cube);
            }
        }
        return result;
    }

    /** Lab only: the top of the floor, or of the stone under {@code box} whose top is no higher than {@code maxY}. */
    public double groundUnder(AABB box, double maxY) {
        double ground = floorY;
        for (AABB cube : stoneIn(new AABB(box.minX, floorY, box.minZ, box.maxX, maxY, box.maxZ))) {
            if (cube.maxY <= maxY) {
                ground = Math.max(ground, cube.maxY);
            }
        }
        return ground;
    }

    /**
     * The first solid face the ray crosses (the stone cubes and the top of the floor), or a miss
     * at its end. Every solid block here is a full cube, and water is never solid to it, whatever
     * the context's fluid says.
     */
    public BlockHitResult clip(ClipContext context) {
        Vec3 start = context.getFrom(), end = context.getTo();
        double dx = end.x - start.x, dy = end.y - start.y, dz = end.z - start.z;
        double best = Double.POSITIVE_INFINITY;
        Direction side = null;
        BlockPos hitBlock = null;
        // The floor: everything below floorY is solid, so a ray going down meets its top.
        if (dy < 0 && start.y >= floorY && end.y < floorY) {
            best = (floorY - start.y) / dy;
            side = Direction.UP;
            hitBlock = BlockPos.containing(start.x + dx * best, floorY - 1, start.z + dz * best);
        }
        // A hair larger, so a ray along a face still finds the cube.
        AABB around = new AABB(Math.min(start.x, end.x) - 1e-7, Math.min(start.y, end.y) - 1e-7, Math.min(start.z, end.z) - 1e-7,
                Math.max(start.x, end.x) + 1e-7, Math.max(start.y, end.y) + 1e-7, Math.max(start.z, end.z) + 1e-7);
        for (AABB cube : stoneIn(around)) {
            // Slabs: where the ray enters the cube, and through which face.
            double enter = 0, leave = 1;
            Direction face = null;
            double[] from = {start.x, start.y, start.z}, along = {dx, dy, dz};
            double[] min = {cube.minX, cube.minY, cube.minZ}, max = {cube.maxX, cube.maxY, cube.maxZ};
            Direction[][] faces = {{Direction.WEST, Direction.EAST}, {Direction.DOWN, Direction.UP}, {Direction.NORTH, Direction.SOUTH}};
            boolean misses = false;
            for (int i = 0; i < 3 && !misses; i++) {
                if (Math.abs(along[i]) < 1e-12) {
                    misses = from[i] < min[i] || from[i] > max[i];
                    continue;
                }
                double t0 = (min[i] - from[i]) / along[i], t1 = (max[i] - from[i]) / along[i];
                Direction near = along[i] > 0 ? faces[i][0] : faces[i][1];
                if (t0 > t1) {
                    double t = t0;
                    t0 = t1;
                    t1 = t;
                }
                if (t0 > enter) {
                    enter = t0;
                    face = near;
                }
                leave = Math.min(leave, t1);
                misses = enter > leave;
            }
            if (!misses && face != null && enter < best) {
                best = enter;
                side = face;
                hitBlock = new BlockPos((int) cube.minX, (int) cube.minY, (int) cube.minZ);
            }
        }
        if (side == null) {
            return BlockHitResult.miss(end, Direction.UP, BlockPos.containing(end.x, end.y, end.z));
        }
        return new BlockHitResult(new Vec3(start.x + dx * best, start.y + dy * best, start.z + dz * best), side, hitBlock, false);
    }
}
