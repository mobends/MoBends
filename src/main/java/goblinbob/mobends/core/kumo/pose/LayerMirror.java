package goblinbob.mobends.core.kumo.pose;

import goblinbob.mobends.core.kumo.state.condition.ITriggerCondition;
import goblinbob.mobends.core.kumo.state.condition.TriggerConditionRegistry;
import goblinbob.mobends.core.kumo.state.template.MalformedKumoTemplateException;
import goblinbob.mobends.core.kumo.state.template.MirrorTemplate;
import goblinbob.mobends.core.kumo.state.condition.ITriggerConditionContext;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runtime form of {@link MirrorTemplate}: the condition, the slot pairing and the negated variables. */
public class LayerMirror
{

    private final Skeleton skeleton;
    private final ITriggerCondition when;
    private final List<List<String>> pairs;
    private final Set<String> negated;
    private int[] pairOf = new int[0];

    public LayerMirror(Skeleton skeleton, MirrorTemplate template) throws MalformedKumoTemplateException
    {
        this.skeleton = skeleton;
        this.when = template.when == null ? null : TriggerConditionRegistry.instance.createFromTemplate(template.when);
        this.pairs = template.pairs == null ? Collections.emptyList() : template.pairs;
        this.negated = template.negate == null ? Collections.emptySet() : new HashSet<>(template.negate);
        for (List<String> pair : this.pairs)
        {
            if (pair == null || pair.size() != 2)
            {
                throw new MalformedKumoTemplateException("A mirror pair needs exactly two bone names.");
            }
            // Register the bones so both sides of every pair have a slot.
            skeleton.indexOf(pair.get(0));
            skeleton.indexOf(pair.get(1));
        }
    }

    public boolean isActive(ITriggerConditionContext context) throws MalformedKumoTemplateException
    {
        return when == null || when.isConditionMet(context);
    }

    public Set<String> getNegatedVariables()
    {
        return negated;
    }

    /** Slot -> its mirror slot (itself for unpaired bones); rebuilt when the skeleton grew. */
    public int[] pairing()
    {
        if (pairOf.length != skeleton.size())
        {
            pairOf = new int[skeleton.size()];
            for (int i = 0; i < pairOf.length; i++)
            {
                pairOf[i] = i;
            }
            for (List<String> pair : pairs)
            {
                int a = skeleton.indexOf(pair.get(0));
                int b = skeleton.indexOf(pair.get(1));
                pairOf[a] = b;
                pairOf[b] = a;
            }
        }
        return pairOf;
    }

}
