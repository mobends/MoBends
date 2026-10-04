package goblinbob.mobends.standard;

import goblinbob.mobends.core.addon.AddonAnimationRegistry;
import goblinbob.mobends.core.addon.IAddon;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.standard.client.model.armor.ArmorModelFactory;
import goblinbob.mobends.standard.client.renderer.entity.ArrowTrailManager;
import goblinbob.mobends.standard.client.renderer.entity.SwordTrail;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomBipedArmor;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomCape;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomElytra;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerCustomHeldItem;
import goblinbob.mobends.standard.client.renderer.entity.layers.LayerPlayerAccessories;
import goblinbob.mobends.standard.client.renderer.entity.mutated.*;
import goblinbob.mobends.standard.data.*;
import goblinbob.mobends.standard.kumo.CapeDriver;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.kumo.SwordTrailDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderIdleLegsTemplate;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsDriver;
import goblinbob.mobends.standard.kumo.spider.SpiderMovingLegsTemplate;
import goblinbob.mobends.standard.main.ModConfig;
import goblinbob.mobends.standard.mutators.*;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerBipedArmor;
import net.minecraft.client.renderer.entity.layers.LayerCape;
import net.minecraft.client.renderer.entity.layers.LayerCustomHead;
import net.minecraft.client.renderer.entity.layers.LayerElytra;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityZombieVillager;
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

		registry.registerNewEntity(EntityZombieVillager.class, ZombieVillagerData::new, ZombieVillagerMutator::new, new ZombieRenderer<>());

		registry.registerNewEntity(EntityPigZombie.class, PigZombieData::new, PigZombieMutator::new, new ZombieRenderer<>());

		registry.registerNewEntity(EntitySpider.class, SpiderData::new, SpiderMutator::new, new SpiderRenderer<>());

		registry.registerNewEntity(EntitySquid.class, SquidData::new, SquidMutator::new, new SquidRenderer<>());

		registry.registerNewEntity(EntityWolf.class, WolfData::new, WolfMutator::new, new WolfRenderer<>());

		// Mobs described as data (cows, pigs, villagers, ...) come from the type files in bends/types/.

		registry.registerDriver("cape", CapeDriver::create, CapeDriver.Template.class);
		registry.registerDriver("sword_trail", SwordTrailDriver::create, SwordTrailDriver.Template.class);
		registry.registerDriver("spider_idle_legs", SpiderIdleLegsDriver::create, SpiderIdleLegsTemplate.class);
		registry.registerDriver("spider_moving_legs", SpiderMovingLegsDriver::create, SpiderMovingLegsTemplate.class);
		MoBendsOperations.register();

		// What model definitions can switch on (their "components" and "layers").
		registry.registerComponent("sword_trail", data -> new SwordTrail(() -> SupporterContent.getTrailColorFor(data.getEntity())));
		registry.registerComponent("cape_wave", data -> new CapeWave(data.getEntity()));
		registry.registerLayer("armor", LayerBipedArmor.class, (renderer, options, bones) -> new LayerCustomBipedArmor(renderer));
		registry.registerLayer("held_item", LayerHeldItem.class, (renderer, options, bones) -> new LayerCustomHeldItem(renderer));
		registry.registerLayer("custom_head", LayerCustomHead.class, (renderer, options, bones) -> new LayerCustomHead(bones.apply(
				options.has("bone") ? options.get("bone").getAsString() : "head")));
		registry.registerLayer("cape", LayerCape.class, (renderer, options, bones) -> new LayerCustomCape((RenderPlayer) renderer));
		registry.registerLayer("elytra", LayerElytra.class, (renderer, options, bones) -> new LayerCustomElytra((RenderPlayer) renderer));
		registry.registerLayer("accessories", null, (renderer, options, bones) -> new LayerPlayerAccessories(renderer));
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
