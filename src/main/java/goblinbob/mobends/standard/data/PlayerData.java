package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.client.model.ModelPartTransform;
import net.minecraft.util.math.MathHelper;
import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{
	protected boolean sprintJumpLeg = false;
	protected boolean sprintJumpLegSwitched = false;
	protected float capeWavePhase = 0;

	public ModelPartTransform cape;

	private static final ResourceLocation ANIMATOR = new ResourceLocation(ModStatics.MODID, "bends/animators/player.json");

	public PlayerData(AbstractClientPlayer entity)
	{
		super(entity);
	}

	@Override
	protected ResourceLocation getDefaultAnimator()
	{
		return ANIMATOR;
	}

	public float getCapeWavePhase()
	{
		return capeWavePhase;
	}

	@Override
	protected void registerKumoBindings()
	{
		super.registerKumoBindings();
		registerState("FLYING", this::isFlying);
		registerState("SLEEPING", () -> entity != null && entity.isEntityAlive() && entity.isPlayerSleeping());
		registerState("ELYTRA_FLYING", () -> entity != null && entity.getTicksElytraFlying() > 4);
		registerState("SPRINT_JUMP_LEG", () -> sprintJumpLeg);
		// The sword combo ends with a whirl, unless the player turned it off or is riding.
		registerState("CAN_SPIN_ATTACK", () -> ModConfig.performSpinAttack && entity != null && !entity.isRiding());

		registerVariable("flightSpeedFactor", this::getFlightSpeedFactor);
		registerVariable("flightPitch", () -> getMomentumPitch() * getFlightSpeedFactor());
	}

	/** How fast the player flies, 0 to 1 (full at 0.2 blocks per tick). */
	private double getFlightSpeedFactor()
	{
		return Math.min(Math.max(getInterpolatedMotionMagnitude(), 0), 0.2) / 0.2;
	}

	/** The angle between the motion and straight up, in degrees. */
	private double getMomentumPitch()
	{
		return MathHelper.atan2(getInterpolatedXZMotionMagnitude(), getMotionY()) * 180.0D / Math.PI;
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();
		
		Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entity);

		cape = new ModelPartTransform(body);
		nameToPartMap.put("cape", cape);
		cape.position.set(0F, 0F, 0F);

		if (((RenderPlayer) render).smallArms)
		{
			rightArm.position.set(-5F, -9.5F, 0F);
			leftArm.position.set(5F, -9.5F, 0F);
		}
	}

	@Override
	public void updateParts(float ticksPerFrame)
	{
		super.updateParts(ticksPerFrame);

		cape.update(ticksPerFrame);
	}

	@Override
	public void update(float partialTicks)
	{
		super.update(partialTicks);

		if (motionY < 0)
		{
			sprintJumpLegSwitched = false;
		}

		if (!sprintJumpLegSwitched && motionY > 0)
		{
			sprintJumpLeg = !sprintJumpLeg;
			sprintJumpLegSwitched = true;
		}

		// The cape ripples faster while flying fast.
		final float capeWaveSpeed = isFlying() && entity.isSprinting() ? 4.0F : 1.0F;
		this.capeWavePhase += capeWaveSpeed * DataUpdateHandler.ticksPerFrame;
		if (this.capeWavePhase > 380.0F)
			this.capeWavePhase -= 380.0F;
	}

	@Override
	public void onLiftoff()
	{
		super.onLiftoff();
		if (!sprintJumpLegSwitched)
		{
			sprintJumpLeg = !sprintJumpLeg;
			sprintJumpLegSwitched = true;
		}
	}

	@Override
	public void onAttack()
	{
		// A sword swing right after another one doesn't start a new slash (the animator reacts to
		// ticksAfterAttack going back to 0); punches always do.
		if (this.entity.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.AIR && this.ticksAfterAttack <= 6.0F)
		{
			return;
		}
		super.onAttack();
	}

	public boolean isFlying()
	{
		return this.entity.capabilities.isFlying;
	}
	
}