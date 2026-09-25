package goblinbob.mobends.core.configuration;

import goblinbob.mobends.core.bender.EntityBender;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import net.minecraftforge.common.config.ConfigCategory;

import java.io.File;
import java.util.Collection;

public class CoreClientConfig extends CoreConfig
{
    private static final String[] emptyStringList = new String[0];

    // General
    private static final String CATEGORY_GENERAL = "General";
    private static final String PROP_APPLIED_PACKS = "AppliedPacks";

    // Animated
    private static final String CATEGORY_ANIMATED = "Animated";

    // Type ranks: the order the user gave the entity types, by type id (absent means 0)
    private static final String CATEGORY_TYPE_RANKS = "TypeRanks";

    public String[] appliedPackKeys;

    public CoreClientConfig(File file)
    {
        super(file);
        appliedPackKeys = new String[] {};
        load();
    }

    public void save()
    {
        for (EntityBender<?> entityBender : EntityBenderRegistry.instance.getRegistered())
        {
            configuration.get(CATEGORY_ANIMATED, entityBender.getKey(), true).setValue(entityBender.isAnimated());
        }

        configuration.save();
    }

    public void load()
    {
        appliedPackKeys = configuration.get(CATEGORY_GENERAL, PROP_APPLIED_PACKS, emptyStringList).getStringList();
    }

    public String[] getAppliedPacks()
    {
        return appliedPackKeys;
    }

    public void setAppliedPacks(String[] packNames)
    {
        appliedPackKeys = packNames;
        configuration.get(CATEGORY_GENERAL, PROP_APPLIED_PACKS, emptyStringList).set(packNames);
    }

    public void setAppliedPacks(Collection<String> packNames)
    {
        setAppliedPacks(packNames.toArray(new String[0]));
    }

    public boolean isEntityAnimated(String alterEntryKey)
    {
        return configuration.get(CATEGORY_ANIMATED, alterEntryKey, true).getBoolean();
    }

    public int getTypeRank(String typeId)
    {
        ConfigCategory ranks = configuration.getCategory(CATEGORY_TYPE_RANKS);
        return ranks.containsKey(typeId) ? ranks.get(typeId).getInt(0) : 0;
    }

    public void setTypeRank(String typeId, int rank)
    {
        configuration.get(CATEGORY_TYPE_RANKS, typeId, 0).set(rank);
    }

    public void clearTypeRank(String typeId)
    {
        configuration.getCategory(CATEGORY_TYPE_RANKS).remove(typeId);
    }
}