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
    private EntityLivingBase trophy;

    @BeforeEach
    void setUp()
    {
        LabBootstrap.ensure();
        DataUpdateHandler.reset();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
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

}
