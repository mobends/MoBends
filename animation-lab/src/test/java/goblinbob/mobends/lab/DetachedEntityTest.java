package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.data.IEntityDataFactory;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * Issues #342 and #244: other mods draw entities that aren't in the world (Just Enough Resources'
 * mob list, OpenBlocks' trophies). In 1.12 their data lasted while they were drawn; now they get
 * none and are drawn as vanilla (nothing would ever drop their data, and their id can be another
 * entity's). Only the menu's previews, which the mod keeps itself, are animated outside the world.
 */
public class DetachedEntityTest
{

    private static final int FRAMES_PER_TICK = 3;

    private final EntityDatabase database = new EntityDatabase();
    @SuppressWarnings("unchecked")
    private final IEntityDataFactory<LivingEntity> factory = entity -> (LivingEntityData<LivingEntity>) EntityKind.ZOMBIE.createData(entity, 1);
    private ClientLevel world;
    private LivingEntity trophy;

    @BeforeEach
    void setUp()
    {
        LabBootstrap.ensure();
        DataUpdateHandler.reset();
        world = new ClientLevel();
        Minecraft.getInstance().level = world;
        Minecraft.getInstance().player = new LocalPlayer(world);
        // Made in a world of its own, as a mod that shows a mob makes it: never in the game's.
        trophy = EntityKind.ZOMBIE.createEntity(new ClientLevel());
    }

    @AfterEach
    void tearDown()
    {
        DataUpdateHandler.reset();
    }

    @Test
    void anEntityOutsideTheWorldGetsNoData()
    {
        for (int i = 0; i < 10; i++)
        {
            database.updateClient();
            for (int frame = 0; frame < FRAMES_PER_TICK; frame++)
            {
                DataUpdateHandler.frame++;
                database.updateRender(0);
                assertNull(database.getOrMake(factory, trophy), "the trophy got data on tick " + (i + 1));
            }
        }
        assertNull(database.get(trophy));
    }

    @Test
    void anEntityOutsideTheWorldWithTheIdOfOneInItLeavesItsData()
    {
        // On a server, the world's entities get their ids from the server, the trophy from the client.
        LivingEntity mob = EntityKind.ZOMBIE.createEntity(world);
        mob.setId(trophy.getId());

        database.updateClient();
        DataUpdateHandler.frame++;
        LivingEntityData<LivingEntity> mobData = database.getOrMake(factory, mob);
        assertNotNull(mobData);
        assertSame(mob, mobData.getEntity());
        assertNull(database.getOrMake(factory, trophy));

        for (int i = 0; i < 40; i++)
        {
            database.updateClient();
            DataUpdateHandler.frame++;
            assertSame(mobData, database.getOrMake(factory, mob), "the mob's data was made anew on tick " + (i + 1));
            assertNull(database.getOrMake(factory, trophy));
            assertSame(mobData, database.get(mob));
            assertNull(database.get(trophy));
        }
    }

    @Test
    void anEntityThatLeavesTheWorldLosesItsDataOnTheNextTick()
    {
        ClientLevel other = new ClientLevel();
        Minecraft.getInstance().level = other;
        LivingEntity mob = EntityKind.ZOMBIE.createEntity(other);
        database.updateClient();
        DataUpdateHandler.frame++;
        database.getOrMake(factory, mob).animate();
        assertNotNull(database.get(mob));

        // The player went to another dimension: the mob's world is gone.
        Minecraft.getInstance().level = world;
        database.updateClient();
        assertNull(database.get(mob));
    }

}
