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

import java.util.ArrayList;
import java.util.List;
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

    // The names a test sets, numbered as animators look them up. A name set after the animator
    // has bound to the subject isn't seen.
    private final List<String> variableNames = new ArrayList<>();
    private final List<String> stateNames = new ArrayList<>();

    @Override
    public int indexOfVariable(String name)
    {
        return indexOf(variableNames, variables.containsKey(name), name);
    }

    @Override
    public double getVariable(int index)
    {
        return variables.get(variableNames.get(index));
    }

    @Override
    public int indexOfState(String name)
    {
        return indexOf(stateNames, states.containsKey(name), name);
    }

    @Override
    public boolean getState(int index)
    {
        return states.get(stateNames.get(index));
    }

    private static int indexOf(List<String> names, boolean exists, String name)
    {
        if (!exists)
        {
            return -1;
        }
        int index = names.indexOf(name);
        if (index < 0)
        {
            index = names.size();
            names.add(name);
        }
        return index;
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
