package goblinbob.mobends.lab.scenarios;

import goblinbob.mobends.lab.sim.Determinism;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.Scenario;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static goblinbob.mobends.lab.scenarios.Scripts.*;

/**
 * The scenario catalogue. Every scenario here has a committed golden trace under
 * {@code animation-lab/golden/<entity>/<name>.json}.
 */
public class Scenarios
{
    public static final int FPS = 30;

    private static final List<Scenario> ALL = new ArrayList<>();

    static
    {
        // --- Biped locomotion, shared by zombie / skeleton / pig zombie ------------------------
        for (EntityKind kind : new EntityKind[] { EntityKind.ZOMBIE, EntityKind.SKELETON, EntityKind.PIG_ZOMBIE })
        {
            add(new Scenario(kind, "stand_walk_stop", FPS, 150, (tick, in) -> {
                if (between(tick, 30, 100)) walk(in, WALK_SPEED);
            }));

            add(new Scenario(kind, "jump_land_walk_jump", FPS, 150, (tick, in) -> {
                if (tick == 20) in.jump = true;
                if (between(tick, 70, 150)) walk(in, WALK_SPEED);
                if (tick == 100) in.jump = true;
            }));

            add(new Scenario(kind, "head_look", FPS, 120, (tick, in) -> {
                lookAround(in, tick);
                if (between(tick, 60, 120)) walk(in, WALK_SPEED);
            }));
        }

        // The scenarios above get an animation-set-1 (stumbling) zombie from its entity id; this one
        // forces set 0 (leaning) so both zombie variants are covered.
        add(new Scenario(EntityKind.ZOMBIE, "lean_walk_jump", FPS, 150, (tick, in) -> {
            if (between(tick, 20, 90)) walk(in, WALK_SPEED);
            if (tick == 60) in.jump = true;
            lookAround(in, tick);
        }, data -> Determinism.setField(data, "animationSet", 0)));

        add(new Scenario(EntityKind.SKELETON, "strafe", FPS, 140, (tick, in) -> {
            if (between(tick, 20, 60)) in.strafeSpeed = WALK_SPEED;
            if (between(tick, 60, 100)) { in.strafeSpeed = WALK_SPEED * 0.7; walk(in, WALK_SPEED * 0.7); }
            if (between(tick, 100, 140)) walk(in, WALK_SPEED);
        }));

        add(new Scenario(EntityKind.PIG_ZOMBIE, "walk_attack", FPS, 120, (tick, in) -> {
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            if (between(tick, 10, 120)) walk(in, WALK_SPEED);
            if (tick == 40 || tick == 70) in.attack = true;
        }));

        // --- Player ------------------------------------------------------------------------------
        add(new Scenario(EntityKind.PLAYER, "stand_walk_sprint_sneak", FPS, 200, (tick, in) -> {
            if (between(tick, 20, 70)) walk(in, WALK_SPEED);
            if (between(tick, 70, 120)) { walk(in, SPRINT_SPEED); in.sprinting = true; }
            if (between(tick, 130, 180)) { walk(in, SNEAK_SPEED); in.sneaking = true; }
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.PLAYER, "sword_combo", FPS, 160, (tick, in) -> {
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            if (tick == 20 || tick == 32 || tick == 44 || tick == 56 || tick == 68) in.attack = true;
            if (between(tick, 100, 160)) walk(in, WALK_SPEED);
            if (tick == 120) in.attack = true;
        }));

        add(new Scenario(EntityKind.PLAYER, "jump_fall_torch", FPS, 200, (tick, in) -> {
            in.mainHand = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH));
            if (tick == 20) in.jump = true;
            // A long fall: no floor contact for a while.
            if (between(tick, 60, 130)) { in.noGravity = true; in.verticalSpeed = -0.5D; }
            if (between(tick, 150, 200)) walk(in, WALK_SPEED);
        }));

        add(new Scenario(EntityKind.PLAYER, "bow_and_eat", FPS, 160, (tick, in) -> {
            if (between(tick, 10, 70))
            {
                in.mainHand = new ItemStack(Items.BOW);
                in.itemUseCount = tick - 10;
                in.itemUseMaxCount = 72000;
            }
            if (between(tick, 90, 150))
            {
                in.mainHand = new ItemStack(Items.APPLE);
                in.itemUseCount = tick - 90;
                in.itemUseMaxCount = 32;
            }
        }));

        add(new Scenario(EntityKind.PLAYER, "fly_and_swim", FPS, 200, (tick, in) -> {
            if (between(tick, 0, 100))
            {
                in.flying = true;
                in.noGravity = true;
                if (between(tick, 30, 60)) { walk(in, SPRINT_SPEED); in.sprinting = true; in.headPitch = -20; }
                if (between(tick, 60, 100)) { walk(in, 0.1D); }
            }
            if (between(tick, 100, 200))
            {
                in.inWater = true;
                in.noGravity = true;
                if (between(tick, 130, 180)) walk(in, 0.1D);
            }
        }));

        // --- Wolf (already KUMO driven) ---------------------------------------------------------
        add(new Scenario(EntityKind.WOLF, "idle_walk_sit", FPS, 240, (tick, in) -> {
            if (between(tick, 40, 110)) walk(in, WALK_SPEED);
            if (between(tick, 130, 200)) in.wolfSitting = true;
            lookAround(in, tick);
        }));

        // --- Spider -----------------------------------------------------------------------------
        add(new Scenario(EntityKind.SPIDER, "idle_move_jump", FPS, 160, (tick, in) -> {
            if (between(tick, 30, 90)) walk(in, WALK_SPEED);
            if (tick == 110) in.jump = true;
            lookAround(in, tick);
        }));

        // --- Squid ------------------------------------------------------------------------------
        add(new Scenario(EntityKind.SQUID, "swim", FPS, 120, (tick, in) -> {
            in.inWater = true;
            in.noGravity = true;
            in.squidRotation = (float) ((tick * 0.2) % (Math.PI * 2));
        }));
    }

    private static void add(Scenario scenario)
    {
        ALL.add(scenario);
    }

    public static List<Scenario> all()
    {
        return Collections.unmodifiableList(ALL);
    }

    public static Scenario byId(String id)
    {
        for (Scenario s : ALL)
        {
            if (s.id().equals(id)) return s;
        }
        throw new IllegalArgumentException("Unknown scenario: " + id);
    }
}
