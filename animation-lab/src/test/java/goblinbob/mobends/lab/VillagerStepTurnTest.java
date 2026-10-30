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
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the villager, grown and baby, checked by forward kinematics as
 * {@link StepTurnTest} checks the golem's: the soles are put back together from the bones' smoothed
 * rotations, the render rotation and the local offset, drawn at RenderVillager's scale, and
 * followed in the world.
 */
public class VillagerStepTurnTest
{

    /** The villager's legs, as its animator declares them: hip, knee, foot. */
    private static final String[] UPPER = {"rightLeg", "leftLeg"};
    private static final String[] LOWER = {"foreRightLeg", "foreLeftLeg"};
    private static final float[][] HIP = {{-2, 12, 0}, {2, 12, 0}};
    private static final float[] KNEE = {0, 6, -2};
    private static final float[] FOOT = {0, 6, 2};
    private static final float GROUND = 24;
    /** The entity's origin in the model (vanilla draws the model 1.501 blocks above it, scaled). */
    private static final double ORIGIN = 1.501 * 16;
    /** Villagers wander at about 0.19 blocks a tick (attribute 0.5, EntityAIWanderAvoidWater at 0.6). */
    private static final double WANDER = 0.19;
    /** The animator's finishDrift: how far from its place a foot the walk left may stay standing. */
    private static final double SETTLE_RADIUS = 4;

    @Test
    void settlesFromWhereverItStops() throws Exception
    {
        // It walks by a clip, its feet sliding; stopping at any point of the stride, the driver
        // takes the feet over where the walk left them, without sliding them, and a foot left
        // further than SETTLE_RADIUS from its place steps back under the body. Grown and baby, stopping a tick apart across a whole stride (the clip's
        // cycle is 9.4 of limb swing, which vanilla advances by 4 times the speed, at most 1 a tick:
        // about 12 ticks at a wander).
        for (boolean child : new boolean[] {false, true})
        {
            int stayed = 0;
            for (int stopAt = 100; stopAt < 113; stopAt++)
            {
                final int stop = stopAt;
                Villager villager = new Villager(child);
                villager.measureFrom = stop;
                villager.run(stop + 80, tick -> villager.inputs.forwardSpeed = tick >= 40 && tick < stop ? WANDER : 0);

                String at = (child ? " (baby)" : "") + " stopping at tick " + stop;
                assertTrue(villager.maxSlide < 0.02, "a planted foot slid " + villager.maxSlide + " blocks" + at);
                // Taken over where they are: hardly faster than the walk moved them (a foot in the air
                // also starts coming down). Placed at rest, they jump up to four times as far.
                assertTrue(villager.maxFootJump < 1.75 * villager.maxFootMoveBefore, "a foot jumped " + villager.maxFootJump + " units in a frame, against "
                        + villager.maxFootMoveBefore + " walking" + at);
                for (int i = 0; i < 2; i++)
                {
                    assertEquals(GROUND, villager.soleY[i], 0.3, "foot " + i + " isn't on the ground" + at);
                    assertTrue(villager.offRest[i] < SETTLE_RADIUS + 0.5, "foot " + i + " stopped " + villager.offRest[i] + " units off its place" + at);
                }
                // A foot the walk left out of place steps back once, not shuffling after the walk's
                // lingering velocity; one nearer stays where it is.
                assertTrue(villager.steps <= 1, villager.steps + " steps settling" + at);
                if (villager.steps == 0)
                {
                    stayed++;
                }
            }
            // Stopping anywhere in the stride, the feet are often left where they are.
            assertTrue(stayed >= 3, "the feet stayed where the walk left them after only " + stayed + " of 13 stops" + (child ? " (baby)" : ""));
        }
    }

