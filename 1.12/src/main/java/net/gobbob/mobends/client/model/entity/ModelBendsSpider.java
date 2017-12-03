package net.gobbob.mobends.client.model.entity;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import net.gobbob.mobends.animatedentity.AnimatedEntity;
import net.gobbob.mobends.client.event.DataUpdateHandler;
import net.gobbob.mobends.client.event.EventHandlerRenderPlayer;
import net.gobbob.mobends.client.model.ModelRendererBends;
import net.gobbob.mobends.data.Data_Spider;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSpider;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySpider;

public class ModelBendsSpider extends ModelSpider implements IBendsModel{
    public ModelRendererBends spiderForeLeg1;
    public ModelRendererBends spiderForeLeg2;
    public ModelRendererBends spiderForeLeg3;
    public ModelRendererBends spiderForeLeg4;
    public ModelRendererBends spiderForeLeg5;
    public ModelRendererBends spiderForeLeg6;
    public ModelRendererBends spiderForeLeg7;
    public ModelRendererBends spiderForeLeg8;
    
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
        
        this.spiderHead = new ModelRendererBends(this, 32, 4);
        this.spiderHead.addBox(-4.0F, -4.0F, -8.0F, 8, 8, 8, f);
        this.spiderHead.setRotationPoint(0.0F, (float)b0, -3.0F);
        this.spiderNeck = new ModelRendererBends(this, 0, 0);
        this.spiderNeck.addBox(-3.0F, -3.0F, -3.0F, 6, 6, 6, f);
        this.spiderNeck.setRotationPoint(0.0F, (float)b0, 0.0F);
        this.spiderBody = new ModelRendererBends(this, 0, 12);
        this.spiderBody.addBox(-5.0F, -4.0F, -6.0F, 10, 8, 12, f);
        this.spiderBody.setRotationPoint(0.0F, (float)b0, 9.0F);
        this.spiderLeg1 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg1.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg1.setRotationPoint(-4.0F, (float)b0, 2.0F);
        ((ModelRendererBends)this.spiderLeg1).offsetBox_Add(+8-legLength, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg2 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg2.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg2.setRotationPoint(4.0F, (float)b0, 2.0F);
        ((ModelRendererBends)this.spiderLeg2).offsetBox_Add(0, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg3 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg3.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg3.setRotationPoint(-4.0F, (float)b0, 1.0F);
        ((ModelRendererBends)this.spiderLeg3).offsetBox_Add(+8-legLength*legRatio, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg4 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg4.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg4.setRotationPoint(4.0F, (float)b0, 1.0F);
        ((ModelRendererBends)this.spiderLeg4).offsetBox_Add(0, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg5 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg5.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg5.setRotationPoint(-4.0F, (float)b0, 0.0F);
        ((ModelRendererBends)this.spiderLeg5).offsetBox_Add(+8-legLength*legRatio, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg6 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg6.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg6.setRotationPoint(4.0F, (float)b0, 0.0F);
        ((ModelRendererBends)this.spiderLeg6).offsetBox_Add(0, -0.01f, -0.01f).resizeBox(legLength*legRatio, 2.02f, 2.02f).updateVertices();
        this.spiderLeg7 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg7.addBox(-7.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg7.setRotationPoint(-4.0F, (float)b0, -1.0F);
        ((ModelRendererBends)this.spiderLeg7).offsetBox_Add(+8-legLength, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        this.spiderLeg8 = new ModelRendererBends(this, 18, 0);
        this.spiderLeg8.addBox(-1.0F, -1.0F, -1.0F, 8, 2, 2, f);
        this.spiderLeg8.setRotationPoint(4.0F, (float)b0, -1.0F);
        ((ModelRendererBends)this.spiderLeg8).offsetBox_Add(0, -0.01f, -0.01f).resizeBox(legLength, 2.02f, 2.02f).updateVertices();
        
        this.spiderForeLeg1 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg1.addBox(-foreLegLength, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg1.setRotationPoint(-legLength+1, -1.0f, 0.0f);
        this.spiderForeLeg1.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg2 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg2.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg2.setRotationPoint(legLength-1, -1.0f, 0);
        this.spiderForeLeg2.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg3 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg3.addBox(-foreLegLength*foreLegRatio, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg3.setRotationPoint(-legLength*legRatio+1, -1.0f, 0);
        this.spiderForeLeg3.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg4 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg4.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg4.setRotationPoint(legLength*legRatio-1, -1.0f, 0);
        this.spiderForeLeg4.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg5 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg5.addBox(-foreLegLength*foreLegRatio, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg5.setRotationPoint(-legLength*legRatio+1, -1.0f, 0.0F);
        this.spiderForeLeg5.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg6 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg6.addBox(0.0F, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg6.setRotationPoint(legLength*legRatio-1, -1.0f, 0.0F);
        this.spiderForeLeg6.resizeBox(foreLegLength*foreLegRatio, 2, 2).updateVertices();
        this.spiderForeLeg7 = new ModelRendererBends(this, 18, 0);
        this.spiderForeLeg7.addBox(-foreLegLength, 0.0F, -1.0F, 8, 2, 2, f);
        this.spiderForeLeg7.setRotationPoint(-legLength+1, -1.0f, 0);
        this.spiderForeLeg7.resizeBox(foreLegLength, 2, 2).updateVertices();
        this.spiderForeLeg8 = new ModelRendererBends(this, 18, 0);
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
    	Data_Spider data = (Data_Spider) EntityData.get(EntityData.SPIDER_DATA, argEntity.getEntityId());
    	AnimatedEntity aEntity = AnimatedEntity.getByEntity(argEntity);
    	EntitySpider spider = (EntitySpider) argEntity;
    	
    	if(Minecraft.getMinecraft().world == null)
    		return;
    	
    	this.headRotationX = p_78087_5_;
    	this.headRotationY = p_78087_4_;
    	this.armSwing = argSwingTime;
    	this.armSwingAmount = argSwingAmount;
    	
    	((ModelRendererBends)this.spiderHead).sync((ModelRendererBends)data.spiderHead);
    	((ModelRendererBends)this.spiderNeck).sync((ModelRendererBends)data.spiderNeck);
    	((ModelRendererBends)this.spiderBody).sync((ModelRendererBends)data.spiderBody);
		
    	((ModelRendererBends)this.spiderLeg1).sync((ModelRendererBends)data.spiderLeg1);
    	((ModelRendererBends)this.spiderLeg2).sync((ModelRendererBends)data.spiderLeg2);
    	((ModelRendererBends)this.spiderLeg3).sync((ModelRendererBends)data.spiderLeg3);
    	((ModelRendererBends)this.spiderLeg4).sync((ModelRendererBends)data.spiderLeg4);
    	((ModelRendererBends)this.spiderLeg5).sync((ModelRendererBends)data.spiderLeg5);
    	((ModelRendererBends)this.spiderLeg6).sync((ModelRendererBends)data.spiderLeg6);
    	((ModelRendererBends)this.spiderLeg7).sync((ModelRendererBends)data.spiderLeg7);
    	((ModelRendererBends)this.spiderLeg8).sync((ModelRendererBends)data.spiderLeg8);
		
    	((ModelRendererBends)this.spiderForeLeg1).sync((ModelRendererBends)data.spiderForeLeg1);
    	((ModelRendererBends)this.spiderForeLeg2).sync((ModelRendererBends)data.spiderForeLeg2);
    	((ModelRendererBends)this.spiderForeLeg3).sync((ModelRendererBends)data.spiderForeLeg3);
    	((ModelRendererBends)this.spiderForeLeg4).sync((ModelRendererBends)data.spiderForeLeg4);
    	((ModelRendererBends)this.spiderForeLeg5).sync((ModelRendererBends)data.spiderForeLeg5);
    	((ModelRendererBends)this.spiderForeLeg6).sync((ModelRendererBends)data.spiderForeLeg6);
    	((ModelRendererBends)this.spiderForeLeg7).sync((ModelRendererBends)data.spiderForeLeg7);
    	((ModelRendererBends)this.spiderForeLeg8).sync((ModelRendererBends)data.spiderForeLeg8);
		
    	this.renderOffset.set(data.renderOffset);
    	this.renderRotation.set(data.renderRotation);
    	
    	if(data.canBeUpdated())
    	{
    		((ModelRendererBends)this.spiderHead).resetScale();
    		((ModelRendererBends)this.spiderNeck).resetScale();
    		((ModelRendererBends)this.spiderBody).resetScale();
    		
    		((ModelRendererBends)this.spiderLeg1).resetScale();
    		((ModelRendererBends)this.spiderLeg2).resetScale();
    		((ModelRendererBends)this.spiderLeg3).resetScale();
    		((ModelRendererBends)this.spiderLeg4).resetScale();
    		((ModelRendererBends)this.spiderLeg5).resetScale();
    		((ModelRendererBends)this.spiderLeg6).resetScale();
    		((ModelRendererBends)this.spiderLeg7).resetScale();
    		((ModelRendererBends)this.spiderLeg8).resetScale();
    		
    		((ModelRendererBends)this.spiderForeLeg1).resetScale();
    		((ModelRendererBends)this.spiderForeLeg2).resetScale();
    		((ModelRendererBends)this.spiderForeLeg3).resetScale();
    		((ModelRendererBends)this.spiderForeLeg4).resetScale();
    		((ModelRendererBends)this.spiderForeLeg5).resetScale();
    		((ModelRendererBends)this.spiderForeLeg6).resetScale();
    		((ModelRendererBends)this.spiderForeLeg7).resetScale();
    		((ModelRendererBends)this.spiderForeLeg8).resetScale();

    		this.animate(aEntity, spider, data);
    		
	        ((ModelRendererBends)this.spiderHead).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderNeck).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderBody).update(DataUpdateHandler.ticksPerFrame);
	        
	        ((ModelRendererBends)this.spiderLeg1).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg2).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg3).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg4).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg5).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg6).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg7).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderLeg8).update(DataUpdateHandler.ticksPerFrame);
		    
	        ((ModelRendererBends)this.spiderForeLeg1).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg2).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg3).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg4).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg5).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg6).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg7).update(DataUpdateHandler.ticksPerFrame);
	        ((ModelRendererBends)this.spiderForeLeg8).update(DataUpdateHandler.ticksPerFrame);
	        
	        this.renderOffset.update(DataUpdateHandler.ticksPerFrame);
	        this.renderRotation.update(DataUpdateHandler.ticksPerFrame);
	        
	        data.updatedThisFrame = true;
	        if(!this.isRenderedInGui()){
		    	data.syncModelInfo(this);
		    }
    	}
    	
    	if(!data.isInitialized()){
    		this.animate(aEntity, spider, data);
    		data.syncModelInfo(this);
    		data.initModelPose();
    	}
    }
    
    public void animate(AnimatedEntity aEntity, EntitySpider entity, Data_Spider data) {
    	if(data.calcCollidedHorizontally()){
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
	
	public void updateWithEntityData(EntitySpider argSpider){
		Data_Spider data = (Data_Spider) EntityData.get(EntityData.SPIDER_DATA, argSpider.getEntityId());
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
