package goblinbob.mobends.lab;

import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.EntityBuiltIns;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Every built-in declared for animators is one the entity data provides, of its declared type. */
class EntityBuiltInsTest
{

    @Test
    void everyEntityProvidesEveryBuiltIn()
    {
        LabBootstrap.ensure();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        for (EntityKind kind : EntityKind.values())
        {
            LivingEntityData<?> data = kind.createData(kind.createEntity(world), 0);
            for (String name : EntityBuiltIns.NUMBERS)
            {
                assertTrue(data.indexOfVariable(name) >= 0, kind + " has no number '" + name + "'");
            }
            for (String name : EntityBuiltIns.DOUBLES)
            {
                assertTrue(data.indexOfVariable(name) >= 0, kind + " has no double '" + name + "'");
            }
            for (String name : EntityBuiltIns.BOOLEANS)
            {
                assertTrue(data.indexOfState(name) >= 0, kind + " has no boolean '" + name + "'");
            }
        }
    }

}
