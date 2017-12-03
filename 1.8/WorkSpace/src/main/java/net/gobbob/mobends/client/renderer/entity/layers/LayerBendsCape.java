package net.gobbob.mobends.client.renderer.entity.layers;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.renderer.entity.RenderBendsPlayer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsCape implements LayerRenderer
{
    private final RenderBendsPlayer playerRenderer;
    private static final String __OBFID = "CL_00002425";

    public LayerBendsCape(RenderBendsPlayer p_i46123_1_)
    {
        this.playerRenderer = p_i46123_1_;
    }

    public void doRenderLayer(AbstractClientPlayer p_177166_1_, float p_177166_2_, float p_177166_3_, float p_177166_4_, float p_177166_5_, float p_177166_6_, float p_177166_7_, float p_177166_8_)
    {
        if (p_177166_1_.hasPlayerInfo() && !p_177166_1_.isInvisible() && p_177166_1_.func_175148_a(EnumPlayerModelParts.CAPE) && p_177166_1_.getLocationCape() != null)
        {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.playerRenderer.bindTexture(p_177166_1_.getLocationCape());
            GlStateManager.pushMatrix();
            this.playerRenderer.getPlayerModel().bipedBody.postRender(0.0625F);
            GlStateManager.translate(0.0F, -12.0F*0.0625F, 0.125F);
            double d0 = p_177166_1_.field_71091_bM + (p_177166_1_.field_71094_bP - p_177166_1_.field_71091_bM) * (double)p_177166_4_ - (p_177166_1_.prevPosX + (p_177166_1_.posX - p_177166_1_.prevPosX) * (double)p_177166_4_);
            double d1 = p_177166_1_.field_71096_bN + (p_177166_1_.field_71095_bQ - p_177166_1_.field_71096_bN) * (double)p_177166_4_ - (p_177166_1_.prevPosY + (p_177166_1_.posY - p_177166_1_.prevPosY) * (double)p_177166_4_);
            double d2 = p_177166_1_.field_71097_bO + (p_177166_1_.field_71085_bR - p_177166_1_.field_71097_bO) * (double)p_177166_4_ - (p_177166_1_.prevPosZ + (p_177166_1_.posZ - p_177166_1_.prevPosZ) * (double)p_177166_4_);
            float f7 = p_177166_1_.prevRenderYawOffset + (p_177166_1_.renderYawOffset - p_177166_1_.prevRenderYawOffset) * p_177166_4_;
            double d3 = (double)MathHelper.sin(f7 * (float)Math.PI / 180.0F);
            double d4 = (double)(-MathHelper.cos(f7 * (float)Math.PI / 180.0F));
            float f8 = (float)d1 * 10.0F;
            f8 = MathHelper.clamp_float(f8, -6.0F, 32.0F);
            float f9 = (float)(d0 * d3 + d2 * d4) * 100.0F;
            float f10 = (float)(d0 * d4 - d2 * d3) * 100.0F;

            if (f9 < 0.0F)
            {
                f9 = 0.0F;
            }

            float f11 = p_177166_1_.prevCameraYaw + (p_177166_1_.cameraYaw - p_177166_1_.prevCameraYaw) * p_177166_4_;
            f8 += MathHelper.sin((p_177166_1_.prevDistanceWalkedModified + (p_177166_1_.distanceWalkedModified - p_177166_1_.prevDistanceWalkedModified) * p_177166_4_) * 6.0F) * 32.0F * f11;

            if (p_177166_1_.isSneaking())
            {
                f8 += 25.0F;
            }

            GlStateManager.rotate(6.0F + f9 / 2.0F + f8, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(f10 / 2.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(-f10 / 2.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
            this.playerRenderer.getPlayerModel().func_178728_c(0.0625F);
            GlStateManager.popMatrix();
            
            GL11.glEnable(GL11.GL_TEXTURE_2D);
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }

    public void doRenderLayer(EntityLivingBase p_177141_1_, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_)
    {
        this.doRenderLayer((AbstractClientPlayer)p_177141_1_, p_177141_2_, p_177141_3_, p_177141_4_, p_177141_5_, p_177141_6_, p_177141_7_, p_177141_8_);
    }
}