package goblinbob.mobends.test.geometry;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.client.definition.DefinedBoxAnchor;
import goblinbob.mobends.core.client.definition.DefinedMutator;
import goblinbob.mobends.core.definition.ModelDefinitions;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.model.ModelWolf;
import net.minecraft.client.model.ModelZombie;
import org.junit.Test;

import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Arrows stuck in a mob ({@code LayerArrow}) pick a part from the model's box list, a box of it,
 * and a point on it, placed by the part's {@code postRender}: in a defined mob, every such box
 * lands where the model draws it.
 */
public class ArrowAnchorTest
{

    private static <M extends ModelBase> M mutated(String name, M model) throws Exception
    {
        try (FileReader reader = new FileReader("src/main/resources/assets/mobends/bends/models/" + name + ".json"))
        {
            new DefinedMutator<>(ModelDefinitions.parse(new JsonParser().parse(reader))).createParts(model);
        }
        return model;
    }

    /** {@code rendered}: what the (mutated) model's render draws. */
    private static void assertBoxesLandOnTheModel(String name, ModelBase model, ModelRenderer... rendered)
    {
        List<double[]> drawn = new ArrayList<>();
        for (Capture.Vertex[] quad : ModelGeometry.render(rendered))
        {
            for (Capture.Vertex v : quad)
            {
                drawn.add(new double[] { v.x, v.y, v.z });
            }
        }
        assertTrue(name + ": no part of the box list is a posed one", model.boxList.stream().anyMatch(e -> e instanceof DefinedBoxAnchor));
        for (ModelRenderer entry : model.boxList)
        {
            if (!(entry instanceof DefinedBoxAnchor))
            {
                // A vanilla part no bone takes over (the player's cape and ears) stays as vanilla has it.
                continue;
            }
            double[] m = ModelGeometry.postRender(entry);
            for (ModelBox box : entry.cubeList)
            {
                for (int corner = 0; corner < 8; corner++)
                {
                    double x = (corner & 1) == 0 ? box.posX1 : box.posX2;
                    double y = (corner & 2) == 0 ? box.posY1 : box.posY2;
                    double z = (corner & 4) == 0 ? box.posZ1 : box.posZ2;
                    double[] p = { m[0] * x + m[4] * y + m[8] * z + m[12], m[1] * x + m[5] * y + m[9] * z + m[13], m[2] * x + m[6] * y + m[10] * z + m[14] };
                    double nearest = Double.MAX_VALUE;
                    double[] at = null;
                    for (double[] v : drawn)
                    {
                        double d = Math.max(Math.abs(v[0] - p[0]), Math.max(Math.abs(v[1] - p[1]), Math.abs(v[2] - p[2])));
                        if (d < nearest)
                        {
                            nearest = d;
                            at = v;
                        }
                    }
                    assertTrue(String.format("%s: a box corner (%s) lands at (%.2f, %.2f, %.2f), %.2f from anything the model draws (%.2f, %.2f, %.2f)",
                            name, box, p[0], p[1], p[2], nearest, at[0], at[1], at[2]), nearest < 0.6);
                }
            }
        }
    }

    @Test
    public void thePlayersArrowsLandOnIt() throws Exception
    {
        ModelPlayer m = mutated("player", new ModelPlayer(0, false));
        assertBoxesLandOnTheModel("player", m, m.bipedHead, m.bipedBody, m.bipedRightArm, m.bipedLeftArm, m.bipedRightLeg, m.bipedLeftLeg, m.bipedHeadwear,
                m.bipedLeftLegwear, m.bipedRightLegwear, m.bipedLeftArmwear, m.bipedRightArmwear, m.bipedBodyWear);
    }

    @Test
    public void theZombiesArrowsLandOnIt() throws Exception
    {
        ModelZombie m = mutated("zombie", new ModelZombie());
        assertBoxesLandOnTheModel("zombie", m, m.bipedHead, m.bipedBody, m.bipedRightArm, m.bipedLeftArm, m.bipedRightLeg, m.bipedLeftLeg, m.bipedHeadwear);
    }

    @Test
    public void theSpidersAndWolfsArrowsLandOnThem() throws Exception
    {
        ModelSpider s = mutated("spider", new ModelSpider());
        assertBoxesLandOnTheModel("spider", s, s.spiderHead, s.spiderNeck, s.spiderBody, s.spiderLeg1, s.spiderLeg2, s.spiderLeg3, s.spiderLeg4,
                s.spiderLeg5, s.spiderLeg6, s.spiderLeg7, s.spiderLeg8);
        ModelWolf w = mutated("wolf", new ModelWolf());
        assertBoxesLandOnTheModel("wolf", w, w.wolfHeadMain, w.wolfBody, w.wolfLeg1, w.wolfLeg2, w.wolfLeg3, w.wolfLeg4, w.wolfTail, w.wolfMane);
    }

}
