package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.util.WildcardPattern;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Config(modid = ModStatics.MODID)
public class ModConfig
{
    @Config.LangKey(ModStatics.MODID + ".config.show_arrow_trails")
    public static boolean showArrowTrails = true;
    @Config.LangKey(ModStatics.MODID + ".config.show_sword_trails")
    public static boolean showSwordTrail = true;
    @Config.LangKey(ModStatics.MODID + ".config.perform_spin_attack")
    public static boolean performSpinAttack = true;
    @Config.LangKey(ModStatics.MODID + ".config.swim_module")
    public static boolean swimModule = true;

    @Config.LangKey(ModStatics.MODID + ".config.weapon_items")
    public static String[] weaponItems = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.tool_items")
    public static String[] toolItems = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.keep_armor_as_vanilla")
    public static String[] keepArmorAsVanilla = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.keep_entity_as_vanilla")
    public static String[] keepEntityAsVanilla = new String[] {};

    @Config.Ignore
    private static Map<Item, Boolean> keepArmorAsVanillaCache;
    @Config.Ignore
    private static Map<Entity, Boolean> keepEntityAsVanillaCache;
    @Config.Ignore
    private static Map<Item, ItemClassification> itemClassificationCache;

    @Config.Ignore
    private static List<Map<?, ?>> caches = Arrays.asList(
        keepArmorAsVanillaCache = new HashMap<>(),
        keepEntityAsVanillaCache = new HashMap<>(),
        itemClassificationCache = new HashMap<>()
    );

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

                // Clearing the caches
                for (Map<?, ?> cache : caches)
                {
                    cache.clear();
                }

                MoBends.refreshSystems();
            }
        }
    }

    public enum ItemClassification
    {
        UNKNOWN,
        WEAPON,
        TOOL,
    }

    private static boolean checkForPatterns(ResourceLocation resourceLocation, String[] patterns)
    {
        final String resourceDomain = resourceLocation.getResourceDomain();
        final String resourcePath = resourceLocation.getResourcePath();

        for (String pattern : patterns)
        {
            final ResourceLocation patternLocation = new ResourceLocation(pattern);

            if (resourceLocation.equals(patternLocation))
                return true;

            WildcardPattern domainPattern = new WildcardPattern(patternLocation.getResourceDomain());
            WildcardPattern pathPattern = new WildcardPattern(patternLocation.getResourcePath());

            if (!domainPattern.matches(resourceDomain))
                continue;

            if (pathPattern.matches(resourcePath))
                return true;
        }

        return false;
    }

    public static ItemClassification getItemClassification(Item item)
    {
        // If cached before, returning the cached classification.
        return itemClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = item.getRegistryName();

            if (checkForPatterns(location, weaponItems))
                return ItemClassification.WEAPON;

            if (checkForPatterns(location, toolItems))
                return ItemClassification.TOOL;

            // Unclassified
            return ItemClassification.UNKNOWN;
        });
    }
    
    public static boolean shouldKeepArmorAsVanilla(Item item)
    {
        // If cached before, returning the cached result.
        return keepArmorAsVanillaCache.computeIfAbsent(item, (i) -> {
            return checkForPatterns(i.getRegistryName(), keepArmorAsVanilla);
        });
    }

    public static boolean shouldKeepEntityAsVanilla(Entity entity)
    {
        // If cached before, returning the cached result.
        return keepEntityAsVanillaCache.computeIfAbsent(entity, (e) -> {
            ResourceLocation location = EntityList.getKey(entity);

            // The player, for example, doesn't have a key.
            return location != null && checkForPatterns(location, keepEntityAsVanilla);
        });
    }
}
