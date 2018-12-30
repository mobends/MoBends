package net.gobbob.mobends.standard;

import net.gobbob.mobends.core.addon.IAddon;
import net.gobbob.mobends.core.animatedentity.AddonAnimationRegistry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.standard.client.mutators.PigZombieMutator;
import net.gobbob.mobends.standard.client.mutators.PlayerMutator;
import net.gobbob.mobends.standard.client.mutators.SpiderMutator;
import net.gobbob.mobends.standard.client.mutators.SquidMutator;
import net.gobbob.mobends.standard.client.mutators.ZombieMutator;
import net.gobbob.mobends.standard.client.mutators.ZombieVillagerMutator;
import net.gobbob.mobends.standard.client.renderer.entity.ArrowTrail;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.PlayerRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.SpiderRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.SquidRenderer;
import net.gobbob.mobends.standard.client.renderer.entity.mutated.ZombieRenderer;
import net.gobbob.mobends.standard.data.PigZombieData;
import net.gobbob.mobends.standard.data.PlayerData;
import net.gobbob.mobends.standard.data.SpiderData;
import net.gobbob.mobends.standard.data.SquidData;
import net.gobbob.mobends.standard.data.ZombieData;
import net.gobbob.mobends.standard.data.ZombieVillagerData;
import net.gobbob.mobends.standard.main.ModConfig;
import net.gobbob.mobends.standard.previewer.BipedPreviewer;
import net.gobbob.mobends.standard.previewer.PlayerPreviewer;
import net.gobbob.mobends.standard.previewer.SpiderPreviewer;
import net.gobbob.mobends.standard.previewer.ZombiePreviewer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
import net.minecraft.entity.passive.EntitySquid;

public class DefaultAddon implements IAddon
{
	
	public static final AnimatedEntity<AbstractClientPlayer> PLAYER = new AnimatedEntity<>("player", "mobends.player", AbstractClientPlayer.class,
			PlayerData::new, PlayerMutator::new, new PlayerRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg", "rightLeg",
					"leftForeLeg", "rightForeLeg", "totalRotation", "leftItemRotation",
					"rightItemRotation" });
	
	public static final AnimatedEntity<EntityZombie> ZOMBIE = new AnimatedEntity<>(EntityZombie.class,
			ZombieData::new, ZombieMutator::new, new ZombieRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
					"rightLeg", "leftForeLeg", "rightForeLeg" }).setPreviewer(new ZombiePreviewer());
	
	public static final AnimatedEntity<EntityZombieVillager> ZOMBIE_VILLAGER = new AnimatedEntity<>(EntityZombieVillager.class,
			ZombieVillagerData::new, ZombieVillagerMutator::new, new ZombieRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
					"rightLeg", "leftForeLeg", "rightForeLeg" }).setPreviewer(new BipedPreviewer());
	
	public static final AnimatedEntity<EntityPigZombie> PIG_ZOMBIE = new AnimatedEntity<>(EntityPigZombie.class,
			PigZombieData::new, PigZombieMutator::new, new ZombieRenderer(),
			new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm",
					"rightForeArm", "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg" }).setPreviewer(new BipedPreviewer());
	
	public static final AnimatedEntity<EntitySpider> SPIDER = new AnimatedEntity<>(EntitySpider.class,
			SpiderData::new, SpiderMutator::new, new SpiderRenderer(),
			new String[] { "head", "body", "neck", "leg1", "leg2", "leg3", "leg4", "leg5", "leg6", "leg7",
					"leg8", "foreLeg1", "foreLeg2", "foreLeg3", "foreLeg4", "foreLeg5", "foreLeg6",
					"foreLeg7", "foreLeg8" }).setPreviewer(new SpiderPreviewer());
	
	public static final AnimatedEntity<EntitySquid> SQUID = new AnimatedEntity<>(EntitySquid.class,
			SquidData::new, SquidMutator::new, new SquidRenderer(),
			new String[] { "body", "tentacle1", "tentacle2", "tentacle3", "tentacle4", "tentacle5", "tentacle6",
					"tentacle7", "tentacle8" });
	
	@Override
	public void registerAnimatedEntities(AddonAnimationRegistry registry)
	{
		PLAYER.setAlterEntry(new PlayerAlterEntry());
		PLAYER.setPreviewer(new PlayerPreviewer());
		
		registry.registerEntity(PLAYER);
		registry.registerEntity(ZOMBIE);
		registry.registerEntity(ZOMBIE_VILLAGER);
		registry.registerEntity(PIG_ZOMBIE);
		registry.registerEntity(SPIDER);
		registry.registerEntity(SQUID);
		
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
	public void onRenderTick(float partialTicks)
	{
		if (ModConfig.showArrowTrails)
			ArrowTrail.onRenderTick();
		PlayerPreviewer.updatePreviewData(partialTicks);
	}
	
	@Override
	public void onClientTick()
	{
		PlayerPreviewer.updatePreviewDataClient();
	}
	
	@Override
	public String getDisplayName()
	{
		return "Default";
	}

}
