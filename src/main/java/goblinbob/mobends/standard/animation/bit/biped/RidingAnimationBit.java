package goblinbob.mobends.standard.animation.bit.biped;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.standard.data.BipedEntityData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class RidingAnimationBit extends AnimationBit<BipedEntityData<?>>
{
	private static final String[] ACTIONS = new String[] { "riding" };
	
	private static final float PI = (float) Math.PI;
	
	@Override
	public String[] getActions(BipedEntityData<?> entityData)
	{
		return ACTIONS;
	}

	@Override
	public void perform(BipedEntityData<?> data)
	{
		final EntityLivingBase living = data.getEntity();

		data.root.offset.slideToZero(0.3F);
		data.root.rotation.setSmoothness(.3F).orientZero();
		data.leftHeldItem.rotation.orientZero();
		data.rightHeldItem.rotation.orientZero();
		
		data.head.rotation.orientX(data.headPitch.get())
		  				  .rotateY(data.headYaw.get());
		data.body.rotation.orientY(0).setSmoothness(0.5F);
		
		data.leftLeg.rotation.orientX(-90.0F).rotateZ(-10.0F).rotateY(-25.0F);
		data.rightLeg.rotation.orientX(-90.0F).rotateZ(10.0F).rotateY(25.0F);
		data.leftShin.rotation.orientX(60.0F);
		data.rightShin.rotation.orientX(60.0F);
		
		data.leftArm.rotation.orientX(0.0F).rotateZ(-10F);
		data.leftForearm.rotation.orientX(-10.0F);
		data.rightArm.rotation.orientX(0.0F).rotateZ(10F);
		data.rightForearm.rotation.orientX(-10.0F);
		
		Entity ridden = living.getRidingEntity();
		if (ridden != null && ridden instanceof EntityLivingBase)
		{
			EntityLivingBase riddenLiving = (EntityLivingBase) ridden;
			float relativeHeadYaw = MathHelper.wrapDegrees(living.rotationYaw - riddenLiving.renderYawOffset);
			float relativeYaw = MathHelper.wrapDegrees(living.rotationYaw - data.headYaw.get() - riddenLiving.renderYawOffset);
			
			data.body.rotation.orientZ(MathHelper.clamp(-relativeHeadYaw * 0.25F, -20.0F, 20.0F));
			data.leftLeg.rotation.rotateX(-MathHelper.sin(relativeYaw / 180.0F * PI * 1.5F) * 45.0F);
			data.rightLeg.rotation.rotateX(MathHelper.sin(relativeYaw / 180.0F * PI * 1.5F) * 45.0F);
		}
		
		if (!data.isStillHorizontally())
		{
			data.body.rotation.orientX(25.0F);
			data.leftArm.rotation.orientX(-45.0F).rotateZ(10F);
			data.leftForearm.rotation.orientX(-10.0F);
			data.rightArm.rotation.orientX(-45.0F).rotateZ(-10F);
			data.rightForearm.rotation.orientX(-10.0F);
			
			float motionMagnitude = (float) (Math.sqrt(living.motionX*living.motionX + living.motionZ*living.motionZ)) * 100;
			if (motionMagnitude > 1)
			{
				float ticks = DataUpdateHandler.getTicks() * 0.5F;
				float bodyRotation = 45.0F + MathHelper.cos(ticks) * 10F;
				data.body.rotation.orientX(bodyRotation);
				data.head.rotation.rotateX(-bodyRotation);
				data.leftArm.rotation.rotateX(-bodyRotation);
				data.rightArm.rotation.rotateX(-bodyRotation);
				data.root.offset.slideY(MathHelper.sin(ticks) * 0.3F);
			}
			else
			{
				data.head.rotation.rotateX(-25.0F);
			}
		}
	}
}
