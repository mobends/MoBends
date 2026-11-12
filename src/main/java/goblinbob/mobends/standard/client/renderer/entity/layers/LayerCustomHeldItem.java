package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.compat.Living;
import goblinbob.mobends.core.data.EntityData;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.util.GlHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import goblinbob.mobends.core.compat.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCustomHeldItem implements LayerRenderer<EntityLivingBase>
{

    protected final RendererLivingEntity<?> livingEntityRenderer;

    public LayerCustomHeldItem(RendererLivingEntity<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    /** As 1.8.9's LayerHeldItem, but the item follows the animated hand (see {@link #translateToHand}). */
    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getHeldItem();

        if (!Living.isEmpty(itemstack))
        {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild)
            {
                GlStateManager.translate(0.0F, 0.625F, 0.0F);
                GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }

            // As Forge does in later versions: before the hand, so the item follows the model's sneak offset.
            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }
            this.translateToHand(EnumHandSide.RIGHT, entitylivingbaseIn);
            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer) entitylivingbaseIn).fishEntity != null)
            {
                // The cast rod.
                itemstack = new ItemStack(Items.fishing_rod, 0);
            }

            Item item = itemstack.getItem();
            if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2)
            {
                GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                final float blockScale = 0.375F;
                GlStateManager.scale(-blockScale, -blockScale, blockScale);
            }

            Minecraft.getMinecraft().getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
    }

    /**
     * MO BENDS
     * This is the only function that had it's code changed
     */
    protected void translateToHand(EnumHandSide handSide, EntityLivingBase entity)
    {
    	((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
    	
    	EntityData<?> entityData = EntityDatabase.instance.get(entity);
    	// How the item turns in the hand: the entity's rightHeldItem or leftHeldItem, if it has one.
    	Object itemRotation = entityData == null ? null : entityData.getPartForName(handSide == EnumHandSide.RIGHT ? "rightHeldItem" : "leftHeldItem");
    	if (itemRotation instanceof SmoothOrientation)
    	{
    		GlStateManager.translate(0, 8F * 0.0625F, 0);
    		GlHelper.rotate(((SmoothOrientation) itemRotation).getSmooth());
            GlStateManager.translate(0, -8F * 0.0625F, 0);
    	}
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

}