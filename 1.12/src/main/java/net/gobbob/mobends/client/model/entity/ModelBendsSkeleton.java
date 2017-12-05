package net.gobbob.mobends.client.model.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;

public class ModelBendsSkeleton extends ModelBiped implements IBendsModel
{
	public ModelPart bipedRightForeArm;
    public ModelPart bipedLeftForeArm;
    public ModelPart bipedRightForeLeg;
    public ModelPart bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    public EnumHandSide primaryHand = EnumHandSide.RIGHT;
	
    private HashMap nameToRendererMap;
    
    public ModelBendsSkeleton()
    {
        this(0.0F, false);
    }
    
    public ModelBendsSkeleton(float p_i1168_1_, boolean p_i1168_2_)
    {
        this(p_i1168_1_, 0.0F, 64, 32);
    }
    
    protected ModelBendsSkeleton(float scaleFactor, float p_i1167_2_, int textureWidth, int textureHeight)
    {
    	this.textureWidth = textureWidth;
        this.textureHeight = textureHeight;
        this.bipedBody = new ModelPart(this, 16, 16);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, scaleFactor);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + p_i1167_2_ + 12, 0.0F);
        this.bipedHead = new ModelPartChild(this, 0, 0).setParent((ModelPart) this.bipedBody);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1167_2_-12, 0.0F);
        this.bipedHeadwear = new ModelPart(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, scaleFactor + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedRightArm = new ModelPartChild(this, 40, 16).setParent((ModelPart) this.bipedBody);
        this.bipedRightArm.addBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedLeftArm = new ModelPartChild(this, 40, 16).setParent((ModelPart) this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -1.0F, 2, 6, 2, scaleFactor);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedRightLeg = new ModelPart(this, 0, 16);
        this.bipedRightLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);
        this.bipedRightLeg.setRotationPoint(-1.9F-1, 12.0F + p_i1167_2_, 0.0F);
        this.bipedLeftLeg = new ModelPart(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-1.0F, 0.0F, -1.0F, 2, 6, 2, scaleFactor);
        this.bipedLeftLeg.setRotationPoint(1.9F+1, 12.0F + p_i1167_2_, 0.0F);
        
        this.bipedRightForeArm = new ModelPart(this, 40, 16+6);
        this.bipedRightForeArm.addBox(0, 0, -2.0f, 2, 6, 2, scaleFactor);
        this.bipedRightForeArm.setRotationPoint(-2.0f+1.0f, 4.0f, 1.0F);
        ((ModelPart)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBox.BOTTOM, 0, -6.0f);
        this.bipedLeftForeArm = new ModelPart(this, 40, 16+6);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0, 0, -2.0f, 2, 6, 2, scaleFactor);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 1.0F);
        ((ModelPart)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBox.BOTTOM, 0, -6.0f);
    
        this.bipedRightForeLeg = new ModelPart(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -1.0F);
        ((ModelPart)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBox.BOTTOM, 0, -6.0f);
        
        this.bipedLeftForeLeg = new ModelPart(this, 0, 16+6);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-1.0F, 0.0F, 0.0F, 2, 6, 2, scaleFactor);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -1.0F);
        ((ModelPart)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBox.BOTTOM, 0, -6.0f);
        
        this.bipedHead.addChild(this.bipedHeadwear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        
        ((ModelPart)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelPart)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelPart)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
        ((ModelPart)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(2.02f, 6.0f, 2.02f).updateVertices();
    
        nameToRendererMap = new HashMap<String, Object>();
        nameToRendererMap.put("head", bipedHead);
        nameToRendererMap.put("body", bipedBody);
        nameToRendererMap.put("leftArm", bipedLeftArm);
        nameToRendererMap.put("rightArm", bipedRightArm);
        nameToRendererMap.put("leftForeArm", bipedLeftForeArm);
        nameToRendererMap.put("rightForeArm", bipedRightForeArm);
        nameToRendererMap.put("leftLeg", bipedLeftLeg);
        nameToRendererMap.put("rightLeg", bipedRightLeg);
        nameToRendererMap.put("leftForeLeg", bipedLeftForeLeg);
        nameToRendererMap.put("rightForeLeg", bipedRightForeLeg);
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, argEntity);

        if (this.isChild)
        {
            float f6 = 2.0F;
            ((ModelPart)this.bipedHead).scale.x*=1.5f;
            ((ModelPart)this.bipedHead).scale.y*=1.5f;
            ((ModelPart)this.bipedHead).scale.z*=1.5f;
            GL11.glPushMatrix();
            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
            GL11.glTranslatef(0.0F, 24.0F * scale, 0.0F);
            this.bipedBody.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            GL11.glPopMatrix();
        }
        else
        {
            this.bipedBody.render(scale);
            this.bipedHead.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
        }
    }
    
    public void setLivingAnimations(EntityLivingBase entitylivingbaseIn, float p_78086_2_, float p_78086_3_, float partialTickTime)
    {
        this.rightArmPose = ModelBiped.ArmPose.EMPTY;
        this.leftArmPose = ModelBiped.ArmPose.EMPTY;
        ItemStack itemstack = entitylivingbaseIn.getHeldItem(EnumHand.MAIN_HAND);

        if (itemstack != null && itemstack.getItem() == Items.BOW && ((AbstractSkeleton)entitylivingbaseIn).isSwingingArms())
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
    	AbstractSkeleton skeleton = (AbstractSkeleton) argEntity;
    	
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	this.primaryHand = skeleton.getPrimaryHand();
    	
    	/*((ModelPart) this.bipedHead).sync(data.head);
    	((ModelPart) this.bipedHeadwear).sync(data.headwear);
    	((ModelPart) this.bipedBody).sync(data.body);
    	((ModelPart) this.bipedRightArm).sync(data.rightArm);
    	((ModelPart) this.bipedLeftArm).sync(data.leftArm);
    	((ModelPart) this.bipedRightLeg).sync(data.rightLeg);
    	((ModelPart) this.bipedLeftLeg).sync(data.leftLeg);
    	((ModelPart) this.bipedRightForeArm).sync(data.rightForeArm);
    	((ModelPart) this.bipedLeftForeArm).sync(data.leftForeArm);
    	((ModelPart) this.bipedRightForeLeg).sync(data.rightForeLeg);
    	((ModelPart) this.bipedLeftForeLeg).sync(data.leftForeLeg);*/
    	
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
    		
    		((ModelPart) this.bipedHead).resetScale();
    		((ModelPart) this.bipedHeadwear).resetScale();
    		((ModelPart) this.bipedBody).resetScale();
    		((ModelPart) this.bipedRightArm).resetScale();
    		((ModelPart) this.bipedLeftArm).resetScale();
    		((ModelPart) this.bipedRightLeg).resetScale();
    		((ModelPart) this.bipedLeftLeg).resetScale();
    		((ModelPart) this.bipedRightForeArm).resetScale();
    		((ModelPart) this.bipedLeftForeArm).resetScale();
    		((ModelPart) this.bipedRightForeLeg).resetScale();
    		((ModelPart) this.bipedLeftForeLeg).resetScale();
    		
    		BendsVariable.tempData = data;
    		
    		this.animate(aEntity, skeleton, data);
    		
    		((ModelPart) this.bipedHead).update(DataUpdateHandler.ticksPerFrame);
    		((ModelPart) this.bipedHeadwear).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedBody).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedLeftArm).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedRightArm).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedLeftLeg).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedRightLeg).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedLeftForeArm).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedRightForeArm).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedLeftForeLeg).update(DataUpdateHandler.ticksPerFrame);
		    ((ModelPart) this.bipedRightForeLeg).update(DataUpdateHandler.ticksPerFrame);
		    
		    this.renderOffset.update(DataUpdateHandler.ticksPerFrame);
		    this.renderRotation.update(DataUpdateHandler.ticksPerFrame);
		    this.renderRightItemRotation.update(DataUpdateHandler.ticksPerFrame);
			this.renderLeftItemRotation.update(DataUpdateHandler.ticksPerFrame);
			
			this.swordTrail.update(DataUpdateHandler.ticksPerFrame);
			
		    /*data.updatedThisFrame = true;
		    if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
			}*/
    	}
    	
    	if(!data.isInitialized()){
    		this.animate(aEntity, skeleton, data);
    		data.syncModelInfo(this);
    		data.initModelPose();
    	}
    }
    
    public void animate(AnimatedEntity aEntity, AbstractSkeleton entity, Data_Skeleton data) {
		/*if(data.motion.x == 0.0f & data.motion.z == 0.0f){
			aEntity.getAnimation("stand").animate(entity, this, data);
			BendsPack.animate(this, aEntity.getName(), "stand");
		}else{
			aEntity.getAnimation("walk").animate(entity, this, data);
			BendsPack.animate(this, aEntity.getName(), "walk");
		}
		
		if(this.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW || this.leftArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			aEntity.getAnimation("bow").animate(entity, this, data);
			BendsPack.animate(this, aEntity.getName(), "bow");
		}else{
    		if(this.swingProgress > 0.0F) {
				BendsPack.animate(this, aEntity.getName(), "attack");
    		}
		}*/
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
	
	public void updateWithEntityData(AbstractSkeleton entitySkeleton){
		Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, entitySkeleton.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
			this.renderRightItemRotation.set(data.renderRightItemRotation);
			this.renderLeftItemRotation.set(data.renderLeftItemRotation);
		}
	}
	
	public boolean isRenderedInGui() {
		return EventHandlerRenderPlayer.renderingGuiScreen;
	}

	@Override
	public Object getPartForName(String name) {
		return nameToRendererMap.get(name);
	}
}