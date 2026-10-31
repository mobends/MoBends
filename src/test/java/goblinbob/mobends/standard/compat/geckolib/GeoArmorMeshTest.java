package goblinbob.mobends.standard.compat.geckolib;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.standard.client.model.armor.ArmorWrapper;
import goblinbob.mobends.test.geometry.Capture;
import goblinbob.mobends.test.geometry.ModelGeometry;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import org.junit.Test;
import software.bernie.geckolib3.geo.raw.pojo.Converter;
import software.bernie.geckolib3.geo.raw.tree.RawGeometryTree;
import software.bernie.geckolib3.geo.render.GeoBuilder;
import software.bernie.geckolib3.geo.render.built.GeoModel;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.*;

/**
 * GeckoLib armour is drawn where the armour wrapper draws vanilla armour: a GeckoLib model made of
 * the boxes of vanilla's armour model lands, part by part, where the wrapper puts the vanilla boxes,
 * standing still and with the body turned and the arms and legs bent at the elbows and knees.
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class GeoArmorMeshTest
{

    /** The boxes of {@code new ModelBiped(1.0F)}'s parts at rest, in model space (pixels, y down), uninflated: GeckoLib bone, then min and max. */
    private static final Object[][] BOXES = {
            { "armorHead", -4, -8, -4, 4, 0, 4 },
            { "armorBody", -4, 0, -2, 4, 12, 2 },
            { "armorRightArm", -8, 0, -2, -4, 12, 2 },
            { "armorLeftArm", 4, 0, -2, 8, 12, 2 },
            { "armorRightLeg", -3.9F, 12, -2, 0.1F, 24, 2 },
            { "armorLeftLeg", -0.1F, 12, -2, 3.9F, 24, 2 },
    };

    /** The GeckoLib armour model of vanilla's boxes, inflated by a pixel as vanilla's are. */
    private static GeoModel vanillaLikeArmor()
    {
        StringBuilder bones = new StringBuilder();
        for (Object[] box : BOXES)
        {
            float x0 = ((Number) box[1]).floatValue(), y0 = ((Number) box[2]).floatValue(), z0 = ((Number) box[3]).floatValue();
            float x1 = ((Number) box[4]).floatValue(), y1 = ((Number) box[5]).floatValue(), z1 = ((Number) box[6]).floatValue();
            if (bones.length() > 0) bones.append(',');
            // A model-space x is GeckoLib's origin x as is; its y counts up from the feet.
            bones.append(String.format(Locale.ROOT,
                    "{\"name\":\"%s\",\"pivot\":[0,24,0],\"rotation\":[-15,10,0],\"cubes\":[{\"origin\":[%f,%f,%f],\"size\":[%f,%f,%f],\"uv\":[0,0],\"inflate\":1}]}",
                    box[0], x0, GeoArmorMesh.GEO_ORIGIN_Y - y1, z0, x1 - x0, y1 - y0, z1 - z0));
        }
        String json = "{\"format_version\":\"1.12.0\",\"minecraft:geometry\":[{\"description\":{\"identifier\":\"geometry.test\",\"texture_width\":64,\"texture_height\":32},\"bones\":[" + bones + "]}]}";
        try
        {
            ResourceLocation location = new ResourceLocation("mobends", "test");
            return GeoBuilder.getGeoBuilder("mobends").constructGeoModel(RawGeometryTree.parseHierarchy(Converter.fromJsonString(json), location));
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    private static GeoArmorRenderer renderer()
    {
        return new GeoArmorRenderer(new AnimatedGeoModel()
        {
            @Override public ResourceLocation getModelLocation(Object object) { return null; }
            @Override public ResourceLocation getTextureLocation(Object object) { return null; }
            @Override public ResourceLocation getAnimationFileLocation(Object animatable) { return null; }
        })
        {
        };
    }

    /** An entity's animated parts, as the player's data names them. */
    private static final class Posed extends EntityData<EntityLivingBase>
    {
        Posed()
        {
            super(null);
        }

        @Override
        protected ResourceLocation getDefaultAnimator()
        {
            return null;
        }

        ModelPartTransform part(String name, float x, float y, float z)
        {
            ModelPartTransform part = new ModelPartTransform();
            part.position.set(x, y, z);
            nameToPartMap.put(name, part);
            return part;
        }
    }

    private static void rotate(ModelPartTransform part, float x, float y, float z, float degrees)
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(x, y, z, (float) Math.toRadians(degrees));
        part.rotation.snapTo(q);
    }

    /** The player's parts at rest; {@code bent}: the body turned, an arm and a leg raised and bent. */
    private static Posed pose(boolean bent)
    {
        Posed posed = new Posed();
        ModelPartTransform body = posed.part("body", 0, 12, 0);
        ModelPartTransform head = posed.part("head", 0, -12, 0);
        ModelPartTransform rightArm = posed.part("rightArm", -5, -10, 0);
        ModelPartTransform rightForeArm = posed.part("rightForeArm", 0, 4, 2);
        posed.part("leftArm", 5, -10, 0);
        ModelPartTransform leftForeArm = posed.part("leftForeArm", 0, 4, 2);
        ModelPartTransform rightLeg = posed.part("rightLeg", 0, 12, 0);
        ModelPartTransform rightForeLeg = posed.part("rightForeLeg", 0, 6, -2);
        posed.part("leftLeg", 0, 12, 0);
        ModelPartTransform leftForeLeg = posed.part("leftForeLeg", 0, 6, -2);
        if (bent)
        {
            rotate(body, 0, 1, 0, 25);
            rotate(head, 1, 0, 0, -20);
            rotate(rightArm, 1, 0.3F, 0, -60);
            rotate(rightForeArm, 1, 0, 0, -80);
            rotate(leftForeArm, 1, 0, 0, -45);
            rotate(rightLeg, 1, 0, 0, -50);
            rotate(rightForeLeg, 1, 0, 0, 70);
            rotate(leftForeLeg, 1, 0, 0, 30);
        }
        return posed;
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

    private static List<Capture.Vertex[]> draw(GeoArmorMesh.Piece piece)
    {
        Capture.reset();
        GlStateManager.pushMatrix();
        GlStateManager.scale(16F, 16F, 16F);
        GeoArmorMesh.draw(piece, 0.0625F, 1, 1, 1, 1);
        GlStateManager.popMatrix();
        return new ArrayList<>(Capture.drawn);
    }

    private static void assertLandsOnTheVanillaArmor(boolean bent)
    {
        ModelBiped vanilla = new ModelBiped(1.0F);
        ArmorWrapper wrapper = ArmorWrapper.createFor(vanilla);
        wrapper.syncUp(pose(bent));

        GeoArmorMesh mesh = new GeoArmorMesh(wrapper);
        mesh.build(vanillaLikeArmor(), renderer());

        wrapper.apply();
        ModelRenderer[] parts = { vanilla.bipedHead, vanilla.bipedBody, vanilla.bipedRightArm, vanilla.bipedLeftArm, vanilla.bipedRightLeg, vanilla.bipedLeftLeg };
        GeoArmorMesh.Piece[] pieces = { mesh.head, mesh.body, mesh.rightArm, mesh.leftArm, mesh.rightLeg, mesh.leftLeg };
        for (int i = 0; i < parts.length; i++)
        {
            double[] expected = bounds(ModelGeometry.render(parts[i]));
            double[] actual = bounds(draw(pieces[i]));
            for (int k = 0; k < 6; k++)
            {
                // The wrapper inflates a limb's segments by a hundredth of a pixel at most.
                assertEquals(BOXES[i][0] + ", bound " + k, expected[k], actual[k], 0.01);
            }
        }
        wrapper.deapply();
    }

    @Test
    public void standingStillTheArmorIsWhereVanillaArmorIs()
    {
        assertLandsOnTheVanillaArmor(false);
    }

    @Test
    public void theArmorFollowsTheBodyAndBendsAtTheJoints()
    {
        assertLandsOnTheVanillaArmor(true);
    }

    @Test
    public void aLimbIsCutAtItsJoint()
    {
        ArmorWrapper wrapper = ArmorWrapper.createFor(new ModelBiped(1.0F));
        GeoArmorMesh mesh = new GeoArmorMesh(wrapper);
        mesh.build(vanillaLikeArmor(), renderer());

        // The arm's four sides each cut at the elbow (its ends whole), as are the leg's at the knee.
        assertEquals(5 * 4, mesh.rightArm.upper.getVertexCount());
        assertEquals(5 * 4, mesh.rightArm.lower.getVertexCount());
        assertEquals(5 * 4, mesh.leftLeg.upper.getVertexCount());
        assertEquals(5 * 4, mesh.leftLeg.lower.getVertexCount());
        // The head and the body aren't cut.
        assertEquals(6 * 4, mesh.head.upper.getVertexCount());
        assertTrue(mesh.head.lower.isEmpty());
        assertTrue(mesh.body.lower.isEmpty());
    }

}
