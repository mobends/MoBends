package goblinbob.mobends.lab.bake;

import goblinbob.mobends.core.animation.bit.AnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.JumpAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.StandAnimationBit;
import goblinbob.mobends.standard.animation.bit.biped.WalkAnimationBit;
import goblinbob.mobends.standard.animation.bit.pigzombie.AttackSlashInwardAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieLeanAnimationBit;
import goblinbob.mobends.standard.animation.bit.zombie_base.ZombieStumblingAnimationBit;
import goblinbob.mobends.lab.sim.EntityKind;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bakes the biped entities' procedural bits into format-2 clips:
 * the shared locomotion (stand / walk / jump) into {@code animations/biped/}, and each entity's
 * extras into its own folder.
 *
 * Usage: BipedBake &lt;mod-resources-dir&gt;
 */
public class BipedBake
{
    static final float TWO_PI = (float) (Math.PI * 2);
    static final float LIMB_SWING_SCALE = 0.6662F;
    static final float KNEEL_TICKS = 1F / 0.15F;

    public static void main(String[] args) throws IOException
    {
        Path animations = Paths.get(args[0]).resolve("assets/mobends/bends/animations");
        bakeBiped(animations.resolve("biped"));
        bakeZombie(animations.resolve("zombie"));
        bakeSkeleton(animations.resolve("skeleton"));
        bakePigZombie(animations.resolve("pigzombie"));
    }

    // --- shared biped locomotion ----------------------------------------------------------------

