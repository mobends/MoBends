package goblinbob.mobends.test.core.kumo;

import com.google.gson.Gson;
import goblinbob.mobends.core.animation.keyframe.KeyframeAnimation;
import goblinbob.mobends.core.kumo.IKumoSubject;
import goblinbob.mobends.core.kumo.KumoSerializer;
import goblinbob.mobends.core.kumo.bind.IBoneSink;
import goblinbob.mobends.core.kumo.bind.OrientationSink;
import goblinbob.mobends.core.kumo.bind.VectorSink;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.KumoAnimatorState;
import goblinbob.mobends.core.kumo.state.template.AnimatorTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.SmoothOrientation;
import goblinbob.mobends.core.math.vector.SmoothVector3f;
import goblinbob.mobends.core.math.vector.Vec3f;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/** A subject with plain rotation bones and settable variables, and helpers to run animators on it. */
class TestSubject implements IKumoSubject
{

    /**
     * The bare names the tests' subjects provide, declared as the host declares its built-ins: a
     * bare name that isn't a built-in fails the animator when it loads.
     */
    static void declareNames()
    {
        for (String name : new String[] { "height", "speed", "sinceAttack", "x", "yaw", "limit", "fall" })
        {
            Expression.registerSubjectBuiltIn(name, Expression.Type.NUMBER);
        }
        for (String name : new String[] { "worldX", "worldZ" })
        {
            Expression.registerSubjectBuiltIn(name, Expression.Type.DOUBLE);
        }
        for (String name : new String[] { "GO", "OUT", "IN", "LEFT_HANDED", "NEVER", "AIRBORNE", "SWORD", "LATE", "ALWAYS", "FLYING", "STILL", "STANDING_STILL" })
        {
            Expression.registerSubjectBuiltIn(name, Expression.Type.BOOLEAN);
        }
    }

    static
    {
        declareNames();
    }

    final Map<String, SmoothOrientation> bones = new HashMap<>();
    /** Each bone's offset, and its smoothing (see {@link #updateOffsets}). */
    final Map<String, Vec3f> offsets = new HashMap<>();
    final Map<String, SmoothVector3f> offsetSmoothings = new HashMap<>();
    /** The entity-level root vector. */
    final SmoothVector3f root = new SmoothVector3f();
    final Map<String, Double> variables = new HashMap<>();
    final Map<String, Boolean> states = new HashMap<>();
    /** What {@code field} reads. */
    Object entity;

    TestSubject(String... boneNames)
    {
        for (String name : boneNames)
        {
            bones.put(name, new SmoothOrientation());
            offsets.put(name, new Vec3f());
            offsetSmoothings.put(name, new SmoothVector3f());
        }
    }

    /** Moves the offsets along their smoothing, as a model part's update does. */
    void updateOffsets(float ticksPerFrame)
    {
        for (Map.Entry<String, SmoothVector3f> entry : offsetSmoothings.entrySet())
        {
            SmoothVector3f smoothing = entry.getValue();
            if (smoothing.isMoving())
            {
                smoothing.update(ticksPerFrame);
                offsets.get(entry.getKey()).set(smoothing.getX(), smoothing.getY(), smoothing.getZ());
            }
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
        if ("root".equals(name))
        {
            return new VectorSink(root);
        }
        SmoothOrientation bone = bones.get(name);
        return bone == null ? null : new OrientationSink(bone, offsets.get(name), offsetSmoothings.get(name));
    }

    @Override
    public Object getEntity()
    {
        return entity;
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
