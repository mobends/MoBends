package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.client.Mesh;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.util.MeshBuilder;
import goblinbob.mobends.core.ModStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/** The wolf's mouth, inside and tongue: textured planes hung from its {@code head}, {@code nose}, {@code mouth} and {@code tongue} bones. */
public class LayerWolfMisc implements LayerRenderer<EntityWolf>
{

    private static final ResourceLocation WOLF_MISC_TEXTURE = new ResourceLocation(ModStatics.MODID,
            "textures/entity/wolf_misc.png");
    private static final int textureWidth = 8;
    private static final int textureHeight = 8;

    protected final TextureManager textureManager;
    private Mesh mouthBottom;
    private Mesh mouthTop;
    private Mesh mouthInside;
    private Mesh tongue;

    public LayerWolfMisc()
    {
        textureManager = Minecraft.getMinecraft().getTextureManager();

        mouthBottom = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        mouthBottom.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(mouthBottom, -1.5F, 0F, -4F, 3, 4, true, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthBottom.finishDrawing();

        mouthTop = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        mouthTop.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(mouthTop, -1.5F, 1F, -4F, 3, 4, false, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthTop.finishDrawing();

        mouthInside = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        mouthInside.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(mouthInside, -1.5F, -4.1F, -3.0F, 3, 1, true, Color.WHITE,
                new int[] { 0, 0, 3, 4 }, textureWidth, textureHeight);
        mouthInside.finishDrawing();

        tongue = new Mesh(DefaultVertexFormats.POSITION_TEX_COLOR_NORMAL, 6);
        tongue.beginDrawing(GL11.GL_TRIANGLES);
        MeshBuilder.texturedXZPlane(tongue, -1.5F, 0F, -4F, 3, 6, true, Color.WHITE,
                new int[] { 3, 0, 6, 6 }, textureWidth, textureHeight);
        tongue.finishDrawing();
    }

    public void doRenderLayer(EntityWolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        final EntityData<?> data = EntityDatabase.instance.get(wolf);
        // The mouth hangs from the head, the head from the body, by the bones' names.
        final IModelPart body = part(data, "body"), head = part(data, "head"), nose = part(data, "nose"), mouth = part(data, "mouth"), tongueBone = part(data, "tongue");
        if (body == null || head == null || nose == null || mouth == null || tongueBone == null)
        {
            return;
        }

        textureManager.bindTexture(WOLF_MISC_TEXTURE);

        boolean isChild = wolf.isChild();

        GlStateManager.pushMatrix();
        if (isChild)
        {
            GlStateManager.translate(0.0F, 10.0F * scale, 0.0F * scale);
            body.applyLocalTransform(scale * 0.5F);
        }
        else
        {
            body.applyLocalTransform(scale);
        }
        head.applyLocalTransform(scale);

        GlStateManager.enableCull();
        // Mouth inside
        GlStateManager.pushMatrix();
        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(scale, scale, scale);
        mouthInside.display();
        GlStateManager.popMatrix();

        // Mouth bottom
        GlStateManager.pushMatrix();
        nose.applyLocalTransform(scale);
        GlStateManager.scale(scale, scale, scale);
        mouthTop.display();
        GlStateManager.popMatrix();

        // Mouth top
        GlStateManager.pushMatrix();
        mouth.applyLocalTransform(scale);
        GlStateManager.scale(scale, scale, scale);
        mouthBottom.display();
        GlStateManager.popMatrix();

        // Tongue
        GlStateManager.pushMatrix();
        tongueBone.applyLocalTransform(scale);
        GlStateManager.scale(scale, scale, scale);
        tongue.display();
        GlStateManager.popMatrix();

        GlStateManager.popMatrix();
    }

    private static IModelPart part(EntityData<?> data, String name)
    {
        Object part = data == null ? null : data.getPartForName(name);
        return part instanceof IModelPart ? (IModelPart) part : null;
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }

}
