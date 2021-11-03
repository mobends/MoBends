package goblinbob.mobends.core;

import goblinbob.mobends.core.asset.AssetReloadListener;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.event.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.pack.PackManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

import javax.annotation.Nullable;

public class CoreClient extends Core<CoreClientConfig>
{
    private static CoreClient INSTANCE;

    private CoreClientConfig configuration;

    CoreClient()
    {
        INSTANCE = this;

        registerModule(new EnvironmentModule.Factory());
        registerModule(new ConnectionManager.Factory());
        registerModule(new AssetsModule.Factory());
        registerModule(new SupporterContent.Factory());
    }

    @Override
    public CoreClientConfig getConfiguration()
    {
        return configuration;
    }

    @Override
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);

        configuration = new CoreClientConfig(event.getSuggestedConfigurationFile());
    }

    @Override
    public void init(FMLInitializationEvent event)
    {
        super.init(event);

        PackManager.INSTANCE.initialize(configuration);
        KeyboardHandler.initKeyBindings();

        MinecraftForge.EVENT_BUS.register(new EntityRenderHandler());
        MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(new FluxHandler());
        MinecraftForge.EVENT_BUS.register(new WorldJoinHandler());

        // Registering a listener to whenever resources have been reloaded.
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        resourceManager.registerReloadListener(new AssetReloadListener());
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);

        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    @Nullable
    public static CoreClient getInstance()
    {
        return INSTANCE;
    }
}
