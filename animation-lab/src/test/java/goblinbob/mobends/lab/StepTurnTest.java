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
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.function.IntConsumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@code core:step_turn} on the iron golem, checked by forward kinematics: the soles are put back
 * together from the bones' smoothed rotations and the model's shown yaw, as the renderer draws them,
 * and followed in the world.
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
    void handsTheBodyBackWhenItWalks() throws Exception
    {
        Golem golem = new Golem();
        golem.run(200, tick -> {
            golem.inputs.bodyYaw = tick < 30 ? 0 : 40;
            golem.inputs.forwardSpeed = tick >= 120 ? 0.15 : 0;
        });
        // Walking, the model shows vanilla's yaw again.
        assertEquals(40, golem.shownYaw, 0.5);
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
        final EntityZombie entity;
        final ScriptedEntity scripted;
        final DefinedEntityData<EntityZombie> data;
        final KumoAnimatorState animator;

        double shownYaw;
        final double[] soleY = new double[2];
        int firstLifted = -1;
        double shownAtFirstLift;
        double shownAtSecondLift = Double.NaN;
        double maxSlide;
        int steps;

        Golem() throws Exception
        {
            EntityModelDefinition definition = ModelDefinitions.INSTANCE.load(new ResourceLocation("mobends", "bends/models/iron_golem.json"));
            LabBootstrap.ensure();
            net.minecraft.entity.Entity.resetIds();
            World world = new World();
            Minecraft.getMinecraft().world = world;
            Minecraft.getMinecraft().player = new EntityPlayerSP(world);
            entity = new EntityZombie(world);
            scripted = new ScriptedEntity(entity, world);
            data = DefinedEntityData.create(definition, entity);
            animator = new KumoAnimatorState(KumoSession.loadAnimator(definition.animator), KumoSession.INSTANCING);
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

                // The yaw the model is drawn at: vanilla's, turned back by the render rotation.
                Quaternion turn = data.renderRotation.getSmooth();
                double bodyYaw = data.getVariable("bodyYaw");
                shownYaw = bodyYaw - Math.toDegrees(2 * Math.atan2(turn.y, turn.w));
                double yaw = Math.toRadians(shownYaw), cos = Math.cos(yaw), sin = Math.sin(yaw);
                // The local offset moves the model in the shown body's frame, where Y and Z are flipped.
                double offX = data.localOffset.getX(), offY = -data.localOffset.getY(), offZ = -data.localOffset.getZ();
                double px = data.getVariable("worldX"), pz = data.getVariable("worldZ");

                for (int i = 0; i < 2; i++)
                {
                    double[] sole = sole(data.getPart(UPPER[i]).rotation.getSmooth(), data.getPart(LOWER[i]).rotation.getSmooth());
                    double mx = HIP[i][0] + offX + sole[0], my = HIP[i][1] + offY + sole[1], mz = HIP[i][2] + offZ + sole[2];
                    double wx = px + (mx * cos + mz * sin) / 16, wz = pz + (mx * sin - mz * cos) / 16;
                    soleY[i] = my;
                    boolean up = my < GROUND - 0.3;
                    if (up && !wasUp[i])
                    {
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
                    // Once the driver has faded in, while standing.
                    boolean down = my > GROUND - 0.05 && inputs.forwardSpeed == 0 && clock.getTick() >= 20;
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
