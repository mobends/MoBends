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
public class RenderBendsZombie extends RenderBiped<EntityZombie>
{
    private static final ResourceLocation ZOMBIE_TEXTURES = new ResourceLocation("textures/entity/zombie/zombie.png");

    public RenderBendsZombie(RenderManager renderManagerIn)
    {
        super(renderManagerIn, new ModelBendsZombie(), 0.5F);
        this.layerRenderers.clear();
        this.addLayer(new LayerBendsHeldItem(this));
        this.addLayer(new LayerBendsBipedArmor(this));
        this.addLayer(new LayerBendsCustomHead(((ModelBendsZombie)this.getMainModel()).bipedHead));
    }

    protected ResourceLocation getEntityTexture(EntityZombie entity)
    {
        return ZOMBIE_TEXTURES;
    }
}