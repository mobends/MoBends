package net.gobbob.mobends.main;

import java.util.Map;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = ModStatics.MODID)
@Config.LangKey(ModStatics.MODID + ".config.title")
public class ModConfig
{
	@Config.Comment("If true, arrow trails will be shown")
	@Config.Name("Show Arrow Trails")
	public static boolean showArrowTrails = true;
	@Config.Name("Show Sword Trails")
	public static boolean showSwordTrail = true;
	@Config.Name("Perform Spin Attack")
	public static boolean performSpinAttack = true;
	
	@Mod.EventBusSubscriber(modid = ModStatics.MODID)
	private static class EventHandler
	{
		/**
		 * Inject the new values and save to the config file when the config has been changed from the GUI.
		 *
		 * @param event The event
		 */
		@SubscribeEvent
		public static void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
		{
			if (event.getModID().equals(ModStatics.MODID))
			{
				ConfigManager.sync(ModStatics.MODID, Config.Type.INSTANCE);
			}
		}
	}
}
