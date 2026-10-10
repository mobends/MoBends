package goblinbob.mobends.core.kumo.driver;

import goblinbob.mobends.core.kumo.api.BooleanInput;
import goblinbob.mobends.core.kumo.api.DoubleArraySlot;
import goblinbob.mobends.core.kumo.api.DoubleInput;
import goblinbob.mobends.core.kumo.api.DoubleSlot;
import goblinbob.mobends.core.kumo.api.DoubleStateHandle;
import goblinbob.mobends.core.kumo.api.DriverBindArgs;
import goblinbob.mobends.core.kumo.api.DriverEvaluator;
import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.api.FloatArraySlot;
import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.api.KumoDriver;
import goblinbob.mobends.core.kumo.api.NumberInput;
import goblinbob.mobends.core.kumo.api.PoseWriter;
import goblinbob.mobends.core.kumo.api.StateHandle;
import goblinbob.mobends.core.kumo.bind.IVectorSink;
import goblinbob.mobends.core.kumo.expr.DeclaredDoubleSlot;
import goblinbob.mobends.core.kumo.expr.DeclaredSlot;
import goblinbob.mobends.core.kumo.expr.DeclaredState;
import goblinbob.mobends.core.kumo.expr.Expression;
import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.expr.ExpressionTemplate;
import goblinbob.mobends.core.kumo.pose.IPoseItem;
import goblinbob.mobends.core.kumo.pose.Pose;
import goblinbob.mobends.core.kumo.pose.Skeleton;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.IKumoInstancingContext;
import goblinbob.mobends.core.kumo.state.StateRef;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.pose.DriverItemTemplate;
import goblinbob.mobends.core.math.Quaternion;
import goblinbob.mobends.core.math.vector.Vec3f;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A use of a registered driver ({@link KumoDriver}) as a pose item: its inputs are evaluated for
 * the frame, then its evaluator poses through a {@link PoseWriter}.
 */
final class BoundDriver implements IPoseItem, EvalContext, PoseWriter, DeclaredSlot.Access
{

    private final DriverEvaluator evaluator;
    private final List<Input> inputs;
    private final List<DeclaredState> slots;
    private ITriggerConditionContext context;
    private Pose pose;

    private BoundDriver(DriverEvaluator evaluator, Binding binding)
    {
        this.evaluator = evaluator;
        this.inputs = binding.inputs;
        this.slots = binding.slots;
    }

    static <T extends DriverItemTemplate> IPoseItem create(KumoDriver<T> driver, IKumoInstancingContext context, Skeleton skeleton, T template) throws MalformedKumoTemplateException
    {
        Binding binding = new Binding(driver.name, context.getExpressionScope(), skeleton);
        DriverEvaluator evaluator = driver.binder.bind(template, binding);
        return new BoundDriver(evaluator, binding);
    }

    @Override
    public void apply(Pose pose, IKumoContext context, float elapsedTicks)
    {
        this.context = context;
        this.pose = pose;
        // Every input, every frame: an edge trigger in one never misses a frame.
        for (Input input : inputs)
        {
            input.evaluate(context);
        }
        evaluator.evaluate(this, this);
    }

    @Override
    public void onNodeStarted(IKumoContext context)
    {
        this.context = context;
        for (DeclaredState slot : slots)
        {
            slot.reset(context.getState());
        }
        evaluator.restart(this);
    }

    @Override
    public void advance(IKumoContext context, float deltaTime)
    {
    }

    // --- EvalContext ------------------------------------------------------------------------------

    @Nullable
    @Override
    public Object entity()
    {
        return context == null ? null : context.getSubject().getEntity();
    }

    @Override
    public float deltaTime()
    {
        return context instanceof IKumoContext ? ((IKumoContext) context).getDeltaTime() : 0;
    }

    @Override
    public goblinbob.mobends.core.kumo.state.EntityState state()
    {
        return context.getState();
    }

    // --- PoseWriter -------------------------------------------------------------------------------

    @Override
    public void rotationSoFar(int bone, Quaternion dest)
    {
        pose.rotationSoFar(bone, dest);
    }

    @Override
    public void offsetSoFar(int bone, Vec3f dest)
    {
        pose.offsetSoFar(bone, dest);
    }

