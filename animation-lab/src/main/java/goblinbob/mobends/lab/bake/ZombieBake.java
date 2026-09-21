package goblinbob.mobends.lab.bake;

import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieLeanAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieStumblingAnimationBit;
import goblinbob.mobends.lab.sim.EntityKind;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bakes the zombie's procedural bits into format-2 clips under
 * {@code src/main/resources/assets/mobends/bends/animations/zombie/}.
 *
 * Usage: ZombieBake &lt;mod-resources-dir&gt;
 */
public class ZombieBake
{
    private static final float TWO_PI = (float) (Math.PI * 2);
    private static final float LIMB_SWING_SCALE = 0.6662F;
    private static final float KNEEL_TICKS = 1F / 0.15F;

    public static void main(String[] args) throws IOException
    {
        Path out = Paths.get(args[0]).resolve("assets/mobends/bends/animations/zombie");
        BakeRig rig = new BakeRig(EntityKind.ZOMBIE);
        Baker baker = new Baker(rig);

        StandAnimationBit<?> stand = new StandAnimationBit<>();
        WalkAnimationBit<?> walk = new WalkAnimationBit<>();
        JumpAnimationBit<?> jump = new JumpAnimationBit<>();
        ZombieLeanAnimationBit lean = new ZombieLeanAnimationBit();
        ZombieStumblingAnimationBit stumble = new ZombieStumblingAnimationBit();

        // ---- stand: breathing on the global tick clock (phase = ticks / 10) ----------------------
        Baker.Setup standIdle = (r, phase) -> { r.ticks(phase * 10F); r.set("ticksAfterTouchdown", 100F); still(r); };
        baker.cycle(stand, standIdle, TWO_PI, 64).write(out.resolve("stand.json"));
        baker.sampleEnterClip(stand, r -> { r.set("ticksAfterTouchdown", 1F); still(r); }).write(out.resolve("stand_enter.json"));
        baker.oneShot(stand, (r, t) -> { r.ticks(0); r.set("ticksAfterTouchdown", t); still(r); }, KNEEL_TICKS, 32)
                .only("body", "root").write(out.resolve("kneel.json"));
        report(baker, "stand", baker.sample(stand, standIdle, 0).smoothness);

        // ---- walk: limb-swing cycle, split into constant pose / stepped fore legs / amplitude part --
        Baker.Setup walkAt = (r, phase) -> { walkSetup(r, phase, 0F); };
        baker.cycle(walk, walkAt, TWO_PI, 64).without("leftForeLeg", "rightForeLeg").write(out.resolve("walk_base.json"));
        baker.stepCycle(walk, walkAt, TWO_PI, 2).only("leftForeLeg", "rightForeLeg").write(out.resolve("walk_forelegs.json"));
        baker.amplitudeDelta(walk,
                (r, phase) -> walkSetup(r, phase, 0F),
                (r, phase) -> walkSetup(r, phase, 1F),
                (r, phase) -> walkSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("walk_swing.json"));
        report(baker, "walk", baker.sample(walk, walkAt, 0).smoothness);

        // ---- jump ---------------------------------------------------------------------------------
        baker.sampleEnterClip(jump, r -> { r.set("ticksInAir", 0F); still(r); }).write(out.resolve("jump_enter.json"));
        baker.oneShot(jump, (r, t) -> { r.set("ticksInAir", t); still(r); }, 10F, 10)
                .without("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("jump.json"));
        baker.oneShot(jump, (r, t) -> { r.set("ticksInAir", 5F); still(r); }, 0F, 0)
                .only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("jump_still.json"));
        Baker.Setup jumpMoving = (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0F); moving(r); };
        baker.cycle(jump, jumpMoving, TWO_PI, 64).only("leftLeg", "rightLeg", "leftForeArm", "rightForeArm").write(out.resolve("jump_moving_base.json"));
        baker.stepCycle(jump, jumpMoving, TWO_PI, 2).only("leftForeLeg", "rightForeLeg").write(out.resolve("jump_moving_forelegs.json"));
        baker.amplitudeDelta(jump,
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0F); moving(r); },
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 1F); moving(r); },
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0.5F); moving(r); }, TWO_PI, 64)
                .pruneIdentity().write(out.resolve("jump_moving_swing.json"));
        report(baker, "jump (still)", baker.sample(jump, (r, t) -> { r.set("ticksInAir", 5F); still(r); }, 0).smoothness);
        report(baker, "jump (moving)", baker.sample(jump, jumpMoving, 0).smoothness);

        // ---- lean (animation set 0): additive deltas on top of the base --------------------------
        baker.oneShot(lean, (r, t) -> { still(r); r.set("currentWalkingState", 0F); }, 0F, 0).pruneIdentity().write(out.resolve("lean.json"));
        baker.oneShot(lean, (r, t) -> { moving(r); r.set("currentWalkingState", 1F); }, 0F, 0).only("leftArm", "rightArm").write(out.resolve("lean_arms_up.json"));

        // ---- stumble (animation set 1): sawtooth warped cycle, sampled densely --------------------
        Baker.Setup stumbleAt = (r, phase) -> walkSetup(r, phase, 0F);
        baker.adaptiveCycle(stumble, stumbleAt, TWO_PI, 64, 5.0).without("head").write(out.resolve("stumble_base.json"));
        baker.adaptiveCycle(stumble, stumbleAt, TWO_PI, 64, 5.0).only("head").write(out.resolve("stumble_head.json"));
        baker.amplitudeDelta(stumble,
                (r, phase) -> walkSetup(r, phase, 0F),
                (r, phase) -> walkSetup(r, phase, 1F),
                (r, phase) -> walkSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("stumble_swing.json"));
        report(baker, "stumble", baker.sample(stumble, stumbleAt, 0).smoothness);

        System.out.print(baker.log);
        System.out.println("partial vector writes (axes written): " + baker.partialVectorWrites);
        System.out.println("baked zombie clips into " + out);
    }

    private static void walkSetup(BakeRig r, float phase, float amount)
    {
        r.limbSwing(phase / LIMB_SWING_SCALE);
        r.limbSwingAmount(amount);
        r.headYaw(0);
        r.headPitch(0);
        r.set("ticksAfterTouchdown", 100F);
        moving(r);
    }

    private static void still(BakeRig r)
    {
        r.set("motionX", 0F);
        r.set("motionZ", 0F);
    }

    private static void moving(BakeRig r)
    {
        r.set("motionX", 0.2F);
        r.set("motionZ", 0F);
    }

    private static void report(Baker baker, String name, java.util.Map<String, Float> smoothness)
    {
        baker.log.append(name).append(" smoothness after perform: ").append(smoothness).append('\n');
    }
}
