package goblinbob.mobends.lab;

import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.lab.sim.LabWorlds;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.monster.Zombie;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code core:ledge_ahead}: a block in front of the feet, at most 1.5 blocks high, with room on top.
 * A zombie's feet at (0.5, 0, 0.5) on the lab's floor (solid below y 0), its body facing +Z.
 */
class LedgeAheadTest
{

    private static boolean ledgeAhead(ClientLevel world, float bodyYaw)
    {
        Zombie zombie = new Zombie(world);
        zombie.setPos(0.5, 0, 0.5);
        zombie.yBodyRot = bodyYaw;
        return MinecraftKumoOperations.ledgeAhead(zombie);
    }

    @Test
    void aBlockAheadIsALedge()
    {
        ClientLevel world = new ClientLevel();
        LabWorlds.placeStone(world, 0, 0, 1, 0, 0, 1);
        assertTrue(ledgeAhead(world, 0));
    }

    @Test
    void theFloorIsNoLedge()
    {
        assertFalse(ledgeAhead(new ClientLevel(), 0));
    }

    @Test
    void aWallTooHighIsNoLedge()
    {
        ClientLevel world = new ClientLevel();
        LabWorlds.placeStone(world, 0, 0, 1, 0, 1, 1);
        assertFalse(ledgeAhead(world, 0));
    }

    @Test
    void aLedgeWithNoRoomOnTopIsNone()
    {
        ClientLevel world = new ClientLevel();
        LabWorlds.placeStone(world, 0, 0, 1, 0, 0, 1);
        LabWorlds.placeStone(world, 0, 2, 1, 0, 2, 1);
        assertFalse(ledgeAhead(world, 0));
    }

    @Test
    void aBlockBehindIsNoLedge()
    {
        ClientLevel world = new ClientLevel();
        LabWorlds.placeStone(world, 0, 0, -1, 0, 0, -1);
        assertFalse(ledgeAhead(world, 0));
        // Turned round (yaw 180 faces -Z), it is ahead.
        assertTrue(ledgeAhead(world, 180));
    }
}
