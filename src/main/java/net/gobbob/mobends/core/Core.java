package net.gobbob.mobends.core;

import net.gobbob.mobends.core.configuration.CoreConfig;
import net.gobbob.mobends.core.network.msg.MessageClientConfigure;
import net.gobbob.mobends.standard.main.ModStatics;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import java.util.logging.Logger;

public abstract class Core<T extends CoreConfig>
{

    public static Core INSTANCE;
    public static final Logger LOG = Logger.getLogger("mobends-core");

    private SimpleNetworkWrapper networkWrapper;

    public abstract T getConfiguration();

    public void preInit(FMLPreInitializationEvent event)
    {
        networkWrapper = NetworkRegistry.INSTANCE.newSimpleChannel(ModStatics.MODID);
        networkWrapper.registerMessage(MessageClientConfigure.Handler.class, MessageClientConfigure.class, 0, Side.CLIENT);
    }

    public void init(FMLInitializationEvent event)
    {

    }

    public void postInit(FMLPostInitializationEvent event)
    {

    }

    // Static methods

    public static void createAsClient()
    {
        if (INSTANCE == null)
            INSTANCE = new CoreClient();
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

}
