package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.definition.ModelDefinitions;
import goblinbob.mobends.core.kumo.driver.LegIk;
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
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the iron golem, checked by forward kinematics: the soles are put back
 * together from the bones' smoothed rotations, the render rotation and the local offset, with
 * vanilla's own rocking of a walking golem, as the renderer draws them, and followed in the world.
 */
public class StepTurnTest
{

    /** The golem's legs, as its animator declares them: hip, knee, foot. */
    private static final String[] UPPER = {"leftLeg", "rightLeg"};
    private static final String[] LOWER = {"leftForeLeg", "rightForeLeg"};
    private static final float[][] HIP = {{-4, 11, 0}, {5, 11, 0}};
    private static final float[] KNEE = {0, 5, -3};
    private static final float[] FOOT = {-0.5F, 8, 2.5F};
    private static final float GROUND = 24;
    /** The entity's origin in the model (vanilla draws the model 1.501 blocks above it). */
    private static final double ORIGIN = 1.501 * 16;
    /** The animator's weightRoll: the most the golem leans over its feet. */
    private static final double WEIGHT_ROLL = 3;

    @Test
    void ikReachesItsTarget()
    {
        Quaternion upper = new Quaternion(), lower = new Quaternion();
        double[][] targets = {{0, 12.5, 0}, {1.5, 12, -2}, {-2, 11, 3}, {0.5, 10, 1}};
        for (double twist : new double[] {0, 0.4, -0.7})
        {
            for (double[] target : targets)
            {
                LegIk.solve(KNEE, FOOT, 1, twist, target[0], target[1], target[2], upper, lower);
                double[] sole = sole(upper, lower);
                assertEquals(target[0], sole[0], 1e-3, "x");
                assertEquals(target[1], sole[1], 1e-3, "y");
                assertEquals(target[2], sole[2], 1e-3, "z");
                // The knee bends forward, never backwards.
                assertTrue(2 * Math.atan2(lower.x, lower.w) >= -1e-4);
            }
        }
        // Out of reach, the leg stays straight and points at it.
        LegIk.solve(KNEE, FOOT, 1, 0, 0, 20, 0, upper, lower);
        assertEquals(0, lower.x, 1e-6);
    }

    @Test
    void turnsBySteppingWithoutSlidingItsFeet() throws Exception
    {
        Golem golem = new Golem();
        final int turnAt = 60;
        // Vanilla turns the body 60° to the right (a higher yaw) over 10 ticks, the head first.
        golem.run(260, tick -> {
            float body = tick < turnAt ? 0 : Math.min(60, (tick - turnAt) * 6);
            golem.inputs.bodyYaw = body;
            golem.inputs.headYaw = (tick < turnAt - 10 ? 0 : 60) - body;
        });

        assertTrue(golem.firstLifted >= 0, "no foot lifted");
        // Turning right, the leg on the right (-X) side leads.
        assertEquals(0, golem.firstLifted, "the leg on the side of the turn steps first");
        assertTrue(Math.abs(golem.shownAtFirstLift) < 1, "the body turned (" + golem.shownAtFirstLift + "°) before a foot stepped");
        assertTrue(golem.maxSlide < 0.02, "a planted foot slid " + golem.maxSlide + " blocks");
        assertTrue(golem.shownAtSecondLift > 10, "the body turned only " + golem.shownAtSecondLift + "° before the other foot stepped");
        assertTrue(golem.steps >= 2, "only " + golem.steps + " steps");
        assertEquals(60, golem.shownYaw, 1, "the turn didn't end squared up");
        for (int i = 0; i < 2; i++)
        {
            assertEquals(GROUND, golem.soleY[i], 0.1, "foot " + i + " isn't on the ground");
        }
    }

