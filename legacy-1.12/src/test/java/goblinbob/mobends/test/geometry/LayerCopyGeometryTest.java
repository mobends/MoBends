package goblinbob.mobends.test.geometry;

import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.client.definition.DefinedModelPart;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelCreeper;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSheep1;
import net.minecraft.client.model.ModelSheep2;
import org.junit.Test;

import java.util.List;
import java.util.function.Function;

import static org.junit.Assert.*;

/**
 * The copies of a mob's model its layers draw (the sheep's wool, a charged creeper's armour) take
 * the mob's model definition as the main model does, keeping their own sizes: the parts stand
 * where the copy's vanilla parts do, as large as they are.
 */
public class LayerCopyGeometryTest
{

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

    /** Each named part of {@code mutated} draws within the bounds its vanilla counterpart of {@code vanilla} draws, as large. */
    private static <M extends ModelBase> void assertSameParts(String name, M vanilla, M mutated, String[] fields, Function<M, ModelRenderer[]> parts) throws Exception
    {
        new DefinedMutator<>(TestDefinitions.read(name)).createParts(mutated);
        ModelRenderer[] a = parts.apply(vanilla), b = parts.apply(mutated);
        for (int i = 0; i < a.length; i++)
        {
            assertTrue(name + " " + fields[i] + " is the definition's", b[i] instanceof DefinedModelPart);
            double[] expected = bounds(ModelGeometry.render(a[i])), actual = bounds(ModelGeometry.render(b[i]));
            for (int k = 0; k < 6; k++)
            {
                assertEquals(name + " " + fields[i] + ", bound " + k, expected[k], actual[k], 1e-3);
            }
        }
    }

    @Test
    public void theWoolIsTheSheepsDefinitionAtTheWoolsSize() throws Exception
    {
        String[] fields = { "head", "leg1", "leg2", "leg3", "leg4" };
        Function<ModelSheep1, ModelRenderer[]> parts = m -> new ModelRenderer[] { m.head, m.leg1, m.leg2, m.leg3, m.leg4 };
        assertSameParts("sheep", new ModelSheep1(), new ModelSheep1(), fields, parts);
        Function<ModelSheep2, ModelRenderer[]> sheared = m -> new ModelRenderer[] { m.head, m.leg1, m.leg2, m.leg3, m.leg4 };
        assertSameParts("sheep", new ModelSheep2(), new ModelSheep2(), fields, sheared);
    }

    @Test
    public void theChargeIsTheCreepersDefinitionAtTheChargesSize() throws Exception
    {
        Function<ModelCreeper, ModelRenderer[]> parts = m -> new ModelRenderer[] { m.head, m.leg1, m.leg2, m.leg3, m.leg4 };
        assertSameParts("creeper", new ModelCreeper(2.0F), new ModelCreeper(2.0F), new String[] { "head", "leg1", "leg2", "leg3", "leg4" }, parts);
    }

}
