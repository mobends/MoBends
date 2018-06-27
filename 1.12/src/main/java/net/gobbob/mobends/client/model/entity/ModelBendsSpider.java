package net.gobbob.mobends.client.model.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.EntityRenderHandler;
import net.gobbob.mobends.client.model.IBendsModel;
import net.gobbob.mobends.client.model.ModelPart;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;

public class ModelBendsSpider extends ModelSpider implements IBendsModel{
    public ModelPart spiderForeLeg1;
    public ModelPart spiderForeLeg2;
    public ModelPart spiderForeLeg3;
    public ModelPart spiderForeLeg4;
    public ModelPart spiderForeLeg5;
    public ModelPart spiderForeLeg6;
    public ModelPart spiderForeLeg7;
    public ModelPart spiderForeLeg8;
    
    public SmoothVector3f renderOffset = new SmoothVector3f();
    public SmoothVector3f renderRotation = new SmoothVector3f();
    
    public float headRotationX,headRotationY;
    public float armSwing, armSwingAmount;
    
    private HashMap nameToRendererMap;
    
    public ModelBendsSpider()
    {
        float f = 0.0F;
        byte b0 = 15;
        
        float legLength = 12;
        float foreLegLength = 15;
        
        float legRatio = 1;
        float foreLegRatio = 1;
        
        this.spiderHead = new ModelPart(this, 32, 4);
        this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, f);
        this.spiderHead.setRotationPoint(0.0F, (float)b0, -3.0F);
        this.spiderNeck = new ModelPart(this, 0, 0);
        this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, f);
        this.spiderNeck.setRotationPoint(0.0F, (float)b0, 0.0F);
        this.spiderBody = new ModelPart(this, 0, 12);
        this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, f);
        this.spiderBody.setRotationPoint(0.0F, (float)b0, 9.0F);
        this.spiderLeg1 = new ModelPart(this, 18, 0);
        this.spiderLeg1.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg1.setRotationPoint(-4.0F, (float)b0, 2.0F);
        ((ModelPart)this.spiderLeg1).offsetBoxBy(+8-legLength, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg2 = new ModelPart(this, 18, 0);
        this.spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg2.setRotationPoint(4.0F, (float)b0, 2.0F);
        ((ModelPart)this.spiderLeg2).offsetBoxBy(0, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg3 = new ModelPart(this, 18, 0);
        this.spiderLeg3.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg3.setRotationPoint(-4.0F, (float)b0, 1.0F);
        ((ModelPart)this.spiderLeg3).offsetBoxBy(+8-legLength*legRatio, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg4 = new ModelPart(this, 18, 0);
        this.spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg4.setRotationPoint(4.0F, (float)b0, 1.0F);
        ((ModelPart)this.spiderLeg4).offsetBoxBy(0, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg5 = new ModelPart(this, 18, 0);
        this.spiderLeg5.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg5.setRotationPoint(-4.0F, (float)b0, 0.0F);
        ((ModelPart)this.spiderLeg5).offsetBoxBy(+8-legLength*legRatio, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg6 = new ModelPart(this, 18, 0);
        this.spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg6.setRotationPoint(4.0F, (float)b0, 0.0F);
        ((ModelPart)this.spiderLeg6).offsetBoxBy(0, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg7 = new ModelPart(this, 18, 0);
        this.spiderLeg7.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg7.setRotationPoint(-4.0F, (float)b0, -1.0F);
        ((ModelPart)this.spiderLeg7).offsetBoxBy(+8-legLength, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg8 = new ModelPart(this, 18, 0);
        this.spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg8.setRotationPoint(4.0F, (float)b0, -1.0F);
        ((ModelPart)this.spiderLeg8).offsetBoxBy(0, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        
        this.spiderForeLeg1 = new ModelPart(this, 18, 0);
        this.spiderForeLeg1.addBox(-foreLegLength, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg1.setRotationPoint(-legLength+1, -1.0f, 0.0f);
        this.spiderForeLeg1.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg2 = new ModelPart(this, 18, 0);
        this.spiderForeLeg2.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg2.setRotationPoint(legLength-1, -1.0f, 0);
        this.spiderForeLeg2.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg3 = new ModelPart(this, 18, 0);
        this.spiderForeLeg3.addBox(-foreLegLength*foreLegRatio, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg3.setRotationPoint(-legLength*legRatio+1, -1.0f, 0);
        this.spiderForeLeg3.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg4 = new ModelPart(this, 18, 0);
        this.spiderForeLeg4.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg4.setRotationPoint(legLength*legRatio-1, -1.0f, 0);
        this.spiderForeLeg4.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg5 = new ModelPart(this, 18, 0);
        this.spiderForeLeg5.addBox(-foreLegLength*foreLegRatio, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg5.setRotationPoint(-legLength*legRatio+1, -1.0f, 0.0F);
        this.spiderForeLeg5.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg6 = new ModelPart(this, 18, 0);
        this.spiderForeLeg6.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg6.setRotationPoint(legLength*legRatio-1, -1.0f, 0.0F);
        this.spiderForeLeg6.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg7 = new ModelPart(this, 18, 0);
        this.spiderForeLeg7.addBox(-foreLegLength, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg7.setRotationPoint(-legLength+1, -1.0f, 0);
        this.spiderForeLeg7.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg8 = new ModelPart(this, 18, 0);
        this.spiderForeLeg8.addBox(0.0f, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg8.setRotationPoint(legLength-1, -1.0f, 0);
        this.spiderForeLeg8.resizeBox(foreLegLength, 2, 2).updateVertices();
        
        this.spiderLeg1.addChild(this.spiderForeLeg1);
        this.spiderLeg2.addChild(this.spiderForeLeg2);
        this.spiderLeg3.addChild(this.spiderForeLeg3);
        this.spiderLeg4.addChild(this.spiderForeLeg4);
        this.spiderLeg5.addChild(this.spiderForeLeg5);
        this.spiderLeg6.addChild(this.spiderForeLeg6);
        this.spiderLeg7.addChild(this.spiderForeLeg7);
        this.spiderLeg8.addChild(this.spiderForeLeg8);
    
        nameToRendererMap = new HashMap<String, Object>();
        nameToRendererMap.put("head", spiderHead);
        nameToRendererMap.put("body", spiderBody);
        nameToRendererMap.put("neck", spiderNeck);
        nameToRendererMap.put("leg1", spiderLeg1);
        nameToRendererMap.put("leg2", spiderLeg2);
        nameToRendererMap.put("leg3", spiderLeg3);
        nameToRendererMap.put("leg4", spiderLeg4);
        nameToRendererMap.put("leg5", spiderLeg5);
        nameToRendererMap.put("leg6", spiderLeg6);
        nameToRendererMap.put("leg7", spiderLeg7);
        nameToRendererMap.put("leg8", spiderLeg8);
        nameToRendererMap.put("foreLeg1", spiderForeLeg1);
        nameToRendererMap.put("foreLeg2", spiderForeLeg2);
        nameToRendererMap.put("foreLeg3", spiderForeLeg3);
        nameToRendererMap.put("foreLeg4", spiderForeLeg4);
        nameToRendererMap.put("foreLeg5", spiderForeLeg5);
        nameToRendererMap.put("foreLeg6", spiderForeLeg6);
        nameToRendererMap.put("foreLeg7", spiderForeLeg7);
        nameToRendererMap.put("foreLeg8", spiderForeLeg8);
    }
    
    public void render(Entity p_78088_1_, float p_78088_2_, float p_78088_3_, float p_78088_4_, float p_78088_5_, float p_78088_6_, float p_78088_7_)
    {
        this.setRotationAngles(p_78088_2_, p_78088_3_, p_78088_4_, p_78088_5_, p_78088_6_, p_78088_7_, p_78088_1_);
        GL11.glPushMatrix();
	        GL11.glTranslatef(0,0.2f,0);
	        this.spiderHead.render(p_78088_7_);
	        this.spiderNeck.render(p_78088_7_);
	        this.spiderBody.render(p_78088_7_);
	        this.spiderLeg1.render(p_78088_7_);
	        this.spiderLeg2.render(p_78088_7_);
	        this.spiderLeg3.render(p_78088_7_);
	        this.spiderLeg4.render(p_78088_7_);
	        this.spiderLeg5.render(p_78088_7_);
	        this.spiderLeg6.render(p_78088_7_);
	        this.spiderLeg7.render(p_78088_7_);
	        this.spiderLeg8.render(p_78088_7_);
        GL11.glPopMatrix();
    }
    
    public void setRotationAngles(float argSwingTime, float argSwingAmount, float p_78087_3_, float p_78087_4_, float p_78087_5_, float p_78087_6_, Entity argEntity)
    {
    	/*Data_Spider data = (Data_Spider) EntityData.get(EntityData.SPIDER_DATA, argEntity.getEntityId());
    	AnimatedEntity aEntity = AnimatedEntity.getByEntity(argEntity);
    	EntitySpider spider = (EntitySpider) argEntity;
    	
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.headRotationX = p_78087_5_;
    	this.headRotationY = p_78087_4_;
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;*/
    	
    	/*((ModelPart)this.spiderHead).sync((ModelPart)data.spiderHead);
    	((ModelPart)this.spiderNeck).sync((ModelPart)data.spiderNeck);
    	((ModelPart)this.spiderBody).sync((ModelPart)data.spiderBody);
		
    	((ModelPart)this.spiderLeg1).sync((ModelPart)data.spiderLeg1);
    	((ModelPart)this.spiderLeg2).sync((ModelPart)data.spiderLeg2);
    	((ModelPart)this.spiderLeg3).sync((ModelPart)data.spiderLeg3);
    	((ModelPart)this.spiderLeg4).sync((ModelPart)data.spiderLeg4);
    	((ModelPart)this.spiderLeg5).sync((ModelPart)data.spiderLeg5);
    	((ModelPart)this.spiderLeg6).sync((ModelPart)data.spiderLeg6);
    	((ModelPart)this.spiderLeg7).sync((ModelPart)data.spiderLeg7);
    	((ModelPart)this.spiderLeg8).sync((ModelPart)data.spiderLeg8);
		
    	((ModelPart)this.spiderForeLeg1).sync((ModelPart)data.spiderForeLeg1);
    	((ModelPart)this.spiderForeLeg2).sync((ModelPart)data.spiderForeLeg2);
    	((ModelPart)this.spiderForeLeg3).sync((ModelPart)data.spiderForeLeg3);
    	((ModelPart)this.spiderForeLeg4).sync((ModelPart)data.spiderForeLeg4);
    	((ModelPart)this.spiderForeLeg5).sync((ModelPart)data.spiderForeLeg5);
    	((ModelPart)this.spiderForeLeg6).sync((ModelPart)data.spiderForeLeg6);
    	((ModelPart)this.spiderForeLeg7).sync((ModelPart)data.spiderForeLeg7);
    	((ModelPart)this.spiderForeLeg8).sync((ModelPart)data.spiderForeLeg8);*/
		
    	/*this.renderOffset.set(data.renderOffset);
    	this.renderRotation.set(data.renderRotation);
    	
    	if(data.canBeUpdated())
    	{
    		((ModelPart)this.spiderHead).resetScale();
    		((ModelPart)this.spiderNeck).resetScale();
    		((ModelPart)this.spiderBody).resetScale();
    		
    		((ModelPart)this.spiderLeg1).resetScale();
    		((ModelPart)this.spiderLeg2).resetScale();
    		((ModelPart)this.spiderLeg3).resetScale();
    		((ModelPart)this.spiderLeg4).resetScale();
    		((ModelPart)this.spiderLeg5).resetScale();
    		((ModelPart)this.spiderLeg6).resetScale();
    		((ModelPart)this.spiderLeg7).resetScale();
    		((ModelPart)this.spiderLeg8).resetScale();
    		
    		((ModelPart)this.spiderForeLeg1).resetScale();
    		((ModelPart)this.spiderForeLeg2).resetScale();
    		((ModelPart)this.spiderForeLeg3).resetScale();
    		((ModelPart)this.spiderForeLeg4).resetScale();
    		((ModelPart)this.spiderForeLeg5).resetScale();
    		((ModelPart)this.spiderForeLeg6).resetScale();
    		((ModelPart)this.spiderForeLeg7).resetScale();
    		((ModelPart)this.spiderForeLeg8).resetScale();

    		this.animate(aEntity, spider, data);
    		
	        ((ModelPart)this.spiderHead).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderNeck).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderBody).update(DataUpdateHandler.ticksPerFrame);
	        
	        ((ModelPart)this.spiderLeg1).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg2).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg3).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg4).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg5).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg6).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg7).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderLeg8).update(DataUpdateHandler.ticksPerFrame);
		    
	        ((ModelPart)this.spiderForeLeg1).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg2).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg3).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg4).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg5).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg6).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg7).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelPart)this.spiderForeLeg8).update(DataUpdateHandler.ticksPerFrame);
	        
	        this.renderOffset.update(DataUpdateHandler.ticksPerFrame);
	        this.renderRotation.update(DataUpdateHandler.ticksPerFrame);*/
	        
	        /*data.updatedThisFrame = true;
	        if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
		    }*/
    	/*}
    	
    	if(!data.isInitialized()){
    		this.animate(aEntity, spider, data);
    		data.syncModelInfo(this);
    		data.initModelPose();
    	}*/
    }
    
    public void animate(AnimatedEntity aEntity, EntitySpider entity, Data_Spider data) {
    	/*if(data.calcCollidedHorizontally()){
			aEntity.getAnimation("wallClimb").animate(entity, this, data);
        	BendsPack.animate(this,"spider","wallClimb");
		}else{
    		if(!data.isOnGround() | data.ticksAfterTouchdown < 2){
    			aEntity.getAnimation("jump").animate(entity, this, data);
	        	BendsPack.animate(this,"spider","jump");
			}else{
				aEntity.getAnimation("onGround").animate(entity, this, data);
		        BendsPack.animate(this, "spider", "onGround");
				if(data.motion.x == 0.0f & data.motion.z == 0.0f){
		        	BendsPack.animate(this, "spider", "stand");
		        }else{
		        	BendsPack.animate(this, "spider", "walk");
		        }
	        }
		}*/
    }
    
    public void postRender(float argScale){
    	this.postRenderTranslate(argScale);
    	this.postRenderRotate(argScale);
    }
    
    public void postRenderTranslate(float argScale){
    	GlStateManager.translate(this.renderOffset.getX()*argScale,-this.renderOffset.getY()*argScale,this.renderOffset.getZ()*argScale);
    }
    
    public void postRenderRotate(float argScale){
    	GlStateManager.rotate(this.renderRotation.getX(),1.0f,0.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getY(),0.0f,1.0f,0.0f);
    	GlStateManager.rotate(this.renderRotation.getZ(),0.0f,0.0f,1.0f);
    }
	
	/*public void updateWithEntityData(EntitySpider argSpider){
		Data_Spider data = (Data_Spider) EntityData.get(EntityData.SPIDER_DATA, argSpider.getEntityId());
		if(data != null){
			this.renderOffset.set(data.renderOffset);
			this.renderRotation.set(data.renderRotation);
		}
	}*/
	
	public boolean isRenderedInGui() {
		return EntityRenderHandler.renderingGuiScreen;
	}

	@Override
	public Object getPartForName(String name) {
		return nameToRendererMap.get(name);
	}
}
