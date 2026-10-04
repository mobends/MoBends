package goblinbob.mobends.lab;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A defined entity's bones sit where its renderer's vanilla model has the parts: read again when
 * another renderer draws it (a player's slim-armed renderer, once the skin is known).
 */
public class DefinedPositionsTest
{

    private static EntityModelDefinition definition(String extra) throws MalformedKumoTemplateException
    {
        return ModelDefinitions.parse(new JsonParser().parse("{\"formatVersion\": 2, \"entity\": \"x.Y\", \"animator\": \"a:b.json\","
                + " \"bones\": [{\"name\": \"body\"}, {\"name\": \"arm\", \"parent\": \"body\"}]" + extra + "}"));
    }

    @Test
    void positionsAreReadAgainFromAnotherRenderer() throws Exception
    {
        LabBootstrap.ensure();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        DefinedEntityData<EntityZombie> data = DefinedEntityData.create(definition(""), new EntityZombie(world));
        Object wide = new Object(), slim = new Object();
        Map<String, float[]> widePositions = Collections.singletonMap("arm", new float[] { -5, -10, 0 });
        Map<String, float[]> slimPositions = Collections.singletonMap("arm", new float[] { -5, -9.5F, 0 });

        assertFalse(data.hasAdoptedPositionsOf(wide));
        data.adoptPositions(wide, widePositions);
        assertTrue(data.hasAdoptedPositionsOf(wide));
        assertEquals(-10, data.getPart("arm").position.y, 1e-6);

        assertFalse(data.hasAdoptedPositionsOf(slim), "another renderer's model has positions of its own");
        data.adoptPositions(slim, slimPositions);
        assertEquals(-9.5, data.getPart("arm").position.y, 1e-6);
        assertFalse(data.hasAdoptedPositionsOf(wide));
    }

    @Test
    void theFirstPersonRestNamesBones() throws Exception
    {
        assertEquals(Collections.singletonList("arm"), definition(", \"renderer\": {\"firstPersonRest\": [\"arm\"]}").renderer.firstPersonRest);
        assertTrue(definition("").renderer.firstPersonRest.isEmpty());
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> definition(", \"renderer\": {\"firstPersonRest\": [\"leg\"]}"));
        assertTrue(e.getMessage().contains("'leg'"), e.getMessage());
    }

    @Test
    void theSneakOffsetWhileFlyingFallsBackToTheSneakOffset() throws Exception
    {
        EntityModelDefinition.RendererSettings settings = definition(", \"renderer\": {\"sneakOffset\": 5}").renderer;
        assertEquals(5, settings.sneakOffset, 0);
        assertNull(settings.flyingSneakOffset);
        assertEquals(0, definition("").renderer.sneakOffset, 0);
    }

}
