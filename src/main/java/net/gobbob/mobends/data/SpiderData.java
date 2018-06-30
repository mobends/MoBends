package net.gobbob.mobends.data;

import java.util.HashMap;

import net.gobbob.mobends.animation.controller.SpiderController;
import net.gobbob.mobends.client.model.ModelPartTransform;
import net.gobbob.mobends.util.SmoothVector3f;
import net.minecraft.entity.Entity;

public class SpiderData extends LivingEntityData
{
	public ModelPartTransform spiderHead;
    public ModelPartTransform spiderNeck;
    public ModelPartTransform spiderBody;
    
    public ModelPartTransform spiderLeg1;
    public ModelPartTransform spiderLeg2;
    public ModelPartTransform spiderLeg3;
    public ModelPartTransform spiderLeg4;
    public ModelPartTransform spiderLeg5;
    public ModelPartTransform spiderLeg6;
    public ModelPartTransform spiderLeg7;
    public ModelPartTransform spiderLeg8;
    
    public ModelPartTransform spiderForeLeg1;
    public ModelPartTransform spiderForeLeg2;
    public ModelPartTransform spiderForeLeg3;
    public ModelPartTransform spiderForeLeg4;
    public ModelPartTransform spiderForeLeg5;
    public ModelPartTransform spiderForeLeg6;
    public ModelPartTransform spiderForeLeg7;
    public ModelPartTransform spiderForeLeg8;
    
	public SpiderData(Entity entity)
	{
		super(entity);
		this.controller = new SpiderController();
	}

	@Override
	public void update(float partialTicks)
	{
		super.update(partialTicks);
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();
		
		this.spiderBody = new ModelPartTransform();
		this.spiderNeck = new ModelPartTransform();
		this.spiderHead = new ModelPartTransform();
		this.spiderLeg1 = new ModelPartTransform();
		this.spiderLeg2 = new ModelPartTransform();
		this.spiderLeg3 = new ModelPartTransform();
		this.spiderLeg4 = new ModelPartTransform();
		this.spiderLeg5 = new ModelPartTransform();
		this.spiderLeg6 = new ModelPartTransform();
		this.spiderLeg7 = new ModelPartTransform();
		this.spiderLeg8 = new ModelPartTransform();
		this.spiderForeLeg1 = new ModelPartTransform();
		this.spiderForeLeg2 = new ModelPartTransform();
		this.spiderForeLeg3 = new ModelPartTransform();
		this.spiderForeLeg4 = new ModelPartTransform();
		this.spiderForeLeg5 = new ModelPartTransform();
		this.spiderForeLeg6 = new ModelPartTransform();
		this.spiderForeLeg7 = new ModelPartTransform();
		this.spiderForeLeg8 = new ModelPartTransform();
		
		this.spiderBody.getRotation().finish();
		this.spiderNeck.getRotation().finish();
		this.spiderHead.getRotation().finish();
		this.spiderLeg1.getRotation().finish();
		this.spiderLeg2.getRotation().finish();
		this.spiderLeg3.getRotation().finish();
		this.spiderLeg4.getRotation().finish();
		this.spiderLeg5.getRotation().finish();
		this.spiderLeg6.getRotation().finish();
		this.spiderLeg7.getRotation().finish();
		this.spiderLeg8.getRotation().finish();
		this.spiderForeLeg1.getRotation().finish();
		this.spiderForeLeg2.getRotation().finish();
		this.spiderForeLeg3.getRotation().finish();
		this.spiderForeLeg4.getRotation().finish();
		this.spiderForeLeg5.getRotation().finish();
		this.spiderForeLeg6.getRotation().finish();
		this.spiderForeLeg7.getRotation().finish();
		this.spiderForeLeg8.getRotation().finish();
		
        nameToPartMap.put("body", spiderBody);
        nameToPartMap.put("neck", spiderNeck);
        nameToPartMap.put("head", spiderHead);
        nameToPartMap.put("leg1", spiderLeg1);
        nameToPartMap.put("leg2", spiderLeg2);
        nameToPartMap.put("leg3", spiderLeg3);
        nameToPartMap.put("leg4", spiderLeg4);
        nameToPartMap.put("leg5", spiderLeg5);
        nameToPartMap.put("leg6", spiderLeg6);
        nameToPartMap.put("leg7", spiderLeg7);
        nameToPartMap.put("leg8", spiderLeg8);
        nameToPartMap.put("foreLeg1", spiderForeLeg1);
        nameToPartMap.put("foreLeg2", spiderForeLeg2);
        nameToPartMap.put("foreLeg3", spiderForeLeg3);
        nameToPartMap.put("foreLeg4", spiderForeLeg4);
        nameToPartMap.put("foreLeg5", spiderForeLeg5);
        nameToPartMap.put("foreLeg6", spiderForeLeg6);
        nameToPartMap.put("foreLeg7", spiderForeLeg7);
        nameToPartMap.put("foreLeg8", spiderForeLeg8);
        
        this.spiderHead.position.set(0.0F, 15.0F, -3.0F);
        this.spiderNeck.position.set(0.0F, 15.0F, 0.0F);
        this.spiderBody.position.set(0.0F, 15.0F, 9.0F);
        this.spiderLeg1.position.set(-4.0F, 15.0F, 2.0F);
        this.spiderLeg2.position.set(4.0F, 15.0F, 2.0F);
        this.spiderLeg3.position.set(-4.0F, 15.0F, 1.0F);
        this.spiderLeg4.position.set(4.0F, 15.0F, 1.0F);
        this.spiderLeg5.position.set(-4.0F, 15.0F, 0.0F);
        this.spiderLeg6.position.set(4.0F, 15.0F, 0.0F);
        this.spiderLeg7.position.set(-4.0F, 15.0F, -1.0F);
        this.spiderLeg8.position.set(4.0F, 15.0F, -1.0F);
        this.spiderForeLeg1.position.set(-11F, -1F, 0F);
        this.spiderForeLeg2.position.set(11F, -1F, 0F);
        this.spiderForeLeg3.position.set(-11F, -1F, 0F);
        this.spiderForeLeg4.position.set(11F, -1F, 0F);
        this.spiderForeLeg5.position.set(-11F, -1F, 0F);
        this.spiderForeLeg6.position.set(11F, -1F, 0F);
        this.spiderForeLeg7.position.set(-11F, -1F, 0F);
        this.spiderForeLeg8.position.set(11F, -1F, 0F);
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);
		
		this.spiderBody.update(ticksPerFrame);
		this.spiderNeck.update(ticksPerFrame);;
		this.spiderHead.update(ticksPerFrame);
		this.spiderLeg1.update(ticksPerFrame);
		this.spiderLeg2.update(ticksPerFrame);
		this.spiderLeg3.update(ticksPerFrame);
		this.spiderLeg4.update(ticksPerFrame);
		this.spiderLeg5.update(ticksPerFrame);
		this.spiderLeg6.update(ticksPerFrame);
		this.spiderLeg7.update(ticksPerFrame);
		this.spiderLeg8.update(ticksPerFrame);
		this.spiderForeLeg1.update(ticksPerFrame);
		this.spiderForeLeg2.update(ticksPerFrame);
		this.spiderForeLeg3.update(ticksPerFrame);
		this.spiderForeLeg4.update(ticksPerFrame);
		this.spiderForeLeg5.update(ticksPerFrame);
		this.spiderForeLeg6.update(ticksPerFrame);
		this.spiderForeLeg7.update(ticksPerFrame);
		this.spiderForeLeg8.update(ticksPerFrame);
	}

	@Override
	public void onTicksRestart()
	{
	}
}