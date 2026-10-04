package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.standard.mutators.SpiderMutator;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSpider;
import org.junit.Test;

import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.*;

/** The spider's model definition draws what the Java spider does: its legs stretched to two 12-unit segments. */
public class SpiderGeometryTest
{

    /** What ModelSpider.render draws. */
    private static ModelRenderer[] rendered(ModelSpider m)
    {
        return new ModelRenderer[] { m.spiderHead, m.spiderNeck, m.spiderBody, m.spiderLeg1, m.spiderLeg2, m.spiderLeg3, m.spiderLeg4,
                m.spiderLeg5, m.spiderLeg6, m.spiderLeg7, m.spiderLeg8 };
    }

    @Test
    public void drawsWhatTheJavaSpiderDraws() throws Exception
    {
        ModelSpider java = new ModelSpider();
        new SpiderMutator().createParts(java);
        ModelSpider defined = new ModelSpider();
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/spider.json"))
        {
            new DefinedMutator<>(ModelDefinitions.parse(new JsonParser().parse(reader))).createParts(defined);
        }
        List<String> differences = ModelGeometry.differences(ModelGeometry.render(rendered(java)), ModelGeometry.render(rendered(defined)), 1e-3);
        assertTrue(differences.size() + " quads differ:\n" + String.join("\n", differences.subList(0, Math.min(30, differences.size()))), differences.isEmpty());
    }

}
