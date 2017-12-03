package net.gobbob.mobends.client.renderer.entity;

import com.google.common.collect.Lists;

import java.util.List;

import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.model.entity.ModelBendsZombieVillager;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsCustomHead;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelZombie;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.LayerVillagerArmor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsHusk extends RenderBendsZombie
{
	private static final ResourceLocation HUSK_ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/husk.png");

    public RenderBendsHusk(RenderManager p_i47204_1_)
    {
        super(p_i47204_1_);
    }
    
    protected void preRenderCallback(EntityZombie entitylivingbaseIn, float partialTickTime)
    {
        float f = 1.0625F;
        GlStateManager.scale(1.0625F, 1.0625F, 1.0625F);
        super.preRenderCallback(entitylivingbaseIn, partialTickTime);
    }
    
    protected ResourceLocation getEntityTexture(EntityZombie entity)
    {
        return HUSK_ZOMBIE_TEXTURES;
    }
}