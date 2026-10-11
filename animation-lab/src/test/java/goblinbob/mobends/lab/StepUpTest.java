package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.StepUpDriver;
import goblinbob.mobends.core.kumo.StepUpDriver.Result;
import goblinbob.mobends.lab.sim.LabWorlds;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@code core:step_up}: the step up ahead, one block high with room on it. A golem's feet at
 * (0.5, 0, 0.5) on the lab's floor (solid below y 0), 2.7 blocks tall.
 */
class StepUpTest
{

    private static final double HEIGHT = 2.7;

    private static Result search(World world, double dx, double dz, int[] step)
    {
        return StepUpDriver.search(world, 0.5, 0, 0.5, dx, dz, 3, HEIGHT, true, step);
    }

    @Test
    void aStepAheadIsFound()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 2, 2, 0, 3);
        int[] step = new int[5];
        assertEquals(Result.STEP, search(world, 0, 1, step));
        assertArrayEquals(new int[] {0, 0, 2, 0, 1}, step);
    }

    @Test
    void walkingAtAnAngleTheFaceIsTheOneItGoesThrough()
    {
        World world = new World();
        LabWorlds.placeStone(world, -5, 0, 2, 5, 0, 3);
        int[] step = new int[5];
        // 30° off +Z, towards +X: it crosses into column 1 on the way, then into the ledge's row.
        assertEquals(Result.STEP, search(world, 0.5, 0.866, step));
        assertEquals(2, step[2]);
        assertEquals(0, step[3], "not the side");
        assertEquals(1, step[4], "into the ledge's face");
    }

    @Test
    void aStepTooFarIsNotFound()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 4, 2, 0, 5);
        assertEquals(Result.NONE, search(world, 0, 1, new int[5]));
    }

    @Test
    void walkingAwayItFindsNone()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 2, 2, 0, 3);
        assertEquals(Result.NONE, search(world, 0, -1, new int[5]));
    }

    @Test
    void aWallTwoHighIsNoStep()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 2, 2, 1, 3);
        assertEquals(Result.BLOCKED, search(world, 0, 1, new int[5]));
    }

    @Test
    void aStepWithoutRoomOnItIsNoStep()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 2, 2, 0, 3);
        // A ceiling over the step, 2 blocks above it: under the golem's 2.7.
        LabWorlds.placeStone(world, -2, 3, 2, 2, 3, 3);
        assertEquals(Result.BLOCKED, search(world, 0, 1, new int[5]));
    }

    @Test
    void noRoomAboveItsHeadToRiseIsNoStep()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 2, 2, 0, 3);
        LabWorlds.placeStone(world, 0, 3, 0, 0, 3, 0);
        assertEquals(Result.BLOCKED, search(world, 0, 1, new int[5]));
    }

    @Test
    void aDropBeforeTheStepEndsTheSearch()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 3, 2, 0, 3);
        // A hole in the floor in row 1, between it and the step.
        world.setBlockState(new BlockPos(0, -1, 1), Blocks.AIR.getDefaultState());
        assertEquals(Result.NONE, search(world, 0, 1, new int[5]));
    }

    @Test
    void waterIsNoStep()
    {
        World world = new World();
        for (int x = -1; x <= 1; x++) world.setBlockState(new BlockPos(x, 0, 2), Blocks.WATER.getDefaultState());
        assertEquals(Result.NONE, search(world, 0, 1, new int[5]));
    }

}
