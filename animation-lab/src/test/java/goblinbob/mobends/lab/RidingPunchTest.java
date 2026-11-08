package goblinbob.mobends.lab;

import goblinbob.mobends.lab.PlayerRuns.Run;
import goblinbob.mobends.lab.sim.Scenario;
import org.junit.jupiter.api.Test;

import static goblinbob.mobends.lab.PlayerRuns.angleBetween;
import static goblinbob.mobends.lab.PlayerRuns.run;
import static org.junit.jupiter.api.Assertions.*;

/** Punching while riding (#238). */
class RidingPunchTest
{

    // --- #238: punching from the saddle leaves the legs and the root as riding has them ---------------------

    @Test
    void punchingWhileRidingLeavesTheLegsSeated() throws Exception
    {
        Scenario.InputScript riding = (tick, in) -> {
            in.noGravity = true;
            in.riding = true;
        };
        Run still = run(80, riding);
        Run punching = run(80, (tick, in) -> {
            riding.fill(tick, in);
            if (tick == 20 || tick == 32) in.attack = true;
        });
        assertTrue(punching.actionNodes.contains("punch_right") && punching.actionNodes.contains("fist_guard"));
        double worstLeg = 0, worstRoot = 0;
        String worst = "";
        for (int f = 0; f < still.frames.size(); f++)
        {
            for (String bone : new String[] {"rightLeg", "leftLeg", "rightForeLeg", "leftForeLeg", "root"})
            {
                double angle = angleBetween(still.frames.get(f).bones.get(bone).r, punching.frames.get(f).bones.get(bone).r);
                if (angle > worstLeg)
                {
                    worstLeg = angle;
                    worst = "frame " + f + " " + bone;
                }
            }
            worstRoot = Math.max(worstRoot, Math.abs(still.frames.get(f).vectors.get("root").v[1]
                    - punching.frames.get(f).vectors.get("root").v[1]));
        }
        assertTrue(worstLeg < 0.5, "the punches turned the legs by " + worstLeg + " degrees (" + worst + ")");
        assertTrue(worstRoot < 0.01, "the punches lowered the rider by " + worstRoot);
    }
}