    @Test
    void walksOnItsFeet() throws Exception
    {
        // A wandering golem (about 0.05 blocks a tick) and a chasing one, 160 ticks each.
        double[] speeds = {0.05, 0.12};
        int[] steps = new int[speeds.length];
        for (int s = 0; s < speeds.length; s++)
        {
            double speed = speeds[s];
            Golem golem = new Golem();
            golem.run(260, tick -> golem.inputs.forwardSpeed = tick >= 40 && tick < 200 ? speed : 0);
            steps[s] = golem.steps;

            String at = " at " + speed + " blocks a tick";
            assertTrue(golem.maxSlide < 0.02, "a planted foot slid " + golem.maxSlide + " blocks" + at);
            assertTrue(golem.alternated, "the feet didn't take turns" + at);
            // Vanilla's rocking is turned back; what is left is the lean over the planted foot.
            assertTrue(golem.maxTilt < WEIGHT_ROLL + 0.1, "the golem tilted " + golem.maxTilt + "°" + at);
            assertTrue(golem.maxTilt > WEIGHT_ROLL * 0.5, "the golem didn't lean over its feet (" + golem.maxTilt + "°)" + at);
            // Having stopped, it stands with its feet together, on the ground.
            for (int i = 0; i < 2; i++)
            {
                assertEquals(GROUND, golem.soleY[i], 0.1, "foot " + i + " isn't on the ground" + at);
                assertTrue(golem.offRest[i] < 0.6, "foot " + i + " stopped " + golem.offRest[i] + " units off its place" + at);
            }
        }
        // A foot goes as far a step as the body over two steps: wandering, at most 14 model units
        // (the animator's strideLength); charging, it runs, up to 24 (runStrideLength).
        double wandering = 2 * 160 * speeds[0] * 16 / steps[0];
        double running = 2 * 160 * speeds[1] * 16 / steps[1];
        assertTrue(wandering <= 14, "wandering, a foot went " + wandering + " units a step");
        assertTrue(running >= 20 && running <= 24.5, "running, a foot went " + running + " units a step");
    }

    @Test
    void runsAZigzaggingPathSteadily() throws Exception
    {
        // A diagonal path at a run: the heading swaps between 0° and 45° every few ticks as the
        // golem passes the path's nodes. Fast, well past what its legs keep up with.
        Golem golem = new Golem();
        double[] range = {Double.MAX_VALUE, -Double.MAX_VALUE};
        golem.run(220, tick -> {
            golem.inputs.forwardSpeed = tick >= 40 ? 0.22 : 0;
            golem.inputs.bodyYaw = tick >= 40 && (tick / 4) % 2 == 1 ? 45 : 0;
            if (tick >= 120)
            {
                range[0] = Math.min(range[0], golem.shownYaw);
                range[1] = Math.max(range[1], golem.shownYaw);
            }
        });
        assertTrue(range[1] - range[0] < 15, "the body swung " + (range[1] - range[0]) + "° with the zigzag");
        // At least a share of the shortest step (4 ticks) each, not a flurry.
        assertTrue(golem.steps < 180 / 2.4, golem.steps + " steps in 180 ticks");
        assertTrue(golem.maxHipJump < 0.5, "the hips jumped " + golem.maxHipJump + " units in a frame");
        assertTrue(golem.maxYawRate < 3, "the body turned " + golem.maxYawRate + "° in a frame");
    }

    @Test
    void runsTheWayItGoes() throws Exception
    {
        // Charging sideways: vanilla's body faces +Z (yaw 0) while the golem goes +X (yaw -90).
        Golem golem = new Golem();
        golem.run(160, tick -> golem.inputs.strafeSpeed = tick >= 40 ? 0.12 : 0);
        assertEquals(-90, golem.shownYaw, 3, "running, the body doesn't face the way it goes");
        assertTrue(golem.maxSlide < 0.02, "a planted foot slid " + golem.maxSlide + " blocks");

        // Wandering sideways, it keeps facing vanilla's way.
        Golem wanderer = new Golem();
        wanderer.run(160, tick -> wanderer.inputs.strafeSpeed = tick >= 40 ? 0.05 : 0);
        assertEquals(0, wanderer.shownYaw, 3, "wandering, the body turned away from vanilla's yaw");
    }

