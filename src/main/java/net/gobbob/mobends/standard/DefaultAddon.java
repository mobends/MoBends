package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.addon.IAddon;
import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;
import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.standard.client.mutators.PigZombieMutator;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.gobbob.mobends.standard.client.mutators.SpiderMutator;
import net.gobbob.mobends.standard.client.mutators.SquidMutator;
import net.gobbob.mobends.standard.client.mutators.ZombieMutator;
import net.gobbob.mobends.standard.client.mutators.ZombieVillagerMutator;
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
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntitySquid;

public class DefaultAddon implements IAddon
{
	
	public static final AnimatedEntity PLAYER = new AnimatedEntity("player", "mobends.player", AbstractClientPlayer.class,
			PlayerMutator::new, new PlayerRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg",
					"leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation",
					"rightItemRotation" }).setPreviewer(new PlayerPreviewer());
	
	public static final AnimatedEntity ZOMBIE = new AnimatedEntity(EntityZombie.class, ZombieMutator::new, new ZombieRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
					"rightLeg", "leftForeLeg", "rightForeLeg" });
	
	public static final AnimatedEntity ZOMBIE_VILLAGER = new AnimatedEntity(EntityZombieVillager.class, ZombieVillagerMutator::new, new ZombieRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
					"rightLeg", "leftForeLeg", "rightForeLeg" });
	
	@Override
	public void registerAnimatedEntities(AddonAnimationRegistry registry)
	{
		PLAYER.setAlterEntry(new PlayerAlterEntry());
		
		registry.registerEntity(PLAYER);
		registry.registerEntity(ZOMBIE);
		registry.registerEntity(ZOMBIE_VILLAGER);
		
		registry.registerEntity(new AnimatedEntity(EntityPigZombie.class, PigZombieMutator::new, new ZombieRenderer(),
						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm",
								"rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg" }));

		registry.registerEntity(new AnimatedEntity(EntitySpider.class, SpiderMutator::new, new SpiderRenderer(),
						new String[] { "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7",
								"leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6",
								"foreLeg7", "foreLeg8" }).setPreviewer(new SpiderPreviewer()));

		registry.registerEntity(new AnimatedEntity(EntitySquid.class, SquidMutator::new, new SquidRenderer(),
						new String[] { "body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6",
								"tentacle7", "tentacle8" }));
		
		
		
//		registry.registerEntity(new AnimatedEntity(EntityHusk.class,
//						new RenderBendsHusk(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
//								"rightLeg", "leftForeLeg", "rightForeLeg" }));
//		registry.registerEntity(new AnimatedEntity(EntityCaveSpider.class,
//						new RenderBendsCaveSpider(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7",
//								"leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6",
//								"foreLeg7", "foreLeg8" }));
//		registry.registerEntity(new AnimatedEntity(EntitySkeleton.class,
//						new RenderBendsSkeleton(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
//								"rightLeg", "leftForeLeg", "rightForeLeg" }));
//		registry.registerEntity(new AnimatedEntity(EntityWitherSkeleton.class,
//						new RenderBendsWitherSkeleton(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
//								"rightLeg", "leftForeLeg", "rightForeLeg" }));
//		registry.registerEntity(new AnimatedEntity(EntityStray.class,
//						new RenderBendsStray(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
//								"rightLeg", "leftForeLeg", "rightForeLeg" }));
	}

	@Override
	public String getDisplayName()
	{
		return "Default";
	}

}
