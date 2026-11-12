package goblinbob.mobends.core.configuration;

import net.minecraftforge.common.config.Configuration;

import javax.annotation.Nullable;
import java.io.File;

public abstract class CoreConfig
{
    /** See {@link #sharedConfiguration(File)}. */
    @Nullable
    private static Configuration shared;

    protected Configuration configuration;

    /**
     * @param file the mod's configuration file, {@code config/mobends.cfg}, shared (see {@link #sharedConfiguration}).
     */
    CoreConfig(File file)
    {
        configuration = sharedConfiguration(file);
    }

    /**
     * The one Configuration of the mod's file, {@code config/mobends.cfg}: the options of Forge's config screen
     * ({@code ModConfig}, in {@link Configuration#CATEGORY_GENERAL}) and the settings of the Mo' Bends menu
     * (categories of their own) live in it, so a save from either side writes both (two Configurations would each
     * overwrite the other's changes).
     */
    public static Configuration sharedConfiguration(File file)
    {
        if (shared == null)
        {
            shared = new Configuration(file);
        }
        return shared;
    }

    /** The shared configuration (see {@link #sharedConfiguration(File)}), or null before the mod's pre-initialization. */
    @Nullable
    public static Configuration sharedConfiguration()
    {
        return shared;
    }

    public abstract void save();
}
