package goblinbob.mobends.core.configuration;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import net.minecraftforge.common.config.ConfigCategory;

import java.io.File;

public class CoreClientConfig extends CoreConfig
{

    // Animated
    private static final String CATEGORY_ANIMATED = "Animated";

    // Type ranks: the order the user gave the entity types, by type id (absent means 0)
    private static final String CATEGORY_TYPE_RANKS = "TypeRanks";

    // Extension ranks: the order the user gave the extensions, by extension id (absent means 0)
    private static final String CATEGORY_EXTENSION_RANKS = "ExtensionRanks";

    public CoreClientConfig(File file)
    {
        super(file);
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
