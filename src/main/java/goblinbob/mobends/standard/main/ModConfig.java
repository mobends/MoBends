package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.core.util.WildcardPattern;
import goblinbob.mobends.standard.UseActionType;
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
import java.util.function.Function;

@Config(modid = ModStatics.MODID)
public class ModConfig
{
    @Config.LangKey(ModStatics.MODID + ".config.show_arrow_trails")
    public static boolean showArrowTrails = true;
    @Config.LangKey(ModStatics.MODID + ".config.show_sword_trails")
    public static boolean showSwordTrail = true;
    @Config.LangKey(ModStatics.MODID + ".config.perform_spin_attack")
    public static boolean performSpinAttack = true;
    @Config.LangKey(ModStatics.MODID + ".config.item_use_classifications")
    public static String[] itemUseClassifications = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.item_attack_classifications")
    public static String[] itemAttackClassifications = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.keep_armor_as_vanilla")
    public static String[] keepArmorAsVanilla = new String[] {};
    @Config.LangKey(ModStatics.MODID + ".config.keep_entity_as_vanilla")
    public static String[] keepEntityAsVanilla = new String[] {};

    @Config.Ignore
    private static Map<Item, Boolean> keepArmorAsVanillaCache;
    @Config.Ignore
    private static Map<Entity, Boolean> keepEntityAsVanillaCache;
    @Config.Ignore
    private static Map<Item, UseActionType> itemUseClassificationCache;
    @Config.Ignore
    private static Map<Item, AttackActionType> itemAttackClassificationCache;
    @Config.Ignore
    private static LinkedList<ItemClassificationEntry<UseActionType>> itemUseClassificationEntries = new LinkedList<>();
    @Config.Ignore
    private static LinkedList<ItemClassificationEntry<AttackActionType>> itemAttackClassificationEntries = new LinkedList<>();

    @Config.Ignore
    private static List<Map<?, ?>> caches = Arrays.asList(
        keepArmorAsVanillaCache = new HashMap<>(),
        keepEntityAsVanillaCache = new HashMap<>(),
        itemUseClassificationCache = new HashMap<>(),
        itemAttackClassificationCache = new HashMap<>()
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

                itemUseClassificationEntries.clear();
                getOrMakeEntries(itemUseClassificationEntries, itemUseClassifications, UseActionType::valueOf);

                itemAttackClassificationEntries.clear();
                getOrMakeEntries(itemAttackClassificationEntries, itemAttackClassifications, AttackActionType::valueOf);

                MoBends.refreshSystems();
            }
        }
    }

    private static <T> LinkedList<ItemClassificationEntry<T>> getOrMakeEntries(LinkedList<ItemClassificationEntry<T>> entries, String[] rawEntries, Function<String, T> parseFunction)
    {
        if (rawEntries.length == 0)
            return entries;

        if (entries.size() == 0)
        {
            for (String rawEntry : rawEntries)
            {
                try
                {
                    entries.addFirst(ItemClassificationEntry.parse(rawEntry, parseFunction));
                }
                catch(MalformedConfigException e)
                {
                    ErrorReporter.showErrorToPlayer(String.format("Invalid configuration! %s", e.getMessage()));
                }
            }
        }

        return entries;
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

    public static UseActionType getItemUseAction(Item item)
    {
        // If cached before, returning the cached classification.
        return itemUseClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = item.getRegistryName();

            if (location != null)
            {
                List<ItemClassificationEntry<UseActionType>> entries = getOrMakeEntries(itemUseClassificationEntries, itemUseClassifications, UseActionType::valueOf);
                for (ItemClassificationEntry<UseActionType> e : entries)
                {
                    if (doesLocationMatchPattern(location, e.pattern))
                    {
                        return e.classification;
                    }
                }
            }

            // Unclassified
            return null;
        });
    }

    public static AttackActionType getItemAttackAction(Item item)
    {
        // If cached before, returning the cached classification.
        return itemAttackClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = item.getRegistryName();

            if (location != null)
            {
                List<ItemClassificationEntry<AttackActionType>> entries = getOrMakeEntries(itemAttackClassificationEntries, itemAttackClassifications, AttackActionType::valueOf);
                for (ItemClassificationEntry<AttackActionType> e : entries)
                {
                    if (doesLocationMatchPattern(location, e.pattern))
                    {
                        return e.classification;
                    }
                }
            }

            // Unclassified
            return null;
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

    private static class ItemClassificationEntry<T>
    {
        public final String pattern;
        public final T classification;

        public ItemClassificationEntry(String pattern, T classification)
        {
            this.pattern = pattern;
            this.classification = classification;
        }

        public static <T> ItemClassificationEntry<T> parse(String encoded, Function<String, T> parsingFunction)
        {
            int indexOfEquals = encoded.indexOf("=");

            if (indexOfEquals == -1)
            {
                throw new IllegalArgumentException(String.format("No equals sign found in the item classification entry: %s", encoded));
            }

            String pattern = encoded.substring(0, indexOfEquals);
            String encodedActionType = encoded.substring(indexOfEquals + 1).toUpperCase();

            try
            {
                T actionType = parsingFunction.apply(encodedActionType);

                return new ItemClassificationEntry<>(pattern, actionType);
            }
            catch(IllegalArgumentException e)
            {
                throw new MalformedConfigException(String.format("Unknown action type: %s", encodedActionType), e);
            }
        }
    }
}
