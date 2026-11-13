package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.data.RemoteFlight;
import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.asset.AssetModels;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.supporters.*;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.core.data.LivingEntityData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.LightUtil;

import java.util.List;

import java.util.Map;
import java.util.Set;

public class LayerPlayerAccessories implements LayerRenderer<AbstractClientPlayer>
{
    private final RendererLivingEntity<?> renderPlayer;
    private final TextureManager textureManager;

    public LayerPlayerAccessories(RendererLivingEntity<?> renderPlayer)
    {
        Minecraft mc = Minecraft.getMinecraft();
        this.renderPlayer = renderPlayer;
        this.textureManager = mc.getTextureManager();
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        Set<Map.Entry<String, AccessoryDetails>> accessories = SupporterContent.getAccessories();

        final LivingEntityData<?> data = BenderHelper.getData(player, renderPlayer);
        if (data == null)
        {
            return;
        }

        Map<String, AccessorySettings> settingsMap = SupporterContent.getAccessorySettingsMapFor(player);

        for (Map.Entry<String, AccessoryDetails> entry : accessories)
        {
            AccessorySettings accessorySettings = settingsMap.getOrDefault(entry.getKey(), AccessorySettings.DEFAULT);
            renderAccessory(player, data, entry.getValue(), accessorySettings, scale);
        }
    }

    private void renderAccessory(AbstractClientPlayer player, LivingEntityData<?> data, AccessoryDetails accessory, AccessorySettings settings, float scale)
    {
        if (!settings.isUnlocked() || settings.isHidden())
        {
            return;
        }

        for (AccessoryPart part : accessory.getParts())
        {
            renderPart(player, data, part, settings, scale);
        }
    }

    private void renderPart(AbstractClientPlayer player, LivingEntityData<?> data, AccessoryPart part, AccessorySettings settings, float scale)
    {
        SimpleBakedModel simpleBakedModel = AssetModels.INSTANCE.getModel(part.getModelPath());

        if (simpleBakedModel == null)
        {
            return;
        }

        GlStateManager.pushMatrix();

        // Reverting the sneak transform
        if (player.isSneaking())
        {
            if (RemoteFlight.isFlying(player))
            {
                GlStateManager.translate(0F, 4F * scale, 0F);
            }
            else
            {
                GlStateManager.translate(0F, 3F * scale, 0F);
            }
        }

        applyBindPointTransform(data, part.getBindPoint(), scale);

        ItemTransformVec3f transformVec3f = simpleBakedModel.getItemCameraTransforms().head;
        GlStateManager.translate(transformVec3f.translation.x, transformVec3f.translation.y, transformVec3f.translation.z);
        GlStateManager.rotate(transformVec3f.rotation.x, 1, 0, 0);
        GlStateManager.rotate(transformVec3f.rotation.y, 0, 1, 0);
        GlStateManager.rotate(transformVec3f.rotation.z, 0, 0, 1);
        GlStateManager.scale(transformVec3f.scale.x, transformVec3f.scale.y, transformVec3f.scale.z);

        // Diffuse pass
        textureManager.bindTexture(part.getDiffuseTexturePath());
        drawModel(simpleBakedModel, 0xffffffff);

        // Inked pass
        AssetLocation inkedLocation = part.getInkedTexturePath();
        if (inkedLocation != null)
        {
            textureManager.bindTexture(inkedLocation);
            GlStateManager.color(1, 0, 0);
            drawModel(simpleBakedModel, Color.asHex(settings.getColor()));
            GlStateManager.color(1, 1, 0);
        }

        GlStateManager.popMatrix();
    }

    private void drawModel(IBakedModel model, int color)
    {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer bufferbuilder = tessellator.getWorldRenderer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            renderQuads(bufferbuilder, model.getFaceQuads(enumfacing), color);
        }

        renderQuads(bufferbuilder, model.getGeneralQuads(), color);
        tessellator.draw();
    }

    /** As RenderItem#renderQuads, for quads without a tint. */
    private static void renderQuads(WorldRenderer buffer, List<BakedQuad> quads, int color)
    {
        for (BakedQuad quad : quads)
        {
            LightUtil.renderQuadColor(buffer, quad, color);
        }
    }

    private void applyBindPointTransform(LivingEntityData<?> data, BindPoint bindPoint, float scale)
    {
        IModelPart modelPart = bindPoint.partOf(data);

        if (modelPart != null)
        {
            modelPart.applyCharacterTransform(scale);
        }
    }

    @Override
    public boolean shouldCombineTextures()
    {
        return false;
    }
}
