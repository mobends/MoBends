package net.gobbob.mobends.client.renderer.entity;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.MoBends;
import net.gobbob.mobends.client.model.entity.ModelBendsBipedArmor;
import net.gobbob.mobends.client.model.entity.ModelBendsPlayer;
import net.gobbob.mobends.client.model.entity.ModelBendsSkeleton;
import net.gobbob.mobends.client.model.entity.ModelBendsSpider;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsBipedArmor;
import net.gobbob.mobends.client.renderer.entity.layers.LayerBendsHeldItem;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.settings.SettingsBoolean;
import net.gobbob.mobends.settings.SettingsNode;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderBendsSkeleton extends RenderBiped<EntitySkeleton>
{
	private static final ResourceLocation skeletonTextures = new ResourceLocation("textures/entity/skeleton/skeleton.png");
    private static final ResourceLocation witherSkeletonTextures = new ResourceLocation("textures/entity/skeleton/wither_skeleton.png");
    
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
    }

    protected ResourceLocation getEntityTexture(EntitySkeleton entity)
    {
        return entity.getSkeletonType() == 1 ? witherSkeletonTextures : skeletonTextures;
    }
    
    protected void preRenderCallback(EntitySkeleton entitylivingbaseIn, float partialTickTime)
    {
    	if(this.refreshModel != MoBends.refreshModel){
    		this.refreshModel = MoBends.refreshModel;
    		//this.mainModel = new ModelBendsSkeleton();
    		//this.modelBipedMain = (ModelBiped) this.mainModel;
    	}
    	
        if (entitylivingbaseIn.getSkeletonType() == 1)
        {
            GlStateManager.scale(1.2F, 1.2F, 1.2F);
        }
        
        ((ModelBendsSkeleton)this.mainModel).updateWithEntityData(entitylivingbaseIn);
        ((ModelBendsSkeleton)this.mainModel).postRenderTranslate(0.0625f);
        
        Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, entitylivingbaseIn.getEntityId());
        
        if(((SettingsBoolean)SettingsNode.getSetting("swordTrail")).data){
			GL11.glPushMatrix();
				float f5 = 0.0625F;
				GL11.glScalef(-f5, -f5, f5);
				data.swordTrail.render();
				GL11.glColor4f(1,1,1,1);
			GL11.glPopMatrix();
        }
        
        
        ((ModelBendsSkeleton)this.mainModel).postRenderRotate(0.0625f);
    }

    public void transformHeldFull3DItemLayer()
    {
        GlStateManager.translate(0.09375F, 0.1875F, 0.0F);
    }
}