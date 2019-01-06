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
		float adjustingSpeed = 0.2F;
		
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
				adjustingProgress += this.adjustingSpeed;
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
			
			this.adjustingSpeed = 0.2F;
			this.adjustingProgress = 0;
			
			final float distance = 1.2F;
			final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
			this.adjustTargetX = Math.cos(this.naturalYaw + bodyYaw) * distance + data.getPositionX();
			this.adjustTargetZ = Math.sin(this.naturalYaw + bodyYaw) * distance + data.getPositionZ();
		}
		
		public void adjustToWorldPosition(double x, double z, float adjustingSpeed)
		{
			if (this.adjustingProgress != 1)
				return;
			
			this.adjustingSpeed = adjustingSpeed;
			this.adjustingProgress = 0;
			this.adjustTargetX = x;
			this.adjustTargetZ = z;
		}
		
		public void adjustToLocalPosition(double x, double z, float adjustingSpeed)
		{
			if (this.adjustingProgress != 1)
				return;
			
			this.adjustingSpeed = adjustingSpeed;
			this.adjustingProgress = 0;
			final float distance = 1.2F;
			final float bodyYaw = data.entity.renderYawOffset / 180F * GUtil.PI;
			this.adjustTargetX = x * Math.cos(bodyYaw) - z * Math.sin(bodyYaw) + data.getPositionX();
			this.adjustTargetZ = x * Math.sin(bodyYaw) + z * Math.cos(bodyYaw) + data.getPositionZ();
		}
		
		public IKResult solveIK(double bodyX, double bodyZ, float pt)
		{
			final double renderYawOffset = (data.entity.prevRenderYawOffset + (data.entity.renderYawOffset - data.entity.prevRenderYawOffset) * pt ) / 180F * Math.PI;
			final double spiderX = data.entity.prevPosX + (data.entity.posX - data.entity.prevPosX) * pt;
	    	final double spiderZ = data.entity.prevPosZ + (data.entity.posZ - data.entity.prevPosZ) * pt;
			final double worldLimbX = this.prevWorldX + (this.worldX - this.prevWorldX) * pt;
			final double worldLimbZ = this.prevWorldZ + (this.worldZ - this.prevWorldZ) * pt;
			final double x = (worldLimbX - spiderX) / 0.0625;
        	final double z = -(worldLimbZ - spiderZ) / 0.0625;
        	final double localX = x * Math.cos(renderYawOffset) - z * Math.sin(renderYawOffset) - bodyX;
        	final double localZ = x * Math.sin(renderYawOffset) + z * Math.cos(renderYawOffset) - bodyZ;
        	final double deltaX = (this.upperPart.position.x - localX);
        	final double deltaZ = (this.upperPart.position.z - localZ);
        	final double xzDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
        	final double xzAngle = Math.atan2(deltaX, deltaZ);
        	
			return new IKResult(
				worldLimbX, worldLimbZ,
				localX, localZ,
				deltaX, deltaZ,
				xzDistance, xzAngle
			);
		}
		
		public void applyIK(IKResult result, double groundLevel, double liftHeight, float pt)
		{
        	double xzAngle = odd ? (Math.PI/2 + result.xzAngle) : (-Math.PI/2 + result.xzAngle);
        	this.upperPart.rotation.orientY((float)(xzAngle / Math.PI * 180F));
        	this.lowerPart.rotation.orientZero();
        	SpiderController.putLimbOnGround(this.upperPart.rotation, this.lowerPart.rotation, this.odd, result.xzDistance, groundLevel - 7 + Math.sin(this.adjustingProgress * Math.PI) * liftHeight);
		}
		
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

		public boolean isOdd()
		{
			return this.odd;
		}
	}
	
	public static class IKResult
	{
		IKResult(double worldX, double worldZ, double localX, double localZ, double deltaX, double deltaZ, double xzDistance, double xzAngle)
		{
			this.worldX = worldX;
			this.worldZ = worldZ;
			this.localX = localX;
			this.localZ = localZ;
			this.deltaX = deltaX;
			this.deltaZ = deltaZ;
			this.xzDistance = xzDistance;
			this.xzAngle = xzAngle;
		}
		
		public final double worldX, worldZ;
		public final double localX, localZ;
		public final double deltaX, deltaZ;
		public final double xzDistance;
		public final double xzAngle;
	}
}