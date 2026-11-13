package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.World;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Issues #342 and #244: other mods draw entities that aren't in the world (Just Enough Resources'
 * mob list, OpenBlocks' trophies). Their data must last while they are drawn: made anew every
 * tick, they would start from their first pose every tick, and shake.
 */
public class DetachedEntityTest
{

    private static final int FRAMES_PER_TICK = 3;

    private final EntityDatabase database = new EntityDatabase();
    @SuppressWarnings("unchecked")
    private final IEntityDataFactory<EntityLivingBase> factory = entity -> (LivingEntityData<EntityLivingBase>) EntityKind.ZOMBIE.createData(entity, 1);
    private World world;
    private EntityLivingBase trophy;

    @BeforeEach
    void setUp()
    {
        LabBootstrap.ensure();
        DataUpdateHandler.reset();
        world = new World();
        Minecraft.getMinecraft().theWorld = world;
        Minecraft.getMinecraft().thePlayer = new EntityPlayerSP(world);
        // Made in a world of its own, as a mod that shows a mob makes it: never in the game's.
        trophy = EntityKind.ZOMBIE.createEntity(new World());
    }

    @AfterEach
    void tearDown()
    {
        DataUpdateHandler.reset();
    }

    /** A client tick, then its frames, drawing the trophy in each if {@code drawn}; its data in the last. */
    private LivingEntityData<EntityLivingBase> tick(boolean drawn)
    {
        database.updateClient();
        LivingEntityData<EntityLivingBase> data = null;
        for (int frame = 0; frame < FRAMES_PER_TICK; frame++)
        {
            DataUpdateHandler.frame++;
            database.updateRender(0);
            if (drawn)
            {
                data = database.getOrMake(factory, trophy);
                data.animate();
            }
        }
        return data;
    }

    @Test
    void anEntityOutsideTheWorldKeepsItsDataWhileItIsDrawn()
    {
        LivingEntityData<EntityLivingBase> first = tick(true);
        for (int i = 0; i < 40; i++)
        {
            assertSame(first, tick(true), "the data was made anew on tick " + (i + 1));
        }
    }

    @Test
    void anEntityOutsideTheWorldLosesItsDataOnceNoLongerDrawn()
    {
        tick(true);
        for (int i = 0; i < 25; i++)
        {
            tick(false);
        }
        assertNull(database.get(trophy));
    }

    @Test
    void anEntityOutsideTheWorldKeepsItsDataBetweenDrawsAtALowFrameRate()
    {
        LivingEntityData<EntityLivingBase> first = tick(true);
        // Ten ticks to a frame, as when the game lags.
        for (int i = 0; i < 9; i++)
        {
            database.updateClient();
        }
        assertNotNull(database.get(trophy));
        assertSame(first, tick(true));
    }

    @Test
    void anEntityOutsideTheWorldLosesItsDataWhileTheGameIsPaused()
    {
        tick(true);
        // The GUI that drew it closed, and the game paused: the entities don't update, but the ticks still count.
        for (int i = 0; i < 25; i++)
        {
            database.tickPaused();
        }
        assertNull(database.get(trophy));
    }

    @Test
    void anEntityOutsideTheWorldWithTheIdOfOneInItKeepsItsOwnData()
    {
        // On a server, the world's entities get their ids from the server, the trophy from the client.
        EntityLivingBase mob = EntityKind.ZOMBIE.createEntity(world);
        mob.setEntityId(trophy.getEntityId());
        world.addEntity(mob);

        database.updateClient();
        DataUpdateHandler.frame++;
        LivingEntityData<EntityLivingBase> mobData = database.getOrMake(factory, mob);
        LivingEntityData<EntityLivingBase> trophyData = database.getOrMake(factory, trophy);
        assertNotSame(mobData, trophyData);
        assertSame(mob, mobData.getEntity());
        assertSame(trophy, trophyData.getEntity());

        for (int i = 0; i < 40; i++)
        {
            database.updateClient();
            DataUpdateHandler.frame++;
            assertSame(mobData, database.getOrMake(factory, mob), "the mob's data was made anew on tick " + (i + 1));
            assertSame(trophyData, database.getOrMake(factory, trophy), "the trophy's data was made anew on tick " + (i + 1));
            assertSame(mobData, database.get(mob));
            assertSame(trophyData, database.get(trophy));
        }
    }

    @Test
    void anEntityThatLeavesTheWorldLosesItsDataOnTheNextTick()
    {
        World other = new World();
        Minecraft.getMinecraft().theWorld = other;
        EntityLivingBase mob = EntityKind.ZOMBIE.createEntity(other);
        other.addEntity(mob);
        database.updateClient();
        DataUpdateHandler.frame++;
        database.getOrMake(factory, mob).animate();

        // The player went to another dimension: the mob's world is gone.
        Minecraft.getMinecraft().theWorld = world;
        database.updateClient();
        assertNull(database.get(mob));
    }

}
