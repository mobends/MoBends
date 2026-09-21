package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.animation.keyframe.AnimationLoader;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.util.GsonResources;
import goblinbob.mobends.lab.trace.FramePose;
import goblinbob.mobends.lab.trace.PoseCapture;
import goblinbob.mobends.lab.trace.PoseTrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Replays a scenario with a KUMO animator in place of the entity's procedural controller. Everything
 * else (entity data counters, bone smoothing, timing) is shared with {@link ReferenceSession}, so
 * a comparison isolates the animator.
 */
public class KumoSession
{
    public final Scenario scenario;
    public final World world;
    public final ScriptedEntity scripted;
    public final LivingEntityData<?> data;
    public final LabClock clock;
    public final KumoAnimatorState<LivingEntityData<?>> animator;

    private final VanillaModelInputs modelInputs = new VanillaModelInputs();
    private final EntityInputs inputs = new EntityInputs();

    /** Clips registered by the lab (e.g. freshly baked) take precedence over resources. */
    private static final Map<String, KeyframeAnimation> registeredClips = new HashMap<>();

    public static void registerClip(String key, KeyframeAnimation animation)
    {
        registeredClips.put(key, animation);
    }

    public static void clearRegisteredClips()
    {
        registeredClips.clear();
    }

    public static final IKumoInstancingContext INSTANCING = new IKumoInstancingContext()
    {
        @Override
        public KeyframeAnimation getAnimation(String key)
        {
            KeyframeAnimation registered = registeredClips.get(key);
            if (registered != null)
            {
                return registered;
            }
            try
            {
                return AnimationLoader.loadFromPath(key);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("cannot load clip " + key, e);
            }
        }

        @Override
        public AnimatorTemplate getAnimator(String key)
        {
            try
            {
                return loadAnimator(key);
            }
            catch (IOException e)
            {
                throw new IllegalStateException("cannot load animator " + key, e);
            }
        }
    };

    /** -Dlab.debugNodes=true prints every layer's current node per frame. */
    private static final boolean DEBUG_NODES = Boolean.getBoolean("lab.debugNodes");
    private int frameIndex = 0;

    public static AnimatorTemplate loadAnimator(String resource) throws IOException
    {
        LabBootstrap.ensure();
        return GsonResources.get(new ResourceLocation(resource), AnimatorTemplate.class);
    }

    public KumoSession(Scenario scenario, AnimatorTemplate template) throws MalformedKumoTemplateException
    {
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        this.scenario = scenario;
        this.world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);

        EntityLivingBase entity = scenario.kind.createEntity(world);
        this.scripted = new ScriptedEntity(entity, world);
        this.data = scenario.kind.createData(entity, scenario.id().hashCode());
        if (scenario.setup != null)
        {
            scenario.setup.accept(this.data);
        }
        this.clock = new LabClock(scenario.fps);
        this.animator = new KumoAnimatorState<>(template, INSTANCING);
    }

    public PoseTrace run() throws MalformedKumoTemplateException
    {
        PoseTrace trace = new PoseTrace(scenario.kind.id(), scenario.name, scenario.fps);
        int frames = scenario.frameCount();
        for (int i = 0; i < frames; i++)
        {
            trace.frames.add(step());
        }
        return trace;
    }

    public FramePose step() throws MalformedKumoTemplateException
    {
        int ticksStarted = clock.nextFrame();
        for (int t = 0; t < ticksStarted; t++)
        {
            int tick = clock.getTick() - ticksStarted + t + 1;
            ReferenceSession.resetInputs(inputs);
            scenario.script.fill(tick, inputs);
            scripted.tick(inputs);
            data.updateClient();
        }

        data.update(clock.getPartialTicks());

        modelInputs.compute(scripted.entity, clock.getPartialTicks());
        data.headYaw.set(MathHelper.wrapDegrees(modelInputs.headYaw));
        data.headPitch.set(MathHelper.wrapDegrees(modelInputs.headPitch));
        data.limbSwing.set(modelInputs.limbSwing);
        data.limbSwingAmount.set(modelInputs.limbSwingAmount);
        data.swingProgress.set(modelInputs.swingProgress);

        animator.update(data, goblinbob.mobends.core.client.event.DataUpdateHandler.ticksPerFrame);
        if (DEBUG_NODES)
        {
            StringBuilder line = new StringBuilder(String.format("frame %d tick %.2f:", frameIndex, clock.getTicks()));
            for (goblinbob.mobends.core.kumo.state.ILayerState layer : animator.getLayers())
            {
                if (layer instanceof goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState)
                {
                    line.append(' ').append(((goblinbob.mobends.core.kumo.state.keyframe.KeyframeLayerState) layer).getCurrentNode().getName());
                }
            }
            System.out.println(line);
        }
        frameIndex++;

        FramePose frame = PoseCapture.capture(data);
        frame.tick = clock.getTicks();
        return frame;
    }
}
