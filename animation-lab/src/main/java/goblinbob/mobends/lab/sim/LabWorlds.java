package goblinbob.mobends.lab.sim;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;

/** Block placement helpers for scenarios (ladders to climb, water to swim in, stone to climb onto). */
public class LabWorlds
{
    /** Adds {@code entity} to {@code level}, as the game does when it spawns: the entity database only keeps the data of entities in it. */
    public static <E extends Entity> E add(ClientLevel level, E entity)
    {
        level.addEntity(entity);
        return entity;
    }

    public static void placeLadderColumn(Level level, Entity entity)
    {
        placeLadderColumn(level, entity, 40);
    }

    /** A ladder of {@code height} blocks from the floor up (a short one exposes the ledge pull-up). */
    public static void placeLadderColumn(Level level, Entity entity, int height)
    {
        int x = (int) Math.floor(entity.getX());
        int z = (int) Math.floor(entity.getZ());
        for (int y = (int) Math.floor(level.floorY); y < level.floorY + height; y++)
        {
            level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.NORTH));
        }
    }

    /** Fills the box from {@code (x0, y0, z0)} to {@code (x1, y1, z1)}, inclusive, with stone: solid ground to climb onto. */
    public static void placeStone(Level level, int x0, int y0, int z0, int x1, int y1, int z1)
    {
        for (int x = x0; x <= x1; x++)
            for (int y = y0; y <= y1; y++)
                for (int z = z0; z <= z1; z++)
                    level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState());
    }

    /** Fills a 3x3 column of still water around the entity from the floor upwards. */
    public static void placeWaterColumn(Level level, Entity entity, int height)
    {
        int cx = (int) Math.floor(entity.getX());
        int cz = (int) Math.floor(entity.getZ());
        for (int x = cx - 1; x <= cx + 1; x++)
            for (int z = cz - 1; z <= cz + 1; z++)
                for (int y = (int) Math.floor(level.floorY); y < level.floorY + height; y++)
                    level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.WATER.defaultBlockState());
    }
}