    @Override
    public void vectorSoFar(int bone, Vec3f dest)
    {
        pose.vectorSoFar(bone, dest);
    }

    @Override
    public void rotate(int bone, Quaternion rotation, Pose.Space space)
    {
        pose.composeRotation(bone, rotation, space);
    }

    @Override
    public void offset(int bone, float x, float y, float z, Pose.Space space)
    {
        pose.composeOffset(bone, x, y, z, space);
    }

    @Override
    public void vector(int bone, float x, float y, float z, Pose.Space space)
    {
        pose.composeVector(bone, x, y, z, space);
    }

    @Override
    public void snapRotation(int bone)
    {
        pose.get(bone).snap = true;
    }

    @Override
    public void snapVector(int bone)
    {
        pose.get(bone).vectorMode = IVectorSink.Mode.SNAP;
    }

    // --- binding ----------------------------------------------------------------------------------

    private interface Input
    {
        void evaluate(ITriggerConditionContext context);
    }

    private static final class ExpressionInput implements Input
    {
        private final Expression expression;
        /** A number or a double: a number is held exactly, and read back as the float it is. */
        private double number;
        private boolean bool;

        ExpressionInput(Expression expression)
        {
            this.expression = expression;
        }

        @Override
        public void evaluate(ITriggerConditionContext context)
        {
            if (expression.getType() == Expression.Type.BOOLEAN) bool = expression.test(context);
            else number = expression.getDouble(context);
        }
    }

    private static final class Binding implements DriverBindArgs
    {
        private final String driver;
        private final ExpressionScope scope;
        private final Skeleton skeleton;
        final List<Input> inputs = new ArrayList<>();
        final List<DeclaredState> slots = new ArrayList<>();

        Binding(String driver, ExpressionScope scope, Skeleton skeleton)
        {
            this.driver = driver;
            this.scope = scope;
            this.skeleton = skeleton;
        }

        @Nullable
        @Override
        public Class<?> entityClass()
        {
            return scope.getEntityClass();
        }

        @Override
        public MalformedKumoTemplateException error(String message)
        {
            return new MalformedKumoTemplateException("'" + driver + "' " + message);
        }

        private MalformedKumoTemplateException missing(String field)
        {
            return error("needs '" + field + "'.");
        }

        @Override
        public NumberInput number(String field, @Nullable ExpressionTemplate expression, float otherwise) throws MalformedKumoTemplateException
        {
            ExpressionInput input = new ExpressionInput(compile(field, expression, Expression.Type.NUMBER, Expression.constant(otherwise)));
            inputs.add(input);
            return context -> (float) input.number;
        }

        @Override
        public NumberInput number(String field, @Nullable ExpressionTemplate expression) throws MalformedKumoTemplateException
        {
            if (expression == null) throw missing(field);
            return number(field, expression, 0);
        }

        @Override
        public DoubleInput doubleNumber(String field, @Nullable ExpressionTemplate expression, double otherwise) throws MalformedKumoTemplateException
        {
            ExpressionInput input = new ExpressionInput(compile(field, expression, Expression.Type.DOUBLE, Expression.doubleConstant(otherwise)));
            inputs.add(input);
            return context -> input.number;
        }

        @Override
        public DoubleInput doubleNumber(String field, @Nullable ExpressionTemplate expression) throws MalformedKumoTemplateException
        {
            if (expression == null) throw missing(field);
            return doubleNumber(field, expression, 0);
        }

        @Override
        public BooleanInput bool(String field, @Nullable ExpressionTemplate expression, boolean otherwise) throws MalformedKumoTemplateException
        {
            ExpressionInput input = new ExpressionInput(compile(field, expression, Expression.Type.BOOLEAN, otherwise ? Expression.TRUE : Expression.FALSE));
            inputs.add(input);
            return context -> input.bool;
        }

        private Expression compile(String field, @Nullable ExpressionTemplate expression, Expression.Type type, Expression otherwise) throws MalformedKumoTemplateException
        {
            if (expression == null) return otherwise;
            try
            {
                return Expression.compile(expression.json, scope, type);
            }
            catch (MalformedKumoTemplateException e)
            {
                throw error("'" + field + "': " + e.getMessage());
            }
        }

