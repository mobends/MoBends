package net.gobbob.mobends.animation.bit.player;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.AnimationLayer;
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
	protected AnimationBit bitAttackStance;

	public AttackAnimationBit()
	{
		this.layerBase = new HardAnimationLayer();
		this.bitAttackStance = new AttackStanceAnimationBit();
	}

	@Override
	public void perform(EntityData entityData)
	{
		this.layerBase.clearAnimation();
		
		if (!(entityData instanceof PlayerData))
			return;

		PlayerData data = (PlayerData) entityData;
		Entity entity = entityData.getEntity();
		if (entity instanceof AbstractClientPlayer)
		{
			AbstractClientPlayer player = (AbstractClientPlayer) entity;
			ItemStack heldItemStack = player.getHeldItem(EnumHand.MAIN_HAND);
			if (heldItemStack != null && heldItemStack.getItem() != Items.AIR)
			{
				if (data.getTicksAfterAttack() < 10)
				{
					// if (data.getCurrentAttack() == 1)
					// {
					// Animation_Attack_Combo0.animate((EntityPlayer) argEntity, model, data);
					// BendsPack.animate(data, "player", "attack");
					// BendsPack.animate(data, "player", "attack_0");
					// } else if (data.getCurrentAttack() == 2)
					// {
					// Animation_Attack_Combo1.animate((EntityPlayer) argEntity, model, data);
					// BendsPack.animate(data, "player", "attack");
					// BendsPack.animate(data, "player", "attack_1");
					// } else if (data.getCurrentAttack() == 3)
					// {
					// Animation_Attack_Combo2.animate((EntityPlayer) argEntity, model, data);
					// BendsPack.animate(data, "player", "attack");
					// BendsPack.animate(data, "player", "attack_2");
					// }
				}
				else if (data.getTicksAfterAttack() < 60)
				{
					this.layerBase.playBit(this.bitAttackStance);
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
			}
		}

		this.layerBase.perform(entityData);
	}
}
