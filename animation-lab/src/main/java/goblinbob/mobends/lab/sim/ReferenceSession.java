package goblinbob.mobends.lab.sim;

import goblinbob.mobends.core.animation.controller.IAnimationController;
import goblinbob.mobends.core.data.LivingEntityData;
import goblinbob.mobends.core.kumo.variable.KumoVariableRegistry;
import goblinbob.mobends.lab.trace.FramePose;
import goblinbob.mobends.lab.trace.PoseCapture;
import goblinbob.mobends.lab.trace.PoseTrace;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

/**
 * Replays a scenario through the mod's original animation pipeline (the "reference") and records
 * the resulting bone poses per frame.
 *
 * Per frame the order matches the client:
 * <ol>
 *   <li>client tick (if one started): script inputs, then {@code EntityData.updateClient()}</li>
 *   <li>render tick start: {@code EntityData.update(partialTicks)} advances the bone smoothing</li>
 *   <li>entity render: vanilla model inputs, then {@code controller.perform(data)} writes new targets</li>
 *   <li>the model is drawn with the smoothed values, which is what gets captured</li>
 * </ol>
 */
public class ReferenceSession
{
    public final Scenario scenario;
    public final World world;
    public final ScriptedEntity scripted;
    public final LivingEntityData<?> data;
    public final LabClock clock;

    private final VanillaModelInputs modelInputs = new VanillaModelInputs();
    private final EntityInputs inputs = new EntityInputs();

    @SuppressWarnings("unchecked")
    public ReferenceSession(Scenario scenario)
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
    }

    /** Runs the whole scenario and returns the trace. */
    public PoseTrace run()
    {
        PoseTrace trace = new PoseTrace(scenario.kind.id(), scenario.name, scenario.fps);
        int frames = scenario.frameCount();
        for (int i = 0; i < frames; i++)
        {
            FramePose frame = step();
            trace.frames.add(frame);
        }
        return trace;
    }

    /** Advances one frame and returns the captured pose. */
    @SuppressWarnings("unchecked")
    public FramePose step()
    {
        int ticksStarted = clock.nextFrame();
        for (int t = 0; t < ticksStarted; t++)
        {
            int tick = clock.getTick() - ticksStarted + t + 1;
            resetInputs(inputs);
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

        KumoVariableRegistry.instance.provideTemporaryData(data);

        IAnimationController<LivingEntityData<?>> controller = (IAnimationController<LivingEntityData<?>>) data.getController();
        controller.perform(data);

        FramePose frame = PoseCapture.capture(data);
        frame.tick = clock.getTicks();
        return frame;
    }

    static void resetInputs(EntityInputs inputs)
    {
        EntityInputs fresh = new EntityInputs();
        // Keep object identity so scripts can hold onto it; copy defaults over.
        inputs.forwardSpeed = fresh.forwardSpeed;
        inputs.strafeSpeed = fresh.strafeSpeed;
        inputs.jump = fresh.jump;
        inputs.verticalSpeed = fresh.verticalSpeed;
        inputs.noGravity = fresh.noGravity;
        inputs.bodyYaw = fresh.bodyYaw;
        inputs.headYaw = fresh.headYaw;
        inputs.headPitch = fresh.headPitch;
        inputs.sprinting = fresh.sprinting;
        inputs.sneaking = fresh.sneaking;
        inputs.inWater = fresh.inWater;
        inputs.onLadder = fresh.onLadder;
        inputs.flying = fresh.flying;
        inputs.sleeping = fresh.sleeping;
        inputs.riding = fresh.riding;
        inputs.elytraTicks = fresh.elytraTicks;
        inputs.attack = fresh.attack;
        inputs.itemUseCount = fresh.itemUseCount;
        inputs.itemUseMaxCount = fresh.itemUseMaxCount;
        inputs.mainHand = fresh.mainHand;
        inputs.offHand = fresh.offHand;
        inputs.wolfSitting = fresh.wolfSitting;
        inputs.wolfInterested = fresh.wolfInterested;
        inputs.wolfShaking = fresh.wolfShaking;
        inputs.spiderClimbing = fresh.spiderClimbing;
        inputs.squidRotation = fresh.squidRotation;
        inputs.health = fresh.health;
    }
}
