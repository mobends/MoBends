package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.BlockBelowDriver;
import goblinbob.mobends.core.kumo.BlockGradientDriver;
import goblinbob.mobends.lab.sim.LabWorlds;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code core:block_below} and {@code core:block_gradient}: the block under the feet, and which way
 * the ground rises round it. The lab's floor is solid below y 0.
 */
class GroundDriversTest
{

    private static void assertBlock(int x, int y, int z, BlockPos actual)
    {
        assertTrue(actual != null, "a block");
        assertEquals(x + " " + y + " " + z, actual.getX() + " " + actual.getY() + " " + actual.getZ());
    }

    @Test
    void standingOnTheFloorItFindsTheBlockUnderTheFeet()
    {
        assertBlock(0, -1, 2, BlockBelowDriver.find(new World(), 0.5, 0, 2.3, 0, true));
    }

    @Test
    void inTheAirItLooksDownAsFarAsItIsTold()
    {
        World world = new World();
        assertBlock(0, -1, 2, BlockBelowDriver.find(world, 0.5, 0.4, 2.3, 0.5, true));
        assertNull(BlockBelowDriver.find(world, 0.5, 0.6, 2.3, 0.5, true));
        assertBlock(0, -1, 2, BlockBelowDriver.find(world, 0.5, 2.9, 2.3, 3, true));
    }

    @Test
    void onlySolidBlocksCountWhenItIsAskedForThem()
    {
        World world = new World();
        world.setBlockState(new BlockPos(0, 0, 2), Blocks.WATER.getDefaultState());
        assertBlock(0, 0, 2, BlockBelowDriver.find(world, 0.5, 1, 2.3, 1, false));
        assertBlock(0, -1, 2, BlockBelowDriver.find(world, 0.5, 1, 2.3, 1, true));
    }

    private static double[] gradient(World world, boolean solid)
    {
        double[] result = new double[6];
        BlockGradientDriver.compute(world, new BlockPos(0, -1, 2), solid, result);
        return result;
    }

    @Test
    void flatGroundHasNoWayUpAndFacesUp()
    {
        double[] g = gradient(new World(), true);
        assertEquals(0, Math.hypot(g[0], Math.hypot(g[1], g[2])), 1e-9, "no way up");
        assertEquals(1, g[4], 1e-9, "the normal straight up");
    }

    @Test
    void besideAStepUpTheWayUpPointsAtTheStep()
    {
        World world = new World();
        LabWorlds.placeStone(world, -2, 0, 3, 2, 0, 4);
        double[] g = gradient(world, true);
        assertEquals(0, g[0], 1e-9, "not to the side");
        assertTrue(g[2] > 0.1, "towards the step: " + g[2]);
        assertTrue(g[1] > 0, "and up: " + g[1]);
        assertEquals(1, Math.sqrt(g[3] * g[3] + g[4] * g[4] + g[5] * g[5]), 1e-9, "a unit normal");
        assertTrue(g[4] > 0 && g[5] < 0, "the normal leans away from the step");
        // The way up is along the surface: across the normal.
        assertEquals(0, g[0] * g[3] + g[1] * g[4] + g[2] * g[5], 1e-9);
        // Its length is the sine of the slope.
        assertEquals(Math.sqrt(1 - g[4] * g[4]), Math.sqrt(g[0] * g[0] + g[1] * g[1] + g[2] * g[2]), 1e-9);
    }

    @Test
    void waterIsNoStepWhenOnlySolidBlocksCount()
    {
        World world = new World();
        for (int x = -1; x <= 1; x++) world.setBlockState(new BlockPos(x, 0, 3), Blocks.WATER.getDefaultState());
        assertEquals(0, gradient(world, true)[2], 1e-9, "flat, under water");
        assertTrue(gradient(world, false)[2] > 0.1, "a step, when any block counts");
    }

    @Test
    void withNothingRoundItEverythingIsZero()
    {
        World world = new World();
        world.floorY = -100;
        double[] g = gradient(world, true);
        for (double v : g) assertEquals(0, v, 1e-9);
    }

}
