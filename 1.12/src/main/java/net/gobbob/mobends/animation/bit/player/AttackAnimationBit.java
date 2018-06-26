package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.bit.biped.AttackSlashDownAnimationBit;
import net.gobbob.mobends.animation.bit.biped.AttackSlashUpAnimationBit;
import net.gobbob.mobends.animation.bit.biped.AttackStanceSprintAnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

public class AttackAnimationBit extends AnimationBit
{
	protected HardAnimationLayer layerBase;
	protected AnimationBit bitAttackStance, bitAttackStanceSprint, bitAttackSlashUp, bitAttackSlashDown;

	public AttackAnimationBit()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitAttackStance = new AttackStanceAnimationBit();
		this.bitAttackStanceSprint = new AttackStanceSprintAnimationBit();
		this.bitAttackSlashUp = new AttackSlashUpAnimationBit();
		this.bitAttackSlashDown = new AttackSlashDownAnimationBit();
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
				if (playerData.getTicksAfterAttack() < 10)
				{
					if (playerData.getCurrentAttack() == 1)
					{
						this.layerBase.playOrContinueBit(this.bitAttackSlashUp, playerData);
					}
					else if (playerData.getCurrentAttack() == 2)
					{
						this.layerBase.playOrContinueBit(this.bitAttackSlashDown, playerData);
					}
					else
					{
						this.layerBase.clearAnimation();
					}
				}
				else if (playerData.getTicksAfterAttack() < 60 && playerData.isOnGround())
				{
					if (player.isSprinting())
					{
						this.layerBase.playOrContinueBit(this.bitAttackStanceSprint, playerData);
					}
					else if (playerData.isStillHorizontally())
					{
						this.layerBase.playOrContinueBit(this.bitAttackStance, playerData);
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
				// if (data.getTicksAfterAttack() < 10)
				// {
				// Animation_Attack_Punch.animate((EntityPlayer) argEntity, model, data);
				// BendsPack.animate(data, "player", "attack");
				// BendsPack.animate(data, "player", "punch");
				// } else if (data.getTicksAfterAttack() < 60)
				// {
				// Animation_Attack_PunchStance.animate((EntityPlayer) argEntity, model, data);
				// BendsPack.animate(data, "player", "punch_stance");
				// }
				this.layerBase.clearAnimation();
			}
		}

		this.layerBase.perform(entityData);
	}
}
