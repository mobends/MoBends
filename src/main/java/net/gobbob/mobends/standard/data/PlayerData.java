package net.gobbob.mobends.standard.data;

import net.gobbob.mobends.core.animation.controller.Controller;
import net.gobbob.mobends.core.client.model.ModelPart;
import net.gobbob.mobends.standard.animation.controller.PlayerController;
import net.gobbob.mobends.standard.main.ModConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.init.Items;
import net.minecraft.util.EnumHand;

public class PlayerData extends BipedEntityData<AbstractClientPlayer>
{
	public ModelPart ears;
	public ModelPart cloak;

	boolean sprintJumpLeg = false;
	boolean sprintJumpLegSwitched = false;
	boolean fistPunchArm = false;
	int currentAttack = 0;

	final PlayerController controller = new PlayerController();
	
	public PlayerData(AbstractClientPlayer entity)
	{
		super(entity);
	}

	@Override
	public void initModelPose()
	{
		super.initModelPose();
		
		Render<AbstractClientPlayer> render = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(this.entity);
		
		if (((RenderPlayer) render).smallArms)
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

	@Override
	public PlayerController getController()
	{
		return this.controller;
	}
}