package net.gobbob.mobends.animation.controller;

import net.gobbob.mobends.animation.bit.AnimationBit;
import net.gobbob.mobends.animation.layer.HardAnimationLayer;
import net.gobbob.mobends.data.EntityData;
import net.gobbob.mobends.data.PlayerData;
import net.gobbob.mobends.pack.BendsPack;
import net.gobbob.mobends.pack.variable.BendsVariable;
import net.minecraft.block.Block;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

/*
 * This is an animation controller for all players.
 * WARNING There is only one instance for all players
 */
public class PlayerController extends Controller
{
	protected String animationTarget = "player";
	protected HardAnimationLayer layerBase;
	protected HardAnimationLayer layerAttack;
	protected AnimationBit bitStand, bitWalk, bitAttack;

	public PlayerController()
	{
		this.layerBase = new HardAnimationLayer();
		this.layerAttack = new HardAnimationLayer();
		this.bitStand = new net.gobbob.mobends.animation.bit.player.StandAnimationBit(animationTarget);
		this.bitWalk = new net.gobbob.mobends.animation.bit.player.WalkAnimationBit(animationTarget);
		this.bitAttack = new net.gobbob.mobends.animation.bit.player.AttackAnimationBit();
		
		this.layerAttack.playOrContinueBit(this.bitAttack);
	}

	@Override
	public void perform(EntityData entityData)
	{
		if (!(entityData instanceof PlayerData))
			return;
		if (!(entityData.getEntity() instanceof AbstractClientPlayer))
			return;

		PlayerData data = (PlayerData) entityData;
		AbstractClientPlayer player = (AbstractClientPlayer) data.getEntity();
		BendsVariable.tempData = data;
		
		boolean still = data.motion.x == 0.0f && data.motion.z == 0.0f;
		if (still)
		{
			layerBase.playOrContinueBit(bitStand);
		}
		else
		{
			layerBase.playOrContinueBit(bitWalk);
		}

		layerBase.perform(entityData);
		layerAttack.perform(entityData);
	}
}
