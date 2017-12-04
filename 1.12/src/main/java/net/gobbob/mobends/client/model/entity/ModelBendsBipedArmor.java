package net.gobbob.mobends.client.model.entity;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelBox;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.client.model.ModelPartChild;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.data.Data_Skeleton;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.EntityPlayer;

public class ModelBendsBipedArmor extends ModelPlayer{
    public ModelPart bipedRightForeArm;
    public ModelPart bipedLeftForeArm;
    public ModelPart bipedRightForeLeg;
    public ModelPart bipedLeftForeLeg;
    
    public ModelPart bipedCloak;
    public ModelPart bipedEars;
    public boolean smallArms;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderRightItemRotation = new SmoothVector3f();
    public SmoothVector3f renderLeftItemRotation = new SmoothVector3f();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    
    public ModelBendsBipedArmor(float p_i46304_1_, boolean p_i46304_2)
    {
    	this(p_i46304_1_, p_i46304_2, true);
    }
    
    public ModelBendsBipedArmor(float p_i46304_1_, boolean p_i46304_2_, boolean bigTexture)
    {
        super(p_i46304_1_, p_i46304_2_);
        
        this.textureWidth = 64;
        this.textureHeight = bigTexture ? 64 : 32;
        
        this.smallArms = p_i46304_2_;
        this.bipedEars = new ModelPart(this, 24, 0);
        this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        this.bipedCloak = new ModelPart(this, 0, 0);
        this.bipedCloak.setTextureSize(64, 32);
        this.bipedCloak.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);
        
