package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.standard.client.renderer.entity.BendsCapeRenderer;
import goblinbob.mobends.standard.mutators.PlayerMutator;
import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCustomCape implements LayerRenderer<AbstractClientPlayer>
{

    private final RenderPlayer playerRenderer;
    private final BendsCapeRenderer capeRenderer;

    public LayerCustomCape(RenderPlayer playerRendererIn)
    {
        this.playerRenderer = playerRendererIn;
        this.capeRenderer = new BendsCapeRenderer();
    }

    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale)
    {
        final EntityBender<EntityLivingBase> entityBender = EntityBenderRegistry.instance.getForEntity(player);
        final PlayerMutator mutator = (PlayerMutator) entityBender.getMutator(playerRenderer);
        final PlayerData data = mutator.getData(player);

        //if (player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(EnumPlayerModelParts.CAPE) && player.getLocationCape() != null)
        {
            final ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            //if (itemstack.getItem() != Items.ELYTRA)
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                // this.playerRenderer.bindTexture(player.getLocationCape());
                GlStateManager.pushMatrix();

                if (player.isSneaking())
                {
                    if (player.capabilities.isFlying)
                    {
                        GlStateManager.translate(0F, 4F * scale, 0F);
                    }
                    else
                    {
                        GlStateManager.translate(0F, 5F * scale, 0F);
                    }
                }

                this.playerRenderer.getMainModel().bipedBody.postRender(0.0625F);
                GlStateManager.translate(0.0F, 0.0F, 0.125F);
                GlStateManager.translate(0.0F, 0.0F, 0.325F);
                double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * (double)partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * (double)partialTicks);
                double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * (double)partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * (double)partialTicks);
                double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * (double)partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double)partialTicks);
                float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
                double d3 = (double) MathHelper.sin(f * 0.017453292F);
                double d4 = (double)(-MathHelper.cos(f * 0.017453292F));
                float f1 = (float)d1 * 10.0F;
                f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
                float f2 = (float)(d0 * d3 + d2 * d4) * 100.0F;
                float f3 = (float)(d0 * d4 - d2 * d3) * 100.0F;

                if (f2 < 0.0F)
                {
                    f2 = 0.0F;
                }

                float f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
                f1 = f1 + MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

                if (player.isSneaking())
                {
                    f1 += 25.0F;
                }

                // GlStateManager.rotate(6.0F + f2 / 2.0F + f1, 1.0F, 0.0F, 0.0F);
                // GlStateManager.rotate(f3 / 2.0F, 0.0F, 0.0F, 1.0F);
                // GlStateManager.rotate(-f3 / 2.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.disableTexture2D();

                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                capeRenderer.applyAnimation(data);
                capeRenderer.render(0.0625F);
                GlStateManager.enableTexture2D();
                GlStateManager.popMatrix();
            }
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
