package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.lab.sim.VanillaModelInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.Collections;
import java.util.function.DoubleConsumer;
import java.util.function.Function;
import java.util.function.IntConsumer;

/**
 * A mob that stands on {@code core:step_turn}, on flat ground, animated at 60 fps, its soles
 * followed in the world by forward kinematics (as {@link StepTurnTest} follows the golem's): put
 * back together from the legs' smoothed rotations, the render rotation and the offsets, drawn at the
 * renderer's scale. What the tests check is gathered as it runs.
 */
class PlantedFeetRig
{

    /** A leg as the animator declares it to the driver: its bones, hip, knee and foot. */
    static final class Leg
    {
        final String upper, lower;
        final float[] hip, knee, foot;

        Leg(String upper, String lower, float[] hip, float[] knee, float[] foot)
        {
            this.upper = upper;
            this.lower = lower;
            this.hip = hip;
            this.knee = knee;
            this.foot = foot;
        }

        /** Where its sole stands at rest, in the model. */
        double restX() { return hip[0] + knee[0] + foot[0]; }

        double restZ() { return hip[2] + knee[2] + foot[2]; }
    }

    /** The ground, in model units down from the model's origin (the soles at rest). */
    static final float GROUND = 24;
    /** The entity's origin in the model (vanilla draws the model 1.501 blocks above it, scaled). */
    static final double ORIGIN = 1.501 * 16;

    final EntityInputs inputs = new EntityInputs();
    final EntityLivingBase entity;
    final ScriptedEntity scripted;
    final DefinedEntityData<EntityLivingBase> data;
    final KumoAnimatorState animator;
    final Leg[] legs;
    /** Model units per block, as the model is drawn. */
    final double unitsPerBlock;
    /**
     * Model units per unit of the offsets, which the renderer applies before vanilla's scale (in
     * sixteenths of a block, times the definition's child scale for a baby).
     */
    final double offsetUnits;

    /** Called every frame once the animator has posed it, with the time in ticks. */
    DoubleConsumer onFrame = time -> {};
    /** The tick from which steps and slides are counted. */
    int measureFrom;

    double shownYaw;
    final double[] soleY;
    /** How far each sole is from under its hip, in model units, in the shown frame. */
    final double[] offRest;
    double maxSlide;
    int steps;
    /**
     * The most a sole moved in one frame (model units): from {@link #measureFrom} on, and over the
     * ten ticks before it.
     */
    double maxFootJump, maxFootMoveBefore;
    /**
     * Per frame from tick 60, the most the hips moved, and the furthest they were from their height
     * at rest (model units): the local offset and the whole model's.
     */
    double maxHipJump, maxHipDrop;

    /**
     * @param scale the renderer's scale of a grown mob (a baby's is the definition's child scale of it)
     * @param id    the entity's id, which some models' entity scopes read (0 leaves the next one)
     */
    PlantedFeetRig(String model, Function<World, EntityLivingBase> create, boolean child, int id, double scale, Leg... legs) throws Exception
    {
        EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", model));
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        entity = create.apply(world);
        if (id != 0)
        {
            entity.setEntityId(id);
        }
        entity.child = child;
        scripted = new ScriptedEntity(entity, world);
        data = DefinedEntityData.create(definition, entity);
        animator = new KumoAnimatorState(definition.entityScope(entity.getClass()), KumoSession.loadAnimator(definition.animator), true,
                Collections.emptyList(), Collections.emptyList(), KumoSession.INSTANCING);
        this.legs = legs;
        soleY = new double[legs.length];
        offRest = new double[legs.length];
        double drawn = child ? scale * definition.childScale : scale;
        unitsPerBlock = 16 / drawn;
        offsetUnits = (child ? definition.childScale : 1) / drawn;
    }