    @Test
    void keepsItsTorsoAndArmsWithinTheirTwist() throws Exception
    {
        // The upper body only twists a little with the steps (12° at most, eased in on springs),
        // walking and turning, however long it goes on: nothing accumulates from frame to frame.
        Villager villager = new Villager(false);
        villager.run(400, tick -> {
            villager.inputs.forwardSpeed = tick >= 40 && tick < 300 ? WANDER : 0;
            villager.inputs.bodyYaw = tick < 100 ? 0 : tick < 200 ? Math.min(120, (tick - 100) * 3F) : Math.max(-60, 120 - (tick - 200) * 3F);
        });
        assertTrue(villager.maxTorsoAngle < 13, "the torso turned " + villager.maxTorsoAngle + "°");
        assertTrue(villager.maxArmsAngle < 20, "the arms turned " + villager.maxArmsAngle + "° off their rest");
    }

    @Test
    void keepsItsHeightAsItStopsAndSetsOff() throws Exception
    {
        // Walking (the driver off) and standing (the driver on), the body is drawn at the same
        // height, and stopping or setting off doesn't jolt it: grown and baby, stopping at
        // different points of the stride, standing a while, then setting off again.
        for (boolean child : new boolean[] {false, true})
        {
            for (int stopAt = 100; stopAt < 113; stopAt += 3)
            {
                final int stop = stopAt;
                Villager villager = new Villager(child);
                villager.run(stop + 80, tick -> villager.inputs.forwardSpeed = tick >= 40 && (tick < stop || tick >= stop + 40) ? WANDER : 0);
                String at = (child ? " (baby)" : "") + " stopping at tick " + stop;
                assertTrue(villager.maxHipJump < 0.3, "the hips jumped " + villager.maxHipJump + " units in a frame" + at);
                // Standing with the feet apart, they go down as far as the feet need to reach (at most
                // the animator's walkCrouch, 1), and the steps' landings dip them a little.
                assertTrue(villager.maxHipDrop < 1.6, "the hips went " + villager.maxHipDrop + " units off their height walking" + at);
            }
        }
    }

    @Test
    void wobblesItsNoseWithEachStep() throws Exception
    {
        // Walking, the nose swings out at every footfall (twice a stride: about 12 ticks a stride at
        // a wander), a few degrees, and hardly back into the face.
        Villager walker = new Villager(false);
        walker.recordNose = true;
        walker.run(260, tick -> walker.inputs.forwardSpeed = tick >= 40 && tick < 160 ? WANDER : 0);
        int flicks = 0;
        double out = 0, in = 0;
        for (int f = 1; f + 1 < walker.nose.size(); f++)
        {
            double[] before = walker.nose.get(f - 1), now = walker.nose.get(f), after = walker.nose.get(f + 1);
            if (now[0] >= 60 && now[0] < 160)
            {
                out = Math.max(out, -now[1]);
                in = Math.max(in, now[1]);
                // A swing out peaking: the most outward (negative) frame round it, and well out.
                if (now[1] < before[1] && now[1] <= after[1] && now[1] < -3)
                {
                    flicks++;
                }
            }
        }
        assertTrue(flicks >= 100 / 12.4 * 2 * 0.8, "the nose swung out " + flicks + " times in 100 ticks of walking");
        assertTrue(out > 4 && out < 10, "the nose swung out " + out + "°");
        assertTrue(in < 2.5, "the nose swung " + in + "° into the face");
        // Standing still a while, it hangs at rest.
        double last = walker.nose.get(walker.nose.size() - 1)[1];
        assertEquals(0, last, 1, "the nose still hangs at " + last + "° after standing");

        // Turning on the spot, the steps the driver takes wobble it too.
        Villager turner = new Villager(false);
        turner.recordNose = true;
        turner.run(160, tick -> {
            float body = tick < 60 ? 0 : Math.min(60, (tick - 60) * 6);
            turner.inputs.bodyYaw = body;
            turner.inputs.headYaw = (tick < 50 ? 0 : 60) - body;
        });
        double turning = 0;
        for (double[] frame : turner.nose)
        {
            if (frame[0] >= 60)
            {
                turning = Math.max(turning, -frame[1]);
            }
        }
        assertTrue(turning > 2 && turning < 8, "turning, the nose swung out " + turning + "°");
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        for (boolean child : new boolean[] {false, true})
        {
            Villager villager = new Villager(child);
            final int turnAt = 60;
            // Vanilla turns the body 60° over 10 ticks, the head first.
            villager.run(200, tick -> {
                float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
                villager.inputs.bodyYaw = body;
                villager.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
            });
            String as = child ? " (baby)" : "";
            assertTrue(villager.steps >= 2, "only " + villager.steps + " steps" + as);
            assertTrue(villager.maxSlide < 0.02, "a planted foot slid " + villager.maxSlide + " blocks" + as);
            assertEquals(60, villager.shownYaw, 1, "the turn didn't end squared up" + as);
        }
    }

