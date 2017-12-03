package net.gobbob.mobends.client.model.entity;

import net.gobbob.mobends.AnimatedEntity;
import net.gobbob.mobends.client.model.ModelBoxBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBends_Child;
import net.gobbob.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.gobbob.mobends.client.renderer.SwordTrail;
import net.gobbob.mobends.data.Data_Player;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.BendsVar;
import net.gobbob.mobends.util.BendsLogger;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

public class ModelBendsPlayerArmor extends ModelBiped{
    public ModelRendererBends bipedRightForeArm;
    public ModelRendererBends bipedLeftForeArm;
    public ModelRendererBends bipedRightForeLeg;
    public ModelRendererBends bipedLeftForeLeg;
    
    public ModelRendererBends bipedCloak;
    public ModelRendererBends bipedEars;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    public SmoothVector3f renderItemRotation = new SmoothVector3f();
    
    public SwordTrail swordTrail = new SwordTrail();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
    
    public ModelBendsPlayerArmor(float p_i46304_1_)
    {
        super(p_i46304_1_);
        
        this.textureWidth = 64;
        this.textureHeight = 32;
        
        this.bipedEars = new ModelRendererBends(this, 24, 0);
        this.bipedEars.addBox(-3.0F, -6.0F, -1.0F, 6, 6, 1, p_i46304_1_);
        this.bipedCloak = new ModelRendererBends(this, 0, 0);
        this.bipedCloak.setTextureSize(64, 32);
        this.bipedCloak.addBox(-5.0F, 0.0F, -1.0F, 10, 16, 1, p_i46304_1_);
        
        this.bipedHead = new ModelRendererBends(this, 0, 0).setShowChildIfHidden(true);
        this.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_);
        this.bipedHead.setRotationPoint(0.0F, -12.0F, 0.0F);
        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i46304_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 16);
        this.bipedBody.addBox(-4.0F, -12.0F, -2.0F, 8, 12, 4, p_i46304_1_);
        this.bipedBody.setRotationPoint(0.0F, 12.0F, 0.0F);
        
            this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftArm.setRotationPoint(5.0F, 2.0F-12.0f, 0.0F);
            this.bipedLeftArm.mirror = true;
            this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 40, 16).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
            this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightArm.setRotationPoint(-5.0F, 2.0F-12.0f, 0.0F);
            
            ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
            ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        
            this.bipedLeftForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedLeftForeArm.addBox(-1.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedLeftForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            this.bipedLeftForeArm.mirror = true;
            ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedLeftForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            this.bipedRightForeArm = new ModelRendererBends(this, 40, 16+6);
            this.bipedRightForeArm.addBox(-3.0F, 0.0F, -4.0F, 4, 6, 4, p_i46304_1_);
            this.bipedRightForeArm.setRotationPoint(0.0F, 4.0F, 2.0F);
            ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
            
        
        this.bipedRightLeg = new ModelRendererBends(this, 0, 16);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 16);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F, 0.0F);
        
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedRightForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 16+6);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i46304_1_);
        this.bipedLeftForeLeg.setRotationPoint(0, 6.0F, -2.0F);
        ((ModelRendererBends)this.bipedLeftForeLeg).getBox().offsetTextureQuad(this.bipedLeftForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        //this.bipedBody.addChild(this.bipedHead);
        this.bipedBody.addChild(this.bipedRightArm);
        this.bipedBody.addChild(this.bipedLeftArm);
        
        this.bipedHead.addChild(this.bipedHeadwear);
        
        this.bipedRightArm.addChild(this.bipedRightForeArm);
        this.bipedLeftArm.addChild(this.bipedLeftForeArm);
        
        this.bipedRightLeg.addChild(this.bipedRightForeLeg);
        this.bipedLeftLeg.addChild(this.bipedLeftForeLeg);
        
        ((ModelRendererBends_SeperatedChild)this.bipedRightArm).setSeperatedPart((ModelRendererBends) this.bipedRightForeArm);
        ((ModelRendererBends_SeperatedChild)this.bipedLeftArm).setSeperatedPart((ModelRendererBends) this.bipedLeftForeArm);
        
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
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
	            
	            GL11.glPushMatrix();
	            	this.bipedBody.postRender(p_78088_7_);
	            	this.bipedHead.render(p_78088_7_);
	            GL11.glPopMatrix();
	        }
	        
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	if(Minecraft.getMinecraft().theWorld == null){
    		return;
    	}
    	
    	if(Minecraft.getMinecraft().theWorld.isRemote){
    		if(Minecraft.getMinecraft().isGamePaused()){
    			return;
    		}
    	}
    	
    	Data_Player data = (Data_Player) Data_Player.get(argEntity.getEntityId());
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	
    	if(Minecraft.getMinecraft().currentScreen != null){
    		this.headRotationY = 0;
    	}
    	
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
    	this.renderItemRotation.set(data.renderItemRotation);
    	this.swordTrail = data.swordTrail;
    }
    
    public void postRender(float argScale){
    	GlStateManager.translate(this.renderOffset.vSmooth.x*argScale,-this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
    
    public void postRenderTranslate(float argScale){
    	GlStateManager.translate(this.renderOffset.vSmooth.x*argScale,-this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
    }
    
    public void postRenderRotate(float argScale){
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
	
	public void updateWithEntityData(AbstractClientPlayer argPlayer){
		Data_Player data = Data_Player.get(argPlayer.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
			this.renderItemRotation.set(data.renderItemRotation);
		}
	}
}
