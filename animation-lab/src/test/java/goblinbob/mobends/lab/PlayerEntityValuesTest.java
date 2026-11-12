package goblinbob.mobends.lab;

import goblinbob.mobends.core.client.event.DataUpdateHandler;
import goblinbob.mobends.core.data.OrientationComponent;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.definition.EntityModelDefinition;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.lab.sim.EntityInputs;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.LabClock;
import goblinbob.mobends.lab.sim.ScriptedEntity;
import goblinbob.mobends.lab.sim.VanillaModelInputs;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The values the player's animator reads of the entity, which its model definition declares
 * (entity.sprintJumpLeg, entity.flightSpeedFactor, entity.flightPitch), are what the player's Java
 * data class computed. The goldens don't cover the sprint-jump leg or the flight pitch.
 */
public class PlayerEntityValuesTest
{

    /**
     * The definition's data, computing the values as the Java data class did, but for the leg: it is
     * picked on every liftoff, and when rising after falling: swapped when jumping again within 5
     * ticks of landing, else the one the sprint cycle has in the air, leading with the right leg
     * while the left one is on the ground (sweeping back).
     */
    static class Reference extends DefinedEntityData<AbstractClientPlayer>
    {
        boolean leg;
        boolean switched;
        /** A liftoff since the last frame: the definition sees it, and reads the limb swing, on the frame. */
        boolean liftedOff;

        Reference(EntityModelDefinition definition, AbstractClientPlayer player)
        {
            super(definition, player);
        }

        @Override
        public void update(float partialTicks)
        {
            super.update(partialTicks);
            if (liftedOff && !switched)
            {
                leg = nextLeg();
            }
            if (liftedOff)
            {
                switched = true;
                liftedOff = false;
            }
            if (motionY < 0)
            {
                switched = false;
            }
            if (!switched && motionY > 0)
            {
                leg = nextLeg();
                switched = true;
            }
        }

        @Override
        public void onLiftoff()
        {
            super.onLiftoff();
            liftedOff = true;
        }

        boolean nextLeg()
        {
            return getTicksAfterTouchdown() < 5 ? !leg : standingOnLeftLeg();
        }

        /** The sprint cycle swings the left leg by -72 cos(phase), phase = limbSwing * 0.6662 * 0.8: it sweeps back while sin(phase) > 0. */
        boolean standingOnLeftLeg()
        {
            return Math.sin(limbSwing * 0.53296) > 0;
        }

        double flightSpeedFactor()
        {
            return Math.min(Math.max(getInterpolatedMotionMagnitude(), 0), 0.2) / 0.2;
        }

        /** As the Java data class did, but of the interpolated vertical motion, as entityMotionY now is. */
        double flightPitch()
        {
            return MathHelper.atan2(getInterpolatedXZMotionMagnitude(), getInterpolatedMotionY()) * 180.0D / Math.PI * flightSpeedFactor();
        }
    }

    /** Turns the held items by the values, so the test can read them: right by the pitch, left by 90 on one leg. */
    private static final String PROBE = "{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"probe\", \"nodes\": {\"probe\": {\"core:pose\": {\"pose\": ["
            + "{\"core:rotate\": {\"bone\": \"rightHeldItem\", \"axis\": \"x\", \"angle\": \"entity.flightPitch\"}, \"@space\": \"override\"},"
            + "{\"core:rotate\": {\"bone\": \"leftHeldItem\", \"axis\": \"x\", \"angle\": {\"if\": [\"entity.sprintJumpLeg\", 90, 0]}}, \"@space\": \"override\"},"
            + "{\"core:rotate\": {\"bone\": \"head\", \"axis\": \"x\", \"angle\": {\"mul\": [\"entity.flightSpeedFactor\", 100]}}, \"@space\": \"override\"}"
            + "]}}}}]}";

    private static void assertTurnedBy(double degrees, Quaternion q, String what)
    {
        Quaternion expected = new Quaternion();
        expected.setFromAxisAngle(1, 0, 0, (float) Math.toRadians(degrees));
        double dot = Math.abs(expected.x * q.x + expected.y * q.y + expected.z * q.z + expected.w * q.w);
        assertEquals(1, dot, 1e-5, what + ": expected " + degrees + " degrees, got " + q);
    }

    @Test
    void theDefinitionComputesWhatTheJavaDataClassDid() throws Exception
    {
        LabBootstrap.ensure();
        net.minecraft.entity.Entity.resetIds();
        World world = new World();
        Minecraft.getMinecraft().theWorld = world;
        Minecraft.getMinecraft().thePlayer = new EntityPlayerSP(world);
        AbstractClientPlayer player = new AbstractClientPlayer(world);
        ScriptedEntity scripted = new ScriptedEntity(player, world);
        Reference data = new Reference(EntityKind.PLAYER.loadDefinition(), player);
        data.initialize();
        AnimatorTemplate probe = KumoSerializer.INSTANCE.gson.fromJson(PROBE, AnimatorTemplate.class);
        KumoAnimatorState animator = new KumoAnimatorState(data.getEntityScope(), probe, true,
                Collections.emptyList(), Collections.emptyList(), KumoSession.INSTANCING);

        LabClock clock = new LabClock(30);
        EntityInputs inputs = new EntityInputs();
        VanillaModelInputs modelInputs = new VanillaModelInputs();
        int legChanges = 0;
        int hopLegChanges = 0;
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
                    // an uneven rhythm, so the jumps land on both legs, then jumping again on landing
                    inputs.jump = tick < 100 ? tick % 23 == 5 + (tick / 23) % 4 * 3 : true;
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
            // the limb swing the definition reads when the animator runs
            modelInputs.compute(player, clock.getPartialTicks());
            data.limbSwing = modelInputs.limbSwing;
            data.update(clock.getPartialTicks());
            animator.update(data, DataUpdateHandler.ticksPerFrame);

            assertTurnedBy(data.flightPitch(), ((OrientationComponent) data.getPartForName("rightHeldItem")).getEnd(), "frame " + frame + ": flightPitch");
            assertTurnedBy(data.leg ? 90 : 0, ((OrientationComponent) data.getPartForName("leftHeldItem")).getEnd(), "frame " + frame + ": sprintJumpLeg");
            assertTurnedBy(data.flightSpeedFactor() * 100, data.getPart("head").rotation.getEnd(), "frame " + frame + ": flightSpeedFactor");
            if (data.leg != lastLeg)
            {
                legChanges++;
                if (clock.getTick() > 100 && clock.getTick() < 160) hopLegChanges++;
                lastLeg = data.leg;
            }
            maxPitch = Math.max(maxPitch, Math.abs(data.flightPitch()));
        }
        assertTrue(legChanges >= 4, "the run switched legs only " + legChanges + " times");
        assertTrue(hopLegChanges >= 3, "jumping again on landing switched legs only " + hopLegChanges + " times");
        assertTrue(maxPitch > 30, "the run never pitched in flight (max " + maxPitch + ")");
    }

}
