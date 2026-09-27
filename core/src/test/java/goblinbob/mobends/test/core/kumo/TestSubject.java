package goblinbob.mobends.test.core.kumo;

import com.google.gson.Gson;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.OrientationSink;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.HashMap;
import java.util.Map;

/** A subject with plain rotation bones and settable variables, and helpers to run animators on it. */
class TestSubject implements IKumoSubject
{

    final Map<String, SmoothOrientation> bones = new HashMap<>();
    final Map<String, Double> variables = new HashMap<>();
    final Map<String, Boolean> states = new HashMap<>();

    TestSubject(String... boneNames)
    {
        for (String name : boneNames)
        {
            bones.put(name, new SmoothOrientation());
        }
    }

    /** The rotation the bone is heading towards. */
    Quaternion target(String bone)
    {
        return bones.get(bone).getEnd();
    }

    @Override
    public IBoneSink getBone(String name)
    {
        SmoothOrientation bone = bones.get(name);
        return bone == null ? null : new OrientationSink(bone, new Vec3f());
    }

    @Override
    public double getVariable(String name)
    {
        Double value = variables.get(name);
        if (value == null)
        {
            throw new IllegalArgumentException("Unknown variable: " + name);
        }
        return value;
    }

    @Override
    public boolean getState(String name)
    {
        Boolean value = states.get(name);
        if (value == null)
        {
            throw new IllegalArgumentException("Unknown state: " + name);
        }
        return value;
    }

    static AnimatorTemplate animator(String json)
    {
        return KumoSerializer.INSTANCE.gson.fromJson(json, AnimatorTemplate.class);
    }

    static KumoAnimatorState instance(String json, Map<String, String> clips) throws MalformedKumoTemplateException
    {
        Map<String, KeyframeAnimation> animations = new HashMap<>();
        for (Map.Entry<String, String> clip : clips.entrySet())
        {
            animations.put(clip.getKey(), new Gson().fromJson(clip.getValue(), KeyframeAnimation.class));
        }
        IKumoInstancingContext context = animations::get;
        return new KumoAnimatorState(animator(json), context);
    }

    static KumoAnimatorState instance(String json) throws MalformedKumoTemplateException
    {
        return instance(json, new HashMap<>());
    }

    static Quaternion axisAngle(float x, float y, float z, float degrees)
    {
        Quaternion q = new Quaternion();
        q.setFromAxisAngle(x, y, z, (float) Math.toRadians(degrees));
        return q;
    }

}
