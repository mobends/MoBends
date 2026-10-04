package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.client.model.ModelPartExtended;
import goblinbob.mobends.core.client.model.ModelPartPostOffset;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.SkeletonMutator;
import goblinbob.mobends.standard.mutators.ZombieMutator;
import goblinbob.mobends.standard.mutators.ZombieVillagerMutator;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSkeleton;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import org.junit.Test;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The zombies' and the skeleton's model definitions draw what their Java mutators do, and attach
 * held items and hats in the same place.
 */
public class BipedGeometryTest
{

    private static ModelBiped defined(String name, ModelBiped model) throws Exception
    {
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/" + name + ".json"))
        {
            new DefinedMutator<>(ModelDefinitions.parse(new JsonParser().parse(reader))).createParts(model);
        }
        return model;
    }

    private static ModelPart part(BipedMutator<?, ?, ?> mutator, String field) throws Exception
    {
        Field f = BipedMutator.class.getDeclaredField(field);
        f.setAccessible(true);
        return (ModelPart) f.get(mutator);
    }

    /** What ModelBiped.render draws, in its order. */
    private static ModelRenderer[] rendered(ModelBiped m)
    {
        return new ModelRenderer[] { m.bipedHead, m.bipedBody, m.bipedRightArm, m.bipedLeftArm, m.bipedRightLeg, m.bipedLeftLeg, m.bipedHeadwear };
    }

    private static void assertSame(String what, ModelBiped java, ModelBiped defined)
    {
        List<String> differences = ModelGeometry.differences(ModelGeometry.render(rendered(java)), ModelGeometry.render(rendered(defined)), 1e-3);
        assertTrue(what + ": " + differences.size() + " quads differ:\n" + String.join("\n", differences.subList(0, Math.min(30, differences.size()))),
                differences.isEmpty());
        assertAttachedAlike(what, java, defined);
    }

    private static void assertAttachedAlike(String what, ModelBiped java, ModelBiped defined)
    {
        String[] names = { "head", "body", "right arm", "left arm" };
        ModelRenderer[] a = { java.bipedHead, java.bipedBody, java.bipedRightArm, java.bipedLeftArm };
        ModelRenderer[] b = { defined.bipedHead, defined.bipedBody, defined.bipedRightArm, defined.bipedLeftArm };
        for (int i = 0; i < a.length; i++)
        {
            double[] expected = ModelGeometry.postRender(a[i]), actual = ModelGeometry.postRender(b[i]);
            for (int k = 0; k < 16; k++)
            {
                assertEquals(what + ", " + names[i] + ": " + ModelGeometry.describe(expected) + " vs " + ModelGeometry.describe(actual), expected[k], actual[k], 1e-4);
            }
        }
    }

    @Test
    public void theZombieDrawsWhatTheJavaZombieDraws() throws Exception
    {
        ModelZombie java = new ModelZombie();
        new ZombieMutator().createParts(java);
        assertSame("zombie", java, defined("zombie", new ModelZombie()));
    }

    @Test
    public void thePigZombieDrawsWhatTheJavaZombieDraws() throws Exception
    {
        ModelZombie java = new ModelZombie();
        new ZombieMutator().createParts(java);
        assertSame("pig zombie", java, defined("pig_zombie", new ModelZombie()));
    }

    @Test
    public void theSkeletonDrawsWhatTheJavaSkeletonDraws() throws Exception
    {
        ModelSkeleton java = new ModelSkeleton();
        SkeletonMutator mutator = new SkeletonMutator();
        Field boneLimbs = SkeletonMutator.class.getDeclaredField("boneLimbs");
        boneLimbs.setAccessible(true);
        boneLimbs.set(mutator, true);
        mutator.createParts(java);
        mirrorLeftArm(mutator, java);
        // Where SkeletonData puts the Java parts, which they take from it every frame.
        part(mutator, "rightArm").setPosition(-5, -10, 0);
        part(mutator, "leftArm").setPosition(5, -10, 0);
        part(mutator, "rightLeg").setPosition(-2, 12, 0);
        part(mutator, "leftLeg").setPosition(2, 12, 0);
        part(mutator, "rightForeArm").setPosition(0, 4, 1);
        part(mutator, "leftForeArm").setPosition(0, 4, 1);
        part(mutator, "rightForeLeg").setPosition(0, 6, -1);
        part(mutator, "leftForeLeg").setPosition(0, 6, -1);
        assertSame("skeleton", java, defined("skeleton", new ModelSkeleton()));
    }

    /**
     * The Java skeleton's left arm isn't mirrored as vanilla's is, so it shows the right arm's
     * skin back to front; the definition copies vanilla's. So does the Java one here, to compare
     * the rest.
     */
    private static void mirrorLeftArm(SkeletonMutator mutator, ModelSkeleton model) throws Exception
    {
        ModelPartExtended arm = (ModelPartExtended) new ModelPartExtended(model, 40, 16).setMirror(true);
        arm.setParent(part(mutator, "body")).setPosition(5.0F, 2.0F, 0.0F)
                .developBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, 0.0F).inflate(0.01F, 0F, 0.01F).hideFace(BoxSide.BOTTOM).create();
        ModelPartPostOffset foreArm = (ModelPartPostOffset) new ModelPartPostOffset(model, 40, 16 + 6).setPostOffset(0, -4F, -1F).setMirror(true);
        foreArm.setPosition(0.0F, 4.0F, 1.0F).setParent(arm)
                .developBox(-1.0F, 0.0F, -2.0F, 2, 6, 2, 0.0F).hideFace(BoxSide.TOP).offsetTextureQuad(BoxSide.BOTTOM, 0, -6F).create();
        arm.setExtension(foreArm);
        model.bipedLeftArm = arm;
        set(mutator, "leftArm", arm);
        set(mutator, "leftForeArm", foreArm);
    }

    private static void set(BipedMutator<?, ?, ?> mutator, String field, Object value) throws Exception
    {
        Field f = BipedMutator.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(mutator, value);
    }

    /**
     * The zombie villager's vanilla model is villager-shaped (a deeper body with a robe, its own
     * arms and legs); the Java one drew a zombie's biped over that texture, the definition draws
     * vanilla's. What is attached to it goes where it did.
     */
    @Test
    public void theZombieVillagerAttachesWhereTheJavaZombieVillagerDoes() throws Exception
    {
        ModelZombieVillager java = new ModelZombieVillager();
        new ZombieVillagerMutator().createParts(java);
        assertAttachedAlike("zombie villager", java, defined("zombie_villager", new ModelZombieVillager()));
    }

}
