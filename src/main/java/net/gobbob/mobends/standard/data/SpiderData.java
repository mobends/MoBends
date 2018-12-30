package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.core.client.model.ModelPartTransform;
import net.gobbob.mobends.core.data.LivingEntityData;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.animation.controller.SpiderController;
import net.minecraft.entity.monster.EntitySpider;

public class SpiderData extends LivingEntityData<EntitySpider>
{
	
	public ModelPartTransform spiderHead;
    public ModelPartTransform spiderNeck;
    public ModelPartTransform spiderBody;
    
    public Limb[] upperLimbs;
    public Limb[] lowerLimbs;
    
    private final SpiderController controller = new SpiderController();
    
	public SpiderData(EntitySpider entity)
	{
		super(entity);
	}
	
	@Override
	public SpiderController getController()
	{
		return controller;
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
		this.upperLimbs = new Limb[8];
		this.lowerLimbs = new Limb[8];
		
		for (int i = 0; i < upperLimbs.length; ++i)
		{
			int z = 2 - (i/2);
			upperLimbs[i] = new Limb(entity, i%2 == 0 ? -4F : 4F, 15.0F, z);
			nameToPartMap.put("leg" + (i+1), upperLimbs[i].part);
		}
		
		for (int i = 0; i < lowerLimbs.length; ++i)
		{
			lowerLimbs[i] = new Limb(entity, i%2 == 0 ? -11F : 11F, -1F, 0F);
			nameToPartMap.put("foreLeg" + (i+1), lowerLimbs[i].part);
		}
		
        nameToPartMap.put("body", spiderBody);
        nameToPartMap.put("neck", spiderNeck);
        nameToPartMap.put("head", spiderHead);
        
        this.spiderHead.position.set(0.0F, 15.0F, -3.0F);
        this.spiderNeck.position.set(0.0F, 15.0F, 0.0F);
        this.spiderBody.position.set(0.0F, 15.0F, 9.0F);
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);
		
		this.spiderBody.update(ticksPerFrame);
		this.spiderNeck.update(ticksPerFrame);;
		this.spiderHead.update(ticksPerFrame);
		
		for (Limb limb : upperLimbs)
		{
			limb.part.update(ticksPerFrame);
		}
		
		for (Limb part : lowerLimbs)
		{
			part.part.update(ticksPerFrame);
		}
	}
	
	public static class Limb
	{
		public ModelPartTransform part;
		double worldX;
		double worldZ;
		
		public Limb(EntitySpider entity, float x, float y, float z)
		{
			this.part = new ModelPartTransform();
			this.part.position.set(x, y, z);
			this.resetPosition(entity);
		}
		
		public void resetPosition(EntitySpider entity)
		{
			final float distance = 2;
			float bodyYaw = entity.renderYawOffset / 180F * GUtil.PI;
			this.worldX = entity.posX + Math.cos(bodyYaw) * distance;
			this.worldZ = entity.posZ + Math.sin(bodyYaw) * distance;
		}
	}
	
}