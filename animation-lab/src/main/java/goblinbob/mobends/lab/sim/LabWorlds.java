package goblinbob.mobends.lab.sim;

import net.minecraft.block.BlockLadder;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Block placement helpers for scenarios (ladders to climb, water to swim in, stone to climb onto). */
public class LabWorlds
{
    public static void placeLadderColumn(World world, Entity entity)
    {
        placeLadderColumn(world, entity, 40);
    }

    /** A ladder of {@code height} blocks from the floor up (a short one exposes the ledge pull-up). */
    public static void placeLadderColumn(World world, Entity entity, int height)
    {
        int x = (int) Math.floor(entity.posX);
        int z = (int) Math.floor(entity.posZ);
        for (int y = (int) Math.floor(world.floorY); y < world.floorY + height; y++)
        {
            world.setBlockState(new BlockPos(x, y, z), Blocks.LADDER.getDefaultState().withProperty(BlockLadder.FACING, EnumFacing.NORTH));
        }
    }

    /** Fills the box from {@code (x0, y0, z0)} to {@code (x1, y1, z1)}, inclusive, with stone: solid ground to climb onto. */
    public static void placeStone(World world, int x0, int y0, int z0, int x1, int y1, int z1)
    {
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++)
                    world.setBlockState(new BlockPos(x, y, z), Blocks.STONE.getDefaultState());
    }

    /** Fills a 3x3 column of still water around the entity from the floor upwards. */
    public static void placeWaterColumn(World world, Entity entity, int height)
    {
        int cx = (int) Math.floor(entity.posX);
        int cz = (int) Math.floor(entity.posZ);
        for (int x = cx - 1; x <= cx + 1; x++)
            for (int z = cz - 1; z <= cz + 1; z++)
                for (int y = (int) Math.floor(world.floorY); y < world.floorY + height; y++)
                    world.setBlockState(new BlockPos(x, y, z), Blocks.WATER.getDefaultState());
    }
}