    @Test
    void swingsRoundFasterWhenCharging() throws Exception
    {
        // Vanilla turns the body 90° at once while the golem wanders, and while it charges.
        double[] speeds = {0.05, 0.12};
        int[] ticksToTurn = new int[speeds.length];
        for (int s = 0; s < speeds.length; s++)
        {
            double speed = speeds[s];
            Golem golem = new Golem();
            int[] turned = {-1};
            golem.run(200, tick -> {
                golem.inputs.forwardSpeed = tick >= 40 ? speed : 0;
                golem.inputs.bodyYaw = tick < 100 ? 0 : 90;
                if (tick >= 100 && turned[0] < 0 && Math.abs(golem.shownYaw - 90) < 10)
                {
                    turned[0] = tick - 100;
                }
            });
            assertTrue(golem.maxSlide < 0.02, "a planted foot slid " + golem.maxSlide + " blocks at " + speed + " blocks a tick");
            ticksToTurn[s] = turned[0];
        }
        assertTrue(ticksToTurn[0] > 0 && ticksToTurn[1] > 0, "the golem didn't turn");
        assertTrue(ticksToTurn[1] <= 12, "charging, it took " + ticksToTurn[1] + " ticks to turn round");
        assertTrue(ticksToTurn[1] * 3 <= ticksToTurn[0], "charging, it turned in " + ticksToTurn[1] + " ticks against " + ticksToTurn[0] + " wandering");
    }

    @Test
    void walksAroundABendWithoutSliding() throws Exception
    {
        Golem golem = new Golem();
        golem.run(240, tick -> {
            golem.inputs.forwardSpeed = tick >= 40 ? 0.06 : 0;
            golem.inputs.bodyYaw = tick < 80 ? 0 : Math.min(90, (tick - 80) * 1.5F);
        });
        assertTrue(golem.maxSlide < 0.02, "a planted foot slid " + golem.maxSlide + " blocks");
        // Walking, the body keeps up with vanilla's yaw by its steps.
        assertEquals(90, golem.shownYaw, 10);
    }

    private static double[] sole(Quaternion upper, Quaternion lower)
    {
        double[] foot = rotate(lower, FOOT[0], FOOT[1], FOOT[2]);
        return rotate(upper, KNEE[0] + foot[0], KNEE[1] + foot[1], KNEE[2] + foot[2]);
    }

    private static double[] rotate(Quaternion q, double x, double y, double z)
    {
        // v + 2w(u × v) + 2u × (u × v)
        double cx = q.y * z - q.z * y, cy = q.z * x - q.x * z, cz = q.x * y - q.y * x;
        double ccx = q.y * cz - q.z * cy, ccy = q.z * cx - q.x * cz, ccz = q.x * cy - q.y * cx;
        return new double[] {x + 2 * (q.w * cx + ccx), y + 2 * (q.w * cy + ccy), z + 2 * (q.w * cz + ccz)};
    }

    /** A golem standing on flat ground, animated at 60 fps, its soles followed in the world. */
    private static class Golem
    {
        final EntityInputs inputs = new EntityInputs();
        final EntityIronGolem entity;
        final ScriptedEntity scripted;
        final DefinedEntityData<EntityIronGolem> data;
        final KumoAnimatorState animator;

        double shownYaw;
        final double[] soleY = new double[2];
        /** How far each sole is from under its hip, in model units, in the shown frame. */
        final double[] offRest = new double[2];
        double maxTilt;
        boolean alternated = true;
        int lastLifted = -1;
        int firstLifted = -1;
        double shownAtFirstLift;
        double shownAtSecondLift = Double.NaN;
        double maxSlide;
        int steps;
        /** Per frame, the most the shown body turned (degrees) and the hips moved (model units). */
        double maxYawRate, maxHipJump;
        double lastShown = Double.NaN, lastHipY;

