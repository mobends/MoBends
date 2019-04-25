package net.gobbob.mobends.standard.animation.bit.spider;

import net.gobbob.mobends.core.animation.bit.AnimationBit;
import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.core.util.GUtil;
import net.gobbob.mobends.standard.data.SpiderData;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.util.math.MathHelper;

public class SpiderMoveAnimationBit extends AnimationBit<SpiderData>
{
	protected static final float KNEEL_DURATION = 10F;
	protected char limbFlags = 0x00;
	
	@Override
	public String[] getActions(SpiderData data)
	{
		return new String[] { "move" };
	}
	
	@Override
	public void onPlay(SpiderData data)
	{
		this.limbFlags = 0x00;
	}
	
	@Override
	public void perform(SpiderData data)
	{
		final float ticks = DataUpdateHandler.getTicks();
		final float pt = DataUpdateHandler.partialTicks;
		EntitySpider spider = data.getEntity();
		
		final float headYaw = data.headYaw.get();
		final float headPitch = data.headPitch.get();
		final float limbSwing = data.limbSwing.get() * 0.6662F;
		final float limbSwingAmount = data.limbSwingAmount.get();
		
		double groundLevel = Math.sin(ticks * 0.1F) * 0.5;
		final float touchdown = Math.min(data.getTicksAfterTouchdown() / KNEEL_DURATION, 1.0F);
		
		if (touchdown < 1.0F)
		{
			float touchdownInv = 1.0F - touchdown;
			groundLevel += Math.sin((touchdown * 1.2F - 0.2F) * Math.PI * 2) * 3.0F * touchdownInv;
		}
		
		data.spiderHead.rotation.orientInstantX(headPitch);
		data.spiderHead.rotation.rotateY(headYaw).finish();
        
    	final double bodyX = Math.sin(ticks * 0.2F) * 0.4;
    	final double bodyZ = Math.cos(ticks * 0.2F) * 0.4;
    	
    	final double advanceX = -data.getSidewaysMomentum() * 10;
    	final double advanceZ = data.getForwardMomentum() * 11;
    	final double xSpread = 4*0.0625;
    	final double zSpread = -9*0.0625;

		for (int i = 0; i < data.limbs.length; ++i)
		{
			SpiderData.Limb limb = data.limbs[i];
			limb.adjustToLocalPosition(limb.upperPart.position.x*xSpread + advanceX, limb.upperPart.position.z*zSpread, 0.2F);
		}

        /*for (int i = 0; i < data.limbs.length; ++i)
        {
        	final int set = (int)(i/2);
        	
        	SpiderData.Limb limb = data.limbs[i];
        	SpiderData.IKResult ikResult = limb.solveIK(bodyX, bodyZ, pt);
        	
        	final double cycle = Math.sin(limbSwing + (int)((i+1)/2)*GUtil.PI );
        	final double maxDistance = 1.5 + set*0.5;
        	
        	if ((cycle > 0.5 && (limbFlags & (1 << i)) == 0) || ikResult.xzDistance*0.0625 > maxDistance || ikResult.xzDistance*0.0625 < 0.1)
    		{
        		limb.adjustToLocalPosition(limb.upperPart.position.x*xSpread + advanceX, limb.upperPart.position.z*zSpread + advanceZ + 0.8, 0.2F);
        		// Setting the flag
        		limbFlags |= 1 << i;
    		}
        	
        	
        	if (cycle <= 0.5)
        	{
        		// Removing the flag
        		limbFlags &= ~(1 << i);
        	}

        	//limb.applyIK(ikResult, groundLevel// + Math.max(0, cycle) * 7//, 7, pt);
        }*/
        
        double advance = data.getForwardMomentum();
        
        data.renderOffset.set((float) bodyX, (float) -groundLevel, (float) -bodyZ);
        data.renderRotation.orientZero();
	}
}
