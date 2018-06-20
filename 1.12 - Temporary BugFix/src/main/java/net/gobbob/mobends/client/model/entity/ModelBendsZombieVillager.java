package net.gobbob.mobends.client.model.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelBoxBends;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.client.model.ModelRendererBends_Child;
import net.gobbob.mobends.client.model.ModelRendererBends_SeperatedChild;
import net.gobbob.mobends.data.Data_Zombie;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelZombieVillager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityZombie;

public class ModelBendsZombieVillager extends ModelZombieVillager implements IBendsModel
{
	public ModelRendererBends bipedRightForeArm;
    public ModelRendererBends bipedLeftForeArm;
    public ModelRendererBends bipedRightForeLeg;
    public ModelRendererBends bipedLeftForeLeg;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    
    public float headRotationX,headRotationY;
    public float armSwing,armSwingAmount;
	
    private HashMap nameToRendererMap;
    
    public ModelBendsZombieVillager()
    {
        this(0.0F, false);
    }
    
    public ModelBendsZombieVillager(float p_i1168_1_, boolean p_i1168_2_)
    {
        this(p_i1168_1_, 0.0F, 64, p_i1168_2_ ? 32 : 64);
    }
    
    protected ModelBendsZombieVillager(float p_i1167_1_, float p_i1167_2_, int p_i1167_3_, int p_i1167_4_)
    {
    	this.textureWidth = p_i1167_3_;
        this.textureHeight = p_i1167_4_;
        this.bipedHead = new ModelRendererBends_Child(this, 0, 0).setMother((ModelRendererBends) this.bipedBody).setShowChildIfHidden(true);
        this.bipedHead.setRotationPoint(0.0F, 0.0F + p_i1167_2_-12, 0.0F);
        this.bipedHead.setTextureOffset(0, 0).addBox(-4.0F, -10.0F, -4.0F, 8, 10, 8, p_i1167_1_);
        this.bipedHead.setTextureOffset(24, 0).addBox(-1.0F, -3.0F, -6.0F, 2, 4, 2, p_i1167_1_);

        this.bipedHeadwear = new ModelRendererBends(this, 32, 0);
        this.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, p_i1167_1_ + 0.5F);
        this.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        this.bipedBody = new ModelRendererBends(this, 16, 20).setShowChildIfHidden(true);
        this.bipedBody.addBox(-4.0F, -12.0F, -3.0F, 8, 12, 6, p_i1167_1_);
        this.bipedBody.setRotationPoint(0.0F, 0.0F + p_i1167_2_ + 12, 0.0F);
        this.bipedBody.setTextureOffset(0, 38).addBox(-4.0F, -12.0F, -3.0F, 8, 18, 6, p_i1167_1_ + 0.05F);
        this.bipedRightArm = new ModelRendererBends_SeperatedChild(this, 44, 38).setMother((ModelRendererBends) this.bipedBody);
        this.bipedRightArm.addBox(-3.0F, -2.0F, -2.0F, 4, 6, 4, p_i1167_1_);
        this.bipedRightArm.setRotationPoint(-5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedLeftArm = new ModelRendererBends_SeperatedChild(this, 44, 38).setMother((ModelRendererBends) this.bipedBody);
        this.bipedLeftArm.mirror = true;
        this.bipedLeftArm.addBox(-1.0F, -2.0F, -2.0F, 4, 6, 4, p_i1167_1_);
        this.bipedLeftArm.setRotationPoint(5.0F, 2.0F + p_i1167_2_ - 12.0f, 0.0F);
        this.bipedRightLeg = new ModelRendererBends(this, 0, 22);
        this.bipedRightLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i1167_1_);
        this.bipedRightLeg.setRotationPoint(-1.9F, 12.0F + p_i1167_2_, 0.0F);
        this.bipedLeftLeg = new ModelRendererBends(this, 0, 22);
        this.bipedLeftLeg.mirror = true;
        this.bipedLeftLeg.addBox(-2.0F, 0.0F, -2.0F, 4, 6, 4, p_i1167_1_);
        this.bipedLeftLeg.setRotationPoint(1.9F, 12.0F + p_i1167_2_, 0.0F);
        
