package net.gobbob.mobends.client.renderer.entity;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.model.entity.ModelBendsBipedArmor;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsSkeletonType;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.SkeletonType;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsSkeleton extends RenderBiped<EntitySkeleton>
{
	private static final ResourceLocation SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation WITHER_SKELETON_TEXTURES = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
    private static final ResourceLocation field_190084_m = new ResourceLocation("textures/entity/skeleton/stray.png");
    
    public int refreshModel = 0;
    
    public RenderBendsSkeleton(RenderManager renderManagerIn)
    {
    	super(renderManagerIn, new ModelBendsSkeleton(), 0.5F);
        this.addLayer(new LayerBendsHeldItem(this));
        this.addLayer(new LayerBendsBipedArmor(this)
        {
            protected void initArmor()
            {
                this.modelLeggings = new ModelBendsBipedArmor(0.5F, true, false);
                this.modelArmor = new ModelBendsBipedArmor(1.0F, true, false);
            }
        });
        this.addLayer(new LayerBendsSkeletonType(this));
    }

    protected ResourceLocation getEntityTexture(EntitySkeleton entity)
    {
        SkeletonType skeletontype = entity.func_189771_df();
        return skeletontype == SkeletonType.WITHER ? WITHER_SKELETON_TEXTURES : (skeletontype == SkeletonType.STRAY ? field_190084_m : SKELETON_TEXTURES);
    }
    
    protected void preRenderCallback(EntitySkeleton entitylivingbaseIn, float partialTickTime)
    {
    	if(this.refreshModel != MoBends.refreshModel){
    		this.refreshModel = MoBends.refreshModel;
    		//this.mainModel = new ModelBendsSkeleton();
    		//this.modelBipedMain = (ModelBiped) this.mainModel;
    	}
    	
    	if (entitylivingbaseIn.func_189771_df() == SkeletonType.WITHER)
        {
            GlStateManager.scale(1.2F, 1.2F, 1.2F);
        }
        
        ((ModelBendsSkeleton)this.mainModel).updateWithEntityData(entitylivingbaseIn);
        ((ModelBendsSkeleton)this.mainModel).postRenderTranslate(0.0625f);
        
        ((ModelBendsSkeleton)this.mainModel).postRenderRotate(0.0625f);
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }
}