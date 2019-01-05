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
    
    public Limb[] limbs;
    
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
		this.limbs = new Limb[8];
		
		for (int i = 0; i < limbs.length; ++i)
		{
			limbs[i] = new Limb(this, i);
			nameToPartMap.put("leg" + (i+1), limbs[i].upperPart);
			nameToPartMap.put("foreLeg" + (i+1), limbs[i].lowerPart);
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
		
		for (Limb limb : limbs)
		{
			limb.upperPart.update(ticksPerFrame);
			limb.lowerPart.update(ticksPerFrame);
		}
	}
	
	@Override
	public void updateClient()
	{
		super.updateClient();
		
		for (Limb limb : limbs)
		{
			limb.updateClient();
		}
	}
	
	public static class Limb
	{
		final SpiderData data;
		final public ModelPartTransform upperPart;
		final public ModelPartTransform lowerPart;
		final int index;
		final boolean odd;
		final double naturalYaw;
		
		double worldX, worldZ, prevWorldX, prevWorldZ;
		double adjustTargetX = 0;
		double adjustTargetZ = 0;
		float adjustingProgress = 1F;
		
		public Limb(SpiderData data, int index)
		{
			this.data = data;
			this.upperPart = new ModelPartTransform();
			this.lowerPart = new ModelPartTransform();
			this.index = index;
			this.odd = index % 2 == 1;
			
			double naturalYaw = (double) this.index / (data.limbs.length-1) * 2 - 1;
			this.naturalYaw = this.odd ? (naturalYaw*1.3) : (Math.PI - naturalYaw*1.3);
			
			int z = 2 - (index / 2);
			this.upperPart.position.set(odd ? 4F : -4F, 15F, z);
			this.lowerPart.position.set(odd ? 11F : -11F, -1F, 0F);
			this.resetPosition();
		}
		
		void resetPosition()
		{
			final float distance = 1;
			final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
			
        	this.worldX = Math.cos(this.naturalYaw + bodyYaw) * distance + data.getPositionX();
        	this.worldZ = Math.sin(this.naturalYaw + bodyYaw) * distance + data.getPositionZ();
        	this.prevWorldX = this.worldX;
        	this.prevWorldZ = this.worldZ;
		}
		
		void updateClient()
		{
			this.prevWorldX = this.worldX;
			this.prevWorldZ = this.worldZ;
			
			if (adjustingProgress < 1)
			{
				adjustingProgress += 0.2;
				if (adjustingProgress >= 1)
				{
					this.worldX = this.adjustTargetX;
					this.worldZ = this.adjustTargetZ;
					adjustingProgress = 1;
				}
				else
				{
					this.worldX += (this.adjustTargetX - this.worldX) * 0.2;
					this.worldZ += (this.adjustTargetZ - this.worldZ) * 0.2;
				}
			}
		}
		
		public void adjustToNeutralPosition()
		{
			if (adjustingProgress != 1)
				return;
			
			adjustingProgress = 0;
			
			final float distance = 1.2F;
			final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
			this.adjustTargetX = Math.cos(this.naturalYaw + bodyYaw) * distance + data.getPositionX();
			this.adjustTargetZ = Math.sin(this.naturalYaw + bodyYaw) * distance + data.getPositionZ();
		}
		
		public void adjustToWorldPosition(double x, double z)
		{
			if (adjustingProgress != 1)
				return;
			
			adjustingProgress = 0;
			this.adjustTargetX = x;
			this.adjustTargetZ = z;
		}
		
		/*
		public void applyIK(double bodyX, double bodyZ, double groundLevel, float pt)
		{
			final double renderYawOffset = (data.entity.prevRenderYawOffset + (data.entity.renderYawOffset - data.entity.prevRenderYawOffset) * pt ) / 180 * Math.PI;
			
			double worldLimbX = (this.prevWorldX + (this.worldX - this.prevWorldX) * pt);
        	double worldLimbZ = (this.prevWorldZ + (this.worldZ - this.prevWorldZ) * pt);
        	double localX = (worldLimbX - worldX) / 0.0625;
        	double localZ = -(worldLimbZ - worldZ) / 0.0625;
        	double x = localX * Math.cos(renderYawOffset) - localZ * Math.sin(renderYawOffset) - bodyX;
        	double z = localX * Math.sin(renderYawOffset) + localZ * Math.cos(renderYawOffset) - bodyZ;
        	double partX = this.upperPart.position.x;
        	double partZ = this.upperPart.position.z;
        	double dX = (partX - x);
        	double dZ = (partZ - z);
        	double xzDistance = Math.sqrt(dX * dX + dZ * dZ);
        	double xzAngle = Math.atan2(dX, dZ);
        	double deviation = GUtil.getRadianDifference(this.naturalYaw, xzAngle + Math.PI/2);
		}
		*/
		
		public double getNaturalYaw()
		{
			return this.naturalYaw;
		}
		
		public double getPrevWorldX()
		{
			return this.prevWorldX;
		}
		
		public double getPrevWorldZ()
		{
			return this.prevWorldZ;
		}
		
		public double getWorldX()
		{
			return this.worldX;
		}
		
		public double getWorldZ()
		{
			return this.worldZ;
		}
		
		public float getAdjustingProgress()
		{
			return this.adjustingProgress;
		}
	}
	
}