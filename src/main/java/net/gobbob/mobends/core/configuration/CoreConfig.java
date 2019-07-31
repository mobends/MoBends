package net.gobbob.mobends.core.configuration;

import net.gobbob.mobends.core.animatedentity.AlterEntry;
import net.gobbob.mobends.core.animatedentity.AnimatedEntity;
import net.gobbob.mobends.core.animatedentity.AnimatedEntityRegistry;
import net.gobbob.mobends.core.pack.PackManager;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nullable;
import java.io.File;

public class CoreConfig
{

    // General
    private static final String CATEGORY_GENERAL = "General";
    private static final String PROP_CURRENT_PACK = "CurrentPack";

    // Animated
    private static final String CATEGORY_ANIMATED = "Animated";

    private Configuration configuration;

    public CoreConfig(File file)
    {
        configuration = new Configuration(file);
    }

    public void save()
    {
        for (AnimatedEntity<?> animatedEntity : AnimatedEntityRegistry.getRegistered())
        {
            for (AlterEntry<?> alterEntry : animatedEntity.getAlterEntries())
            {
                configuration.get(CATEGORY_ANIMATED, alterEntry.getKey(), true).setValue(alterEntry.isAnimated());
            }
        }

        if (PackManager.instance.getAppliedPack() != null)
            setCurrentPack(PackManager.instance.getAppliedPack().getName());
        else
            setCurrentPack(null);

        configuration.save();
    }

    @Nullable
    public String getCurrentPack()
    {
        String value = configuration.get(CATEGORY_GENERAL, PROP_CURRENT_PACK, "none").getString();
        return value.equals("none") ? null : value;
    }

    public void setCurrentPack(@Nullable String packName)
    {
        String value = packName == null ? "none" : packName;
        configuration.get(CATEGORY_GENERAL, PROP_CURRENT_PACK, "none").setValue(value);
    }

    public boolean isEntityAnimated(String alterEntryKey)
    {
        return configuration.get(CATEGORY_ANIMATED, alterEntryKey, true).getBoolean();
    }

}