package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.asset.AssetLocation;
import goblinbob.mobends.core.asset.AssetModels;
import goblinbob.mobends.core.client.model.IModelPart;
import goblinbob.mobends.core.supporters.*;
import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.core.util.Color;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import java.util.Map;
import java.util.Set;

public class LayerPlayerAccessories implements LayerRenderer<AbstractClientPlayer>
{
    private final RenderLivingBase<? extends AbstractClientPlayer> renderPlayer;
    private final RenderItem itemRenderer;
    private final ModelManager modelManager;
    private final TextureManager textureManager;
    private final ItemStack emptyItemStack = new ItemStack(Items.AIR);

    public LayerPlayerAccessories(RenderLivingBase<? extends AbstractClientPlayer> renderPlayer)
    {
        Minecraft mc = Minecraft.getMinecraft();
        this.renderPlayer = renderPlayer;
        this.itemRenderer = mc.getRenderItem();
        this.modelManager = mc.getRenderItem().getItemModelMesher().getModelManager();
        this.textureManager = mc.getTextureManager();
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        Set<Map.Entry<String, AccessoryDetails>> accessories = SupporterContent.getAccessories();

        final PlayerData data = BenderHelper.getData(player, renderPlayer);
        assert data != null;

        Map<String, AccessorySettings> settingsMap = SupporterContent.getAccessorySettingsMapFor(player);

        for (Map.Entry<String, AccessoryDetails> entry : accessories)
        {
            AccessorySettings accessorySettings = settingsMap.getOrDefault(entry.getKey(), AccessorySettings.DEFAULT);
            renderAccessory(player, data, entry.getValue(), accessorySettings, scale);
        }
    }

    private void renderAccessory(AbstractClientPlayer player, PlayerData data, AccessoryDetails accessory, AccessorySettings settings, float scale)
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

    private void renderPart(AbstractClientPlayer player, PlayerData data, AccessoryPart part, AccessorySettings settings, float scale)
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
            if (player.capabilities.isFlying)
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
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.ITEM);

        for (EnumFacing enumfacing : EnumFacing.values())
        {
            itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, enumfacing, 0L), color, emptyItemStack);
        }

        itemRenderer.renderQuads(bufferbuilder, model.getQuads(null, null, 0L), color, emptyItemStack);
        tessellator.draw();
    }

    private void applyBindPointTransform(PlayerData data, BindPoint bindPoint, float scale)
    {
        IModelPart modelPart = bindPoint.getPartSelector().apply(data);

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