        @Override
        public int bone(String field, @Nullable String name) throws MalformedKumoTemplateException
        {
            if (name == null) throw missing(field);
            return skeleton.indexOf(name);
        }

        @Override
        public StateHandle inout(String field, @Nullable String state) throws MalformedKumoTemplateException
        {
            return numberHandle(inoutState(field, state), "'" + driver + "'");
        }

        @Override
        public DoubleStateHandle doubleInout(String field, @Nullable String state) throws MalformedKumoTemplateException
        {
            return doubleHandle(inoutState(field, state), "'" + driver + "'");
        }

        private StateRef inoutState(String field, @Nullable String state) throws MalformedKumoTemplateException
        {
            if (state == null) throw error("needs '" + field + "' (the state it steps).");
            return scope.resolveState(state, "'" + driver + "'");
        }

        @Override
        public Outputs outputs(@Nullable Map<String, String> out, String... outputs) throws MalformedKumoTemplateException
        {
            // Every state the file names is resolved now; its type is checked as the driver gets it.
            Map<String, StateRef> states = new HashMap<>();
            for (Map.Entry<String, String> entry : (out == null ? Collections.<String, String>emptyMap() : out).entrySet())
            {
                if (!Arrays.asList(outputs).contains(entry.getKey()))
                {
                    throw error(String.format("has no output '%s' (it has %s).", entry.getKey(), String.join(", ", outputs)));
                }
                states.put(entry.getKey(), scope.resolveState(entry.getValue(), outputName(entry.getKey())));
            }
            return new Outputs()
            {
                @Nullable
                @Override
                public StateHandle get(String output) throws MalformedKumoTemplateException
                {
                    StateRef ref = states.get(output);
                    return ref == null ? null : numberHandle(ref, outputName(output));
                }

                @Nullable
                @Override
                public DoubleStateHandle getDouble(String output) throws MalformedKumoTemplateException
                {
                    StateRef ref = states.get(output);
                    return ref == null ? null : doubleHandle(ref, outputName(output));
                }
            };
        }

        private String outputName(String output)
        {
            return "'" + driver + "''s output '" + output + "'";
        }

        private static void requireType(StateRef ref, Expression.Type type, String what) throws MalformedKumoTemplateException
        {
            if (ref.type != type)
            {
                throw new MalformedKumoTemplateException(String.format("%s writes '%s', which is %s: it writes %ss.", what, ref.name, ref.type.description,
                        type == Expression.Type.DOUBLE ? "double" : "number"));
            }
        }

        private static StateHandle numberHandle(StateRef ref, String what) throws MalformedKumoTemplateException
        {
            requireType(ref, Expression.Type.NUMBER, what);
            return new StateHandle()
            {
                @Override
                public float get(EvalContext context)
                {
                    return (float) ref.get(((BoundDriver) context).context);
                }

                @Override
                public void set(EvalContext context, float value)
                {
                    ref.set(value, ((BoundDriver) context).context);
                }
            };
        }

        private static DoubleStateHandle doubleHandle(StateRef ref, String what) throws MalformedKumoTemplateException
        {
            requireType(ref, Expression.Type.DOUBLE, what);
            return new DoubleStateHandle()
            {
                @Override
                public double get(EvalContext context)
                {
                    return ref.get(((BoundDriver) context).context);
                }

                @Override
                public void set(EvalContext context, double value)
                {
                    ref.set(value, ((BoundDriver) context).context);
                }
            };
        }

        @Override
        public FloatSlot slot(String name, float initial)
        {
            DeclaredSlot slot = new DeclaredSlot(scope.getLayout(), 1, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public FloatArraySlot slots(String name, int size, float initial)
        {
            DeclaredSlot slot = new DeclaredSlot(scope.getLayout(), size, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public DoubleSlot doubleSlot(String name, double initial)
        {
            DeclaredDoubleSlot slot = new DeclaredDoubleSlot(scope.getLayout(), 1, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public DoubleArraySlot doubleSlots(String name, int size, double initial)
        {
            DeclaredDoubleSlot slot = new DeclaredDoubleSlot(scope.getLayout(), size, initial);
            slots.add(slot);
            return slot;
        }
    }

}
