package goblinbob.mobends.core.kumo.state;

import goblinbob.mobends.core.kumo.expr.ExpressionScope;
import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.BranchTemplate;
import goblinbob.mobends.core.kumo.state.template.ConnectionTemplate;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A machine's selector: an ordered decision tree over the machine's own members. The first branch
 * whose condition holds is taken, and a taken branch decides inside it: if none of its own branches
 * holds, the selector chooses nothing.
 */
public class Selector
{

    private final List<Branch> branches;

    private Selector(List<Branch> branches)
    {
        this.branches = branches;
    }

    /**
     * @param machine the machine the selector belongs to: every branch has to lead to one of its own members
     * @param scope   the named expressions and conditions the branches' conditions see
     */
    public static Selector create(List<BranchTemplate> templates, MachineState machine, Map<String, MachineMember> membersByName, ExpressionScope scope) throws MalformedKumoTemplateException
    {
        return new Selector(createBranches(templates, machine, membersByName, scope, 0F, ConnectionTemplate.Easing.EASE_IN_OUT, null));
    }

    private static List<Branch> createBranches(List<BranchTemplate> templates, MachineState machine, Map<String, MachineMember> membersByName, ExpressionScope scope,
                                               float duration, ConnectionTemplate.Easing easing, @Nullable Map<String, Float> set) throws MalformedKumoTemplateException
    {
        List<Branch> branches = new ArrayList<>();
        for (BranchTemplate template : templates)
        {
            if (template == null)
            {
                throw new MalformedKumoTemplateException(String.format("A selector of %s has a null branch.", machine.describe()));
            }
            ITriggerCondition when = template.when == null ? null : TriggerConditionRegistry.INSTANCE.createFromTemplate(template.when, scope);
            float branchDuration = template.transitionDuration == null ? duration : template.transitionDuration;
            ConnectionTemplate.Easing branchEasing = template.transitionEasing == null ? easing : template.transitionEasing;
            Map<String, Float> branchSet = set;
            if (template.set != null && !template.set.isEmpty())
            {
                branchSet = set == null ? new HashMap<String, Float>() : new HashMap<>(set);
                branchSet.putAll(template.set);
            }

            if (template.branches != null)
            {
                branches.add(new Branch(when, null, createBranches(template.branches, machine, membersByName, scope, branchDuration, branchEasing, branchSet),
                                        branchDuration, branchEasing, branchSet));
                continue;
            }
            MachineMember target = membersByName.get(template.target);
            if (target == null)
            {
                throw new MalformedKumoTemplateException(String.format("A selector branch leads to '%s', which is no node or machine of the layer.", template.target));
            }
            if (target.parent != machine)
            {
                throw new MalformedKumoTemplateException(String.format("A selector branch of %s leads to '%s', which isn't one of its own nodes or machines.", machine.describe(), template.target));
            }
            branches.add(new Branch(when, target, null, branchDuration, branchEasing, branchSet));
        }
        return branches;
    }

    /**
     * The branch the selector takes (one that leads to a member), or null if it chooses nothing.
     * Every condition of the tree is evaluated, whatever is chosen, so edge triggers stay fresh.
     */
    @Nullable
    public Branch choose(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        return choose(branches, context);
    }

    @Nullable
    private static Branch choose(List<Branch> branches, ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        Branch chosen = null;
        boolean decided = false;
        for (Branch branch : branches)
        {
            boolean met = branch.when == null || branch.when.isConditionMet(context);
            Branch inner = branch.branches == null ? branch : choose(branch.branches, context);
            if (met && !decided)
            {
                decided = true;
                chosen = inner;
            }
        }
        return chosen;
    }

    /** Starts the conditions with a clock or a memory over: the selector's machine was entered. */
    public void start(ITriggerConditionContext context)
    {
        start(branches, context);
    }

    private static void start(List<Branch> branches, ITriggerConditionContext context)
    {
        for (Branch branch : branches)
        {
            if (branch.when != null)
            {
                branch.when.onNodeStarted(context);
            }
            if (branch.branches != null)
            {
                start(branch.branches, context);
            }
        }
    }

    /** A branch; one that leads to a member is a transition, with what its enclosing branches set filled in. */
    public static final class Branch implements ITransition
    {

        @Nullable
        private final ITriggerCondition when;
        /** The member it leads to, or null for a branch with branches of its own. */
        @Nullable
        private final MachineMember target;
        @Nullable
        private final List<Branch> branches;
        private final float duration;
        private final ConnectionTemplate.Easing easing;
        @Nullable
        private final Map<String, Float> set;

        private Branch(@Nullable ITriggerCondition when, @Nullable MachineMember target, @Nullable List<Branch> branches,
                       float duration, ConnectionTemplate.Easing easing, @Nullable Map<String, Float> set)
        {
            this.when = when;
            this.target = target;
            this.branches = branches;
            this.duration = duration;
            this.easing = easing;
            this.set = set == null ? null : Collections.unmodifiableMap(set);
        }

        @Override
        public MachineMember getTarget()
        {
            return target;
        }

        @Override
        public float getDuration()
        {
            return duration;
        }

        @Override
        public ConnectionTemplate.Easing getEasing()
        {
            return easing;
        }

        @Nullable
        @Override
        public Map<String, Float> getSet()
        {
            return set;
        }

    }

}
