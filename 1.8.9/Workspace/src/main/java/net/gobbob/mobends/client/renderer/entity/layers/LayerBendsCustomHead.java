package net.gobbob.mobends.client.renderer.entity.layers;

import com.mojang.authlib.GameProfile;

import java.util.UUID;

import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySkullRenderer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerBendsCustomHead implements LayerRenderer
{
    private final ModelBendsPlayer model;
    
    public LayerBendsCustomHead(ModelBendsPlayer p_i46120_1_)
    {
        this.model = p_i46120_1_;
    }

    public void doRenderLayer(EntityLivingBase p_177141_1_, float p_177141_2_, float p_177141_3_, float p_177141_4_, float p_177141_5_, float p_177141_6_, float p_177141_7_, float p_177141_8_)
    {
        ItemStack itemstack = p_177141_1_.getCurrentArmor(3);

        if (itemstack != null && itemstack.getItem() != null)
        {
            Item item = itemstack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();
            GlStateManager.pushMatrix();

            if (p_177141_1_.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.2F, 0.0F);
            }

            boolean flag = p_177141_1_ instanceof EntityVillager || p_177141_1_ instanceof EntityZombie && ((EntityZombie)p_177141_1_).isVillager();
            float f7;

            if (!flag && p_177141_1_.isChild())
            {
                f7 = 2.0F;
                float f8 = 1.4F;
                GlStateManager.scale(f8 / f7, f8 / f7, f8 / f7);
                GlStateManager.translate(0.0F, 16.0F * p_177141_8_, 0.0F);
            }

            this.model.bipedBody.postRender(0.0625F);
            this.model.bipedHead.postRender(0.0625F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            if (item instanceof ItemBlock)
            {
                f7 = 0.625F;
                GlStateManager.translate(0.0F, -0.25F, 0.0F);
                GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
                GlStateManager.scale(f7, -f7, -f7);

                if (flag)
                {
                    GlStateManager.translate(0.0F, 0.1875F, 0.0F);
                }

                minecraft.getItemRenderer().renderItem(p_177141_1_, itemstack, ItemCameraTransforms.TransformType.HEAD);
            }
            else if (item == Items.skull)
            {
                f7 = 1.1875F;
                GlStateManager.scale(f7, -f7, -f7);

                if (flag)
                {
                    GlStateManager.translate(0.0F, 0.0625F, 0.0F);
                }

                GameProfile gameprofile = null;

                if (itemstack.hasTagCompound())
                {
                    NBTTagCompound nbttagcompound = itemstack.getTagCompound();

                    if (nbttagcompound.hasKey("SkullOwner", 10))
                    {
                        gameprofile = NBTUtil.readGameProfileFromNBT(nbttagcompound.getCompoundTag("SkullOwner"));
                    }
                    else if (nbttagcompound.hasKey("SkullOwner", 8))
                    {
                        gameprofile = TileEntitySkull.updateGameprofile(new GameProfile((UUID)null, nbttagcompound.getString("SkullOwner")));
                        nbttagcompound.setTag("SkullOwner", NBTUtil.writeGameProfile(new NBTTagCompound(), gameprofile));
                    }
                }

                TileEntitySkullRenderer.instance.renderSkull(-0.5F, 0.0F, -0.5F, EnumFacing.UP, 180.0F, itemstack.getMetadata(), gameprofile, -1);
            }

            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return true;
    }
}