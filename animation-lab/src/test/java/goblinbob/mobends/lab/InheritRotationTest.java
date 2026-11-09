package goblinbob.mobends.lab;

import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A bone that doesn't inherit its parent's rotation (a biped's head): it sits where its parent puts
 * it, but turns relative to the model, so its own rotation is all it has, and the bones inside it
 * turn with it as usual. How the drawn part turns so is the renderer's ({@code DefinedModelPart}),
 * which the lab doesn't compile; here, which bones the definitions say so of.
 */
public class InheritRotationTest
{
    @Test
    void onlyABoneWithAParentCanLeaveOutItsRotation()
    {
        BoneDefinition bone = new BoneDefinition();
        bone.name = "head";
        bone.inheritRotation = false;
        assertThrows(MalformedKumoTemplateException.class, bone::validate);
        bone.parent = "body";
        assertDoesNotThrow(bone::validate);
    }

    @Test
    void theBipedsHeadsLookWhereTheyLookHoweverTheBodyBends() throws Exception
    {
        LabBootstrap.ensure();
        ClientLevel world = new ClientLevel();
        Minecraft.getInstance().level = world;
        Minecraft.getInstance().player = new LocalPlayer(world);
        for (String name : new String[] { "player", "zombie", "zombie_villager", "pig_zombie", "skeleton" })
        {
            EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(ResourceLocation.fromNamespaceAndPath("mobends", "bends/models/" + name + ".json"));
            LivingEntity entity = DefinedModelsTest.entityOf(definition, world);
            DefinedEntityData<LivingEntity> data = EntityKind.newData(definition, entity);
            assertFalse(data.getPart("head").inheritRotation, name + ": the head turns with the body");
            assertTrue(data.getPart("headwear").inheritRotation, name + ": the headwear doesn't turn with the head");
            assertTrue(data.getPart("rightArm").inheritRotation, name + ": the arm doesn't turn with the body");
        }
    }
}
