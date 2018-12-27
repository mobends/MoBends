package net.gobbob.mobends.data;

import net.gobbob.mobends.animation.controller.PlayerController;
import net.gobbob.mobends.client.mutators.PlayerMutator;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.core.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class PlayerData extends BipedEntityData<PlayerData, AbstractClientPlayer>
{
	public ModelPart ears;
	public ModelPart cloak;

	boolean sprintJumpLeg = false;
	boolean sprintJumpLegSwitched = false;
	boolean fistPunchArm = false;
	int currentAttack = 0;

	public PlayerData(AbstractClientPlayer entity)
	{
		super(entity);
		this.controller = new PlayerController();
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();

		Render<Entity> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entity);
		PlayerMutator mutator = PlayerMutator.getMutatorForRenderer(render);
		if (mutator == null)
			return;

		if (mutator.hasSmallArms())
		{
			this.rightArm.position.set(-5F, -9.5F, 0F);
			this.leftArm.position.set(5F, -9.5F, 0F);
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
	public void onPunch()
	{
		if (this.entity.getHeldItem(EnumHand.MAIN_HAND).getItem() != Items.AIR)
		{
			if (this.getTicksAfterAttack() > 6.0f)
			{
				if (this.currentAttack == 0)
				{
					this.currentAttack = 1;
					this.ticksAfterAttack = 0;
				}
				else
				{
					if (this.getTicksAfterAttack() < 15.0f)
					{
						if (this.currentAttack == 1)
							this.currentAttack = 2;
						else if (this.currentAttack == 2)
						{
							this.currentAttack = (!ModConfig.performSpinAttack || this.getEntity().isRiding()) ? 1 : 3;
						}
						else if (this.currentAttack == 3)
							this.currentAttack = 1;
						this.ticksAfterAttack = 0;
					}
				}
			}
		}
		else
		{
			this.fistPunchArm = !this.fistPunchArm;
			this.ticksAfterAttack = 0;
		}
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
}