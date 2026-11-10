package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.definition.ModelPartLookup;
import goblinbob.mobends.core.definition.BoneDefinition;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.sim.LabBootstrap;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Model definitions for any model, a mod's included: the model named by its class
 * ({@link EntityModelDefinition#modelClass()}) and the bones' parts ({@link ModelPartLookup}) by
 * path, by a vanilla model's 1.12 field, or by a mod's model's own field.
 */
class ModPartLookupTest
{

    private final ModelPart leftArm = new ModelPart(Map.of());
    private final ModelPart body = new ModelPart(Map.of("left_arm", leftArm));
    private final ModelPart root = new ModelPart(Map.of("body", body, "left_arm", leftArm));

    /** A mod's model, as it keeps its parts: in fields of its own names. */
    static class ModMobModel
    {
        private final ModelPart torso;
        final ModelPart[] tentacles;

        ModMobModel(ModelPart torso, ModelPart... tentacles)
        {
            this.torso = torso;
            this.tentacles = tentacles;
        }
    }

    /** A mod's model of another, which declares the field. */
    static class BiggerModMobModel extends ModMobModel
    {
        BiggerModMobModel(ModelPart torso)
        {
            super(torso);
        }
    }

    public ModPartLookupTest()
    {
        LabBootstrap.ensure();
    }

    private static EntityModelDefinition definition(String model)
    {
        EntityModelDefinition definition = new EntityModelDefinition();
        definition.model = model;
        return definition;
    }

    private static BoneDefinition.VanillaPart part(String path)
    {
        BoneDefinition.VanillaPart ref = new BoneDefinition.VanillaPart();
        ref.part = path;
        return ref;
    }

    private static BoneDefinition.VanillaPart field(String name, int element)
    {
        BoneDefinition.VanillaPart ref = new BoneDefinition.VanillaPart();
        ref.field = name;
        ref.element = element;
        return ref;
    }

    @Test
    void aModelIsNamedByItsClass()
    {
        // Minecraft's by its 1.12 name, a mod's by its own full name.
        assertSame(HumanoidModel.class, definition("net.minecraft.client.model.ModelBiped").modelClass());
        assertSame(ModMobModel.class, definition(ModMobModel.class.getName()).modelClass());
        // Minecraft's own names aren't loaded: on Fabric they are renamed in the game.
        assertNull(definition(HumanoidModel.class.getName()).modelClass());
        assertNull(definition("com.example.NoSuchModel").modelClass());
        assertNull(definition(null).modelClass());
    }

    @Test
    void aPartIsFoundByItsPath()
    {
        EntityModelDefinition definition = definition(ModMobModel.class.getName());
        assertSame(body, ModelPartLookup.find(definition, part("body"), root, null));
        assertSame(leftArm, ModelPartLookup.find(definition, part("body/left_arm"), root, null));
        assertNull(ModelPartLookup.find(definition, part("body/right_arm"), root, null));
        assertNull(ModelPartLookup.find(definition, part("body//left_arm"), root, null));
        // The same for a vanilla model.
        assertSame(leftArm, ModelPartLookup.find(definition("net.minecraft.client.model.ModelBiped"), part("body/left_arm"), root, null));
    }

    @Test
    void aVanillaModelsFieldIsItsOneTwelveName()
    {
        EntityModelDefinition definition = definition("net.minecraft.client.model.ModelBiped");
        assertSame(leftArm, ModelPartLookup.find(definition, field("bipedLeftArm", -1), root, null));
        assertNull(ModelPartLookup.find(definition, field("leftArm", -1), root, null));
    }

    @Test
    void aModsModelsFieldIsFoundByReflection()
    {
        ModelPart first = new ModelPart(Map.of()), second = new ModelPart(Map.of());
        ModMobModel model = new ModMobModel(body, first, second);
        EntityModelDefinition definition = definition(ModMobModel.class.getName());
        assertSame(body, ModelPartLookup.find(definition, field("torso", -1), root, model));
        assertSame(second, ModelPartLookup.find(definition, field("tentacles", 1), root, model));
        assertNull(ModelPartLookup.find(definition, field("tentacles", 2), root, model));
        assertNull(ModelPartLookup.find(definition, field("legs", -1), root, model));
        // Declared by the class it extends.
        assertSame(body, ModelPartLookup.find(definition(BiggerModMobModel.class.getName()), field("torso", -1), root, new BiggerModMobModel(body)));
    }

    @Test
    void aBonesPartNeedsAPathFieldOrIndex()
    {
        BoneDefinition bone = new BoneDefinition();
        bone.name = "arm";
        bone.vanilla = part("body/left_arm");
        assertDoesNotThrow(() -> bone.validate());
        bone.vanilla = new BoneDefinition.VanillaPart();
        assertThrows(MalformedKumoTemplateException.class, () -> bone.validate());
    }
}
