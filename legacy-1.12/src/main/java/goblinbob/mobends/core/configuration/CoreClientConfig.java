package goblinbob.mobends.core.configuration;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.kumo.option.KumoOptions;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class CoreClientConfig extends CoreConfig
{

    // Animated
    private static final String CATEGORY_ANIMATED = "Animated";

    // Type ranks: the order the user gave the entity types, by type id (absent means 0)
    private static final String CATEGORY_TYPE_RANKS = "TypeRanks";

    // Extension ranks: the order the user gave the extensions, by extension id (absent means 0)
    private static final String CATEGORY_EXTENSION_RANKS = "ExtensionRanks";

    // Options: the values the user gave the options of model definitions, types and extensions, by
    // key (<owner>#<name>); absent means the option's default
    private static final String CATEGORY_OPTIONS = "Options";

    public CoreClientConfig(File file)
    {
        super(file);
        KumoOptions.INSTANCE.load(getOptionValues());
        KumoOptions.INSTANCE.setListener(this::setOptionValue);
    }

    /**
     * Moves an on/off setting of Forge's config (its {@code general} category) to an option, once:
     * switched off, the option is off too, unless the user already set the option.
     */
    public void migrateToOption(String setting, String optionKey)
    {
        ConfigCategory general = configuration.hasCategory(Configuration.CATEGORY_GENERAL) ? configuration.getCategory(Configuration.CATEGORY_GENERAL) : null;
        if (general == null || !general.containsKey(setting))
        {
            return;
        }
        if (!general.get(setting).getBoolean(true) && !configuration.getCategory(CATEGORY_OPTIONS).containsKey(optionKey))
        {
            setOptionValue(optionKey, 0.0);
            KumoOptions.INSTANCE.load(getOptionValues());
        }
        general.remove(setting);
        configuration.save();
    }

    /** The values the user gave options, by key. */
    public Map<String, Double> getOptionValues()
    {
        Map<String, Double> values = new HashMap<>();
        for (Map.Entry<String, Property> entry : configuration.getCategory(CATEGORY_OPTIONS).entrySet())
        {
            values.put(entry.getKey(), entry.getValue().getDouble());
        }
        return values;
    }

    /** Stores the value the user gave an option; null, its default, removes it. */
    public void setOptionValue(String key, @Nullable Double value)
    {
        if (value == null)
        {
            configuration.getCategory(CATEGORY_OPTIONS).remove(key);
        }
        else
        {
            configuration.get(CATEGORY_OPTIONS, key, 0.0).set(value);
        }
    }

    public void save()
    {
        for (EntityBender<?> entityBender : EntityBenderRegistry.instance.getRegistered())
        {
            configuration.get(CATEGORY_ANIMATED, entityBender.getKey(), true).setValue(entityBender.isAnimated());
        }

        configuration.save();
    }

    public boolean isEntityAnimated(EntityBender<?> bender)
    {
        ConfigCategory animated = configuration.getCategory(CATEGORY_ANIMATED);
        // Settings saved before 2.0 are under the old key; carried over once.
        if (!animated.containsKey(bender.getKey()) && animated.containsKey(bender.getLegacyKey()))
        {
            boolean value = animated.get(bender.getLegacyKey()).getBoolean(true);
            animated.remove(bender.getLegacyKey());
            configuration.get(CATEGORY_ANIMATED, bender.getKey(), true).set(value);
            return value;
        }
        return configuration.get(CATEGORY_ANIMATED, bender.getKey(), true).getBoolean();
    }

    public int getTypeRank(String typeId)
    {
        return getRank(CATEGORY_TYPE_RANKS, typeId);
    }

    public void setTypeRank(String typeId, int rank)
    {
        configuration.get(CATEGORY_TYPE_RANKS, typeId, 0).set(rank);
    }

    public void clearTypeRank(String typeId)
    {
        configuration.getCategory(CATEGORY_TYPE_RANKS).remove(typeId);
    }

    public int getExtensionRank(String extensionId)
    {
        return getRank(CATEGORY_EXTENSION_RANKS, extensionId);
    }

    public void setExtensionRank(String extensionId, int rank)
    {
        configuration.get(CATEGORY_EXTENSION_RANKS, extensionId, 0).set(rank);
    }

    public void clearExtensionRank(String extensionId)
    {
        configuration.getCategory(CATEGORY_EXTENSION_RANKS).remove(extensionId);
    }

    private int getRank(String category, String id)
    {
        ConfigCategory ranks = configuration.getCategory(category);
        return ranks.containsKey(id) ? ranks.get(id).getInt(0) : 0;
    }

}
