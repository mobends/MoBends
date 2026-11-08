package goblinbob.mobends.standard.client;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.core.addon.AddonHelper;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.standard.DefaultAddon;
import goblinbob.mobends.standard.client.event.RenderingEventHandler;
import goblinbob.mobends.standard.client.renderer.entity.RenderBendsSpectralArrow;
import goblinbob.mobends.standard.client.renderer.entity.RenderBendsTippedArrow;
import goblinbob.mobends.standard.kumo.MoBendsOperations;
import goblinbob.mobends.standard.main.CommonProxy;
import goblinbob.mobends.standard.main.ModConfig;
import net.minecraft.entity.projectile.EntitySpectralArrow;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

public class ClientProxy extends CommonProxy
{
	
	@Override
	public void preInit()
	{
		// "Perform Spin Attack" was a setting of ModConfig before 2.0; it is the player's option now.
		CoreClient.getInstance().getConfiguration().migrateToOption("performSpinAttack", MoBendsOperations.SPIN_ATTACK_OPTION);
		RenderingRegistry.registerEntityRenderingHandler(EntitySpectralArrow.class, RenderBendsSpectralArrow::new);
		RenderingRegistry.registerEntityRenderingHandler(EntityTippedArrow.class, RenderBendsTippedArrow::new);
	}

	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register(new RenderingEventHandler());

		// Registering the standard set of animations.
		AddonHelper.registerAddon(ModStatics.MODID, new DefaultAddon());
		EntityBenderRegistry.instance.setKeepVanilla(ModConfig::shouldKeepEntityAsVanilla);
	}
	
	@Override
	public void postInit() {}
	
	@Override
	public void createCore()
	{
		Core.createAsClient();
	}
	
}
