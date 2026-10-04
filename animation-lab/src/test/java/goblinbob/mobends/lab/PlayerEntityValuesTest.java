package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.standard.data.PlayerData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The values the player's animator reads of the entity, which its model definition now declares
 * (entity.sprintJumpLeg, entity.flightSpeedFactor, entity.flightPitch), are what PlayerData
 * computed in Java. The goldens don't cover the sprint-jump leg or the flight pitch.
 */
public class PlayerEntityValuesTest
{

    /** PlayerData as it was: the leg switches on every liftoff, and when rising after falling. */
    static class Reference extends PlayerData
    {
        boolean leg;
        boolean switched;

        Reference(AbstractClientPlayer player)
        {
            super(player);
        }

        @Override
        public void update(float partialTicks)
        {
            super.update(partialTicks);
            if (motionY < 0)
            {
                switched = false;
            }
            if (!switched && motionY > 0)
            {
                leg = !leg;
                switched = true;
            }
        }

        @Override
        public void onLiftoff()
        {
            super.onLiftoff();
            if (!switched)
            {
                leg = !leg;
                switched = true;
            }
        }

        double flightSpeedFactor()
        {
            return Math.min(Math.max(getInterpolatedMotionMagnitude(), 0), 0.2) / 0.2;
        }

        double flightPitch()
        {
            return MathHelper.atan2(getInterpolatedXZMotionMagnitude(), getMotionY()) * 180.0D / Math.PI * flightSpeedFactor();
        }
    }

    /** Turns the held items by the values, so the test can read them: right by the pitch, left by 90 on one leg. */
    private static final String PROBE = "{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"probe\", \"nodes\": {\"probe\": {\"core:pose\": {\"pose\": ["
            + "{\"core:axis_rotate\": {\"bone\": \"rightHeldItem\", \"axis\": \"x\", \"angle\": \"entity.flightPitch\"}, \"@space\": \"override\"},"
            + "{\"core:axis_rotate\": {\"bone\": \"leftHeldItem\", \"axis\": \"x\", \"angle\": {\"if\": [\"entity.sprintJumpLeg\", 90, 0]}}, \"@space\": \"override\"},"
            + "{\"core:axis_rotate\": {\"bone\": \"head\", \"axis\": \"x\", \"angle\": {\"mul\": [\"entity.flightSpeedFactor\", 100]}}, \"@space\": \"override\"}"
            + "]}}}}]}";

    private static void assertTurnedBy(double degrees, Quaternion q, String what)
    {
        Quaternion expected = new Quaternion();
        expected.setFromAxisAngle(1, 0, 0, (float) Math.toRadians(degrees));
        double dot = Math.abs(expected.x * q.x + expected.y * q.y + expected.z * q.z + expected.w * q.w);
        assertEquals(1, dot, 1e-5, what + ": expected " + degrees + " degrees, got " + q);
    }

    @Test
    void theDefinitionComputesWhatPlayerDataDid() throws Exception
    {
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().world = world;
        Minecraft.getMinecraft().player = new EntityPlayerSP(world);
        AbstractClientPlayer player = new AbstractClientPlayer(world);
        ScriptedEntity scripted = new ScriptedEntity(player, world);
        Reference data = new Reference(player);
        data.initialize();
        AnimatorTemplate probe = KumoSerializer.INSTANCE.gson.fromJson(PROBE, AnimatorTemplate.class);
        KumoAnimatorState animator = new KumoAnimatorState(data.getEntityScope(), probe, true,
                Collections.emptyList(), Collections.emptyList(), KumoSession.INSTANCING);

        LabClock clock = new LabClock(30);
        EntityInputs inputs = new EntityInputs();
        int legChanges = 0;
        boolean lastLeg = data.leg;
        double maxPitch = 0;
        // Sprint-jumps for 160 ticks, then flies: forward, climbing, sinking.
        for (int frame = 0; frame < 450; frame++)
        {
            int ticksStarted = clock.nextFrame();
            for (int t = 0; t < ticksStarted; t++)
            {
                int tick = clock.getTick() - ticksStarted + t + 1;
                inputs.reset();
                if (tick < 160)
                {
                    inputs.forwardSpeed = 0.28;
                    inputs.sprinting = true;
                    inputs.jump = tick % 23 == 5;
                }
                else
                {
                    inputs.flying = true;
                    inputs.forwardSpeed = tick < 220 ? 0.25 : 0.1;
                    inputs.verticalSpeed = tick < 200 ? 0.0 : tick < 250 ? 0.3 : -0.2;
                }
                scripted.tick(inputs);
                data.updateClient();
            }
            data.update(clock.getPartialTicks());
            animator.update(data, DataUpdateHandler.ticksPerFrame);

            assertTurnedBy(data.flightPitch(), data.rightHeldItem.getEnd(), "frame " + frame + ": flightPitch");
            assertTurnedBy(data.leg ? 90 : 0, data.leftHeldItem.getEnd(), "frame " + frame + ": sprintJumpLeg");
            assertTurnedBy(data.flightSpeedFactor() * 100, data.head.rotation.getEnd(), "frame " + frame + ": flightSpeedFactor");
            if (data.leg != lastLeg)
            {
                legChanges++;
                lastLeg = data.leg;
            }
            maxPitch = Math.max(maxPitch, Math.abs(data.flightPitch()));
        }
        assertTrue(legChanges >= 4, "the run switched legs only " + legChanges + " times");
        assertTrue(maxPitch > 30, "the run never pitched in flight (max " + maxPitch + ")");
    }

}
