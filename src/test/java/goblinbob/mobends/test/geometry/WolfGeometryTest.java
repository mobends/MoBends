package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.standard.mutators.WolfMutator;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelWolf;
import org.junit.Test;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The wolf's model definition draws what the Java wolf does: the body along Z with its texture
 * turned, the nose, mouth and ears as bones of their own.
 */
public class WolfGeometryTest
{

    /** What ModelWolf.render draws, for a grown wolf. */
    private static ModelRenderer[] rendered(ModelWolf m)
    {
        return new ModelRenderer[] { m.wolfHeadMain, m.wolfBody, m.wolfLeg1, m.wolfLeg2, m.wolfLeg3, m.wolfLeg4, m.wolfTail, m.wolfMane };
    }

    @Test
    public void drawsWhatTheJavaWolfDraws() throws Exception
    {
        ModelWolf java = new ModelWolf();
        WolfMutator mutator = new WolfMutator();
        mutator.createParts(java);
        // Where WolfData puts the Java parts, which they take from it every frame.
        mutator.wolfBody.setPosition(0, 14, 8);
        mutator.wolfHeadMain.setPosition(0, -0.5F, -13);
        mutator.wolfMane.setPosition(0, -0.5F, -12);
        mutator.nose.setPosition(0, 1, -3);
        mutator.mouth.setPosition(0, 2, -3);
        mutator.leftEar.setPosition(-2, -3, -1);
        mutator.rightEar.setPosition(2, -3, -1);
        mutator.wolfLeg1.setPosition(-2, 3, -1);
        mutator.wolfLeg2.setPosition(2, 3, -1);
        mutator.wolfLeg3.setPosition(-2, 3, -12);
        mutator.wolfLeg4.setPosition(2, 3, -12);
        mutator.wolfTail.setPosition(0, -3, 0);
        mutator.foreLeg1.setPosition(0, 4, -1);
        mutator.foreLeg2.setPosition(0, 4, -1);
        mutator.foreLeg3.setPosition(0, 4, 1);
        mutator.foreLeg4.setPosition(0, 4, 1);

        ModelWolf defined = new ModelWolf();
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/wolf.json"))
        {
            new DefinedMutator<>(ModelDefinitions.parse(new JsonParser().parse(reader))).createParts(defined);
        }
        List<String> differences = ModelGeometry.differences(ModelGeometry.render(rendered(java)), ModelGeometry.render(rendered(defined)), 1e-3);
        assertTrue(differences.size() + " quads differ:\n" + String.join("\n", differences.subList(0, Math.min(30, differences.size()))), differences.isEmpty());
    }

}
