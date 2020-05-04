package goblinbob.mobends.standard.client.renderer.entity.mutated;

import goblinbob.mobends.core.client.Mesh;
import goblinbob.mobends.core.client.MutatedRenderer;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.MeshBuilder;
import goblinbob.mobends.standard.main.ModStatics;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class WolfRenderer<T extends EntityWolf> extends MutatedRenderer<T>
{

    private static final ResourceLocation WOLF_MISC_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/entity/wolf_misc.png");
    private static final int textureWidth = 8;
    private static final int textureHeight = 8;

    private Mesh mouthBottom;
    private Mesh mouthTop;
    private Mesh tongue;

    public WolfRenderer()
    {
        mouthBottom = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        mouthBottom.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(mouthBottom, -1.5F, 1F, -4F, 3, 4, true, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthBottom.finishDrawing();

        mouthTop = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        mouthTop.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(mouthTop, -1.5F, 0F, -4F, 3, 4, false, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthTop.finishDrawing();

        tongue = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        tongue.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(tongue, -1, -1, -1, 1, 1, true, Color.WHITE,
                new int[] { 3, 0, 6, 6 }, textureWidth, textureHeight);
        tongue.finishDrawing();
    }

}
