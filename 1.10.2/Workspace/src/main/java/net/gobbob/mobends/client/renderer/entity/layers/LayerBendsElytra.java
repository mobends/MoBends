package net.gobbob.mobends.client.renderer.entity.layers;

import net.gobbob.mobends.client.model.ModelBendsElytra;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelElytra;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsElytra implements LayerRenderer<AbstractClientPlayer>
{
    /** The basic Elytra texture. */
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
    /** Instance of the player renderer. */
    private final RenderPlayer renderPlayer;
    /** The model used by the Elytra. */
    private final ModelBendsElytra modelElytra = new ModelBendsElytra();

    public LayerBendsElytra(RenderPlayer renderPlayerIn)
    {
        this.renderPlayer = renderPlayerIn;
    }

    public void doRenderLayer(AbstractClientPlayer entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (itemstack != null && itemstack.getItem() == Items.ELYTRA)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (entitylivingbaseIn.isPlayerInfoSet() && entitylivingbaseIn.getLocationElytra() != null)
            {
                this.renderPlayer.bindTexture(entitylivingbaseIn.getLocationElytra());
            }
            else if (entitylivingbaseIn.hasPlayerInfo() && entitylivingbaseIn.getLocationCape() != null && entitylivingbaseIn.isWearing(EnumPlayerModelParts.CAPE))
            {
                this.renderPlayer.bindTexture(entitylivingbaseIn.getLocationCape());
            }
            else
            {
                this.renderPlayer.bindTexture(TEXTURE_ELYTRA);
            }

            GlStateManager.pushMatrix();
            this.renderPlayer.getMainModel().bipedBody.postRender(scale);
            GlStateManager.translate(0.0F, -12.0F*scale, 0.125F);
            this.modelElytra.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entitylivingbaseIn);
            this.modelElytra.render(entitylivingbaseIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);

            if (itemstack.isItemEnchanted())
            {
                LayerArmorBase.renderEnchantedGlint(this.renderPlayer, entitylivingbaseIn, this.modelElytra, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale);
            }

            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}