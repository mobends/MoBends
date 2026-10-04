package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.client.model.BoxSide;
import goblinbob.mobends.core.client.model.ModelPart;
import goblinbob.mobends.core.client.model.ModelPartExtended;
import goblinbob.mobends.standard.mutators.BipedMutator;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import org.junit.Test;

import java.io.FileReader;
import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The player's model definition draws what the Java player does (PlayerMutator), box for box,
 * texture coordinate for texture coordinate, and puts what is attached to it in the same place.
 */
public class PlayerGeometryTest
{

    private static EntityModelDefinition definition() throws Exception
    {
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/player.json"))
        {
            return ModelDefinitions.parse(new JsonParser().parse(reader));
        }
    }

    private static ModelPlayer java(boolean slim) throws Exception
    {
        ModelPlayer model = new ModelPlayer(0, slim);
        PlayerMutator mutator = new PlayerMutator();
        Field smallArms = PlayerMutator.class.getDeclaredField("smallArms");
        smallArms.setAccessible(true);
        smallArms.set(mutator, slim);
        mutator.createParts(model);
        fixLeftShin(mutator, model);
        return model;
    }

    /**
     * The Java player's left shin keeps the biped's: the right leg's skin (0, 22), mirrored, not
     * the player skin's left leg (16, 54). The definition takes the left leg's; so does the Java
     * one here, to compare the rest.
     */
    private static void fixLeftShin(PlayerMutator mutator, ModelPlayer model) throws Exception
    {
        Field legField = BipedMutator.class.getDeclaredField("leftLeg");
        Field shinField = BipedMutator.class.getDeclaredField("leftForeLeg");
        legField.setAccessible(true);
        shinField.setAccessible(true);
        ModelPartExtended leg = (ModelPartExtended) legField.get(mutator);
        ModelPart oldShin = (ModelPart) shinField.get(mutator);
        ModelPart shin = new ModelPart(model, 16, 48 + 6).setParent(leg).setPosition(0, 6.0F, -2.0F);
        shin.developBox(-0.1F, 0.0F, 0.0F, 4, 6, 4, 0.0F)
                .inflate(0.01F, 0, 0.01F)
                .offsetTextureQuad(BoxSide.BOTTOM, 0, -6F)
                .create();
        if (oldShin.childModels != null)
        {
            for (net.minecraft.client.model.ModelRenderer child : oldShin.childModels) shin.addChild(child);
        }
        leg.setExtension(shin);
        shinField.set(mutator, shin);
    }

    private static ModelPlayer defined(boolean slim) throws Exception
    {
        ModelPlayer model = new ModelPlayer(0, slim);
        new DefinedMutator<>(definition()).createParts(model);
        return model;
    }

    /** What ModelPlayer.render draws, in its order (without the sneaking and child transforms). */
    private static ModelRenderer[] rendered(ModelPlayer m)
    {
        return new ModelRenderer[] { m.bipedHead, m.bipedBody, m.bipedRightArm, m.bipedLeftArm, m.bipedRightLeg, m.bipedLeftLeg, m.bipedHeadwear,
                m.bipedLeftLegwear, m.bipedRightLegwear, m.bipedLeftArmwear, m.bipedRightArmwear, m.bipedBodyWear };
    }

    private static void assertSameGeometry(boolean slim) throws Exception
    {
        List<String> differences = ModelGeometry.differences(ModelGeometry.render(rendered(java(slim))), ModelGeometry.render(rendered(defined(slim))), 1e-3);
        assertTrue((slim ? "slim: " : "") + differences.size() + " quads differ:\n" + String.join("\n", differences.subList(0, Math.min(30, differences.size()))),
                differences.isEmpty());
    }

    @Test
    public void drawsWhatTheJavaPlayerDraws() throws Exception
    {
        assertSameGeometry(false);
    }

    @Test
    public void drawsWhatTheJavaPlayerDrawsWithSlimArms() throws Exception
    {
        assertSameGeometry(true);
    }

    @Test
    public void attachesWhereTheJavaPlayerAttaches() throws Exception
    {
        for (boolean slim : new boolean[] { false, true })
        {
            ModelPlayer java = java(slim), defined = defined(slim);
            String[] names = { "head", "body", "right arm", "left arm" };
            ModelRenderer[] a = { java.bipedHead, java.bipedBody, java.bipedRightArm, java.bipedLeftArm };
            ModelRenderer[] b = { defined.bipedHead, defined.bipedBody, defined.bipedRightArm, defined.bipedLeftArm };
            for (int i = 0; i < a.length; i++)
            {
                double[] expected = ModelGeometry.postRender(a[i]), actual = ModelGeometry.postRender(b[i]);
                for (int k = 0; k < 16; k++)
                {
                    assertEquals(names[i] + (slim ? " (slim)" : "") + ": " + ModelGeometry.describe(expected) + " vs " + ModelGeometry.describe(actual),
                            expected[k], actual[k], 1e-4);
                }
            }
        }
    }

}
