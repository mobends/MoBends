package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.standard.mutators.SquidMutator;
import net.minecraft.client.model.ModelSquid;
import org.junit.Test;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/**
 * The squid's model definition: the body is the Java squid's; each tentacle is vanilla's (where
 * vanilla puts it, which the Java squid moved a unit in and down), cut into nine closed sections.
 */
public class SquidGeometryTest
{

    private static ModelSquid defined() throws Exception
    {
        ModelSquid model = new ModelSquid();
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/squid.json"))
        {
            new DefinedMutator<>(ModelDefinitions.parse(new JsonParser().parse(reader))).createParts(model);
        }
        return model;
    }

    private static double[] bounds(List<Capture.Vertex[]> quads)
    {
        double[] b = { Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE };
        for (Capture.Vertex[] quad : quads)
        {
            for (Capture.Vertex v : quad)
            {
                double[] p = { v.x, v.y, v.z };
                for (int k = 0; k < 3; k++)
                {
                    b[k] = Math.min(b[k], p[k]);
                    b[k + 3] = Math.max(b[k + 3], p[k]);
                }
            }
        }
        return b;
    }

    @Test
    public void theBodyIsTheJavaSquids() throws Exception
    {
        ModelSquid java = new ModelSquid();
        new SquidMutator().createParts(java);
        List<String> differences = ModelGeometry.differences(ModelGeometry.render(java.squidBody), ModelGeometry.render(defined().squidBody), 1e-3);
        assertTrue(String.join("\n", differences), differences.isEmpty());
    }

    @Test
    public void everyTentacleIsVanillasInNineSections() throws Exception
    {
        ModelSquid vanilla = new ModelSquid();
        ModelSquid defined = defined();
        for (int i = 0; i < 8; i++)
        {
            // The animator turns each tentacle where vanilla does, so compare them unturned.
            vanilla.squidTentacles[i].rotateAngleY = 0;
            List<Capture.Vertex[]> sections = ModelGeometry.render(defined.squidTentacles[i]);
            assertEquals("tentacle " + i + ": nine closed boxes", 9 * 6, sections.size());
            double[] expected = bounds(ModelGeometry.render(vanilla.squidTentacles[i])), actual = bounds(sections);
            for (int k = 0; k < 6; k++)
            {
                assertEquals("tentacle " + i + ", bound " + k, expected[k], actual[k], 1e-3);
            }
        }
    }

}
