package net.gobbob.mobends.client.model.entity;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.IModelPart;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.client.model.ModelPartChildExtended;
import net.gobbob.mobends.client.model.ModelPartExtended;
import net.gobbob.mobends.data.DataPlayer;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

public class ModelBipedArmorM extends ModelBiped {
    public ModelPart bipedRightForeArm;
    public ModelPart bipedLeftForeArm;
    public ModelPart bipedRightForeLeg;
    public ModelPart bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    
    public ModelBipedArmorM(float modelSize)
    {
    	this(modelSize, 0);
    }
    
    public ModelBipedArmorM(float modelSize, float offset)
    {
        super(modelSize, offset, 64, 32);
        
        this.textureWidth = 64;
        this.textureHeight = 32;
        
        this.bipedHeadwear = new ModelPart(this, 32, 0).setPosition(0.0F, 0.0F, 0.0F);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize + 0.5F);
        this.bipedBody = new ModelPart(this, 16, 16).setPosition(0.0F, 12.0F, 0.0F);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, modelSize);
        this.bipedHead = new ModelPartChild(this, 0, 0).setParent((IModelPart) this.bipedBody).setPosition(0.0F, -12.0F, 0.0F);;
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, modelSize);
        
        // Arms
        int armWidth = 4;
        float armHeight = -10.0F;
        this.bipedLeftArm = new ModelPartChildExtended(this, 40, 16)
        		.setParent((IModelPart) this.bipedBody).setHideLikeParent(true)
        		.setPosition(5.0F, armHeight, 0.0F);
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, armWidth, 6, 4, modelSize);
        this.bipedRightArm = new ModelPartChildExtended(this, 40, 16)
        		.setParent((IModelPart) this.bipedBody).setHideLikeParent(true)
        		.setPosition(-5.0F, armHeight, 0.0F);
        this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 6, 4, modelSize);
        ((ModelPart)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
        ((ModelPart)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
        
        this.bipedLeftForeArm = new ModelPart(this, 40, 16+6).setPosition(0.0F, 4.0F, 2.0F);;
        this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, armWidth, 6, 4, modelSize);
        this.bipedRightForeArm = new ModelPart(this, 40, 16+6).setPosition(0.0F, 4.0F, 2.0F);
        this.bipedRightForeArm.addBox(-2.0F, 0.0F, -4.0F, armWidth, 6, 4, modelSize);
        
        ((ModelPart)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm, ModelBox.TOP, -4.0f, -6.0f);
        ((ModelPart)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm, ModelBox.TOP, -4.0f, -6.0f);
        
        // Legs
        this.bipedRightLeg = new ModelPartExtended(this, 0, 16).setPosition(-1.9F, 12.0F, 0.0F);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, modelSize);
        this.bipedLeftLeg = new ModelPartExtended(this, 0, 16).setPosition(1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, modelSize);
        this.bipedLeftLeg.mirror = true;
        
        this.bipedRightForeLeg = new ModelPart(this, 0, 16+6).setPosition(0, 6.0F, -2.0F);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        this.bipedLeftForeLeg = new ModelPart(this, 0, 16+6).setPosition(0, 6.0F, -2.0F);
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, modelSize);
        ((ModelPart)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg, ModelBox.BOTTOM, 0, -6.0f);
        ((ModelPart)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float scale)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, scale, argEntity);
        
        GL11.glPushMatrix();
        if (this.isChild)
        {
            float f6 = 2.0F;
            GL11.glPushMatrix();
            GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
            GL11.glTranslatef(0.0F, 16.0F * scale, 0.0F);
            this.bipedHead.render(scale);
            GL11.glPopMatrix();
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
        	this.bipedHead.render(scale);
            this.bipedBody.render(scale);
            this.bipedRightLeg.render(scale);
            this.bipedLeftLeg.render(scale);
            this.bipedRightArm.render(scale);
            this.bipedLeftArm.render(scale);
        }
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float swingTime, float swingAmount, float armSway, float headYaw, float headPitch, float argNr6, Entity entity)
    {
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.armSwing = swingTime;
    	this.armSwingAmount = swingAmount;
    	this.headRotationX = headPitch;
    	this.headRotationY = headYaw;
    	
    	if(entity instanceof EntityPlayer) {
	    	DataPlayer data = (DataPlayer) EntityData.get(EntityData.PLAYER_DATA, entity.getEntityId());
	    	((ModelPart) this.bipedHead).syncUp(data.head);
	    	((ModelPart) this.bipedHeadwear).syncUp(data.headwear);
	    	((ModelPart) this.bipedBody).syncUp(data.body);
	    	((ModelPart) this.bipedRightArm).syncUp(data.rightArm);
	    	((ModelPart) this.bipedLeftArm).syncUp(data.leftArm);
	    	((ModelPart) this.bipedRightLeg).syncUp(data.rightLeg);
	    	((ModelPart) this.bipedLeftLeg).syncUp(data.leftLeg);
	    	((ModelPart) this.bipedRightForeArm).syncUp(data.rightForeArm);
	    	((ModelPart) this.bipedLeftForeArm).syncUp(data.leftForeArm);
	    	((ModelPart) this.bipedRightForeLeg).syncUp(data.rightForeLeg);
	    	((ModelPart) this.bipedLeftForeLeg).syncUp(data.leftForeLeg);
	    	
	    	/*this.renderOffset.set(data.renderOffset);
	    	this.renderRotation.set(data.renderRotation);
	    	this.renderRightItemRotation.set(data.renderRightItemRotation);
	    	this.renderLeftItemRotation.set(data.renderLeftItemRotation);*/
    	}else if(entity instanceof EntityZombie) {
    		Data_Zombie data = (Data_Zombie) EntityData.get(EntityData.ZOMBIE_DATA, entity.getEntityId());
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
    	}else if(entity instanceof EntitySkeleton) {
    		Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, entity.getEntityId());
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
    	}
    	
    	if(this.isRenderedInGui()){
    		((ModelPart) this.bipedHead).rotation.setY(this.headRotationY);
    		((ModelPart) this.bipedHead).rotation.setX(this.headRotationX);
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
	
	@Override
	public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        this.bipedLeftForeArm.showModel = visible;
        this.bipedRightForeArm.showModel = visible;
        this.bipedLeftForeLeg.showModel = visible;
        this.bipedRightForeLeg.showModel = visible;
    }
	
	public boolean isRenderedInGui() {
		return EventHandlerRenderPlayer.renderingGuiScreen;
	}
}
