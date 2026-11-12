package goblinbob.mobends.standard.main;

import goblinbob.mobends.core.CoreClient;
import goblinbob.mobends.core.ModStatics;
import goblinbob.mobends.core.compat.EntityIds;
import goblinbob.mobends.core.compat.Events;
import goblinbob.mobends.core.configuration.CoreConfig;
import goblinbob.mobends.core.util.ErrorReporter;
import goblinbob.mobends.standard.AttackActionType;
import goblinbob.mobends.core.util.WildcardPattern;
import goblinbob.mobends.standard.UseActionType;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.*;
import java.util.function.Function;

/**
 * The options of Forge's config screen for the mod, in the {@link Configuration#CATEGORY_GENERAL general} category of
 * the mod's shared configuration file (see {@link CoreConfig#sharedConfiguration}), under the names 1.12.2's
 * {@code @Config} gave them.
 */
public class ModConfig
{
    public static boolean showArrowTrails = true;
    public static boolean showSwordTrail = true;
    public static String[] itemUseClassifications = new String[] {};
    public static String[] itemAttackClassifications = new String[] {};
    public static String[] keepArmorAsVanilla = new String[] {};
    public static String[] keepEntityAsVanilla = new String[] {};

    private static Configuration configuration;

    private static Map<Item, Boolean> keepArmorAsVanillaCache;
    /** By entity type id: the answer depends on the entity's type only (a variant is a type of its own). */
    private static Map<ResourceLocation, Boolean> keepEntityAsVanillaCache;
    private static Map<Item, UseActionType> itemUseClassificationCache;
    private static Map<Item, AttackActionType> itemAttackClassificationCache;
    private static LinkedList<ItemClassificationEntry<UseActionType>> itemUseClassificationEntries = new LinkedList<>();
    private static LinkedList<ItemClassificationEntry<AttackActionType>> itemAttackClassificationEntries = new LinkedList<>();

    private static List<Map<?, ?>> caches = Arrays.asList(
        keepArmorAsVanillaCache = new HashMap<>(),
        keepEntityAsVanillaCache = new HashMap<>(),
        itemUseClassificationCache = new HashMap<>(),
        itemAttackClassificationCache = new HashMap<>()
    );

    /** Reads the options from the mod's configuration file (config/mobends.cfg), and saves any missing. */
    public static void load(File file)
    {
        configuration = CoreConfig.sharedConfiguration(file);
        configuration.load();
        sync();
        Events.register(new EventHandler());
    }

    /** Reads the options from the configuration, writing any it lacks, and forgets what was worked out from them. */
    private static void sync()
    {
        final String category = Configuration.CATEGORY_GENERAL;
        final String lang = ModStatics.MODID + ".config.";
        showArrowTrails = configuration.getBoolean("showArrowTrails", category, true, "", lang + "show_arrow_trails");
        showSwordTrail = configuration.getBoolean("showSwordTrail", category, true, "", lang + "show_sword_trails");
        itemUseClassifications = configuration.getStringList("itemUseClassifications", category, new String[] {}, "", null, lang + "item_use_classifications");
        itemAttackClassifications = configuration.getStringList("itemAttackClassifications", category, new String[] {}, "", null, lang + "item_attack_classifications");
        keepArmorAsVanilla = configuration.getStringList("keepArmorAsVanilla", category, new String[] {}, "", null, lang + "keep_armor_as_vanilla");
        keepEntityAsVanilla = configuration.getStringList("keepEntityAsVanilla", category, new String[] {}, "", null, lang + "keep_entity_as_vanilla");
        if (configuration.hasChanged())
        {
            configuration.save();
        }

        // Clearing the caches
        for (Map<?, ?> cache : caches)
        {
            cache.clear();
        }

        itemUseClassificationEntries.clear();
        getOrMakeEntries(itemUseClassificationEntries, itemUseClassifications, UseActionType::valueOf);

        itemAttackClassificationEntries.clear();
        getOrMakeEntries(itemAttackClassificationEntries, itemAttackClassifications, AttackActionType::valueOf);
    }

    public static class EventHandler
    {
        /**
         * Reads the new values when the config has been changed from Forge's screen (which wrote them to the
         * configuration; saving it is up to the mod).
         *
         * @param event The event
         */
        @SubscribeEvent
        public void onConfigChanged(final ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if (event.modID.equals(ModStatics.MODID))
            {
                configuration.save();
                sync();
                CoreClient.refresh();
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
        for (String pattern : patterns)
        {
            if (doesLocationMatchPattern(resourceLocation, pattern))
                return true;
        }

        return false;
    }

    public static UseActionType getItemUseAction(Item item)
    {
        // If cached before, returning the cached classification.
        return itemUseClassificationCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = Item.itemRegistry.getNameForObject(item);

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
            ResourceLocation location = Item.itemRegistry.getNameForObject(item);

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
        return keepArmorAsVanillaCache.computeIfAbsent(item, (i) -> {
            ResourceLocation location = Item.itemRegistry.getNameForObject(i);
            return location != null && checkForPatterns(location, keepArmorAsVanilla);
        });
    }

    public static boolean shouldKeepEntityAsVanilla(Entity entity)
    {
        // If cached before, returning the cached result.
        final ResourceLocation location = EntityIds.of(entity);
        // An unregistered entity has no id.
        return location != null && keepEntityAsVanillaCache.computeIfAbsent(location, l -> checkForPatterns(l, keepEntityAsVanilla));
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
