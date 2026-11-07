package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Slashing while flying with an elytra (#89). */
class ElytraSlashTest
{

    private static final int FPS = 20;
    private static final int ACTION_LAYER = 4;

    /** Each frame's poses and the action layer's node. */
    private static final class Run
    {
        final List<FramePose> frames = new ArrayList<>();
        final List<String> actionNodes = new ArrayList<>();
    }

    private static Run run(int ticks, Scenario.InputScript script) throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.PLAYER, "issue", FPS, ticks, script);
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)));
        Run run = new Run();
        for (int f = 0; f < scenario.frameCount(); f++)
        {
            run.frames.add(session.step());
            run.actionNodes.add(session.animator.getCurrentNodes().get(ACTION_LAYER));
        }
        return run;
    }

    private static double angleBetween(float[] a, float[] b)
    {
        double dot = Math.abs(a[0] * b[0] + a[1] * b[1] + a[2] * b[2] + a[3] * b[3]);
        return Math.toDegrees(2 * Math.acos(Math.min(1, dot)));
    }

    // --- #89: a slash while flying with an elytra leaves the head looking ahead along the flight --------------

    @Test
    void aSlashWhileFlyingWithAnElytraKeepsTheHeadAhead() throws Exception
    {
        Scenario.InputScript flying = (tick, in) -> {
            in.noGravity = true;
            in.elytraTicks = tick;
            in.forwardSpeed = 0.4;
            in.headPitch = -20;
            in.mainHand = new ItemStack(Items.IRON_SWORD);
        };
        Run still = run(60, flying);
        Run slashing = run(60, (tick, in) -> {
            flying.fill(tick, in);
            if (tick == 30) in.attack = true;
        });
        assertTrue(slashing.actionNodes.contains("slash_up"));
        double worst = 0;
        for (int f = 0; f < still.frames.size(); f++)
        {
            worst = Math.max(worst, angleBetween(still.frames.get(f).bones.get("head").r, slashing.frames.get(f).bones.get("head").r));
        }
        assertTrue(worst < 0.5, "the slash turned the head by " + worst + " degrees from where the elytra has it");
    }
}
