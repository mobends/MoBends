package goblinbob.mobends.lab;

import goblinbob.mobends.lab.PlayerRuns.Run;
import goblinbob.mobends.lab.sim.Scenario;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

import static goblinbob.mobends.lab.PlayerRuns.run;
import static org.junit.jupiter.api.Assertions.*;

/** Attacking while flying with an elytra (#89). */
class ElytraSlashTest
{

    /** The combo's first four slashes: flying with an elytra, the fifth attack starts it over instead of whirling. */
    private static final String[] COMBO = {"slash_up", "slash_down", "slash_inward", "slash_outward"};

    private static Scenario.InputScript flying(boolean leftHanded, Item held)
    {
        return (tick, in) -> {
            in.noGravity = true;
            in.elytraTicks = tick;
            in.forwardSpeed = 0.4;
            in.headPitch = -20;
            in.leftHanded = leftHanded;
            in.mainHand = new ItemStack(held);
        };
    }

    // --- #89: an attack while flying with an elytra leaves the head looking ahead along the flight -----------

    /** Five attacks of the sword combo, with either hand. */
    private static void assertTheComboKeepsTheHeadAhead(boolean leftHanded) throws Exception
    {
        Scenario.InputScript flying = flying(leftHanded, Items.IRON_SWORD);
        Run still = run(120, flying);
        Run slashing = run(120, (tick, in) -> {
            flying.fill(tick, in);
            if (tick == 20 || tick == 32 || tick == 44 || tick == 56 || tick == 68) in.attack = true;
        });
        for (String slash : COMBO)
        {
            assertTrue(slashing.actionNodes.contains(slash), slash + " didn't play: " + slashing.actionNodes);
        }
        assertFalse(slashing.actionNodes.contains("slash_whirl"), "the whirl played mid flight");
        assertEquals("slash_up", lastSlash(slashing), "the fifth attack didn't start the combo over");
        double worst = still.worstAngle(slashing, "head");
        assertTrue(worst < 0.5, "the combo turned the head by " + worst + " degrees from where the elytra has it");
    }

    private static String lastSlash(Run run)
    {
        String last = null;
        for (String node : run.actionNodes)
        {
            if (node != null && node.startsWith("slash_")) last = node;
        }
        return last;
    }

    @Test
    void theSwordComboWhileFlyingWithAnElytraKeepsTheHeadAhead() throws Exception
    {
        assertTheComboKeepsTheHeadAhead(false);
    }

    @Test
    void theLeftHandedSwordComboWhileFlyingWithAnElytraKeepsTheHeadAhead() throws Exception
    {
        assertTheComboKeepsTheHeadAhead(true);
    }

    @Test
    void aToolSwingWhileFlyingWithAnElytraKeepsTheHeadAhead() throws Exception
    {
        Scenario.InputScript flying = flying(false, Items.IRON_PICKAXE);
        Run still = run(60, flying);
        Run swinging = run(60, (tick, in) -> {
            flying.fill(tick, in);
            if (tick == 30) in.attack = true;
        });
        assertTrue(swinging.actionNodes.contains("tool"), "the tool swing didn't play: " + swinging.actionNodes);
        double worst = still.worstAngle(swinging, "head");
        assertTrue(worst < 0.5, "the swing turned the head by " + worst + " degrees from where the elytra has it");
    }
}
