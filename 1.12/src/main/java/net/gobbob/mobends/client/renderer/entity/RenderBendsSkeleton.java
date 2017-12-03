package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.client.model.entity.ModelBendsBipedArmor;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.model.entity.ModelBendsZombie;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsCustomHead;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.gobbob.mobends.main.ModBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsSkeleton extends RenderBiped<AbstractSkeleton>
{
	private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    public int refreshModel = 0;
    
    public RenderBendsSkeleton(RenderManager renderManagerIn)
    {
    	super(renderManagerIn, new ModelBendsSkeleton(), 0.5F);
    	this.layerRenderers.clear();
    	this.addLayer(new LayerBendsHeldItem(this));
        this.addLayer(new LayerBendsBipedArmor(this));
        this.addLayer(new LayerBendsCustomHead(((ModelBendsSkeleton)this.getMainModel()).bipedHead));
    }
    
    protected void preRenderCallback(EntitySkeleton entitylivingbaseIn, float partialTickTime)
    {
    	if(this.refreshModel != ModBase.refreshModel){
    		this.refreshModel = ModBase.refreshModel;
    		//this.mainModel = new ModelBendsSkeleton();
    		//this.modelBipedMain = (ModelBiped) this.mainModel;
    	}
        
        ((ModelBendsSkeleton)this.mainModel).updateWithEntityData(entitylivingbaseIn);
        ((ModelBendsSkeleton)this.mainModel).postRenderTranslate(0.0625f);
        
        ((ModelBendsSkeleton)this.mainModel).postRenderRotate(0.0625f);
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }

    /**
     * Returns the location of an entity's texture. Doesn't seem to be called unless you call Render.bindEntityTexture.
     */
    protected ResourceLocation getEntityTexture(AbstractSkeleton entity)
    {
        return SKELETON_TEXTURES;
    }
}