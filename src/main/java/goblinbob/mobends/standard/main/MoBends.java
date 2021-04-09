package goblinbob.mobends.standard.main;

import com.google.gson.Gson;
import goblinbob.bendslib.resource.BinaryPolyReloadListener;
import goblinbob.bendslib.resource.JsonPolyReloadListener;
import goblinbob.bendslib.resource.ParsedElement;
import goblinbob.mobends.core.Core;
import goblinbob.mobends.core.error.ErrorReportRegistry;
import goblinbob.mobends.core.exceptions.InvalidPackFormatException;
import goblinbob.mobends.core.exceptions.ResourceException;
import goblinbob.mobends.core.kumo.AnimatorTemplate;
import goblinbob.mobends.core.kumo.driver.DriverFunctionRegistry;
import goblinbob.mobends.core.kumo.driver.DriverLayerTemplate;
import goblinbob.mobends.core.kumo.driver.node.LookAroundDriverNodeTemplate;
import goblinbob.mobends.core.kumo.driver.node.StandardDriverNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.KeyframeLayerTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.MovementKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.keyframe.node.StandardKeyframeNodeTemplate;
import goblinbob.mobends.core.kumo.trigger.*;
import goblinbob.mobends.core.mutation.MutationInstructions;
import goblinbob.mobends.forge.*;
import goblinbob.mobends.forge.addon.Addons;
import goblinbob.mobends.forge.client.event.KeyboardHandler;
import goblinbob.mobends.forge.client.event.RenderHandler;
import goblinbob.mobends.forge.trigger.EquipmentNameCondition;
import goblinbob.mobends.standard.main.trigger.WolfStateCondition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityType;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;

@Mod(ModStatics.MODID)
public class MoBends
{
    public static final Logger LOGGER = LogManager.getLogger();

    private final SerialContext serialContext;
    private final BenderProvider<SerialContext> benderProvider;
    private final DriverFunctionRegistry<EntityData> driverFunctionRegistry;
    private final KeyboardHandler keyboardHandler;

    public MoBends()
    {
        this.serialContext = new SerialContext();
        this.benderProvider = new BenderProvider<>(this.serialContext);
        this.keyboardHandler = new KeyboardHandler();

        // Notifying the addons context about what bender container to use.
        Addons.setBenderProvider(this.benderProvider);

        this.serialContext.layerRegistry.register("core:keyframe", KeyframeLayerTemplate::deserialize);
        this.serialContext.layerRegistry.register("core:driver", DriverLayerTemplate::deserialize);

        this.serialContext.keyframeNodeRegistry.register("core:standard", StandardKeyframeNodeTemplate::deserialize);
        this.serialContext.keyframeNodeRegistry.register("core:movement", MovementKeyframeNodeTemplate::deserialize);

        this.serialContext.driverNodeRegistry.register("core:standard", StandardDriverNodeTemplate::deserialize);
        this.serialContext.driverNodeRegistry.register("core:look_around", LookAroundDriverNodeTemplate::deserialize);

        this.serialContext.triggerConditionRegistry.register("core:or", OrCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:and", AndCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:not", NotCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:state", StateCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:ticks_passed", TicksPassedCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:equipment_name", EquipmentNameCondition.Template::deserialize);
        this.serialContext.triggerConditionRegistry.register("core:animation_finished", AnimationFinishedCondition.Template::deserialize);

        this.serialContext.triggerConditionRegistry.register("mobends:wolf_state", WolfStateCondition.Template::deserialize);

        this.driverFunctionRegistry = new DriverFunctionRegistry<>();
        StandardDriverFunctions.popualteRegistry(this.driverFunctionRegistry);

        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(this.keyboardHandler);
        MinecraftForge.EVENT_BUS.register(new RenderHandler(benderProvider, driverFunctionRegistry));
        MinecraftForge.EVENT_BUS.register(new DataUpdateHandler(benderProvider::updateDataOnClientTick));

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupClient);

        this.setupResourceListeners();
    }

    private void setupResourceListeners()
    {
        IReloadableResourceManager resourceManager = (IReloadableResourceManager) Minecraft.getInstance().getResourceManager();
        BenderProvider<SerialContext> benderProvider = this.benderProvider;

        resourceManager.registerReloadListener(new BinaryPolyReloadListener<AnimatorTemplate, SerialContext>(serialContext, AnimatorTemplate::deserialize, "bendsanimators", ".bends") {
            @Override
            protected void apply(Collection<ParsedElement<AnimatorTemplate>> animators, IResourceManager innerResourceManager, IProfiler profiler)
            {
                benderProvider.clear();

                for (ParsedElement<AnimatorTemplate> parsed : animators)
                {
                    benderProvider.registerAnimator(Registry.ENTITY_TYPE.get(parsed.relativeLocation), parsed.data);
                }
            }

            @Override
            protected void onResourceIgnored(ResourceLocation location, Exception exception)
            {
                LOGGER.error(String.format("Couldn't load animator from '%s'.", location));
                exception.printStackTrace();
            }
        });

        resourceManager.registerReloadListener(new JsonPolyReloadListener<MutationInstructions>(new Gson(), "bendsmutators", MutationInstructions.class) {
            @Override
            protected void apply(Collection<ParsedElement<MutationInstructions>> parsedElements, IResourceManager innerResourceManager, IProfiler profiler)
            {
                for (ParsedElement<MutationInstructions> parsed : parsedElements)
                {
                    EntityType<?> entityType = Registry.ENTITY_TYPE.get(parsed.relativeLocation);
                    benderProvider.registerMutator(entityType, parsed.data);

                    // Finalizing the entity.
                    try
                    {
                        benderProvider.finalizeEntity(entityType);
                    }
                    catch (ResourceException e)
                    {
                        LOGGER.error(String.format("Couldn't finalize entity '%s'. Reason: %s", parsed.relativeLocation, e.getMessage()));
                    }
                }
            }

            @Override
            protected void onResourceIgnored(ResourceLocation location, Exception exception)
            {
                LOGGER.error(String.format("Couldn't load mutator from '%s'. Reason: %s", location, exception.getMessage()));
            }
        });
    }

    public void onRefresh()
    {
        Core core = new Core();
        ReportOutput reportOutput = new ReportOutput();
        ErrorReportRegistry reportRegistry = new ErrorReportRegistry(reportOutput);

        core.registerErrors(reportRegistry);
        reportRegistry.report(new InvalidPackFormatException("Bruh", "Waddup"));
    }

    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent event)
    {
        this.keyboardHandler.setup();
    }
}
