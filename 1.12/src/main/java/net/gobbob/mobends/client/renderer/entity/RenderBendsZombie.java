package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBipedCustomArmor;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
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
        this.addLayer(new LayerBipedCustomArmor(this));
        // this.addLayer(new LayerBendsCustomHead(((ModelBendsZombie)this.getMainModel()).bipedHead));
    }

    protected ResourceLocation getEntityTexture(EntityZombie entity)
    {
        return ZOMBIE_TEXTURES;
    }
}