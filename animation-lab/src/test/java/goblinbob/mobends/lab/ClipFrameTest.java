package goblinbob.mobends.lab;

import com.google.gson.Gson;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.lab.scenarios.Scenarios;
import goblinbob.mobends.lab.sim.EntityKind;
import goblinbob.mobends.lab.sim.KumoSession;
import goblinbob.mobends.lab.sim.Scenario;
import goblinbob.mobends.lab.trace.FramePose;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Clip items (misc/kumo-format.md, "Clips"): frame, duration, clipLength and elapsed. */
public class ClipFrameTest
{

    /** leftArm turns by -10° per frame: 11 keyframes over a clipLength of 10. */
    private static final String RAMP = "mobends_test:bends/animations/ramp.json";

    @BeforeAll
    static void registerRamp()
    {
        StringBuilder keyframes = new StringBuilder();
        for (int i = 0; i <= 10; i++)
        {
            double half = Math.toRadians(-10 * i) / 2;
            if (i > 0) keyframes.append(',');
            keyframes.append("{\"position\": [0, 0, 0], \"rotation\": [").append(Math.sin(half)).append(", 0, 0, ")
                    .append(Math.cos(half)).append("], \"scale\": [1, 1, 1]}");
        }
        KeyframeAnimation ramp = new Gson().fromJson("{\"duration\": 10, \"bones\": {\"leftArm\": {\"keyframes\": [" + keyframes + "]}}}", KeyframeAnimation.class);
        KumoSession.registerClip(RAMP, ramp);
    }

    @AfterAll
    static void unregisterRamp()
    {
        KumoSession.clearRegisteredClips();
    }

    private static FramePose run(int ticks, String pose) throws MalformedKumoTemplateException
    {
        return run(ticks, pose, "", "");
    }

    /**
     * Runs an animator entering the node "main" whose pose is {@code pose} (JSON items), with
     * {@code mainFields} added to it and {@code otherNodes} after it, for {@code ticks} ticks.
     */
    private static FramePose run(int ticks, String pose, String mainFields, String otherNodes) throws MalformedKumoTemplateException
    {
        String nodes = "\"main\": {\"type\": \"core:pose\", \"pose\": [" + pose + "]" + mainFields + "}" + otherNodes;
        AnimatorTemplate template = KumoSerializer.INSTANCE.gson.fromJson("{\"formatVersion\": 2, \"layers\": [{\"type\": \"KEYFRAME\", \"entryNode\": \"main\", \"nodes\": {"
                + nodes + "}}]}", AnimatorTemplate.class);
        Scenario scenario = new Scenario(EntityKind.PLAYER, "clip_frame", Scenarios.FPS, ticks, (tick, in) -> {});
        List<FramePose> frames = new KumoSession(scenario, template).run().frames;
        return frames.get(frames.size() - 1);
    }

    private static String clip(String fields)
    {
        return "{\"animationKey\": \"" + RAMP + "\", \"space\": \"OVERRIDE\"" + (fields.isEmpty() ? "" : ", " + fields) + "}";
    }

    /** Also writes elapsed to the right arm's X angle, to compare against. */
    private static final String ELAPSED_PROBE = "{\"driver\": \"core:axis_rotate\", \"bone\": \"rightArm\", \"axis\": \"X\", \"angle\": \"elapsed\", \"space\": \"OVERRIDE\"}";

    @Test
    void frameIsInTheClipsUnitsAndHoldsTheEnds() throws Exception
    {
        assertEquals(-30, xAngle(run(2, clip("\"frame\": 3")), "leftArm"), 0.05);
        assertEquals(-100, xAngle(run(2, clip("\"frame\": 25")), "leftArm"), 0.05, "past clipLength holds the last keyframe");
        assertEquals(0, xAngle(run(2, clip("\"frame\": -3")), "leftArm"), 0.05, "before 0 holds the first keyframe");
    }

    @Test
    void frameSeesClipLengthAndDuration() throws Exception
    {
        assertEquals(-50, xAngle(run(2, clip("\"frame\": {\"div\": [\"clipLength\", 2]}")), "leftArm"), 0.05);
        assertEquals(-60, xAngle(run(2, clip("\"duration\": 20, \"frame\": {\"mul\": [\"duration\", 0.3]}")), "leftArm"), 0.05);
        assertEquals(-70, xAngle(run(2, clip("\"frame\": {\"mod\": [-3, \"clipLength\"]}")), "leftArm"), 0.05, "mod is floored, so looping wraps negative frames");
    }

    @Test
    void defaultFrameFitsTheClipToTheDuration() throws Exception
    {
        FramePose fitted = run(5, clip("\"duration\": 40") + "," + ELAPSED_PROBE);
        float elapsed = xAngle(fitted, "rightArm");
        assertTrue(elapsed > 1, "elapsed grows: " + elapsed);
        assertEquals(-100 * elapsed / 40, xAngle(fitted, "leftArm"), 0.05, "elapsed / duration * clipLength");

        FramePose unitPerTick = run(5, clip("") + "," + ELAPSED_PROBE);
        assertEquals(-10 * xAngle(unitPerTick, "rightArm"), xAngle(unitPerTick, "leftArm"), 0.05, "without a duration, a unit per tick");
    }

    @Test
    void theClipIsFinishedOnceItsDurationHasPassed() throws Exception
    {
        String toDone = ", \"connections\": [{\"target\": \"done\", \"triggerCondition\": {\"type\": \"core:animation_finished\"}}]";
        String done = ", \"done\": {\"type\": \"core:pose\", \"pose\": [{\"driver\": \"core:axis_rotate\", \"bone\": \"rightLeg\", \"axis\": \"X\", \"angle\": -45, \"space\": \"OVERRIDE\"}]}";
        assertEquals(-45, xAngle(run(10, clip("\"duration\": 3"), toDone, done), "rightLeg"), 0.05);
        assertEquals(0, xAngle(run(10, clip(""), toDone, done), "rightLeg"), 0.05, "without a duration it never finishes");
    }

    @Test
    void aNegativeDurationIsAMistake()
    {
        MalformedKumoTemplateException e = assertThrows(MalformedKumoTemplateException.class, () -> run(1, clip("\"duration\": -1")));
        assertTrue(e.getMessage().contains("can't be negative"), e.getMessage());
    }

    private static float xAngle(FramePose frame, String bone)
    {
        float[] q = frame.bones.get(bone).rt;
        return (float) Math.toDegrees(2 * Math.atan2(q[0], q[3]));
    }

}