        /*this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16).setHideLikeParent(true);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i46304_1_);
        this.bipedBody.setRotationPoint(0.0F, 12.0F, 0.0F);
        this.bipedHead = new ModelRendererBendsChild(this, 0, 0).setParent((ModelRendererBends) this.bipedBody);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_);
        this.bipedHead.setRotationPoint(0.0F, -12.0F, 0.0F);
        
        if (p_i46304_2_)
        {
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.5F-12.0f, 0.0F);
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedRightArm.addBox(-2.0F, -2.0F, -2.0F, 3, 6, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.5F-12.0f, 0.0F);
            ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
            ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(3.02f, 6.0f, 4.02f).updateVertices();
            
            this.bipedLeftForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_);
            this.bipedLeftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.TOP, -4.0f, -6.0f);
            this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedRightForeArm.addBox(-2.0F, 0.0F, -4.0F, 3, 6, 4, p_i46304_1_);
            this.bipedRightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.TOP, -4.0f, -6.0f);
        }
        else
        {
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F-12.0f, 0.0F);
            this.bipedLeftArm.mirror = true;
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setHideLikeParent(true);
            this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F-12.0f, 0.0F);
            ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
            ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
            
            this.bipedLeftForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.TOP, -4.0f, -6.0f);
            this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedRightForeArm.addBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.TOP, -4.0f, -6.0f);
        }
        
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg.mirror = true;
        
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedBodyWear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends) this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends) this.bipedLeftForeArm);
        
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();*/
    }
    
    public void render(Entity argEntity, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, argEntity);
        
        GL11.glPushMatrix();
	        if (this.isChild)
	        {
	            float f6 = 2.0F;
	            GL11.glPushMatrix();
	            GL11.glScalef(1.5F / f6, 1.5F / f6, 1.5F / f6);
	            GL11.glTranslatef(0.0F, 16.0F * p_78088_7_, 0.0F);
	            this.bipedHead.render(p_78088_7_);
	            GL11.glPopMatrix();
	            GL11.glPushMatrix();
	            GL11.glScalef(1.0F / f6, 1.0F / f6, 1.0F / f6);
	            GL11.glTranslatef(0.0F, 24.0F * p_78088_7_, 0.0F);
	            this.bipedBody.render(p_78088_7_);
	            this.bipedRightArm.render(p_78088_7_);
	            this.bipedLeftArm.render(p_78088_7_);
	            this.bipedRightLeg.render(p_78088_7_);
	            this.bipedLeftLeg.render(p_78088_7_);
	            this.bipedHeadwear.render(p_78088_7_);
	            GL11.glPopMatrix();
	        }
	        else
	        {
	            this.bipedBody.render(p_78088_7_);
	            this.bipedRightLeg.render(p_78088_7_);
	            this.bipedLeftLeg.render(p_78088_7_);
	        }
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	if(Minecraft.getMinecraft().world == null) return;
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	
    	if(argEntity instanceof EntityPlayer) {
	    	Data_Player data = (Data_Player) EntityData.get(EntityData.PLAYER_DATA, argEntity.getEntityId());
	    	((ModelPart) this.bipedHead).sync(data.head);
	    	((ModelPart) this.bipedHeadwear).sync(data.headwear);
	    	((ModelPart) this.bipedBody).sync(data.body);
	    	((ModelPart) this.bipedRightArm).sync(data.rightArm);
	    	((ModelPart) this.bipedLeftArm).sync(data.leftArm);
	    	((ModelPart) this.bipedRightLeg).sync(data.rightLeg);
	    	((ModelPart) this.bipedLeftLeg).sync(data.leftLeg);
	    	((ModelPart) this.bipedRightForeArm).sync(data.rightForeArm);
	    	((ModelPart) this.bipedLeftForeArm).sync(data.leftForeArm);
	    	((ModelPart) this.bipedRightForeLeg).sync(data.rightForeLeg);
	    	((ModelPart) this.bipedLeftForeLeg).sync(data.leftForeLeg);
	    	
	    	this.renderOffset.set(data.renderOffset);
	    	this.renderRotation.set(data.renderRotation);
	    	this.renderRightItemRotation.set(data.renderRightItemRotation);
	    	this.renderLeftItemRotation.set(data.renderLeftItemRotation);
    	}else if(argEntity instanceof EntityZombie) {
    		Data_Zombie data = (Data_Zombie) EntityData.get(EntityData.ZOMBIE_DATA, argEntity.getEntityId());
	    	((ModelPart) this.bipedHead).sync(data.head);
	    	((ModelPart) this.bipedHeadwear).sync(data.headwear);
	    	((ModelPart) this.bipedBody).sync(data.body);
	    	((ModelPart) this.bipedRightArm).sync(data.rightArm);
	    	((ModelPart) this.bipedLeftArm).sync(data.leftArm);
	    	((ModelPart) this.bipedRightLeg).sync(data.rightLeg);
	    	((ModelPart) this.bipedLeftLeg).sync(data.leftLeg);
	    	((ModelPart) this.bipedRightForeArm).sync(data.rightForeArm);
	    	((ModelPart) this.bipedLeftForeArm).sync(data.leftForeArm);
	    	((ModelPart) this.bipedRightForeLeg).sync(data.rightForeLeg);
	    	((ModelPart) this.bipedLeftForeLeg).sync(data.leftForeLeg);
	    	
	    	this.renderOffset.set(data.renderOffset);
	    	this.renderRotation.set(data.renderRotation);
    	}else if(argEntity instanceof EntitySkeleton) {
    		Data_Skeleton data = (Data_Skeleton) EntityData.get(EntityData.SKELETON_DATA, argEntity.getEntityId());
	    	((ModelPart) this.bipedHead).sync(data.head);
	    	((ModelPart) this.bipedHeadwear).sync(data.headwear);
	    	((ModelPart) this.bipedBody).sync(data.body);
	    	((ModelPart) this.bipedRightArm).sync(data.rightArm);
	    	((ModelPart) this.bipedLeftArm).sync(data.leftArm);
	    	((ModelPart) this.bipedRightLeg).sync(data.rightLeg);
	    	((ModelPart) this.bipedLeftLeg).sync(data.leftLeg);
	    	((ModelPart) this.bipedRightForeArm).sync(data.rightForeArm);
	    	((ModelPart) this.bipedLeftForeArm).sync(data.leftForeArm);
	    	((ModelPart) this.bipedRightForeLeg).sync(data.rightForeLeg);
	    	((ModelPart) this.bipedLeftForeLeg).sync(data.leftForeLeg);
	    	
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
