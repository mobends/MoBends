package goblinbob.mobends.standard.client.renderer.entity.layers;

import goblinbob.mobends.core.util.BenderHelper;
import goblinbob.mobends.standard.client.renderer.entity.BendsCapeRenderer;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EnumPlayerModelParts;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
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
        final PlayerData data = BenderHelper.getData(player, playerRenderer);
        assert data != null;

        if (player.hasPlayerInfo() && !player.isInvisible() && player.isWearing(EnumPlayerModelParts.CAPE) && player.getLocationCape() != null)
        {
            final ItemStack itemstack = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            if (itemstack.getItem() != Items.ELYTRA)
            {
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                this.playerRenderer.bindTexture(player.getLocationCape());
                GlStateManager.pushMatrix();

                if (player.isSneaking())
                {
                    if (player.capabilities.isFlying)
                    {
                        GlStateManager.translate(0F, 4F * scale, 0F);
                    }
                    else
                    {
                        GlStateManager.translate(0F, 4F * scale, 0F);
                    }
                }

                data.body.applyLocalTransform(0.0625F);
                GlStateManager.translate(0.0F, -12.0F * scale, 2.2F * scale);
                data.cape.applyLocalTransform(0.0625F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);

                capeRenderer.applyAnimation(data);
                capeRenderer.render(0.0625F);

                GlStateManager.popMatrix();
            }
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
