package goblinbob.mobends.lab;

import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.LabBootstrap;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The scenarios recorded on 1.12.2 that 1.8.9 can't play, which the parity tests leave out: only these, and for
 * these reasons (a scenario that starts needing what 1.8.9 lacks shows here, instead of silently dropping out).
 */
class LaterVersionsOnlyTest
{
    @Test
    void onlyTheseScenariosNeedWhat189Lacks()
    {
        LabBootstrap.ensure();
        Map<String, String> expected = new LinkedHashMap<>();
        expected.put("skeleton/bow_and_sword", "items used by other entities than players");
        expected.put("skeleton/bow_and_sword_left_handed", "left-handed entities");
        expected.put("pig_zombie/walk_attack_left_handed", "left-handed entities");
        expected.put("player/sword_combo_left_handed", "left-handed entities");
        expected.put("player/offhand_use", "the off hand");
        expected.put("player/sleep_sit_elytra", "the elytra");
        expected.put("player/left_handed", "left-handed entities");
        expected.put("player/right_handed_torch_offhand", "the off hand");
        assertEquals(expected, Scenarios.laterVersionsOnly());
    }
}
