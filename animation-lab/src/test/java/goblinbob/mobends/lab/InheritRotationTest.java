package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A bone that doesn't inherit its parent's rotation (a biped's head): it sits where its parent puts
 * it, but turns relative to the model, so its own rotation is all it has, and the bones inside it
 * turn with it as usual.
 */
public class InheritRotationTest
{
    private static Quaternion axisAngle(float x, float y, float z, float degrees)
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(x, y, z, (float) Math.toRadians(degrees));
        return q;
    }

    private static void assertSameRotation(Quaternion expected, Quaternion actual)
    {
        double dot = Math.abs(expected.x * actual.x + expected.y * actual.y + expected.z * actual.z + expected.w * actual.w);
        assertEquals(1, dot, 1e-5, "rotations differ: expected " + expected.x + ", " + expected.y + ", " + expected.z + ", " + expected.w
                + ", got " + actual.x + ", " + actual.y + ", " + actual.z + ", " + actual.w);
    }

    @Test
    void aBoneThatDoesntTurnWithItsParentTurnsOnlyByItself()
    {
        ModelPartTransform body = new ModelPartTransform();
        ModelPartTransform head = new ModelPartTransform(body);
        ModelPartTransform hat = new ModelPartTransform(head);
        ModelPartTransform arm = new ModelPartTransform(body);
        head.inheritRotation = false;

        Quaternion bodyTurn = axisAngle(1, 0, 0, 30);
        Quaternion headTurn = axisAngle(0, 1, 0, 45);
        Quaternion armTurn = axisAngle(0, 0, 1, 20);
        body.rotation.snapTo(bodyTurn);
        head.rotation.snapTo(headTurn);
        arm.rotation.snapTo(armTurn);

        Quaternion result = new Quaternion();
        head.getModelRotation(result);
        assertSameRotation(headTurn, result);
        // Inside the head, a bone turns with it, and stops where the head does.
        hat.getModelRotation(result);
        assertSameRotation(headTurn, result);
        // Any other bone still turns with its parent.
        arm.getModelRotation(result);
        assertSameRotation(Quaternion.mul(bodyTurn, armTurn, new Quaternion()), result);
    }

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
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        for (String name : new String[] { "player", "zombie", "zombie_villager", "pig_zombie", "skeleton" })
        {
            EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/" + name + ".json"));
            EntityLivingBase entity = DefinedModelsTest.entityOf(definition, world);
            DefinedEntityData<EntityLivingBase> data = DefinedEntityData.create(definition, entity);
            assertFalse(data.getPart("head").inheritsRotation(), name + ": the head turns with the body");
            assertTrue(data.getPart("headwear").inheritsRotation(), name + ": the headwear doesn't turn with the head");
            assertTrue(data.getPart("rightArm").inheritsRotation(), name + ": the arm doesn't turn with the body");
        }
    }
}
