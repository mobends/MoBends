package goblinbob.mobends.core.configuration;

import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.ModStatics;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nullable;
import java.io.File;
import java.lang.reflect.Method;
import java.util.logging.Level;

public abstract class CoreConfig
{
    protected Configuration configuration;

    /**
     * @param file the mod's configuration file, {@code config/mobends.cfg}. Forge's {@code @Config}
     *             ({@code ModConfig}) keeps its options there too, so its {@link Configuration} is
     *             shared: the settings of the Mo' Bends menu are categories of their own in it, and a
     *             save from either side writes both (two Configurations would each overwrite the
     *             other's changes).
     */
    CoreConfig(File file)
    {
        Configuration shared = forgeConfiguration();
        if (shared == null)
        {
            Core.LOG.warning("Could not share Forge's configuration of " + file + "; changes made in Forge's mod options may be overwritten.");
            shared = new Configuration(file);
        }
        configuration = shared;
    }

    /** The Configuration Forge's {@code @Config} keeps for this mod's file, or null if it can't be reached. */
    @Nullable
    private static Configuration forgeConfiguration()
    {
        try
        {
            // Package-private in Forge; there is no public way to get it.
            Method getConfiguration = ConfigManager.class.getDeclaredMethod("getConfiguration", String.class, String.class);
            getConfiguration.setAccessible(true);
            Configuration configuration = (Configuration) getConfiguration.invoke(null, ModStatics.MODID, null);
            if (configuration == null)
            {
                // Forge creates it when it first syncs the @Config classes (when the mod is constructed).
                ConfigManager.sync(ModStatics.MODID, Config.Type.INSTANCE);
                configuration = (Configuration) getConfiguration.invoke(null, ModStatics.MODID, null);
            }
            return configuration;
        }
        catch (ReflectiveOperationException | RuntimeException e)
        {
            Core.LOG.log(Level.WARNING, "Could not reach Forge's configuration", e);
            return null;
        }
    }

    public abstract void save();
}
