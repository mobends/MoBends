package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.bit.biped.AttackStanceSprintAnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AttackAnimationBit extends AnimationBit
{
	protected HardAnimationLayer layerBase;
	protected AnimationBit bitAttackStance, bitAttackStanceSprint, bitAttackSlashUp, bitAttackSlashDown,
			bitAttackWhirlSlash, bitFistGuard, bitPunch;

	public AttackAnimationBit()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitAttackStance = new AttackStanceAnimationBit();
		this.bitAttackStanceSprint = new AttackStanceSprintAnimationBit();
		/*this.bitAttackSlashUp = new AttackSlashUpAnimationBit();
		this.bitAttackSlashDown = new AttackSlashDownAnimationBit();
		this.bitAttackWhirlSlash = new AttackWhirlSlashAnimationBit();
		this.bitFistGuard = new FistGuardAnimationBit();
		this.bitPunch = new PunchAnimationBit();*/
	}

	@Override
	public String[] getActions(EntityData entityData)
	{
		if (this.layerBase.isPlaying())
		{
			return this.layerBase.getPerformedBit().getActions(entityData);
		}

		return new String[] {};
	}

	public boolean shouldPerformAttack(AbstractClientPlayer player)
	{
		ItemStack heldItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
		return heldItemStack != null && heldItemStack.getItem() != Items.AIR;
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;

		PlayerData playerData = (PlayerData) entityData;
		Entity entity = entityData.getEntity();
		if (entity instanceof AbstractClientPlayer)
		{
			AbstractClientPlayer player = (AbstractClientPlayer) entity;
			if (this.shouldPerformAttack(player))
			{
				/*if (playerData.getTicksAfterAttack() < 10)
				{
					if (playerData.getCurrentAttack() == 1)
					{
						this.layerBase.playOrContinueBit(this.bitAttackSlashUp, playerData);
					}
					else if (playerData.getCurrentAttack() == 2)
					{
						this.layerBase.playOrContinueBit(this.bitAttackSlashDown, playerData);
					}
					else if (playerData.getCurrentAttack() == 3)
					{
						this.layerBase.playOrContinueBit(this.bitAttackWhirlSlash, playerData);
					}
					else
					{
						this.layerBase.clearAnimation();
					}
				}
				else */if (playerData.getTicksAfterAttack() < 60 && playerData.isOnGround())
				{
					if (player.isSprinting())
					{
						this.layerBase.playOrContinueBit(this.bitAttackStanceSprint, entityData);
					}
					else if (playerData.isStillHorizontally())
					{
						this.layerBase.playOrContinueBit(this.bitAttackStance, entityData);
					}
					else
					{
						this.layerBase.clearAnimation();
					}
				}
				else
				{
					this.layerBase.clearAnimation();
				}
			}
			else
			{
				/*if (playerData.getTicksAfterAttack() < 10)
				{
					this.layerBase.playOrContinueBit(this.bitPunch, entityData);
				}
				else if (playerData.getTicksAfterAttack() < 60 && playerData.isStillHorizontally())
				{
					this.layerBase.playOrContinueBit(this.bitFistGuard, entityData);
				}
				else
				{
					this.layerBase.clearAnimation();
				}*/
			}
		}

		this.layerBase.perform(entityData);
	}
}
