package goblinbob.mobends.lab.scenarios;

import goblinbob.mobends.lab.sim.Determinism;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.Scenario;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

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
        for (EntityKind kind : new EntityKind[] { EntityKind.ZOMBIE, EntityKind.ZOMBIE_VILLAGER, EntityKind.SKELETON, EntityKind.PIG_ZOMBIE })
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

        for (boolean left : new boolean[] { false, true })
        {
            add(new Scenario(EntityKind.SKELETON, left ? "bow_and_sword_left_handed" : "bow_and_sword", FPS, 200, (tick, in) -> {
                in.leftHanded = left;
                if (tick >= 5 && tick < 110) in.mainHand = new ItemStack(Items.BOW);
                if (between(tick, 20, 90))
                {
                    in.itemUseCount = 72000 - (tick - 20);
                    in.itemUseMaxCount = tick - 20;
                }
                if (between(tick, 60, 100)) walk(in, WALK_SPEED);
                if (tick >= 110) in.mainHand = new ItemStack(Items.IRON_SWORD);
                if (tick == 120 || tick == 132 || tick == 144) in.attack = true;
                if (between(tick, 170, 200)) walk(in, WALK_SPEED);
                if (tick == 175) in.attack = true;
                lookAround(in, tick);
            }));
        }

        add(new Scenario(EntityKind.PIG_ZOMBIE, "walk_attack_left_handed", FPS, 120, (tick, in) -> {
            in.leftHanded = true;
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            if (between(tick, 10, 60)) walk(in, WALK_SPEED);
            if (tick == 40 || tick == 80) in.attack = true;
            lookAround(in, tick);
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

        // Item use: vanilla counts the remaining use ticks down (itemInUseCount) and exposes the
        // ticks used so far as getItemInUseMaxCount().
        // The item is switched to a few ticks before it is used, as a player would.
        add(new Scenario(EntityKind.PLAYER, "bow_and_eat", FPS, 160, (tick, in) -> {
            if (between(tick, 5, 80)) in.mainHand = new ItemStack(Items.BOW);
            if (between(tick, 10, 70))
            {
                in.itemUseCount = 72000 - (tick - 10);
                in.itemUseMaxCount = tick - 10;
            }
            if (between(tick, 85, 160)) in.mainHand = new ItemStack(Items.APPLE);
            if (between(tick, 90, 150))
            {
                in.itemUseCount = Math.max(0, 32 - (tick - 90));
                in.itemUseMaxCount = tick - 90;
            }
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.PLAYER, "sword_moves", FPS, 200, (tick, in) -> {
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            // Two hits, a pause longer than the combo window, then the combo starts over.
            if (tick == 10 || tick == 22 || tick == 60) in.attack = true;
            // The sprinting stance.
            if (between(tick, 90, 140)) { walk(in, SPRINT_SPEED); in.sprinting = true; }
            if (tick == 100) in.attack = true;
            // A hit in the air.
            if (tick == 150) in.jump = true;
            if (tick == 152) in.attack = true;
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.PLAYER, "punch_and_tool", FPS, 200, (tick, in) -> {
            // Bare fists: three punches standing still, one while walking, then the guard fades.
            if (tick == 10 || tick == 22 || tick == 34) in.attack = true;
            if (between(tick, 50, 90)) walk(in, WALK_SPEED);
            if (tick == 60) in.attack = true;
            // A pickaxe: swings standing, then while sneaking.
            if (tick >= 110) in.mainHand = new ItemStack(Items.IRON_PICKAXE);
            if (tick == 120 || tick == 132) in.attack = true;
            if (between(tick, 150, 200)) { walk(in, SNEAK_SPEED); in.sneaking = true; }
            if (tick == 160 || tick == 175) in.attack = true;
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.PLAYER, "offhand_use", FPS, 160, (tick, in) -> {
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            in.offHand = new ItemStack(Items.SHIELD);
            // Blocking with the off-hand shield, then a slash once it is lowered.
            if (between(tick, 10, 60))
            {
                in.activeHand = EnumHand.OFF_HAND;
                in.itemUseCount = 72000 - (tick - 10);
                in.itemUseMaxCount = tick - 10;
            }
            if (between(tick, 70, 100)) walk(in, WALK_SPEED);
            if (tick == 75) in.attack = true;
            // Eating from the off hand.
            if (between(tick, 110, 160))
            {
                in.offHand = new ItemStack(Items.APPLE);
                in.activeHand = EnumHand.OFF_HAND;
                in.itemUseCount = Math.max(0, 32 - (tick - 110));
                in.itemUseMaxCount = tick - 110;
            }
            lookAround(in, tick);
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
                // Deep water from tick 150: the swimming bit switches to its underwater pose.
                if (tick >= 150) in.waterHeight = 8;
            }
        }));

        add(new Scenario(EntityKind.PLAYER, "ladder_climb", FPS, 160, (tick, in) -> {
            in.ladderColumn = true;
            if (between(tick, 10, 150))
            {
                in.onLadder = true;
                in.noGravity = true;
                in.verticalSpeed = between(tick, 10, 80) ? 0.12D : (between(tick, 80, 110) ? 0.0D : -0.12D);
                in.headYaw = (float) (Math.sin(tick * 0.05) * 60.0);
                in.headPitch = -30;
            }
        }));

        add(new Scenario(EntityKind.PLAYER, "riding", FPS, 160, (tick, in) -> {
            in.noGravity = true;
            in.riding = true;
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            lookAround(in, tick);
            // A slash from the saddle: the still-standing legs must not apply.
            if (tick == 20) in.attack = true;
            if (between(tick, 60, 120)) walk(in, 0.3D);
            if (between(tick, 120, 160)) walk(in, 0.05D);
        }));

        add(new Scenario(EntityKind.PLAYER, "ladder_bow", FPS, 140, (tick, in) -> {
            in.ladderColumn = true;
            in.mainHand = new ItemStack(Items.BOW);
            if (between(tick, 10, 130))
            {
                in.onLadder = true;
                in.noGravity = true;
                in.verticalSpeed = between(tick, 10, 60) ? 0.12D : 0.0D;
                in.headYaw = (float) (Math.sin(tick * 0.05) * 60.0);
                in.headPitch = -20;
            }
            if (between(tick, 40, 120))
            {
                in.itemUseCount = 72000 - (tick - 40);
                in.itemUseMaxCount = tick - 40;
            }
        }));

        add(new Scenario(EntityKind.PLAYER, "sleep_sit_elytra", FPS, 180, (tick, in) -> {
            if (between(tick, 0, 60)) in.sleeping = true;
            if (between(tick, 60, 120))
            {
                in.noGravity = true;
                in.elytraTicks = tick - 60;
                walk(in, 0.4D);
                in.headPitch = -20;
                in.headYaw = 30;
            }
            if (between(tick, 120, 180))
            {
                in.noGravity = true;
                in.riding = true;
                in.ridingLiving = false;
                lookAround(in, tick);
            }
        }));

        // --- Wolf (already KUMO driven) ---------------------------------------------------------
        add(new Scenario(EntityKind.WOLF, "idle_walk_sit", FPS, 240, (tick, in) -> {
            if (between(tick, 40, 110)) walk(in, WALK_SPEED);
            if (between(tick, 130, 200)) in.wolfSitting = true;
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.WOLF, "interest_and_shake", FPS, 160, (tick, in) -> {
            // Vanilla ramps the interest 0..1 while the wolf begs, and the shake timer 0..~2 while it dries off.
            if (between(tick, 10, 70)) in.wolfInterested = Math.min(1F, (tick - 10) * 0.04F);
            if (between(tick, 90, 150)) in.wolfShaking = (tick - 90) * 0.04F;
            lookAround(in, tick);
        }));

        // A left-handed player: the attack bits mirror on the primary hand, the use actions follow the active hand.
        add(new Scenario(EntityKind.PLAYER, "left_handed", FPS, 260, (tick, in) -> {
            in.leftHanded = true;
            in.mainHand = new ItemStack(Items.IRON_SWORD);
            if (tick == 10 || tick == 22 || tick == 34) in.attack = true;
            if (between(tick, 60, 100)) walk(in, WALK_SPEED);
            if (tick == 70) in.attack = true;
            if (between(tick, 100, 130)) { walk(in, SPRINT_SPEED); in.sprinting = true; }
            if (tick == 110) in.attack = true;
            if (tick >= 140) in.mainHand = new ItemStack(Items.IRON_PICKAXE);
            if (tick == 150 || tick == 162) in.attack = true;
            if (tick >= 180) in.mainHand = ItemStack.EMPTY;
            if (tick == 190 || tick == 202) in.attack = true;
            // a torch, in the main hand then the off hand
            if (between(tick, 220, 240)) in.mainHand = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH));
            if (between(tick, 240, 260)) in.offHand = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH));
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.PLAYER, "right_handed_torch_offhand", FPS, 80, (tick, in) -> {
            if (between(tick, 10, 70)) in.offHand = new ItemStack(net.minecraft.item.Item.getItemFromBlock(net.minecraft.init.Blocks.TORCH));
            if (between(tick, 40, 70)) walk(in, WALK_SPEED);
            lookAround(in, tick);
        }));

        // --- Spider -----------------------------------------------------------------------------
        add(new Scenario(EntityKind.SPIDER, "idle_move_jump", FPS, 160, (tick, in) -> {
            if (between(tick, 30, 90)) walk(in, WALK_SPEED);
            if (tick == 110) in.jump = true;
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.SPIDER, "death", FPS, 120, (tick, in) -> {
            if (between(tick, 10, 60)) walk(in, WALK_SPEED);
            if (tick >= 60) in.health = 0;
            lookAround(in, tick);
        }));

        add(new Scenario(EntityKind.SPIDER, "crawl", FPS, 140, (tick, in) -> {
            if (between(tick, 20, 120))
            {
                in.spiderClimbing = true;
                in.onLadder = true;
                in.ladderColumn = true;
                in.noGravity = true;
                in.verticalSpeed = between(tick, 30, 100) ? 0.1D : 0.0D;
            }
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
