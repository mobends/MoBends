package goblinbob.mobends.standard.main;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Config(modid = ModStatics.MODID)
public class ModConfig
{

    @Config.LangKey(ModStatics.MODID + ".config.show_arrow_trails")
    public static boolean showArrowTrails = true;
    @Config.LangKey(ModStatics.MODID + ".config.show_sword_trails")
    public static boolean showSwordTrail = true;
    @Config.LangKey(ModStatics.MODID + ".config.weapon_items")
    public static String[] weaponItems = new String[] {};

    private static Set<ResourceLocation> weaponItemsSet = null;

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
                weaponItemsSet = null;
            }
        }

    }

    public static boolean isItemWeapon(Item item)
    {
        if (weaponItemsSet == null)
        {
            weaponItemsSet = new HashSet<>();
            for (String itemLocationPath : weaponItems)
            {
                weaponItemsSet.add(new ResourceLocation(itemLocationPath));
            }
        }
        return weaponItemsSet.contains(item.getRegistryName());
    }

}
