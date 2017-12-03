package net.gobbob.mobends.client.model.entity;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.model.ModelBoxBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.event.EventHandler_RenderPlayer;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelBendsSkeleton extends ModelBiped
{
	public ModelRendererBends bipedRightForeArm;
    public ModelRendererBends bipedLeftForeArm;
    public ModelRendererBends bipedRightForeLeg;
    public ModelRendererBends bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    public EnumHandSide primaryHand = EnumHandSide.RIGHT;
	
    public ModelBendsSkeleton()
    {
        this(0.0F, false);
    }
    
    public ModelBendsSkeleton(float p_i1168_1_, boolean p_i1168_2_)
    {
        this(p_i1168_1_, 0.0F, 64, 32);
    }
    
    protected ModelBendsSkeleton(float p_i1167_1_, float p_i1167_2_, int p_i1167_3_, int p_i1167_4_)
    {
    	this.textureWidth = p_i1167_3_;
        this.textureHeight = p_i1167_4_;
        this.bipedHead = new ModelRendererBends(this, 0, 0);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1167_1_);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1167_2_-12, 0.0F);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1167_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16).setShowChildIfHidden(true);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i1167_1_);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + p_i1167_2_ + 12, 0.0F);
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody);
        this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, p_i1167_1_);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, p_i1167_1_);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, p_i1167_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F-1, 12.0F + p_i1167_2_, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, p_i1167_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F+1, 12.0F + p_i1167_2_, 0.0F);
        
        this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
        this.bipedRightForeArm.addBox(0, 0, -2.0f, 2, 6, 2, p_i1167_1_);
        this.bipedRightForeArm.setRotationPoint(-2.0f+1.0f, 4.0f, 1.0F);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeArm = new ModelRendererBends(this, 40, 16+6);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0, 0, -2.0f, 2, 6, 2, p_i1167_1_);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 1.0F);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
    
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, p_i1167_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -1.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, p_i1167_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -1.0F);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        
        this.bipedHead.addChild(this.bipedHeadwear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends) this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends) this.bipedLeftForeArm);
        
        ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);

        if (this.isChild)
        {
            float f6 = 2.0F;
            ((ModelRendererBends)this.bipedHead).scaleX*=1.5f;
            ((ModelRendererBends)this.bipedHead).scaleY*=1.5f;
            ((ModelRendererBends)this.bipedHead).scaleZ*=1.5f;
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * p_78088_7_, 0.0F);
            this.bipedBody.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
            GL11.glPopMatrix();
        }
        else
        {
            this.bipedBody.render(p_78088_7_);
            this.bipedRightLeg.render(p_78088_7_);
            this.bipedLeftLeg.render(p_78088_7_);
        }
    }
    
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime)
    {
        this.rightArmPose = ModelBiped.ArmPose.EMPTY;
        this.leftArmPose = ModelBiped.ArmPose.EMPTY;
        ItemStack itemstack = entitylivingbaseIn.getHeldItem(EnumHand.MAIN_HAND);

        if (itemstack != null && itemstack.getItem() == Items.BOW && ((EntitySkeleton)entitylivingbaseIn).isSwingingArms())
        {
            if (entitylivingbaseIn.getPrimaryHand() == EnumHandSide.RIGHT)
            {
                this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
            else
            {
                this.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            }
        }

        super.setLivingAnimations(entitylivingbaseIn, p_78086_2_, p_78086_3_, partialTickTime);
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, argEntity.getEntityId());
    	AnimatedEntity aEntity = AnimatedEntity.getByEntity(argEntity);
    	EntitySkeleton skeleton = (EntitySkeleton) argEntity;
    	
    	if(Minecraft.getMinecraft().theWorld == null)
    		return;
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	this.primaryHand = ((EntitySkeleton)argEntity).getPrimaryHand();
    	
    	((ModelRendererBends) this.bipedHead).sync(data.head);
    	((ModelRendererBends) this.bipedHeadwear).sync(data.headwear);
    	((ModelRendererBends) this.bipedBody).sync(data.body);
    	((ModelRendererBends) this.bipedRightArm).sync(data.rightArm);
    	((ModelRendererBends) this.bipedLeftArm).sync(data.leftArm);
    	((ModelRendererBends) this.bipedRightLeg).sync(data.rightLeg);
    	((ModelRendererBends) this.bipedLeftLeg).sync(data.leftLeg);
    	((ModelRendererBends) this.bipedRightForeArm).sync(data.rightForeArm);
    	((ModelRendererBends) this.bipedLeftForeArm).sync(data.leftForeArm);
    	((ModelRendererBends) this.bipedRightForeLeg).sync(data.rightForeLeg);
    	((ModelRendererBends) this.bipedLeftForeLeg).sync(data.leftForeLeg);
    	
    	this.renderOffset.set(data.renderOffset);
    	this.renderRotation.set(data.renderRotation);
    	this.renderRightItemRotation.set(data.renderRightItemRotation);
    	this.renderLeftItemRotation.set(data.renderLeftItemRotation);
    	this.swordTrail = data.swordTrail;
    	
    	if(data.canBeUpdated()){
    		this.renderOffset.setSmooth(new Vector3f(0,-1f,0),0.5f);
    		this.renderRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    		this.renderRightItemRotation.setSmooth(new Vector3f(0,0,0),0.5f);
        	this.renderLeftItemRotation.setSmooth(new Vector3f(0,0,0),0.5f);
    		
    		((ModelRendererBends) this.bipedHead).resetScale();
    		((ModelRendererBends) this.bipedHeadwear).resetScale();
    		((ModelRendererBends) this.bipedBody).resetScale();
    		((ModelRendererBends) this.bipedRightArm).resetScale();
    		((ModelRendererBends) this.bipedLeftArm).resetScale();
    		((ModelRendererBends) this.bipedRightLeg).resetScale();
    		((ModelRendererBends) this.bipedLeftLeg).resetScale();
    		((ModelRendererBends) this.bipedRightForeArm).resetScale();
    		((ModelRendererBends) this.bipedLeftForeArm).resetScale();
    		((ModelRendererBends) this.bipedRightForeLeg).resetScale();
    		((ModelRendererBends) this.bipedLeftForeLeg).resetScale();
    		
    		BendsVar.tempData = data;
    		
    		this.animate(aEntity, skeleton, data);
    		
    		((ModelRendererBends) this.bipedHead).update(data.ticksPerFrame);
    		((ModelRendererBends) this.bipedHeadwear).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedBody).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedLeftArm).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedRightArm).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedLeftLeg).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedRightLeg).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedLeftForeArm).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedRightForeArm).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedLeftForeLeg).update(data.ticksPerFrame);
		    ((ModelRendererBends) this.bipedRightForeLeg).update(data.ticksPerFrame);
		    
		    this.renderOffset.update(data.ticksPerFrame);
		    this.renderRotation.update(data.ticksPerFrame);
		    this.renderRightItemRotation.update(data.ticksPerFrame);
			this.renderLeftItemRotation.update(data.ticksPerFrame);
			
			this.swordTrail.update(data.ticksPerFrame);
			
		    data.updatedThisFrame = true;
		    if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
			}
    	}
    	
    	if(!data.isInitialized()){
    		this.animate(aEntity, skeleton, data);
    		data.syncModelInfo(this);
    		data.initModelPose();
    	}
    }
    
    public void animate(AnimatedEntity aEntity, EntitySkeleton entity, Data_Skeleton data) {
    	String bendsTargetName = entity.getSkeletonType() == 1 ? "witherSkeleton" : "skeleton";
		if(data.motion.x == 0.0f & data.motion.z == 0.0f){
			aEntity.get("stand").animate(entity, this, data);
			BendsPack.animate(this,bendsTargetName,"stand");
		}else{
			aEntity.get("walk").animate(entity, this, data);
			BendsPack.animate(this,bendsTargetName,"walk");
		}
		
		if(this.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW || this.leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			aEntity.get("bow").animate(entity, this, data);
			BendsPack.animate(this,bendsTargetName,"bow");
		}else{
    		if(this.swingProgress > 0.0F) {
    			aEntity.get("attack").animate(entity, this, data);
				BendsPack.animate(this,bendsTargetName,"attack");
    		}
		}
    }
    
    public void postRender(float argScale){
    	this.postRenderTranslate(argScale);
    	this.postRenderRotate(argScale);
    }
    
    public void postRenderTranslate(float argScale){
    	GlStateManager.translate(this.renderOffset.vSmooth.x*argScale,-this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
    }
    
    public void postRenderRotate(float argScale){
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
	
	public void updateWithEntityData(EntitySkeleton entitySkeleton){
		Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, entitySkeleton.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
			this.renderRightItemRotation.set(data.renderRightItemRotation);
			this.renderLeftItemRotation.set(data.renderLeftItemRotation);
		}
	}
	
	public boolean isRenderedInGui() {
		return EventHandler_RenderPlayer.renderingGuiScreen;
	}
}