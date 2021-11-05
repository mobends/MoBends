package goblinbob.mobends.standard.main;

import goblinbob.mobends.standard.ItemClassification;
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

import java.util.*;

@Config(modid = ModStatics.MODID)
public class ModConfig
{
    @Config.LangKey(ModStatics.MODID + ".config.show_arrow_trails")
    public static boolean showArrowTrails = true;
    @Config.LangKey(ModStatics.MODID + ".config.show_sword_trails")
    public static boolean showSwordTrail = true;
    @Config.LangKey(ModStatics.MODID + ".config.perform_spin_attack")
    public static boolean performSpinAttack = true;
    @Config.LangKey(ModStatics.MODID + ".config.item_classifications")
    public static String[] itemClassificationsRaw = new String[] {};
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
    private static LinkedList<ItemClassificationEntry> itemClassificationEntries = new LinkedList<>();

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

                itemClassificationEntries.clear();
                for (String rawEntry : itemClassificationsRaw)
                {
                    itemClassificationEntries.addFirst(ItemClassificationEntry.parse(rawEntry));
                }

                MoBends.refreshSystems();
            }
        }
    }

    private static boolean doesLocationMatchPattern(ResourceLocation resourceLocation, String pattern)
    {
        final ResourceLocation patternLocation = new ResourceLocation(pattern);

        if (resourceLocation.equals(patternLocation))
            return true;

        WildcardPattern domainPattern = new WildcardPattern(patternLocation.getResourceDomain());
        WildcardPattern pathPattern = new WildcardPattern(patternLocation.getResourcePath());

        return domainPattern.matches(resourceLocation.getResourceDomain()) &&
               pathPattern.matches(resourceLocation.getResourcePath());
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

            if (location != null)
            {
                for (ItemClassificationEntry e : itemClassificationEntries)
                {
                    if (doesLocationMatchPattern(location, e.pattern))
                    {
                        return e.classification;
                    }
                }
            }

            // Unclassified
            return ItemClassification.UNKNOWN;
        });
    }
    
    public static boolean shouldKeepArmorAsVanilla(Item item)
    {
        // If cached before, returning the cached result.
        return keepArmorAsVanillaCache.computeIfAbsent(item, (i) -> checkForPatterns(i.getRegistryName(), keepArmorAsVanilla));
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

    private static class ItemClassificationEntry
    {
        public final String pattern;
        public final ItemClassification classification;

        public ItemClassificationEntry(String pattern, ItemClassification classification)
        {
            this.pattern = pattern;
            this.classification = classification;
        }

        public static ItemClassificationEntry parse(String encoded)
        {
            int indexOfEquals = encoded.indexOf("=");

            if (indexOfEquals == -1)
            {
                throw new IllegalArgumentException(String.format("No equals sign found in the item classification entry: %s", encoded));
            }

            String pattern = encoded.substring(0, indexOfEquals);
            ItemClassification classification = ItemClassification.valueOf(encoded.substring(indexOfEquals + 1).toUpperCase());

            return new ItemClassificationEntry(pattern, classification);
        }
    }
}
