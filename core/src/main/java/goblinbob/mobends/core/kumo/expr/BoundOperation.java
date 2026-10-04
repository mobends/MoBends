package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.EvalArgs;
import goblinbob.mobends.core.kumo.api.EvalContext;
import goblinbob.mobends.core.kumo.api.Evaluator;
import goblinbob.mobends.core.kumo.api.FloatArraySlot;
import goblinbob.mobends.core.kumo.api.FloatSlot;
import goblinbob.mobends.core.kumo.api.KumoOperation;
import goblinbob.mobends.core.kumo.api.NumberEvaluator;
import goblinbob.mobends.core.kumo.state.IKumoContext;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A use of a registered operation ({@link KumoOperation}), bound to the entity's class when the
 * animator loads. Its state lives here: an animator is compiled for each entity it animates.
 */
final class BoundOperation
{

    private BoundOperation()
    {
    }

    static Expression compile(KumoOperation operation, ExpressionOperations.Arguments args) throws MalformedKumoTemplateException
    {
        Binding binding = new Binding(args);
        Evaluator evaluator = operation.binder.bind(binding);
        Expression fallback = args.fallback();
        if (fallback != null && fallback.getType() != operation.returns)
        {
            throw new MalformedKumoTemplateException(String.format("'%s' is %s, but its \"@fallback\" is %s.", operation.name,
                    operation.returns.description, fallback.getType().description));
        }
        if (evaluator == null)
        {
            if (fallback == null)
            {
                Class<?> type = args.entityClass();
                throw new MalformedKumoTemplateException(String.format("'%s' doesn't apply to %s%s.", operation.name,
                        type == null ? "an entity of unknown class" : type.getName(),
                        operation.takesFallback ? " (and it has no \"@fallback\")" : ""));
            }
            return fallback;
        }
        boolean number = evaluator instanceof NumberEvaluator;
        boolean matches = number ? operation.returns == Expression.Type.NUMBER
                : evaluator instanceof BooleanEvaluator && operation.returns == Expression.Type.BOOLEAN;
        if (!matches)
        {
            throw new IllegalStateException("Operation '" + operation.name + "' returns " + operation.returns.description + ", but its binder made a "
                    + evaluator.getClass().getSimpleName() + ".");
        }
        Call call = number ? new NumberCall(args, binding, (NumberEvaluator) evaluator)
                : new BooleanCall(args, binding, (BooleanEvaluator) evaluator);
        if (operation.pure && binding.slots.isEmpty() && call.constantArguments())
        {
            // Same arguments, same result: computed once.
            return number ? Expression.constant(call.get(null)) : (call.test(null) ? Expression.TRUE : Expression.FALSE);
        }
        return (Expression) call;
    }

    /** What the binder gets, and the state it declares. */
    private static final class Binding implements BindArgs
    {
        private final ExpressionOperations.Arguments args;
        final List<DeclaredSlot> slots = new ArrayList<>();

        Binding(ExpressionOperations.Arguments args)
        {
            this.args = args;
        }

        @Override
        public int count()
        {
            return args.count();
        }

        @Override
        public String string(int index)
        {
            return args.string(index);
        }

        @Override
        public float constant(int index)
        {
            return args.constant(index);
        }

        @Nullable
        @Override
        public Class<?> entityClass()
        {
            return args.entityClass();
        }

        @Override
        public MalformedKumoTemplateException error(int index, String message)
        {
            return args.error(index, message);
        }

        @Override
        public FloatSlot slot(String name, float initial)
        {
            DeclaredSlot slot = new DeclaredSlot(1, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public FloatArraySlot slots(String name, int size, float initial)
        {
            DeclaredSlot slot = new DeclaredSlot(size, initial);
            slots.add(slot);
            return slot;
        }
    }

    /** The parts both kinds of call share: the arguments, evaluated into a reused view, and the state. */
    private interface Call
    {
        boolean constantArguments();

        float get(@Nullable ITriggerConditionContext context);

        boolean test(@Nullable ITriggerConditionContext context);
    }

    private static final class Frame implements EvalArgs, EvalContext
    {
        private final Expression[] expressions;
        private final float[] numbers;
        private final boolean[] booleans;
        private final List<DeclaredSlot> slots;
        private final boolean stateful;
        private ITriggerConditionContext context;

        Frame(ExpressionOperations.Arguments args, Binding binding)
        {
            int count = args.count();
            this.expressions = Arrays.copyOf(args.expressions(), count);
            this.numbers = new float[count];
            this.booleans = new boolean[count];
            for (int i = 0; i < count; i++)
            {
                // A constant argument reads as a number.
                if (expressions[i] == null) numbers[i] = args.constant(i);
            }
            this.slots = binding.slots;
            this.stateful = !slots.isEmpty() || anyStateful(expressions);
        }

        private static boolean anyStateful(Expression[] expressions)
        {
            for (Expression expression : expressions)
            {
                if (expression != null && expression.isStateful()) return true;
            }
            return false;
        }

        boolean constantArguments()
        {
            for (Expression expression : expressions)
            {
                if (expression != null && !expression.isConstant()) return false;
            }
            return true;
        }

        Frame evaluate(@Nullable ITriggerConditionContext context)
        {
            this.context = context;
            for (int i = 0; i < expressions.length; i++)
            {
                Expression expression = expressions[i];
                if (expression == null) continue;
                if (expression.getType() == Expression.Type.BOOLEAN) booleans[i] = expression.test(context);
                else numbers[i] = expression.get(context);
            }
            return this;
        }

        void restart(ITriggerConditionContext context)
        {
            for (DeclaredSlot slot : slots) slot.reset();
            for (Expression expression : expressions)
            {
                if (expression != null && expression.isStateful()) expression.restart(context);
            }
        }

        @Override
        public int count()
        {
            return numbers.length;
        }

        @Override
        public float number(int index)
        {
            return numbers[index];
        }

        @Override
        public boolean bool(int index)
        {
            return booleans[index];
        }

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
    }

    private static final class NumberCall extends Expression.NumberExpression implements Call
    {
        private final Frame frame;
        private final NumberEvaluator evaluator;

        NumberCall(ExpressionOperations.Arguments args, Binding binding, NumberEvaluator evaluator)
        {
            this.frame = new Frame(args, binding);
            this.evaluator = evaluator;
        }

        @Override
        public float get(@Nullable ITriggerConditionContext context)
        {
            Frame frame = this.frame.evaluate(context);
            return evaluator.evaluate(frame, frame);
        }

        @Override
        public boolean constantArguments()
        {
            return frame.constantArguments();
        }

        @Override
        public boolean isStateful()
        {
            return frame.stateful;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            frame.restart(context);
        }
    }

    private static final class BooleanCall extends Expression.BooleanExpression implements Call
    {
        private final Frame frame;
        private final BooleanEvaluator evaluator;

        BooleanCall(ExpressionOperations.Arguments args, Binding binding, BooleanEvaluator evaluator)
        {
            this.frame = new Frame(args, binding);
            this.evaluator = evaluator;
        }

        @Override
        public boolean test(@Nullable ITriggerConditionContext context)
        {
            Frame frame = this.frame.evaluate(context);
            return evaluator.evaluate(frame, frame);
        }

        @Override
        public boolean constantArguments()
        {
            return frame.constantArguments();
        }

        @Override
        public boolean isStateful()
        {
            return frame.stateful;
        }

        @Override
        public void restart(ITriggerConditionContext context)
        {
            frame.restart(context);
        }
    }

}
