package goblinbob.mobends.core.env;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.module.IModule;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

/**
 * Responsible for supplying info about the mod's environment. Helpful when you want to replace
 * configuration during development.
 *
 * By default, the module supplies the DEFAULT_CONFIG. However, if an "env.json" file exists in the "mobends"
 * config folder, then the variables specified in the "env.json" replace the ones in the default set.
 *
 * @author GoblinBob
 */
public class EnvironmentModule
{
    private static EnvironmentModule INSTANCE;
    private static final EnvironmentConfig DEFAULT_CONFIG = new EnvironmentConfig();
    static
    {
        DEFAULT_CONFIG.apiUrl = "https://mobends.com";
    }

    private final File localConfigFile;
    private EnvironmentConfig config = DEFAULT_CONFIG;

    public EnvironmentModule(File configDirectory)
    {
        File modConfigDirectory = new File(configDirectory, "mobends");
        this.localConfigFile = new File(modConfigDirectory, "env.json");

        this.resolveConfig();
    }

    private void resolveConfig()
    {
        JsonObject localConfig = getLocalEnvironment(this.localConfigFile);

        if (localConfig != null)
        {
            Gson gson = new Gson();
            JsonObject propertyMap = gson.toJsonTree(DEFAULT_CONFIG).getAsJsonObject();

            // Replace defaults with the local values
            for (Map.Entry<String, JsonElement> entry : localConfig.entrySet())
            {
                propertyMap.add(entry.getKey(), entry.getValue());
            }

            this.config = gson.fromJson(propertyMap, EnvironmentConfig.class);
        }
    }

    private static JsonObject getLocalEnvironment(File envFile)
    {
        try
        {
            if (envFile.isFile())
            {
                return new Gson().fromJson(new BufferedReader(new FileReader(envFile)), JsonObject.class);
            }
        }
        catch(IOException e)
        {
            Core.LOG.warning("Couldn't load the local environment configuration.");
        }

        return null;
    }

    public static EnvironmentConfig getConfig()
    {
        return INSTANCE.config;
    }

    public static class Factory implements IModule
    {
        @Override
        public void preInit(FMLPreInitializationEvent event)
        {
            EnvironmentModule.INSTANCE = new EnvironmentModule(event.getModConfigurationDirectory());
        }

        @Override
        public void onRefresh()
        {
            EnvironmentModule.INSTANCE.resolveConfig();
        }
    }
}
