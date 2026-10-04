package goblinbob.mobends.lab;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import goblinbob.mobends.core.data.OrientationComponent;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import goblinbob.mobends.standard.data.CapeWave;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A model definition's {@code components}: what the entity's data carries besides its bones, found
 * by name by the layers and drivers that use it, whatever the data's class.
 */
public class DefinedComponentsTest
{

    private static EntityModelDefinition player() throws IOException, MalformedKumoTemplateException
    {
        return ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/player.json"));
    }

    @Test
    void thePlayersDefinitionGivesItsDataTheComponentsTheLayersRead() throws Exception
    {
        // The mod's default addon registers the components (here LabBootstrap).
        LabBootstrap.ensure();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        AbstractClientPlayer player = new AbstractClientPlayer(world);
        DefinedEntityData<AbstractClientPlayer> data = DefinedEntityData.create(player(), player);

        assertNotNull(data.getComponent("swordTrail", SwordTrail.class));
        assertNotNull(data.getComponent("capeWave", CapeWave.class));
        assertNull(data.getComponent("swordTrail", CapeWave.class), "a component of another type is not found");
        // The held items' orientations are bones too, which the animator poses.
        assertTrue(data.getPartForName("rightHeldItem") instanceof OrientationComponent);
        assertTrue(data.getPartForName("leftHeldItem") instanceof OrientationComponent);
        assertNotNull(data.getBone("rightHeldItem"));
        assertNotNull(data.getPart("cape"), "the cape hangs from a bone of its own");

        // The biped's animators are tuned for its offset and turn smoothed at twice the usual rate.
        assertEquals(2, data.globalOffset.smoothness.x, 0);

        // Components move on with the parts.
        CapeWave wave = data.getComponent("capeWave", CapeWave.class);
        data.updateParts(1);
        data.updateParts(1);
        assertEquals(2, wave.getPhase(), 1e-6);
    }

    @Test
    void anUnknownComponentIsLeftOut() throws Exception
    {
        JsonObject json = new JsonParser().parse(new String(Files.readAllBytes(LabPaths.root()
                .resolve("../src/main/resources/assets/mobends/bends/models/player.json")), StandardCharsets.UTF_8)).getAsJsonObject();
        json.getAsJsonObject("components").addProperty("wings", "nomod:wings");
        EntityModelDefinition copy = ModelDefinitions.parse(json);
        LabBootstrap.ensure();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        DefinedEntityData<AbstractClientPlayer> data = DefinedEntityData.create(copy, new AbstractClientPlayer(world));
        assertNull(data.getPartForName("wings"));
    }

    @Test
    void aComponentCannotTakeABonesName()
    {
        String json = "{\"formatVersion\": 2, \"entity\": \"x.Y\", \"animator\": \"a:b.json\","
                + " \"bones\": [{\"name\": \"head\"}], \"components\": {\"head\": \"core:orientation\"}}";
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class,
                () -> ModelDefinitions.parse(new JsonParser().parse(json)));
        assertTrue(e.getMessage().contains("'head'"), e.getMessage());
    }

}
