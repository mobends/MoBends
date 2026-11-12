package goblinbob.mobends.lab;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.kumo.InstalledMods;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.types.ExtensionDefinition;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.LabBootstrap;
import goblinbob.mobends.lab.sim.Scenario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * The Mo' Bends Compat+ resource pack (misc/compat-plus-pack): its extension loads on the player's
 * animator whether the mods it is for are installed or not, and leaves the player to Mo' Bends
 * while they play nothing. (The lab has no Player Animation Library, so
 * {@code mobends:compat_pal_animation_playing} never holds here: going vanilla while it does is
 * tried in the game, with {@code ./gradlew :26.3-fabric:runClient -PbetterCombat}.)
 */
public class CompatPlusPackTest
{
    private static final Path PACK = LabPaths.root().resolve("../misc/compat-plus-pack/assets/mobends_compat_plus/bends");

    @AfterEach
    void noModsAgain()
    {
        // As LabBootstrap has it: no other mods are installed in the lab.
        InstalledMods.set(modId -> null);
    }

    @Test
    void theExtensionTargetsThePlayer() throws Exception
    {
        ExtensionDefinition extension = ExtensionDefinition.parse(JsonParser.parseString(
                new String(Files.readAllBytes(PACK.resolve("extensions/player.json")), StandardCharsets.UTF_8)));
        assertEquals("mobends:player", extension.type);
        assertEquals("mobends_compat_plus:bends/animators/player.json", extension.animator);
    }

    @Test
    void withoutBetterCombatThePlayerIsMoBends() throws Exception
    {
        LabBootstrap.ensure();
        InstalledMods.set(modId -> null);
        staysAnimatedWhileAttacking();
    }

    @Test
    void withBetterCombatThePlayerIsMoBendsWhilePalPlaysNothing() throws Exception
    {
        LabBootstrap.ensure();
        InstalledMods.set(modId -> "bettercombat".equals(modId) ? "3.2.2+26.3-fabric" : null);
        staysAnimatedWhileAttacking();
    }

    private static void staysAnimatedWhileAttacking() throws Exception
    {
        AnimatorTemplate compat;
        try (Reader reader = Files.newBufferedReader(PACK.resolve("animators/player.json")))
        {
            compat = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }
        Scenario scenario = new Scenario(EntityKind.PLAYER, "compat_plus_attack", Scenarios.FPS, 40, (tick, in) -> {
            Scripts.walk(in, Scripts.WALK_SPEED);
            in.attack = tick == 10 || tick == 20;
        });
        KumoSession session = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)), Collections.singletonList(compat));
        for (int frame = 0; frame < scenario.frameCount(); frame++)
        {
            session.step();
            assertFalse(session.animator.wantsVanilla(), "animated on frame " + frame);
        }
    }
}