    private static double[] sole(Quaternion upper, Quaternion lower)
    {
        double[] foot = rotate(lower, FOOT[0], FOOT[1], FOOT[2]);
        return rotate(upper, KNEE[0] + foot[0], KNEE[1] + foot[1], KNEE[2] + foot[2]);
    }

    /** How far a rotation turns, in degrees. */
    private static double angle(Quaternion q)
    {
        return Math.toDegrees(2 * Math.acos(Math.min(1, Math.abs(q.w))));
    }

    /** How far apart two rotations are, in degrees. */
    private static double angleBetween(Quaternion a, Quaternion b)
    {
        double dot = Math.abs(a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

    private static double[] rotate(Quaternion q, double x, double y, double z)
    {
        // v + 2w(u × v) + 2u × (u × v)
        double cx = q.y * z - q.z * y, cy = q.z * x - q.x * z, cz = q.x * y - q.y * x;
        double ccx = q.y * cz - q.z * cy, ccy = q.z * cx - q.x * cz, ccz = q.x * cy - q.y * cx;
        return new double[] {x + 2 * (q.w * cx + ccx), y + 2 * (q.w * cy + ccy), z + 2 * (q.w * cz + ccz)};
    }

    /** A villager standing on flat ground, animated at 60 fps, its soles followed in the world. */
    private static class Villager
    {
        final EntityInputs inputs = new EntityInputs();
        final EntityVillager entity;
        final ScriptedEntity scripted;
        final DefinedEntityData<EntityVillager> data;
        final KumoAnimatorState animator;
        /** Model units per block: RenderVillager draws the model at 0.9375, a baby's at half that. */
        final double unitsPerBlock;
        /**
         * Model units per unit of the local offset, which the renderer applies before vanilla's
         * scale (in sixteenths of a block, times the definition's child scale for a baby).
         */
        final double offsetUnits;

        /** The tick from which steps and slides are counted. */
        int measureFrom;
        /** Whether to record the nose's swing about X (degrees, out is negative) every frame, with the tick. */
        boolean recordNose;
        final java.util.List<double[]> nose = new java.util.ArrayList<>();
        /**
         * The most a sole moved in one frame (model units): from {@link #measureFrom} on, and over
         * the ten ticks before it.
         */
        double maxFootJump, maxFootMoveBefore;
        final double[][] lastSole = new double[2][];
        double shownYaw;
        final double[] soleY = new double[2];
        /** How far each sole is from under its hip, in model units, in the shown frame. */
        final double[] offRest = new double[2];
        boolean alternated = true;
        int lastLifted = -1;
        double maxSlide;
        int steps;
        /** The most the torso turned, and the arms off how they rest, in degrees. */
        double maxTorsoAngle, maxArmsAngle;
        Quaternion armsAtRest;
        /** Per frame, the most the hips moved, and the furthest they were from their height walking (model units). */
        double maxHipJump, maxHipDrop;
        double lastHipY = Double.NaN;

        Villager(boolean child) throws Exception
        {
            EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/villager.json"));
            LabBootstrap.ensure();
            net.minecraft.entity.Entity.resetIds();
            World world = new World();
            Minecraft.getMinecraft().world = world;
            Minecraft.getMinecraft().player = new EntityPlayerSP(world);
            entity = new EntityVillager(world);
            entity.child = child;
            scripted = new ScriptedEntity(entity, world);
            data = DefinedEntityData.create(definition, entity);
            animator = new KumoAnimatorState(definition.entityScope(entity.getClass()), KumoSession.loadAnimator(definition.animator), true,
                    java.util.Collections.emptyList(), java.util.Collections.emptyList(), KumoSession.INSTANCING);
            double scale = child ? 0.9375 * 0.5 : 0.9375;
            unitsPerBlock = 16 / scale;
            offsetUnits = (child ? definition.childScale : 1) / scale;
        }

        void run(int ticks, IntConsumer script) throws Exception
        {
            LabClock clock = new LabClock(60);
            VanillaModelInputs modelInputs = new VanillaModelInputs();
            double[][] planted = new double[2][];
            boolean[] wasUp = new boolean[2];
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

                // The renderer turns the model by -bodyYaw, the render rotation, the local offset,
                // then vanilla's own: the model is scaled, drawn flipped in Y and Z, the entity's
                // origin ORIGIN units down it.
                Quaternion render = data.renderRotation.getSmooth();
                double[] forward = rotate(render, 0, 0, 1);
                double bodyYaw = data.getVariable("entityBodyYaw");
                shownYaw = bodyYaw - Math.toDegrees(Math.atan2(forward[0], forward[2]));
                // How high the hips are drawn: the local offset and the whole model's (the landing kick).
                double hipY = data.localOffset.getY() * offsetUnits + data.globalOffset.getY() * offsetUnits;
                if (!Double.isNaN(lastHipY) && clock.getTick() >= 60)
                {
                    maxHipJump = Math.max(maxHipJump, Math.abs(hipY - lastHipY));
                    maxHipDrop = Math.max(maxHipDrop, Math.abs(hipY));
                }
                lastHipY = hipY;
                if (recordNose)
                {
                    Quaternion q = data.getPart("nose").rotation.getSmooth();
                    nose.add(new double[] {clock.getTick() + pt, Math.toDegrees(2 * Math.atan2(q.x, q.w))});
                }
                maxTorsoAngle = Math.max(maxTorsoAngle, angle(data.getPart("body").rotation.getSmooth()));
                Quaternion arms = data.getPart("arms").rotation.getSmooth();
                if (armsAtRest == null)
                {
                    armsAtRest = new Quaternion(arms.x, arms.y, arms.z, arms.w);
                }
                maxArmsAngle = Math.max(maxArmsAngle, angleBetween(armsAtRest, arms));
                double yaw = Math.toRadians(bodyYaw), cos = Math.cos(yaw), sin = Math.sin(yaw);
                double shown = Math.toRadians(shownYaw), cosS = Math.cos(shown), sinS = Math.sin(shown);
                double[] offset = {data.localOffset.getX() * offsetUnits, data.localOffset.getY() * offsetUnits, data.localOffset.getZ() * offsetUnits};
                double px = data.getVariable("entityWorldX"), pz = data.getVariable("entityWorldZ");

                for (int i = 0; i < 2; i++)
                {
                    double[] sole = sole(data.getPart(UPPER[i]).rotation.getSmooth(), data.getPart(LOWER[i]).rotation.getSmooth());
                    double mx = HIP[i][0] + sole[0], my = HIP[i][1] + sole[1], mz = HIP[i][2] + sole[2];
                    double[] o = rotate(render, mx + offset[0], ORIGIN - my + offset[1], -mz + offset[2]);
                    double wx = px + (o[0] * cos - o[2] * sin) / unitsPerBlock, wz = pz + (o[0] * sin + o[2] * cos) / unitsPerBlock;
                    my = ORIGIN - o[1];
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
                    offRest[i] = Math.hypot(sx - (HIP[i][0] + KNEE[0] + FOOT[0]), sz - (HIP[i][2] + KNEE[2] + FOOT[2]));
                    boolean up = my < GROUND - 0.3;
                    if (up && !wasUp[i] && clock.getTick() >= measureFrom)
                    {
                        if (i == lastLifted && inputs.forwardSpeed != 0)
                        {
                            alternated = false;
                        }
                        lastLifted = i;
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
    }

}