        this.bipedRightForeArm = new ModelRendererBends(this, 44, 38+6);
        this.bipedRightForeArm.addBox(0, 0, -4.0f, 4, 6, 4, p_i1167_1_);
        this.bipedRightForeArm.setRotationPoint(-4.0f+1.0f, 4.0f, 2.0F);
        ((ModelRendererBends)this.bipedRightForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
        this.bipedLeftForeArm = new ModelRendererBends(this, 44, 38+6);
        this.bipedLeftForeArm.mirror = true;
        this.bipedLeftForeArm.addBox(0, 0, -4.0f, 4, 6, 4, p_i1167_1_);
        this.bipedLeftForeArm.setRotationPoint(-1.0f, 4.0f, 2.0F);
        ((ModelRendererBends)this.bipedLeftForeArm).getBox().offsetTextureQuad(this.bipedRightForeArm,ModelBoxBends.BOTTOM, 0, -6.0f);
    
        this.bipedRightForeLeg = new ModelRendererBends(this, 0, 22+6);
        this.bipedRightForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i1167_1_);
        this.bipedRightForeLeg.setRotationPoint(0.0f, 6.0f, -2.0F);
        ((ModelRendererBends)this.bipedRightForeLeg).getBox().offsetTextureQuad(this.bipedRightForeLeg,ModelBoxBends.BOTTOM, 0, -6.0f);
        
        this.bipedLeftForeLeg = new ModelRendererBends(this, 0, 22+6);
        this.bipedLeftForeLeg.mirror = true;
        this.bipedLeftForeLeg.addBox(-2.0F, 0.0F, 0.0F, 4, 6, 4, p_i1167_1_);
        this.bipedLeftForeLeg.setRotationPoint(0.0f, 6.0f, -2.0F);
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
        
        ((ModelRendererBends)this.bipedRightArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftArm).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedRightLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
        ((ModelRendererBends)this.bipedLeftLeg).offsetBox_Add(-0.01f, 0, -0.01f).resizeBox(4.02f, 6.0f, 4.02f).updateVertices();
    
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
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float argArmSway, float argHeadY, float argHeadX, float argNr6, Entity argEntity)
    {
    	Data_Zombie data = (Data_Zombie) EntityData.get(EntityData.ZOMBIE_DATA, argEntity.getEntityId());

    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	this.headRotationX = argHeadX;
    	this.headRotationY = argHeadY;
    	
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
    	
    	if(data.canBeUpdated()){
			this.renderOffset.setSmooth(new Vector3f(0,-1f,0),0.5f);
			this.renderRotation.setSmooth(new Vector3f(0,0,0),0.5f);
			
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
			
			BendsVariable.tempData = data;
			
			if(data.motion.x == 0.0f & data.motion.z == 0.0f){
				AnimatedEntity.getByEntity(argEntity).getAnimation("stand").animate((EntityLivingBase)argEntity, this, data);
				BendsPack.animate(this,"zombie","stand");
			}else{
				AnimatedEntity.getByEntity(argEntity).getAnimation("walk").animate((EntityLivingBase)argEntity, this, data);
				BendsPack.animate(this,"zombie","walk");
			}
			
	        ((ModelRendererBends) this.bipedHead).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedHeadwear).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedBody).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedLeftArm).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedRightArm).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedLeftLeg).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedRightLeg).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedLeftForeArm).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedRightForeArm).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedLeftForeLeg).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends) this.bipedRightForeLeg).update(DataUpdateHandler.ticksPerFrame);
	        
	        this.renderOffset.update(DataUpdateHandler.ticksPerFrame);
	        this.renderRotation.update(DataUpdateHandler.ticksPerFrame);
	        
	        data.updatedThisFrame = true;
	        if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
		    }
		}
    	if(!data.isInitialized()){
        	data.initModelPose();
        }
    }
    
    public void postRender(float argScale){
		GL11.glTranslatef(this.renderOffset.vSmooth.x*argScale,this.renderOffset.vSmooth.y*argScale,this.renderOffset.vSmooth.z*argScale);
		GL11.glRotatef(-this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GL11.glRotatef(-this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GL11.glRotatef(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
	}
    
	public void updateWithEntityData(EntityZombie argZombie){
		Data_Zombie data = (Data_Zombie) EntityData.get(EntityData.ZOMBIE_DATA, argZombie.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
		}
	}
	
	public boolean isRenderedInGui() {
		return EventHandlerRenderPlayer.renderingGuiScreen;
	}

	@Override
	public Object getRendererForName(String name) {
		return nameToRendererMap.get(name);
	}
}
