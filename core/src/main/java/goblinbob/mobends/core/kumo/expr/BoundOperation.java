package goblinbob.mobends.core.kumo.expr;

import goblinbob.mobends.core.kumo.api.BindArgs;
import goblinbob.mobends.core.kumo.api.BooleanEvaluator;
import goblinbob.mobends.core.kumo.api.DoubleArraySlot;
import goblinbob.mobends.core.kumo.api.DoubleEvaluator;
import goblinbob.mobends.core.kumo.api.DoubleSlot;
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
        if (args.inSelector() && !operation.selectorSafe)
        {
            throw new MalformedKumoTemplateException(String.format("'%s' can't be in a type file's selector: it reads the entity's data, which doesn't exist yet.", operation.name));
        }
        Binding binding = new Binding(args);
        Evaluator evaluator = operation.binder.bind(binding);
        Expression fallback = args.fallback() == null ? null : Expression.adopt(args.fallback(), operation.returns);
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
        Call call;
        if (evaluator instanceof NumberEvaluator && operation.returns == Expression.Type.NUMBER)
        {
            call = new NumberCall(args, binding, (NumberEvaluator) evaluator);
        }
        else if (evaluator instanceof DoubleEvaluator && operation.returns == Expression.Type.DOUBLE)
        {
            call = new DoubleCall(args, binding, (DoubleEvaluator) evaluator);
        }
        else if (evaluator instanceof BooleanEvaluator && operation.returns == Expression.Type.BOOLEAN)
        {
            call = new BooleanCall(args, binding, (BooleanEvaluator) evaluator);
        }
        else
        {
            throw new IllegalStateException("Operation '" + operation.name + "' returns " + operation.returns.description + ", but its binder made a "
                    + evaluator.getClass().getSimpleName() + ".");
        }
        if (operation.pure && binding.slots.isEmpty() && call.constantArguments())
        {
            // Same arguments, same result: computed once.
            switch (operation.returns)
            {
                case NUMBER: return Expression.constant(((Expression) call).get(null));
                case DOUBLE: return Expression.doubleConstant(((Expression) call).getDouble(null));
                default: return ((Expression) call).test(null) ? Expression.TRUE : Expression.FALSE;
            }
        }
        return (Expression) call;
    }

    /** What the binder gets, and the state it declares. */
    private static final class Binding implements BindArgs
    {
        private final ExpressionOperations.Arguments args;
        final List<DeclaredState> slots = new ArrayList<>();

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
            DeclaredSlot slot = new DeclaredSlot(args.layout(), 1, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public FloatArraySlot slots(String name, int size, float initial)
        {
            DeclaredSlot slot = new DeclaredSlot(args.layout(), size, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public DoubleSlot doubleSlot(String name, double initial)
        {
            DeclaredDoubleSlot slot = new DeclaredDoubleSlot(args.layout(), 1, initial);
            slots.add(slot);
            return slot;
        }

        @Override
        public DoubleArraySlot doubleSlots(String name, int size, double initial)
        {
            DeclaredDoubleSlot slot = new DeclaredDoubleSlot(args.layout(), size, initial);
            slots.add(slot);
            return slot;
        }
    }

    /** The part every kind of call shares: the arguments, evaluated into a reused view, and the state. */
    private interface Call
    {
        boolean constantArguments();
    }

    private static final class Frame implements EvalArgs, EvalContext, DeclaredSlot.Access
    {
        private final Expression[] expressions;
        /** The number and double arguments: a number is held exactly, and read back as the float it is. */
        private final double[] numbers;
        private final boolean[] booleans;
        private final List<DeclaredState> slots;
        private final boolean stateful;
        private ITriggerConditionContext context;

        Frame(ExpressionOperations.Arguments args, Binding binding)
        {
            int count = args.count();
            this.expressions = Arrays.copyOf(args.expressions(), count);
            this.numbers = new double[count];
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
                else numbers[i] = expression.getDouble(context);
            }
            return this;
        }

        void restart(ITriggerConditionContext context)
        {
            for (DeclaredState slot : slots) slot.reset(context.getState());
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
            return (float) numbers[index];
        }

        @Override
        public double doubleNumber(int index)
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

        @Override
        public goblinbob.mobends.core.kumo.state.EntityState state()
        {
            return context.getState();
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

    private static final class DoubleCall extends Expression.DoubleExpression implements Call
    {
        private final Frame frame;
        private final DoubleEvaluator evaluator;

        DoubleCall(ExpressionOperations.Arguments args, Binding binding, DoubleEvaluator evaluator)
        {
            this.frame = new Frame(args, binding);
            this.evaluator = evaluator;
        }

        @Override
        public double getDouble(@Nullable ITriggerConditionContext context)
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
