package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.addon.IAddon;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.standard.client.mutators.PigZombieMutator;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.gobbob.mobends.standard.client.mutators.SpiderMutator;
import net.gobbob.mobends.standard.client.mutators.SquidMutator;
import net.gobbob.mobends.standard.client.mutators.ZombieMutator;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.PlayerRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.SpiderRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.SquidRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.ZombieRenderer;
import net.gobbob.mobends.standard.previewer.PlayerPreviewer;
import net.gobbob.mobends.standard.previewer.SpiderPreviewer;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySquid;

public class DefaultAddon implements IAddon
{

	@Override
	public void registerAnimatedEntities(AnimatedEntityRegistry registry)
	{
		registry.registerEntity(new AnimatedEntity<AbstractClientPlayer>("player", "Player", AbstractClientPlayer.class,
						PlayerMutator::apply, PlayerMutator::deapply, PlayerMutator::refresh, new PlayerRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation",
								"rightItemRotation" }).setPreviewer(new PlayerPreviewer()));

		registry.registerEntity(new AnimatedEntity<EntityPigZombie>("pig_zombie", "Zombie Pigman", EntityPigZombie.class,
						PigZombieMutator::apply, PigZombieMutator::deapply, PigZombieMutator::refresh,
						new ZombieRenderer(), new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm",
								"rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg" }));

		registry.registerEntity(new AnimatedEntity<EntityZombie>("zombie", "Zombie", EntityZombie.class, ZombieMutator::apply,
						ZombieMutator::deapply, ZombieMutator::refresh, new ZombieRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
								"rightLeg", "leftForeLeg", "rightForeLeg" }));

		registry.registerEntity(new AnimatedEntity<EntitySpider>("spider", "Spider", EntitySpider.class, SpiderMutator::apply,
						SpiderMutator::deapply, SpiderMutator::refresh, new SpiderRenderer(),
						new String[] { "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7",
								"leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6",
								"foreLeg7", "foreLeg8" }).setPreviewer(new SpiderPreviewer()));

		registry.registerEntity(new AnimatedEntity<EntitySquid>("squid", "Squid", EntitySquid.class, SquidMutator::apply,
						SquidMutator::deapply, SquidMutator::refresh, new SquidRenderer(),
						new String[] { "body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6",
								"tentacle7", "tentacle8" }));
	}

	@Override
	public String getDisplayName()
	{
		return "Default";
	}

}
