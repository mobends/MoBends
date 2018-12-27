package net.gobbob.mobends.standard.data;

import java.util.Random;

import net.gobbob.mobends.core.client.event.DataUpdateHandler;
import net.gobbob.mobends.standard.animation.controller.ZombieController;
import net.minecraft.entity.monster.EntityZombie;

public class ZombieData extends BipedEntityData<ZombieData, EntityZombie>
{
	public static final int ANIMATION_SETS_AMOUNT = 2;

	/*
	 * This determines what set of animations this zombie should get.
	 */
	protected int animationSet = 0;
	protected int currentWalkingState = 0;
	protected float ticksBeforeStateChange = 0;

	public ZombieData(EntityZombie entity)
	{
		super(entity);
		this.controller = new ZombieController();
		// Getting a pseudo-random animationType based on something
		// that is shared across clients, so that every players
		// sees the same variation
		this.animationSet = ((int) (entity.getEntityId() * 3.61352F)) % ANIMATION_SETS_AMOUNT;
	}

	public int getAnimationSet()
	{
		return this.animationSet;
	}
	
	public int getCurrentWalkingState()
	{
		return this.currentWalkingState;
	}
	
	@Override
	public void update(float partialTicks)
	{
		super.update(partialTicks);

		this.ticksBeforeStateChange -= DataUpdateHandler.ticksPerFrame;

		if (this.ticksBeforeStateChange <= 0)
		{
			Random random = new Random();
			this.currentWalkingState = random.nextInt(2);
			this.ticksBeforeStateChange = 80 + random.nextInt(20);
		}
	}

	@Override
	public void onTicksRestart()
	{
	}
}