        Golem() throws Exception
        {
            EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/iron_golem.json"));
            LabBootstrap.ensure();
            net.minecraft.entity.Entity.resetIds();
            World world = new World();
            Minecraft.getMinecraft().world = world;
            Minecraft.getMinecraft().player = new EntityPlayerSP(world);
            entity = new EntityIronGolem(world);
            scripted = new ScriptedEntity(entity, world);
            data = DefinedEntityData.create(definition, entity);
            animator = new KumoAnimatorState(definition.entityScope(entity.getClass()), KumoSession.loadAnimator(definition.animator), true,
                    java.util.Collections.emptyList(), java.util.Collections.emptyList(), KumoSession.INSTANCING);
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
                // then vanilla's own: RenderIronGolem rocks it about Z while it walks, and the model
                // is drawn flipped in Y and Z, the entity's origin ORIGIN units down it.
                Quaternion render = data.renderRotation.getSmooth();
                Quaternion rock = new Quaternion();
                float rockAngle = entity.limbSwingAmount >= 0.01 ? 6.5F * (Math.abs((data.limbSwing + 6) % 13 - 6.5F) - 3.25F) / 3.25F : 0F;
                rock.setFromAxisAngle(0, 0, 1, (float) Math.toRadians(-rockAngle));
                double[] forward = rotate(render, 0, 0, 1);
                double bodyYaw = data.getVariable("entityBodyYaw");
                shownYaw = bodyYaw - Math.toDegrees(Math.atan2(forward[0], forward[2]));
                // Once running: how far the body turns and the hips move in one frame.
                if (!Double.isNaN(lastShown) && clock.getTick() >= 60)
                {
                    maxYawRate = Math.max(maxYawRate, Math.abs(MathHelper.wrapDegrees(shownYaw - lastShown)));
                    maxHipJump = Math.max(maxHipJump, Math.abs(data.localOffset.getY() - lastHipY));
                }
                lastShown = shownYaw;
                lastHipY = data.localOffset.getY();
                Quaternion drawn = Quaternion.mul(render, rock, new Quaternion());
                double[] upright = rotate(drawn, 0, 1, 0);
                maxTilt = Math.max(maxTilt, Math.toDegrees(Math.acos(Math.min(1, upright[1]))));
                double yaw = Math.toRadians(bodyYaw), cos = Math.cos(yaw), sin = Math.sin(yaw);
                double shown = Math.toRadians(shownYaw), cosS = Math.cos(shown), sinS = Math.sin(shown);
                double[] offset = {data.localOffset.getX(), data.localOffset.getY(), data.localOffset.getZ()};
                double px = data.getVariable("entityWorldX"), pz = data.getVariable("entityWorldZ");

                for (int i = 0; i < 2; i++)
                {
                    double[] sole = sole(data.getPart(UPPER[i]).rotation.getSmooth(), data.getPart(LOWER[i]).rotation.getSmooth());
                    double mx = HIP[i][0] + sole[0], my = HIP[i][1] + sole[1], mz = HIP[i][2] + sole[2];
                    double[] rocked = rotate(rock, mx, ORIGIN - my, -mz);
                    double[] o = rotate(render, rocked[0] + offset[0], rocked[1] + offset[1], rocked[2] + offset[2]);
                    double wx = px + (o[0] * cos - o[2] * sin) / 16, wz = pz + (o[0] * sin + o[2] * cos) / 16;
                    my = ORIGIN - o[1];
                    soleY[i] = my;
                    // Under the hip in the shown frame: back from the world, turned by the shown yaw.
                    double dx = (wx - px) * 16, dz = (wz - pz) * 16;
                    double sx = dx * cosS + dz * sinS, sz = dx * sinS - dz * cosS;
                    offRest[i] = Math.hypot(sx - (HIP[i][0] + KNEE[0] + FOOT[0]), sz - (HIP[i][2] + KNEE[2] + FOOT[2]));
                    boolean up = my < GROUND - 0.3;
                    if (up && !wasUp[i])
                    {
                        if (i == lastLifted && inputs.forwardSpeed != 0)
                        {
                            alternated = false;
                        }
                        lastLifted = i;
                        steps++;
                        if (steps == 2)
                        {
                            shownAtSecondLift = shownYaw;
                        }
                        if (firstLifted < 0)
                        {
                            firstLifted = i;
                            shownAtFirstLift = shownYaw;
                        }
                    }
                    wasUp[i] = up;
                    // Once the driver has faded in.
                    boolean down = my > GROUND - 0.05 && clock.getTick() >= 20;
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
                        maxSlide = Math.max(maxSlide, Math.hypot(wx - planted[i][0], wz - planted[i][1]));                    }
                }
            }
        }
    }

}
