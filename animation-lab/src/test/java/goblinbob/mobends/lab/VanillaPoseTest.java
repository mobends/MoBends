package goblinbob.mobends.lab;

import com.google.gson.JsonParser;
import goblinbob.mobends.core.definition.DefinedEntityData;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.bind.IVanillaPose;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;
import goblinbob.mobends.core.types.ExtensionDefinition;
import goblinbob.mobends.lab.scenarios.Animators;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.scenarios.Scripts;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static goblinbob.mobends.lab.PlayerRuns.angleBetween;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@code core:vanilla_pose} (misc/kumo-format.md, "Nodes") on a player: the lab has no vanilla model
 * to pose, so the player's data is handed a vanilla pose made up here, as the renderer hands it the
 * vanilla model's ({@code VanillaPoseReader}).
 */
public class VanillaPoseTest
{

    /** How close two runs' poses are (the goldens' gate, 0.1 degrees): two runs of the player differ by a few hundredths. */
    private static final double OWN = 0.1;

    private static final Path EXAMPLE = LabPaths.root().resolve("../misc/examples/vanilla-pose-extension/assets/mobends_vanilla_pose/bends");

    /** A layer always in a vanilla pose node, which bends the right elbow on top of it. */
    private static final String TAKE = "{\"formatVersion\": 2, \"layers\": [{\"defaultOnEntry\": \"theirs\", \"nodes\": {\"theirs\": "
            + "{\"core:vanilla_pose\": {\"pose\": [{\"core:rotate\": {\"bone\": \"rightForeArm\", \"axis\": \"x\", \"angle\": -25}}]}}}}]}";

    /** A vanilla pose of the right arm held out forward and the body turned, the rest left out. */
    private static final class MadeUpPose implements IVanillaPose
    {
        final Map<String, Quaternion> rotations = new HashMap<>();
        final Map<String, Vec3f> offsets = new HashMap<>();

        MadeUpPose()
        {
            rotations.put("rightArm", axisAngle(1, 0, 0, -90));
            offsets.put("rightArm", new Vec3f(0, 1, 0));
            rotations.put("body", axisAngle(0, 1, 0, 30));
            offsets.put("body", new Vec3f());
            // Vanilla draws a split bone's further segments straight with its part.
            rotations.put("rightForeArm", new Quaternion());
            offsets.put("rightForeArm", new Vec3f());
        }

        @Override
        public boolean read(String bone, Quaternion rotation, Vec3f offset, Vec3f scale)
        {
            Quaternion r = rotations.get(bone);
            if (r == null) return false;
            rotation.set(r);
            offset.set(offsets.get(bone));
            scale.set(1, 1, 1);
            return true;
        }
    }

    private static Quaternion axisAngle(float x, float y, float z, float degrees)
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(x, y, z, (float) Math.toRadians(degrees));
        return q;
    }

    private static float[] array(Quaternion q)
    {
        return new float[] { q.x, q.y, q.z, q.w };
    }

    @Test
    void theNodePosesWhatTheVanillaPoseHasAndTheRestIsThePlayersOwn() throws Exception
    {
        Scenario scenario = new Scenario(EntityKind.PLAYER, "vanilla_pose", Scenarios.FPS, 40, (tick, in) -> Scripts.walk(in, Scripts.WALK_SPEED));
        // One session at a time: the lab's randomness is seeded per session.
        java.util.List<FramePose> ownFrames = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER))).run().frames;
        KumoSession taking = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)),
                Collections.singletonList(KumoSerializer.INSTANCE.gson.fromJson(TAKE, AnimatorTemplate.class)));
        DefinedEntityData<?> data = (DefinedEntityData<?>) taking.data;
        MadeUpPose vanilla = new MadeUpPose();

        int checked = 0;
        for (int frame = 0; frame < scenario.frameCount(); frame++)
        {
            float tick = frame * 20F / Scenarios.FPS;
            // The renderer hands the pose over only while it can read the vanilla model; here, ticks 10 to 30.
            data.setVanillaPose(tick >= 10 && tick < 30 ? vanilla : null);
            FramePose own = ownFrames.get(frame);
            FramePose taken = taking.step();
            if (tick < 10)
            {
                // Without a vanilla pose to read, the node poses nothing: the player's own animation shows.
                assertTrue(angleBetween(own.bones.get("rightArm").rt, taken.bones.get("rightArm").rt) < OWN, "own arm, tick " + tick);
                assertTrue(angleBetween(own.bones.get("body").rt, taken.bones.get("body").rt) < OWN, "own body, tick " + tick);
            }
            else if (tick >= 10 && tick < 30)
            {
                assertTrue(angleBetween(array(vanilla.rotations.get("rightArm")), taken.bones.get("rightArm").rt) < OWN,
                        "the vanilla pose's arm, tick " + tick);
                assertArrayEquals(new float[] { 0, 1, 0 }, taken.bones.get("rightArm").o, 1e-4F, "the vanilla pose's arm offset, tick " + tick);
                assertTrue(angleBetween(array(vanilla.rotations.get("body")), taken.bones.get("body").rt) < OWN, "the vanilla pose's body, tick " + tick);
                // The node's own item bends the elbow, which the vanilla pose has straight.
                assertEquals(25, angleBetween(array(new Quaternion()), taken.bones.get("rightForeArm").rt), OWN, "the bent elbow, tick " + tick);
                // What the vanilla pose doesn't have is the player's own.
                assertTrue(angleBetween(own.bones.get("leftLeg").rt, taken.bones.get("leftLeg").rt) < OWN, "own leg, tick " + tick);
                checked++;
            }
        }
        assertTrue(checked > 0);
    }

    @Test
    void theExampleTakesNothingWithoutPlayerAnimationLibrary() throws Exception
    {
        ExtensionDefinition extension = ExtensionDefinition.parse(JsonParser.parseString(
                new String(Files.readAllBytes(EXAMPLE.resolve("extensions/vanilla_pose.json")), StandardCharsets.UTF_8)));
        assertEquals("mobends:player", extension.type);
        AnimatorTemplate example;
        try (Reader reader = Files.newBufferedReader(EXAMPLE.resolve("animators/vanilla_pose.json")))
        {
            example = KumoSerializer.INSTANCE.gson.fromJson(reader, AnimatorTemplate.class);
        }

        Scenario scenario = new Scenario(EntityKind.PLAYER, "vanilla_pose_example", Scenarios.FPS, 20, (tick, in) -> Scripts.walk(in, Scripts.WALK_SPEED));
        java.util.List<FramePose> ownFrames = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER))).run().frames;
        KumoSession withExample = new KumoSession(scenario, KumoSession.loadAnimator(Animators.forKind(EntityKind.PLAYER)),
                Collections.singletonList(example));
        ((DefinedEntityData<?>) withExample.data).setVanillaPose(new MadeUpPose());
        for (int frame = 0; frame < scenario.frameCount(); frame++)
        {
            FramePose own = ownFrames.get(frame);
            FramePose taken = withExample.step();
            // The lab has no PAL: its layer never holds, whatever vanilla pose there is.
            assertTrue(angleBetween(own.bones.get("rightArm").rt, taken.bones.get("rightArm").rt) < OWN, "frame " + frame);
            assertTrue(angleBetween(own.bones.get("rightForeArm").rt, taken.bones.get("rightForeArm").rt) < OWN, "frame " + frame);
        }
    }

}
