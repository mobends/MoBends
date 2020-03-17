package goblinbob.mobends.standard.data;

import goblinbob.mobends.core.client.model.ModelPartTransform;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.animation.controller.PlayerController;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{

	boolean sprintJumpLeg = false;
	boolean sprintJumpLegSwitched = false;
	boolean fistPunchArm = false;
	int currentAttack = 0;
	public ModelPartTransform cape;
	
	public PlayerData(AbstractClientPlayer entity)
	{
		super(entity);
	}
	
	private final PlayerController controller = new PlayerController();
	
	@Override
	public PlayerController getController()
	{
		return controller;
	}

	private Boolean flyingStateOverride = null;
	
	public void overrideFlyingState(boolean flying)
	{
		this.flyingStateOverride = flying;
	}
	
	public void unsetFlyingStateOverride()
	{
		this.flyingStateOverride = null;
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
	public void update(float partialTicks)
	{
		super.update(partialTicks);

		if (getTicksAfterAttack() > 20)
		{
			currentAttack = 0;
		}

		if (motionY < 0)
		{
			sprintJumpLegSwitched = false;
		}

		if (!sprintJumpLegSwitched && motionY > 0)
		{
			sprintJumpLeg = !sprintJumpLeg;
			sprintJumpLegSwitched = true;
		}
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
		if (this.entity.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.AIR)
		{
			this.fistPunchArm = !this.fistPunchArm;
			this.ticksAfterAttack = 0;
			return;
		}

		if (this.ticksAfterAttack <= 6.0F)
		{
			// Sword swing cooldown
			return;
		}

		switch (this.currentAttack)
		{
			case 0:
			case 3:
				this.currentAttack = 1;
				break;
			case 1:
				this.currentAttack = 2;
				break;
			case 2:
				this.currentAttack = (!ModConfig.performSpinAttack || this.getEntity().isRiding()) ? 1 : 3;
				break;
		}

		this.ticksAfterAttack = 0;
	}

	public int getCurrentAttack()
	{
		return currentAttack;
	}

	public boolean getFistPunchArm()
	{
		return fistPunchArm;
	}

	public boolean getSprintJumpLeg()
	{
		return sprintJumpLeg;
	}
	
	public boolean isFlying()
	{
		return this.flyingStateOverride != null ?
				this.flyingStateOverride :
				this.entity.capabilities.isFlying;
	}
	
}