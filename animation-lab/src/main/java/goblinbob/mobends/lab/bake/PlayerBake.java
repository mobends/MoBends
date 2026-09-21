package goblinbob.mobends.lab.bake;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.standard.animation.bit.biped.*;
import goblinbob.mobends.standard.animation.bit.player.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static goblinbob.mobends.lab.bake.BipedBake.*;

/**
 * Bakes the player-only procedural bits into {@code animations/player/}.
 * Usage: PlayerBake &lt;mod-resources-dir&gt;
 */
public class PlayerBake
{
    public static void main(String[] args) throws IOException
    {
        Path out = Paths.get(args[0]).resolve("assets/mobends/bends/animations/player");
        BakeRig rig = new BakeRig(EntityKind.PLAYER);
        Baker baker = new Baker(rig);

        // ---- sprint: limb swing scaled by 0.8, forearms stepped -------------------------------------
        AnimationBit<?> sprint = new goblinbob.mobends.standard.animation.bit.biped.SprintAnimationBit<>();
        Baker.Setup sprintAt = (r, phase) -> sprintSetup(r, phase, 0F);
        baker.cycle(sprint, sprintAt, TWO_PI, 64).without("leftForeArm", "rightForeArm").write(out.resolve("sprint_base.json"));
        baker.stepCycle(sprint, sprintAt, TWO_PI, 2).only("leftForeArm", "rightForeArm").write(out.resolve("sprint_forearms.json"));
        baker.amplitudeDelta(sprint,
                (r, phase) -> sprintSetup(r, phase, 0F),
                (r, phase) -> sprintSetup(r, phase, 1F),
                (r, phase) -> sprintSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("sprint_swing.json"));

        // ---- sneak: overlay on stand / walk -----------------------------------------------------------
        AnimationBit<?> sneak = new SneakAnimationBit();
        Baker.Setup sneakAt = (r, phase) -> walkSetup(r, phase, 0F);
        baker.cycle(sneak, sneakAt, TWO_PI, 64).only("leftLeg", "rightLeg", "leftArm", "rightArm", "root", "localOffset").write(out.resolve("sneak_base.json"));
        baker.stepCycle(sneak, sneakAt, TWO_PI, 2).only("leftForeLeg", "rightForeLeg").write(out.resolve("sneak_forelegs.json"));
        baker.amplitudeDelta(sneak,
                (r, phase) -> walkSetup(r, phase, 0F),
                (r, phase) -> walkSetup(r, phase, 1F),
                (r, phase) -> walkSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("sneak_swing.json"));
        baker.cycle(sneak, sneakAt, TWO_PI, 64).only("body").write(out.resolve("sneak_body.json"));
        baker.cycle(sneak, sneakAt, TWO_PI, 64).only("head").write(out.resolve("sneak_head.json"));

        // ---- sleeping: constant pose + head breathing on the tick clock ----------------------------
        AnimationBit<?> sleeping = new SleepingAnimationBit();
        baker.cycle(sleeping, (r, phase) -> { r.ticks(phase * 10F); still(r); }, TWO_PI, 64).write(out.resolve("sleeping.json"));

        // ---- sitting (boat / minecart) --------------------------------------------------------------
        AnimationBit<?> sitting = new SittingAnimationBit();
        baker.oneShot(sitting, (r, t) -> { still(r); r.headYaw(0); r.headPitch(0); }, 0F, 0).write(out.resolve("sitting.json"));

        // ---- riding a living mount --------------------------------------------------------------------
        AnimationBit<?> riding = new RidingAnimationBit();
        baker.oneShot(riding, (r, t) -> { still(r); r.headYaw(0); r.headPitch(0); r.ride(false); }, 0F, 0).write(out.resolve("riding.json"));
        // legs as a function of the yaw relative to the mount, -180..180
        baker.curve(riding, (r, yaw) -> { still(r); r.headYaw(0); r.headPitch(0); r.ride(true); r.entity().rotationYaw = yaw; }, -180F, 180F, 72)
                .only("leftLeg", "rightLeg").write(out.resolve("riding_legs.json"));
        Baker.Setup ridingSlow = (r, t) -> { r.headYaw(0); r.headPitch(0); r.ride(false); forward(r); r.entity().motionX = 0; r.entity().motionZ = 0; };
        baker.oneShot(riding, ridingSlow, 0F, 0).only("body", "leftArm", "rightArm", "leftForeArm", "rightForeArm", "head").write(out.resolve("riding_moving.json"));
        Baker.Setup ridingFast = (r, phase) -> { r.ticks(phase / 0.5F); r.headYaw(0); r.headPitch(0); r.ride(false); forward(r); r.entity().motionX = 0.3; r.entity().motionZ = 0; };
        baker.cycle(riding, ridingFast, TWO_PI, 64).only("body", "leftArm", "rightArm", "head", "root").write(out.resolve("riding_fast.json"));

        // ---- flying: hovering sways (two clocks), the rest are drivers ---------------------------------
        AnimationBit<?> flying = new FlyingAnimationBit();
        Baker.Setup hoverA = (r, phase) -> { r.ticks(phase / 0.0825F); still(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); };
        baker.cycle(flying, hoverA, TWO_PI, 64).only("leftArm", "rightArm", "leftForeArm", "rightForeArm", "body").write(out.resolve("fly_hover_arms.json"));
        Baker.Setup hoverB = (r, phase) -> { r.ticks(phase / 0.125F); still(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); };
        baker.cycle(flying, hoverB, TWO_PI, 64).only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("fly_hover_legs.json"));