    static void bakeBiped(Path out) throws IOException
    {
        BakeRig rig = new BakeRig(EntityKind.ZOMBIE);
        Baker baker = new Baker(rig);

        StandAnimationBit<?> stand = new StandAnimationBit<>();
        WalkAnimationBit<?> walk = new WalkAnimationBit<>();
        JumpAnimationBit<?> jump = new JumpAnimationBit<>();

        // stand: breathing on the global tick clock (phase = ticks / 10)
        Baker.Setup standIdle = (r, phase) -> { r.ticks(phase * 10F); r.set("ticksAfterTouchdown", 100F); still(r); };
        baker.cycle(stand, standIdle, TWO_PI, 64).write(out.resolve("stand.json"));
        baker.sampleEnterClip(stand, r -> { r.set("ticksAfterTouchdown", 1F); still(r); }).write(out.resolve("stand_enter.json"));
        baker.oneShot(stand, (r, t) -> { r.ticks(0); r.set("ticksAfterTouchdown", t); still(r); }, KNEEL_TICKS, 32)
                .only("body", "root").write(out.resolve("kneel.json"));

        // walk: limb-swing cycle, split into constant pose / stepped fore legs / amplitude part
        Baker.Setup walkAt = (r, phase) -> walkSetup(r, phase, 0F);
        baker.cycle(walk, walkAt, TWO_PI, 64).without("leftForeLeg", "rightForeLeg").write(out.resolve("walk_base.json"));
        baker.stepCycle(walk, walkAt, TWO_PI, 2).only("leftForeLeg", "rightForeLeg").write(out.resolve("walk_forelegs.json"));
        baker.amplitudeDelta(walk,
                (r, phase) -> walkSetup(r, phase, 0F),
                (r, phase) -> walkSetup(r, phase, 1F),
                (r, phase) -> walkSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("walk_swing.json"));

        // jump
        baker.sampleEnterClip(jump, r -> { r.set("ticksInAir", 0F); still(r); }).write(out.resolve("jump_enter.json"));
        baker.oneShot(jump, (r, t) -> { r.set("ticksInAir", t); still(r); }, 10F, 10)
                .without("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("jump.json"));
        baker.oneShot(jump, (r, t) -> { r.set("ticksInAir", 5F); still(r); }, 0F, 0)
                .only("leftLeg", "rightLeg", "leftForeLeg", "rightForeLeg").write(out.resolve("jump_still.json"));
        Baker.Setup jumpMoving = (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0F); };
        baker.cycle(jump, jumpMoving, TWO_PI, 64).only("leftLeg", "rightLeg", "leftForeArm", "rightForeArm").write(out.resolve("jump_moving_base.json"));
        baker.stepCycle(jump, jumpMoving, TWO_PI, 2).only("leftForeLeg", "rightForeLeg").write(out.resolve("jump_moving_forelegs.json"));
        baker.amplitudeDelta(jump,
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0F); },
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 1F); },
                (r, phase) -> { r.set("ticksInAir", 5F); walkSetup(r, phase, 0.5F); }, TWO_PI, 64)
                .pruneIdentity().write(out.resolve("jump_moving_swing.json"));

        finish(baker, out);
    }

    // --- zombie: animation sets -----------------------------------------------------------------

    static void bakeZombie(Path out) throws IOException
    {
        BakeRig rig = new BakeRig(EntityKind.ZOMBIE);
        Baker baker = new Baker(rig);
        ZombieLeanAnimationBit lean = new ZombieLeanAnimationBit();
        ZombieStumblingAnimationBit stumble = new ZombieStumblingAnimationBit();

        baker.oneShot(lean, (r, t) -> { still(r); r.set("currentWalkingState", 0F); }, 0F, 0).pruneIdentity().write(out.resolve("lean.json"));
        baker.oneShot(lean, (r, t) -> { forward(r); r.set("currentWalkingState", 1F); }, 0F, 0).only("leftArm", "rightArm").write(out.resolve("lean_arms_up.json"));

        Baker.Setup stumbleAt = (r, phase) -> walkSetup(r, phase, 0F);
        baker.adaptiveCycle(stumble, stumbleAt, TWO_PI, 64, 5.0).without("head").write(out.resolve("stumble_base.json"));
        baker.adaptiveCycle(stumble, stumbleAt, TWO_PI, 64, 5.0).only("head").write(out.resolve("stumble_head.json"));
        baker.amplitudeDelta(stumble,
                (r, phase) -> walkSetup(r, phase, 0F),
                (r, phase) -> walkSetup(r, phase, 1F),
                (r, phase) -> walkSetup(r, phase, 0.5F), TWO_PI, 64)
                .pruneIdentity().write(out.resolve("stumble_swing.json"));

        finish(baker, out);
    }

    // --- skeleton: strafing legs ----------------------------------------------------------------

    static void bakeSkeleton(Path out) throws IOException
    {
        BakeRig rig = new BakeRig(EntityKind.SKELETON);
        Baker baker = new Baker(rig);
        AnimationBit<?> walk = new goblinbob.mobends.standard.animation.bit.skeleton.WalkAnimationBit();

        Baker.Setup strafeAt = (r, phase) -> { walkSetup(r, phase, 0F); strafe(r); };
        baker.cycle(walk, strafeAt, TWO_PI, 64).only("leftLeg", "rightLeg").write(out.resolve("strafe_base.json"));
        baker.amplitudeDelta(walk,
                (r, phase) -> { walkSetup(r, phase, 0F); strafe(r); },
                (r, phase) -> { walkSetup(r, phase, 1F); strafe(r); },
                (r, phase) -> { walkSetup(r, phase, 0.5F); strafe(r); }, TWO_PI, 64)
                .only("leftLeg", "rightLeg").write(out.resolve("strafe_swing.json"));

        finish(baker, out);
    }

    // --- pig zombie: attack --------------------------------------------------------------------

    static void bakePigZombie(Path out) throws IOException
    {
        BakeRig rig = new BakeRig(EntityKind.PIG_ZOMBIE);
        Baker baker = new Baker(rig);
        AttackSlashInwardAnimationBit attack = new AttackSlashInwardAnimationBit();
        AnimationBit<?> pigWalk = new goblinbob.mobends.standard.animation.bit.pigzombie.WalkAnimationBit();

        // The slash over ticksAfterAttack (arm swing saturates at 3.33 ticks); sampled while moving so
        // the "standing still" overlay stays out, then that overlay on its own.
        baker.oneShot(attack, (r, t) -> { r.set("ticksAfterAttack", t); r.headYaw(0); r.headPitch(0); forward(r); }, 10F, 30)
                .write(out.resolve("slash.json"));
        baker.oneShot(attack, (r, t) -> { r.set("ticksAfterAttack", 5F); r.headYaw(0); r.headPitch(0); still(r); }, 0F, 0)
                .only("leftLeg", "rightLeg", "rightForeLeg", "renderRotation", "root").write(out.resolve("slash_still.json"));

        // The walking bob of the pig zombie's global offset (its constant pose is hand-authored).
        baker.cycle(pigWalk, (r, phase) -> walkSetup(r, phase, 0F), TWO_PI, 64).only("root").write(out.resolve("walk_bob.json"));

        finish(baker, out);
    }

    // --- helpers --------------------------------------------------------------------------------

    static void walkSetup(BakeRig r, float phase, float amount)
    {
        r.limbSwing(phase / LIMB_SWING_SCALE);
        r.limbSwingAmount(amount);
        r.headYaw(0);
        r.headPitch(0);
        r.set("ticksAfterTouchdown", 100F);
        forward(r);
    }

    static void still(BakeRig r)
    {
        r.set("motionX", 0F);
        r.set("motionZ", 0F);
    }

    /** Moving along the look direction (yaw 0 looks down +Z), so the entity is not strafing. */
    static void forward(BakeRig r)
    {
        r.set("motionX", 0F);
        r.set("motionZ", 0.2F);
    }

    static void strafe(BakeRig r)
    {
        r.set("motionX", 0.2F);
        r.set("motionZ", 0F);
    }

    static void finish(Baker baker, Path out)
    {
        System.out.print(baker.log);
        if (!baker.partialVectorWrites.isEmpty())
        {
            System.out.println("partial vector writes (axes written): " + baker.partialVectorWrites);
        }
        System.out.println("baked clips into " + out);
    }
}
