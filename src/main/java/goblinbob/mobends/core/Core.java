package goblinbob.mobends.core;

import goblinbob.mobends.core.configuration.CoreConfig;
import goblinbob.mobends.core.kumo.MinecraftKumoOperations;
import goblinbob.mobends.core.module.IModule;
import goblinbob.mobends.core.network.msg.MessageConfigRequest;
import goblinbob.mobends.core.network.msg.MessageConfigResponse;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public abstract class Core<T extends CoreConfig>
{
    private static Core INSTANCE;
    public static final Logger LOG = createLogger();

    private SimpleNetworkWrapper networkWrapper;
    private static final int MESSAGE_CONFIG_REQUEST = 0;
    private static final int MESSAGE_CONFIG_RESPONSE = 1;

    private Collection<IModule> modules = new ArrayList<>();

    public abstract T getConfiguration();

    public void preInit(FMLPreInitializationEvent event)
    {
        MinecraftKumoOperations.register();

        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModStatics.MODID);
        networkWrapper.registerMessage(MessageConfigRequest.Handler.class, MessageConfigRequest.class, MESSAGE_CONFIG_REQUEST, Side.SERVER);
        networkWrapper.registerMessage(MessageConfigResponse.Handler.class, MessageConfigResponse.class, MESSAGE_CONFIG_RESPONSE, Side.CLIENT);

        for (IModule module : modules)
        {
            module.preInit(event);
        }
    }

    public void init(FMLInitializationEvent event)
    {

    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

    public void registerModule(IModule module)
    {
        this.modules.add(module);
    }

    public void refreshModules()
    {
        for (IModule module : modules)
        {
            module.onRefresh();
        }
    }

    // Static methods

    public static Core getInstance()
    {
        return INSTANCE;
    }

    public static void createAsClient()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new CoreClient();
            goblinbob.mobends.core.addon.Addons.onClientCoreCreated();
        }
    }

    public static void createAsServer()
    {
        if (INSTANCE == null)
            INSTANCE = new CoreServer();
    }

    public static SimpleNetworkWrapper getNetworkWrapper()
    {
        return INSTANCE.networkWrapper;
    }

    public static void saveConfiguration()
    {
        INSTANCE.getConfiguration().save();
    }

    /**
     * The mod's logger, forwarded to the game's log (latest.log): java.util.logging on its own only
     * reaches the console, so warnings for pack authors (a broken type or extension) went unseen.
     */
    private static Logger createLogger()
    {
        Logger logger = Logger.getLogger("mobends-core");
        org.apache.logging.log4j.Logger target = org.apache.logging.log4j.LogManager.getLogger(ModStatics.MODID);
        logger.setUseParentHandlers(false);
        logger.addHandler(new Handler()
        {
            private final SimpleFormatter formatter = new SimpleFormatter();

            @Override
            public void publish(LogRecord record)
            {
                int level = record.getLevel().intValue();
                org.apache.logging.log4j.Level targetLevel = level >= Level.SEVERE.intValue() ? org.apache.logging.log4j.Level.ERROR
                        : level >= Level.WARNING.intValue() ? org.apache.logging.log4j.Level.WARN
                        : level >= Level.INFO.intValue() ? org.apache.logging.log4j.Level.INFO
                        : org.apache.logging.log4j.Level.DEBUG;
                target.log(targetLevel, formatter.formatMessage(record), record.getThrown());
            }

            @Override
            public void flush()
            {
            }

            @Override
            public void close()
            {
            }
        });
        return logger;
    }

}
