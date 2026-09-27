package goblinbob.mobends.standard;

import goblinbob.mobends.core.ModStatics;

import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager;
import goblinbob.mobends.standard.client.renderer.entity.mutated.*;
import goblinbob.mobends.standard.data.*;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.WolfStateCondition;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.*;
import goblinbob.mobends.standard.selector.SkinVariantCondition;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityWolf;

public class DefaultAddon implements IAddon
{
	@Override
	public void registerContent(AddonAnimationRegistry registry)
	{
		registry.registerEntity(new PlayerBender());
		
		registry.registerNewEntity(EntityZombie.class, ZombieData::new, ZombieMutator::new, new ZombieRenderer<>());

		registry.registerNewEntity(EntitySkeleton.class, SkeletonData::new, SkeletonMutator::new, new BipedRenderer<>());

//		registry.registerNewEntity(EntityZombieVillager.class, ZombieVillagerData::new, ZombieVillagerMutator::new, new ZombieRenderer<>(),
//				"head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm",
//				"leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg");
//
		registry.registerNewEntity(EntityPigZombie.class, PigZombieData::new, PigZombieMutator::new, new ZombieRenderer<>());

		registry.registerNewEntity(EntitySpider.class, SpiderData::new, SpiderMutator::new, new SpiderRenderer<>());

		registry.registerNewEntity(EntitySquid.class, SquidData::new, SquidMutator::new, new SquidRenderer<>());

		registry.registerNewEntity(EntityWolf.class, WolfData::new, WolfMutator::new, new WolfRenderer<>());



//		registry.registerEntity(new AnimatedEntity(EntityHusk.class,
//						new RenderBendsHusk(Minecraft.getMinecraft().getRenderManager()),
//						new String[] { "head", "body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "leftLeg",
//								"rightLeg", "leftForeLeg", "rightForeLeg" }));

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

		// Mobs described as data: bends/models/index.json (see misc/kumo-format.md).
		goblinbob.mobends.core.client.definition.DefinedBenders.registerAll(ModStatics.MODID, registry);

		registry.registerTriggerCondition("wolf_state", WolfStateCondition::new, WolfStateCondition.Template.class);
		registry.registerDriver("cape", CapeDriver::create, CapeDriver.Template.class);
		registry.registerDriver("sword_trail", goblinbob.mobends.standard.kumo.SwordTrailDriver::create, goblinbob.mobends.standard.kumo.SwordTrailDriver.Template.class);
		registry.registerDriver("spider_idle_legs", goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver::create, goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate.class);
		registry.registerDriver("spider_moving_legs", goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver::create, goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate.class);
		registry.registerSelectorCondition("skin_variant", SkinVariantCondition::create);
	}

	@Override
	public void onRenderTick(float partialTicks)
	{
		if (ModConfig.showArrowTrails)
			ArrowTrailManager.onRenderTick();
	}
	
	@Override
	public void onRefresh()
	{
		ArmorModelFactory.refresh();
	}
	
	@Override
	public String getDisplayName()
	{
		return "Default";
	}
}
