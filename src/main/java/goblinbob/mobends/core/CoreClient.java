package goblinbob.mobends.core;

import goblinbob.mobends.core.addon.Addons;
import goblinbob.mobends.core.asset.AssetReloadListener;
import goblinbob.mobends.core.asset.AssetsModule;
import goblinbob.mobends.core.bender.EntityBenderRegistry;
import goblinbob.mobends.core.client.AnimationPolicy;
import goblinbob.mobends.core.client.event.*;
import goblinbob.mobends.core.configuration.CoreClientConfig;
import goblinbob.mobends.core.connection.ConnectionManager;
import goblinbob.mobends.core.data.EntityDatabase;
import goblinbob.mobends.core.definition.DefinedFields;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.env.EnvironmentModule;
import goblinbob.mobends.core.kumo.AnimationLimits;
import goblinbob.mobends.core.kumo.AnimatorResources;
import goblinbob.mobends.core.supporters.SupporterContent;
import goblinbob.mobends.core.types.EntityTypeRegistry;
import goblinbob.mobends.core.vanilla.VanillaEntityFields;
import goblinbob.mobends.core.vanilla.VanillaModelParts;
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

        KeyboardHandler.initKeyBindings();

        MinecraftForge.EVENT_BUS.register(new EntityRenderHandler());
        MinecraftForge.EVENT_BUS.register(new DataUpdateHandler());
        MinecraftForge.EVENT_BUS.register(new KeyboardHandler());
        MinecraftForge.EVENT_BUS.register(new WorldJoinHandler());

        // Registering a listener to whenever resources have been reloaded.
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager();
        resourceManager.registerReloadListener(new AssetReloadListener());
        // Resource packs can add, change or remove types, and the model definitions and animators they use.
        resourceManager.registerReloadListener(manager -> reloadAnimation());

        // Model definitions find vanilla fields through accessors generated against them (see DefinedFields).
        DefinedFields.install(VanillaEntityFields::get, VanillaModelParts::get);

        // Resource packs' animation is limited as the server says.
        AnimationLimits.setProvider(AnimationPolicy.INSTANCE::limits);
    }

    @Override
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);

        // The types load on first use, or with the first resource reload.
        EntityBenderRegistry.instance.applyConfiguration(configuration);
    }

    /**
     * Reloads the entity types, extensions, animators, clips and model definitions: after a
     * resource reload, or when the server changes what resource packs may do.
     */
    public static void reloadAnimation()
    {
        AnimationPolicy.INSTANCE.onContentReloaded();
        ModelDefinitions.INSTANCE.clearCache();
        AnimatorResources.INSTANCE.clearCache();
        EntityDatabase.instance.refresh();
        EntityBenderRegistry.instance.refreshMutators();
        EntityTypeRegistry.INSTANCE.reload();
    }

    /**
     * Reloads the animation and refreshes the addons and modules: the refresh key, and changes to
     * the mod's configuration.
     */
    public static void refresh()
    {
        reloadAnimation();
        Addons.onRefresh();
        INSTANCE.refreshModules();
    }

    @Nullable
    public static CoreClient getInstance()
    {
        return INSTANCE;
    }
}
