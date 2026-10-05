package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.lab.sim.LabWorlds;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** {@code core:ledge_ahead}: a block in front of the feet the entity can climb onto. */
class LedgeAheadTest
{

    /** A golem (1.4 wide) facing +Z, pressed against whatever starts at z = 3. */
    private static EntityIronGolem golem(World world)
    {
        EntityIronGolem golem = new EntityIronGolem(world);
        golem.setLocationAndAngles(0.5D, 0, 2.3D, 0, 0);
        golem.prevPosX = golem.posX;
        golem.prevPosZ = golem.posZ;
        return golem;
    }

    @Test
    void aBlockInFrontIsALedge()
    {
        World world = new World();
        LabWorlds.placeStone(world, -1, 0, 3, 1, 0, 3);
        assertTrue(MinecraftKumoOperations.ledgeAhead(golem(world)));
    }

    @Test
    void nothingInFrontIsNoLedge()
    {
        assertFalse(MinecraftKumoOperations.ledgeAhead(golem(new World())));
    }

    @Test
    void aWallTooHighToJumpIsNoLedge()
    {
        World world = new World();
        LabWorlds.placeStone(world, -1, 0, 3, 1, 1, 3);
        assertFalse(MinecraftKumoOperations.ledgeAhead(golem(world)));
    }

    @Test
    void aLedgeWithNoRoomOnTopIsNoLedge()
    {
        World world = new World();
        LabWorlds.placeStone(world, -1, 0, 3, 1, 0, 3);
        LabWorlds.placeStone(world, -1, 3, 3, 1, 3, 4);
        assertFalse(MinecraftKumoOperations.ledgeAhead(golem(world)));
    }

    @Test
    void aBlockBehindIsNoLedge()
    {
        World world = new World();
        LabWorlds.placeStone(world, -1, 0, 0, 1, 0, 0);
        EntityIronGolem golem = golem(world);
        golem.setLocationAndAngles(0.5D, 0, 1.8D, 0, 0);
        golem.prevPosX = golem.posX;
        golem.prevPosZ = golem.posZ;
        assertFalse(MinecraftKumoOperations.ledgeAhead(golem));
    }

    @Test
    void frontIsTheWayItMoves()
    {
        World world = new World();
        LabWorlds.placeStone(world, -1, 0, 3, 1, 0, 3);
        EntityIronGolem golem = golem(world);
        // Facing away, but moving towards the block.
        golem.renderYawOffset = 180;
        golem.prevPosZ = golem.posZ - 0.1D;
        assertTrue(MinecraftKumoOperations.ledgeAhead(golem));
    }

}