    void run(int ticks, IntConsumer script) throws Exception
    {
        LabClock clock = new LabClock(60);
        VanillaModelInputs modelInputs = new VanillaModelInputs();
        double[][] planted = new double[legs.length][];
        double[][] lastSole = new double[legs.length][];
        boolean[] wasUp = new boolean[legs.length];
        double lastHipY = Double.NaN;
        while (clock.getTick() < ticks)
        {
            int ticksStarted = clock.nextFrame();
            for (int t = 0; t < ticksStarted; t++)
            {
                int tick = clock.getTick() - ticksStarted + t + 1;
                float forward = (float) inputs.forwardSpeed;
                inputs.reset();
                inputs.forwardSpeed = forward;
                script.accept(tick);
                scripted.tick(inputs);
                data.updateClient();
            }
            float pt = clock.getPartialTicks();
            data.update(pt);
            modelInputs.compute(entity, pt);
            data.headYaw = MathHelper.wrapDegrees(modelInputs.headYaw);
            data.headPitch = MathHelper.wrapDegrees(modelInputs.headPitch);
            data.limbSwing = modelInputs.limbSwing;
            data.limbSwingAmount = modelInputs.limbSwingAmount;
            data.swingProgress = modelInputs.swingProgress;
            animator.update(data, DataUpdateHandler.ticksPerFrame);
            onFrame.accept(clock.getTick() + pt);

            // The renderer moves the model by the whole model's offset, turns it by -bodyYaw, the
            // render rotation, the local offset, then vanilla's own: the model is scaled, drawn
            // flipped in Y and Z, the entity's origin ORIGIN units down it.
            Quaternion render = data.renderRotation.getSmooth();
            double[] facing = rotate(render, 0, 0, 1);
            double bodyYaw = data.getVariable("entityBodyYaw");
            shownYaw = bodyYaw - Math.toDegrees(Math.atan2(facing[0], facing[2]));
            double rootY = data.globalOffset.getY() * offsetUnits;
            double hipY = data.localOffset.getY() * offsetUnits + rootY;
            if (!Double.isNaN(lastHipY) && clock.getTick() >= 60)
            {
                maxHipJump = Math.max(maxHipJump, Math.abs(hipY - lastHipY));
                maxHipDrop = Math.max(maxHipDrop, Math.abs(hipY));
            }
            lastHipY = hipY;
            double yaw = Math.toRadians(bodyYaw), cos = Math.cos(yaw), sin = Math.sin(yaw);
            double shown = Math.toRadians(shownYaw), cosS = Math.cos(shown), sinS = Math.sin(shown);
            double[] offset = {data.localOffset.getX() * offsetUnits, data.localOffset.getY() * offsetUnits, data.localOffset.getZ() * offsetUnits};
            double px = data.getVariable("entityWorldX"), pz = data.getVariable("entityWorldZ");

            for (int i = 0; i < legs.length; i++)
            {
                Leg leg = legs[i];
                double[] sole = sole(leg, data.getPart(leg.upper).rotation.getSmooth(), data.getPart(leg.lower).rotation.getSmooth());
                double mx = leg.hip[0] + sole[0], my = leg.hip[1] + sole[1], mz = leg.hip[2] + sole[2];
                double[] o = rotate(render, mx + offset[0], ORIGIN - my + offset[1], -mz + offset[2]);
                double wx = px + (o[0] * cos - o[2] * sin) / unitsPerBlock, wz = pz + (o[0] * sin + o[2] * cos) / unitsPerBlock;
                my = ORIGIN - (o[1] + rootY);
                soleY[i] = my;
                double[] now = {wx * unitsPerBlock, my, wz * unitsPerBlock};
                if (lastSole[i] != null)
                {
                    double moved = Math.sqrt(Math.pow(now[0] - lastSole[i][0], 2) + Math.pow(now[1] - lastSole[i][1], 2) + Math.pow(now[2] - lastSole[i][2], 2));
                    if (clock.getTick() >= measureFrom)
                    {
                        maxFootJump = Math.max(maxFootJump, moved);
                    }
                    else if (clock.getTick() >= measureFrom - 10)
                    {
                        maxFootMoveBefore = Math.max(maxFootMoveBefore, moved);
                    }
                }
                lastSole[i] = now;
                // Under the hip in the shown frame: back from the world, turned by the shown yaw.
                double dx = (wx - px) * unitsPerBlock, dz = (wz - pz) * unitsPerBlock;
                double sx = dx * cosS + dz * sinS, sz = dx * sinS - dz * cosS;
                offRest[i] = Math.hypot(sx - leg.restX(), sz - leg.restZ());
                boolean up = my < GROUND - 0.3;
                if (up && !wasUp[i] && clock.getTick() >= measureFrom)
                {
                    steps++;
                }
                wasUp[i] = up;
                // Once the driver has faded in.
                boolean down = my > GROUND - 0.05 && clock.getTick() >= Math.max(20, measureFrom);
                if (!down)
                {
                    planted[i] = null;
                }
                else if (planted[i] == null)
                {
                    planted[i] = new double[] {wx, wz};
                }
                else
                {
                    maxSlide = Math.max(maxSlide, Math.hypot(wx - planted[i][0], wz - planted[i][1]));
                }
            }
        }
    }

    private static double[] sole(Leg leg, Quaternion upper, Quaternion lower)
    {
        double[] foot = rotate(lower, leg.foot[0], leg.foot[1], leg.foot[2]);
        return rotate(upper, leg.knee[0] + foot[0], leg.knee[1] + foot[1], leg.knee[2] + foot[2]);
    }

    /** How far a rotation turns, in degrees. */
    static double angle(Quaternion q)
    {
        return Math.toDegrees(2 * Math.acos(Math.min(1, Math.abs(q.w))));
    }

    /** How far apart two rotations are, in degrees. */
    static double angleBetween(Quaternion a, Quaternion b)
    {
        double dot = Math.abs(a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

    static double[] rotate(Quaternion q, double x, double y, double z)
    {
        // v + 2w(u × v) + 2u × (u × v)
        double cx = q.y * z - q.z * y, cy = q.z * x - q.x * z, cz = q.x * y - q.y * x;
        double ccx = q.y * cz - q.z * cy, ccy = q.z * cx - q.x * cz, ccz = q.x * cy - q.y * cx;
        return new double[] {x + 2 * (q.w * cx + ccx), y + 2 * (q.w * cy + ccy), z + 2 * (q.w * cz + ccz)};
    }

}