        // ---- falling: flailing on the tick clock (ticks * 0.5) --------------------------------------
        AnimationBit<?> falling = new FallingAnimationBit();
        Baker.Setup fallAt = (r, phase) -> { r.ticks(phase / 0.5F); still(r); r.headYaw(0); r.headPitch(0); r.set("ticksFalling", 100F); };
        baker.cycle(falling, fallAt, TWO_PI, 64).without("head").write(out.resolve("falling.json"));

        // ---- ladder: everything cyclic over the climbing cycle --------------------------------------
        AnimationBit<?> ladder = new LadderClimbAnimationBit();
        rig.placeLadderColumn();
        Baker.Setup climbAt = (r, phase) -> { r.set("climbingCycle", phase); r.headYaw(0); r.headPitch(0); r.entity().rotationYaw = 0; still(r); };
        baker.cycle(ladder, climbAt, TWO_PI, 64).without("head", "renderRotation").write(out.resolve("ladder.json"));

        bakeActions(baker, out);

        // ---- swimming: surface (two clocks) and underwater (two clocks) -----------------------------
        AnimationBit<?> swimming = new SwimmingAnimationBit();
        Baker.Setup surfaceA = (r, phase) -> { r.ticks(phase / 0.0825F); still(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); BakeRig.setField(swimming, "transformTransition", 0F); };
        baker.cycle(swimming, surfaceA, TWO_PI, 64).only("leftArm", "rightArm", "leftForeArm", "rightForeArm", "body").write(out.resolve("swim_surface_arms.json"));
        Baker.Setup surfaceB = (r, phase) -> { r.ticks(phase / 0.2625F); still(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); BakeRig.setField(swimming, "transformTransition", 0F); };
        baker.cycle(swimming, surfaceB, TWO_PI, 64).only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("swim_surface_legs.json"));
        // Underwater needs water blocks and motion; the arms' ramp-dependent part is generated analytically.
        goblinbob.mobends.lab.sim.LabWorlds.placeWaterColumn(rig.entity().world, rig.entity(), 8);
        rig.entity().inWater = true;
        Baker.Setup deepA = (r, phase) -> { r.ticks(phase / 0.1625F); forward(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); BakeRig.setField(swimming, "transformTransition", 1F); };
        baker.adaptiveCycle(swimming, deepA, TWO_PI, 64, 5.0).only("leftForeArm", "rightForeArm", "body", "renderRightItemRotation").write(out.resolve("swim_deep_arms.json"));
        Baker.Setup deepB = (r, phase) -> { r.ticks(phase / 0.4625F); forward(r); r.headYaw(0); r.headPitch(0); r.set("ticksAfterAttack", 100F); BakeRig.setField(swimming, "transformTransition", 1F); };
        baker.cycle(swimming, deepB, TWO_PI, 64).only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("swim_deep_legs.json"));

        finish(baker, out);
    }

    // ---- the action layer: sword slashes and the attack stance's landing kneel ----------------------
    static void bakeActions(Baker baker, Path out) throws IOException
    {
        // Slashes driven by ticksAfterAttack (up, inward, whirl) and by the bit's own clock
        // (down, outward: ticksPlayed, i.e. the node's elapsed time). Sampled while moving and
        // looking straight ahead: the look goes in through drivers around the baked head part.
        AnimationBit<?> up = new AttackSlashUpAnimationBit();
        AnimationBit<?> inward = new AttackSlashInwardAnimationBit();
        AnimationBit<?> whirl = new AttackWhirlSlashAnimationBit();
        AnimationBit<?> down = new AttackSlashDownAnimationBit();
        AnimationBit<?> outward = new AttackSlashOutwardAnimationBit();
        Baker.Setup byAttack = (r, t) -> { r.set("ticksAfterAttack", t); r.headYaw(0); r.headPitch(0); forward(r); };
        Baker.Setup byAttackStill = (r, t) -> { r.set("ticksAfterAttack", 5F); r.headYaw(0); r.headPitch(0); still(r); };
        String[] stillBonesA = { "leftLeg", "rightLeg", "rightForeLeg", "renderRotation", "root" };
        String[] stillBonesB = { "leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg", "renderRotation", "root" };

        baker.oneShot(up, byAttack, 10F, 30).write(out.resolve("slash_up.json"));
        baker.oneShot(up, byAttackStill, 0F, 0).only(stillBonesA).write(out.resolve("slash_up_still.json"));
        baker.oneShot(inward, byAttack, 10F, 30).write(out.resolve("slash_inward.json"));
        baker.oneShot(inward, byAttackStill, 0F, 0).only(stillBonesA).write(out.resolve("slash_inward_still.json"));
        // The whirl's render rotation spins a full turn in 6.25 ticks: dense samples keep the
        // interpolation error small.
        baker.oneShot(whirl, byAttack, 10F, 100).write(out.resolve("slash_whirl.json"));
        baker.oneShot(whirl, byAttackStill, 0F, 0).only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("slash_whirl_still.json"));

        for (Object[] pair : new Object[][] { { down, "slash_down" }, { outward, "slash_outward" } })
        {
            AnimationBit<?> bit = (AnimationBit<?>) pair[0];
            String name = (String) pair[1];
            Baker.Setup byClock = (r, t) -> { BakeRig.setField(bit, "ticksPlayed", t); r.set("ticksAfterAttack", 100F); r.headYaw(0); r.headPitch(0); forward(r); };
            Baker.Setup byClockStill = (r, t) -> { BakeRig.setField(bit, "ticksPlayed", 5F); r.set("ticksAfterAttack", 100F); r.headYaw(0); r.headPitch(0); still(r); };
            baker.oneShot(bit, byClock, 10F, 30).write(out.resolve(name + ".json"));
            baker.oneShot(bit, byClockStill, 0F, 0).only(stillBonesB).write(out.resolve(name + "_still.json"));
        }

        // The stance's landing kneel over ticksAfterTouchdown (body and the global offset), with the
        // breathing at phase 0 (ticks = 0).
        AnimationBit<?> stance = new AttackStanceAnimationBit();
        baker.oneShot(stance, (r, t) -> { r.set("ticksAfterTouchdown", t); r.ticks(0); r.headYaw(0); r.headPitch(0); still(r); r.set("ticksAfterAttack", 30F); }, 1F / 0.15F, 40)
                .only("body", "root").write(out.resolve("stance_kneel.json"));
    }

    static void sprintSetup(BakeRig r, float phase, float amount)
    {
        r.limbSwing(phase / (LIMB_SWING_SCALE * 0.8F));
        r.limbSwingAmount(amount);
        r.headYaw(0);
        r.headPitch(0);
        r.set("ticksAfterTouchdown", 100F);
        r.set("ticksAfterAttack", 100F);
        forward(r);
    }
}
